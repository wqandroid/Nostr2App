package nostr.postr.db

import android.content.Context
import androidx.room.*
import io.reactivex.rxjava3.core.Flowable


@Database(
    entities = [FeedItem::class, UserProfile::class, BlockUser::class,
        ChatMessage::class,ChatRoom::class,
        FollowUserKey::class],
    version = 1
)
abstract class NostrDB : RoomDatabase() {


    abstract fun feedDao(): FeedDao
    abstract fun chatDao(): ChatDao
    abstract fun profileDao(): UserProfileDao

    abstract fun blockUserDao(): BlockUserDao

    abstract fun followUserKeyDao(): FollowUserKeyDao

    //
    companion object {
        private var appDatabase: NostrDB? = null
        fun getDatabase(context: Context): NostrDB {
            if (appDatabase == null) {
                synchronized(this) {
                    appDatabase = Room.databaseBuilder(context, NostrDB::class.java, "nor18.db")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                }
            }
            return appDatabase!!
        }
    }

}

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed_info ORDER BY created_at DESC LIMIT 200")
    suspend fun getAll(): List<FeedItem>

    @Query("SELECT * FROM feed_info ORDER BY created_at DESC LIMIT 1")
    fun getLast(): FeedItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: FeedItem)

    @Query("SELECT COUNT(*)  FROM feed_info ")
    suspend fun getCount(): Int

    @Query("SELECT * FROM feed_info where pubkey =:k")
    suspend fun getFeedByPublicKey(k: String): List<FeedItem>

    @Delete
    suspend fun delete(feed: FeedItem): Int

}


@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile")
    suspend fun getAll(): List<UserProfile>

    @Query("select * from user_profile where pubkey =:s")
    suspend fun getUserInfo(s: String): UserProfile?

    @Query("select * from user_profile where pubkey =:s")
    fun getUserInfo2(s: String): UserProfile?

    @Query("select * from user_profile where pubkey =:s")
    fun getUserInfoRx(s: String): Flowable<UserProfile?>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)


}

@Dao
interface FollowUserKeyDao {

    @Query("SELECT * FROM follow_user")
    fun getAll(): Flowable<List<FollowUserKey>>

    @Query("select * from follow_user where pubkey =:s")
    suspend fun getFollowUser(s: String): FollowUserKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(user: FollowUserKey)

    @Delete
    suspend fun delete(feed: FollowUserKey): Int
}

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createChatRoom(chat: ChatRoom)

    @Query("SELECT * FROM chat ORDER BY createAt DESC LIMIT 1")
    fun getLast(): ChatMessage?
    @Query("select * from chat_room")
    fun getAllChatRoom(): Flowable<List<ChatRoom>>

    @Query("select * from chat_room")
    suspend fun getTotalChatRoom(): List<ChatRoom>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMsg(chat: ChatMessage)

    @Query("select * from chat where roomId=:s")
     fun getChatGroupMessage(s: String): Flowable<List<ChatMessage>>

}


@Dao
interface BlockUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockUser(user: BlockUser)

    @Query("SELECT * FROM block_user2")
    suspend fun getAllBlock(): List<BlockUser>

    @Query("select * from block_user2 where pubkey =:s or contentMD5=:m")
    suspend fun getBlockUserInfo(s: String, m: String): BlockUser?
}







