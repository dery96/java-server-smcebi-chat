package Controllers;

import Helpers.DbConnection;
import Models.Channel;
import Models.Subscribed;
import Models.Token;
import Models.User;
import org.javalite.activejdbc.Base;

import java.util.List;
import java.util.Objects;

import static Controllers.AccountControllers.GetId;

public class ChannelController {
    public static Integer CreateChannel(String name, String owner_id, String size) {
//        DbConnection.BaseConnection();
        String channelJson = Channel.find("name = '" + name + "'").toJson(true);

        if (Objects.equals(channelJson, "[\n\n]")) {
            Channel channel = new Channel();
            channel.set("name", name);
            channel.set("owner_id", owner_id);
            channel.set("size", size);
            channel.saveIt();
            return 201; // SUCCEED
        }
        return 403; // FORBIDDEN
    }

    public static Integer DeleteChannel(String channel_id, String owner_id) {
//        DbConnection.BaseConnection();
        String channelQuery = "name = '" + channel_id + "' and owner_id = '" + owner_id + "'";
        String channelJson = Channel.find(channelQuery).toJson(true);

        if (!channelJson.equals("[\n\n]")) {
//            DbConnection.BaseConnection();
            Channel channel = Channel.findFirst("id = ?", channel_id);
            channel.delete();
            return 202; // Accepted SUCCEED in Delete
        }
        return 403; // FORBIDDEN
    }

    public static String GetChannels(String token) {
        String channelFindAll = Channel.findAll().toJson(true);
        String tokenQuery = Token.find("token = ?", token).toJson(true);

        if (!tokenQuery.equals("[\n\n]")) {
            return channelFindAll;
        }
        return ("FORBIDDEN");
    }

    public static String GetUserSubscribedChannels(String token, String login) {
        String tokenQuery = Token.find("token = ?", token).toJson(true);
        if (!tokenQuery.equals("[\n\n]")) {
            String user_id = GetId(login);
            List<Subscribed> subscribed_channels = Subscribed.where("user_id = ?", user_id);
            System.out.println(subscribed_channels);
        }
        return ("FORBIDDEN");
    }
}
