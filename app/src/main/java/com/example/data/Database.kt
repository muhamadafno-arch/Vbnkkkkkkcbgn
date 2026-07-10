package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. ROOM ENTITIES
// ==========================================

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String, // e.g. NC-34827195 or @nova_x82
    val username: String,
    val passwordHash: String,
    val displayName: String,
    val bio: String,
    val avatarUrl: String,
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val isE2EEnabled: Boolean = true
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarUrl: String,
    val status: String, // FRIEND, PENDING_OUTGOING, PENDING_INCOMING, BLOCKED
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val chatId: String, // contact userId or group/channel uuid
    val chatType: String, // PRIVATE, GROUP, CHANNEL
    val title: String,
    val avatarUrl: String,
    val description: String,
    val pinnedMessageId: String? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessageText: String = "",
    val lastMessageTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaType: String = "NONE", // NONE, IMAGE, VIDEO, DOCUMENT, VOICE, STICKER, GIF
    val mediaUrl: String = "",
    val isRead: Boolean = false,
    val isSelfDestruct: Boolean = false,
    val selfDestructDuration: Long = 0L, // in seconds (e.g., 5, 10, 30)
    val reactions: String = "", // comma-separated list like "👍,@nova_user1;❤️,@nova_user2"
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val isE2EEncrypted: Boolean = true // Signal Protocol E2E Encrypted
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val callId: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserAvatar: String,
    val callType: String, // VOICE, VIDEO, GROUP_VOICE, GROUP_VIDEO
    val isOutgoing: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0L, // 0 for missed/canceled
    val callStatus: String // COMPLETED, MISSED, REJECTED, BUSY
)

// ==========================================
// 2. ROOM DAOS
// ==========================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE status = 'FRIEND' ORDER BY displayName ASC")
    fun getFriends(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE status IN ('PENDING_INCOMING', 'PENDING_OUTGOING') ORDER BY displayName ASC")
    fun getPendingRequests(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE status = 'BLOCKED' ORDER BY displayName ASC")
    fun getBlockedUsers(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE userId = :userId LIMIT 1")
    suspend fun getContactById(userId: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE username = :username LIMIT 1")
    suspend fun getContactByUsername(username: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE userId = :userId")
    suspend fun deleteContactById(userId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE isArchived = 1 ORDER BY lastMessageTime DESC")
    fun getArchivedChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE isArchived = 0 ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getActiveChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId LIMIT 1")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("UPDATE chats SET unreadCount = 0 WHERE chatId = :chatId")
    suspend fun resetUnreadCount(chatId: String)

    @Delete
    suspend fun deleteChat(chat: ChatEntity)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE messageId = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("UPDATE messages SET isDeleted = 1, text = 'Message deleted' WHERE messageId = :messageId")
    suspend fun deleteForEveryone(messageId: String)

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND text LIKE '%' || :query || '%' AND isDeleted = 0 ORDER BY timestamp DESC")
    suspend fun searchMessages(chatId: String, query: String): List<MessageEntity>
}

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity)

    @Query("DELETE FROM call_logs")
    suspend fun clearAllCallLogs()
}

// ==========================================
// 3. APP DATABASE
// ==========================================

@Database(
    entities = [
        UserEntity::class,
        ContactEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        CallLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun callLogDao(): CallLogDao
}
