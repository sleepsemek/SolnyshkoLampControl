package com.example.lampcontrol.Fragments;

import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.Activities.MainActivity;
import com.example.lampcontrol.Adapters.BondedDevicesAdapter;
import com.example.lampcontrol.R;

import java.util.ArrayList;

public class PageFragmentConnect extends Fragment {

    private BondedDevicesAdapter bondedDevicesAdapter;
    private RecyclerView recyclerView;

    private AppCompatButton refreshButton;
    private AppCompatButton addButton;
    private ValueAnimator animator;

    private final ArrayList<BluetoothDevice> devicesList = new ArrayList<>();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;

    private boolean scanning = false;
    private Handler handler = new Handler();

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
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

        bondedDevicesAdapter = new BondedDevicesAdapter(requireActivity().getApplicationContext(), devicesList);
        recyclerView.setAdapter(bondedDevicesAdapter);

        refreshButton = requireView().findViewById(R.id.refresh);
        refreshButton.setOnClickListener(view1 -> {
            scanForDevices();
        });

        addButton = requireView().findViewById(R.id.add);
        addButton.setOnClickListener(view1 -> {
            openBluetoothDevicesSettings();
        });

        scanForDevices();

    }

    private void startRefreshAnimation() {
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(500);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (scanning) {
                    float rotationDegrees = (float) animation.getAnimatedValue();
                    refreshButton.setRotation(rotationDegrees);
                } else {
                    animator.cancel();
                    refreshButton.setRotation(0);
                }
            }
        });

        animator.start();
    }

    private void scanForDevices() {
        MainActivity mAct = (MainActivity) requireActivity();
        if (!mAct.checkForPermissions()) {
            makeToast("Пожалуйста, выдайте необходимые для работы разрешения");
            return;
        }
        if (!scanning) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothScanner.stopScan(scanCallback);
                }
            }, 10000);

            scanning = true;
            bluetoothScanner.startScan(scanCallback);
        } else {
            scanning = false;
            bluetoothScanner.stopScan(scanCallback);
        }
        startRefreshAnimation();
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            bondedDevicesAdapter.addDevice(device);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            System.out.println(errorCode);
        }
    };

    private void makeToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void openBluetoothDevicesSettings() {
        Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentOpenBluetoothSettings);
    }

}