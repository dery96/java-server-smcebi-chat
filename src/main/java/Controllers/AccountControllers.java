package Controllers;

import Models.User;
import org.javalite.activejdbc.Base;

import java.util.List;
import java.util.Objects;

public class AccountControllers {
    public static Integer AccountLogin(String login, String password) {
        System.out.println(login + ", " + password);
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String userQuery = "login = '" + login + "' and password = '" + password + "'";
        String userJson = User.find(userQuery).toJson(true);
        Base.close();
        if (!Objects.equals(userJson, "[\n\n]")) {
            return 202; // ACCEPTED
        }
        // correct login and password
        return 401; // UNAUTHORIZED
    }

    public static Integer ChangePassword (String login, String password, String newPassword) {
        if (AccountLogin(login, password).equals(202)) {
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            String userQuery = "login = '" + login + "' and password = '" + password + "'";
            List<User> list = User.find(userQuery);
            if (!list.get(0).toJson(true).equals("[\n\n]")) {
                User user = list.get(0);
                user.set("password", newPassword);
                user.saveIt();
                Base.close();
                return 202; // ACCEPTED
            }
            Base.close();
            return 400; // BAD REQUEST
        }
        return 401; // UNAUTHORIZED
    }
    public static Integer ChangeNickname (String login, String password, String newNickname) {
        if (AccountLogin(login, password).equals(202)) {
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            String userQuery = "login = '" + login + "' and password = '" + password + "'";
            List<User> list = User.find(userQuery);
            if (!list.get(0).toJson(true).equals("[\n\n]")) {
                User user = list.get(0);
                user.set("nickname", newNickname);
                user.saveIt();
                Base.close();
                return 202; // ACCEPTED
            }
            Base.close();
            return 400; // BAD REQUEST
        }
        return 401; // UNAUTHORIZED
    }
}
