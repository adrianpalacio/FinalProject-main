package dacd.adrianpalacio.control;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.sql.*;

public class SQLiteDatamartStore implements DatamartStore{

    private final Connection connection;

    public SQLiteDatamartStore(String path) {
        try {
            String dbPath = path + "/datamart/datamart.db";
            new File(dbPath).getParentFile().mkdirs();
            this.connection = connect(dbPath);
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void save (String data){
        try {
            JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
            String ss = jsonObject.get("ss").getAsString();
            if ("Weather-Provider".equals(ss)) {
                addPredictionData(jsonObject);
            } else if ("Hotel-Provider".equals(ss)) {
                addHotelData(jsonObject);
            }
            connection.commit(); // Commit the transaction after processing all statements
        } catch (SQLException e) {
            try {
                connection.rollback(); // Rollback the transaction in case of an exception
            } catch (SQLException rollbackException) {
                throw new RuntimeException("Error rolling back transaction", rollbackException);
            }
            throw new RuntimeException("Error saving data", e);
        }
    }

    private Connection connect(String dbPath) {
        try {
            String url = "jdbc:sqlite:" + dbPath;
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createPredictionTable(String tableName) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "predictionTs TEXT PRIMARY KEY, " +
                            "place TEXT, " +
                            "temperature REAL, " +
                            "precipitation REAL, " +
                            "humidity INTEGER, " +
                            "clouds INTEGER, " +
                            "wind_velocity REAL)"
            );

            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addPredictionData(JsonObject data) {
        String tableName = "Prediction_" + data.getAsJsonObject("location").get("place").getAsString().replaceAll("\\s", "")
                .replaceAll("-","_");
        System.out.println(tableName);
        createPredictionTable(tableName);
        if (countRowsInTable(tableName) == 5) clearTable(tableName);

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + tableName +
                            "(predictionTs, place, " +
                            "temperature, precipitation, humidity, clouds, wind_velocity) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );

            statement.setString(1, data.get("predictionTs").getAsString());
            statement.setString(2, data.getAsJsonObject("location").get("place").getAsString());
            statement.setDouble(3, data.get("temperature").getAsDouble());
            statement.setDouble(4, data.get("precipitation").getAsDouble());
            statement.setInt(5, data.get("humidity").getAsInt());
            statement.setInt(6, data.get("clouds").getAsInt());
            statement.setDouble(7, data.get("windVelocity").getAsDouble());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createHotelTable(String tableName) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "name TEXT PRIMARY KEY, " +
                            "place TEXT, " +
                            "priceRangePerNight TEXT, " +
                            "rating TEXT) "

            );

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addHotelData(JsonObject data) {
        String tableName = "Hotels_" + data.get("place").getAsString().replaceAll("\\s", "")
                .replaceAll("-", "_");
        System.out.println(tableName);
        createHotelTable(tableName);

        if (hotelExists(tableName, data.get("name").getAsString())) {
            updateHotelData(tableName, data);
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO " + tableName +
                                " (name, place, priceRangePerNight, rating) " +
                                "VALUES (?, ?, ?, ?)"
                );

                statement.setString(1, data.get("name").getAsString());
                statement.setString(2, data.get("place").getAsString());
                statement.setString(3, data.get("priceRangePerNight").getAsString());
                statement.setString(4, data.get("rating").getAsString());

                statement.execute();
                System.out.println("New hotel added: " + data);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean hotelExists(String tableName, String hotelName) {
        try {
            String query = "SELECT COUNT(*) FROM " + tableName + " WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, hotelName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private void updateHotelData(String tableName, JsonObject data) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE " + tableName +
                            " SET priceRangePerNight = ?, rating = ?" +
                            " WHERE name = ?"
            );

            statement.setString(1, data.get("priceRangePerNight").getAsString());
            statement.setString(2, data.get("rating").getAsString());
            statement.setString(3, data.get("name").getAsString());

            statement.executeUpdate();
            System.out.println("Hotel updated: " + data);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int countRowsInTable(String tableName) {
        int rowCount = 0;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName);

            if (resultSet.next()) {
                rowCount = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowCount;
    }

    private void clearTable(String tableName) {
        try {
            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM " + tableName);
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
