package com.example.intelligentdoorlock;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

public class GlobalVarious extends Application {
    private BluetoothSocket globalBlueSocket = null;
    private String open_close = null;
    private String time_set = null;
    private String auto_control = null;
    private String safety_mode = null;
    private String steer_angle = null;
    private String unlock_direction = null;
    private String indicator_light_mode = null;
    private String battery = null;
    private String current_mode = null;

    public void setGlobalBlueSocket(BluetoothSocket globalBlueSocket) {
        this.globalBlueSocket = globalBlueSocket;
    }

    public BluetoothSocket getGlobalBlueSocket() {
        return globalBlueSocket;
    }

    public void setOpen_close(String string) {
        this.open_close = string;
    }

    public String getOpen_close() {
        return open_close;
    }

    public void setTime_set(String string) {
        this.time_set = string;
    }

    public String getTime_set() {
        return time_set;
    }

    public void setAuto_control(String string) {
        this.auto_control = string;
    }

    public String getAuto_control() {
        return auto_control;
    }

    public void setSafety_mode(String string) {
        this.safety_mode = string;
    }

    public String getSafety_mode() {
        return safety_mode;
    }

    public void setSteer_angle(String string) {
        this.steer_angle = string;
    }

    public String getSteer_angle() {
        return steer_angle;
    }

    public void setUnlock_direction(String string) {
        this.unlock_direction = string;
    }

    public String getUnlock_direction() {
        return unlock_direction;
    }

    public void setIndicator_light_mode(String string) {
        this.indicator_light_mode = string;
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
}