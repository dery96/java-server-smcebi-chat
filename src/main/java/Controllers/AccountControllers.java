package Controllers;

import Models.User;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

import java.util.Objects;

public class AccountControllers {
    public static Integer AccountLogin(String login, String password) {
        System.out.println(login + " + " + password);
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        LazyList userQuery = User.where("login = '" + login + "' and password = '" + password + "'");
        String userJson = userQuery.toJson(true);
        if (Objects.equals(userJson, "[\n\n]")) {
            // incorrect login or password
            return 400; // Bad Request
        } else {
            // correct login and password
            return 202; // Accepted
        }
    }
    public static Integer ChangePassword (String newPassword, String id) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        return 400;
//        return 202;
    }
    public static Integer ChangeNickname (String newNickname, String id) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        return 400;
//        return 202;
    }
}
