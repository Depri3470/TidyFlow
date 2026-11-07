package com.depri.tidyflow.util

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Хэширование паролей: PBKDF2-HMAC-SHA256 + случайная соль. Формат: "соль:хэш". */
object PasswordHasher {

    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH = 256

    fun hash(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return "${toHex(salt)}:${toHex(derive(password, salt))}"
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val salt = fromHex(parts[0])
        return toHex(derive(password, salt)) == parts[1]
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    private fun fromHex(hex: String): ByteArray =
        hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
