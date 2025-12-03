package com.example.secupay_jni.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secupay_jni.Dao.UserDao;
import com.example.secupay_jni.DataBase.AppDatabase;
import com.example.secupay_jni.R;
import com.example.secupay_jni.model.User;


public class loadActivity extends AppCompatActivity {

    private EditText editTextAccount, editTextPassword;
    private Button btnNext;
    private TextView tvRegister, tvFind;

    private AppDatabase db;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load); // 注意：这是登录页 layout

        // 绑定控件
        editTextAccount = findViewById(R.id.editTextAccount);
        editTextPassword = findViewById(R.id.editTextpassword);
        btnNext = findViewById(R.id.btnNext);
        tvRegister = findViewById(R.id.tvRegister);
        tvFind = findViewById(R.id.tvFind);

        // 初始化数据库
        db = AppDatabase.getDb(this);
        userDao = db.userDao();



        // 跳转到注册页
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(loadActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 找回账号
        tvFind.setOnClickListener(v -> {
            Toast.makeText(this, "正在跳转至找回密码页面...", Toast.LENGTH_SHORT).show();
            // 可跳转或弹出对话框
        });

        // 登录逻辑
        btnNext.setOnClickListener(v -> {
            String username = editTextAccount.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入完整的账号和密码", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    User user = userDao.login(username, password);
                    runOnUiThread(() -> {
                        if (user != null) {
                            Toast.makeText(loadActivity.this, "登录成功！欢迎回来", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(loadActivity.this, MainActivity.class);
                            intent.putExtra("USERNAME", username); // 可带账号跳转
                            startActivity(intent);
                        } else {
                            Toast.makeText(loadActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(loadActivity.this, "登录异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
    }
}