package com.example.monika;

public class DetailEntry {
    public String label;
    public float value;

    // Konstruktor kosong wajib ada untuk Firebase
    public DetailEntry() {}

    public DetailEntry(String label, float value) {
        this.label = label;
        this.value = value;
    }
}
