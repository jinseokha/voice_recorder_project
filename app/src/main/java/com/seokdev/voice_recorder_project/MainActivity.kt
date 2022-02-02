package com.seokdev.voice_recorder_project

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.seokdev.voice_recorder_project.databinding.ActivityMainBinding
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val recordingFilePath : String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    private var state = State.BEFORE_RECORDING
    set(value) {
        field = value
        binding.resetButton.isEnabled = (value == State.AFTER_RECORDING || value == State.ON_PLAYING)
        binding.recordButton.updateIconWithState(value)
    }

    private var recorder : MediaRecorder? = null
    private var player : MediaPlayer? = null

    private lateinit var binding : ActivityMainBinding

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        requestAudioPermission()
        initViews()
        bindViews()
        initVariables()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                    grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (!audioRecordPermissionGranted)
            finish()
    }

    private fun requestAudioPermission() {
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun initViews() {
        binding.recordButton.updateIconWithState(state)
    }

    private fun bindViews() {

        binding.soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude ?: 0
        }

        binding.recordButton.setOnClickListener {
            when (state) {
                State.BEFORE_RECORDING -> {
                    startRecoding()
                }
                State.ON_RECORDING -> {
                    stopRecording()
                }
                State.AFTER_RECORDING -> {
                    startPlaying()
                }
                State.ON_PLAYING -> {
                    stopPlaying()
                }
            }
        }

        binding.resetButton.setOnClickListener {

            binding.soundVisualizerView.clearVisualization()
            binding.recordTimeTextView.clearCountTime()
            state = State.BEFORE_RECORDING
        }
    }

    private fun initVariables() {
        state = State.BEFORE_RECORDING
    }

    private fun startRecoding() {
        recorder = MediaRecorder()
            .apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFilePath)
                prepare()
            }
        recorder?.start()
        binding.recordTimeTextView.startCountUp()
        binding.soundVisualizerView.startVisualizing(false)
        state = State.ON_RECORDING
    }

    private fun stopRecording() {
        recorder?.run {
            stop()
            release()
        }
        recorder = null
        binding.soundVisualizerView.stopVisualizing()
        binding.recordTimeTextView.stopCountUp()
        state = State.AFTER_RECORDING
    }

    private fun startPlaying() {
        player = MediaPlayer()
            .apply {
                setDataSource(recordingFilePath)
                prepare()
            }

        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECORDING
        }

        player?.start()
        binding.recordTimeTextView.stopCountUp()

        binding.soundVisualizerView.startVisualizing(true)

        state = State.ON_PLAYING
    }

    private fun stopPlaying() {
        player?.release()
        player = null
        binding.soundVisualizerView.stopVisualizing()
        binding.recordTimeTextView.stopCountUp()

        state = State.AFTER_RECORDING
    }
}