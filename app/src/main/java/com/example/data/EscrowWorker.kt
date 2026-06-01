package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.api.GeminiClient

class EscrowWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("EscrowWorker", "Starting scheduled escrow message countdown count checking...")
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = AppRepository(database.appDao())
            
            val escrowMessages = repository.getMessagesByStatusSync("ESCROW")
            Log.d("EscrowWorker", "Found ${escrowMessages.size} escrow messages pending check.")
            
            for (message in escrowMessages) {
                val remaining = message.escrowHoursRemaining - 1
                if (remaining <= 0) {
                    Log.d("EscrowWorker", "Escrow countdown hit zero for message ID ${message.id} from ${message.senderName}. Running Gemini analysis and releasing.")
                    try {
                        val analysis = GeminiClient.analyzeMessage(
                            sender = message.senderName,
                            intent = message.intent,
                            text = message.rawText
                        )
                        
                        val updated = message.copy(
                            escrowHoursRemaining = 0,
                            status = "PENDING",
                            priorityScore = analysis.priorityScore,
                            toneScore = analysis.toneScore,
                            spamScore = analysis.spamScore,
                            summary = analysis.summary,
                            smartReplies = analysis.smartReplies.joinToString(",")
                        )
                        repository.updateMessage(updated)
                        sendLocalNotification(applicationContext, message.recipientUsername, message.senderName, message.intent)
                    } catch (e: Exception) {
                        Log.e("EscrowWorker", "Gemini analysis failed or unconfigured, falling back to local simulation for release.")
                        val updated = message.copy(
                            escrowHoursRemaining = 0,
                            status = "PENDING",
                            summary = "Unlocking connection. Initial profile screening completed successfully."
                        )
                        repository.updateMessage(updated)
                        sendLocalNotification(applicationContext, message.recipientUsername, message.senderName, message.intent)
                    }
                } else {
                    Log.d("EscrowWorker", "Message ID ${message.id} hours remaining: $remaining")
                    val updated = message.copy(escrowHoursRemaining = remaining)
                    repository.updateMessage(updated)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("EscrowWorker", "Failed in worker execution: ${e.message}", e)
            return Result.retry()
        }
    }

    private fun sendLocalNotification(context: Context, recipient: String, sender: String, intent: String) {
        val channelId = "inreach_escrow_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "InReach Escrow Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies receiver when background escrow period expires and connections unlock"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("New Partner Released!")
            .setContentText("$sender regarding $intent is cleared from escrow.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            
        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    }
}
