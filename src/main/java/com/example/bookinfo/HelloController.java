/*
    Name: William Espitia
    Class: CSC 311
    Assignment: Homework 1
 */

package com.example.bookinfo;

import com.google.gson.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloController {
    @FXML
    private TextField textFieldAddNewName;
    @FXML
    private TextField textFieldAddNewCategory;
    @FXML
    private TextField textFieldAddNewScore;
    @FXML
    private ListView listViewBooks;


    // this method will initialize the ObservableList
    public void initialize() {
        // lets us know that ObservableList has been initialized
        System.out.println ("Initialize Called");
        // get the observable list reference
        ObservableList<String> items = listViewBooks.getItems();

        /*
        create database
         */
        /*
        String dbFilePath = ".//GradeInformation.accdb";
        String databaseURL = "jdbc:ucanaccess://" + dbFilePath;
        File dbFile = new File(dbFilePath);
        if (!dbFile.exists()) {
            try (Database db =
                         DatabaseBuilder.create(Database.FileFormat.V2010, new File(dbFilePath))) {
                System.out.println("The database file has been created.");
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        }

         */

        /*
        create table
         */
        /* try {
            Connection conn = DriverManager.getConnection(databaseURL);
            String sql;
            sql = "CREATE TABLE GradeInformation (Name nvarchar(255), Category nvarchar(255), Score INT)";
            Statement createTableStatement = conn.createStatement();
            createTableStatement.execute(sql);
            conn.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        */



    }
    // this method will insert the data into the database and be called upon when
    // the load database from json button is pressed
    public static void insertData(Connection conn, String name, String category, int score) {
        // the three ? characters will be filled with data by the preparedStatement class
        String sql = "INSERT INTO GradeInformation (Name, Category, Score) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = null;
        try {
            // declares that conn has a valid connection to the database
            preparedStatement = conn.prepareStatement(sql);
            // replaces the first ? in the SQL string with data from the first variable
            preparedStatement.setString(1, name);
            // replaces the second ? in the SQL string with data from the second variable
            preparedStatement.setString(2, category);
            // replaces the third ? in the SQL string with data from the third variable
            preparedStatement.setInt(3, score);
            // once all data is inserted, it will execute the preparedStatement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // this method will load the database from the json file with information
    public void handleLoadDBFromJsonMenuItem() {
        // name of the database
        String dbFilePath = ".//GradeInformation.accdb";
        // pathway to the database
        String databaseURL = "jdbc:ucanaccess://" + dbFilePath;
        Connection conn = null;
        try {
            // opens the connection to the database
            conn = DriverManager.getConnection(databaseURL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // lets us know that the load database from json menu option was pressed
        System.out.println ("handleLoadDBFromJsonMenuItem called");
        // calls the clearDataBase() method to clear the information stored in the database
        // needed to avoid having the information duplicated
        clearDataBase();
        // sets up gson
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            // opens the file
            FileReader fr = new FileReader("grades.json");
            // pass in the filereader and pass in the data type of the json being read
            // in this case the data type is an array
            holdGradeInfo[] info = gson.fromJson(fr, holdGradeInfo[].class);
            // special for loop will iterate through the json file and get the information from each category
            // and insert it into the database
            for (holdGradeInfo g : info) {
                insertData(conn, g.getName(), g.getCategory(), g.getScore());
            }
            // lets us know that the data was loaded into the database successfully
            System.out.println ("Data loaded from Json to database successfully");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // this method will display the grades from the database
    public void handleDisplayGradesFromDB() {
        // name of the database
        String dbFilePath = ".//GradeInformation.accdb";
        // pathway to the database
        String databaseURL = "jdbc:ucanaccess://" + dbFilePath;
        Connection conn = null;
        try {
            // opens connection to the database
            conn = DriverManager.getConnection(databaseURL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // lets us know that the user pressed the display grades from DB button
        System.out.println ("handleDisplayGradesFromDB called");
        // clears the list view in case any information is still showing
        listViewBooks.getItems().clear();

        try {
            // sets table name equal to GradeInformation
            String tableName = "GradeInformation";
            Statement stmt = conn.createStatement();
            // ResultSet contains all rows of data from the database
            ResultSet result = stmt.executeQuery("select * from " + tableName);
            // loops through the ResultSet
            while (result.next()) {
                // gets data from name column and stores in the information
                String name = result.getString("Name");
                // gets data from the category column and stores the information
                String category = result.getString("Category");
                // gets data from the score column and stores the information
                int score = result.getInt("Score");
                // adds the information into the list view
                // separated by a comma and a space
                listViewBooks.getItems().add(name + ", " + category + ", " + score);
            }
            // lets the user know the data was successfully displayed from the database
            System.out.println ("Data successfully displayed from the database");
        } catch (SQLException except) {
            except.printStackTrace();
        }

    }

    // this method will close the application when "close" is pressed from the menu
    public void handleCloseMenuItem() {
        // lets us know that the close menu option was pressed
        System.out.println ("handleCloseMenuItem called");
        // closes the application
        Platform.exit();
        // lets us know the application was closed successfully
        System.out.println ("Application closed successfully");
    }
    // this method will add the book info inputted by the user
    public void handleAddGradeToDB() {
        // lets us know that the user pushed the button
        System.out.println ("handleAddGradeToDB called");

        String newName; // name declared a String
        String newCategory; // category declared a String
        String newScore; // score declared a String
        newName = textFieldAddNewName.getText(); // gets text from Name text box
        newCategory = textFieldAddNewCategory.getText(); // gets text from Category box
        newScore = textFieldAddNewScore.getText(); // gets integer from Score box

        // if statement that checks if name, category and score all are valid inputs
        if (isValidInput(newName, newCategory, newScore)) {
            // put the name, category and score into the ListView
            ObservableList<String> items;
            items = listViewBooks.getItems();

            // when outputted, will be separated by comma and space to look neater
            items.add(newName + ", " + newCategory + ", " + newScore);

            // clears the text boxes after addGradeToDB button is pushed
            textFieldAddNewName.clear();
            textFieldAddNewCategory.clear();
            textFieldAddNewScore.clear();
        }
        // if an input is invalid, it will let us know
        else {
            // create alert dialog box
            Alert alert = new Alert (Alert.AlertType.WARNING); // creates the alert as a warning
            alert.setTitle("Invalid Input"); // title will be Invalid Input
            // text that goes under the title
            alert.setContentText("Unable to add new grade to the database. One or more of the input values are incorrect.");
            // show and wait will leave the alert box up until user exits by pressing ok
            alert.showAndWait();
            // if user presses the ok button or the x to close the box, it will let us know the user acknowledged
            // that the input was incorrect
            if (alert.getResult() == ButtonType.OK) {
                System.out.println ("User acknowledged incorrect input.");
            }
            System.out.println ("Invalid input please try again.");
        }
    }

    // this class will be how each check if the inputs are valid and contain
    // the specifications as directed
    private boolean isValidInput (String name, String category, String score) {
        // if name is empty, it will return false
        if (name.isEmpty()) {
            return false;
        }
        // if category is empty or it does not contain letters a-z or A-Z it will return false
        // if it has numbers or spaces it will return false, this can only have letters
        // the * function helps with that, states that it can contain any amount of letters
        // within that range, but once anything other than a letter is inputted it will be false
        if ((category.isEmpty()) || (!category.matches("[a-zA-Z]+"))) {
            return false;
        }
        // if score is empty, or it has a space, or has anything that isn't a digit it will return false
        if ((score.isEmpty()) || (score.matches("\\s")) || (!score.matches("\\d*"))) {
            return false;
        }
        // if all conditions are met, it will return true
        return true;
    }

    // this method will serve as our setter/getter method
    public static class holdGradeInfo {
        private String name;
        private String category;
        private int score;
        // grade info constructor with three parameters
        public holdGradeInfo (String n, String c, int s) {
            name = n;
            category = c;
            score = s;
        }
        // sets name equal to n
        public void setName (String n) {
            name = n;
        }
        // sets category equal to c
        public void setCategory (String c) {
            category = c;
        }
        // sets score equal to s
        public void setScore (int s) {
            score = s;
        }
        // gets name and returns it
        public String getName() {
            return name;
        }
        // gets category and returns it
        public String getCategory() {
            return category;
        }
        // gets score and returns it
        public int getScore() {
            return score;
        }
    }

    // this method will clear the database before adding new information to it
    public void clearDataBase() {
        // name of the database
        String dbFilePath = ".//GradeInformation.accdb";
        // pathway to the database
        String databaseURL = "jdbc:ucanaccess://" + dbFilePath;
        Connection conn = null;
        try {
            // opens the connection to the database
            conn = DriverManager.getConnection(databaseURL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // creates SQL string to delete all records from the table
        String sql = "DELETE FROM GradeInformation";
        PreparedStatement preparedStatement = null;
        try {
            // declares that conn has a valid connection to the database
            preparedStatement = conn.prepareStatement(sql);
            // run the delete statement, it will return the number of rows deleted
            int rowsDeleted = preparedStatement.executeUpdate();
            // lets us know in the output box how many rows were deleted
            System.out.println ("Number of rows deleted: " + rowsDeleted);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}