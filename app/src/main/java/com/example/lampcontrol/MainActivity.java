package com.example.lampcontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createTabs();
    }

    private void createTabs() {
        tabLayout = findViewById(R.id.tabLayout);

        TabLayout.Tab control = tabLayout.newTab();
        control.setText("Управление");
        tabLayout.addTab(control, 0, true);

        TabLayout.Tab connect = tabLayout.newTab();
        connect.setText("Подключить");
        tabLayout.addTab(connect, 1, true);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        fragmentManager = getSupportFragmentManager();
        setTab(0);
    }

    private void setTab(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
            default:
                fragment = new PageFragmentControl();
                break;
            case 1:
                fragment = new PageFragmentConnect();
                break;
        }
        beginTransaction(fragment);
        Objects.requireNonNull(tabLayout.getTabAt(position)).select();
    }

    private void beginTransaction(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

}