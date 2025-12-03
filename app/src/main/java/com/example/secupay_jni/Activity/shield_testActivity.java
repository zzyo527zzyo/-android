package com.example.secupay_jni.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secupay_jni.R;
import com.example.secupay_jni.model.Transaction;
import com.example.secupay_jni.risk.RiskEngine;
import com.example.secupay_jni.ui.Adapter;
import com.example.secupay_jni.ui.RingChartView;
import com.google.android.material.bottomnavigation.BottomNavigationView;




public class shield_testActivity extends AppCompatActivity {

    // 交易列表适配器（根据风险等级着色）
    private Adapter adapter;
    // 风控规则引擎：实时评估交易风险
    private RiskEngine riskEngine;
    // 定位
    private static final int REQ_LOCATION = 101;
    private LocationManager locationManager;
    private Location lastFix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shield_test);
        // 绑定界面控件
        Button btnGenerate = findViewById(R.id.btnStartShield);
        RecyclerView rv = findViewById(R.id.rvTransactions);

        // 初始化列表与适配器
        adapter = new Adapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 初始化风险引擎（除记忆上一次地点外无状态）
        riskEngine = new RiskEngine();

        // 定位服务
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 运行时权限检查与请求
        ensureLocationPermission();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        //底部菜单跳转页面
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent intent = new Intent(shield_testActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_notifications) {
                    // 处理通知
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(shield_testActivity.this, mineActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 先检查设备定位服务是否开启
                if (!isLocationServiceEnabled()) {
                    showLocationServiceDialog(); // 弹窗引导用户开启
                    return; // 直接返回，不再执行后面的逻辑
                }

                // 如果服务已开启，再请求位置更新
                requestOneLocationUpdate(new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        lastFix = location;
                        handleGenerate();

                        try {
                            locationManager.removeUpdates(this);
                        } catch (Exception ignored) {
                        }
                    }
                });
            }
        });//每一次点击就生成一个位置更新的监听器，更新位置。

    }


    //执行风险评估，并插入列表展示。
    private void handleGenerate() {

        String locationLabel = resolveLocationLabel();
        // 使用当前时间戳构建交易对象
        Transaction tx = new Transaction(locationLabel, System.currentTimeMillis());
        riskEngine.evaluate(this, tx);
        adapter.addTransaction(tx);

        // 通过 ID 找到 TextView
        TextView tvRiskScore = findViewById(R.id.tv_risk_score);
        // 计算风险评分
        int riskScore = calculateRiskScore(tx);
        // 设置文本
        tvRiskScore.setText("风险评分：" + riskScore);

        // 左侧图：显示检测到的风险项
        RingChartView leftRingChart = findViewById(R.id.left_ringChart);
        leftRingChart.setShowDetectedRisks(true); // 显示风险项
        leftRingChart.setData(tx);

        // 右侧图：显示未检测的安全项（如果需要的话）
         RingChartView rightRingChart = findViewById(R.id.right_ringChart);
         rightRingChart.setShowDetectedRisks(false); // 显示安全项
         rightRingChart.setData(tx);

    }

    // 运行时定位权限检查与请求
    private void ensureLocationPermission() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fine) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION);
        }
    }


    // 解析位置标签（城市, 国家），失败返回“未知位置”或经纬度
    public String resolveLocationLabel() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fine) {
            return "没有权限";
        }
        try {
            Location loc = lastFix;
            if (loc == null && locationManager != null) {
                try { loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); } catch (SecurityException ignored) {}
                if (loc == null) {
                    try { loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); } catch (SecurityException ignored) {}
                }
            }
            if (loc == null) return "未知位置";

            try {
                if (Geocoder.isPresent()) {
                    Geocoder geocoder = new Geocoder(this, java.util.Locale.getDefault());
                    java.util.List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                    if (list != null && !list.isEmpty()) {
                        Address a = list.get(0);
                        String city = a.getLocality();
                        String district = a.getSubAdminArea();
                        String province = a.getAdminArea();
                        String country = a.getCountryName();
                        String main = !TextUtils.isEmpty(city) ? city : (!TextUtils.isEmpty(district) ? district : province);
                        if (!TextUtils.isEmpty(main)) {
                            return !TextUtils.isEmpty(country) ? (main + ", " + country) : main;
                        }
                        String feature = a.getFeatureName();
                        String thoroughfare = a.getThoroughfare();
                        if (!TextUtils.isEmpty(feature)) return feature;
                        if (!TextUtils.isEmpty(thoroughfare)) return thoroughfare;
                    }
                }
            } catch (Exception ignored) {}

            return String.format(java.util.Locale.getDefault(), "%.4f,%.4f", loc.getLatitude(), loc.getLongitude());
        } catch (Exception e) {
            return "未知位置";
        }
    }

    // 主动请求一次位置更新
    private void requestOneLocationUpdate(LocationListener listener) {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if ((!fine && !coarse) || locationManager == null) return;
        try { locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, listener, Looper.getMainLooper()); } catch (Exception ignored) {}
        try { locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener, Looper.getMainLooper()); } catch (Exception ignored) {}
    }
    // 检查设备的定位服务（GPS/网络定位）是否开启
    private boolean isLocationServiceEnabled() {
        try {
            // 检查GPS定位是否开启
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // 检查网络定位是否开启
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            // 只要有一个定位源可用，就返回true
            return isGpsEnabled || isNetworkEnabled;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 显示对话框，引导用户去系统设置中开启定位服务
    private void showLocationServiceDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("需要开启定位服务")
                .setMessage("检测到您的手机定位服务未开启，这将无法获取位置信息。请前往系统设置中开启定位服务。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    // 跳转到系统的位置服务设置界面
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }
    //评分算法
    private int calculateRiskScore(Transaction transaction) {
        if (transaction == null) {
            return 0;
        }

        int riskScore = 0;

        // 各项风险权重分配（可以根据实际需求调整）
        final int HIGH_RISK_WEIGHT = 20;    // 高风险项
        final int MEDIUM_RISK_WEIGHT = 15;  // 中风险项
        final int LOW_RISK_WEIGHT = 10;     // 低风险项

        // 高风险项（严重安全问题）
        if (transaction.isDebuggerConnected()) {
            riskScore += HIGH_RISK_WEIGHT;
        }
        if (transaction.isDebuggable()) {
            riskScore += HIGH_RISK_WEIGHT;
        }
        if (transaction.isJdwpDetected()) {
            riskScore += HIGH_RISK_WEIGHT;
        }


        // 中风险项（潜在安全问题）
        if (transaction.isRootDetected()) {
            riskScore += MEDIUM_RISK_WEIGHT;
        }
        if (transaction.isRunningInEmulator()) {
            riskScore += MEDIUM_RISK_WEIGHT;
        }
        if (transaction.isJdwpDetected()) {
            riskScore += MEDIUM_RISK_WEIGHT;
        }

        // 签名校验相关（中风险）
        boolean signatureIssues = transaction.isSha256Mismatch() ||
                transaction.isMd5Mismatch() ||
                transaction.isCrcCheckFailed() ||
                transaction.isEnhancedCrcCheckFailed();
        if (signatureIssues) {
            riskScore += MEDIUM_RISK_WEIGHT;
        }

        // 低风险项（环境风险）
        if (transaction.isNight()) {
            riskScore += LOW_RISK_WEIGHT;
        }
        if (transaction.isLocationJump()) {
            riskScore += LOW_RISK_WEIGHT;
        }
        if (transaction.isInvalidVersion()) {
            riskScore += LOW_RISK_WEIGHT;
        }

        // 确保分数在 0-100 范围内
        return Math.min(riskScore, 100);
    }
}