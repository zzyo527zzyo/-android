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


public class RegisterActivity extends AppCompatActivity {

    private EditText editTextAccount, editTextPassword, editTextPasswordNext;
    private Button btnNext;
    private TextView tvRegister, tvFind;

    private AppDatabase db;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 绑定控件
        editTextAccount = findViewById(R.id.editTextAccount);
        editTextPassword = findViewById(R.id.editTextpassword);
        editTextPasswordNext = findViewById(R.id.editTextpassword_next);
        btnNext = findViewById(R.id.btnNext);
        tvRegister = findViewById(R.id.tvRegister);
        tvFind = findViewById(R.id.tvFind);

        // 初始化数据库
        db = AppDatabase.getDb(this);
        userDao = db.userDao();



        // 跳转到登录页
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, loadActivity.class);
            startActivity(intent);
        });

        // 找回账号（注册页一般不常用，可提示）
        tvFind.setOnClickListener(v -> {
            Toast.makeText(this, "正在跳转至找回密码页面...", Toast.LENGTH_SHORT).show();
        });

        // 注册逻辑
        btnNext.setOnClickListener(v -> {
            String username = editTextAccount.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String passwordConfirm = editTextPasswordNext.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(passwordConfirm)) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show();
                return;
            }

            // 检查是否已注册
            new Thread(() -> {
                try {
                    User exist = userDao.findByUsername(username);
                    if (exist != null) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "该账号已存在", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    User newUser = new User(username, password);
                    long result = userDao.insert(newUser);
                    runOnUiThread(() -> {
                        if (result > 0) {
                            Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                            // 自动跳转到登录页
                            Intent intent = new Intent(RegisterActivity.this, loadActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "注册出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
    }
}