package com.example.lampcontrol.Utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.lampcontrol.Models.Lamp;

import java.util.ArrayList;
import java.util.List;

public class LampsDataBase {

    private final SQLiteDatabase lampsDataBase;
    private final ArrayList<Lamp> devicesAddedList = new ArrayList<>();
    private List<DataBaseListener> listeners = new ArrayList<>();

    public LampsDataBase(Context context) {
        lampsDataBase = context.openOrCreateDatabase("lamps.db", MODE_PRIVATE, null);
        lampsDataBase.execSQL("CREATE TABLE IF NOT EXISTS lamps (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT," +
                "address TEXT UNIQUE);");
        updateList();
    }

    public void addLamp(String name, String address) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("address", address);
        lampsDataBase.replace("lamps", null, values);
        updateList();
    }

    private ArrayList<Lamp> updateList() {
        devicesAddedList.clear();
        Cursor cursor = lampsDataBase.rawQuery("SELECT * FROM lamps;", null);
        while (cursor.moveToNext()) {
            Lamp createList = new Lamp();
            String name = cursor.getString(1);
            String address = cursor.getString(2);
            createList.setName(name);
            createList.setAddress(address);
            devicesAddedList.add(createList);
        }
        cursor.close();
        if (!listeners.isEmpty()) {
            triggerEvent();
        }
        return devicesAddedList;
    }

    public void removeLamp(String address) {
        lampsDataBase.delete("lamps", "address='" + address + "'", null);
        updateList();
    }

    public ArrayList<Lamp> getList() {
        return updateList();
    }

    public void addDataBaseListener(DataBaseListener listener) {
        listeners.add(listener);
    }

    public void triggerEvent() {
        for (DataBaseListener listener : listeners) {
            listener.onSetChange(devicesAddedList);
        }
    }

    public interface DataBaseListener {
        void onSetChange(ArrayList<Lamp> list);
    }


}
