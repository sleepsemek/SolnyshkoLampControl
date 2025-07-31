package com.solnyshco.lampcontrol.presenters;

import static com.solnyshco.lampcontrol.models.POJO.ReceivedLampState.RelayState.OFF;
import static com.solnyshco.lampcontrol.models.POJO.ReceivedLampState.RelayState.ON;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import com.solnyshco.lampcontrol.LampApplication;
import com.solnyshco.lampcontrol.models.POJO.ReceivedLampState;
import com.solnyshco.lampcontrol.models.POJO.SentCommand;
import com.solnyshco.lampcontrol.models.PreferencesManager;
import com.solnyshco.lampcontrol.repository.BluetoothLeConnectionThread;
import com.solnyshco.lampcontrol.views.ControlLampView;

import moxy.MvpPresenter;

public class ControlLampPresenter extends MvpPresenter<ControlLampView> {

    private BluetoothLeConnectionThread connectedThread;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final String address;
    private final String name;
    private CountDownTimer preheatTimer;
    private CountDownTimer mainTimer;
    private PreferencesManager preferencesManager;

    public ControlLampPresenter(String address, String name) {
        this.address = address;
        this.name = name;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        preferencesManager = LampApplication.getInstance().getPreferencesManager();
        connectedThread = LampApplication.getInstance().getBluetoothConnectionThread();
        getViewState().startLoading(name);
        connectedThread.startConnection(address);
        connectedThread.setOnDataReceivedListener(new BluetoothLeConnectionThread.onDataReceivedListener() {
            @Override
            public void onStateChange(boolean state) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (state) {
                            getViewState().stopLoading(name);
                        } else {
                            if (preheatTimer != null) preheatTimer.cancel();
                            if (mainTimer != null) mainTimer.cancel();
                            getViewState().startLoading(name);
                        }
                    }
                });

            }

            @Override
            public void onCommandReceived(ReceivedLampState lampState) {
                getViewState().setButtonsState(lampState.getState());
                switch (lampState.getState()) {
                    case OFF:
                    case ON:
                        stopPreheat();
                        stopTimer();
                        break;

                    case ACTIVE:
                        startTimer(lampState.getTimer());
                        break;

                    case PAUSED:
                        pauseTimer(lampState.getTimer());
                        break;

                    case PREHEATING:
                        startPreheat(lampState.getPreheat());
                        break;

                    default:
                        getViewState().showLampInfo(name, address, lampState.getVersion());

                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connectedThread.cancel();

        handler.post(() -> {
            if (preheatTimer != null) {
                preheatTimer.cancel();
                preheatTimer = null;
            }
            if (mainTimer != null) {
                mainTimer.cancel();
                mainTimer = null;
            }
        });
    }

    private void startPreheat(ReceivedLampState.Preheat preheatData) {
        handler.post(() -> {
            if (preheatTimer != null) {
                preheatTimer.cancel();
                preheatTimer = null;
            }

            preheatTimer = new CountDownTimer(preheatData.getTimeLeft(), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int totalSeconds = (int) (millisUntilFinished / 1000);
                    int minutes = totalSeconds / 60;
                    int seconds = totalSeconds % 60;
                    getViewState().drawPreheatView(minutes, seconds);
                }

                @Override
                public void onFinish() {

                }
            }.start();
        });
    }

    private void stopPreheat() {
        handler.post(() -> {
            if (preheatTimer != null) {
                preheatTimer.cancel();
                preheatTimer = null;
            }
            getViewState().clearTimerView();
        });
    }

    private void startTimer(ReceivedLampState.Timer timerData) {
        handler.post(() -> {
            if (preheatTimer != null) {
                preheatTimer.cancel();
                preheatTimer = null;
            }
            if (mainTimer != null) {
                mainTimer.cancel();
                mainTimer = null;
            }

            int generalCycles = timerData.getGeneralCycles();
            int generalCycleTime = timerData.getGeneralCycleTime();
            int remainedTime = timerData.getTimeLeft();
            int generalTime = generalCycles * generalCycleTime;
            int remainedCycleTime = remainedTime % generalCycleTime;
            int remainedCycles = remainedTime / generalCycleTime;

            mainTimer = new CountDownTimer(remainedCycleTime, 1000) {
                @Override
                public void onTick(long l) {

                    int total = (int) Math.ceil((double) l / 1000);
                    int minutes = total / 60;
                    int seconds = total % 60;
                    getViewState().drawTimerView(minutes, seconds, generalCycles - remainedCycles, generalCycleTime / 60000, (generalCycleTime % 60000) / 1000, generalCycles);
                }

                @Override
                public void onFinish() {

                }

            }.start();
        });
    }

    public void stopTimer() {
        handler.post(() -> {
            if (mainTimer != null) {
                mainTimer.cancel();
                mainTimer = null;
            }
            getViewState().clearTimerView();
        });
    }

    private void pauseTimer(ReceivedLampState.Timer timerData) {
        handler.post(() -> {
            if (preheatTimer != null) {
                preheatTimer.cancel();
                preheatTimer = null;
            }
            if (mainTimer != null) {
                mainTimer.cancel();
                mainTimer = null;
            }

            int generalCycles = timerData.getGeneralCycles();
            int generalCycleTime = timerData.getGeneralCycleTime();
            int remainedTime = timerData.getTimeLeft();
            int generalTime = generalCycles * generalCycleTime;
            int remainedCycleTime = remainedTime % generalCycleTime;
            int remainedCycles = remainedTime / generalCycleTime;

            int total = (int) Math.ceil((double) remainedCycleTime / 1000);
            System.out.println(total);
            int minutes = total / 60;
            int seconds = total % 60;

            getViewState().drawTimerView(minutes, seconds, generalCycles - remainedCycles, generalCycleTime / 60000, (generalCycleTime % 60000) / 1000, generalCycles);
        });
    }

    public void handleButtonClick(ReceivedLampState.RelayState relayState) {
        switch (relayState) {
            case OFF:
            case ON:
                getViewState().openTimerBottomSheet(preferencesManager.getMinutes(address), preferencesManager.getSeconds(address), preferencesManager.getIterations(address));
                break;
            case ACTIVE:
                connectedThread.sendCommand(new SentCommand("pause"));
                break;
            case PAUSED:
                connectedThread.sendCommand(new SentCommand("resume"));
                break;
            case PREHEATING:
                break;

        }
    }

    public void handleSideButtonClick(ReceivedLampState.RelayState relayState) {
        switch (relayState) {
            case ON:
                connectedThread.sendCommand(new SentCommand(OFF.ordinal()));
                break;
            case OFF:
                connectedThread.sendCommand(new SentCommand(ON.ordinal()));
                break;
            case ACTIVE:
            case PAUSED:
                connectedThread.sendCommand(new SentCommand("stop"));
                break;
            case PREHEATING:
                getViewState().showAlertDialog();
                break;
        }
    }

    public void handleBottomSheetStart(int minutes, int seconds, int cycles) {
        connectedThread.sendCommand(new SentCommand("set", (minutes * 60L + seconds) * 1000, cycles));
        preferencesManager.setLastTime(minutes, seconds, cycles, address);
        getViewState().closeTimerBottomSheet();
    }

    public void handleBottomSheetInfo() {
        connectedThread.getVersion();
    }

    public void handleAlertConfirm() {
        connectedThread.sendCommand(new SentCommand(OFF.ordinal()));
        getViewState().hideAlertDialog();
    }

    public void handleAlertCancel() {
        getViewState().hideAlertDialog();
    }

    public void handleInfoBottomSheetCancelButton() {
        getViewState().hideLampInfo();
    }
}
