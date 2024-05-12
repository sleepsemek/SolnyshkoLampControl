package com.solnyshco.lampcontrol.ui.views;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.solnyshco.lampcontrol.R;

public class LampInfoBottomSheet extends BottomSheetDialog {

    private TextView name;
    private TextView address;
    private TextView version;

    private AppCompatButton cancelButton;

    public LampInfoBottomSheet(@NonNull Context context) {
        super(context);
        init();
    }

    public LampInfoBottomSheet(@NonNull Context context, int theme) {
        super(context, theme);
        init();
    }

    protected LampInfoBottomSheet(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        View view = getLayoutInflater().inflate(R.layout.lamp_info_bottom_sheet, null);
        this.setContentView(view);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        cancelButton = this.findViewById(R.id.cancel_button);

        name = this.findViewById(R.id.lamp_name);
        address = this.findViewById(R.id.lamp_address);
        version = this.findViewById(R.id.lamp_version);

    }

    public void setInfo(String name, String address, String version) {
        this.name.setText(name);
        this.address.setText(address);
        this.version.setText(version);
    }

    public AppCompatButton getCancelButton() {
        return cancelButton;
    }

}
