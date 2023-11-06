package com.example.lampcontrol.Views;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.example.lampcontrol.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class EditLampBottomSheet extends BottomSheetDialog {

    private EditText name;
    private AppCompatButton confirmButton;
    private AppCompatButton cancelButton;

    public EditLampBottomSheet(@NonNull Context context) {
        super(context);
        init();
    }

    public EditLampBottomSheet(@NonNull Context context, int theme) {
        super(context, theme);
        init();
    }

    protected EditLampBottomSheet(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        View view = getLayoutInflater().inflate(R.layout.edit_lamp_bottom_sheet, null);
        this.setContentView(view);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        name = this.findViewById(R.id.lamp_name);
        cancelButton = this.findViewById(R.id.cancel_rename_button);
        confirmButton = this.findViewById(R.id.confirm_rename_button);

        cancelButton.setOnClickListener(v -> {
            this.cancel();
        });
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public String getName() {
        return this.name.getText().toString();
    }

    public AppCompatButton getConfirmButton() {
        return confirmButton;
    }

    public AppCompatButton getCancelButton() {
        return cancelButton;
    }
}
