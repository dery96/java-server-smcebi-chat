package Controllers;

import Helpers.DbConnection;
import Models.Token;
import Models.User;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TokenController {
    public static Integer CreateUserToken(String login) {
        Base.detach();
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd") ;
        String tokenJson = Token.find("login = ?", login).toJson(true);
        System.out.println(tokenJson);
        if (Objects.equals(tokenJson, "[\n\n]")) {
//            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd") ;
            String generatedtoken = GenerateToken();
            Token newToken = new Token();
            newToken.set("token", generatedtoken);
            newToken.set("login", login);
            System.out.println("czemu nie sejwuje kurdełe");
            newToken.saveIt();
            return 201; // CREATED
        }
        Base.close();
        return 403; // Forbidden REQUEST
    }

    public static String GenerateToken() {
        // Simple user Token generator, [unique immutable UUID code]
        return UUID.randomUUID().toString();
    }

    public static String GetToken(String login) {
//        DbConnection.BaseConnection();
        String userQuery = "login = '" + login + "'";
        String userJson = User.find(userQuery).toJson(true);
        if (!Objects.equals(userJson, "[\n\n]")) {
            List<Token> list = Token.find("login = '" + login + "'");
            return list.get(0).get("token").toString();
        }
        return ("Unauthorized");
    }

    public static void RefreshToken(String token) throws java.text.ParseException {
//        DbConnection.BaseConnection();
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date now = dataFormat.parse(dataFormat.format(new Date()));
        LocalDateTime yourDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        yourDate = yourDate.plus(Duration.parse("PT3H"));

        String out = dataFormat.format(
                Date.from(
                        yourDate.atZone(
                                ZoneId.systemDefault()).toInstant()
                ));

        Token tokenQuery = Token.findFirst("token = ?", token);
        tokenQuery.set("expire_time", out);
        tokenQuery.saveIt();
    }
}
