package es.deustotech.urosoundapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.os.*
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.squti.androidwaverecorder.WaveRecorder
import es.deustotech.urosoundapp.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : Activity()  {

    private lateinit var binding: ActivityMainBinding

    //Variables and widgets definitions
    var btnRecordWAV: Button? = null
    var hintText: Button? = null
    var pathSave = ""
    lateinit var fileSave:File
    var fileNameToSave = ""
    lateinit var waveRecorder: WaveRecorder
    var recording = false
    var dialogON = false
    val REQUEST_PERMISSION_CODE = 1000
    private var startHTime = 0L
    private var counteraudios = 0
    private val MAX_REC_WAV:Long = 90000//-- 1.5 minutes
    var egg_counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnRecordWAV = findViewById(R.id.recordButtonwav);
        hintText = findViewById(R.id.hintText);

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        egg_counter = 0;
        counteraudios = 0

        if (!checkPermissionFromDevice()) {
            requestPermission();
        }
        start_or_stop_recording()
    }

   override fun onResume() {
        super.onResume()
        dialogON = false
    }

    fun start_or_stop_recording(){
        if (!recording) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            startRecordingAudio()
        } else {
            stopRecordingAudio()
        }
    }

    //If the user wants to delete the audio by pressing the right-upper button,
    // the onStop method is called right before closing the app
    override fun onStop() {
        super.onStop()
        Process.killProcess(Process.myPid())
    }

    //When pressing the button: records if idle, stop recording if recording
    fun audioButtonClickedwav(target: View?) {
        start_or_stop_recording()
    }

    var mycounter = object : CountDownTimer(30000, 30000) {
        override fun onTick(millisUntilFinished: Long) {
        }
        override fun onFinish() {
            egg_counter = 0;
        }
    }

    fun send_audio_server(target: View?) {
        if(egg_counter==0) {
            mycounter.start()
        }
        //The egg counter is used to access the Control panel activity:
        // user needs to press the UroSound label 7 times or more
        egg_counter = egg_counter + 1
        if (egg_counter >= 7) {
            mycounter.cancel()
            mycounter2.cancel()
            mycounter_max_recording.cancel()
            mycounter_aftersave.cancel()

            egg_counter = 0
            val uploadIntent = Intent(this, SettingsActivity::class.java)
            startActivity(uploadIntent)
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ), REQUEST_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_accept, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.permission_decline, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionFromDevice(): Boolean {
        val write_external_storage_result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val record_audio_result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED
    }


    //Control the hardware buttons of the Oppo smartwatch.
    //Note that it might vary across smartwatches models/manufacturers
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.repeatCount == 0) {
            when (keyCode) {
                KeyEvent.KEYCODE_STEM_1 -> {
                    if (dialogON){

                    }
                    else
                        start_or_stop_recording()

                    true
                                    }
                KeyEvent.KEYCODE_STEM_2 -> {
                    // Do stuff
                    true
                }
                KeyEvent.KEYCODE_STEM_3 -> {
                    // Do stuff
                    true
                }
                else -> {
                    super.onKeyDown(keyCode, event)
                }
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }



    var mycounter_max_recording = object : CountDownTimer(MAX_REC_WAV, MAX_REC_WAV) {
        override fun onTick(millisUntilFinished: Long) {
        }
        override fun onFinish() {
            //VIBRATE WATCH
            val v = (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
            v.vibrate(VibrationEffect.createOneShot(500,
                VibrationEffect.DEFAULT_AMPLITUDE))
            stopRecordingAudio()
        }

    }

        var mycounter2 = object : CountDownTimer(20000, 20000) {
        override fun onTick(millisUntilFinished: Long) {
        }
        override fun onFinish() {
            alert.cancel()
            dialog_yes()        }
    }


    lateinit var dialog: AlertDialog.Builder
    lateinit var alert:AlertDialog

    var mycounter_aftersave = object : CountDownTimer(4000, 4000) {
        override fun onTick(millisUntilFinished: Long) {
        }
        override fun onFinish() {
            Process.killProcess(Process.myPid())
        }
    }

    private fun dialog_yes(){
        btnRecordWAV!!.setText(R.string.start_recording)
        show_toast(getString(R.string.audio_stored), 0)
        mycounter2.cancel()
        dialogON = false
        mycounter_aftersave.start()

    }

    private fun show_dialog() {
        dialogON = true
        dialog = AlertDialog.Builder(this)

        dialog.setPositiveButton(
            R.string.negative_answer
        ) { dialog, which ->
            fileSave.delete()
            btnRecordWAV!!.setText(R.string.start_recording)
            show_toast(getString(R.string.audio_deleted), 0)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            mycounter2.cancel()
            dialogON = false
        }

        dialog.setNegativeButton(
            R.string.positive_answer
        ) { dialog, which ->
            dialog_yes()
        }

        alert = dialog.create()

        val title_of_dialog = TextView(applicationContext)
        title_of_dialog.textSize = 33f
        title_of_dialog.text = getString(R.string.save_audios)
        title_of_dialog.setTextColor(Color.WHITE)
        title_of_dialog.gravity = Gravity.CENTER
        alert.setView(title_of_dialog)


        alert.show()


        val btn1 = alert.getButton(DialogInterface.BUTTON_POSITIVE)
        btn1.text = getString(R.string.negative_answer)
        btn1.textSize = 30f
        val btn2 = alert.getButton(DialogInterface.BUTTON_NEGATIVE)
        btn2.textSize = 35f
        btn2.setBackgroundColor(Color.GRAY)
        btn2.text = getString(R.string.positive_answer)+"   --->"

        alert.setOnKeyListener(object : DialogInterface.OnKeyListener {

            override fun onKey(dialog:DialogInterface , keyCode:Int,  event:KeyEvent): Boolean {
                when (keyCode) {
                    KeyEvent.KEYCODE_STEM_1 -> {
                        if (dialogON) {
                            alert.cancel()
                            dialog_yes()
                        } else{}

                    }

                }
                return true
            }
        })

    }

    fun show_toast(text: String?, duration: Int?) {
        val toast = Toast.makeText(this@MainActivity, text, duration!!)
        val group = toast.view as ViewGroup?
        val messageTextView = group!!.getChildAt(0) as TextView
        messageTextView.textSize = 20f
        toast.show()
    }


    fun startRecordingAudio() {

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mycounter_aftersave.cancel()
        val folder_wav = File(this.cacheDir.toString() + File.separator + "UroSound")
        var success = folder_wav.exists()
        if (!success) {
            success = folder_wav.mkdirs()
        }
        if (!success) return
        val sdf = SimpleDateFormat("yyyy_MM_dd'_'HH_mm_ss", Locale.getDefault())
        val currentTime = sdf.format(Date())
        fileNameToSave = currentTime
        pathSave = folder_wav.absolutePath + File.separator +
                currentTime + ".wav"
        waveRecorder = WaveRecorder(pathSave)
        waveRecorder!!.waveConfig.sampleRate = 16000
        waveRecorder!!.audioSessionId

        waveRecorder!!.waveConfig.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        waveRecorder!!.waveConfig.channels = AudioFormat.CHANNEL_IN_MONO // default is IN_MONO
        waveRecorder!!.noiseSuppressorActive = false
        waveRecorder!!.startRecording()
        btnRecordWAV!!.setText(R.string.stop_recording)
        startHTime = SystemClock.uptimeMillis()
        hintText!!.text = getString(R.string.recording)
        recording = true

        mycounter_max_recording.start()
    }


    private fun stopRecordingAudio() {
        waveRecorder.stopRecording()
        mycounter_max_recording.cancel()
        recording = false
        fileSave = File(pathSave)
        hintText!!.setText(R.string.title)
        mycounter2.start()
        dialogON = true
        show_dialog()


    }

}