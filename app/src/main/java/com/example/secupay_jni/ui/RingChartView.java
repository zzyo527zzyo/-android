// RingChartView.java
package com.example.secupay_jni.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.secupay_jni.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class RingChartView extends View {

    private Paint paint;
    private Paint textPaint;
    private Paint legendPaint;
    private Transaction transaction;
    private List<ChartSegment> segments = new ArrayList<>();
    private boolean showDetectedRisks = true;

    // 美观的颜色方案 - 风险项颜色
    private static final int[] RISK_COLORS = {
            Color.parseColor("#FF6B6B"),   // night - 珊瑚红
            Color.parseColor("#4ECDC4"),   // locationJump - 青蓝色
            Color.parseColor("#45B7D1"),   // rootDetected - 天蓝色
            Color.parseColor("#96CEB4"),   // debuggable - 薄荷绿
            Color.parseColor("#FFEAA7"),   // debuggerConnected - 浅黄色
            Color.parseColor("#DDA0DD"),   // invalidVersion - 梅红色
            Color.parseColor("#98D8C8"),   // 签名校验 - 海绿色
            Color.parseColor("#F7DC6F"),   // runningInEmulator - 金黄色
            Color.parseColor("#BB8FCE")    // jdwpDetected - 淡紫色
    };

    // 安全项颜色 - 更柔和的颜色
    private static final int[] SAFE_COLORS = {
            Color.parseColor("#B8E0D2"),   // night - 淡绿色
            Color.parseColor("#95B8D1"),   // locationJump - 淡蓝色
            Color.parseColor("#C8E6C9"),   // rootDetected - 浅绿色
            Color.parseColor("#FFF9C4"),   // debuggable - 淡黄色
            Color.parseColor("#E1BEE7"),   // debuggerConnected - 淡紫色
            Color.parseColor("#FFCDD2"),   // invalidVersion - 淡粉色
            Color.parseColor("#B2DFDB"),   // 签名校验 - 淡青色
            Color.parseColor("#F0F4C3"),   // runningInEmulator - 淡黄绿色
            Color.parseColor("#D7CCC8")    // jdwpDetected - 淡棕色
    };

    public RingChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.LEFT);

        legendPaint = new Paint();
        legendPaint.setAntiAlias(true);
        legendPaint.setColor(Color.DKGRAY);
        legendPaint.setTextSize(28f); // 更小的字体
        legendPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setData(Transaction tx) {
        this.transaction = tx;
        generateSegments();
        invalidate();
    }

    public void setShowDetectedRisks(boolean showDetectedRisks) {
        this.showDetectedRisks = showDetectedRisks;
        if (transaction != null) {
            generateSegments();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (transaction == null || segments.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();

        // 使用更小的半径0.25f，并将环形图向右移动
        float outerRadius = Math.min(width, height) * 0.25f; // 0.25f半径
        float innerRadius = outerRadius * 0.5f;

        // 将环形图向右移动，紧贴左侧但为图例留出空间
        int centerX = (int) (outerRadius + 20); // 环形图紧贴左侧，留出20px边距
        int centerY = height / 2;

        RectF outerRect = new RectF(
                centerX - outerRadius,
                centerY - outerRadius,
                centerX + outerRadius,
                centerY + outerRadius
        );

        // 绘制环形图
        float startAngle = -90f;
        for (int i = 0; i < segments.size(); i++) {
            ChartSegment segment = segments.get(i);
            if (segment.active) {
                paint.setColor(segment.color);
                canvas.drawArc(outerRect, startAngle, segment.angle, true, paint);
                startAngle += segment.angle;
            }
        }

        // 绘制中心空白圆
        paint.setColor(Color.WHITE);
        canvas.drawCircle(outerRect.centerX(), outerRect.centerY(), innerRadius, paint);

        // 绘制图例 - 在环形图右侧，使用剩余的大部分空间
        float legendStartX = outerRect.right + 15; // 减少间距
        float legendStartY = outerRect.top;
        float legendWidth = width - legendStartX - 10; // 计算图例可用宽度
        drawLegend(canvas, legendStartX, legendStartY, outerRect.height(), legendWidth);
    }

    private void drawLegend(Canvas canvas, float startX, float startY, float maxHeight, float availableWidth) {
        if (segments.isEmpty()) return;

        float currentY = startY;
        float lineHeight = 40f; // 更小的行高
        float colorBoxSize = 22f; // 更小的颜色方块
        float textMargin = 10f; // 更小的边距
        float textYOffset = 15f; // 调整文字垂直偏移

        // 绘制图例标题
        legendPaint.setColor(Color.BLACK);
        legendPaint.setTextSize(26f);
        String title = showDetectedRisks ? "风险项" : "安全项";
        canvas.drawText(title, startX, currentY - 5, legendPaint);
        currentY += 30;

        for (int i = 0; i < segments.size(); i++) {
            ChartSegment segment = segments.get(i);
            if (!segment.active) continue;

            // 绘制颜色方块
            paint.setColor(segment.color);
            RectF colorRect = new RectF(startX, currentY, startX + colorBoxSize, currentY + colorBoxSize);
            canvas.drawRoundRect(colorRect, 4, 4, paint);

            // 绘制文字说明
            legendPaint.setColor(Color.BLACK);
            legendPaint.setTextSize(24f);

            // 测量文字宽度，如果太长则自动换行或截断
            String displayName = segment.displayName;
            float textWidth = legendPaint.measureText(displayName);

            // 如果文字宽度超过可用空间，使用缩写
            if (textWidth > availableWidth - colorBoxSize - textMargin) {
                if (displayName.length() > 3) {
                    displayName = displayName.substring(0, 3);
                }
            }

            canvas.drawText(displayName,
                    startX + colorBoxSize + textMargin,
                    currentY + textYOffset,
                    legendPaint);

            currentY += lineHeight;

            // 如果超出高度，提前结束
            if (currentY > startY + maxHeight - lineHeight) {
                // 可以在这里添加多列支持
                break;
            }
        }

        // 如果没有检测项，显示提示信息
        if (isAllSegmentsInactive()) {
            legendPaint.setColor(Color.GRAY);
            legendPaint.setTextSize(24f);
            String message = showDetectedRisks ? "无风险" : "无安全项";
            canvas.drawText(message, startX, currentY + 25, legendPaint);
        }
    }

    private boolean isAllSegmentsInactive() {
        for (ChartSegment segment : segments) {
            if (segment.active) {
                return false;
            }
        }
        return true;
    }

    private void generateSegments() {
        segments.clear();

        if (transaction == null) return;

        // 收集检测项
        List<DetectionItem> items = new ArrayList<>();

        // 添加所有检测项
        items.add(new DetectionItem("night", "深夜交易", 0, transaction.isNight()));
        items.add(new DetectionItem("locationJump", "位置跳跃", 1, transaction.isLocationJump()));
        items.add(new DetectionItem("rootDetected", "Root环境", 2, transaction.isRootDetected()));
        items.add(new DetectionItem("debuggable", "调试模式", 3, transaction.isDebuggable()));
        items.add(new DetectionItem("debuggerConnected", "调试器连接", 4, transaction.isDebuggerConnected()));
        items.add(new DetectionItem("invalidVersion", "版本异常", 5, transaction.isInvalidVersion()));

        // 合并签名校验相关项
        boolean signatureDetected = transaction.isSha256Mismatch() ||
                transaction.isMd5Mismatch() ||
                transaction.isCrcCheckFailed() ||
                transaction.isEnhancedCrcCheckFailed();
        items.add(new DetectionItem("signature", "签名校验", 6, signatureDetected));

        items.add(new DetectionItem("runningInEmulator", "模拟器", 7, transaction.isRunningInEmulator()));
        items.add(new DetectionItem("jdwpDetected", "JDWP", 8, transaction.isJdwpDetected()));

        // 根据显示模式过滤项
        List<DetectionItem> activeItems = new ArrayList<>();
        for (DetectionItem item : items) {
            if (showDetectedRisks) {
                if (item.detected) {
                    activeItems.add(item);
                }
            } else {
                if (!item.detected) {
                    activeItems.add(item);
                }
            }
        }

        // 如果没有符合条件的项
        if (activeItems.isEmpty()) {
            String displayName = showDetectedRisks ? "无风险" : "无安全项";
            int color = showDetectedRisks ? Color.parseColor("#E8E8E8") : Color.parseColor("#F5F5F5");
            segments.add(new ChartSegment(displayName, true, 360f, color));
            return;
        }

        // 计算每个项的扇形角度（等分）
        float anglePerItem = 360f / activeItems.size();

        // 创建扇形段
        for (DetectionItem item : activeItems) {
            int colorIndex = Math.min(item.colorIndex,
                    showDetectedRisks ? RISK_COLORS.length - 1 : SAFE_COLORS.length - 1);
            int color = showDetectedRisks ? RISK_COLORS[colorIndex] : SAFE_COLORS[colorIndex];

            segments.add(new ChartSegment(
                    item.displayName,
                    true,
                    anglePerItem,
                    color
            ));
        }
    }

    // 检测项数据结构
    private static class DetectionItem {
        String key;
        String displayName;
        int colorIndex;
        boolean detected;

        DetectionItem(String key, String displayName, int colorIndex, boolean detected) {
            this.key = key;
            this.displayName = displayName;
            this.colorIndex = colorIndex;
            this.detected = detected;
        }
    }

    // 扇形段数据结构
    private static class ChartSegment {
        String displayName;
        boolean active;
        float angle;
        int color;

        ChartSegment(String displayName, boolean active, float angle, int color) {
            this.displayName = displayName;
            this.active = active;
            this.angle = angle;
            this.color = color;
        }
    }
}