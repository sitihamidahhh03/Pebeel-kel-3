package com.example.monika;

public class NotificationModel {
    private String title;
    private String message;
    private String time;
    private String status;

    public NotificationModel(String title, String message, String time, String status) {
        this.title = title;
        this.message = message;
        this.time = time;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
}
