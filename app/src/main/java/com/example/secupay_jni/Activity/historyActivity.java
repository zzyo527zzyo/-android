package com.example.secupay_jni.Activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secupay_jni.Dao.UserDao;
import com.example.secupay_jni.DataBase.AppDatabase;
import com.example.secupay_jni.R;
import com.example.secupay_jni.model.User;
import com.example.secupay_jni.ui.HistoryAdapter;
import java.util.ArrayList;


public class historyActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private String username; // 存储接收到的用户名
    // 数据库相关
    private AppDatabase db;
    private UserDao userDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        // 接收传来的用户名
        username = getIntent().getStringExtra("USERNAME");
        // 初始化 adapter，传空列表
        adapter = new HistoryAdapter(new ArrayList<>());
        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 使用异步方式访问数据库
        new Thread(new Runnable() {
            @Override
            public void run() {
                 db = AppDatabase.getDb(historyActivity.this);
                 userDao = db.userDao();
                // 在后台线程执行数据库操作
                User user = userDao.getUserByUsername(username);
                if (user != null && user.getDescriptions() != null) {
                    for (String desc : user.getDescriptions()) {
                        adapter.addDescription(desc);
                    }
                }
            }
        }).start();
    }




}