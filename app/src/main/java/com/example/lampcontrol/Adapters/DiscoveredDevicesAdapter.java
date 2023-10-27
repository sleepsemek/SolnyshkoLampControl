package com.example.lampcontrol.Adapters;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.Activities.EditLampActivity;
import com.example.lampcontrol.R;

import java.util.ArrayList;

public class DiscoveredDevicesAdapter extends RecyclerView.Adapter<DiscoveredDevicesAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<BluetoothDevice> devices;

    public DiscoveredDevicesAdapter(Context applicationContext, ArrayList<BluetoothDevice> devices) {
        this.context = applicationContext;
        this.devices = devices;
    }

    @NonNull
    @Override
    public DiscoveredDevicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discovered_lamp, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoveredDevicesAdapter.ViewHolder holder, int position) {
        holder.title.setText(devices.get(position).getName());
        holder.address.setText(devices.get(position).getAddress());
        holder.addDeviceBox.setOnClickListener(view -> {
            Intent intent = new Intent(context, EditLampActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("address", devices.get(holder.getAdapterPosition()).getAddress() + "");
            intent.putExtra("name", (devices.get(holder.getAdapterPosition()).getName() == null) ? "Без имени" : (devices.get(holder.getAdapterPosition()).getName() + ""));
            context.startActivity(intent);
        });
    }

    public void addDevice(BluetoothDevice device) {
        if (!devices.contains(device)) {
            devices.add(device);
            notifyItemInserted(devices.size() - 1);
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView title;
        private final TextView address;
        private final LinearLayout addDeviceBox;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.BTdeviceName);
            address = view.findViewById(R.id.BTdeviceAddress);
            addDeviceBox = view.findViewById(R.id.addDeviceBox);
        }
    }

}
