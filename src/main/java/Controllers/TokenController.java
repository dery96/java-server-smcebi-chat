package Controllers;

import Models.Token;
import Models.User;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

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

    public static String getToken(String login, String password) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String userQuery = "login = '" + login + "' and password = '" + password + "'";
        String userJson = User.find(userQuery).toJson(true);
        if (!Objects.equals(userJson, "[\n\n]")) {
            return Token.find("login = '" + login + "'").toJson(true);
        }
        Base.close();
        return ("Unauthorized");
    }
}
