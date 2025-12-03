package com.example.secupay_jni.DataBase;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.secupay_jni.Dao.UserDao;
import com.example.secupay_jni.model.User;

@Database(
        entities = {User.class},
        version = 2,
        exportSchema = false
)
@TypeConverters({UserDao.class}) // 注册转换器
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    private static AppDatabase INSTANCE;

    public static synchronized AppDatabase getDb(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "user_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}