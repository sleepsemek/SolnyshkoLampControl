package com.solnyshco.lampcontrol.presenters;

import static android.app.Activity.RESULT_OK;

import android.bluetooth.BluetoothAdapter;

import com.solnyshco.lampcontrol.Fragments.PageFragmentConnect;
import com.solnyshco.lampcontrol.Fragments.PageFragmentControl;
import com.solnyshco.lampcontrol.LampApplication;
import com.solnyshco.lampcontrol.repository.LampsDataBaseManager;
import com.solnyshco.lampcontrol.views.MainView;

import moxy.MvpPresenter;

public class MainPresenter extends MvpPresenter<MainView> {

    private LampsDataBaseManager lampsDataBaseManager;

    private PageFragmentControl pageFragmentControl;
    private PageFragmentConnect pageFragmentConnect;

    private BluetoothAdapter bluetoothAdapter;
    public static final int REQUEST_ENABLE_BT = 202;

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        pageFragmentControl = new PageFragmentControl();
        pageFragmentConnect = new PageFragmentConnect();

        LampApplication application = LampApplication.getInstance();
        lampsDataBaseManager = application.getDatabaseManager();
        getViewState().switchFabBreathing(lampsDataBaseManager.getList().isEmpty());
        getViewState().showFragment(pageFragmentControl);
        getViewState().updateFab(false);

        getViewState().checkForPermissions();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkIfBluetoothEnabled();
    }

    private void checkIfBluetoothEnabled() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            getViewState().requestBluetoothEnable();
        }
    }

    public void handlePermissionResult(boolean result) {
        if (!result) {
            getViewState().requestPermissions();
        }
    }

    public void handleBluetoothEnabledResult(int requestCode, int resultCode) {
        if (requestCode != REQUEST_ENABLE_BT) {
            return;
        }

        if (resultCode != RESULT_OK) {
            getViewState().makeMessage("Для работы приложения требуется включение Bluetooth");
        }
    }

    public void handleFragmentSwitchFAB(boolean isActionButtonToggled) {
        getViewState().checkForPermissions();
        checkIfBluetoothEnabled();

        if (isActionButtonToggled) {
            getViewState().updateFab(false);
            getViewState().showFragment(pageFragmentControl);
            getViewState().switchFabBreathing(lampsDataBaseManager.getList().isEmpty());

        } else {
            getViewState().updateFab(true);
            getViewState().showFragment(pageFragmentConnect);
            getViewState().switchFabBreathing(false);

        }
    }

    public void handleBackPress(boolean isActionButtonToggled) {
        if (isActionButtonToggled) {
            getViewState().updateFab(false);
            getViewState().showFragment(pageFragmentControl);
        }
    }


}
