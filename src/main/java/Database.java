import java.sql.*;

public class Database {
    private static Connection conn =  null;
    public static void connect() {
        try {
            String url = "jdbc:sqlite:src/main/resources/public/chat.db";
            conn = DriverManager.getConnection(url);
            Statement statement = conn.createStatement();
            System.out.println("Connection to SQLite has been established.");
            ResultSet rs = statement.executeQuery("select * from user");
            while(rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("id = " + rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
