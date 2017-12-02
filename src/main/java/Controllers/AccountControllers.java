package Controllers;
import Models.User;

import java.util.List;
import java.util.Objects;


public class AccountControllers {
    public static Integer AccountLogin(String login, String password) {
//        DbConnection.BaseConnection();
        String userQuery = "login = '" + login + "' and password = '" + password + "'";
        String userJson = User.find(userQuery).toJson(true);
        if (!Objects.equals(userJson, "[\n\n]")) {
            return 202; // ACCEPTED
        }
        return 401; // UNAUTHORIZED
    }

    public static String GetId(String login) {
//        DbConnection.BaseConnection();
        String userQuery = "login = '" + login + "'";
        String userJson = User.find(userQuery).toString();
        if (!Objects.equals(userJson, "[\n\n]")) {
            List<User> list = User.find("login = '" + login + "'");
            return list.get(0).get("id").toString();
        }
        return userJson;
    }

    public static Integer ChangePassword(String id, String password, String newPassword) {
//        DbConnection.BaseConnection();
        String userQuery = "id = '" + id + "' and password = '" + password + "'";
        List<User> list = User.find(userQuery);
        if (!list.get(0).toJson(true).equals("[\n\n]")) {
            User user = list.get(0);
            user.set("password", newPassword);
            user.saveIt();
            return 202; // ACCEPTED
        }
        return 403; // FORBIDDEN
    }

    public static Integer ChangeNickname(String id, String password, String newNickname) {
//        DbConnection.BaseConnection();
        String userQuery = "id = '" + id + "' and password = '" + password + "'";
        List<User> list = User.find(userQuery);
        if (!list.get(0).toJson(true).equals("[\n\n]")) {
            User user = list.get(0);
            user.set("nickname", newNickname);
            user.saveIt();
            return 202; // ACCEPTED
        }
        return 403; // FORBIDDEN
    }
}
