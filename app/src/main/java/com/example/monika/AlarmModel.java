package com.example.monika;

public class AlarmModel {
    private int id;
    private String time;
    private String label;
    private boolean isActive;

    // Constructor untuk alarm baru
    public AlarmModel(String time, String label) {
        this.time = time;
        this.label = label;
        this.isActive = true;
    }

    // Constructor lengkap dengan id
    public AlarmModel(int id, String time, String label, boolean isActive) {
        this.id = id;
        this.time = time;
        this.label = label;
        this.isActive = isActive;
    }

    // Getter dan Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
}