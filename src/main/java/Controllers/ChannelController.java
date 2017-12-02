package Controllers;

import Helpers.DbConnection;
import Models.Channel;
import Models.Token;
import org.javalite.activejdbc.Base;

import java.util.Objects;

public class ChannelController {
    public static Integer CreateChannel(String name, String owner_id, String size) {
//        System.out.println("name: " + name + " owner: " + owner_id + " size: " + size);
//        DbConnection.BaseConnection();
        String channelJson = Channel.find("name = '" + name + "'").toJson(true);

        if (Objects.equals(channelJson, "[\n\n]")) {
            Channel channel = new Channel();
            channel.set("name", name);
            channel.set("owner_id", owner_id);
            channel.set("size", size);
            channel.saveIt();
            // Base.detach(.*);
            return 201; // SUCCEED
        }
        // Base.detach(.*);
        return 403; // FORBIDDEN
    }

    public static Integer DeleteChannel(String channel_id, String owner_id) {
//        System.out.println("name: " + channel_id + " owner: " + owner_id);
//        DbConnection.BaseConnection();
        String channelQuery = "name = '" + channel_id + "' and owner_id = '" + owner_id + "'";
        String channelJson = Channel.find(channelQuery).toJson(true);

        if (!channelJson.equals("[\n\n]")) {
//            DbConnection.BaseConnection();
            Channel channel = Channel.findFirst("id = ?", channel_id);
            channel.delete();
            // Base.detach(.*);
            return 202; // Accepted SUCCEED in Delete
        }
        // Base.detach(.*);
        return 403; // FORBIDDEN
    }

    public static String GetChannels(String token) {
//        DbConnection.BaseConnection();
        String channelFindAll = Channel.findAll().toJson(true);
        String tokenQuery = Token.find("token = ?", token).toJson(true);
        // Base.detach(.*);

        if (!tokenQuery.equals("[\n\n]")) {
            return channelFindAll;
        }
        return ("FORBIDDEN");
    }
}
