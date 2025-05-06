package com.example.spa_sys.models;

import java.time.LocalDateTime;

public class Appointment {
    private int id;
    private int userId;
    private int serviceId;
    private int staffId;
    private String remarks;
    private LocalDateTime appointmentDate;
    private String status;
    private int paymentMethodId;

    private String staffColour;
    private String customerName;
    private String serviceName;
    private String staffName;
    private String paymentMethodName;

    // Add these to your full constructor!
    public Appointment(
            int id, int userId, int serviceId, int staffId, String remarks,
            LocalDateTime appointmentDate, String status, int paymentMethodId,
            String serviceName, String staffName, String paymentMethodName,
            String staffColour, String customerName
    ) {
        this.id = id;
        this.userId = userId;
        this.serviceId = serviceId;
        this.staffId = staffId;
        this.remarks = remarks;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.paymentMethodId = paymentMethodId;
        this.serviceName = serviceName;
        this.staffName = staffName;
        this.paymentMethodName = paymentMethodName;
        this.staffColour = staffColour;
        this.customerName = customerName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getServiceId() {
        return serviceId;
    }
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }
    public int getStaffId() {
        return staffId;
    }
    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }
    public String getRemarks() {
        return remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }
    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getPaymentMethodId() {
        return paymentMethodId;
    }
    public void setPaymentMethodId(int paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public String getStaffName() {
        return staffName;
    }
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
    public String getPaymentMethodName() {
        return paymentMethodName;
    }
    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public String getStaffColour() { return staffColour; }
    public void setStaffColour(String staffColor) { this.staffColour = staffColour; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

}
