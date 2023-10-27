package com.example.lampcontrol.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lampcontrol.Fragments.PageFragmentConnect;
import com.example.lampcontrol.Fragments.PageFragmentControl;
import com.example.lampcontrol.R;
import com.example.lampcontrol.Utils.PermissionManager;


public class MainActivity extends AppCompatActivity {

    private AppCompatButton actionButton;

    private FragmentManager fragmentManager;
    private BluetoothAdapter bluetoothAdapter;
    private PermissionManager permissionManager;

    private boolean isToggled = false;
    private static final int REQUEST_ENABLE_BT = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        beginTransaction(new PageFragmentControl());

        actionButton = findViewById(R.id.actionButton);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionButton.animate().rotation(360).setDuration(500).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        actionButton.setRotation(0);
                    }
                });

                if (!isToggled) {
                    actionButton.setForeground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_lightbulb_48));
                    beginTransaction(new PageFragmentConnect());
                } else {
                    actionButton.setForeground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_add_48));
                    beginTransaction(new PageFragmentControl());
                }

                isToggled = !isToggled;

            }
        });

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
        fragmentTransaction.commit();
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

}