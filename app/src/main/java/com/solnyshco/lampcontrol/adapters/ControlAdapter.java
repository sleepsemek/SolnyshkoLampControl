package com.solnyshco.lampcontrol.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.solnyshco.lampcontrol.R;
import com.solnyshco.lampcontrol.models.POJO.Lamp;

import java.util.ArrayList;

public class ControlAdapter extends RecyclerView.Adapter<ControlAdapter.ViewHolder> {

    private final ArrayList<Lamp> addedList;
    private final OnButtonClickListener onClickListener;

    public ControlAdapter(ArrayList<Lamp> addedList, OnButtonClickListener onClickListener) {
        this.addedList = addedList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ControlAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.added_lamp, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ControlAdapter.ViewHolder holder, int position) {
        holder.bind(addedList.get(position), onClickListener);
    }

    public void addLamp(int position) {
        notifyItemInserted(position);
    }

    public void editLamp(int position) {
//        notifyItemChanged(position);
        notifyDataSetChanged();
    }

    public void deleteLamp(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return addedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final AppCompatButton renameButton;
        private final AppCompatButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.addedName);
            renameButton = itemView.findViewById(R.id.rename_device);
            deleteButton = itemView.findViewById(R.id.delete_device);
        }

        public void bind(Lamp lamp, OnButtonClickListener onClickListener) {
            title.setText(lamp.getName());

            if (onClickListener == null) {
                return;
            }

            itemView.setOnClickListener(view -> onClickListener.onInstanceClicked(lamp, getAdapterPosition()));

            renameButton.setOnClickListener(view -> onClickListener.onRenameClicked(lamp, getAdapterPosition()));

            deleteButton.setOnClickListener(view -> onClickListener.onDeleteClicked(lamp, getAdapterPosition()));

        }

    }

    public interface OnButtonClickListener {

        void onDeleteClicked(Lamp lamp, int position);

        void onRenameClicked(Lamp lamp, int position);

        void onInstanceClicked(Lamp lamp, int position);
    }

}
