package com.example.lampcontrol.Adapters;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.Fragments.PageFragmentConnect;
import com.example.lampcontrol.R;

import java.util.ArrayList;

public class DiscoveredDevicesAdapter extends RecyclerView.Adapter<DiscoveredDevicesAdapter.ViewHolder> {

    private final PageFragmentConnect pageFragmentConnect;
    private final ArrayList<BluetoothDevice> devices;

    public DiscoveredDevicesAdapter(PageFragmentConnect pageFragmentConnect, ArrayList<BluetoothDevice> devices) {
        this.pageFragmentConnect = (PageFragmentConnect) pageFragmentConnect;
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
        holder.itemView.setOnClickListener(view -> {
            pageFragmentConnect.addLamp(devices.get(position).getName(), devices.get(position).getAddress());
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

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.BTdeviceName);
            address = view.findViewById(R.id.BTdeviceAddress);
        }
    }

}
