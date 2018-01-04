package Controllers;

import Models.Token;
import Models.User;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TokenController {
    public static Integer CreateUserToken(String login) throws java.text.ParseException {
            String tokenJson = Token.find("login = ?", login).toJson(true);
        if (Objects.equals(tokenJson, "[\n\n]")) {
            Token tok = new Token();
            tok.set("token", GenerateToken());
            tok.set("login", login);
            tok.defrost();
            tok.saveIt();
            return 201; // CREATED
        }
        return 403; // Forbidden REQUEST
    }

    public static String GenerateToken() {
        // Simple user Token generator, [unique immutable UUID code]
        return UUID.randomUUID().toString();
    }

    public static String GetToken(String login) {
        String userQuery = "login = '" + login + "'";
        String userJson = User.find(userQuery).toJson(true);
        if (!Objects.equals(userJson, "[\n\n]")) {
            List<Token> list = Token.find("login = '" + login + "'");
            return list.get(0).get("token").toString();
        }
        return ("Unauthorized");
    }
    public static String GenerateExpireTime() throws java.text.ParseException {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date now = dataFormat.parse(dataFormat.format(new Date()));
        LocalDateTime yourDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        yourDate = yourDate.plus(Duration.parse("PT3H"));

        String out = dataFormat.format(
                Date.from(
                        yourDate.atZone(
                                ZoneId.systemDefault()).toInstant()
                ));
        return out;
    }
    public static void RefreshToken(String token) throws java.text.ParseException {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date now = dataFormat.parse(dataFormat.format(new Date()));
        LocalDateTime yourDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        yourDate = yourDate.plus(Duration.parse("PT3H"));

        String out = dataFormat.format(
                Date.from(
                        yourDate.atZone(
                                ZoneId.systemDefault()).toInstant()
                ));

        Token tokenQuery = Token.findFirst("token = ?", token);
        tokenQuery.set("expire_time", out);
        tokenQuery.saveIt();
    }
}
