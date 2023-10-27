package com.example.lampcontrol.Fragments;

import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
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
import com.example.lampcontrol.Adapters.DiscoveredDevicesAdapter;
import com.example.lampcontrol.R;
import com.example.lampcontrol.Utils.PermissionManager;

import java.util.ArrayList;

public class PageFragmentConnect extends Fragment {

    private DiscoveredDevicesAdapter discoveredDevicesAdapter;
    private RecyclerView recyclerView;

    private AppCompatButton refreshButton;
    private ValueAnimator animator;

    private final ArrayList<BluetoothDevice> devicesList = new ArrayList<>();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;

    private PermissionManager permissionManager;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        discoveredDevicesAdapter = new DiscoveredDevicesAdapter(requireActivity().getApplicationContext(), devicesList);
        recyclerView.setAdapter(discoveredDevicesAdapter);

        MainActivity mAct = (MainActivity) requireActivity();
        permissionManager = mAct.getPermissionManager();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

        refreshButton = requireView().findViewById(R.id.refresh);
        refreshButton.setOnClickListener(view1 -> {
            scanForDevices();
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

        if (!permissionManager.checkForPermissions()) {
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
            discoveredDevicesAdapter.addDevice(device);
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

}