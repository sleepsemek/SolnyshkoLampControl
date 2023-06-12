package com.example.lampcontrol.Adapters;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.Activities.EditLampActivity;
import com.example.lampcontrol.POJO.Lamp;
import com.example.lampcontrol.R;

import java.util.ArrayList;

public class BondedDevicesAdapter extends RecyclerView.Adapter<BondedDevicesAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Lamp> devices;

    public BondedDevicesAdapter(Context applicationContext, ArrayList<Lamp> devices) {
        this.context = applicationContext;
        this.devices = devices;
    }

    @NonNull
    @Override
    public BondedDevicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bonded_lamp, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BondedDevicesAdapter.ViewHolder holder, int position) {
        holder.title.setText(devices.get(position).getName());
        holder.address.setText(devices.get(position).getAddress());
        holder.addDevice.setOnClickListener(view -> {
            Intent intent = new Intent(context, EditLampActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("address", devices.get(holder.getAdapterPosition()).getAddress() + "");
            intent.putExtra("name", devices.get(holder.getAdapterPosition()).getName() + "");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView title;
        private final TextView address;
        private final ImageButton addDevice;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.BTdeviceName);
            address = view.findViewById(R.id.BTdeviceAddress);
            addDevice = view.findViewById(R.id.addDevice);
        }
    }

}
