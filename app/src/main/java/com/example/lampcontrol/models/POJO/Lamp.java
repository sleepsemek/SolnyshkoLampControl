package com.example.lampcontrol.models.POJO;

public class Lamp {

    private String name;
    private String address;

    public Lamp(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public void setName(String android_name) {
        this.name = android_name;
    }
    public void setAddress(String android_address) {
        this.address = android_address;
    }

}