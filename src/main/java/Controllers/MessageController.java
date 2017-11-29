package Controllers;

import Models.Channel;
import Models.Message;
import org.javalite.activejdbc.Base;

public class MessageController {
    public static void CreateTextMessage(String user_id, String channel_id, String data) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        Message message = new Message();
        message.set("data", data);
        message.set("user_id", user_id);
        message.set("channel_id", channel_id);
        message.saveIt();
    }
}
