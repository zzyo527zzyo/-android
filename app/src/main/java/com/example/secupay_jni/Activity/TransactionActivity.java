package com.example.secupay_jni.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.secupay_jni.DataBase.AppDatabase;
import com.example.secupay_jni.Dao.UserDao;
import com.example.secupay_jni.model.User;
import com.example.secupay_jni.R;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionActivity extends AppCompatActivity {

    private TextView tvBalance;
    private TextInputEditText etPayee;
    private TextInputEditText etAmount;
    private Button btnSend;

    // 数据库相关
    private AppDatabase db;
    private UserDao userDao;
    private String currentUsername;

    // 线程池，用于异步操作数据库
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        // 初始化控件
        tvBalance = findViewById(R.id.tv_balance);
        etPayee = findViewById(R.id.et_payee);
        etAmount = findViewById(R.id.et_amount);
        btnSend = findViewById(R.id.btn_send);

        // 获取数据库实例
        db = AppDatabase.getDb(this);
        userDao = db.userDao();

        // 获取当前用户名（从登录页传递）
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 加载并显示余额
        loadAndDisplayBalance();

        // 发送按钮点击事件
        btnSend.setOnClickListener(v -> processTransaction());
    }


     //从数据库加载用户余额并显示
    private void loadAndDisplayBalance() {
        executor.execute(() -> {
            User user = userDao.getUserByUsername(currentUsername);
            runOnUiThread(() -> {
                if (user != null) {
                    tvBalance.setText("余额：¥ " + String.format("%.2f", user.balance));
                } else {
                    Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }


     //处理转账逻辑
    private void processTransaction() {
        String payee = etPayee.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        // 校验收款人
        if (payee.isEmpty()) {
            Toast.makeText(this, "请输入收款人", Toast.LENGTH_SHORT).show();
            return;
        }

        // 校验金额
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的金额", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "金额必须大于 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // 开始交易（异步）
        executor.execute(() -> {
            try {
                // 查询付款人
                User payer = userDao.getUserByUsername(currentUsername);
                if (payer == null) {
                    runOnUiThread(() -> Toast.makeText(this, "付款人不存在", Toast.LENGTH_SHORT).show());
                    return;
                }

                //  查询收款人
                User payeeUser = userDao.getUserByUsername(payee);
                if (payeeUser == null) {
                    runOnUiThread(() -> Toast.makeText(this, "收款人不存在", Toast.LENGTH_SHORT).show());
                    return;
                }

                // 检查余额是否足够
                if (payer.balance < amount) {
                    runOnUiThread(() -> Toast.makeText(this, "余额不足！", Toast.LENGTH_SHORT).show());
                    return;
                }

                // 执行转账
                payer.balance -= amount;
                payeeUser.balance += amount;

                // 获取现有描述列表，如果为 null 则新建
                List<String> payerDescs = payer.getDescriptions();
                if (payerDescs == null) {
                    payerDescs = new ArrayList<>();
                }


                // 添加新记录
                String newDesc = payer.username + " 向 " + payeeUser.username + " 转账 ¥" + amount + " 成功！" +
                        "\n时间：" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) +
                        "\n地点：重庆";

                payerDescs.add(newDesc);
                payer.setDescriptions(payerDescs);

                // 更新双方到数据库
                userDao.updateUser(payer);
                userDao.updateUser(payeeUser);

                // 回到主线程更新 UI
                runOnUiThread(() -> {
                    tvBalance.setText("余额：¥ " + String.format("%.2f", payer.balance));
                    Toast.makeText(this,
                            "向 \"" + payee + "\" 转账 ¥" + String.format("%.2f", amount) + " 成功！",
                            Toast.LENGTH_LONG).show();

                    // 清空输入框
                    etPayee.getText().clear();
                    etAmount.getText().clear();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "交易失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭线程池
        executor.shutdown();
    }
}