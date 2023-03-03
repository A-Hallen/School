package com.hallen.school.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.hallen.school.R
import com.hallen.school.databinding.FragmentNotasBinding
import com.hallen.school.ui.welcome.notas.Nota
import com.hallen.school.ui.welcome.notas.NotasAdapter


class FragmentNotas : Fragment() {
   private lateinit var binding: FragmentNotasBinding
   private lateinit var adapter: NotasAdapter
   private lateinit var database: FirebaseDatabase
   private var auth: FirebaseUser? = null
   private lateinit var notasRef: DatabaseReference
   private var actualItemPosition: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance().currentUser
        database = FirebaseDatabase.getInstance("https://school-e8de3-default-rtdb.firebaseio.com/")
        notasRef = database.getReference("usuarios").child(auth!!.uid).child("notas")

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Maneja el evento de presionar atrás aquí
                // Por ejemplo, para volver a la actividad principal:
                if(binding.editNote.visibility == View.VISIBLE){
                    backNotas()
                }
            }
        })

        binding = FragmentNotasBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            // Create a new NotasAdapter object and pass in the context and an empty arraylist
            adapter = NotasAdapter(requireContext(), arrayListOf())
            // Set the layout manager of the notasRecyclerView to a LinearLayoutManager
            binding.notasRecyclerView.layoutManager = LinearLayoutManager(context)
            // Set the adapter of the notasRecyclerView to the adapter created above
            binding.notasRecyclerView.adapter = adapter

            loadNotas() // load the notes
            listeners() // load all the listeners
        }

    /**
     * Loads all the notes in the database
     */
     private fun loadNotas() {
            // Get a reference to the notas database
            notasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                // When the data is loaded
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Create an array list of notas
                    val notas = arrayListOf<Pair<String, Nota>>()
                    // If the snapshot exists and has children
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        // For each child in the snapshot
                        for (childSnapShot in snapshot.children) {
                            // Get childSnapShot key
                            val key = childSnapShot.key ?: continue
                            // Get the value of the child as a Nota object
                            childSnapShot.getValue(Nota::class.java)?.let {
                                // Add the Nota object to the array list
                                notas.add(key to it)
                            }
                        }
                    }
                    // Set the adapter's notas to the array list
                    adapter.setNotas(notas)
                }
                // If an error occurs
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(),
                        "A ocurrido un error al cargas las notas desde la base de datos",
                        Toast.LENGTH_SHORT).show()
                }
            })
        }

    /**
     * Set all the listeners of the fragment
     */
    private fun listeners() {

        // Create new note
        binding.newNote.setOnClickListener {    actualItemPosition = null; loadNotasEdit()   }

        binding.notasBack.setOnClickListener {
            // Make the edit note header and content invisible
            backNotas()
        }

        binding.guardarNota.setOnClickListener {
            // Get the title and content of the note
            var title = binding.editNoteTitle.text.toString()
            val content = binding.editNoteContent.text.toString()
            // If the content is blank, return
            if (content.isBlank()) {
                Toast.makeText(requireContext(),
                    "El contenido no debe estar vacio",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // If the title is blank, set the title to the first 20 characters of the content
            if (title.isBlank()){
                title = if (content.length <= 20){   content   } else content.substring(0, 20)
            }
            // Get the size of the adapter
            val size = if (actualItemPosition == null) adapter.getSize() else actualItemPosition!!
            // Get the key of the note if already exists
            val key = if (actualItemPosition != null){
                adapter.getItemKey(actualItemPosition!!)
            } else null

            // Save the note
            guardarNota(size, title, content, key)
        }

        adapter.setOnItemClickListener(object : NotasAdapter.OnItemClickListenr{
            override fun onItemClick(item: Pair<String, Nota>, adapterPosition: Int) {
                actualItemPosition = adapterPosition
                loadNotasEdit()
                binding.editNoteTitle.setText(item.second.title)
                binding.editNoteContent.setText(item.second.content)
            }
        })
        adapter.setOnItemLongClickListener(object : NotasAdapter.OnItemLongClickListener{
            override fun onItemLongClick(mapNota: Pair<String, Nota>, adapterPosition: Int, view: View) {
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.menuInflater.inflate(R.menu.simple_delete_menu, popupMenu.menu)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener {
                    deleteNote(mapNota, adapterPosition)
                    true
                }
            }

        })
    }

    private fun deleteNote(mapNota: Pair<String, Nota>, adapterPosition: Int) {

        notasRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val notas = arrayListOf<Pair<String, Nota>>()

                if (snapshot.exists() and snapshot.hasChildren()){
                    notasRef.child(mapNota.first).setValue(null) // eliminamos el elemento de la base de datos.
                    for (childSnapShot in snapshot.children){
                        val notaKey = childSnapShot.key ?: continue
                        val nota = childSnapShot.getValue(Nota::class.java) ?: continue
                        val index = nota.index
                        if (index > mapNota.second.index){
                            nota.index = nota.index - 1
                            childSnapShot.ref.setValue(nota)
                        }
                        notas.add(notaKey to nota)
                    }
                    adapter.deleteItem(adapterPosition, notas)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun backNotas() {
        binding.editHeader.visibility = View.GONE
        binding.editNote.visibility   = View.GONE
        // Make the new note button visible
        binding.newNote.visibility = View.VISIBLE
    }


    /**
    *  shows the edit mode of the notes
    */
    private fun loadNotasEdit() {
        // Load the animation for the edit note header and content
        val animacionContent: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.apears_edit_note)
        val animacionHeader:  Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.appears_edit_note_header)
        // Make the edit note header and content visible
        binding.editHeader.visibility = View.VISIBLE
        binding.editNote.visibility   = View.VISIBLE
        // Start the animation for the edit note header and content
        binding.editHeader.startAnimation(animacionHeader)
        binding.editNote.startAnimation(animacionContent)
        // Make the new note button invisible
        binding.newNote.visibility = View.GONE
    }

    /**
    *  Function to save a note
    *  @param index The index of the note
    *  @param title The title of the note
    *  @param content The content of the note
    *  @param key (optional) the key for store in the database
    */
    private fun guardarNota(index: Int, title: String, content: String, key: String? = null) {
        //Create a new note object
        val nota = Nota(
            index = index,
            title = title,
            content = content,
        )
        //Get the key for the note
        val notasKey = key ?: (notasRef.push().key ?: return)
        //Set the value of the note in the database
        notasRef.child(notasKey).setValue(nota)
        //Add the note to the adapter
        if (key == null){
            adapter.addNota(notasKey to nota)
        } else adapter.updateNota(notasKey to nota)
        backNotas()
    }

}