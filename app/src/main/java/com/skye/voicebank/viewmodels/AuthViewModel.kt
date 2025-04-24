package com.skye.voicebank.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.skye.voicebank.data.UserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val authResult: MutableLiveData<Result<FirebaseUser?>> = MutableLiveData()
    val signOutResult: MutableLiveData<Result<Void?>> = MutableLiveData()
    val isLoggedIn: MutableLiveData<Boolean> = MutableLiveData()

    init {
        updateCurrentUser()
    }

    fun signUp(email: String, password: String, userDetails: UserDetails) {
        viewModelScope.launch {
            val result = authRepository.signUp(email, password, userDetails)
            Log.d("AuthViewModel", "Signing up with email: $email")
            authResult.value = result
            updateCurrentUser()
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            authResult.value = result
            updateCurrentUser()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {

                val result = authRepository.signOut()
                signOutResult.value = result


                currentUser.postValue(null)


                isLoggedIn.postValue(false)


                val user = authRepository.getCurrentUser()
                if (user == null) {
                    Log.d("AuthViewModel", "User successfully logged out")
                } else {
                    Log.e("AuthViewModel", "User still logged in: ${user.uid}")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign out error: ${e.message}")
            }
        }
    }


    private fun updateCurrentUser() {
        val user = authRepository.getCurrentUser()
        currentUser.postValue(user)
        isLoggedIn.postValue(user != null)
    }



    fun fetchEmbeddings(onResult: (List<Float>?) -> Unit) {
        val userId = getCurrentUserId()
        Log.d("FRILL", "Fetching embeddings for userId: $userId")

        if (userId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val embeddings = authRepository.getUserEmbeddings(userId)
                Log.d("FRILL", "Fetched embeddings: $embeddings")
                onResult(embeddings)
            }
        } else {
            Log.e("FRILL", "User ID is null, cannot fetch embeddings")
            onResult(null)
        }
    }

    fun fetchEmbeddingsForLogin(uid: String, onResult: (List<Float>?) -> Unit) {
        Log.d("FRILL", "Fetching embeddings for UID: $uid")

        viewModelScope.launch(Dispatchers.IO) {
            try {

                val embeddings = authRepository.getUserEmbeddings(uid)
                Log.d("FRILL", "Fetched embeddings: $embeddings")
                onResult(embeddings)
            } catch (e: Exception) {
                Log.e("FRILL", "Error fetching embeddings: ${e.message}")
                onResult(null)
            }
        }
    }



    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    fun fetchUidByEmail(email: String, onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = authRepository.getUidByEmail(email)
            onResult(uid)
        }
    }

    fun getBalance(): Double {

        return 10000.0
    }

    fun creditAmount(amount: Double) {

    }

    fun debitAmount(amount: Double) {

    }

    fun sendMoney(toEmail: String, amount: Double, onResult: (Result<Unit>) -> Unit) {
        val fromUid = getCurrentUserId() ?: return onResult(Result.failure(Exception("User not logged in")))
        viewModelScope.launch {
            val result = authRepository.sendAmount(fromUid, toEmail, amount)
            onResult(result)
        }
    }


}
