package com.example.spa_sys.models;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Sales {
    private final IntegerProperty id;
    private final StringProperty customerName;
    private final StringProperty staffName;
    private final StringProperty serviceName;
    private final ObjectProperty<LocalDate> saleDate;
    private final StringProperty paymentMethod;
    private final DoubleProperty totalAmount;

    public Sales(int id, String customerName, String staffName, String serviceName, LocalDate saleDate, String paymentMethod, double totalAmount) {
        this.id = new SimpleIntegerProperty(id);
        this.customerName = new SimpleStringProperty(customerName);
        this.staffName = new SimpleStringProperty(staffName);
        this.serviceName = new SimpleStringProperty(serviceName);
        this.saleDate = new SimpleObjectProperty<>(saleDate);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty staffNameProperty() { return staffName; }
    public StringProperty serviceNameProperty() { return serviceName; }
    public ObjectProperty<LocalDate> saleDateProperty() { return saleDate; }
    public StringProperty paymentMethodProperty() { return paymentMethod; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }

    public int getId() { return id.get(); }
    public String getCustomerName() { return customerName.get(); }
    public String getStaffName() { return staffName.get(); }
    public String getServiceName() { return serviceName.get(); }
    public LocalDate getSaleDate() { return saleDate.get(); }
    public String getPaymentMethod() { return paymentMethod.get(); }
    public double getTotalAmount() { return totalAmount.get(); }
}
