package com.example.intelligentdoorlock;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

public class GlobalVarious extends Application {
    private BluetoothSocket globalBlueSocket = null;
    private String open_close = "";
    private String auto_control_open = "";
    private String auto_control_close = "";
    private String safety_mode = "";
    private String steer_angle = "";
    private String unlock_direction = "";
    private String indicator_light_mode = "";
    private String battery = "";
    private String current_mode = "";
    private String latest_modified = "";

    public void setGlobalBlueSocket(BluetoothSocket globalBlueSocket) {
        this.globalBlueSocket = globalBlueSocket;
    }

    public BluetoothSocket getGlobalBlueSocket() {
        return globalBlueSocket;
    }

    public void setOpen_close(String string) {
        this.open_close = string;
        latest_modified = "open_close";
    }

    public String getOpen_close() {
        return open_close;
    }

    public void setAuto_control_open(String string) {
        this.auto_control_open = string;
        latest_modified = "auto_control_open";
    }

    public String getAuto_control_open() {
        return auto_control_open;
    }

    public void setAuto_control_close(String string) {
        this.auto_control_close = string;
        latest_modified = "auto_control_close";
    }

    public String getAuto_control_close() {
        return auto_control_close;
    }

    public void setSafety_mode(String string) {
        this.safety_mode = string;
        latest_modified = "safety_mode";
    }

    public String getSafety_mode() {
        return safety_mode;
    }

    public void setSteer_angle(String string) {
        this.steer_angle = string;
        latest_modified = "steer_angle";
    }

    public String getSteer_angle() {
        return steer_angle;
    }

    public void setUnlock_direction(String string) {
        this.unlock_direction = string;
        latest_modified = "unlock_direction";
    }

    public String getUnlock_direction() {
        return unlock_direction;
    }

    public void setIndicator_light_mode(String string) {
        this.indicator_light_mode = string;
        latest_modified = "indicator_light_mode";
    }

    public String getIndicator_light_mode() {
        return indicator_light_mode;
    }

    public void setBattery(String string) {
        this.battery = string;
    }

    public String getBattery() {
        return battery;
    }

    public void setCurrent_mode(String string) {
        this.current_mode = string;
    }

    public String getCurrent_mode() {
        return current_mode;
    }

    public void setLatest_modified(String string) {
        this.latest_modified = string;
    }

    public String getLatest_modified() {
        return latest_modified;
    }
}