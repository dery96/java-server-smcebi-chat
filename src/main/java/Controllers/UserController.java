package Controllers;

import Helpers.DbConnection;
import Models.Token;
import Models.User;
import org.javalite.activejdbc.Base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import static Controllers.TokenController.CreateUserToken;

public class UserController {

    public static Integer CreateUser(String login, String password, String nickname, String gender) {
//        DbConnection.BaseConnection();
        String userJson = User.find("login = '" + login + "'").toJson(true);
        if (Objects.equals(userJson, "[\n\n]")) {
            System.out.println("login: " + login + " password: " + password + " nickname: " + nickname + " gender: " + gender);
            User channel = new User();
            channel.set("login", login);
            channel.set("password", password);
            channel.set("nickname", nickname);
            channel.set("gender", gender);
            channel.saveIt();
            return 201; // CREATED
        }
        return 403; // FORBIDDEN
    }

    public static String getUsers(String token) {
//        DbConnection.BaseConnection();
        String userFindAll = User.findAll().toJson(true);
        String tokenQuery = Token.find("token = ?", token).toJson(true);
        if (!tokenQuery.equals("[\n\n]")) {
            return userFindAll;
        }
        return ("FORBIDDEN");
    }
}
