package com.example.lampcontrol.views;

import com.example.lampcontrol.models.POJO.ReceivedLampState;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ControlLampView extends MvpView {
    void setButtonsState(ReceivedLampState.RelayState state);
    void openTimerBottomSheet(int minutes, int seconds, int iterations);
    void closeTimerBottomSheet();
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "loadingScreen")
    void startLoading();
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "loadingScreen")
    void stopLoading(String name);
    void clearTimerView();
    void drawTimerView(int minutes, int seconds, int iterations, int totalMinutes, int totalSeconds, int totalIterations);
    void drawPreheatView(int minutes, int seconds);
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "lampDisableAlert")
    void showAlertDialog();
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "lampDisableAlert")
    void hideAlertDialog();
}
