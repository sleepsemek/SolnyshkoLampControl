package com.example.lampcontrol.Fragments;

import android.animation.ValueAnimator;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.adapters.ConnectAdapter;
import com.example.lampcontrol.R;
import com.example.lampcontrol.presenters.ConnectPresenter;
import com.example.lampcontrol.views.ConnectView;
import com.example.lampcontrol.views.MainView;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;

public class PageFragmentConnect extends MvpAppCompatFragment implements ConnectView {

    @InjectPresenter
    ConnectPresenter connectPresenter;

    private RecyclerView recyclerView;
    ConnectAdapter connectAdapter;

    private AppCompatButton refreshButton;
    private ValueAnimator animator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_page_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        recyclerView = requireView().findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        refreshButton = requireView().findViewById(R.id.refresh);

        refreshButton.setOnClickListener(view1 -> {
            connectPresenter.handleScanButton();
        });

    }

    private void startRefreshAnimation() {
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(500);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float rotationDegrees = (float) animation.getAnimatedValue();
                refreshButton.setRotation(rotationDegrees);
            }
        });

        animator.start();
    }


    @Override
    public void setScannedDevicesListAdapter() {
        connectAdapter = new ConnectAdapter(new ConnectAdapter.OnButtonClickListener() {
            @Override
            public void onAddClicked(BluetoothDevice device) {
                connectPresenter.handleAddButtonClick(device);
            }
        });

        recyclerView.setAdapter(connectAdapter);
    }

    @Override
    public void updateScanningState(boolean isScanning) {
        if (isScanning) {
            startRefreshAnimation();
        } else {
            animator.cancel();
            refreshButton.setRotation(0);
        }
    }

    @Override
    public void updateScanningDeviceList(BluetoothDevice device) {
        connectAdapter.addDevice(device);
    }

    @Override
    public void removeAddedDeviceFromScanningList(BluetoothDevice device) {
        connectAdapter.removeDevice(device);
    }

    @Override
    public void openFragment(Fragment fragment) {
        PageFragmentControl pageFragmentControl = new PageFragmentControl();
        if (getActivity() instanceof MainView) {
            ((MainView) getActivity()).showFragment(fragment);
            ((MainView) getActivity()).updateFab(false);
        }
    }

}