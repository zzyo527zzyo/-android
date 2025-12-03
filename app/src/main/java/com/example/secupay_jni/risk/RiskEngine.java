package com.example.secupay_jni.risk;
import android.content.Context;

import com.example.secupay_jni.model.RiskLevel;
import com.example.secupay_jni.model.Transaction;
import com.example.secupay_jni.security.SecurityChecks;


public class RiskEngine {


    private String lastLocation = null;
    private static final long EXPECTED_FileCrc = 5414; // 预期的FileCrc


    //评估风险等级与原因。
    public void evaluate(Context context, Transaction tx) {
        RiskLevel level = RiskLevel.LOW;
        StringBuilder reasons = new StringBuilder();


        // 深夜时间规则
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(tx.getTimestampMs());
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour <= 6) {
            level = elevate(level, RiskLevel.LOW);
            appendReason(reasons, "深夜交易");
            tx.setNight(true);
        }

        // 地理位置跳跃（示例：字符串不同视为跳跃）
        if (lastLocation != null && !lastLocation.equalsIgnoreCase(tx.getLocation())) {
            level = elevate(level, RiskLevel.LOW);
            appendReason(reasons, "地理位置跳跃");
            tx.setLocationJump(true);
        }
        lastLocation = tx.getLocation();

        //Root环境
        boolean rooted = SecurityChecks.isDeviceRooted();
        if (rooted) {
            level = elevate(level, RiskLevel.MEDIUM);
            appendReason(reasons, "Root环境");
            tx.setRootDetected(true);
//            System.exit(0);
        }
        //debug调试环境检测
        boolean debug = SecurityChecks.isDeviceDebuggable(context);
        boolean appDebuggable = SecurityChecks.isAppDebuggable(context);

        if (debug || appDebuggable) {
            level = elevate(level, RiskLevel.HIGH);
            tx.setDebuggable(true);
            if (debug && appDebuggable) {
                appendReason(reasons, "设备调试模式 · 应用可调试状态");
            } else if (debug) {
                appendReason(reasons, "设备调试模式");
            } else {
                appendReason(reasons, "应用可调试状态");
            }
        }
        // 连接调试器验证
        boolean DebuggerValid = SecurityChecks.checkForDebugger();
        if (!DebuggerValid) {
            level = elevate(level, RiskLevel.HIGH);
            appendReason(reasons, "调试器正在连接中");
            tx.setDebuggerConnected(true);
//            System.exit(0);
        }
        // 版本号验证
        boolean versionValid = SecurityChecks.isAppVersionValid(context);
        if (!versionValid) {
            level = elevate(level, RiskLevel.LOW);
            appendReason(reasons, "版本验证失败");
            tx.setInvalidVersion(true);
//            System.exit(0);
        }

        //基于 SHA-256 的 APK 签名校验
        boolean signatureValid = SecurityChecks.SHA256_Valid(context);
        if (!signatureValid) {
            level = elevate(level, RiskLevel.MEDIUM);
            appendReason(reasons, "SHA-256签名验证失败");
//            System.exit(0);
            tx.setSha256Mismatch(true);
        }
        //基于 MD5 的 APK 签名校验
        boolean MD5Valid = SecurityChecks.MD5Check(context);
        if (!MD5Valid) {
            level = elevate(level, RiskLevel.MEDIUM);
            appendReason(reasons, "MD5签名验证失败");
            tx.setMd5Mismatch(true);
//            System.exit(0);
        }
        //基于 CRC 的签名校验
        boolean CRCValid = SecurityChecks.check_crc(context);
        if (!CRCValid) {
            level = elevate(level, RiskLevel.MEDIUM);
            appendReason(reasons, "CRC签名验证失败");
            tx.setCrcCheckFailed(true);
//            System.exit(0);
        }
        //增强版CRC校验
        boolean CRCFileValid = SecurityChecks.verifyFileCrc(context, "classes.dex", EXPECTED_FileCrc);
        if (!CRCFileValid) {
            level = elevate(level, RiskLevel.MEDIUM);
            appendReason(reasons, "增强版CRC签名验证失败");
            tx.setEnhancedCrcCheckFailed(true);
//            System.exit(0);
        }
        //检测是否运行在模拟器中
        boolean emulatorValid = SecurityChecks.isEmulator();
        if (!emulatorValid) {
            level = elevate(level, RiskLevel.MEDIUM);
            appendReason(reasons, "模拟器运行中");
            tx.setRunningInEmulator(true);
//            System.exit(0);
        }
        //使用JDWP检测
        int result = SecurityChecks.DetectJDWP();
        if (result != 0) {
            level = elevate(level, RiskLevel.HIGH);
            appendReason(reasons, "native层检测到调试行为");
            tx.setJdwpDetected(true);
        }
        tx.setRiskLevel(level);
        tx.setRiskReason(reasons.toString());
    }

    //将风险原因以“·”拼接的紧凑形式追加。
    private static void appendReason(StringBuilder reasons, String reason) {
        if (reasons.length() > 0) reasons.append(" · ");
        reasons.append(reason);
    }


     //取较高的风险等级作为结果。
    private static RiskLevel elevate(RiskLevel current, RiskLevel incoming) {
        if (current == RiskLevel.HIGH || incoming == RiskLevel.HIGH) return RiskLevel.HIGH;
        if (current == RiskLevel.MEDIUM || incoming == RiskLevel.MEDIUM) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }





}


