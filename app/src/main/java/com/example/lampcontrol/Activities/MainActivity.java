package com.example.lampcontrol.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lampcontrol.Fragments.PageFragmentConnect;
import com.example.lampcontrol.Fragments.PageFragmentControl;
import com.example.lampcontrol.R;
import com.example.lampcontrol.Utils.PermissionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private FragmentManager fragmentManager;
    private BluetoothAdapter bluetoothAdapter;
    private PermissionManager permissionManager;

    private static final int REQUEST_ENABLE_BT = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionManager = new PermissionManager(this);
        permissionManager.checkForPermissions();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        createTabs();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Bluetooth недоступен", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createTabs() {
        tabLayout = findViewById(R.id.tabLayout);

        TabLayout.Tab control = tabLayout.newTab();
        control.setText("Управление");
        tabLayout.addTab(control, 0, true);

        TabLayout.Tab connect = tabLayout.newTab();
        connect.setText("Подключить");
        tabLayout.addTab(connect, 1, true);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        fragmentManager = getSupportFragmentManager();
        setTab(0);
    }

    private void setTab(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
            default:
                fragment = new PageFragmentControl();
                break;
            case 1:
                fragment = new PageFragmentConnect();
                break;
        }
        beginTransaction(fragment);
        Objects.requireNonNull(tabLayout.getTabAt(position)).select();
    }

    private void beginTransaction(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

}