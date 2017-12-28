package Controllers;

import Helpers.DbConnection;
import Models.Token;
import Models.User;
import org.javalite.activejdbc.Base;
import java.util.Objects;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    public static Boolean CloseSession(String token) throws java.text.ParseException {
//        DbConnection.BaseConnection();
        String tokenJson = Token.find("token = ?", token).toJson(true);
        if (!Objects.equals(tokenJson, "[\n\n]")) {
            Token tokenQuery = Token.findFirst("token = ?", token);
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date now = dataFormat.parse(dataFormat.format(new Date()));
            LocalDateTime yourDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
            yourDate = yourDate.plus(Duration.parse("PT0H"));

            String out = dataFormat.format(
                    Date.from(
                            yourDate.atZone(
                                    ZoneId.systemDefault()).toInstant()
                    ));
            tokenQuery.set("expire_time", out);
            tokenQuery.saveIt();
            return true;
        }
        return false;
    }
}
