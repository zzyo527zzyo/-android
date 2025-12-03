package com.example.secupay_jni.Dao;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.Update;

import com.example.secupay_jni.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

@Dao
public interface UserDao {
    static Gson gson = new Gson();
    @Insert
    long insert(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);
    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);

    // List<String> → String（存入数据库）
    @TypeConverter
    public static String fromStringList(List<String> list) {
        return gson.toJson(list);
    }

    // String → List<String>（从数据库读取）
    @TypeConverter
    public static List<String> toStringList(String json) {
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }
    @Update
    void updateUser(User user);
}