package com.example.sit;

public class IPAddressSingleton {
    private static IPAddressSingleton instance;
    private String ipAddress;

    private IPAddressSingleton() {}

    public static synchronized IPAddressSingleton getInstance() {
        if (instance == null) {
            instance = new IPAddressSingleton();
        }
        return instance;
    }

    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIPAddress() {
        return ipAddress;
    }
}

