package com.example.secupay_jni.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 交易模型：包含交易信息与评估后的风险结果。
 */
public class Transaction {

    private final String location;
    private final long timestampMs;
    private RiskLevel riskLevel;
    private String riskReason;
    // 深夜时间规则：检测交易时间是否在 0:00 - 5:00 之间，是则增加风险
    private boolean night;

    // 地理位置跳跃：检测短时间内地理位置是否发生大幅变化（如跨城市），是则增加风险
    private boolean locationJump;

    // Root环境
    private boolean rootDetected;

    // debug调试环境检测
    private boolean debuggable;

    // 连接调试器验证
    private boolean debuggerConnected;

    // 版本号验证
    private boolean invalidVersion;

    // 基于 SHA-256 的 APK 签名校验
    private boolean sha256Mismatch;

    // 基于 MD5 的 APK 签名校验
    private boolean md5Mismatch;

    // 基于 CRC 的签名校验
    private boolean crcCheckFailed;

    // 增强版CRC校验
    private boolean enhancedCrcCheckFailed;

    // 检测是否运行在模拟器中
    private boolean runningInEmulator;

    // 使用JDWP检测
    private boolean jdwpDetected;

    public boolean isNight() {
        return night;
    }

    public void setNight(boolean night) {
        this.night = night;
    }

    public boolean isLocationJump() {
        return locationJump;
    }

    public void setLocationJump(boolean locationJump) {
        this.locationJump = locationJump;
    }

    public boolean isRootDetected() {
        return rootDetected;
    }

    public void setRootDetected(boolean rootDetected) {
        this.rootDetected = rootDetected;
    }

    public boolean isDebuggable() {
        return debuggable;
    }

    public void setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
    }

    public boolean isDebuggerConnected() {
        return debuggerConnected;
    }

    public void setDebuggerConnected(boolean debuggerConnected) {
        this.debuggerConnected = debuggerConnected;
    }

    public boolean isInvalidVersion() {
        return invalidVersion;
    }

    public void setInvalidVersion(boolean invalidVersion) {
        this.invalidVersion = invalidVersion;
    }

    public boolean isSha256Mismatch() {
        return sha256Mismatch;
    }

    public void setSha256Mismatch(boolean sha256Mismatch) {
        this.sha256Mismatch = sha256Mismatch;
    }

    public boolean isMd5Mismatch() {
        return md5Mismatch;
    }

    public void setMd5Mismatch(boolean md5Mismatch) {
        this.md5Mismatch = md5Mismatch;
    }

    public boolean isCrcCheckFailed() {
        return crcCheckFailed;
    }

    public void setCrcCheckFailed(boolean crcCheckFailed) {
        this.crcCheckFailed = crcCheckFailed;
    }

    public boolean isEnhancedCrcCheckFailed() {
        return enhancedCrcCheckFailed;
    }

    public void setEnhancedCrcCheckFailed(boolean enhancedCrcCheckFailed) {
        this.enhancedCrcCheckFailed = enhancedCrcCheckFailed;
    }

    public boolean isRunningInEmulator() {
        return runningInEmulator;
    }

    public void setRunningInEmulator(boolean runningInEmulator) {
        this.runningInEmulator = runningInEmulator;
    }

    public boolean isJdwpDetected() {
        return jdwpDetected;
    }

    public void setJdwpDetected(boolean jdwpDetected) {
        this.jdwpDetected = jdwpDetected;
    }

    public Transaction(String location, long timestampMs) {
        this.location = location;
        this.timestampMs = timestampMs;
    }


    public String getLocation() {
        return location;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskReason() {
        return riskReason;
    }

    public void setRiskReason(String riskReason) {
        this.riskReason = riskReason;
    }

    /**
     * 将时间戳格式化为本地可读的日期时间字符串。
     */
    public String getFormattedTime() {
        Date date = new Date(timestampMs);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date);
    }
}


