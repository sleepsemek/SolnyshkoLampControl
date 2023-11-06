package com.example.lampcontrol.Adapters;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.Fragments.PageFragmentControl;
import com.example.lampcontrol.Models.Lamp;
import com.example.lampcontrol.R;

import java.util.ArrayList;

public class AddedDevicesAdapter extends RecyclerView.Adapter<AddedDevicesAdapter.ViewHolder> {

    private final PageFragmentControl fragmentControl;
    private final ArrayList<Lamp> addedList;

    private final int MENU_RENAME = R.id.rename;
    private final int MENU_DELETE = R.id.delete;

    public AddedDevicesAdapter(PageFragmentControl fragmentControl, ArrayList<Lamp> addedList) {
        this.fragmentControl = (PageFragmentControl) fragmentControl;
        this.addedList = addedList;
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
            fragmentControl.startControlActivity(holder.getAdapterPosition());
        });

        holder.itemView.setOnLongClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(fragmentControl.getContext(), R.style.PopupMenu);
            PopupMenu popup = new PopupMenu(wrapper, holder.itemView);
            popup.inflate(R.menu.added_lamp_menu);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case MENU_RENAME:
                        renameLamp(holder.getAdapterPosition());
                        return true;
                    case MENU_DELETE:
                        deleteDialogShow(holder);
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
            return true;
        });

    }

    private void remove(String address) {
        fragmentControl.deleteLamp(address);
    }

    private void renameLamp(int index) {
        fragmentControl.renameLamp(index);
    }

    private void deleteDialogShow(ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle(Html.fromHtml("<font color='#e31e24'>Подтвердить действие</font>"));
        builder.setMessage("Вы уверены, что хотите удалить устройство?");

        builder.setPositiveButton(Html.fromHtml("<font color='#e31e24'>Удалить</font>"), (dialog, which) -> {
            holder.itemView.animate().scaleX(0).scaleY(0).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    remove(addedList.get(holder.getAdapterPosition()).getAddress());
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            }).start();
        });

        builder.setNegativeButton("Отменить", (dialog, which) -> {});

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return addedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.addedName);
        }
    }
}
