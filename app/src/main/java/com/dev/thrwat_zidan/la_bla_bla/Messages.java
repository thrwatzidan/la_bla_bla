package com.dev.thrwat_zidan.la_bla_bla;

public class Messages {
    private String from;
    private String message;
    private String type;
    private String to;
    private String time;
    private String date;
    private String message_id;
    private String name;

    public Messages(String from, String message, String type, String to, String time, String date, String message_id, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.time = time;
        this.date = date;
        this.message_id = message_id;
        this.name = name;
    }

    public Messages() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
