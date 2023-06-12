package com.example.lampcontrol.Fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.Activities.MainActivity;
import com.example.lampcontrol.Adapters.BondedDevicesAdapter;
import com.example.lampcontrol.POJO.Lamp;
import com.example.lampcontrol.R;

import java.util.ArrayList;
import java.util.Set;

public class PageFragmentConnect extends Fragment {

    private BondedDevicesAdapter bondedDevicesAdapter;
    private RecyclerView recyclerView;

    private AppCompatButton refreshButton;
    private AppCompatButton addButton;

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

        bondedDevicesAdapter = new BondedDevicesAdapter(requireActivity().getApplicationContext(), list);
        recyclerView.setAdapter(bondedDevicesAdapter);

        displayConnected();

        refreshButton = requireView().findViewById(R.id.refresh);
        refreshButton.setOnClickListener(view1 -> {
            displayConnected();
            refreshButton.animate().rotationBy(720).setDuration(500).setInterpolator(new DecelerateInterpolator());
        });
        addButton = requireView().findViewById(R.id.add);
        addButton.setOnClickListener(view1 -> {
            openBluetoothDevicesSettings();
        });
    }

    private void displayConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            makeToast("Предоставьте необходимые разрешения");
            MainActivity mAct = (MainActivity) requireActivity();
            mAct.checkForPermissions();
            return;
        }

        list.clear();
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            Lamp createList = new Lamp();
            createList.setName(device.getName());
            createList.setAddress(device.getAddress());
            list.add(createList);
        }
        recyclerView.setAdapter(bondedDevicesAdapter);

    }

    private void makeToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void openBluetoothDevicesSettings() {
        Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentOpenBluetoothSettings);
    }

}