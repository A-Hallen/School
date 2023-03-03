package com.hallen.school

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.hallen.school.databinding.ActivityResultBinding
import com.hallen.school.model.group.AsistancePageAdapter
import com.hallen.school.ui.CircularImageView
import com.hallen.school.ui.CreateGroup
import com.hallen.school.ui.GroupV
import com.hallen.school.ui.welcome.PagerAdapter
import java.io.ByteArrayOutputStream
import java.io.InputStream


class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var groupsRef: DatabaseReference
    private lateinit var homeAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance("https://school-e8de3-default-rtdb.firebaseio.com/")
        groupsRef = database.getReference("usuarios").child(auth.uid!!).child("groups")
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        homeAdapter = PagerAdapter(supportFragmentManager, lifecycle)
        loadHome()
        setListeners()
        loadAvatar()
        loadGroups()
    }

    private fun loadHome() {
        val fragmentsArray = arrayOf("Horario", "Eventos", "Notas")
        binding.welcomeScreen.pager.adapter = homeAdapter
        TabLayoutMediator(binding.welcomeScreen.tabLayout, binding.welcomeScreen.pager){  tab, position ->
            tab.setCustomView(R.layout.tab_custom_view)
            tab.text = fragmentsArray[position]
        }.attach()
        //binding.welcomeScreen.pager.setPageTransformer(PageTransformer())
    }

    private fun displayGroup(groupKey: String) {
        val group = groupsRef.child(groupKey)
        val fragmentsArray = arrayOf("Grupo", "Registro", "Gráficas")
        val pagerAdapter = AsistancePageAdapter(supportFragmentManager, lifecycle, group)

        binding.welcomeScreen.pager.adapter = pagerAdapter
        val tabLayout = binding.welcomeScreen.tabLayout

        TabLayoutMediator(tabLayout, binding.welcomeScreen.pager){  tab, position ->
            tab.setCustomView(R.layout.tab_custom_view)
            tab.text = fragmentsArray[position]
        }.attach()
    }

    private fun loadAvatar() {
        val user = auth.currentUser
        val displayName = user?.displayName
        val photoUri = user?.photoUrl
        binding.nickName.text = displayName
        Glide.with(this).load(photoUri).centerCrop().placeholder(R.drawable.ic_avatar).into(binding.avatar.imageView)
        Glide.with(this).load(photoUri).centerCrop().placeholder(R.drawable.ic_avatar).into(binding.topBar.avatarMini.imageView)
    }

    private fun setListeners() {
        binding.horarioNav.setOnClickListener{ loadHome() }
        binding.topBar.avatarMini.setOnClickListener { avatarMiniMenu(it as CircularImageView) }
        binding.topBar.hamburgerIcon.setOnClickListener {   binding.root.openDrawer(GravityCompat.START)    }
        binding.avatar.setOnClickListener { pickImageFromGallery() }
        binding.btnAgregarGrupo.setOnClickListener {
            createGroup()
        }
    }

    private fun avatarMiniMenu(view: CircularImageView) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.avatar_mini)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.logg_out ->{
                    auth.signOut()
                    startActivity(Intent(this@ResultActivity, MainActivity::class.java))
                }
            }
            true
        }
    }

    val groupMenuListener = View.OnCreateContextMenuListener { menu, v, _ ->
        v as GroupV
        val groupName = v.textView.text
        menu.add(0, 0, 0, "Eliminar")
        menu.add(0, 1, 1, "Editar")

        menu.findItem(0).setOnMenuItemClickListener {
            // Lógica para editar el elemento
            val group = groupsRef.child(groupName.toString())
            // Preguntar si realmente se quiere eliminar el grupo
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.yes_or_no_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val textWarning = dialog.findViewById<TextView>(R.id.dialog_text_warning)
            textWarning.text = getString(R.string.eliminar_template, groupName.toString())
            val cancelButton = dialog.findViewById<TextView>(R.id.cancel_button)
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            val aceptButton = dialog.findViewById<TextView>(R.id.ok_button)
            aceptButton.setOnClickListener{
                group.removeValue().addOnSuccessListener {

                    // El grupo a sido eliminado correctamente
                    Toast.makeText(this, "$groupName a sido eliminado", Toast.LENGTH_SHORT).show()
                    loadGroups()
                }.addOnFailureListener {
                    Toast.makeText(this, "A ocurrido un error al eliminar $groupName", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            dialog.show()
            true
        }

        menu.findItem(1).setOnMenuItemClickListener {
            // Lógica para eliminar el elemento
            val createGroup = CreateGroup( {     loadGroups()    }, groupName.toString())
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.welcome_screen, createGroup)
            ft.addToBackStack(null)
            ft.show(createGroup)
            ft.commit()
            true
        }
    }

    private fun loadGroups(){
        binding.groupContainer.removeAllViews()
        val database = FirebaseDatabase.getInstance("https://school-e8de3-default-rtdb.firebaseio.com/")
        val groupsRef = database.getReference("usuarios").child(auth.uid!!).child("groups")

        groupsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val groupKey = childSnapshot.key // clave del hijo
                        val view = GroupV(this@ResultActivity, groupKey)
                        view.setOnCreateContextMenuListener(groupMenuListener)
                        view.setOnClickListener {
                            displayGroup(groupKey!!)
                        }
                        binding.groupContainer.addView(view)
                        // hacer algo con los datos obtenidos
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar el error
            }
        })

    }

    private fun createGroup() {
        val createGroup = CreateGroup( {     loadGroups()    })
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.welcome_screen, createGroup)
        ft.addToBackStack(null)
        ft.show(createGroup)
        ft.commit()
    }

    private fun saveImage(imageUri: Uri){
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(imageUri)
            .build()

        auth.currentUser!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // La URL de la foto de perfil se ha establecido correctamente.
                    loadAvatar()
                }
            }
    }
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri1: Uri? ->
        val storageRef = Firebase.storage.reference
        // Verificamos que se haya seleccionado una imagen
        if (uri1 != null) {
            // Si se ha seleccionado una imagen, la establecemos en el ImageView y la guardamos en el almacenamiento interno
            val inputStream: InputStream = contentResolver.openInputStream(uri1) ?: return@registerForActivityResult
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also {  len = it } != -1){
                byteArrayOutputStream.write(buffer, 0, len)
            }
            val data = byteArrayOutputStream.toByteArray()
            val imageRef = storageRef.child("users").child(auth.currentUser!!.uid).child("profilePicture.png")
            imageRef.putBytes(data)
                .addOnSuccessListener {
                    // Obtén la URL de descarga de la imagen
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Guarda la URL de descarga en Firebase auth
                        saveImage(uri)
                       // databaseRef.child("imagenUrl").setValue(imageUrl)
                    }
                }
                .addOnFailureListener { exception ->
                    // Maneja el error
                    exception.printStackTrace()
                    Toast.makeText(this, "A ocurrido un error al subir la imagen de perfil", Toast.LENGTH_SHORT).show()
                }
        }
    }
    // Esta es la función que se ejecuta cuando se hace clic en el ImageView
    private fun pickImageFromGallery() {
        // Lanzamos el selector de imágenes utilizando el contrato getContent
        // Especificamos el tipo de contenido que queremos obtener (en este caso, cualquier imagen)
        getContent.launch("image/*")
    }
}