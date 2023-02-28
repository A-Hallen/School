package com.hallen.school.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.hallen.school.R
import com.hallen.school.databinding.FragmentCreateGroupBinding
import com.hallen.school.model.group.InfiniteAdapter
import com.hallen.school.model.group.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateGroup(private val loadGroups: () -> Unit, private val groupName: String = "") : Fragment() {
    private lateinit var binding: FragmentCreateGroupBinding
    private lateinit var adapter: InfiniteAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var data: ArrayList<Student> = arrayListOf()
    private var isLoading = false
    private var isLastPage = false
    private var currentPage = 0
    private var auth: FirebaseUser? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var groupsRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        auth = FirebaseAuth.getInstance().currentUser
        database = FirebaseDatabase.getInstance("https://school-e8de3-default-rtdb.firebaseio.com/")
        groupsRef = database.getReference("usuarios").child(auth!!.uid).child("groups")
        binding = FragmentCreateGroupBinding.inflate(inflater, container, false); return binding.root
    }

    fun updateData(group1Ref: DatabaseReference){
        //Guardar en la base de datos

        val studentsUnordered = arrayListOf<Student>()
        for(datos in adapter.data){
            if (datos.name.isNotBlank()) {
                datos.name.replaceFirstChar { it.uppercase() }
                studentsUnordered.add(datos)
            }
        }
        val students = studentsUnordered.sortedBy { it.name }
        val studentsMap = mutableMapOf<String, Any>()  // Crear un mapa de todos los estudiantes
        for (student in students) {
            studentsMap[group1Ref.push().key!!] = mapOf(
                "name" to student.name, "movil" to student.movil
            )
        }

        // Ejecutar la transacción para actualizar los datos en la base de datos
        group1Ref.setValue(studentsMap).addOnSuccessListener {
            parentFragmentManager.beginTransaction().hide(this@CreateGroup).commit()
            loadGroups()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "A ocurrido un error al guardar el grupo en la base de datos", Toast.LENGTH_SHORT).show()
            it.printStackTrace()
        }

        /*group1Ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                currentData.value = studentsMap; return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                // Datos enviados correctamente
                if (error != null) {
                    Toast.makeText(requireContext(), "A ocurrido un error", Toast.LENGTH_SHORT).show()
                    // Manejar el error
                } else {
                    Log.i("Hallen", studentsMap.toString())
                    parentFragmentManager.beginTransaction().hide(this@CreateGroup).commit()
                    loadGroups()
                }
            }
        })*/

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (groupName != "") loadStudents() else {
            for (i in 0..10){
                data.add(Student(""))
            }
        }
        adapter = InfiniteAdapter(data)
        layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadMoreItems()
                    }
                }
            }
        })
        binding.guardar.setOnClickListener {
            // Preguntar el nombre del grupo
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_group_name)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val groupNameEditText = dialog.findViewById<EditText>(R.id.dialog_group_name)
            groupNameEditText.setText(groupName)

            val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            val okButton = dialog.findViewById<Button>(R.id.ok_button)
            okButton.setOnClickListener {
                if (groupNameEditText.text.isBlank()) {
                    Toast.makeText(requireContext(), "El nombre del grupo no debe estar vacío", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val group1Ref = groupsRef.child(groupNameEditText.text.toString())
                group1Ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                            val dialogYesOrNot = Dialog(requireContext())
                            dialogYesOrNot.setContentView(R.layout.yes_or_no_dialog)
                            dialogYesOrNot.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            dialogYesOrNot.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            val textWarning = dialogYesOrNot.findViewById<TextView>(R.id.dialog_text_warning)
                            textWarning.text = "Deseas sobreescribir el grupo: ${groupName}?"
                            val cancelButtonNot = dialogYesOrNot.findViewById<TextView>(R.id.cancel_button)
                            cancelButtonNot.setOnClickListener {
                                dialog.dismiss()
                            }
                            val aceptButton = dialogYesOrNot.findViewById<TextView>(R.id.ok_button)
                            aceptButton.setOnClickListener {
                                // El usuario a aceptado sobreescribir los datos asi que ...
                                //Guardar en la base de datos
                                updateData(group1Ref)
                                dialogYesOrNot.dismiss()
                            }
                            dialogYesOrNot.show()
                        } else {
                            updateData(group1Ref)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {    }
                })
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun loadStudents() {
        val group = groupsRef.child(groupName)
        group.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() and  snapshot.hasChildren()){
                    for (childSnapshot in snapshot.children) {
                        val movil = childSnapshot.child("movil").getValue(String::class.java) // Obtiene el email del grupo
                        val name = childSnapshot.child("name").getValue(String::class.java) // Obtiene el nombre del grupo
                        val student = Student(name ?: "", movil ?: "")
                        data.add(student)
                        adapter.notifyItemInserted(data.size - 1)

                        // Realiza la lógica necesaria con los datos obtenidos
                        // hacer algo con los datos obtenidos
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "A ocurrido un error, no se pueden cargar los datos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadMoreItems() {
        isLoading = true
        currentPage++

        // Aquí se llamaría a una función que carga más datos en background
        // Una vez que se cargan los nuevos datos, se llamaría a la función "onLoadMoreItemsComplete"
        // con los nuevos datos.
        CoroutineScope(Dispatchers.IO).launch {
            val data = arrayListOf<Student>()
            for (i in 0..10){
                data.add(Student(""))
            }
            withContext(Dispatchers.Main) {
                adapter.addData(data)
                isLoading = false
            }
        }
    }
}