package com.solnyshco.lampcontrol.views;

import com.solnyshco.lampcontrol.models.POJO.ReceivedLampState;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ControlLampView extends MvpView {
    void setButtonsState(ReceivedLampState.RelayState state);
    void openTimerBottomSheet(int minutes, int seconds, int iterations);
    void closeTimerBottomSheet();
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "loadingScreen")
    void startLoading(String name);
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "loadingScreen")
    void stopLoading(String name);
    void clearTimerView();
    void drawTimerView(int minutes, int seconds, int iterations, int totalMinutes, int totalSeconds, int totalIterations);
    void drawPreheatView(int minutes, int seconds);
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "lampDisableAlert")
    void showAlertDialog();
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "lampDisableAlert")
    void hideAlertDialog();
    @StateStrategyType(SkipStrategy.class)
    void makeMessage(String msg);
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "infoBottomSheet")
    void showLampInfo(String name, String address, String version);
    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = "infoBottomSheet")
    void hideLampInfo();
}
