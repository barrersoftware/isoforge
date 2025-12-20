package com.barrersoftware.isotool
import java.io.InputStream
class IsoValidator {
    fun isValidIso(stream: InputStream): ValidationResult {
        return try {
            val header = ByteArray(32768)
            stream.read(header)
            val isoSignature = String(header, 32769 - 6, 5, Charsets.UTF_8)
            if (isoSignature == "CD001") {
                ValidationResult.Valid("ISO 9660 filesystem detected")
            } else {
                ValidationResult.Invalid("Not a valid ISO file")
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Error reading file: ${e.message}")
        }
    }
    sealed class ValidationResult {
        data class Valid(val message: String) : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
}
