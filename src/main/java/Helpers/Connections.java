package Helpers;

import io.javalin.embeddedserver.jetty.websocket.WsSession;

import java.util.Map;

public class Connections {
    public static void repairClosed(Map<WsSession, String > hashMap) {
        hashMap.keySet().forEach(user -> {
//            System.out.println("User: " + users.get(user) + " " + user.isOpen());
            if (!user.isOpen()) {
                user.close();
                hashMap.remove(user);
            }
        });
    }
}
