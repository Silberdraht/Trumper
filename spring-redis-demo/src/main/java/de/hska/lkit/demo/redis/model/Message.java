package de.hska.lkit.demo.redis.model;

import java.io.Serializable;

public class Message implements Serializable {

    String id;
    String timestamp;
    String autor;
    String text;


    public Message() {}

    public Message(String id, String autor, String timestamp, String text) {
        setId(id);
        setAutor(autor);
        setTimestamp(timestamp);
        setText(text);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
