package com.example.data.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object SecurityHelper {
    
    /**
     * Generates a secure, cryptographically random salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    }

    /**
     * Hashes a password with a unique salt using SHA-256.
     */
    fun hashPassword(password: String, salt: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
            digest.update(saltBytes)
            val hashedBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    /**
     * Verifies if a given raw password matches the stored password hash.
     */
    fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
        if (password.isEmpty() || salt.isEmpty() || storedHash.isEmpty()) return false
        val computedHash = hashPassword(password, salt)
        return computedHash == storedHash
    }

    /**
     * Validates if a password meets strong safety requirements (min 6 chars, alphanumeric/special).
     */
    fun isPasswordStrong(password: String): Boolean {
        if (password.length < 6) return false
        var hasLetter = false
        var hasDigit = false
        for (char in password) {
            if (char.isLetter()) hasLetter = true
            if (char.isDigit()) hasDigit = true
        }
        return hasLetter && hasDigit
    }
}
