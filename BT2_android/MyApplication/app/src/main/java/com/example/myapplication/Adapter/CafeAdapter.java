package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Model.Cafe;
import com.example.myapplication.R;
import java.util.List;

public class CafeAdapter extends RecyclerView.Adapter<CafeAdapter.CafeViewHolder> {

    private List<Cafe> cafeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cafe cafe);
    }

    public CafeAdapter(List<Cafe> cafeList, OnItemClickListener listener) {
        this.cafeList = cafeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CafeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cafe, parent, false);
        return new CafeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CafeViewHolder holder, int position) {
        Cafe cafe = cafeList.get(position);
        holder.cafeName.setText(cafe.getName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(cafe));
    }

    @Override
    public int getItemCount() {
        return cafeList.size();
    }

    public static class CafeViewHolder extends RecyclerView.ViewHolder {
        TextView cafeName;

        public CafeViewHolder(@NonNull View itemView) {
            super(itemView);
            cafeName = itemView.findViewById(R.id.cafeName);
        }
    }
}