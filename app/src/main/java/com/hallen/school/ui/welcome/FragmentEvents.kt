package com.hallen.school.ui.welcome

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.sundeepk.compactcalendarview.CompactCalendarView.CompactCalendarViewListener
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.hallen.school.R
import com.hallen.school.databinding.FragmentEventsBinding
import com.hallen.school.model.events.EventAdapter
import com.hallen.school.ui.welcome.alarm.AlarmReceiver
import java.lang.reflect.InvocationTargetException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class FragmentEvents : Fragment() {
    private lateinit var binding: FragmentEventsBinding
    private lateinit var adapter: EventAdapter
    private lateinit var eventRef: DatabaseReference
    private var auth: FirebaseUser? = null
    private lateinit var database: FirebaseDatabase
    private val littleFormat:   DateFormat = SimpleDateFormat("hh:mm a", Locale.US)
    private val littleFormat2:   DateFormat = SimpleDateFormat("hh:mm-a", Locale.US)
    private val dateFormat:     DateFormat = SimpleDateFormat("dd-MM-yyyy")
    private val databaseFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy-hh:mm-a", Locale.US)

    private lateinit var currentDay: Date
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        auth = FirebaseAuth.getInstance().currentUser
        currentDay = Date()
        database = FirebaseDatabase.getInstance("https://school-e8de3-default-rtdb.firebaseio.com/")
        eventRef = database.getReference("usuarios").child(auth!!.uid).child("events")
        binding = FragmentEventsBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = EventAdapter(requireContext(), arrayListOf())
        binding.eventsList.adapter = adapter
        binding.newEvent.setOnClickListener {
            newEventDialog()
        }
        setCalendarListeners()
    }

    /**
    *
    *  Sets the listeners for the [CompactCalendarView] and the [ListView]
    */
    private fun setCalendarListeners() {
        // the textview binding.dayName will observe and change its text according to
        val coolFormat = DateFormat.getDateInstance()
        val fechaActual = coolFormat.format(Date())
        binding.dayName.text = fechaActual
        binding.eventsCalendar.setListener(object : CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date?) {
                binding.dayName.text = coolFormat.format(dateClicked)
                //val eventList: List<Event> = binding.eventsCalendar.getEvents(dateClicked)
                currentDay = dateClicked ?: return
                loadDayEvent(currentDay)
                getAllEvents()
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date?) {
                binding.dayName.text = coolFormat.format(firstDayOfNewMonth ?: return)
                currentDay = firstDayOfNewMonth
                getAllEvents()
                loadDayEvent(currentDay)
            }
        })

        binding.eventsCalendar.setCurrentDate(Date())
        binding.eventLeft.setOnClickListener  { binding.eventsCalendar.scrollLeft()  }
        binding.eventRight.setOnClickListener { binding.eventsCalendar.scrollRight() }
        binding.eventsCalendar.shouldDrawIndicatorsBelowSelectedDays(true)
        loadEventForMonth()
        loadDayEvent(currentDay)

        binding.eventsList.setOnItemLongClickListener { adapterView, view, position, l ->
            val item = adapter.getItem(position) as Map<String, String>
            val key: String = item["key"] ?: return@setOnItemLongClickListener false
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.simple_delete_menu, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                deleteEvent(key, position, view); true
            }
            true
        }
    }

    private fun deleteEvent(key: String, position: Int, view: View) {
        eventRef.child(key).setValue(null)
        view.animate()?.apply {
            duration = 200
            alpha(0f)
            translationX(-view.width.toFloat())
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    adapter.remove(position)
                }
            })
            start()
        }
    }

    private fun loadDayEvent(currentDay: Date) {
        binding.eventHeader.visibility = View.INVISIBLE; binding.eventHeaderDivider.visibility = View.INVISIBLE
        eventRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()){
                    adapter.clear()
                    for (child in snapshot.children){

                        val childValue = child.value as Map<String, Any>
                        val tiempo = (childValue["time"] ?: continue) as String

                        val date = try { databaseFormat.parse(tiempo)
                        } catch (e: InvocationTargetException){ e.printStackTrace(); null
                        } catch (e:ParseException){ e.printStackTrace(); null }?: continue

                        val parseDate  = dateFormat.format(date) ?: continue
                        val actualDate = dateFormat.format(currentDay)

                        if (parseDate == actualDate){
                            val key: String = child.key ?: continue
                            val title: String   = (childValue["title"]   ?: "ERROR") as String
                            val details: String = (childValue["details"] ?: "ERROR") as String
                            val color = (childValue["prioridad"] ?: continue) as String
                            val hora = littleFormat.format(date)

                            val map = mapOf(
                                "title"   to title,
                                "details" to details,
                                "color"   to color,
                                "hora"    to hora,
                                "key"     to key
                            )
                            if (binding.eventHeader.visibility == View.INVISIBLE){
                                binding.eventHeader.visibility = View.VISIBLE
                                binding.eventHeaderDivider.visibility = View.VISIBLE
                            }
                            adapter.addView(map)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "A ocurrido un error al cargar el evento", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadEventForMonth() {

    }

    private fun getAllEvents(){
        val colorTemplates = mapOf("Alta" to R.color.alta, "Media" to R.color.media, "Baja" to R.color.baja)
        var event: Event?
        eventRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()){
                    for (childSnapShot in snapshot.children){

                        val eventDatabase = childSnapShot.value as Map<String, Any>
                        val color = colorTemplates[eventDatabase["prioridad"]] ?: Color.WHITE

                        val date: String = (eventDatabase["time"] ?: continue) as String

                        val time: Date = try { databaseFormat.parse(date)
                        } catch (e: InvocationTargetException){ e.printStackTrace(); null
                        } catch (e:ParseException){ e.printStackTrace(); null }?: continue


                        event = Event(ContextCompat.getColor(requireContext(), color), time.time)
                        binding.eventsCalendar.removeEvent(event, false)
                        binding.eventsCalendar.addEvent(event, true)
                        //adapter.addView(title to details)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {    }
        })
    }

    private fun newEventDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_new_event)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val eventNameEditText = dialog.findViewById<EditText>(R.id.dialog_event_title)
        eventNameEditText.hint = "Nombre del evento"

        val eventDetailsEditText = dialog.findViewById<EditText>(R.id.dialog_event_details)
        eventDetailsEditText.hint = "Detalles"

        val pmSpinner = dialog.findViewById<Spinner>(R.id.pm_am)
        val mpArray = arrayOf("AM", "PM")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mpArray)
        pmSpinner.adapter = spinnerAdapter

        val horaEditText = dialog.findViewById<EditText>(R.id.hora_event)
        horaEditText.setText(getString(R.string._08_00))

        val prioritySpinner = dialog.findViewById<Spinner>(R.id.event_priority)
        val priorityArray = arrayOf("Baja", "Media", "Alta")
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            priorityArray)
        prioritySpinner.adapter = priorityAdapter

        val notificarCheckBox = dialog.findViewById<CheckBox>(R.id.event_notificar)

        val acept = dialog.findViewById<TextView>(R.id.ok_button)
        val cancel = dialog.findViewById<TextView>(R.id.cancel_button)
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        acept.setOnClickListener {
            val timeMode = mpArray[pmSpinner.selectedItemPosition]
            val horaTest = horaEditText.text.toString()
            val hora = "$horaTest-$timeMode"
            val horaConverted = try {
                littleFormat2.parse(hora)
            } catch (e: Exception){
                Toast.makeText(requireContext(), "Hora inválida: $hora" , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val time = dateFormat.format(currentDay) + "-" + littleFormat2.format(horaConverted)

            val prioridad = priorityArray[prioritySpinner.selectedItemPosition]
            val notificar = notificarCheckBox.isChecked
            val title   = eventNameEditText.text.toString()
            val details = eventDetailsEditText.text.toString()
            if (isValidName(title)){
                newEvent(title, details, time, prioridad, notificar)
            } else {
                Toast.makeText(requireContext(), "El nombre del evento no es válido", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }


        dialog.show()
    }



    private fun isValidName(name: String): Boolean {
        // Comprueba si el nombre contiene solo letras (incluso acentuadas/con tildes)
        return name.matches(Regex("[a-zA-ZäöüÄÖÜáéíóúÉÍÓÚàèìòùÀÈÌÒÙâêîôûÂÊÎÔÛãõÃÕçḉÇḈ ]*"))
    }

    /**
     * This code generates a unique key for a new value in the "events" reference of the database.
     */
        private fun newEvent(
        title: String,
        details: String,
        time: String,
        prioridad: String,
        notificar: Boolean
    ) {
        // Create a map of the content to be stored in the database
        val content = mapOf(
            "time"      to time,
            "title"     to title,
            "details"   to details,
            "prioridad" to prioridad,
            "notificar" to notificar)
        // Get the key for the new event
        val eventKey = eventRef.push().key ?: return
        // Store the content in the database
        eventRef.child(eventKey).setValue(content) { error, _ ->
            // If there is no error, reload the events
            if (error == null){
                Toast.makeText(requireContext(), "Se a creado el evento", Toast.LENGTH_SHORT).show()
                loadDayEvent(currentDay)
                if (notificar){
                    val timeStamp = (databaseFormat.parse(time) ?: return@setValue).time
                    crearRecordatorio(timeStamp, title, details)
                }
            } else {
                // Otherwise, show an error message
                Toast.makeText(requireContext(), "A ocurrido un error al crear el evento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun crearRecordatorio(time: Long, titulo: String, descripcion: String) {
        Toast.makeText(requireContext(), "1 - Title: $titulo, Details: $descripcion", Toast.LENGTH_SHORT).show()
        val alarmManager = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(requireContext(), AlarmReceiver::class.java)

        alarmIntent.putExtra("date", time)
        alarmIntent.putExtra("title", titulo)
        alarmIntent.putExtra("details", descripcion)

        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 1, alarmIntent, PendingIntent.FLAG_ONE_SHOT)

        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }

}