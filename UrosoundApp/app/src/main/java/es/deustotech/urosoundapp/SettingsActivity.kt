package es.deustotech.urosoundapp


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import es.deustotech.urosoundapp.databinding.ActivitySettingsBinding
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SettingsActivity : Activity() {

    private lateinit var binding: ActivitySettingsBinding
    private var queue: RequestQueue? = null

    private lateinit var newPatientID : TextView
    private lateinit var audioinfoText : TextView
    private lateinit var uploadBut : Button
    private lateinit var registerBut : Button

    private var patientID = ""
    var android_id: String? = null
    var registrado = false
    var egg_counter = 0
    var token:String? = null

    var URL = "" //Indicate here your web service URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = ""
        queue = Volley.newRequestQueue(this)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        newPatientID = findViewById(R.id.patientIDtext)
        newPatientID.hint =getString(R.string.patient_code)
        newPatientID.text = ""
        audioinfoText = findViewById(R.id.infoText)
        uploadBut = findViewById(R.id.upload_butt)
        registerBut = findViewById(R.id.update_butt)
        egg_counter = 0;

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        android_id = Settings.Secure.getString(this.getContentResolver(),
            Settings.Secure.ANDROID_ID);

        registrado = false

        //Path name to save audio on the phone
        val path = this.cacheDir.toString() + File.separator + "UroSound"
        val directory = File(path)
        val files = directory.listFiles()
        nfiles = files.size
        audioinfoText.text = getString(R.string.tot_audios) + "$nfiles"

    }


    fun show_toast(text: String?,duration:Int) {
        var toast =  Toast.makeText(this, text, duration)
        val group: ViewGroup = toast.getView() as ViewGroup
        val messageTextView = group.getChildAt(0) as TextView
        messageTextView.textSize = 20f
        toast.show()
    }

    var nfiles = 0
    var nfiles_sent = 0

    fun upload_audios(view: View){
        if (patientID == null || patientID=="" || !registrado){
            show_toast(getString(R.string.register_first),1)
        }
        else {
            val path = this.cacheDir.toString() + File.separator + "UroSound"
            if (File(path).listFiles().size < 1) {
                show_toast(getString(R.string.no_audios_2send),1)
            } else {
                goToLogin_upload(this, patientID, android_id!!)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    fun update_patientID(view: View){
        patientID = newPatientID.text.toString()

        if (patientID == ""){
            show_toast(getString(R.string.code_not_empty),1)
        }
        else
        {
            registerBut.text = getString(R.string.reg)
            goToRegisterAnonymous(this,patientID,android_id!!)
        }

    }

    private fun goToRegisterAnonymous(context: Context, patientID: String, pass: String) {
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            URL + "route-of-the-web-service",
            Response.Listener {
                registrado = true
                show_toast("PACIENTE REGISTRADO CON EXITO: "+patientID,1)
                registerBut.text = getString(R.string.add_patient)
            },
            Response.ErrorListener {error ->
                registerBut.text = getString(R.string.add_patient)
                try {
                    show_toast("ERROR - REVISE LA CONEXION A INTERNET Y EL CODIGO DEL PACIENTE",1)
                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            override fun getParams(): Map<String, String> {
                val sdf = SimpleDateFormat("dd/MM/yyy", Locale.getDefault())
                var currentTimeDB = sdf.format(Date()).toString()

                val params: MutableMap<String, String> = HashMap()
                params["patientID"] = patientID
                params["password"] = pass
                params["name"] = patientID
                params["surname"] = pass
                params["DOB"] = currentTimeDB
                params["dateJoined"] = currentTimeDB
                params["clinicHistorial"] = ""
                return params
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                token = response.headers["auth-token"]
                return super.parseNetworkResponse(response)
            }
        }
        queue!!.add(jsonObjRequest)

    }

    private fun iterateOverFolderFiles(context: SettingsActivity) {
        val path = this.cacheDir.toString() + File.separator + "UroSound"
        val directory = File(path)
        val files = directory.listFiles()
        nfiles = files.size
        audioinfoText.text = "Total: $nfiles / "+getString(R.string.sent)+ "0"

        for (i in files.indices) {
            if (files[i].absolutePath.substring(files[i].absolutePath.lastIndexOf(".")) == ".wav") {
                  val durationB:Float =0.0f//
                 uploadAudioToMongoDB(files[i], this,durationB.toString())
            }
        }
    }


    fun updateGUiAudios(){
        nfiles_sent = nfiles_sent + 1
        audioinfoText.text = "Total: $nfiles / "+getString(R.string.sent)+"$nfiles_sent"

        if(nfiles_sent >=nfiles){
            show_toast(nfiles_sent.toString()+" "+getString(R.string.audios_sent)+newPatientID.text,0)
            nfiles = 0;
            nfiles_sent = 0;
            uploadBut.text =getString(R.string.send_audios)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun uploadAudioToMongoDB(file: File, context: SettingsActivity,duration:String) {
        val jsonObjRequest: StringRequest = object : StringRequest(
           Method.POST, URL + "route-of-the-web-service",
            Response.Listener { response ->
                updateGUiAudios()
                file.delete()
            },

            Response.ErrorListener { error ->
                show_toast(
                    "Error envio audio. Compruebe conexión a internet",
                    1
                )
                uploadBut.text =   getString(R.string.send_audios)
                audioinfoText.text = "Total Audios: $nfiles"

            }) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            override fun getParams(): Map<String, String> {
                var base64: String? = null
                try {
                    base64 = Base64.encodeToString(
                        FileUtils.readFileToByteArray(file),
                        Base64.NO_WRAP
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                val params: MutableMap<String, String> = HashMap()
                val datetime = file.name.substring(0, file.name.lastIndexOf('.'))
                val date2db = datetime.replace("_", "/").substring(0, 10)
                val time2db = datetime.replace("_", ":").substring(11)
                val audioDuration = duration
                params["authtoken"] = token!!
                params["data"] = base64!!
                params["format"] = "wav"
                params["timeMade"] = time2db
                params["dateMade"] = date2db
                params["duration"] = audioDuration
                params["name"] = datetime
                return params
            }
        }

        queue!!.add(jsonObjRequest)
    }


    private fun goToLogin_upload(context: Context, patientID: String, pass: String) {
        val jsonObjRequest: StringRequest = object : StringRequest(
            Method.POST,
            URL + "route-of-the-web-service",
            Response.Listener {
                uploadBut.text = getString(R.string.send_wait)
                iterateOverFolderFiles(this)
            },
            Response.ErrorListener {
                try {
                    show_toast("ERROR - REVISE LA CONEXION A INTERNET Y EL CODIGO DEL PACIENTE",1)

                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }) {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            override fun getParams(): Map<String, String> {

                val params: MutableMap<String, String> = HashMap()
                params["patientID"] = patientID
                params["password"] = pass

                return params
            }


            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                token = response.headers["auth-token"]
                return super.parseNetworkResponse(response)
            }
        }
        queue!!.add(jsonObjRequest)

    }



}