package com.skye.voicebank.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.skye.voicebank.data.UserDetails
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.skye.voicebank.utils.AudioProcessor
import com.skye.voicebank.utils.FRILLModel


class FirebaseAuthRepository(private val frillModel: FRILLModel, private val context: Context): AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val audioProcessor = AudioProcessor()

    @SuppressLint(
        "MissingPermission"
    )
    override suspend fun signUp(email: String, password: String, userDetails: UserDetails): Result<FirebaseUser?> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            user?.let {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(userDetails.name)
                    .build()
                it.updateProfile(profileUpdates).await()
                val embedding = audioProcessor.recordAndProcessAudio(frillModel)?.toList() ?: emptyList()
                val userMap = mapOf(
                    "name" to userDetails.name,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis(),
                    "voiceEmbedding" to embedding,
                    "balance" to 10000.0,
                    "transactionHistory" to emptyList<Map<String, Any>>()
                )
                firestore.collection("users").document(it.uid).set(userMap).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Void?> {
        return try {
            firebaseAuth.signOut()
            forceClearSession()
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun getUserEmbeddings(userId: String): List<Float>? {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            snapshot.get("voiceEmbedding") as? List<Float>
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUidByEmail(email: String): String? {
        Log.d("FIRESTORE", "Looking up UID for email: $email")
        val querySnapshot = firestore.collection("users")
            .whereEqualTo("email", email.trim().lowercase())
            .limit(1)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            val uid = querySnapshot.documents[0].id
            Log.d("FIRESTORE", "Found UID: $uid for email: $email")
            uid
        } else {
            Log.e("FIRESTORE", "No user found with email: $email")
            null
        }
    }


    private fun forceClearSession() {
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        Log.d("FirebaseAuthRepository", "Firebase session cleared")
    }

    override suspend fun sendAmount(fromUid: String, toEmail: String, amount: Double): Result<Unit> {
        return try {
            val toUid = getUidByEmail(toEmail)
            if (toUid == null) return Result.failure(Exception("Recipient not found"))
            val fromRef = firestore.collection("users").document(fromUid)
            val toRef = firestore.collection("users").document(toUid)
            firestore.runTransaction { transaction ->
                val fromSnapshot = transaction.get(fromRef)
                val toSnapshot = transaction.get(toRef)
                val fromBalance = fromSnapshot.getDouble("balance") ?: 0.0
                if (fromBalance < amount) throw Exception("Insufficient funds")
                val toBalance = toSnapshot.getDouble("balance") ?: 0.0
                transaction.update(fromRef, "balance", fromBalance - amount)
                transaction.update(toRef, "balance", toBalance + amount)
                val now = System.currentTimeMillis()
                val fromTxn = mapOf("type" to "debit", "amount" to amount, "to" to toEmail, "timestamp" to now)
                val toTxn = mapOf("type" to "credit", "amount" to amount, "from" to firebaseAuth.currentUser?.email, "timestamp" to now)
                transaction.update(fromRef, "transactionHistory", FieldValue.arrayUnion(fromTxn))
                transaction.update(toRef, "transactionHistory", FieldValue.arrayUnion(toTxn))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactionHistory(uid: String): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val history = snapshot.get("transactionHistory") as? List<Map<String, Any>> ?: emptyList()
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




}