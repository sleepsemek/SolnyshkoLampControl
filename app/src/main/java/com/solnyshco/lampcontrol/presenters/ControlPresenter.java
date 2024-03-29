package com.solnyshco.lampcontrol.presenters;

import com.solnyshco.lampcontrol.LampApplication;
import com.solnyshco.lampcontrol.models.POJO.Lamp;
import com.solnyshco.lampcontrol.repository.LampsDataBaseManager;
import com.solnyshco.lampcontrol.views.ControlView;

import java.util.ArrayList;

import moxy.MvpPresenter;

public class ControlPresenter extends MvpPresenter<ControlView> {

    private LampsDataBaseManager lampsDataBaseManager;

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        LampApplication application = LampApplication.getInstance();
        lampsDataBaseManager = application.getDatabaseManager();
        getViewState().updateList(lampsDataBaseManager.getList().isEmpty());
        lampsDataBaseManager.setDataBaseListener(new LampsDataBaseManager.DataBaseListener() {
            @Override
            public void onSetChange(ArrayList<Lamp> list) {
                getViewState().updateList(list.isEmpty());
            }

            @Override
            public void onLampAdded(int position) {
                getViewState().notifyAddLampToList(position);
            }
        });
        getViewState().setDevicesListAdapter(lampsDataBaseManager.getList());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startLampControlActivity(Lamp lamp) {
        getViewState().startControlLampActivity(lamp.getName(), lamp.getAddress());
    }

    public void handleRenameButtonClick(Lamp lamp, int position) {
        getViewState().showLampRenameMenu(lamp, position);
    }

    public void handleDeleteButtonClick(Lamp lamp, int position) {
        getViewState().showLampDeleteMenu(lamp, position);
    }

    public void handleConfirmDeleteButtonClick(Lamp lamp, int position) {
        lampsDataBaseManager.deleteLamp(lamp);
        getViewState().lampDeleteConfirmed();
        getViewState().notifyDeleteLampFromList(position);
    }

    public void handleCancelDeleteButtonClick(Lamp lamp, int position) {
        getViewState().lampDeleteCancelled();
    }

    public void handleConfirmRenameLampButtonClick(String newName, Lamp lamp, int position) {
        if (newName.length() >= 35) {
            getViewState().makeMessage("Название не должно превышать 35 символов");
            return;
        }
        lampsDataBaseManager.renameLamp(newName, lamp);
        getViewState().lampRenameConfirmed();
        getViewState().notifyEditLampFromList(position);
    }

    public void handleCancelRenameLampButtonClick(String newName, Lamp lamp, int position) {
        getViewState().lampRenameCancelled();
    }
}
