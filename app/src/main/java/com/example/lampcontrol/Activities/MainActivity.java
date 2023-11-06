package com.example.lampcontrol.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lampcontrol.Application.LampApplication;
import com.example.lampcontrol.Fragments.PageFragmentConnect;
import com.example.lampcontrol.Fragments.PageFragmentControl;
import com.example.lampcontrol.R;
import com.example.lampcontrol.Utils.LampsDataBase;
import com.example.lampcontrol.Utils.PermissionManager;
import com.example.lampcontrol.Views.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

    private FloatingActionButton actionButton;
    private LampApplication lampApplication;
    private LampsDataBase lampsDataBase;

    private FragmentManager fragmentManager;
    private BluetoothAdapter bluetoothAdapter;
    private PermissionManager permissionManager;

    private static final int REQUEST_ENABLE_BT = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionButton = findViewById(R.id.actionButton);

        lampApplication = (LampApplication) getApplication();
        lampsDataBase = lampApplication.getLampsDataBase();

        lampsDataBase.addDataBaseListener(list -> setBreathingAnimation(list.isEmpty()));

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionButton.isToggled()) {
                    beginTransaction(new PageFragmentControl());
                } else {
                    beginTransaction(new PageFragmentConnect());
                }
                actionButton.onClick(view);
            }
        });

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.frameLayout);
            if (currentFragment instanceof PageFragmentConnect) {
                actionButton.toggleOn();
            } else {
                actionButton.toggleOff();
            }
        });
        beginTransaction(new PageFragmentControl());

        permissionManager = new PermissionManager(this);
        permissionManager.checkForPermissions();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

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

    private void beginTransaction(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void setBreathingAnimation(boolean isEmpty) {
        if (isEmpty) {
            actionButton.startBreathingAnimation();
        } else {
            actionButton.cancelBreathingAnimation();
        }
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    @Override
    public void onBackPressed() {
        if (actionButton.isToggled()) {
            actionButton.callOnClick();
        }

    }

}