package Controllers;

import Models.User;
import org.javalite.activejdbc.Base;

import java.util.List;
import java.util.Objects;

public class AccountControllers {
    public static Integer AccountLogin(String login, String password) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String userQuery = "login = '" + login + "' and password = '" + password + "'";
        String userJson = User.find(userQuery).toJson(true);
        Base.close();
        if (!Objects.equals(userJson, "[\n\n]")) {
            return 202; // ACCEPTED
        }
        return 401; // UNAUTHORIZED
    }

    public static String GetId(String login) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String userQuery = "login = '" + login + "'";
        String userJson = User.find(userQuery).toString();
        Base.close();

        return userJson;
    }

    public static Integer ChangePassword(String id, String password, String newPassword) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String userQuery = "id = '" + id + "' and password = '" + password + "'";
        List<User> list = User.find(userQuery);
        if (!list.get(0).toJson(true).equals("[\n\n]")) {
            User user = list.get(0);
            user.set("password", newPassword);
            user.saveIt();
            Base.close();
            return 202; // ACCEPTED
        }
        Base.close();
        return 403; // FORBIDDEN
    }

    public static Integer ChangeNickname(String id, String password, String newNickname) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String userQuery = "id = '" + id + "' and password = '" + password + "'";
        List<User> list = User.find(userQuery);
        if (!list.get(0).toJson(true).equals("[\n\n]")) {
            User user = list.get(0);
            user.set("nickname", newNickname);
            user.saveIt();
            Base.close();
            return 202; // ACCEPTED
        }
        Base.close();
        return 403; // FORBIDDEN
    }
}
