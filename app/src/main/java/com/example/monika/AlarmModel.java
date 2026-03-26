// AlarmModel.java
package com.example.monika;

public class AlarmModel {
    private int id;
    private int hour;
    private int minute;
    private String label;
    private boolean isActive;

    public AlarmModel(int id, int hour, int minute, String label, boolean isActive) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.label = label;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    public String getTime() {
        return String.format("%02d:%02d", hour, minute);
    }
}
