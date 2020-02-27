package com.example.coroutines

import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {

    private val TAG: String = "AppDebug"

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000 // ms
    private lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job_button.setOnClickListener {

            if (!::job.isInitialized){
                initjob()
            }
            job_progress_bar.startJobOrCancel(job)
        }
    }

    private fun initjob() {
        Log.d(TAG, "working!!!")
        job_button.setText("Start Job #1")
        updateJobCompleteTextView("")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrBlank()){
                    msg = "Unknown cancellation error."
                }
                Log.e(TAG, "${job} was cancelled. Reason: ${msg}")
                showToast(msg)
            }
        }
        job_progress_bar.max = PROGRESS_MAX
        job_progress_bar.progress = PROGRESS_START
    }

    fun ProgressBar.startJobOrCancel(job: Job){
        if (this.progress > 0){
            Log.d(TAG, "${job} is already. active. Cancelling...")
            resetJob()
        } else {
            job_button.setText("Cancel Job #1")
            CoroutineScope(IO + job).launch {
                Log.d(TAG, "coroutine ${this} is activated with job ${job}.")

                for(i in PROGRESS_START..PROGRESS_MAX){
                    delay((JOB_TIME / PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }
                updateJobCompleteTextView("Job is complete!")
            }
        }
    }

    private fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting job"))
        }
        initjob()
    }

    private fun updateJobCompleteTextView(s: String) {
        GlobalScope.launch (Main){
            job_complete_text.setText(s)
        }
    }

    private fun showToast(msg: String) {
        GlobalScope.launch(Main){
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
