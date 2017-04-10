package com.scheidegger.jegplayer.model;

import java.util.Comparator;

public class JegMusic implements Comparator<JegMusic>, Comparable<JegMusic>{

    private int id;
    private String name;
    private String fileName;
    private String country;
    private int length;
    private String description;
    private int resId;

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

    public int getResId() { return resId; }

    public void setResId(int resId) { this.resId = resId; }

    @Override
    public int compare(JegMusic o1, JegMusic o2) {
        return o1.getFileName().compareTo(o2.getFileName());
    }

    @Override
    public int compareTo(JegMusic o) {
        return this.getFileName().compareTo(o.getFileName());
    }
}
