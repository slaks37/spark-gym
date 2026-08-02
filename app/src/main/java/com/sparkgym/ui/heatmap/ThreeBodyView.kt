package com.sparkgym.ui.heatmap

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.model.Muscle
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ThreeBodyView(
    heat: Map<Muscle, HeatmapEngine.MuscleHeat>,
    angle: ViewAngle,
    modifier: Modifier = Modifier,
    onMuscleTap: (Muscle?) -> Unit = {}
) {
    val bridge = remember(onMuscleTap) {
        object {
            @JavascriptInterface
            fun onMuscleTapped(muscleName: String) {
                val muscle = Muscle.entries.firstOrNull { it.name.equals(muscleName, ignoreCase = true) }
                onMuscleTap(muscle)
            }
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val json = JSONObject()
                        heat.forEach { (m, h) -> json.put(m.name, h.intensity) }
                        evaluateJavascript("window.updateMuscleHeat('${json}')", null)
                        evaluateJavascript("window.setAngle(${angle.degrees})", null)
                    }
                }
                addJavascriptInterface(bridge, "AndroidBridge")
                loadUrl("file:///android_asset/3d_body/index.html")
            }
        },
        update = { webView ->
            val json = JSONObject()
            heat.forEach { (m, h) -> json.put(m.name, h.intensity) }
            webView.evaluateJavascript("window.updateMuscleHeat('${json}')", null)
            webView.evaluateJavascript("window.setAngle(${angle.degrees})", null)
        },
        modifier = modifier
    )
}
