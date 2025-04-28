package com.skye.voicebank.viewmodels

import com.google.firebase.auth.FirebaseUser
import com.skye.voicebank.data.UserDetails

interface AuthRepository {
    suspend fun signUp(email: String, password: String, userDetails: UserDetails): Result<FirebaseUser?>
    suspend fun signIn(email: String, password: String): Result<FirebaseUser?>
    suspend fun signOut(): Result<Void?>
    fun getCurrentUser(): FirebaseUser?
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    suspend fun getUserEmbeddings(userId: String): List<Float>?
    suspend fun getUidByEmail(email: String): String?
    suspend fun sendAmount(fromUid: String, toEmail: String, amount: Double): Result<Unit>
    suspend fun getTransactionHistory(uid: String): Result<List<Map<String, Any>>>


}
