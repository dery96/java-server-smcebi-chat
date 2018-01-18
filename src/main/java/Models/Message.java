package Models;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@IdName("id")
@Table("message")
public class Message extends Model {
    static{
        timestampFormat("yyyy.MM.dd HH:mm:ss", "date");
    }

    public static LazyList<Message> getRecent(String channelId, int count) {
        return Message.find("channel_id = ?", channelId).limit(count).load();
    }
}
