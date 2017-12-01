package Controllers;

import Models.Token;
import Models.User;
import org.javalite.activejdbc.Base;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static Controllers.AccountControllers.AccountLogin;
import static Controllers.TokenController.RefreshToken;

public class SessionController {
    public static Boolean ExpireSessionTest(String login, String password) {
        if (AccountLogin(login, password).equals(202)) {
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            Token tokenQuery = Token.findFirst("login = ?", login);
            Date expire_time = (Date) tokenQuery.get("expire_time");
            Base.close();
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                expire_time = dateFormat.parse(dateFormat.format(expire_time));
                Date now = dateFormat.parse(dateFormat.format(new Date()));

                if (now.compareTo(expire_time) > 0) {
                    System.out.println("Session is outdated must be refreshed");
                    return true;
                }
            } catch (Exception e) {
                System.out.println("{From My TryCatch} Error: " + e);
            }
        }
        return false;
    }
}
