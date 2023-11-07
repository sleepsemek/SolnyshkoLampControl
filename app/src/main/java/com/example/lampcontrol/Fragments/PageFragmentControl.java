package com.example.lampcontrol.Fragments;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.Activities.ControlLampActivity;
import com.example.lampcontrol.Adapters.AddedDevicesAdapter;
import com.example.lampcontrol.Application.LampApplication;
import com.example.lampcontrol.R;
import com.example.lampcontrol.Utils.LampsDataBase;
import com.example.lampcontrol.views.EditLampBottomSheet;

public class PageFragmentControl extends Fragment {

    private Hint hintText;
    private LampsDataBase dataBase;
    private LampApplication application;
    private AddedDevicesAdapter adapter;
    private EditLampBottomSheet editLampBottomSheet;

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
        application = (LampApplication) requireActivity().getApplication();
        dataBase = application.getLampsDataBase();
        dataBase.addDataBaseListener(list -> hintText.setHintText(list.isEmpty()));

        hintText = new Hint(R.id.textHint);
        editLampBottomSheet = new EditLampBottomSheet(requireContext());

        RecyclerView devicesList = requireView().findViewById(R.id.devicesList);
        devicesList.setHasFixedSize(true);
        devicesList.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        devicesList.setItemAnimator(new DefaultItemAnimator());
        adapter = new AddedDevicesAdapter(this, dataBase.getList());
        devicesList.setAdapter(adapter);
    }

    public void startControlActivity(int index) {
        Intent intent = new Intent(requireActivity(), ControlLampActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("address", dataBase.getLamp(index).getAddress());
        intent.putExtra("name", dataBase.getLamp(index).getName());
        startActivity(intent);
    }

    public void deleteLamp(String address) {
        dataBase.deleteLamp(address);
    }

    public void renameLamp(int index) {
        editLampBottomSheet.setName(dataBase.getLamp(index).getName());
        editLampBottomSheet.getConfirmButton().setOnClickListener(view -> {
            dataBase.addLamp(editLampBottomSheet.getName(), dataBase.getLamp(index).getAddress());
            adapter.notifyItemChanged(index);
            editLampBottomSheet.cancel();
        });
        editLampBottomSheet.show();
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