package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// ==========================================
// VIEWMODEL STATES
// ==========================================

sealed interface AuthUiState {
    object Unauthenticated : AuthUiState
    object Registering : AuthUiState
    object Loading : AuthUiState
    data class Authenticated(val user: UserEntity) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

data class ActiveCallState(
    val callId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String = "",
    val callType: String = "VOICE", // VOICE, VIDEO, GROUP_VOICE, GROUP_VIDEO
    val isOutgoing: Boolean = true,
    val callStage: CallStage = CallStage.IDLE,
    val durationSeconds: Long = 0L,
    val isMuted: Boolean = false,
    val isCameraOn: Boolean = true,
    val isNoiseSuppressionEnabled: Boolean = true,
    val isEchoCancellationEnabled: Boolean = true,
    val bitrateKbps: Int = 1200,
    val packetLossPercent: Double = 0.0,
    val resolution: String = "1080p (60fps)"
)

enum class CallStage {
    IDLE, RINGING, CONNECTING, ACTIVE, DISCONNECTED
}

class NovaChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NovaChatRepository(application)

    // Auth State
    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Unauthenticated)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // Chats & Current Selected Chat
    val activeChats: StateFlow<List<ChatEntity>> = repository.getChatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedChats: StateFlow<List<ChatEntity>> = repository.getArchivedChatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedChat = MutableStateFlow<ChatEntity?>(null)
    val selectedChat: StateFlow<ChatEntity?> = _selectedChat.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private var messagesJob: Job? = null

    // Contacts lists
    val friends: StateFlow<List<ContactEntity>> = repository.getFriendsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<ContactEntity>> = repository.getPendingRequestsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedUsers: StateFlow<List<ContactEntity>> = repository.getBlockedUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search Network state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResult = MutableStateFlow<ContactEntity?>(null)
    val searchResult: StateFlow<ContactEntity?> = _searchResult.asStateFlow()

    private val _isSearchingNetwork = MutableStateFlow(false)
    val isSearchingNetwork: StateFlow<Boolean> = _isSearchingNetwork.asStateFlow()

    // Call state
    private val _callState = MutableStateFlow(ActiveCallState())
    val callState: StateFlow<ActiveCallState> = _callState.asStateFlow()

    private var callTimerJob: Job? = null

    // Message reply & edit helpers
    private val _replyingToMessage = MutableStateFlow<MessageEntity?>(null)
    val replyingToMessage: StateFlow<MessageEntity?> = _replyingToMessage.asStateFlow()

    private val _editingMessage = MutableStateFlow<MessageEntity?>(null)
    val editingMessage: StateFlow<MessageEntity?> = _editingMessage.asStateFlow()

    // Self-destruct current active timer selection (0 = none)
    private val _selfDestructSeconds = MutableStateFlow(0L)
    val selfDestructSeconds: StateFlow<Long> = _selfDestructSeconds.asStateFlow()

    // App Preferences state
    private val _isDarkTheme = MutableStateFlow(true) // Defaults to modern slick dark theme
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Call logs
    val callLogs: StateFlow<List<CallLogEntity>> = repository.getCallLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // AUTH ACTIONS
    // ==========================================

    fun changeAuthStage(stage: AuthUiState) {
        _authUiState.value = stage
    }

    fun login(username: String, pword: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val user = repository.loginUser(username.trim().lowercase(), pword)
            if (user != null) {
                _authUiState.value = AuthUiState.Authenticated(user)
            } else {
                _authUiState.value = AuthUiState.Error("Invalid username or password. Check credentials.")
            }
        }
    }

    fun register(username: String, pword: String, dispName: String, bio: String) {
        viewModelScope.launch {
            if (username.length < 3 || pword.length < 4) {
                _authUiState.value = AuthUiState.Error("Username must be >= 3 and Password >= 4 characters.")
                return@launch
            }
            _authUiState.value = AuthUiState.Loading
            try {
                // Ensure unique username
                val existing = repository.database.userDao().getUserByUsername(username.trim().lowercase())
                if (existing != null) {
                    _authUiState.value = AuthUiState.Error("Username already taken. Please try another.")
                    return@launch
                }
                val user = repository.registerUser(username.trim().lowercase(), pword, dispName, bio)
                _authUiState.value = AuthUiState.Authenticated(user)
            } catch (e: Exception) {
                _authUiState.value = AuthUiState.Error(e.message ?: "Registration failed.")
            }
        }
    }

    fun logout() {
        val current = _authUiState.value
        if (current is AuthUiState.Authenticated) {
            viewModelScope.launch {
                repository.logoutUser(current.user.userId)
                _authUiState.value = AuthUiState.Unauthenticated
                _selectedChat.value = null
            }
        }
    }

    fun updateProfile(displayName: String, bio: String, avatarUrl: String, e2e: Boolean) {
        val current = _authUiState.value
        if (current is AuthUiState.Authenticated) {
            viewModelScope.launch {
                val updated = current.user.copy(
                    displayName = displayName,
                    bio = bio,
                    avatarUrl = avatarUrl,
                    isE2EEnabled = e2e
                )
                repository.updateUserProfile(updated)
                _authUiState.value = AuthUiState.Authenticated(updated)
            }
        }
    }

    // ==========================================
    // NAVIGATION & CHAT ACTIONS
    // ==========================================

    fun selectChat(chat: ChatEntity?) {
        _selectedChat.value = chat
        messagesJob?.cancel()
        if (chat != null) {
            // Reset unread counts on open
            viewModelScope.launch {
                repository.database.chatDao().resetUnreadCount(chat.chatId)
            }
            messagesJob = viewModelScope.launch {
                repository.getMessagesFlow(chat.chatId).collect { msgs ->
                    _messages.value = msgs
                    // Handle self-destruct counts
                    checkSelfDestructCountdowns(msgs)
                }
            }
        } else {
            _messages.value = emptyList()
        }
    }

    private fun checkSelfDestructCountdowns(msgs: List<MessageEntity>) {
        msgs.forEach { msg ->
            if (msg.isSelfDestruct) {
                val elapsed = (System.currentTimeMillis() - msg.timestamp) / 1000
                if (elapsed >= msg.selfDestructDuration) {
                    // Trigger immediate local removal
                    viewModelScope.launch {
                        repository.deleteMessageForEveryone(msg.messageId)
                    }
                }
            }
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // ==========================================
    // MESSAGING
    // ==========================================

    fun setReplyingTo(message: MessageEntity?) {
        _replyingToMessage.value = message
        _editingMessage.value = null
    }

    fun setEditing(message: MessageEntity?) {
        _editingMessage.value = message
        _replyingToMessage.value = null
    }

    fun setSelfDestruct(seconds: Long) {
        _selfDestructSeconds.value = seconds
    }

    fun sendTextMessage(text: String, mediaType: String = "NONE", mediaUrl: String = "") {
        val currentAuth = _authUiState.value as? AuthUiState.Authenticated ?: return
        val currentChat = _selectedChat.value ?: return

        viewModelScope.launch {
            if (_editingMessage.value != null) {
                repository.editMessage(_editingMessage.value!!.messageId, text)
                _editingMessage.value = null
            } else {
                repository.sendMessage(
                    chatId = currentChat.chatId,
                    senderId = currentAuth.user.userId,
                    senderName = currentAuth.user.displayName,
                    text = text,
                    mediaType = mediaType,
                    mediaUrl = mediaUrl,
                    replyToId = _replyingToMessage.value?.messageId,
                    replyToText = _replyingToMessage.value?.text,
                    selfDestructSec = _selfDestructSeconds.value
                )
                _replyingToMessage.value = null
            }
        }
    }

    fun deleteMessage(msg: MessageEntity, forEveryone: Boolean) {
        viewModelScope.launch {
            if (forEveryone) {
                repository.deleteMessageForEveryone(msg.messageId)
            } else {
                repository.deleteMessageForMe(msg.messageId)
            }
        }
    }

    fun reactToMessage(msg: MessageEntity, reaction: String) {
        val currentAuth = _authUiState.value as? AuthUiState.Authenticated ?: return
        viewModelScope.launch {
            repository.addReaction(msg.messageId, reaction, currentAuth.user.userId)
        }
    }

    fun createGroupChat(title: String, desc: String, avatar: String) {
        viewModelScope.launch {
            val newChat = repository.createGroupChat(title, desc, avatar)
            selectChat(newChat)
        }
    }

    fun createBroadcastChannel(title: String, desc: String, avatar: String) {
        viewModelScope.launch {
            val newChat = repository.createBroadcastChannel(title, desc, avatar)
            selectChat(newChat)
        }
    }

    fun togglePin(chat: ChatEntity) {
        viewModelScope.launch {
            repository.togglePinChat(chat.chatId)
        }
    }

    fun toggleArchive(chat: ChatEntity) {
        viewModelScope.launch {
            repository.toggleArchiveChat(chat.chatId)
            if (_selectedChat.value?.chatId == chat.chatId) {
                selectChat(null)
            }
        }
    }

    // ==========================================
    // CONTACTS & SEARCH
    // ==========================================

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResult.value = null
            _isSearchingNetwork.value = false
            return
        }

        _isSearchingNetwork.value = true
        viewModelScope.launch {
            delay(400) // Debounce search query input
            val res = repository.searchUserInNetwork(query)
            _searchResult.value = res
            _isSearchingNetwork.value = false
        }
    }

    fun addNewContact(userId: String, username: String, displayName: String, avatarUrl: String) {
        viewModelScope.launch {
            repository.sendFriendRequest(userId, username, displayName, avatarUrl)
        }
    }

    fun acceptRequest(userId: String) {
        viewModelScope.launch {
            repository.acceptFriendRequest(userId)
        }
    }

    fun rejectRequest(userId: String) {
        viewModelScope.launch {
            repository.rejectFriendRequest(userId)
        }
    }

    fun removeContact(userId: String) {
        viewModelScope.launch {
            repository.removeFriend(userId)
        }
    }

    fun blockContact(userId: String) {
        viewModelScope.launch {
            repository.blockUser(userId)
        }
    }

    fun reportContact(userId: String, reason: String) {
        viewModelScope.launch {
            repository.reportUser(userId, reason)
        }
    }

    // ==========================================
    // REALTIME WEBRTC CALL SIMULATION
    // ==========================================

    fun startCall(otherUserId: String, otherUserName: String, otherUserAvatar: String, type: String) {
        _callState.value = ActiveCallState(
            callId = "CALL-${UUID.randomUUID()}",
            otherUserId = otherUserId,
            otherUserName = otherUserName,
            otherUserAvatar = otherUserAvatar,
            callType = type,
            isOutgoing = true,
            callStage = CallStage.RINGING,
            durationSeconds = 0L,
            bitrateKbps = 1450,
            packetLossPercent = 0.05
        )

        // Simulated connect sequence
        viewModelScope.launch {
            delay(2500) // Rings for 2.5 seconds
            if (_callState.value.callStage == CallStage.RINGING) {
                _callState.value = _callState.value.copy(callStage = CallStage.CONNECTING)
                delay(1000) // Connect handshake
                _callState.value = _callState.value.copy(callStage = CallStage.ACTIVE)
                startCallTimer()
            }
        }
    }

    fun receiveIncomingCall(userId: String, name: String, avatar: String, type: String) {
        _callState.value = ActiveCallState(
            callId = "CALL-${UUID.randomUUID()}",
            otherUserId = userId,
            otherUserName = name,
            otherUserAvatar = avatar,
            callType = type,
            isOutgoing = false,
            callStage = CallStage.RINGING,
            durationSeconds = 0L,
            bitrateKbps = 980
        )
    }

    fun acceptCall() {
        val current = _callState.value
        if (current.callStage == CallStage.RINGING) {
            _callState.value = current.copy(callStage = CallStage.CONNECTING)
            viewModelScope.launch {
                delay(1000)
                _callState.value = _callState.value.copy(callStage = CallStage.ACTIVE)
                startCallTimer()
            }
        }
    }

    fun declineCall() {
        val current = _callState.value
        if (current.callStage != CallStage.IDLE) {
            viewModelScope.launch {
                repository.addCallLog(
                    otherUserId = current.otherUserId,
                    otherUserName = current.otherUserName,
                    otherUserAvatar = current.otherUserAvatar,
                    callType = current.callType,
                    isOutgoing = current.isOutgoing,
                    durationSeconds = 0,
                    status = if (current.isOutgoing) "COMPLETED" else "REJECTED"
                )
                _callState.value = current.copy(callStage = CallStage.DISCONNECTED)
                delay(800)
                _callState.value = ActiveCallState() // reset
            }
        }
    }

    fun hangUpCall() {
        val current = _callState.value
        callTimerJob?.cancel()
        if (current.callStage != CallStage.IDLE) {
            viewModelScope.launch {
                repository.addCallLog(
                    otherUserId = current.otherUserId,
                    otherUserName = current.otherUserName,
                    otherUserAvatar = current.otherUserAvatar,
                    callType = current.callType,
                    isOutgoing = current.isOutgoing,
                    durationSeconds = current.durationSeconds,
                    status = "COMPLETED"
                )
                _callState.value = current.copy(callStage = CallStage.DISCONNECTED)
                delay(800)
                _callState.value = ActiveCallState() // reset
            }
        }
    }

    fun toggleMute() {
        _callState.value = _callState.value.copy(isMuted = !_callState.value.isMuted)
    }

    fun toggleCamera() {
        _callState.value = _callState.value.copy(isCameraOn = !_callState.value.isCameraOn)
    }

    fun toggleNoiseSuppression() {
        _callState.value = _callState.value.copy(isNoiseSuppressionEnabled = !_callState.value.isNoiseSuppressionEnabled)
    }

    fun toggleEchoCancellation() {
        _callState.value = _callState.value.copy(isEchoCancellationEnabled = !_callState.value.isEchoCancellationEnabled)
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _callState.value
                if (current.callStage == CallStage.ACTIVE) {
                    // Slightly jitter bitrate and packet loss for live visual realistic indicators
                    val deltaBitrate = (-30..30).random()
                    val packetJitter = if ((0..5).random() == 1) (0..3).random().toDouble() / 10.0 else current.packetLossPercent
                    _callState.value = current.copy(
                        durationSeconds = current.durationSeconds + 1,
                        bitrateKbps = (current.bitrateKbps + deltaBitrate).coerceIn(800, 2000),
                        packetLossPercent = packetJitter
                    )
                } else {
                    break
                }
            }
        }
    }

    fun triggerMockIncomingCall() {
        viewModelScope.launch {
            // Trigger an incoming video call from Bob after a brief delay
            delay(1000)
            receiveIncomingCall(
                userId = "NC-88204910",
                name = "Bob Signals",
                avatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
                type = "VIDEO"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        messagesJob?.cancel()
    }
}
