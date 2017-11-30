package Controllers;

import Models.User;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

import java.util.Objects;
import java.util.UUID;

public class UserController {

    public static Integer CreateUser(String login, String password, String nickname, String gender) {
        System.out.println("login: " + login + " password: " + password + " nickname: " + nickname + " sex: " + gender);
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        LazyList userQuery = User.where("login = '" + login + "' and password = '" + password + "'");
        String userJson = userQuery.toJson(true);
        if (Objects.equals(userJson, "[\n\n]")) {
            User channel = new User();
            channel.set("login", login);
            channel.set("password", password);
            channel.set("nickname", nickname);
            channel.set("gender", gender);
            channel.saveIt();
            Base.close();

            if (TokenController.CreateUserToken(login).equals(201)) { // CREATE UNIQUE USER TOKEN
                return 201;
            } else {
                return 403;
            }
        } else {
            // User typed correct login and password
            return 403;
        }
    }

    public static String getUsers() {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        return User.findAll().toJson(true);
    }
}
