package Helpers;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;

import java.sql.Connection;
import java.sql.SQLException;

public class DbConnection {
    public static Connection connection = null;
    public static DB db = null;

    public static Connection BaseConnection() {
        if (db == null) {

        }
        try {
            DbConnection.db = new DB();
            DbConnection.db.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            DbConnection.connection = db.getConnection();
        } catch (Exception e) {
            System.out.println("e: " + e);
        }
        System.out.println(DbConnection.connection);
        return DbConnection.connection;
    }

    public static void CloseConnection(Connection connection) throws SQLException{
        connection.close();
    };
}
