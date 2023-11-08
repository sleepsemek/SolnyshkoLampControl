package com.example.lampcontrol.Fragments;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lampcontrol.activities.ControlLampActivity;
import com.example.lampcontrol.adapters.ControlAdapter;
import com.example.lampcontrol.models.POJO.Lamp;
import com.example.lampcontrol.R;
import com.example.lampcontrol.presenters.ControlPresenter;
import com.example.lampcontrol.ui.views.EditLampBottomSheet;
import com.example.lampcontrol.views.ControlView;
import com.example.lampcontrol.views.MainView;

import java.util.ArrayList;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;

public class PageFragmentControl extends MvpAppCompatFragment implements ControlView {

    @InjectPresenter
    public ControlPresenter controlPresenter;


    private ControlAdapter adapter;
    private RecyclerView devicesList;
    private EditLampBottomSheet editLampBottomSheet;
    private AlertDialog alertDialog;

    private TextView hintText;

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
    public void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.cancel();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        editLampBottomSheet = new EditLampBottomSheet(requireContext());

        devicesList = requireView().findViewById(R.id.devicesList);
        devicesList.setHasFixedSize(true);
        devicesList.setLayoutManager(new LinearLayoutManager(requireActivity().getApplicationContext()));
        devicesList.setItemAnimator(new DefaultItemAnimator());

        hintText = requireView().findViewById(R.id.textHint);
    }

    @Override
    public void setDevicesListAdapter(ArrayList<Lamp> lampList) {
        adapter = new ControlAdapter(lampList, new ControlAdapter.OnButtonClickListener() {

            @Override
            public void onDeleteClicked(Lamp lamp, int position) {
                controlPresenter.handleDeleteButtonClick(lamp, position);
            }

            @Override
            public void onRenameClicked(Lamp lamp, int position) {
                controlPresenter.handleRenameButtonClick(lamp, position);
            }

            @Override
            public void onInstanceClicked(Lamp lamp, int position) {
                controlPresenter.startLampControlActivity(lamp);
            }

        });
        adapter.setHasStableIds(true);
        devicesList.setAdapter(adapter);
    }

    @Override
    public void showLampRenameMenu(Lamp lamp, int position) {
        editLampBottomSheet.setName(lamp.getName());
        editLampBottomSheet.getConfirmButton().setOnClickListener(view -> {
            controlPresenter.handleConfirmRenameLampButtonClick(editLampBottomSheet.getName(), lamp, position);

        });
        editLampBottomSheet.getCancelButton().setOnClickListener(view -> {
            controlPresenter.handleCancelRenameLampButtonClick(editLampBottomSheet.getName(), lamp, position);
        });
        editLampBottomSheet.show();
    }

    @Override
    public void updateList(boolean isEmpty) {
        if (isEmpty) {
            if (getActivity() instanceof MainView) {
                ((MainView) getActivity()).switchFabBreathing(true);
            }
            hintText.setVisibility(View.VISIBLE);
        } else {
            hintText.setVisibility(View.GONE);
        }
    }

    @Override
    public void showLampDeleteMenu(Lamp lamp, int position) {
        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(Html.fromHtml(getString(R.string.delete_dialog_title)))
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(Html.fromHtml(getString(R.string.delete_dialog_confirm_button)), (dialog, which) -> {
                    controlPresenter.handleConfirmDeleteButtonClick(lamp, position);
                })
                .setNegativeButton(R.string.delete_dialog_cancel_button, (dialog, which) -> {
                    controlPresenter.handleCancelDeleteButtonClick(lamp, position);
                })
                .create();

        alertDialog.show();
    }

    @Override
    public void lampRenameConfirmed() {
        editLampBottomSheet.cancel();
    }

    @Override
    public void lampRenameCancelled() {
        editLampBottomSheet.cancel();
    }

    @Override
    public void startControlLampActivity(String name, String address) {
        Intent intent = new Intent(getContext(), ControlLampActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("address", address);
        intent.putExtra("name", name);
        startActivity(intent);
    }

    public void notifyEditLampFromList(int position) {
        adapter.editLamp(position);
    }

    public void notifyAddLampToList(int position) {
        adapter.addLamp(position);
    }

    public void notifyDeleteLampFromList(int position) {
        adapter.deleteLamp(position);
        System.out.println("delete");
    }

    @Override
    public void lampDeleteConfirmed() {
        alertDialog.cancel();
    }

    @Override
    public void lampDeleteCancelled() {
        alertDialog.cancel();
    }

}