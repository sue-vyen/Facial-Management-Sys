package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.Sales;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SalesController {

    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private Button searchButton;
    @FXML private TableView<Sales> salesTable;
    @FXML private ComboBox<String> staffComboBox;
    @FXML private ComboBox<String> serviceComboBox;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TableColumn<Sales, Integer> idColumn;
    @FXML private TableColumn<Sales, String> customerColumn;
    @FXML private TableColumn<Sales, String> staffColumn;    // Added Staff
    @FXML private TableColumn<Sales, String> serviceColumn;  // Added Service
    @FXML private TableColumn<Sales, LocalDate> dateColumn;
    @FXML private TableColumn<Sales, String> paymentMethodColumn;
    @FXML private TableColumn<Sales, Double> totalColumn;
    @FXML private Label totalRevenueLabel, completedAppointmentsLabel;

    private ObservableList<Sales> SalesList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
//        Create the table columns
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        customerColumn.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        staffColumn.setCellValueFactory(cellData -> cellData.getValue().staffNameProperty());   // Added
        serviceColumn.setCellValueFactory(cellData -> cellData.getValue().serviceNameProperty()); // Added
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().saleDateProperty());
        paymentMethodColumn.setCellValueFactory(cellData -> cellData.getValue().paymentMethodProperty());
        totalColumn.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());

//        bind the observable list to the sales table
        salesTable.setItems(SalesList);
        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

//        Refreshes when search is clicked
        searchButton.setOnAction(e -> loadSales());
        loadSalesAll();
        loadFilterOptions();

        // ADD LISTENERS to ComboBoxes
        staffComboBox.setOnAction(e -> loadSales());
        serviceComboBox.setOnAction(e -> loadSales());
        paymentMethodComboBox.setOnAction(e -> loadSales());
    }

    private void loadSalesAll() {
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        loadSales(); // Just call loadSales() without any date filtering
    }

    private void loadSales() {
        SalesList.clear();
        totalRevenueLabel.setText("Total Revenue: RM0");
        completedAppointmentsLabel.setText("Completed Appointments: 0");

        String query = "SELECT s.id, u.name AS customer_name, staff.name AS staff_name, sv.name AS service_name, " +
                "s.amount, s.sale_date, pm.method_name " +
                "FROM sales s " +
                "JOIN users u ON s.user_id = u.id " +
                "JOIN appointments a ON s.appointment_id = a.id " +
                "JOIN users staff ON a.staff_id = staff.id " +
                "JOIN services sv ON s.service_id = sv.id " +
                "JOIN payment_methods pm ON s.payment_method_id = pm.id " +
                "WHERE 1=1";

        // Build filters
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String selectedStaff = staffComboBox.getValue();
        String selectedService = serviceComboBox.getValue();
        String selectedPayment = paymentMethodComboBox.getValue();

        if (startDate != null && endDate != null) {
            query += " AND s.sale_date BETWEEN ? AND ?";
        }
        if (selectedStaff != null && !selectedStaff.equals("All Staff")) {
            query += " AND staff.name = ?";
        }
        if (selectedService != null && !selectedService.equals("All Services")) {
            query += " AND sv.name = ?";
        }
        if (selectedPayment != null && !selectedPayment.equals("All Methods")) {
            query += " AND pm.method_name = ?";
        }

        double totalRevenue = 0;
        int completedAppointments = 0;
        Map<String, Double> paymentBreakdown = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (startDate != null && endDate != null) {
                pstmt.setDate(paramIndex++, Date.valueOf(startDate));
                pstmt.setDate(paramIndex++, Date.valueOf(endDate));
            }
            if (selectedStaff != null && !selectedStaff.equals("All Staff")) {
                pstmt.setString(paramIndex++, selectedStaff);
            }
            if (selectedService != null && !selectedService.equals("All Services")) {
                pstmt.setString(paramIndex++, selectedService);
            }
            if (selectedPayment != null && !selectedPayment.equals("All Methods")) {
                pstmt.setString(paramIndex++, selectedPayment);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String customerName = rs.getString("customer_name");
                String staffName = rs.getString("staff_name");
                String serviceName = rs.getString("service_name");
                LocalDate saleDate = rs.getDate("sale_date").toLocalDate();
                String paymentMethod = rs.getString("method_name");
                double totalAmount = rs.getDouble("amount");

                SalesList.add(new Sales(id, customerName, staffName, serviceName, saleDate, paymentMethod, totalAmount));

                totalRevenue += totalAmount;
                completedAppointments++;

                paymentBreakdown.put(paymentMethod, paymentBreakdown.getOrDefault(paymentMethod, 0.0) + totalAmount);
            }

            totalRevenueLabel.setText(String.format("Total Revenue: RM%.2f", totalRevenue));
            completedAppointmentsLabel.setText("Completed Appointments: " + completedAppointments);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load sales data.");
        }
    }

    private void loadFilterOptions() {
        try (Connection conn = DBConnection.getConnection()) {

            // Load Staff
            staffComboBox.getItems().add("All Staff");
            String staffQuery = "SELECT name FROM users WHERE role = 'admin'";
            PreparedStatement staffStmt = conn.prepareStatement(staffQuery);
            ResultSet staffRs = staffStmt.executeQuery();
            while (staffRs.next()) {
                staffComboBox.getItems().add(staffRs.getString("name"));
            }
            staffComboBox.getSelectionModel().selectFirst();

            // Load Services
            serviceComboBox.getItems().add("All Services");
            String serviceQuery = "SELECT name FROM services";
            PreparedStatement serviceStmt = conn.prepareStatement(serviceQuery);
            ResultSet serviceRs = serviceStmt.executeQuery();
            while (serviceRs.next()) {
                serviceComboBox.getItems().add(serviceRs.getString("name"));
            }
            serviceComboBox.getSelectionModel().selectFirst();

            // Load Payment Methods
            paymentMethodComboBox.getItems().add("All Methods");
            String paymentQuery = "SELECT method_name FROM payment_methods";
            PreparedStatement paymentStmt = conn.prepareStatement(paymentQuery);
            ResultSet paymentRs = paymentStmt.executeQuery();
            while (paymentRs.next()) {
                paymentMethodComboBox.getItems().add(paymentRs.getString("method_name"));
            }
            paymentMethodComboBox.getSelectionModel().selectFirst();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
