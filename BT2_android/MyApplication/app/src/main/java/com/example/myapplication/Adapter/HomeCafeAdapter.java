package com.example.myapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.Cafe;
import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HomeCafeAdapter extends RecyclerView.Adapter<HomeCafeAdapter.CafeViewHolder> {

    private Context context;
    private List<Cafe> cafeList;
    private OnCafeClickListener onCafeClickListener; // Listener cho sự kiện nhấn

    // Constructor đầy đủ với listener
    public HomeCafeAdapter(Context context, List<Cafe> cafeList, OnCafeClickListener listener) {
        this.context = context;
        this.cafeList = cafeList;
        this.onCafeClickListener = listener;
    }

    // Constructor không có listener (tương thích với code cũ)
    public HomeCafeAdapter(Context context, List<Cafe> cafeList) {
        this(context, cafeList, null);
    }

    @NonNull
    @Override
    public CafeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cafe, parent, false);
        return new CafeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CafeViewHolder holder, int position) {
        Cafe cafe = cafeList.get(position);

        // Hiển thị thông tin quán
        holder.tvCafeName.setText(cafe.getName() != null ? cafe.getName() : "Tên quán");
        holder.tvAddress.setText("Địa chỉ: " + (cafe.getLocationText() != null ? cafe.getLocationText() : "Không có địa chỉ"));
        holder.tvRating.setText("Đánh giá: " + (cafe.getRatingStar() != null ? String.format("%.1f", cafe.getRatingStar()) : "0.0") + "/5");

        // Hiển thị hình ảnh
        if (cafe.getImage1() != null && !cafe.getImage1().isEmpty()) {
            Picasso.get().load(cafe.getImage1()).into(holder.ivCafeImage);
        } else {
            holder.ivCafeImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Xử lý sự kiện nhấn vào item
        holder.itemView.setOnClickListener(v -> {
            if (onCafeClickListener != null) {
                onCafeClickListener.onCafeClick(cafe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cafeList.size();
    }

    // Class ViewHolder ánh xạ với layout mới
    public static class CafeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCafeImage;
        TextView tvCafeName, tvAddress, tvRating;

        public CafeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCafeImage = itemView.findViewById(R.id.ivCafeImage);
            tvCafeName = itemView.findViewById(R.id.tvCafeName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }

    // Interface cho sự kiện nhấn vào item
    public interface OnCafeClickListener {
        void onCafeClick(Cafe cafe);
    }
}