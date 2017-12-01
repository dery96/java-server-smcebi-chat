package Controllers;

import Models.Token;
import Models.User;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class TokenController {
    public static Integer CreateUserToken(String login) {
        try {
            String generatedtoken = GenerateToken();
            System.out.println("login: " + login + " token: " + generatedtoken);
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            Token token = new Token();
            token.set("token", generatedtoken);
            token.set("login", login);
            token.saveIt();
            Base.close();
            return 201; // CREATED
        } catch (Exception e) {
            System.out.println(e);
            return 403; // Forbidden REQUEST
        }
    }

    public static String GenerateToken() {
        // Simple user Token generator, [unique immutable UUID code]
        return UUID.randomUUID().toString();
    }
//    public static Integer RefreshTokenSession() {
//    }

    public static String GetToken(String login, String password) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String userQuery = "login = '" + login + "' and password = '" + password + "'";
        String userJson = User.find(userQuery).toJson(true);
        if (!Objects.equals(userJson, "[\n\n]")) {
            String tokenFind = Token.find("login = '" + login + "'").toJson(true);
            Base.close();
            return tokenFind;
        }
        Base.close();
        return ("Unauthorized");
    }

    public static void RefreshToken(String login) {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date now = dataFormat.parse(dataFormat.format(new Date()));
            LocalDateTime yourDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
            yourDate = yourDate.plus(Duration.parse("PT3H"));

            String out = dataFormat.format(
                    Date.from(
                            yourDate.atZone(
                                    ZoneId.systemDefault()).toInstant()
                    ));

            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            Token tokenQuery = Token.findFirst("login = ?", login);
            tokenQuery.set("expire_time", out);
            tokenQuery.saveIt();
            Base.close();
        } catch (Exception e) {
            System.out.println("<OWN Exception from RefreshToken: " + e);
        }
    }
}
