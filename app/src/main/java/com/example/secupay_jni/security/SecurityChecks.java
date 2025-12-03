package com.example.secupay_jni.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;
import android.util.Log;


import com.example.secupay_jni.Utils.Base64Utils;
import com.example.secupay_jni.Utils.MD5Utils;

import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class SecurityChecks {
    private static final String EXPECTED_APP_VERSION = "1.2.0"; // 预期的应用版本号
    // 声明 native 方法
    public static native int DetectJDWP();
    static {
        System.loadLibrary("secupay_jni");
    }

//   若设备开启ADB调试则返回true。
    public static boolean isDeviceDebuggable(Context context) {
        try {
            int adb = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
            return adb == 1;
        } catch (Exception ignored) {
            return false;
        }
    }

//     检查应用是否设置为可调试状态（android:debuggable="true"）
    public static boolean isAppDebuggable(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);

            // 检查应用标志是否包含DEBUGGABLE标志
            boolean isDebuggable = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

            Log.d("SecurityChecks", "应用可调试状态: " + isDebuggable);
            return isDebuggable;

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SecurityChecks", "获取应用调试信息失败", e);
            return false;
        }
    }
    //检查是否连接调试器
    public static boolean checkForDebugger() {
        if (Debug.isDebuggerConnected()) {
            return false;
        }
        return true;
    }
//  若存在常见su文件或Build.TAGS包含test-keys则认为已Root。
    public static boolean isDeviceRooted() {
        String[] paths = new String[] {
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        String tags = Build.TAGS;
        return tags != null && tags.contains("test-keys");
    }
    //基于 SHA-256 的 APK 签名校验
    public static boolean SHA256_Valid(Context context) {
        try {
            // 获取当前应用的签名信息
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures = packageInfo.signatures;

            // 计算当前签名的哈希值（这里使用SHA-256）
            //获取SHA-256哈希算法实例
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(signatures[0].toByteArray());
            byte[] digest = md.digest();
            String currentSignature = bytesToHex(digest);
            System.out.println("签名哈希: " + currentSignature); // 同时输出到System.out
            // 这里替换为你预期的正确签名哈希值
            String expectedSignature = "f8cf791cd2ef25dfdd3eb7197b5cd87e3e7da0351687cfa8008e868196f1978c";
            return currentSignature.equalsIgnoreCase(expectedSignature);

        } catch (Exception e) {
            return false;
        }
    }
    // 字节数组转十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    // 版本号验证方法
    public static boolean isAppVersionValid(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            String currentVersion = packageInfo.versionName;
            Log.d("RiskEngine", "当前应用版本: " + currentVersion + ", 预期版本: " + EXPECTED_APP_VERSION);
            return currentVersion.equals(EXPECTED_APP_VERSION);

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("RiskEngine", "获取应用版本信息失败", e);
            return false;
        }
    }
    //基于 MD5 的 APK 签名校验
    public static boolean MD5Check(Context context) {
        String str;
        Signature[] signatureArr;
        try {
            // Android 9.0 (API 28) 及以上使用新的签名API
            if (Build.VERSION.SDK_INT >= 28) {
                // 134217728 = PackageManager.GET_SIGNING_CERTIFICATES
                // 新API提供更完整的签名验证，包括APK内容签名者
                signatureArr = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 134217728).signingInfo.getApkContentsSigners();
            } else {
                // 旧版本使用传统方式获取签名，64 = PackageManager.GET_SIGNATURES
                signatureArr = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 64).signatures;
            }

            // 计算签名的MD5值
            // 步骤：签名字节数组 -> Base64编码 -> MD5哈希
            String base64Signature = Base64Utils.encodeToString(signatureArr[0].toByteArray());
            str = MD5Utils.MD5(base64Signature);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            str = "";
        }
        Log.e("RiskEngine", "签名MD5: " + str);

        // 比较计算出的MD5值与预置的正确值是否相等
        return "66cfd88a88c9e22bb45c89e651500c2b".equals(str);
    }
    //基于 CRC 的签名校验
    public static boolean check_crc(Context context) {
        try {
            String apkPath = context.getPackageCodePath();
            ZipFile zipFile = new ZipFile(apkPath);
            ZipEntry entry = zipFile.getEntry("classes.dex");
            if (entry != null) {
                long crc = entry.getCrc();
                Log.e("RiskEngine", "dexCrc:" + crc);
                return crc == 5437337;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //增强版CRC校验 - 计算文件内容的CRC32（更可靠）
    public static boolean verifyFileCrc(Context context, String zipEntryName, long expectedCrc) {
        CRC32 crc = new CRC32();
        try (ZipFile apkZip = new ZipFile(new File(context.getPackageCodePath()))) {
            ZipEntry entry = apkZip.getEntry(zipEntryName);
            if (entry == null) return false;

            try (InputStream input = apkZip.getInputStream(entry)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    crc.update(buffer, 0, bytesRead);
                }
                long actualCrc = crc.getValue();
                Log.d("RiskEngine", "文件CRC校验: " + zipEntryName + ", FileCrc实际=" + actualCrc + ", FileCrc预期=" + expectedCrc);
                return actualCrc == expectedCrc;
            }
        } catch (Exception e) {
            Log.e("RiskEngine", "计算文件CRC异常: " + e.getMessage());
            return false;
        }
    }

     //检测是否运行在模拟器中
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.HOST.startsWith("Build")
                || Build.PRODUCT.equals("google_sdk");
    }
}


