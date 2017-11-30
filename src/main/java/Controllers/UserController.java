package Controllers;

import Models.User;
import org.javalite.activejdbc.Base;

import java.util.Objects;

public class UserController {

    public static Integer CreateUser(String login, String password, String nickname, String gender) {
        System.out.println("login: " + login + " password: " + password + " nickname: " + nickname + " gender: " + gender);
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String userJson = User.find("login = '" + login + "'").toJson(true);
        if (Objects.equals(userJson, "[\n\n]")) {
            User channel = new User();
            channel.set("login", login);
            channel.set("password", password);
            channel.set("nickname", nickname);
            channel.set("gender", gender);
            channel.saveIt();
            Base.close();
            if (TokenController.CreateUserToken(login).equals(201)) { // CREATE UNIQUE USER TOKEN
                return 201; // CREATED
            }
            return 401; // UNAUTHORIZED
        }
        Base.close();
        return 401; // UNAUTHORIZED
    }

    public static String getUsers(String login, String password) {
        if (AccountControllers.AccountLogin(login, password).equals(202)) {
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            String userFindAll = User.findAll().toJson(true);
            Base.close();
            return userFindAll;
        }
        return ("Unauthorized");
    }
}
