package com.example.secupay_jni.Activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.secupay_jni.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class mineActivity extends AppCompatActivity {
    private String username; // 存储接收到的用户名
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mine);
        // 接收从 loadActivity 传来的用户名
        username = getIntent().getStringExtra("USERNAME");
        // 找到 TextView 并设置用户名
        TextView nameTextView = findViewById(R.id.name);
        if (username==null){
            nameTextView.setText("未登录");
        }else {
            nameTextView.setText(username);
        }
        //底部菜单跳转功能
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        //底部菜单跳转
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent intent = new Intent(mineActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", username); // 带账号跳转
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_notifications) {
                    // 处理通知
                    return true;
                } else if (id == R.id.nav_profile) {
                    // 处理我的
                    return true;
                }

                return false;
            }
        });
    }
    public void load(View view) {
        Intent intent = new Intent(this, loadActivity.class);
        startActivity(intent);
    }
}

