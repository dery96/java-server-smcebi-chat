package Controllers;

import Models.Channel;
import Models.Token;
import org.javalite.activejdbc.Base;

import java.util.Objects;

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
        return 403; // FORBIDDEN
    }

    public static Integer DeleteChannel(String channel_id, String owner_id) {
//        System.out.println("name: " + channel_id + " owner: " + owner_id);
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String channelQuery = "name = '" + channel_id + "' and owner_id = '" + owner_id + "'";
        String channelJson = Channel.find(channelQuery).toJson(true);

        if (!channelJson.equals("[\n\n]")) {
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            Channel channel = Channel.findFirst("id = ?", channel_id);
            channel.delete();
            Base.close();
            return 202; // Accepted SUCCEED in Delete
        }
        Base.close();
        return 403; // FORBIDDEN
    }

    public static String GetChannels(String token) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        String channelFindAll = Channel.findAll().toJson(true);
        String tokenQuery = Token.find("token = ?", token).toJson(true);
        Base.close();

        if (!tokenQuery.equals("[\n\n]")) {
            return channelFindAll;
        }
        return ("FORBIDDEN");
    }
}
