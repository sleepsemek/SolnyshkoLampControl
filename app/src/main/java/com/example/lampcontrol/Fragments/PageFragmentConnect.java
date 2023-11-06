package com.example.lampcontrol.Fragments;

import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.Activities.MainActivity;
import com.example.lampcontrol.Adapters.DiscoveredDevicesAdapter;
import com.example.lampcontrol.Application.LampApplication;
import com.example.lampcontrol.Models.AdvertisementData;
import com.example.lampcontrol.R;
import com.example.lampcontrol.Utils.LampsDataBase;
import com.example.lampcontrol.Utils.PermissionManager;

import java.util.ArrayList;
import java.util.List;

public class PageFragmentConnect extends Fragment {

    private DiscoveredDevicesAdapter discoveredDevicesAdapter;
    private RecyclerView recyclerView;

    private LampsDataBase dataBase;
    private LampApplication application;

    private AppCompatButton refreshButton;
    private ValueAnimator animator;

    private final ArrayList<BluetoothDevice> devicesList = new ArrayList<>();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;

    private PermissionManager permissionManager;

    private boolean scanning = false;
    private final Handler handler = new Handler();

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
        application = (LampApplication) requireActivity().getApplication();
        dataBase = application.getLampsDataBase();
        recyclerView = requireView().findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        discoveredDevicesAdapter = new DiscoveredDevicesAdapter(this, devicesList);
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

    public void addLamp(String name, String address) {
        dataBase.addLamp(name, address);
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, new PageFragmentControl());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
            bluetoothScanner.startScan(setupFilters(), setupSettings(), scanCallback);
        } else {
            scanning = false;
            bluetoothScanner.stopScan(scanCallback);
        }
        startRefreshAnimation();
    }

    private List<ScanFilter> setupFilters() {
        List<ScanFilter> filters = new ArrayList<>();
        AdvertisementData advertisementData = new AdvertisementData("NS", "Solnyshko OYFB-04M");
        ScanFilter filter = new ScanFilter.Builder()
                .setManufacturerData(advertisementData.getAdvertisementId(), advertisementData.getByteArrayAdvertisementData())
                .build();
            filters.add(filter);
        return filters;
    }

    private ScanSettings setupSettings() {
        return new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

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