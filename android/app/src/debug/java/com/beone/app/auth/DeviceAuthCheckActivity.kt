package com.beone.app.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.beone.app.R

// debug 빌드 전용 실기기 점검 화면. 제품 화면이 아니며 release에는 포함되지 않는다.
class DeviceAuthCheckActivity : AppCompatActivity() {

    private lateinit var viewModel: VirtualCardActivationViewModel
    private lateinit var authenticator: DeviceAuthenticator
    private lateinit var statusText: TextView
    private lateinit var remainingText: TextView
    private lateinit var activeIndicator: Button

    private val ticker = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            render()
            ticker.postDelayed(this, TICK_MILLIS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_auth_check)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.check_root)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[VirtualCardActivationViewModel::class.java]
        // 재생성마다 새로 만들어야 진행 중이던 인증 결과를 이어 받는다.
        authenticator = BiometricDeviceAuthenticator(this) { outcome ->
            viewModel.onAuthOutcome(outcome)
            render()
        }
        statusText = findViewById(R.id.check_status)
        remainingText = findViewById(R.id.check_remaining)
        activeIndicator = findViewById(R.id.check_active_indicator)
        findViewById<Button>(R.id.check_authenticate).setOnClickListener { requestAuthentication() }

        // PRD §4.3-1: 화면을 처음 열면 인증을 요청한다.
        if (savedInstanceState == null) {
            requestAuthentication()
        }
    }

    override fun onStart() {
        super.onStart()
        ticker.post(tick)
    }

    override fun onStop() {
        ticker.removeCallbacks(tick)
        viewModel.onScreenStopped(isChangingConfigurations, authenticator)
        super.onStop()
    }

    private fun requestAuthentication() {
        viewModel.requestAuthentication(authenticator)
        render()
    }

    private fun render() {
        val state = viewModel.state
        activeIndicator.isEnabled = state is ActivationState.Active
        remainingText.text = getString(R.string.check_remaining_seconds, (viewModel.remainingMillis() + 999) / 1000)
        statusText.text = when {
            state is ActivationState.Active -> getString(R.string.check_status_active, state.method.name)
            viewModel.isAuthenticating -> getString(R.string.check_status_authenticating)
            else -> getString(inactiveMessage((state as ActivationState.Inactive).reason))
        }
    }

    private fun inactiveMessage(reason: InactiveReason): Int = when (reason) {
        InactiveReason.NOT_AUTHENTICATED -> R.string.check_status_not_authenticated
        InactiveReason.EXPIRED -> R.string.check_status_expired
        InactiveReason.AUTH_FAILED -> R.string.check_status_failed
        InactiveReason.AUTH_CANCELED -> R.string.check_status_canceled
        InactiveReason.LEFT_APP -> R.string.check_status_left_app
        InactiveReason.SETUP_REQUIRED -> R.string.check_status_setup_required
        InactiveReason.UNAVAILABLE -> R.string.check_status_unavailable
    }

    private companion object {
        const val TICK_MILLIS = 250L
    }
}
