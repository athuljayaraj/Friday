package com.flytxt.friday

import ai.api.AIServiceException
import ai.api.android.AIConfiguration
import ai.api.android.AIDataService
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ai.api.model.AIRequest
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import net.gotev.speech.*
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val speech = "speech"

    override fun onInit(p0: Int) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Speech.init(this)
            doRecording()
        } else {
            Toast.makeText(this, "Permission Required inorder to record audio", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        graphWebView.settings.javaScriptEnabled = true

        Log.i("SpeechInitialized", "Initialised")

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 123)
            return
        }
        Speech.init(this)
        doRecording()
    }

    private fun doRecording() {
        try {
            // you must have android.permission.RECORD_AUDIO granted at this point
            Speech.getInstance().startListening(progress, object : SpeechDelegate {
                override fun onStartOfSpeech() {
                    Log.i("SpeechStart", "speech recognition is now active")
                }

                override fun onSpeechRmsChanged(value: Float) {
                    Log.d(speech, "rms is now: " + value)
                }

                override fun onSpeechPartialResults(results: List<String>) {
                    val str = StringBuilder()
                    for (res in results) {
                        str.append(res).append(" ")
                    }

                    Log.i(speech, "partial result: " + str.toString().trim { it <= ' ' })
                }

                override fun onSpeechResult(result: String) {
                    Log.i("SpeechFinish", "result: " + result)
                    parse(result)

                }
            })
        } catch (exc: SpeechRecognitionNotAvailable) {
            Log.e(speech, "Speech recognition is not available on this device!")
        } catch (exc: GoogleVoiceTypingDisabledException) {
            Log.e(speech, "Google voice typing must be enabled!")
        }
    }

    private fun parse(text: String) {

        val tts = TextToSpeech(this@MainActivity, this@MainActivity)
        tts.language = Locale.US

        val config = AIConfiguration("95693ca526604801ba01875feee22c6c", ai.api.AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System)

        val aiDataService = AIDataService(applicationContext, config)
        val aiRequest = AIRequest()
        aiRequest.setQuery(text)
        launch(CommonPool) {
            try {
                val response = aiDataService.request(aiRequest)
                Log.i("Response", response.result.parameters.toString())

                tts.speak((response.result.fulfillment.speech), TextToSpeech.QUEUE_FLUSH, null)

                launch(UI) {
                    graphWebView.loadUrl("https://arunsoman.github.io/polymer-d3/")
                    Toast.makeText(this@MainActivity, " Context: " + response.result.toString(), Toast.LENGTH_LONG).show()

                }
            } catch (e: AIServiceException) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("System", "Destroy")
        Speech.getInstance().shutdown()

    }
}
