package com.example.lampcontrol.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lampcontrol.LampsDataBase;
import com.example.lampcontrol.R;

public class EditLampActivity extends AppCompatActivity {

    private String address;
    private String name;
    private String addedName;

    private EditText deviceName;
    private Button button;

    private LampsDataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_lamp);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");

        button = findViewById(R.id.add);
        deviceName = findViewById(R.id.NewName);
        deviceName.setText(name);

        dataBase = new LampsDataBase(getApplicationContext());

        button.setOnClickListener(view -> {
            addedName = String.valueOf(deviceName.getText());
            buildPkg();
        });
    }

    private void buildPkg() {
        dataBase.addLamp(addedName, address);
        back();
    }

    private void back() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }
}