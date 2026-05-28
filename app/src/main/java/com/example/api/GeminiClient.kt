package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiAnalysisResult(
    val priorityScore: Int,
    val toneScore: Int,
    val spamScore: Int,
    val summary: String,
    val smartReplies: List<String>
)

object GeminiClient {
    private const val TAG = "GeminiClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    suspend fun analyzeMessage(sender: String, intent: String, text: String): AiAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or placeholder!")
            return@withContext AiAnalysisResult(
                priorityScore = (40..90).random(),
                toneScore = 80,
                spamScore = 10,
                summary = "AI Summarizer placeholder (Key missing or placeholder: Please set GEMINI_API_KEY).",
                smartReplies = listOf("Thank you, let's collaborate!", "Let's align this week.", "Please tell me more.")
            )
        }

        val prompt = """
            Analyze this professional connection message sent to a recipient.
            Sender: $sender
            Intent: $intent
            Message: $text
            
            Provide a response strictly in JSON format with these exact keys:
            "priorityScore": (integer between 0 and 100 based on professional value and relevance)
            "toneScore": (integer between 0 and 100 representing courtesy and professionalism)
            "spamScore": (integer between 0 and 100 representing probability of being spam or copy-paste mass email)
            "summary": (exactly one line summarizing the core opportunity or request)
            "smartReplies": (list of exactly 3 short, relevant contextual replies the recipient can click)
            
            Ensure the response is valid JSON and contains absolutely no markdown formatting outside the JSON block. Do not wrap in ```json or ```.
        """.trimIndent()

        try {
            val jsonResponse = callGeminiRaw(apiKey, prompt)
            val cleanJson = sanitizeJsonString(jsonResponse)
            val json = JSONObject(cleanJson)
            val repliesArray = json.optJSONArray("smartReplies")
            val replies = mutableListOf<String>()
            if (repliesArray != null) {
                for (i in 0 until repliesArray.length()) {
                    replies.add(repliesArray.getString(i))
                }
            } else {
                replies.addAll(listOf("Sounds interesting, tell me more!", "Let's jump on a call.", "Thanks for reaching out!"))
            }

            AiAnalysisResult(
                priorityScore = json.optInt("priorityScore", 70),
                toneScore = json.optInt("toneScore", 80),
                spamScore = json.optInt("spamScore", 10),
                summary = json.optString("summary", "New connection request regarding $intent"),
                smartReplies = replies
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing message: ${e.message}", e)
            AiAnalysisResult(
                priorityScore = 75,
                toneScore = 85,
                spamScore = 5,
                summary = text.take(50) + "...",
                smartReplies = listOf("Interesting, tell me more!", "Let's match next Monday.", "Thanks, let's proceed.")
            )
        }
    }

    suspend fun generateDraft(intent: String, recipientName: String, extraDetails: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Dear $recipientName,\n\nI am reaching out regarding a potential collaboration/opportunity ($intent). I would love to connect and align on how we can work together.\n\nBest regards,\nSender"
        }

        val prompt = """
            Draft a highly professional, engaging first-outreach message on the platform InReach.
            Recipient: $recipientName
            Intent/Type: $intent
            Focus details: $extraDetails
            
            Keep the draft beautiful, compelling, and under 150 words. Write straight to the point.
        """.trimIndent()

        try {
            val text = callGeminiText(apiKey, prompt)
            if (text.isNotEmpty()) text else "Draft placeholder for $intent"
        } catch (e: Exception) {
            "Professional draft for $intent regarding $extraDetails"
        }
    }

    suspend fun generateMeetingAgenda(intent: String, title: String, notesContext: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "1. Welcome & Brief Intros (5m)\n2. Aligning on $intent core objectives\n3. Reviewing shared whiteboard and sticky ideas\n4. Defining milestones and assigning task deadlines\n5. Next Steps & Action Items"
        }

        val prompt = """
            Create a highly structured, 5-point meeting agenda for an upcoming alignment call.
            Project Context: $title
            Aesthetic Intent: $intent
            Workspace Notes so far: $notesContext
            
            Respond with direct, actionable discussion items and time splits (e.g. 10 mins).
        """.trimIndent()

        try {
            callGeminiText(apiKey, prompt)
        } catch (e: Exception) {
            "1. Alignment on $title (15m)\n2. Next steps on deliverables"
        }
    }

    suspend fun summarizeWorkspace(text: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Collaboration is active. Milestones are set, tasks are progress-checked daily. High momentum reported between team members."
        }

        val prompt = """
            Summarize the workspace progress and outcomes into a concise 'Proof of Work' shareable paragraph.
            Notes and activities details:
            $text
            
            Make it look professional, inspiring and highlight verified progress. Keep it under 80 words.
        """.trimIndent()

        try {
            callGeminiText(apiKey, prompt)
        } catch (e: Exception) {
            "Verified outcome of joint collaboration."
        }
    }

    suspend fun translateText(text: String, toTamil: Boolean): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext if (toTamil) "வணக்கம்! (Tamil: Connection translation is active)" else "Hello! (English: Translation is active)"
        }

        val prompt = if (toTamil) {
            "Translate this professional message to elegant Tamil. Keep it formal and polished:\n$text"
        } else {
            "Translate this Tamil message to elegant professional English:\n$text"
        }

        try {
            callGeminiText(apiKey, prompt)
        } catch (e: Exception) {
            text
        }
    }

    private suspend fun callGeminiText(apiKey: String, prompt: String): String {
        val json = callGeminiRaw(apiKey, prompt)
        try {
            val root = JSONObject(json)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response: ${e.message}")
        }
        return ""
    }

    private fun callGeminiRaw(apiKey: String, prompt: String): String {
        val url = "$BASE_URL?key=$apiKey"
        val requestBodyJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObject = JSONObject()
        val partsArray = JSONArray()
        val partObject = JSONObject()

        partObject.put("text", prompt)
        partsArray.put(partObject)
        contentObject.put("parts", partsArray)
        contentsArray.put(contentObject)
        requestBodyJson.put("contents", contentsArray)

        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorMsg = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "Gemini call failed with code ${response.code}: $errorMsg")
                throw Exception("Gemini service failed: $errorMsg")
            }
            return response.body?.string() ?: ""
        }
    }

    private fun sanitizeJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```")) {
            val firstLineEnd = str.indexOf("\n")
            if (firstLineEnd != -1) {
                str = str.substring(firstLineEnd + 1)
            }
            if (str.endsWith("```")) {
                str = str.substring(0, str.length - 3)
            }
        }
        return str.trim()
    }
}
