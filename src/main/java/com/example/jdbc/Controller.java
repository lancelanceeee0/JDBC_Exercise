package com.example.jdbc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class Controller {

    @FXML private TextField txtName;
    @FXML private TextField txtCourse;
    @FXML private ChoiceBox<YearLevel> cbYear;

    @FXML private TableView<Student> table;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colCourse;
    @FXML private TableColumn<Student, String> colYear;

    private ObservableList<Student> list = FXCollections.observableArrayList();
    private Connection conn;
    private int selectedId = -1;

    @FXML
    public void initialize() {
        conn = DBConnection.connect();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Cannot connect to PostgreSQL.");
            return;
        }

        // Fill ChoiceBox with enum values
        cbYear.getItems().setAll(YearLevel.values());

        // Set up table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("yearLevel"));

        loadData();

        // Row click -> populate fields
        table.setOnMouseClicked(event -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedId = selected.getId();
                txtName.setText(selected.getName());
                txtCourse.setText(selected.getCourse());

                // Convert stored string back to YearLevel enum
                String storedYear = selected.getYearLevel();
                for (YearLevel y : YearLevel.values()) {
                    if (y.toString().equals(storedYear)) {
                        cbYear.setValue(y);
                        break;
                    }
                }
            }
        });
    }

    private void loadData() {
        list.clear();
        String query = "SELECT * FROM students ORDER BY id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                list.add(new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("course"),
                        rs.getString("year_level")
                ));
            }
            table.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load data from database.");
        }
    }

    @FXML
    private void addStudent() {
        if (!validateFields()) return;

        String sql = "INSERT INTO students (name, course, year_level) VALUES (?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, txtName.getText().trim());
            pst.setString(2, txtCourse.getText().trim());
            pst.setString(3, cbYear.getValue().toString());   // store "1st Year", etc.
            pst.executeUpdate();
            loadData();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Add Error", "Could not add student.");
        }
    }

    @FXML
    private void updateStudent() {
        if (selectedId == -1) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student from the table.");
            return;
        }
        if (!validateFields()) return;

        String sql = "UPDATE students SET name = ?, course = ?, year_level = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, txtName.getText().trim());
            pst.setString(2, txtCourse.getText().trim());
            pst.setString(3, cbYear.getValue().toString());
            pst.setInt(4, selectedId);
            pst.executeUpdate();
            loadData();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Update Error", "Could not update student.");
        }
    }

    @FXML
    private void deleteStudent() {
        if (selectedId == -1) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this student?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM students WHERE id = ?";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setInt(1, selectedId);
                    pst.executeUpdate();
                    loadData();
                    clearFields();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Delete Error", "Could not delete student.");
                }
            }
        });
    }

    @FXML
    private void clearFields() {
        txtName.clear();
        txtCourse.clear();
        cbYear.setValue(null);
        selectedId = -1;
    }

    private boolean validateFields() {
        if (txtName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name cannot be empty.");
            return false;
        }
        if (txtCourse.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Course cannot be empty.");
            return false;
        }
        if (cbYear.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a year level.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}