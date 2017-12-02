package Controllers;

import Helpers.DbConnection;
import Models.Token;
import org.javalite.activejdbc.Base;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SessionController {
    public static Boolean ExpireSessionTest(String token) {
//        DbConnection.BaseConnection();
        Token tokenQuery = Token.findFirst("token = ?", token);
        Date expire_time = (Date) tokenQuery.get("expire_time");
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

        return false;
    }
}
