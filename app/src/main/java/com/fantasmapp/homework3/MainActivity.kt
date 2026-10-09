package com.fantasmapp.homework3

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fantasmapp.homework3.ui.theme.Homework3Theme

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TEXT = "com.fantasmapp.authorside.EXTRA_TEXT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainScreen(
                    onOpenSecond = ::openSecondActivity,
                    onDial = ::openDialer,
                    onShare = ::shareText
                )
            }
        }
    }

    // 1. Явный Intent
    private fun openSecondActivity(text: String) {
        val intent = Intent(
            this,
            SecondActivity::class.java
        ).apply {
            putExtra(EXTRA_TEXT, text)
        }

        startActivity(intent)
    }

    // 2. Неявный Intent
    private fun openDialer(phone: String) {
        val normalizedPhone = phone.filter {
            it.isDigit() || it == '+'
        }

        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", normalizedPhone, null)
        )

        launchSafely(intent)
    }

    // 3. Системное меню отправки
    private fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        val chooser = Intent.createChooser(
            sendIntent,
            "Поделиться через"
        )

        launchSafely(chooser)
    }

    private fun launchSafely(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Подходящее приложение не найдено",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
private fun MainScreen(
    onOpenSecond: (String) -> Unit,
    onDial: (String) -> Unit,
    onShare: (String) -> Unit
) {
    var input by rememberSaveable {
        mutableStateOf("")
    }

    var error by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    fun getValidatedText(): String? {
        val text = input.trim()

        if (text.isEmpty()) {
            error = "Поле не должно быть пустым"
            return null
        }

        error = null
        return text
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Практическое задание №3",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Работа с Android Intent",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it
                    error = null
                },
                label = {
                    Text("Текст или номер телефона")
                },
                isError = error != null,
                supportingText = {
                    error?.let { message ->
                        Text(message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    getValidatedText()?.let(onOpenSecond)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Открыть вторую Activity")
            }

            Button(
                onClick = {
                    val phone = getValidatedText()
                        ?: return@Button

                    if (!isValidPhone(phone)) {
                        error = "Некорректный номер телефона"
                        return@Button
                    }

                    onDial(phone)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Позвонить другу")
            }

            OutlinedButton(
                onClick = {
                    getValidatedText()?.let(onShare)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Поделиться текстом")
            }
        }
    }
}

private fun isValidPhone(phone: String): Boolean {
    val pattern = Regex("""^\+?[0-9()\s-]+$""")
    val digitCount = phone.count { it.isDigit() }

    return pattern.matches(phone) &&
            digitCount in 5..15
}