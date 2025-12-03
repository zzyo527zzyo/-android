package com.example.secupay_jni.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.secupay_jni.R;
import com.example.secupay_jni.model.RiskLevel;
import com.example.secupay_jni.model.Transaction;

import java.util.ArrayList;
import java.util.List;


//RecyclerView适配器：渲染交易列表，并按风险等级着色。
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private final List<Transaction> transactions = new ArrayList<>();
    private Context context; // ✅ 添加这一行：声明成员变量
    public Adapter(Context context) {
        this.context = context;
    }
    //将item_transaction.xml转化成view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactions.get(position);
        Context ctx = holder.itemView.getContext();
        holder.tvLocation.setText("支付地点:"+tx.getLocation());
        holder.tvTime.setText("支付时间:"+tx.getFormattedTime());
        String label = riskLabel(ctx, tx.getRiskLevel());
        holder.tvRisk.setText("风险等级:"+label);
        holder.tvReason.setText("风险原因:"+tx.getRiskReason());

        // 风险可视高亮
        View container = holder.container;
        int bgColor;
        if (tx.getRiskLevel() == RiskLevel.HIGH) {
            bgColor = ctx.getResources().getColor(R.color.risk_high_bg);
        } else if (tx.getRiskLevel() == RiskLevel.MEDIUM) {
            bgColor = ctx.getResources().getColor(R.color.risk_medium_bg);
        } else {
            bgColor = ctx.getResources().getColor(R.color.risk_low_bg);
        }
        container.setBackgroundColor(bgColor);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }


    //刷新
    public void addTransaction(Transaction tx) {
        // 插入到顶部（最新交易在最上面）
        transactions.add(0, tx);

        // 限制最多显示 2 条，删除最老的
        if (transactions.size() > 5) {
            transactions.remove(transactions.size() - 1);
            notifyItemRemoved(transactions.size()); // 动画：移除最后一条
        }

        // 通知插入第一条（带动画）
        notifyItemInserted(0);

        // 弹出提示
        Toast.makeText(context, "检测成功", Toast.LENGTH_SHORT).show();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvLocation;
        TextView tvTime;
        TextView tvRisk;
        TextView tvReason;
        View container;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRisk = itemView.findViewById(R.id.tvRisk);
            tvReason = itemView.findViewById(R.id.tvReason);
            container = itemView.findViewById(R.id.container);
        }
    }


    //将风险等级映射为本地化字符串。
    private String riskLabel(Context ctx, RiskLevel level) {
        switch (level) {
            case HIGH:
                return ctx.getString(R.string.risk_high);
            case MEDIUM:
                return ctx.getString(R.string.risk_medium);
            default:
                return ctx.getString(R.string.risk_low);
        }
    }
}


