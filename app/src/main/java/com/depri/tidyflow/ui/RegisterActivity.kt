package com.depri.tidyflow.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.lifecycle.lifecycleScope
import com.depri.tidyflow.R
import com.depri.tidyflow.data.User
import com.depri.tidyflow.databinding.ActivityRegisterBinding
import com.depri.tidyflow.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets(binding.root)

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.btnToLogin.setOnClickListener { finish() }
    }

    private fun attemptRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim().lowercase()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirm.text.toString()

        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirm.error = null

        var valid = true
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.err_empty)
            valid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.err_email)
            valid = false
        }
        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.err_password_short)
            valid = false
        }
        if (confirm != password) {
            binding.tilConfirm.error = getString(R.string.err_password_mismatch)
            valid = false
        }
        if (!valid) return

        binding.btnRegister.isEnabled = false
        lifecycleScope.launch {
            if (db.userDao().findByEmail(email) != null) {
                binding.tilEmail.error = getString(R.string.err_email_taken)
                binding.btnRegister.isEnabled = true
                return@launch
            }
            val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password) }
            val id = db.userDao().insert(User(name = name, email = email, passwordHash = hash))
            session.userId = id
            startActivity(
                Intent(this@RegisterActivity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }
    }
}
