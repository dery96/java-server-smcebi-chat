package Controllers;

import Models.Channel;
import Models.User;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

import java.util.List;
import java.util.Objects;

import static Controllers.AccountControllers.AccountLogin;

public class ChannelController {
    public static Integer CreateChannel(String name, String owner_id, String size) {
//        System.out.println("name: " + name + " owner: " + owner_id + " size: " + size);
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String channelJson = Channel.find("name = '" + name + "'").toJson(true);

        if (Objects.equals(channelJson, "[\n\n]")) {
            Channel channel = new Channel();
            channel.set("name", name);
            channel.set("owner_id", owner_id);
            channel.set("size", size);
            channel.saveIt();
            Base.close();
            return 201; // SUCCEED
        }
        Base.close();
        return 401; // UNAUTHORIZED
    }

    public static Integer DeleteChannel(String channel_id, String owner_id) {
        System.out.println("name: " + channel_id + " owner: " + owner_id);
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String channelQuery = "id = '" + channel_id + "' and owner_id = '" + owner_id + "'";
        String channelJson = Channel.find(channelQuery).toJson(true);

        if (!channelJson.equals("[\n\n]")) {
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            Channel channel = Channel.findFirst("id = ?", channel_id);
            channel.delete();
            Base.close();
            return 202; // Accepted SUCCEED in Delete
        }
        Base.close();
        return 401; // UNAUTHORIZED
    }

    public static String getChannels(String login, String password) {
        if (AccountLogin(login, password).equals(202)) {
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            String json = Channel.findAll().toJson(true);
            Base.close();
            return json;
        }
        return ("Unauthorized");
    }
}
