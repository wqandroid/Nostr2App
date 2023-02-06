package nostr.postr.db

import android.content.Context
import androidx.room.*


@Database(entities = [FeedItem::class, UserProfile::class], version = 1)
abstract class NostrDB : RoomDatabase() {


    abstract fun feedDao(): FeedDao
    abstract fun profileDao(): UserProfileDao

    //


    //
    companion object{
        private  var appDatabase: NostrDB?=null
        open fun getDatabase(context: Context): NostrDB {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(context, NostrDB::class.java, "nor2.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }
            return appDatabase!!
        }
    }



}

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed_info")
    suspend fun getAll(): List<FeedItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeed(feed: FeedItem)

}


@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile")
    suspend fun getAll(): List<UserProfile>

    @Query("select * from user_profile where pubkey =:s")
    suspend fun getUserInfo(s: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

}









