package br.com.tecladoia

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.util.concurrent.Executors

class AiKeyboardService : InputMethodService() {
    private val worker = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private val keyBg = Color.rgb(48, 49, 51)
    private val accent = Color.rgb(190, 93, 218)

    override fun onCreateInputView(): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(5, 7, 5, 8)
            setBackgroundColor(Color.rgb(25, 27, 29))
        }
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        toolbar.addView(key("✦ IA", accent) { correctWithAi() }, params(1f))
        toolbar.addView(key("⌫", keyBg) { currentInputConnection?.deleteSurroundingText(1, 0) }, params(1f))
        toolbar.addView(key("↵", keyBg) { currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER)) }, params(1f))
        layout.addView(toolbar)

        val rows = listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("⇧","z","x","c","v","b","n","m","⌫"),
            listOf("?123",",","☺","ESPAÇO",".","↵")
        )
        rows.forEach { row ->
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEach { value ->
                val b = key(value, keyBg) {
                    when (value) {
                        "⌫" -> currentInputConnection?.deleteSurroundingText(1, 0)
                        "↵" -> currentInputConnection?.performEditorAction(currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE)
                        "ESPAÇO" -> insertSpaceWithLocalCorrection()
                        "☺" -> currentInputConnection?.commitText("😊", 1)
                        "⇧" -> Toast.makeText(this, "Maiúsculas serão aprimoradas em uma próxima versão.", Toast.LENGTH_SHORT).show()
                        "?123" -> Toast.makeText(this, "Números e símbolos serão adicionados depois.", Toast.LENGTH_SHORT).show()
                        else -> currentInputConnection?.commitText(value, 1)
                    }
                }
                val weight = if (value == "ESPAÇO") 3.8f else if (value in listOf("?123","⇧","⌫","↵")) 1.3f else 1f
                line.addView(b, params(weight))
            }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48))
            lp.topMargin = dp(3)
            layout.addView(line, lp)
        }
        return layout
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun params(weight: Float) = LinearLayout.LayoutParams(0, dp(48), weight).apply {
        setMargins(dp(2), dp(2), dp(2), dp(2))
    }

    private fun key(label: String, color: Int, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = if (label == "ESPAÇO") 11f else 16f
        setTextColor(Color.WHITE); isAllCaps = false; setPadding(0, 0, 0, 0)
        background = GradientDrawable().apply { setColor(color); cornerRadius = dp(7).toFloat() }
        minWidth = 0; minimumWidth = 0; minHeight = dp(42); minimumHeight = dp(42)
        setOnClickListener { action() }
    }

    private fun insertSpaceWithLocalCorrection() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(40, 0)?.toString().orEmpty()
        val match = Regex("""(\p{L}+)$""").find(before)
        if (match != null) {
            val word = match.value
            val fixes = mapOf(
                "vc" to "você", "vcs" to "vocês", "hj" to "hoje",
                "tb" to "também", "tbm" to "também", "pq" to "porque",
                "q" to "que", "oq" to "o que", "nao" to "não",
                "eh" to "é", "obg" to "obrigado", "blz" to "beleza"
            )
            val fixed = fixes[word.lowercase()]
            if (fixed != null) {
                val replacement = if (word.firstOrNull()?.isUpperCase() == true)
                    fixed.replaceFirstChar { it.uppercase() } else fixed
                ic.deleteSurroundingText(word.length, 0)
                ic.commitText("$replacement ", 1)
                return
            }
        }
        ic.commitText(" ", 1)
    }

    private fun correctWithAi() {
        val ic = currentInputConnection ?: return
        val original = ic.getTextBeforeCursor(1500, 0)?.toString().orEmpty()
        if (original.isBlank()) {
            Toast.makeText(this, "Digite uma mensagem antes de usar a IA.", Toast.LENGTH_SHORT).show()
            return
        }
        val apiKey = getSharedPreferences("teclado_ia", MODE_PRIVATE).getString("gemini_key", "")?.trim().orEmpty()
        if (apiKey.isBlank()) {
            Toast.makeText(this, "Abra Teclado IA e configure a chave Gemini.", Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(this, "Corrigindo com IA…", Toast.LENGTH_SHORT).show()
        worker.execute {
            try {
                val endpoint = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                val conn = endpoint.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"; conn.connectTimeout = 15000; conn.readTimeout = 20000; conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("x-goog-api-key", apiKey)
                val prompt = "Corrija em português brasileiro a ortografia, abreviações comuns e pontuação. Preserve sentido e tom informal. Não explique, não use aspas. Retorne somente o texto corrigido. Texto: $original"
                val body = JSONObject().put("contents", org.json.JSONArray().put(
                    JSONObject().put("parts", org.json.JSONArray().put(JSONObject().put("text", prompt)))
                )).put("generationConfig", JSONObject().put("temperature", 0.2).put("maxOutputTokens", 512))
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
                val status = conn.responseCode
                val stream = if (status in 200..299) conn.inputStream else conn.errorStream
                val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                conn.disconnect()
                if (status !in 200..299) throw Exception("HTTP $status")
                val corrected = JSONObject(response).getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text").trim()
                handler.post {
                    val active = currentInputConnection ?: return@post
                    active.deleteSurroundingText(original.length, 0)
                    active.commitText(corrected, 1)
                    Toast.makeText(this, "Texto corrigido.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handler.post { Toast.makeText(this, "Falha na IA. Confira internet, chave e limite da API.", Toast.LENGTH_LONG).show() }
            }
        }
    }
}
