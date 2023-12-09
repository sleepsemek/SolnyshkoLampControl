package com.example.lampcontrol.views;

import com.example.lampcontrol.models.POJO.Lamp;

import java.util.ArrayList;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.AddToEndStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SingleStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndStrategy.class)
public interface ControlView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void updateList(boolean isEmpty);
    @StateStrategyType(AddToEndSingleStrategy.class)
    void setDevicesListAdapter(ArrayList<Lamp> lampList);
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "renameDialog")
    void showLampRenameMenu(Lamp lamp, int position);
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "deleteDialog")
    void showLampDeleteMenu(Lamp lamp, int position);
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "renameDialog")
    void lampRenameConfirmed();
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "renameDialog")
    void lampRenameCancelled();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void startControlLampActivity(String name, String address);
    void notifyEditLampFromList(int position);
    void notifyAddLampToList(int position);
    void notifyDeleteLampFromList(int position);
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "deleteDialog")
    void lampDeleteConfirmed();
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "deleteDialog")
    void lampDeleteCancelled();

}
