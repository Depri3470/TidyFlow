package com.depri.tidyflow.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.lifecycle.lifecycleScope
import com.depri.tidyflow.R
import com.depri.tidyflow.databinding.ActivityLoginBinding
import com.depri.tidyflow.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets(binding.root)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim().lowercase()
        val password = binding.etPassword.text.toString()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        var valid = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.err_email)
            valid = false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.err_empty)
            valid = false
        }
        if (!valid) return

        binding.btnLogin.isEnabled = false
        lifecycleScope.launch {
            val user = db.userDao().findByEmail(email)
            val ok = user != null && withContext(Dispatchers.Default) {
                PasswordHasher.verify(password, user.passwordHash)
            }
            if (ok && user != null) {
                session.userId = user.id
                startActivity(
                    Intent(this@LoginActivity, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            } else {
                binding.tilPassword.error = getString(R.string.err_wrong_credentials)
                binding.btnLogin.isEnabled = true
            }
        }
    }
}
