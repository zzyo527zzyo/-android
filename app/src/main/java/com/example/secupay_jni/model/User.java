package com.example.secupay_jni.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;


    public String username;
    public String password;

    public double balance;
    private List<String> descriptions;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.balance =100.0;  // 默认余额为 0
    }
    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }
}