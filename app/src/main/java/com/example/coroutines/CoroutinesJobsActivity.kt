package com.example.coroutines

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_lesson2.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class CoroutinesJobsActivity : AppCompatActivity() {

    private val TAG: String = "CoroutineJobsActivity"

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000 // ms
    private lateinit var job: CompletableJob


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_jobs)

        job_button.setOnClickListener {
            if(!::job.isInitialized){
                initjob()
            }

            job_progress_bar.startJobOrCancel(job)
        }

        button2.setOnClickListener {
            intent = Intent(this, ParallelBackgroundTasks::class.java)
            startActivity(intent)
        }

    }

    fun resetjob(){
        if(job.isActive || job.isCompleted){
            job.cancel(CancellationException("Resetting job"))
        }
        initjob()
    }


    fun initjob(){
        job_button.setText("Start Job #1")
        updateJobCompleteTextView("")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let{
                var msg = it
                if(msg.isNullOrBlank()){
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

        if(this.progress > 0){
            Log.d(TAG, "${job} is already active. Cancelling...")
            resetjob()
        } else{

            job_button.setText("Cancel Job #1")
            CoroutineScope(IO + job).launch{

                Log.d(TAG, "coroutine ${this} is activated with job ${job}.")

                for(i in PROGRESS_START..PROGRESS_MAX){
                    delay((JOB_TIME / PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }

                updateJobCompleteTextView("Job is complete!")
            }
        }
    }



    private fun updateJobCompleteTextView(text: String){
        GlobalScope.launch (Main){
            job_complete_text.setText(text)
        }
    }

    fun showToast(text: String){
//        GlobalScope.launch(Main) {
//            Toast.makeText(this@CoroutinesJobsActivity, text, Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
