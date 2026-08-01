package com.sparkgym.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.browser.customtabs.CustomTabsIntent
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sparkgym.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Fitbit OAuth 2.0 with PKCE.
 *
 * PKCE means no client secret ever ships in the APK — the only thing embedded is
 * the public client id, and even that is supplied through local.properties so
 * the repository stays clean. Tokens live in EncryptedSharedPreferences.
 */
class FitbitAuth(private val context: Context) {

    companion object {
        private const val AUTHORIZE_URL = "https://www.fitbit.com/oauth2/authorize"
        private const val TOKEN_URL = "https://api.fitbit.com/oauth2/token"
        private const val REVOKE_URL = "https://api.fitbit.com/oauth2/revoke"

        /** Only what the app actually reads. Asking for less gets more approvals. */
        private val SCOPES = listOf("activity", "heartrate", "sleep", "profile", "weight")

        private const val PREFS = "sparkgym_secure_tokens"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EXPIRES = "expires_at"
        private const val KEY_USER = "fitbit_user_id"
        private const val KEY_VERIFIER = "pkce_verifier"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient()

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    val isConfigured: Boolean get() = BuildConfig.FITBIT_CLIENT_ID.isNotBlank()

    val isLinked: Boolean get() = prefs.getString(KEY_REFRESH, null) != null

    val linkedUserId: String? get() = prefs.getString(KEY_USER, null)

    // ---------------------------------------------------------------- authorize

    /** Launches the Fitbit consent screen in a Custom Tab. */
    fun beginAuthorization(activityContext: Context) {
        val verifier = generateCodeVerifier().also { prefs.edit().putString(KEY_VERIFIER, it).apply() }
        val challenge = codeChallenge(verifier)

        val uri = Uri.parse(AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("client_id", BuildConfig.FITBIT_CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("scope", SCOPES.joinToString(" "))
            .appendQueryParameter("redirect_uri", BuildConfig.FITBIT_REDIRECT_URI)
            .build()

        runCatching {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(activityContext, uri)
        }.onFailure {
            // No browser that supports Custom Tabs — fall back to any browser.
            activityContext.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    /** Handles the `sparkgym://fitbit-callback?code=…` redirect. */
    suspend fun completeAuthorization(redirect: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        val error = redirect.getQueryParameter("error")
        if (error != null) {
            return@withContext Result.failure(
                IllegalStateException(redirect.getQueryParameter("error_description") ?: error)
            )
        }
        val code = redirect.getQueryParameter("code")
            ?: return@withContext Result.failure(IllegalStateException("No authorization code in redirect"))
        val verifier = prefs.getString(KEY_VERIFIER, null)
            ?: return@withContext Result.failure(IllegalStateException("Missing PKCE verifier — start the link again"))

        val body = FormBody.Builder()
            .add("client_id", BuildConfig.FITBIT_CLIENT_ID)
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("code_verifier", verifier)
            .add("redirect_uri", BuildConfig.FITBIT_REDIRECT_URI)
            .build()

        exchange(body).map { persist(it) }
    }

    // ------------------------------------------------------------------ tokens

    /** Returns a valid access token, refreshing when it is within a minute of expiry. */
    suspend fun accessToken(): String? = withContext(Dispatchers.IO) {
        val current = prefs.getString(KEY_ACCESS, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES, 0)
        if (current != null && System.currentTimeMillis() < expiresAt - 60_000) return@withContext current

        val refresh = prefs.getString(KEY_REFRESH, null) ?: return@withContext null
        val body = FormBody.Builder()
            .add("client_id", BuildConfig.FITBIT_CLIENT_ID)
            .add("grant_type", "refresh_token")
            .add("refresh_token", refresh)
            .build()

        exchange(body).fold(
            onSuccess = { persist(it); it.accessToken },
            onFailure = { null }
        )
    }

    suspend fun unlink() = withContext(Dispatchers.IO) {
        val token = prefs.getString(KEY_ACCESS, null)
        if (token != null) {
            runCatching {
                val request = Request.Builder()
                    .url(REVOKE_URL)
                    .header("Authorization", "Bearer $token")
                    .post(
                        FormBody.Builder()
                            .add("client_id", BuildConfig.FITBIT_CLIENT_ID)
                            .add("token", token)
                            .build()
                    )
                    .build()
                http.newCall(request).execute().close()
            }
        }
        prefs.edit().clear().apply()
    }

    private fun exchange(body: FormBody): Result<TokenResponse> = runCatching {
        val request = Request.Builder().url(TOKEN_URL).post(body).build()
        http.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("Fitbit token request failed (${response.code}): $text")
            }
            json.decodeFromString(TokenResponse.serializer(), text)
        }
    }

    private fun persist(token: TokenResponse) {
        prefs.edit()
            .putString(KEY_ACCESS, token.accessToken)
            .putString(KEY_REFRESH, token.refreshToken)
            .putLong(KEY_EXPIRES, System.currentTimeMillis() + token.expiresIn * 1000L)
            .putString(KEY_USER, token.userId)
            .remove(KEY_VERIFIER)
            .apply()
    }

    // -------------------------------------------------------------------- PKCE

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun codeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    @Serializable
    private data class TokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("refresh_token") val refreshToken: String,
        @SerialName("expires_in") val expiresIn: Long,
        @SerialName("user_id") val userId: String = "",
        @SerialName("scope") val scope: String = ""
    )
}
