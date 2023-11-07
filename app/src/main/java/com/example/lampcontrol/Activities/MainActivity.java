package com.example.lampcontrol.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lampcontrol.R;
import com.example.lampcontrol.views.FloatingActionButton;
import com.example.lampcontrol.views.MainView;
import com.example.lampcontrol.presenters.MainPresenter;

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
                actionButton.onClick(view);
            }
        });

        fragmentManager = getSupportFragmentManager();

    }

    @Override
    public void onBackPressed() {
        if (actionButton.isToggled()) {
            actionButton.callOnClick();
        }

    }

    @Override
    public void showFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void showEmptyListHint(boolean isEmpty) {
        if (isEmpty) {
            actionButton.startBreathingAnimation();
        } else {
            actionButton.cancelBreathingAnimation();
        }
    }

    @Override
    public void requestBluetoothEnable() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, mainPresenter.REQUEST_ENABLE_BT);
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
}