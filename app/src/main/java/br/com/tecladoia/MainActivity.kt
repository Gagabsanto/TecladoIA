package br.com.tecladoia

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.inputmethod.InputMethodManager
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
        root.addView(label("Teclado IA", 28f))
        root.addView(label("Teclado Android com correção local e IA online.", 16f))
        root.addView(label("Chave da API Gemini:", 14f))
        val key = EditText(this).apply {
            hint = "Cole sua chave aqui"
            setHintTextColor(Color.LTGRAY); setTextColor(Color.WHITE); setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(prefs.getString("gemini_key", "") ?: "")
        }
        root.addView(key, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(Button(this).apply {
            text = "Salvar chave"
            setOnClickListener {
                prefs.edit().putString("gemini_key", key.text.toString().trim()).apply()
                Toast.makeText(this@MainActivity, "Chave salva neste aparelho.", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(label("Ative o teclado nas configurações do Android e depois selecione-o como teclado padrão.", 14f))
        root.addView(Button(this).apply {
            text = "Ativar teclado"
            setOnClickListener { startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS")) }
        })
        root.addView(Button(this).apply {
            text = "Selecionar teclado"
            setOnClickListener {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
            }
        })
        root.addView(label("A IA envia o texto próximo ao cursor para correção somente quando você toca no botão IA. Não use em campos de senha. Chaves em apps clientes não ficam totalmente protegidas; não compartilhe APK com chave configurada.", 13f))
        setContentView(root)
    }
}
