package com.example.lampcontrol.views;

import androidx.fragment.app.Fragment;

import com.example.lampcontrol.models.POJO.Lamp;

import java.util.ArrayList;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SingleStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndStrategy.class)
public interface MainView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void switchFabBreathing(boolean breathing);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void showFragment(Fragment fragment);
    void updateFab(boolean isToggled);
    void requestBluetoothEnable();
    void makeMessage(String msg);
    void checkForPermissions();
    void requestPermissions();
}
