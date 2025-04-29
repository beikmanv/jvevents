package com.northcoders.jvevents.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.northcoders.jvevents.ui.mainactivity.RecyclerViewInterface;

import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseAdapter.BaseViewHolder> {
    protected List<T> itemList;
    protected Context context;
    protected RecyclerViewInterface recyclerViewInterface;

    public BaseAdapter(Context context, List<T> itemList, RecyclerViewInterface recyclerViewInterface) {
        this.itemList = itemList;
        this.recyclerViewInterface = recyclerViewInterface;
        this.context = context;
    }

    @NonNull
    @Override
    public abstract BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        T item = itemList.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            if (recyclerViewInterface != null) {
                recyclerViewInterface.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setFilteredList(List<T> filteredList) {
        this.itemList = filteredList;
        notifyDataSetChanged();
    }

    public abstract static class BaseViewHolder<T> extends RecyclerView.ViewHolder {
        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bind(T item);
    }
}
