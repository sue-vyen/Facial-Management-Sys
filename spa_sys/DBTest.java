package com.example.spa_sys;

import java.sql.Connection;
import java.sql.SQLException;
import com.example.spa_sys.database.DBConnection;


public class DBTest {
    public static void main(String[] args)
    {
        try (Connection connection = DBConnection.getConnection())
        {
            if (connection != null) {
                System.out.println("Connected to MySQL successfully!");
            }
        }
        catch (SQLException e){
            System.out.println("Connection failed: " + e.getMessage());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
