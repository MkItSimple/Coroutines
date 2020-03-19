package com.example.coroutines

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_lesson2.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class NetworkTimeoutsActivity : AppCompatActivity() {

    val JOB_TIMEOUT = 2100L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_timeouts)

        button.setOnClickListener {
            setNewText("Click!")

            CoroutineScope(Dispatchers.IO).launch {
                fakeApiRequest()
            }
        }

        button2.setOnClickListener {
            intent = Intent(this, CoroutinesJobsActivity::class.java)
            startActivity(intent)
        }
    }

    private suspend fun fakeApiRequest(){
        withContext(IO){
            val job = withTimeoutOrNull(JOB_TIMEOUT) {

                val result1 = getResult1FromApi()
                setTextOnMainThread("Got $result1")


                val result2 = getResult2FromApi()
                setTextOnMainThread("Got $result2")
            } // wait

            if (job == null){
                val cancelMessage = "Cancelling job...Job took longer than $JOB_TIMEOUT ms"
                println("debug: $cancelMessage")
                setTextOnMainThread(cancelMessage)
            }

            // val job2 = launch {}
        }
    }

    private fun setNewText(input: String){
        val newText = text.text.toString() + "\n$input"
        text.text = newText
    }
    private suspend fun setTextOnMainThread(input: String) {
        withContext (Dispatchers.Main) {
            setNewText(input)
        }
    }

    private suspend fun getResult1FromApi(): String {
        delay(1000) // Does not block thread. Just suspends the coroutine inside the thread
        return "Result #1"
    }

    private suspend fun getResult2FromApi(): String {
        delay(1000)
        return "Result #2"
    }
}
