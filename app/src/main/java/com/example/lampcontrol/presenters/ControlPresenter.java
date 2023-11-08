package com.example.lampcontrol.presenters;

import com.example.lampcontrol.LampApplication;
import com.example.lampcontrol.models.LampsDataBaseManager;
import com.example.lampcontrol.models.POJO.Lamp;
import com.example.lampcontrol.views.ControlView;

import java.util.ArrayList;

import moxy.InjectViewState;
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
        lampsDataBaseManager.renameLamp(newName, lamp);
        getViewState().lampRenameConfirmed();
        getViewState().notifyEditLampFromList(position);
    }

    public void handleCancelRenameLampButtonClick(String newName, Lamp lamp, int position) {
        getViewState().lampRenameCancelled();
    }
}
