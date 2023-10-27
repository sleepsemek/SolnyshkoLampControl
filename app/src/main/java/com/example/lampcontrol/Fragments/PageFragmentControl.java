package com.example.lampcontrol.Fragments;

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

import com.example.lampcontrol.Adapters.AddedDevicesAdapter;
import com.example.lampcontrol.Application.LampApplication;
import com.example.lampcontrol.R;
import com.example.lampcontrol.Utils.LampsDataBase;

public class PageFragmentControl extends Fragment {

    private Hint hintText;
    private LampsDataBase dataBase;
    private LampApplication application;

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
        dataBase.setDataBaseListener(list -> hintText.setHintText(list.isEmpty()));

        hintText = new Hint(R.id.textHint);

        RecyclerView devicesList = requireView().findViewById(R.id.devicesList);
        devicesList.setHasFixedSize(true);
        devicesList.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        devicesList.setItemAnimator(new DefaultItemAnimator());
        devicesList.setAdapter(new AddedDevicesAdapter(requireActivity().getApplicationContext(), dataBase));
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