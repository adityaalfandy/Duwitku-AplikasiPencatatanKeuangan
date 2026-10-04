package com.pemmob.duwitku.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.pemmob.duwitku.util.CurrencyFormatter

@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }
            val limited = if (digits.length > 12) digits.take(12) else digits
            val noLeadingZeros = if (limited.length > 1 && limited.startsWith("0")) {
                limited.dropWhile { it == '0' }.ifEmpty { "0" }
            } else {
                limited
            }
            onValueChange(noLeadingZeros)
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = errorMessage != null,
        supportingText = errorMessage?.let { { Text(it) } },
        visualTransformation = CurrencyVisualTransformation()
    )
}

private class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        
        val formatted = CurrencyFormatter.formatRp(originalText.toLongOrNull() ?: 0)
        
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 3 // length of "Rp "
                if (offset >= originalText.length) return formatted.length
                
                var transformedCursor = 3
                var originalCursor = 0
                while (originalCursor < offset && transformedCursor < formatted.length) {
                    if (formatted[transformedCursor].isDigit()) {
                        originalCursor++
                    }
                    transformedCursor++
                }
                return transformedCursor
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return 0
                if (offset >= formatted.length) return originalText.length
                
                var originalCursor = 0
                for (i in 3 until offset) {
                    if (formatted[i].isDigit()) {
                        originalCursor++
                    }
                }
                return originalCursor
            }
        }
        
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
