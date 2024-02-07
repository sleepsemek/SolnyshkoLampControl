package com.solnyshco.lampcontrol.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.solnyshco.lampcontrol.R;

import java.util.ArrayList;

public class ConnectAdapter extends RecyclerView.Adapter<ConnectAdapter.ViewHolder> {

    private final ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private final OnButtonClickListener onClickListener;

    public ConnectAdapter(OnButtonClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ConnectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.discovered_lamp, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectAdapter.ViewHolder holder, int position) {
        holder.bind(devices.get(holder.getAdapterPosition()), onClickListener);
    }

    public void addDevice(BluetoothDevice device) {
        if (!devices.contains(device)) {
            devices.add(device);
            notifyItemInserted(devices.size() - 1);
        }
    }

    public void removeDevice(BluetoothDevice device) {
        if (devices.contains(device)) {
            notifyItemRemoved(devices.indexOf(device));
            devices.remove(device);
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

        public void bind(BluetoothDevice device, OnButtonClickListener onClickListener) {
            title.setText(device.getName());
            address.setText(device.getAddress());

            if (onClickListener == null) {
                return;
            }

            itemView.setOnClickListener(view -> onClickListener.onAddClicked(device));
        }
    }

    public interface OnButtonClickListener {
        void onAddClicked(BluetoothDevice device);
    }

}
