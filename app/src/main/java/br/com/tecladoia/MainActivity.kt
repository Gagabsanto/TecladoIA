package br.com.tecladoia

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Color
import android.view.inputmethod.InputMethodManager
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("teclado_ia", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(18, 18, 18)
        window.navigationBarColor = Color.BLACK
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 36, 24, 24)
            setBackgroundColor(Color.rgb(18, 18, 18))
        }
        fun label(s: String, size: Float) = TextView(this).apply {
            text = s; textSize = size; setTextColor(Color.WHITE); setPadding(0, 10, 0, 10)
        }
        fun button(s: String, onClick: () -> Unit) = Button(this).apply {
            text = s; isAllCaps = false; setOnClickListener { onClick() }
        }

        root.addView(label("Teclado IA", 28f))
        root.addView(label("Teclado Android com correção local e IA online.", 16f))

        // Passo 1: chave
        root.addView(label("1. Pegue sua chave grátis do Gemini", 18f))
        root.addView(label(
            "A assinatura Gemini Pro não inclui chave de API. A chave é gerada à parte, de graça, no Google AI Studio: " +
                "entre com sua conta Google, toque em \"Criar chave de API\", copie e volte aqui.", 14f))
        root.addView(button("Obter chave grátis no Google") {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey")))
        })

        root.addView(label("2. Cole a chave aqui", 18f))
        val key = EditText(this).apply {
            hint = "Cole sua chave aqui"
            setHintTextColor(Color.LTGRAY); setTextColor(Color.WHITE); setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(prefs.getString("gemini_key", "") ?: "")
        }
        root.addView(key, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(button("Colar do que copiei") {
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val text = cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()?.trim().orEmpty()
            if (text.isBlank()) Toast.makeText(this, "Nada copiado ainda.", Toast.LENGTH_SHORT).show()
            else key.setText(text)
        })
        root.addView(button("Salvar chave") {
            prefs.edit().putString("gemini_key", key.text.toString().trim()).apply()
            Toast.makeText(this, "Chave salva neste aparelho.", Toast.LENGTH_SHORT).show()
        })

        // Passo 3: ativar
        root.addView(label("3. Ative o teclado", 18f))
        root.addView(label("Ligue o Teclado IA na lista e depois selecione-o como teclado.", 14f))
        root.addView(button("Ativar teclado") {
            startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS"))
        })
        root.addView(button("Selecionar teclado") {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        })

        root.addView(label("A IA envia o texto próximo ao cursor para correção somente quando você toca no botão IA. Não use em campos de senha. Chaves em apps clientes não ficam totalmente protegidas; não compartilhe APK com chave configurada.", 13f))

        setContentView(ScrollView(this).apply { addView(root) })
    }
}
