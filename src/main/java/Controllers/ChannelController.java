package Controllers;

import Models.Channel;
import Models.User;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

import java.util.List;
import java.util.Objects;

public class ChannelController {
    public static Integer CreateChannel(String name, String owner_id, String size) {
        System.out.println("name: " + name + " owner: " + owner_id + " size: " + size);
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        LazyList channelQuery = Channel.where("name = '" + name + "'");
        String channelJson = channelQuery.toJson(true);
        Base.close();

        if (Objects.equals(channelJson, "[\n\n]")) {
            System.out.println("Channel " + name + " is being created");
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            Channel channel = new Channel();
            channel.set("name", name);
            channel.set("owner_id", owner_id);
            channel.set("size", size);
            channel.saveIt();
            return 201; // SUCCEED
        } else {
            System.out.println("Channel " + name + " already exsits");
            return 409;
        }
    }

    public static Integer DeleteChannel(String channel_id, String owner_id) {
        System.out.println("name: " + channel_id + " owner: " + owner_id);
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        LazyList channelQuery = Channel.where("id = '" + channel_id + "' and owner_id = '" + owner_id + "'");
        String channelJson = channelQuery.toJson(true);

        if (channelJson == "[]") {
            return 401;
        } else {
            Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
            Channel channel = Channel.findFirst("id = ?", channel_id);
            channel.delete();
            return 202; // Accepted SUCCEED in Delete
        }
    }

    public static String getChannels() {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
        return Channel.findAll().toJson(true);
    }
}
