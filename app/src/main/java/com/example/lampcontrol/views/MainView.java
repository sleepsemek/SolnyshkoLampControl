package com.example.lampcontrol.views;

import androidx.fragment.app.Fragment;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndStrategy.class)
public interface MainView extends MvpView {

    void showFragment(Fragment fragment);
    void showEmptyListHint(boolean isEmpty);
    void requestBluetoothEnable();
    void makeMessage(String msg);
}
