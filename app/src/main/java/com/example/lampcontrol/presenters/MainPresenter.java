package com.example.lampcontrol.presenters;

import static android.app.Activity.RESULT_OK;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.example.lampcontrol.Application.LampApplication;
import com.example.lampcontrol.Fragments.PageFragmentConnect;
import com.example.lampcontrol.Fragments.PageFragmentControl;
import com.example.lampcontrol.Utils.LampsDataBase;
import com.example.lampcontrol.Utils.PermissionManager;
import com.example.lampcontrol.views.MainView;

import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class MainPresenter extends MvpPresenter<MainView> {

    private LampApplication lampApplication;
    private LampsDataBase lampsDataBase;
    private BluetoothAdapter bluetoothAdapter;
    private PermissionManager permissionManager;

    public static final int REQUEST_ENABLE_BT = 202;

    public MainPresenter() {
        getViewState().showFragment(new PageFragmentControl());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkIfBluetoothEnabled();

    }

    private void checkIfBluetoothEnabled() {
        if (!bluetoothAdapter.isEnabled()) {
            getViewState().requestBluetoothEnable();
        }
    }

    public void handleBluetoothEnabledResult(int requestCode, int resultCode) {
        if (requestCode != REQUEST_ENABLE_BT) {
            return;
        }

        if (resultCode != RESULT_OK) {
            getViewState().makeMessage("Bluetooth недоступен");
        }
    }

    public void handleFragmentSwitchFAB(boolean toggled) {
        if (toggled) {
            getViewState().showFragment(new PageFragmentControl());
        } else {
            getViewState().showFragment(new PageFragmentConnect());
        }
    }

    public void handleAddButtonClick(BluetoothDevice device) {

    }
}
