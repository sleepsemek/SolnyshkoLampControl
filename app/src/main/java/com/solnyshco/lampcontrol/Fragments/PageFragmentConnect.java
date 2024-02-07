package com.solnyshco.lampcontrol.Fragments;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.solnyshco.lampcontrol.R;
import com.solnyshco.lampcontrol.adapters.ConnectAdapter;
import com.solnyshco.lampcontrol.presenters.ConnectPresenter;
import com.solnyshco.lampcontrol.views.ConnectView;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;

public class PageFragmentConnect extends MvpAppCompatFragment implements ConnectView {

    @InjectPresenter
    ConnectPresenter connectPresenter;

    private RecyclerView recyclerView;
    ConnectAdapter connectAdapter;

    private TextView scanButtonText;
    private AppCompatButton scanButton;

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
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        scanButtonText = requireView().findViewById(R.id.refreshText);
        scanButton = requireView().findViewById(R.id.refreshButton);

        scanButton.setOnClickListener(view1 -> connectPresenter.handleScanButtonPress());

    }


    @Override
    public void setScannedDevicesListAdapter() {
        connectAdapter = new ConnectAdapter(device -> connectPresenter.handleAddButtonClick(device));

        recyclerView.setAdapter(connectAdapter);
    }

    @Override
    public void startScanningAnimation() {
        requireActivity().runOnUiThread(() -> {
            scanButtonText.setVisibility(View.VISIBLE);
            scanButton.setVisibility(View.GONE);
        });
    }

    @Override
    public void stopScanningAnimation() {
        requireActivity().runOnUiThread(() -> {
            scanButtonText.setVisibility(View.GONE);
            scanButton.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void updateScanningDeviceList(BluetoothDevice device) {
        connectAdapter.addDevice(device);
    }

    @Override
    public void checkIfScanIsPermitted() {
        //TODO:: возможно фризит прилу, проверить
        connectPresenter.handlePermissionResult(Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void checkIfLocationEnabled() {
        requireActivity().runOnUiThread(() -> {
            LocationManager locationManager = (LocationManager) requireActivity().getSystemService(LOCATION_SERVICE);
            connectPresenter.handleGPSpermissionResult(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        });
    }

    @Override
    public void backPress() {
        requireActivity().onBackPressed();
    }

    @Override
    public void redirectToAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void redirectToGPSSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void removeAddedDeviceFromScanningList(BluetoothDevice device) {
        connectAdapter.removeDevice(device);
    }

    @Override
    public void makeMessage(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

}