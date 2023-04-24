package com.example.lampcontrol.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.example.lampcontrol.Adapters.BondedDevicesAdapter;
import com.example.lampcontrol.Lamp;
import com.example.lampcontrol.R;

import java.util.ArrayList;
import java.util.Set;

public class PageFragmentConnect extends Fragment {

    private BondedDevicesAdapter bondedDevicesAdapter;
    private RecyclerView recyclerView;

    private Button displayConnected;

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    private final ArrayList<Lamp> list = new ArrayList<>();

    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_page_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        recyclerView = requireView().findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        turnOnBT();
        displayConnected();

        bondedDevicesAdapter = new BondedDevicesAdapter(requireActivity().getApplicationContext(), list);
        recyclerView.setAdapter(bondedDevicesAdapter);

        displayConnected = requireView().findViewById(R.id.Connected);
        displayConnected.setOnClickListener(view1 -> {
            displayConnected();
            displayConnected.animate().rotationBy(360).setDuration(500).setInterpolator(new DecelerateInterpolator());
        });
    }

    private void displayConnected() {
        list.clear();
        if (bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(requireActivity().getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_DENIED) {
                Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : devices) {
                    Lamp createList = new Lamp();
                    createList.setName(device.getName());
                    createList.setAddress(device.getAddress());
                    list.add(createList);
                }
            } else {
                makeToast("Пожалуйста, выдайте\n необходимые разрешения для\n корректной работы Bluetooth");
            }
            recyclerView.setAdapter(bondedDevicesAdapter);
        } else {
            makeToast("Bluetooth выключен");
        }
    }

    public void turnOnBT() {
        if (!bluetoothAdapter.isEnabled()) {
            makeToast("Включение Bluetooth...");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
    }

    public void turnDiscoverable() {
        if (ActivityCompat.checkSelfPermission(requireActivity().getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_DENIED) {
            if (!bluetoothAdapter.isDiscovering()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(intent, REQUEST_DISCOVER_BT);
            }
        } else {
            makeToast("Пожалуйста выдайте разрешение для корректной работы Bluetooth");
        }
    }


    private void makeToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                makeToast("Bluetooth недоступен");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}