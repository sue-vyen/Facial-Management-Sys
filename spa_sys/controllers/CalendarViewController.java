package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.Appointment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

public class CalendarViewController {
    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;
    @FXML private ListView<Appointment> appointmentsList;
    @FXML private VBox detailsContainer;

    private YearMonth currentYearMonth;

    // Map staff name to their assigned colour code
    private Map<String, String> staffColourMap = new LinkedHashMap<>();

    public void initialize() {
        currentYearMonth = YearMonth.now();
        updateCalendar();
        calendarGrid.setAlignment(Pos.CENTER);
    }

//    Updates the calendar whenever the month is changed
    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();

//        Create 7 columns for the days
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0/7);
            calendarGrid.getColumnConstraints().add(cc);
        }

//        Set each month's name
        monthLabel.setText(
                currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                        + " " + currentYearMonth.getYear()
        );
        populateCalendarCells();
        detailsContainer.setVisible(false); // to hide the details pane unless clicked
    }


//    fill up the calendar cells with appts made and days
    private void populateCalendarCells() {
        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};

//        Create the day headers
        for (int i=0; i<7; i++) {
            Label header = new Label(days[i]);
            header.setStyle("-fx-font-weight:bold; -fx-text-fill:#2c3e50;");
            calendarGrid.add(header, i, 0);
        }

//        add blank cells bfr the 1st of each month so that it looks like a rl calendar
        LocalDate first = currentYearMonth.atDay(1);
        int startCol = first.getDayOfWeek().getValue()-1;
        for (int c=0; c<startCol; c++) {
            VBox ph = new VBox(); ph.getStyleClass().add("calendar-cell");
            calendarGrid.add(ph, c, 1);
        }

//        adds the exact day number for each month
        int col=startCol, row=1;
        for (int day=1; day<=currentYearMonth.lengthOfMonth(); day++) {
            VBox cell = new VBox(3);
            cell.setPrefHeight(100);
            cell.getStyleClass().add("calendar-cell");
            int thisDay=day;

//            IF day cell is clicked to see appts
            cell.setOnMouseClicked(e -> showDayDetails(thisDay));
            Label dayNum = new Label(String.valueOf(day));
            cell.getChildren().add(dayNum);

//            load in the appts for the day
            for (Appointment ap : getAppointmentsForDay(day)) {
                Label apptLbl = new Label(ap.getServiceName());
                apptLbl.setStyle(
                        "-fx-background-color:"+ap.getStaffColour()+";"
                                +"-fx-background-radius:5; -fx-padding:3 6; -fx-text-fill:white;"
                );
                apptLbl.setTooltip(new Tooltip(
                        ap.getAppointmentDate().toLocalTime()+" — "+ap.getCustomerName()
                ));
                cell.getChildren().add(apptLbl);
            }
            calendarGrid.add(cell, col, row);
            if (++col>6) { col=0; row++; }
        }
    }

//    ppt details for that day is shown
    private List<Appointment> getAppointmentsForDay(int day) {
        List<Appointment> appts = new ArrayList<>();
        String sql =
                "SELECT a.id, a.user_id, a.service_id, a.staff_id, a.remarks, a.appointment_date, a.status, a.payment_method_id, " +
                        "s.name AS service_name, u2.name AS staff_name, pm.method_name AS payment_method_name, " +
                        "u.name AS customer_name, u2.colour_code AS staff_color " +
                        "FROM appointments a " +
                        "JOIN users u ON a.user_id = u.id " +
                        "JOIN services s ON a.service_id = s.id " +
                        "JOIN users u2 ON a.staff_id = u2.id " +
                        "JOIN payment_methods pm ON a.payment_method_id = pm.id " +
                        "WHERE YEAR(a.appointment_date)=? AND MONTH(a.appointment_date)=? AND DAY(a.appointment_date)=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentYearMonth.getYear());
            ps.setInt(2, currentYearMonth.getMonthValue());
            ps.setInt(3, day);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appts.add(new Appointment(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("service_id"),
                            rs.getInt("staff_id"),
                            rs.getString("remarks"),
                            rs.getTimestamp("appointment_date").toLocalDateTime(),
                            rs.getString("status"),
                            rs.getInt("payment_method_id"),
                            rs.getString("service_name"),
                            rs.getString("staff_name"),
                            rs.getString("payment_method_name"),
                            rs.getString("staff_color"),
                            rs.getString("customer_name")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appts;
    }

//    IF day is clicked
    private void showDayDetails(int day) {
        List<Appointment> appts = getAppointmentsForDay(day);
        if (appts.isEmpty()) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(calendarGrid.getScene().getWindow());
        dialog.setTitle("Appointments on " +
                currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                " " + day);
        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("appointment-dialog");
        pane.getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPrefWidth(300);

        for (Appointment a : appts) {
//            since not all appts have remarks
            String remarks = a.getRemarks();
            if (remarks == null || remarks.isEmpty()) {
                remarks = "(none)";
            }

            Label lbl = new Label(String.format(
                    "%s\nCustomer: %s\nFacialist: %s\nTime: %s\nRemarks: %s",
                    a.getServiceName(),
                    a.getCustomerName(),
                    a.getStaffName(),
                    a.getAppointmentDate().toLocalTime(),
                    remarks
            ));
            lbl.setWrapText(true);
            lbl.setStyle(
                    "-fx-background-color:" + a.getStaffColour() + ";" +
                            "-fx-text-fill:white; -fx-background-radius:6; -fx-padding:8;"
            );
            content.getChildren().add(lbl);
        }

        pane.setContent(content);
        dialog.showAndWait();
    }

    @FXML private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }
    @FXML private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

//    opens the popup window to book appt from admin side
    @FXML
    private void openBookAppointmentPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/calendar_appointment_popup.fxml"));
            Parent popupContent = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Book New Appointment");
            popupStage.setScene(new Scene(popupContent));

            CalendarAppointmentPopupController popupController = loader.getController();
            // pass refresh method to popup
            popupController.setOnAppointmentBooked(this::updateCalendar);

            popupStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
