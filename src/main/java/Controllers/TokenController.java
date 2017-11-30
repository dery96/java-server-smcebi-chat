package Controllers;

import Models.Token;
import org.javalite.activejdbc.Base;

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
//
//    }

    public static String getTokens() {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        return Token.findAll().toJson(true);
    }
}
