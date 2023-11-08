package com.example.lampcontrol.repository;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.lampcontrol.models.POJO.Lamp;

import java.util.ArrayList;

public class LampsDataBaseManager {

    private final SQLiteDatabase lampsDataBase;
    private final ArrayList<Lamp> devicesAddedList = new ArrayList<>();
    private DataBaseListener listener;

    public LampsDataBaseManager(Context context) {
        lampsDataBase = context.openOrCreateDatabase("lamps.db", MODE_PRIVATE, null);
        lampsDataBase.execSQL(
                "CREATE TABLE IF NOT EXISTS lamps (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT," +
                "address TEXT UNIQUE);"
        );
        updateList();
    }

    public void addLamp(Lamp lamp) {
        ContentValues values = new ContentValues();
        values.put("name", lamp.getName());
        values.put("address", lamp.getAddress());
        lampsDataBase.replace("lamps", null, values);
        devicesAddedList.add(lamp);
        if (listener != null) {
            listener.onLampAdded(devicesAddedList.size() - 1);
            listener.onSetChange(devicesAddedList);
        }
    }

    public void deleteLamp(Lamp lamp) {
        lampsDataBase.delete("lamps", "address='" + lamp.getAddress() + "'", null);
        devicesAddedList.remove(lamp);
        if (listener != null) {
            listener.onSetChange(devicesAddedList);
        }
    }

    public void renameLamp(String newName, Lamp lamp) {
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("address", lamp.getAddress());
        lampsDataBase.replace("lamps", null, values);
        lamp.setName(newName);
        if (listener != null) {
            listener.onSetChange(devicesAddedList);
        }
//        devicesAddedList.set(devicesAddedList.indexOf(lamp), lamp);
    }

    private void updateList() {
        devicesAddedList.clear();
        Cursor cursor = lampsDataBase.rawQuery("SELECT * FROM lamps;", null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(1);
            String address = cursor.getString(2);
            Lamp createList = new Lamp(name, address);
            devicesAddedList.add(createList);
        }
        cursor.close();
        if (listener != null) {
            listener.onSetChange(devicesAddedList);
        }

    }

    public ArrayList<Lamp> getList() {
        return devicesAddedList;
    }

    public void setDataBaseListener(DataBaseListener listener) {
        this.listener = listener;
    }

    public interface DataBaseListener {
        void onSetChange(ArrayList<Lamp> list);
        void onLampAdded(int position);
    }


}
