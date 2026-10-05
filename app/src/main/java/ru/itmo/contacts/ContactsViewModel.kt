package ru.itmo.contacts

import android.app.Application
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ContactsUiState {
    data object Loading : ContactsUiState
    data class Loaded(val sections: List<ContactSection>, val count: Int) : ContactsUiState
}

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val contentResolver = application.contentResolver
    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private var observer: ContentObserver? = null
    private var loadJob: Job? = null

    fun onPermissionGranted() {
        if (observer != null) return
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = load()
        }.also { contentResolver.registerContentObserver(Phone.CONTENT_URI, true, it) }
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val contacts = withContext(Dispatchers.IO) { contentResolver.fetchContacts() }
            _uiState.value = ContactsUiState.Loaded(contacts.groupByInitial(), contacts.size)
        }
    }

    override fun onCleared() {
        observer?.let(contentResolver::unregisterContentObserver)
    }
}
