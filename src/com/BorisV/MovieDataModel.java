package com.BorisV;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MovieDataModel extends AbstractTableModel {

    private int rowCount = 0;
    private int colCount = 0;
    ResultSet resultSet;

    public MovieDataModel(ResultSet rs) {
        this.resultSet = rs;
        setup(); 
    }

    private void setup(){

        countRows();

        try{
            colCount = resultSet.getMetaData().getColumnCount();

        } catch (SQLException se) {
            System.out.println("Error counting columns" + se);
        }

    }


    public void updateResultSet(ResultSet newRS){
        resultSet = newRS;
        setup();
    }


    private void countRows() {
        rowCount = 0;
        try {
            //Move cursor to the start...
            resultSet.beforeFirst();
            // next() method moves the cursor forward one row and returns true if there is another row ahead
            while (resultSet.next()) {
                rowCount++;

            }
            resultSet.beforeFirst();

        } catch (SQLException se) {
            System.out.println("Error counting rows " + se);
        }

    }
    @Override
    public int getRowCount() {
        countRows();
        return rowCount;
    }

    @Override
    public int getColumnCount(){
        return colCount;
    }

    @Override
    public Object getValueAt(int row, int col){
        try{
            //  System.out.println("get value at, row = " +row);
            resultSet.absolute(row+1);
            Object o = resultSet.getObject(col+1);
            return o.toString();
        }catch (SQLException se) {
            System.out.println(se);
            //se.printStackTrace();
            return se.toString();

        }
    }

    @Override
    //This is called when user edits an editable cell
    public void setValueAt(Object newValue, int row, int col) {

        //Make sure newValue is an integer AND that it is in the range of valid ratings

        int newRating;

        try {
            newRating = Integer.parseInt(newValue.toString());

            if (newRating < MovieDatabase.MOVIE_MIN_RATING || newRating > MovieDatabase.MOVIE_MAX_RATING) {
                throw new NumberFormatException("Movie rating must be within " + MovieDatabase.MOVIE_MIN_RATING +
                        " and " + MovieDatabase.MOVIE_MAX_RATING);
            }
        } catch (NumberFormatException ne) {
            //Error dialog box. First argument is the parent GUI component, which is only used to center the
            // dialog box over that component. We don't have a reference to any GUI components here
            // but are allowed to use null - this means the dialog box will show in the center of your screen.
            JOptionPane.showMessageDialog(null, "Entering a number between " + MovieDatabase.MOVIE_MIN_RATING + " " + MovieDatabase.MOVIE_MAX_RATING);
            //return prevents the following database update code happening...
            return;
        }

        //This only happens if the new rating is valid
        try {
            resultSet.absolute(row + 1);
            resultSet.updateInt(MovieDatabase.RATING_COLUMN, newRating);
            resultSet.updateRow();
            fireTableDataChanged(); //This gets the database updated.
        } catch (SQLException e) {
            System.out.println("error changing rating " + e);
        }

    }

    //We only want user to be able to edit column 3 - the rating column.
    //If this method always returns true, the whole table will be editable.

    //This might change if we were to add more data to our table, for example storing names of people who created the review.
    //TODO To fix: look into table column models, and generate the number columns based on the columns found in the ResultSet.

    /**
     * One option is to do a if(col==3) where 3 is the column with the name that need editing,
     * but what happens if the columns expand, the program stills looks at number 3 column. That
     * why doing the getColumnName is better and is method from the AbstractTableModel.
     * @param col number of the column that can be edited.
     * @see #getColumnName(int)
     */
    @Override
    public boolean isCellEditable(int row, int col){
        String colName = MovieDatabase.RATING_COLUMN; //Get the var that stores the name of the column.

        //This gets the name of the column that the user has clicked on.
        String clickedName = getColumnName(col);
        if (clickedName.equals(colName)) {
            return true;
        }

        //Default
        return false;
    }

    //Delete row, return true if successful, false otherwise
    public boolean deleteRow(int row){
        try {
            resultSet.absolute(row + 1);
            resultSet.deleteRow();
            //Tell table to redraw itself
            fireTableDataChanged();
            return true;
        }catch (SQLException se) {
            System.out.println("Delete row error " + se);
            return false;
        }
    }

    //returns true if successful, false if error occurs
    public boolean insertRow(String title, int year, int rating) {

        try {
            //Move to insert row, insert the appropriate data in each column, insert the row, move cursor back to where it was before we started
            resultSet.moveToInsertRow();
            resultSet.updateString(MovieDatabase.TITLE_COLUMN, title);
            resultSet.updateInt(MovieDatabase.YEAR_COLUMN, year);
            resultSet.updateInt(MovieDatabase.RATING_COLUMN, rating);
            resultSet.insertRow();
            resultSet.moveToCurrentRow();
            fireTableDataChanged();
            return true;

        } catch (SQLException e) {
            System.out.println("Error adding row");
            System.out.println(e);
            return false;
        }

    }

    @Override
    public String getColumnName(int col){
        //Get from ResultSet metadata, which contains the database column names
        try {
            return resultSet.getMetaData().getColumnName(col + 1);

        } catch (SQLException se) {
            System.out.println("Error fetching column names" + se);
            return "?";
        }
    }


}
