package com.example.lampcontrol.activities;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lampcontrol.R;
import com.example.lampcontrol.models.POJO.Lamp;
import com.example.lampcontrol.ui.views.FloatingActionButton;
import com.example.lampcontrol.views.MainView;
import com.example.lampcontrol.presenters.MainPresenter;

import java.util.ArrayList;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;

public class MainActivity extends MvpAppCompatActivity implements MainView {

    @InjectPresenter
    public MainPresenter mainPresenter;

    private FloatingActionButton actionButton;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionButton = findViewById(R.id.actionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainPresenter.handleFragmentSwitchFAB(actionButton.isToggled());
            }
        });

        fragmentManager = getSupportFragmentManager();

    }

    @Override
    public void onBackPressed() {
        mainPresenter.handleBackPress(actionButton.isToggled());
    }

    @Override
    public void showFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void updateFab(boolean isToggled) {
        if (isToggled) {
            actionButton.toggleOn();
        } else {
            actionButton.toggleOff();
        }
    }

    @Override
    public void switchFabBreathing(boolean breathing) {
        if (breathing) {
            actionButton.startBreathingAnimation();
        } else {
            actionButton.cancelBreathingAnimation();
        }
    }

    @Override
    public void requestBluetoothEnable() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, MainPresenter.REQUEST_ENABLE_BT);
    }

    @Override
    public void makeMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mainPresenter.handleBluetoothEnabledResult(requestCode, resultCode);
    }

    @Override
    public void checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
             if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                 mainPresenter.handlePermissionResult(false);
             }
        } else {
             if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                 mainPresenter.handlePermissionResult(false);
             }
        }
        mainPresenter.handlePermissionResult(true);
    }

    @Override
    public void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN},
                    101);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
        }
    }

}