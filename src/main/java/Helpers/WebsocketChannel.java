package Helpers;

import io.javalin.embeddedserver.jetty.websocket.WsSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketChannel {
    public String id;
    public String name;
    public Map<WsSession, String> users = new ConcurrentHashMap<>();
    public List history = new ArrayList();

    public WebsocketChannel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "WebsocketChannel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", users=" + users +
                '}';
    }

    public synchronized void clearConnections() {
//        System.out.println("Users before check connection state: " + users);
        users.keySet().forEach(user -> {
//            System.out.println("User: " + users.get(user) + " " + user.isOpen());
            if (!user.isOpen()) {
                user.close();
                users.remove(user);
            }
        });
    }
}
