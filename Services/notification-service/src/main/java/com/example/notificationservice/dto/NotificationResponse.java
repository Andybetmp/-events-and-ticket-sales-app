package com.example.notificationservice.dto;

import java.time.Instant;

public class NotificationResponse {
    private String notificationId;
    private String status;
    private Instant timestamp;
    private String message;

    public NotificationResponse() {
    }

    public NotificationResponse(String notificationId, String status, Instant timestamp, String message) {
        this.notificationId = notificationId;
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
