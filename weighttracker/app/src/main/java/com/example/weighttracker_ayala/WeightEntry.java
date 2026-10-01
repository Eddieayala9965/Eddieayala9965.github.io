package com.example.weighttracker_ayala;

public class WeightEntry {

    private final long id;
    private final long userId;
    private String date;
    private double weight;
    private String notes;

    public WeightEntry(long id, long userId, String date, double weight, String notes) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.weight = weight;
        this.notes = notes;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getDate() { return date; }
    public double getWeight() { return weight; }
    public String getNotes() { return notes; }

    public void setDate(String date) { this.date = date; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setNotes(String notes) { this.notes = notes; }
}
