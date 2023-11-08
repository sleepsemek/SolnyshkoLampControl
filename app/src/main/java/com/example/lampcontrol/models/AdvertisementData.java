package com.example.lampcontrol.models;

public class AdvertisementData {
    private String id;
    private String data;

    public AdvertisementData(String id, String data) {
        this.id = id;
        this.data = data;
    }

    public int getAdvertisementId() {
        byte[] bytes = id.getBytes();
        int result = 0;
        for (byte b : bytes) {
            result = (result << 8) | (b & 0xFF);
        }
        return result;
    }

    public byte[] getByteArrayAdvertisementData() {
        StringBuilder hexString = new StringBuilder();
        for (char character : data.toCharArray()) {
            hexString.append(String.format("%02X", (int) character));
        }
        String hex = hexString.toString();
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Нечетная длина строки");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

}
