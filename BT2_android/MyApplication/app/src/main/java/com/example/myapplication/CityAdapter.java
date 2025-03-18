// CityAdapter.java
package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private List<City> cityList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(City city);
    }

    public CityAdapter(List<City> cityList, OnItemClickListener listener) {
        this.cityList = cityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        City city = cityList.get(position);
        holder.cityName.setText(city.getName());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(city));
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    public static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView cityName;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.cityName);
        }
    }
}
