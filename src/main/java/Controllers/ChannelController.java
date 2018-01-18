package Controllers;

import Models.Channel;
import Models.Subscribe;
import Models.Token;
import org.javalite.activejdbc.LazyList;

import java.util.List;
import java.util.Objects;

import static Controllers.AccountControllers.GetId;

public class ChannelController {
    public static Integer CreateChannel(String name, String owner_id, String size) {
        String channelJson = Channel.find("name = '" + name + "'").toJson(true);

        if (Objects.equals(channelJson, "[\n\n]")) {
            Channel channel = new Channel();
            channel.set("name", name);
            channel.set("owner_id", owner_id);
            channel.set("size", size);
            channel.saveIt();
            return 201; // CREATED
        }
        return 403; // FORBIDDEN
    }

    public static Integer SubscribeChannel(String channel_id, String user_id) {
        String subscribeQuery = "channel_id = '" + channel_id + "' and user_id = '" + user_id + "'";
        String subscribeJson = Subscribe.find(subscribeQuery).toJson(true);

        if (Objects.equals(subscribeJson, "[\n\n]")) {
            Subscribe subscribe = new Subscribe();
            subscribe.set("channel_id", channel_id);
            subscribe.set("user_id", user_id);
            subscribe.saveIt();
            return 201; // CREATED
        }
        return 403; // FORBIDDEN
    }

    public static Integer DeleteChannel(String channel_id, String owner_id) {
        String channelQuery = "name = '" + channel_id + "' and owner_id = '" + owner_id + "'";
        String channelJson = Channel.find(channelQuery).toJson(true);

        if (!channelJson.equals("[\n\n]")) {
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

    public static String GetUserSubscribedChannels(String token, String user_id) {
        String tokenQuery = Token.find("token = ?", token).toJson(true);
        if (!tokenQuery.equals("[\n\n]")) {
            LazyList<Subscribe> subscribe_channels = Subscribe.where("user_id = ?", user_id);
            return subscribe_channels.toJson(false, "channel_id", "user_id");
        }
        return ("FORBIDDEN");
    }
}
