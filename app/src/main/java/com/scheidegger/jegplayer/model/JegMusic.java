package com.scheidegger.jegplayer.model;

public class JegMusic {

    private int id;
    private String name;
    private String fileName;
    private String country;
    private int length;
    private String description;

    public JegMusic(int id, String name, String fileName, String country, int length, String description) {
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.country = country;
        this.length = length;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
