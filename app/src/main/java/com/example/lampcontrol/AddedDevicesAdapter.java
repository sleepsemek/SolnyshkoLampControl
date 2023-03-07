package com.example.lampcontrol;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AddedDevicesAdapter extends RecyclerView.Adapter<AddedDevicesAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<CreateList> addedList;
    private final DeviceDataBase dataBase;

    private final int MENU_RENAME = R.id.rename;
    private final int MENU_DELETE = R.id.delete;

    public AddedDevicesAdapter(Context applicationContext, DeviceDataBase dataBase) {
        this.context = applicationContext;
        this.addedList = dataBase.getList();
        this.dataBase = dataBase;
    }

    @NonNull
    @Override
    public AddedDevicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.added_lamp, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddedDevicesAdapter.ViewHolder holder, int position) {
        holder.title.setText(addedList.get(position).getName());
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, detailedLampActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("address", addedList.get(holder.getAdapterPosition()).getAddress() + "");
            intent.putExtra("name", addedList.get(holder.getAdapterPosition()).getName() + "");
            context.startActivity(intent);
        });

        holder.edit.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(context, R.style.PopupMenu);
            PopupMenu popup = new PopupMenu(wrapper, holder.edit);
            popup.inflate(R.menu.added_lamp_menu);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case MENU_RENAME:
                        renameActivity(holder);
                        return true;
                    case MENU_DELETE:
                        deleteDialogShow(holder);
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        });
    }

    private void remove(String address) {
        dataBase.removeLamp(address);
    }

    private void deleteDialogShow(ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Подтвердить действие");
        builder.setMessage("Вы уверены, что хотите удалить устройство?");

        builder.setPositiveButton("Удалить", (dialog, which) -> {
            remove(addedList.get(holder.getAdapterPosition()).getAddress());
            notifyDataSetChanged();
        });

        builder.setNegativeButton("Отменить", (dialog, which) -> {});

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void renameActivity(ViewHolder holder) {
        Intent intent = new Intent(context, detailedAddLamp.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("address", addedList.get(holder.getAdapterPosition()).getAddress() + "");
        intent.putExtra("name", addedList.get(holder.getAdapterPosition()).getName() + "");
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return addedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final ImageButton edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.addedName);
            edit = itemView.findViewById(R.id.edit);
        }
    }
}
