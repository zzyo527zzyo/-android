package com.example.secupay_jni.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secupay_jni.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<String> descriptionList;
    public HistoryAdapter(List<String> descriptionList) {
        this.descriptionList = descriptionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String desc = descriptionList.get(position);
        if (desc  != null && !desc.isEmpty()) {
            holder.tvDescription.setText(desc.toString());
        } else {
            holder.tvDescription.setText("无转账记录");
        }
    }

    @Override
    public int getItemCount() {
        return descriptionList == null ? 0 : descriptionList.size();
    }
    //刷新
    public void addDescription(String des) {
        descriptionList.add(0,des);

        // 限制最多显示 5 条
        if (descriptionList.size() > 5) {
            descriptionList.remove(descriptionList.size() - 1);
            notifyItemRemoved(descriptionList.size()); // 动画：移除最后一条
        }

        // 通知插入第一条（带动画）
        notifyItemInserted(0);

    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }
}