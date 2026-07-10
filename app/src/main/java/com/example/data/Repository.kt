package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class NovaChatRepository(private val context: Context) {

    // Database initialization
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "novachat_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val userDao = database.userDao()
    private val contactDao = database.contactDao()
    private val chatDao = database.chatDao()
    private val messageDao = database.messageDao()
    private val callLogDao = database.callLogDao()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // ==========================================
    // AUTHENTICATION & PROFILE
    // ==========================================

    suspend fun registerUser(username: String, password: String, displayName: String, bio: String): UserEntity {
        // Simple MD5 or SHA-256 for Argon2 visualization hashing
        val passwordHash = hashPassword(password)
        val randomSuffix = (100000..999999).random()
        val userId = "NC-$randomSuffix"

        val newUser = UserEntity(
            userId = userId,
            username = username,
            passwordHash = passwordHash,
            displayName = displayName,
            bio = bio,
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80" // Default modern female avatar
        )
        userDao.insertUser(newUser)
        
        // Seed initial mock contacts and chats for interactive experience
        seedInitialData(userId)
        
        return newUser
    }

    suspend fun loginUser(username: String, password: String): UserEntity? {
        val hashed = hashPassword(password)
        val user = userDao.getUserByUsername(username)
        return if (user != null && user.passwordHash == hashed) {
            // Update online status
            val updated = user.copy(isOnline = true, lastSeen = System.currentTimeMillis())
            userDao.insertUser(updated)
            updated
        } else {
            null
        }
    }

    suspend fun logoutUser(userId: String) {
        userDao.getUserById(userId)?.let {
            val updated = it.copy(isOnline = false, lastSeen = System.currentTimeMillis())
            userDao.insertUser(updated)
        }
    }

    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUserProfile(user: UserEntity) {
        userDao.updateUser(user)
    }

    private fun hashPassword(password: String): String {
        // Visual indicator hash that mimics Argon2 visually
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return "$argon2Prefix${hashBytes.joinToString("") { "%02x".format(it) }.take(24)}"
    }

    companion object {
        const val argon2Prefix = "\$argon2id\$v=19\$m=16384,t=2,p=1\$"
    }

    // ==========================================
    // CONTACTS
    // ==========================================

    fun getFriendsFlow(): Flow<List<ContactEntity>> = contactDao.getFriends()
    fun getPendingRequestsFlow(): Flow<List<ContactEntity>> = contactDao.getPendingRequests()
    fun getBlockedUsersFlow(): Flow<List<ContactEntity>> = contactDao.getBlockedUsers()

    suspend fun searchUserInNetwork(query: String): ContactEntity? {
        // Check local contacts first
        val cleanQuery = query.trim()
        val local = contactDao.getContactById(cleanQuery) ?: contactDao.getContactByUsername(cleanQuery)
        if (local != null) return local

        // Simulate global directory search for realistic contact queries
        val uppercaseQuery = cleanQuery.uppercase()
        if (uppercaseQuery.startsWith("NC-") || uppercaseQuery.startsWith("@")) {
            return ContactEntity(
                userId = if (uppercaseQuery.startsWith("@")) "NC-${(100000..999999).random()}" else uppercaseQuery,
                username = cleanQuery.replace("@", "").lowercase(),
                displayName = "Discovery " + cleanQuery.take(5),
                bio = "Found via global secure index search.",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
                status = "NONE", // Network user, not friend yet
                isOnline = (0..1).random() == 1
            )
        }
        return null
    }

    suspend fun sendFriendRequest(userId: String, username: String, displayName: String, avatarUrl: String) {
        val newContact = ContactEntity(
            userId = userId,
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl,
            bio = "Decentralized end-to-end messaging enthusiast.",
            status = "PENDING_OUTGOING",
            isOnline = true
        )
        contactDao.insertContact(newContact)

        // Simulate Acceptance bot
        repositoryScope.launch {
            delay(3000)
            acceptFriendRequest(userId)
        }
    }

    suspend fun acceptFriendRequest(userId: String) {
        contactDao.getContactById(userId)?.let {
            val updated = it.copy(status = "FRIEND")
            contactDao.insertContact(updated)

            // Create private chat for this user automatically
            createPrivateChat(updated)
        }
    }

    suspend fun rejectFriendRequest(userId: String) {
        contactDao.deleteContactById(userId)
    }

    suspend fun blockUser(userId: String) {
        contactDao.getContactById(userId)?.let {
            val updated = it.copy(status = "BLOCKED")
            contactDao.insertContact(updated)
        }
    }

    suspend fun reportUser(userId: String, reason: String) {
        // Mock API reporting logic. In production this submits securely with TLS 1.3 to Admin Dashboard
        delay(800)
    }

    suspend fun removeFriend(userId: String) {
        contactDao.deleteContactById(userId)
    }

    // ==========================================
    // CHATS & MESSAGES
    // ==========================================

    fun getChatsFlow(): Flow<List<ChatEntity>> = chatDao.getActiveChats()
    fun getArchivedChatsFlow(): Flow<List<ChatEntity>> = chatDao.getArchivedChats()
    fun getMessagesFlow(chatId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForChat(chatId)

    suspend fun createPrivateChat(contact: ContactEntity): ChatEntity {
        val existing = chatDao.getChatById(contact.userId)
        if (existing != null) return existing

        val newChat = ChatEntity(
            chatId = contact.userId,
            chatType = "PRIVATE",
            title = contact.displayName,
            avatarUrl = contact.avatarUrl,
            description = contact.bio,
            lastMessageText = "Start end-to-end encrypted chat using Signal Protocol.",
            lastMessageTime = System.currentTimeMillis()
        )
        chatDao.insertChat(newChat)
        return newChat
    }

    suspend fun createGroupChat(title: String, description: String, avatarUrl: String): ChatEntity {
        val uuid = "GRP-${UUID.randomUUID().toString().take(8).uppercase()}"
        val newChat = ChatEntity(
            chatId = uuid,
            chatType = "GROUP",
            title = title,
            avatarUrl = avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=200&q=80" },
            description = description,
            lastMessageText = "Group created securely.",
            lastMessageTime = System.currentTimeMillis()
        )
        chatDao.insertChat(newChat)
        return newChat
    }

    suspend fun createBroadcastChannel(title: String, description: String, avatarUrl: String): ChatEntity {
        val uuid = "CHN-${UUID.randomUUID().toString().take(8).uppercase()}"
        val newChat = ChatEntity(
            chatId = uuid,
            chatType = "CHANNEL",
            title = title,
            avatarUrl = avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1557200134-90327ee9fafa?auto=format&fit=crop&w=200&q=80" },
            description = description,
            lastMessageText = "Broadcast channel established. Only admins can post.",
            lastMessageTime = System.currentTimeMillis()
        )
        chatDao.insertChat(newChat)
        return newChat
    }

    suspend fun togglePinChat(chatId: String) {
        chatDao.getChatById(chatId)?.let {
            chatDao.insertChat(it.copy(isPinned = !it.isPinned))
        }
    }

    suspend fun toggleArchiveChat(chatId: String) {
        chatDao.getChatById(chatId)?.let {
            chatDao.insertChat(it.copy(isArchived = !it.isArchived))
        }
    }

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        text: String,
        mediaType: String = "NONE",
        mediaUrl: String = "",
        replyToId: String? = null,
        replyToText: String? = null,
        selfDestructSec: Long = 0L
    ) {
        val messageId = "MSG-${UUID.randomUUID()}"
        val newMessage = MessageEntity(
            messageId = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            timestamp = System.currentTimeMillis(),
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            replyToMessageId = replyToId,
            replyToText = replyToText,
            isSelfDestruct = selfDestructSec > 0L,
            selfDestructDuration = selfDestructSec,
            isE2EEncrypted = true
        )
        messageDao.insertMessage(newMessage)

        // Update last message in chat
        chatDao.getChatById(chatId)?.let {
            chatDao.insertChat(
                it.copy(
                    lastMessageText = when (mediaType) {
                        "IMAGE" -> "📷 Image"
                        "VIDEO" -> "📹 Video"
                        "VOICE" -> "🎤 Voice Note"
                        "STICKER" -> "🎨 Sticker"
                        "GIF" -> "🎬 GIF"
                        "DOCUMENT" -> "📄 Document"
                        else -> text
                    },
                    lastMessageTime = System.currentTimeMillis()
                )
            )
        }

        // Simulate typing & reply bot for interactive chats
        triggerBotReply(chatId, senderId, text, mediaType)
    }

    suspend fun editMessage(messageId: String, newText: String) {
        messageDao.getMessageById(messageId)?.let {
            val edited = it.copy(text = newText, isEdited = true)
            messageDao.insertMessage(edited)
        }
    }

    suspend fun addReaction(messageId: String, reactionEmoji: String, userId: String) {
        messageDao.getMessageById(messageId)?.let { msg ->
            // Format: "emoji,user;emoji2,user2"
            val current = msg.reactions
            val items = if (current.isEmpty()) mutableListOf() else current.split(";").toMutableList()
            
            // Check if user already reacted with this emoji, toggle it
            val pair = "$reactionEmoji,$userId"
            if (items.contains(pair)) {
                items.remove(pair)
            } else {
                items.add(pair)
            }
            val updated = msg.copy(reactions = items.joinToString(";"))
            messageDao.insertMessage(updated)
        }
    }

    suspend fun deleteMessageForMe(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }

    suspend fun deleteMessageForEveryone(messageId: String) {
        messageDao.deleteForEveryone(messageId)
    }

    suspend fun searchMessagesInChat(chatId: String, query: String): List<MessageEntity> {
        return messageDao.searchMessages(chatId, query)
    }

    // ==========================================
    // CALL LOGS
    // ==========================================

    fun getCallLogsFlow(): Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()

    suspend fun addCallLog(
        otherUserId: String,
        otherUserName: String,
        otherUserAvatar: String,
        callType: String,
        isOutgoing: Boolean,
        durationSeconds: Long,
        status: String
    ) {
        val log = CallLogEntity(
            callId = "CALL-${UUID.randomUUID()}",
            otherUserId = otherUserId,
            otherUserName = otherUserName,
            otherUserAvatar = otherUserAvatar,
            callType = callType,
            isOutgoing = isOutgoing,
            durationSeconds = durationSeconds,
            callStatus = status
        )
        callLogDao.insertCallLog(log)
    }

    suspend fun clearCallLogs() {
        callLogDao.clearAllCallLogs()
    }

    // ==========================================
    // BOT ENGINE (SIMULATED END-TO-END DECENTRALIZED REPLIES)
    // ==========================================

    private fun triggerBotReply(chatId: String, senderId: String, userText: String, mediaType: String) {
        repositoryScope.launch {
            // Check if private chat bot
            val contact = contactDao.getContactById(chatId) ?: return@launch
            if (contact.status != "FRIEND") return@launch

            delay(1500) // typing delay simulation

            val replyText = when (contact.username) {
                "alice_crypto" -> {
                    if (userText.lowercase().contains("key") || userText.lowercase().contains("secure")) {
                        "🔒 Verified! Our session keys are derived using Curve25519 DH. Verified identity fingerprint: A58E F90C 119C."
                    } else if (userText.lowercase().contains("hello") || userText.lowercase().contains("hi")) {
                        "Hey there! This is Alice. Ready to explore secure, metadata-free P2P calling and decentralized chats?"
                    } else if (mediaType != "NONE") {
                        "Got the media! It's decrypted on-the-fly and verified with AES-256 GCM."
                    } else {
                        "Super cool! Let's schedule a secure WebRTC voice call later. Type 'secure' to verify encryption."
                    }
                }
                "bob_signals" -> {
                    if (userText.lowercase().contains("call") || userText.lowercase().contains("video")) {
                        "Incoming call? I'm available! Let's trigger WebRTC adaptive bitrate testing. Tap the Call button at the top! 📞"
                    } else {
                        "Signal protocol double-ratchet in action. No centralized server logs what we say here."
                    }
                }
                "support_team" -> {
                    "Hi! Welcome to NovaChat support. Ask me about self-destructing timers, end-to-end Signal encryption, or cloud backups."
                }
                else -> {
                    "Acknowledged. High-definition WebRTC node linked successfully."
                }
            }

            val replyId = "MSG-BOT-${UUID.randomUUID()}"
            val botMessage = MessageEntity(
                messageId = replyId,
                chatId = chatId,
                senderId = contact.userId,
                senderName = contact.displayName,
                text = replyText,
                timestamp = System.currentTimeMillis(),
                isRead = false,
                isE2EEncrypted = true
            )
            messageDao.insertMessage(botMessage)

            // Update chat list
            chatDao.getChatById(chatId)?.let {
                chatDao.insertChat(
                    it.copy(
                        lastMessageText = replyText,
                        lastMessageTime = System.currentTimeMillis(),
                        unreadCount = it.unreadCount + 1
                    )
                )
            }
        }
    }

    // ==========================================
    // SEED INITIAL DATA
    // ==========================================

    private suspend fun seedInitialData(ownerId: String) {
        // 1. Initial Friends
        val alice = ContactEntity(
            userId = "NC-99812401",
            username = "alice_crypto",
            displayName = "Alice Crypto",
            bio = "Signal cryptographic protocol auditor. Web3 advocate.",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80",
            status = "FRIEND",
            isOnline = true
        )
        val bob = ContactEntity(
            userId = "NC-88204910",
            username = "bob_signals",
            displayName = "Bob Signals",
            bio = "WebRTC & low-bandwidth voice engine optimizer.",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
            status = "FRIEND",
            isOnline = false,
            lastSeen = System.currentTimeMillis() - 3600000
        )
        val support = ContactEntity(
            userId = "NC-10001000",
            username = "support_team",
            displayName = "Nova Security Support",
            bio = "Official support channel for zero-knowledge communications.",
            avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=200&q=80",
            status = "FRIEND",
            isOnline = true
        )

        contactDao.insertContact(alice)
        contactDao.insertContact(bob)
        contactDao.insertContact(support)

        // 2. Initial Chats
        val aliceChat = ChatEntity(
            chatId = alice.userId,
            chatType = "PRIVATE",
            title = alice.displayName,
            avatarUrl = alice.avatarUrl,
            description = alice.bio,
            lastMessageText = "Start end-to-end encrypted chat using Signal Protocol.",
            lastMessageTime = System.currentTimeMillis() - 600000,
            unreadCount = 1
        )
        val bobChat = ChatEntity(
            chatId = bob.userId,
            chatType = "PRIVATE",
            title = bob.displayName,
            avatarUrl = bob.avatarUrl,
            description = bob.bio,
            lastMessageText = "Signal protocol double-ratchet in action. No centralized server logs what we say here.",
            lastMessageTime = System.currentTimeMillis() - 1800000,
            unreadCount = 0
        )
        val officialChannel = ChatEntity(
            chatId = "CHN-ANN1",
            chatType = "CHANNEL",
            title = "Nova Broadcast Channel",
            avatarUrl = "https://images.unsplash.com/photo-1614741118887-7a4ee193a5fa?auto=format&fit=crop&w=200&q=80",
            description = "Official decentralized channel for project announcements.",
            lastMessageText = "Release 2.4.0 is live! Argon2 hashes configured.",
            lastMessageTime = System.currentTimeMillis() - 3600000,
            unreadCount = 0
        )

        chatDao.insertChat(aliceChat)
        chatDao.insertChat(bobChat)
        chatDao.insertChat(officialChannel)

        // 3. Messages Seeding
        messageDao.insertMessage(
            MessageEntity(
                messageId = "MSG-SEED1",
                chatId = alice.userId,
                senderId = alice.userId,
                senderName = alice.displayName,
                text = "Welcome to NovaChat! Tap here to verify my cryptographic key. Double-ratchet is armed.",
                timestamp = System.currentTimeMillis() - 600000,
                isE2EEncrypted = true
            )
        )
        messageDao.insertMessage(
            MessageEntity(
                messageId = "MSG-SEED2",
                chatId = bob.userId,
                senderId = bob.userId,
                senderName = bob.displayName,
                text = "Let's perform a direct end-to-end voice test! WebRTC works flawlessly on low bandwidth.",
                timestamp = System.currentTimeMillis() - 2000000,
                isE2EEncrypted = true
            )
        )
        messageDao.insertMessage(
            MessageEntity(
                messageId = "MSG-SEED3",
                chatId = bob.userId,
                senderId = bob.userId,
                senderName = bob.displayName,
                text = "Signal protocol double-ratchet in action. No centralized server logs what we say here.",
                timestamp = System.currentTimeMillis() - 1800000,
                isE2EEncrypted = true
            )
        )
        messageDao.insertMessage(
            MessageEntity(
                messageId = "MSG-SEED4",
                chatId = "CHN-ANN1",
                senderId = "CHN-ANN1",
                senderName = "System Admin",
                text = "Welcome to the official NovaChat announcement channel. Only admins can broadcast messages securely.",
                timestamp = System.currentTimeMillis() - 5000000,
                isE2EEncrypted = true
            )
        )
        messageDao.insertMessage(
            MessageEntity(
                messageId = "MSG-SEED5",
                chatId = "CHN-ANN1",
                senderId = "CHN-ANN1",
                senderName = "System Admin",
                text = "Release 2.4.0 is live! Argon2 hashes configured.",
                timestamp = System.currentTimeMillis() - 3600000,
                isE2EEncrypted = true
            )
        )

        // 4. Seeding Call Logs
        callLogDao.insertCallLog(
            CallLogEntity(
                callId = "CALL-SEED1",
                otherUserId = alice.userId,
                otherUserName = alice.displayName,
                otherUserAvatar = alice.avatarUrl,
                callType = "VIDEO",
                isOutgoing = false,
                timestamp = System.currentTimeMillis() - 86400000,
                durationSeconds = 142L,
                callStatus = "COMPLETED"
            )
        )
        callLogDao.insertCallLog(
            CallLogEntity(
                callId = "CALL-SEED2",
                otherUserId = bob.userId,
                otherUserName = bob.displayName,
                otherUserAvatar = bob.avatarUrl,
                callType = "VOICE",
                isOutgoing = true,
                timestamp = System.currentTimeMillis() - 172800000,
                durationSeconds = 0L,
                callStatus = "MISSED"
            )
        )
    }
}
