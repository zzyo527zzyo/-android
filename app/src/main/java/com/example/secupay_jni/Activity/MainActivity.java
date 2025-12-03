package com.example.secupay_jni.Activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.secupay_jni.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {
    private String username; // 存储接收到的用户名
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 接收从 loadActivity 传来的用户名
        username = getIntent().getStringExtra("USERNAME");
        //底部菜单跳转功能
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        //底部菜单跳转
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
//                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                    intent.putExtra("USERNAME", username); // 带账号跳转
//                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_notifications) {
                    // 处理通知
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, mineActivity.class);
                    intent.putExtra("USERNAME", username); // 带账号跳转
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });
    }
    public void payment(View view) {
        Intent intent = new Intent(this, TransactionActivity.class);
        intent.putExtra("USERNAME", username); // 带账号跳转
        startActivity(intent);
    }
    public void history(View view) {
        Intent intent = new Intent(this, historyActivity.class);
        intent.putExtra("USERNAME", username); // 带账号跳转
        startActivity(intent);
    }
    public void shield_test(View view) {
        Intent intent = new Intent(this, shield_testActivity.class);
        startActivity(intent);
    }
    //搜索按钮点击事件
    public void search(View view) {
        //  获取控件
        TextInputLayout tilSearchLayout = findViewById(R.id.tilSearch);
        EditText etSearch = tilSearchLayout.getEditText();
        String query = etSearch.getText().toString().trim().toLowerCase();

        LinearLayout itemNewTransaction = findViewById(R.id.item_new_transaction);
        LinearLayout itemHistory = findViewById(R.id.item_history);
        LinearLayout itemSecurityCheck = findViewById(R.id.item_security_check);

        TextView transition = findViewById(R.id.transition);
        TextView history = findViewById(R.id.history);
        TextView security_check_tv = findViewById(R.id.security_check);

        // 如果搜索为空，显示所有项（带淡入动画）
        if (query.isEmpty()) {
            applyVisibilityWithAnimation(itemNewTransaction, View.VISIBLE);
            applyVisibilityWithAnimation(itemHistory, View.VISIBLE);
            applyVisibilityWithAnimation(itemSecurityCheck, View.VISIBLE);
            return;
        }

        // 否则根据匹配结果显示/隐藏（带动画）
        applyVisibilityWithAnimation(itemNewTransaction,
                transition.getText().toString().toLowerCase().contains(query) ? View.VISIBLE : View.GONE);

        applyVisibilityWithAnimation(itemHistory,
                history.getText().toString().toLowerCase().contains(query) ? View.VISIBLE : View.GONE);

        applyVisibilityWithAnimation(itemSecurityCheck,
                security_check_tv.getText().toString().toLowerCase().contains(query) ? View.VISIBLE : View.GONE);
    }
    //为 View 设置可见性，并添加淡入淡出动画
    private void applyVisibilityWithAnimation(View view, int visibility) {
        if (view.getVisibility() == visibility) {
            return; // 状态未变，无需动画
        }

        if (visibility == View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
            view.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        } else {
            view.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }
}

