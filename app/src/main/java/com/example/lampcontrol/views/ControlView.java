package com.example.lampcontrol.views;

import com.example.lampcontrol.models.POJO.Lamp;

import java.util.ArrayList;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.AddToEndStrategy;
import moxy.viewstate.strategy.SingleStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndStrategy.class)
public interface ControlView extends MvpView {

    void updateList(boolean isEmpty);
    @StateStrategyType(AddToEndSingleStrategy.class)
    void setDevicesListAdapter(ArrayList<Lamp> lampList);
    void showLampRenameMenu(Lamp lamp, int position);
    @StateStrategyType(SingleStateStrategy.class)
    void showLampDeleteMenu(Lamp lamp, int position);
    void lampRenameConfirmed();
    void lampRenameCancelled();
    void startControlLampActivity(String name, String address);
    void notifyEditLampFromList(int position);
    void notifyAddLampToList(int position);
    void notifyDeleteLampFromList(int position);
    void lampDeleteConfirmed();
    void lampDeleteCancelled();

}
