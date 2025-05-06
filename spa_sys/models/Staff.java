package com.example.spa_sys.models;

public class Staff {
    private int    id;
    private String fullName;
    private String phone;
    private String email;
    private String gender;      // "Male", "Female", "Other"
    private String status;      // "Available", "On-Leave", "Unavailable"
    private String colourCode;   // e.g. "#FFAA33"

    // Full constructor
    public Staff(int id, String fullName, String phone, String email,
                 String gender, String status, String colourCode) {
        this.id        = id;
        this.fullName  = fullName;
        this.phone     = phone;
        this.email     = email;
        this.gender    = gender;
        this.status    = status;
        this.colourCode = colourCode;
    }

    // Constructor for new (no id yet)
    public Staff(String fullName, String phone, String email, String gender, String status, String colourCode) {
        this(0, fullName, phone, email, gender, status, colourCode);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getColourCode() {
        return colourCode;
    }

    public void setColourCode(String colourCode) {
        this.colourCode = colourCode;
    }
}

