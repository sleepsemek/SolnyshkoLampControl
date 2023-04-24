package com.example.lampcontrol.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.Adapters.AddedDevicesAdapter;
import com.example.lampcontrol.DeviceDataBase;
import com.example.lampcontrol.R;

public class PageFragmentControl extends Fragment {

    private Hint hintText;
    private DeviceDataBase dataBase;

    public PageFragmentControl() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_page_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        hintText = new Hint(R.id.textHint);
        dataBase = new DeviceDataBase(requireActivity().getApplicationContext());
        dataBase.setDataBaseListener(list -> hintText.setHintText(list.isEmpty()));

        RecyclerView devicesList = requireView().findViewById(R.id.devicesList);
        devicesList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        devicesList.setLayoutManager(layoutManager);
        AddedDevicesAdapter addedDevicesAdapter = new AddedDevicesAdapter(requireActivity().getApplicationContext(), dataBase);
        devicesList.setAdapter(addedDevicesAdapter);
    }

    public class Hint {
        private final TextView hintText;
        private Hint(int id) {
            hintText = requireView().findViewById(id);
        }

        public void hideHint() {
            hintText.setVisibility(View.GONE);
        }

        public void showHint() {
            hintText.setVisibility(View.VISIBLE);
        }

        public void setHintText(boolean isEmpty) {
            if (!isEmpty) {
                hideHint();
            } else {
                showHint();
            }
        }
    }
}