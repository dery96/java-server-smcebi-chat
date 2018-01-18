
// Server packages

import Helpers.Connections;
import Helpers.DbConnection;
import Helpers.WebsocketChannel;
import Models.User;
import io.javalin.Javalin;
import io.javalin.embeddedserver.jetty.websocket.WsSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.javalite.activejdbc.Base;
import org.json.JSONObject;


import static Controllers.AccountControllers.*;
import static Controllers.ChannelController.*;
import static Controllers.SessionController.CloseSession;
import static Controllers.SessionController.ExpireSessionTest;
import static Controllers.TokenController.CreateUserToken;
import static Controllers.TokenController.GetToken;
import static Controllers.TokenController.RefreshToken;

// Models, Controlles
import static Controllers.UserController.CreateUser;
import static Controllers.UserController.getUsers;
import static j2html.TagCreator.*;

public class Server {
    private static Map<String, String> activeUsersList = new ConcurrentHashMap<>(); //
    private static Map<WsSession, String> sessionList = new ConcurrentHashMap<>();
    private static Map<String, WebsocketChannel> channelMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Javalin.create()
                .port(7171)
                .enableStaticFiles("/public")
                .enableCorsForAllOrigins()
                .enableCorsForOrigin("origin")
                .enableCorsForOrigin("*")
                .ws("/chat/*", ws -> {
                    ws.onConnect(session -> {
                        String channel_id = session.queryParam("id");
                        String channel_name = session.queryParam("channel_name");

                        String username = session.queryParam("username");
                        String token = session.queryParam("token");

                        if (channel_id == null) {
                            Connections.repairClosed(sessionList);
                            if (!sessionList.containsValue(username)) {
                                sessionList.put(session, username);
                            }


                            initBroadcast(session, "CHANNELS", token, username);
                            onlineUsers("ONLINE_USERS");
                        } else {
                            if (!channelMap.containsKey(channel_id)) {
//                                System.out.println("Channel not exsists must create instance of that Channel");
                                WebsocketChannel channel = new WebsocketChannel(channel_id, channel_name);
                                channelMap.put(channel_id, channel);
                            }
                            Connections.repairClosed(channelMap.get(channel_id).users);
                            // Check if User is declared in Channel
                            if (!channelMap.get(channel_id).users.containsValue(username)) {
//                                System.out.println("User is not declared in channel id:" + channel_id + " username: " + username);
                                channelMap.get(channel_id).users.put(session, username);
                            }
//                            else {
//                                // User is in connected in channel
//                                System.out.println("User is in channel");
//                                System.out.println("userMap of this channel" + channelMap.get(channel_id).users);
//                            }
//                            System.out.println("----------------------------------");

                            broadcastMessage(
                                    channelMap.get(channel_id).users,
                                    channel_id,
                                    "Server",
                                    session.queryParam("username") + " joined the chat",
                                    "MESSAGE",
                                    channelMap.get(channel_id).history.toString()
                            );
                        }
                    });
                    ws.onClose((session, status, message) -> {
                        String nickname = sessionList.get(session);
                        sessionList.remove(session);
                        onlineUsers("ONLINE_USERS");
                    });
                    ws.onMessage((session, message) -> {

                        JSONObject obj = new JSONObject(message);
                        String channel_id = obj.getString("channelId");
                        String username = obj.getString("username");
                        String text = obj.getString("message");


                        broadcastMessage(
                                channelMap.get(channel_id).users,
                                channel_id,
                                username,
                                text,
                                "MESSAGE",
                                channelMap.get(channel_id).history.toString()
                        );

//                        channelMap.get(channel_id).history.add()
                    });
                })
                .post("/account/login/", ctx -> {
                    ctx.header("Access-Control-Allow-Headers", "*");
                    ctx.header("Access-Control-Allow-Origin", "*");
                    ctx.header("Access-Control-Allow-Credentials", "true");
                    ctx.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    Integer accountLogin = AccountLogin(ctx.formParam("login"), ctx.formParam("password"));
                    if (accountLogin.equals(202)) {

                        String token = GetToken(ctx.formParam("login"));
                        String id = GetId(ctx.formParam("login"));
                        User userData = GetUser(ctx.formParam("login"), ctx.formParam("password"));

                        RefreshToken(token);
//                        System.out.println(GetUserSubscribedChannels(token, ctx.formParam("id")));
                        JSONObject obj = new JSONObject();
                        obj.put("token", token);
                        obj.put("id", id);
                        obj.put("nickname", userData.get("nickname"));
                        obj.put("gender", userData.get("gender"));
                        obj.put("registration_date", userData.get("registration_date"));
                        obj.put("login", userData.get("login"));
                        obj.put("subscribedChannels", GetUserSubscribedChannels(token, id));

                        activeUsersList.put(token, (String) userData.get("nickname"));

                        ctx.result(obj.toString());
                        ctx.status(202); // ACCEPTED
                    }
                    ctx.status(accountLogin); // UNAUTHORIZED
                })
                .post("/account/logout/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    if (CloseSession(ctx.formParam("token"))) {
                        activeUsersList.remove(ctx.formParam("token"));
                        onlineUsers("ONLINE_USERS");

                        ctx.status(201); // CREATED
                    } else {
                        ctx.status(401);
                    }
                })
                .post("/account/new/", ctx -> {
                    ctx.header("Access-Control-Allow-Headers", "*");
                    ctx.header("Access-Control-Allow-Credentials", "true");
                    ctx.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    Integer createStatus = CreateUser(ctx.formParam("login"), ctx.formParam("password"), ctx.formParam("nickname"), ctx.formParam("gender"));
                    if (createStatus == 201) {
                        ctx.status(CreateUserToken(ctx.formParam("login")));
                    } else {
                        ctx.status(createStatus);
                    }
                })
                .post("/account/change/password/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(ChangePassword(ctx.formParam("id"), ctx.formParam("password"), ctx.formParam("newPassword")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/account/change/nickname/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(ChangeNickname(ctx.formParam("id"), ctx.formParam("password"), ctx.formParam("newNickname")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/channel/new/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(CreateChannel(ctx.formParam("name"), ctx.formParam("owner_id"), ctx.formParam("size")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/channel/subscribe/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));

                        SubscribeChannel(ctx.formParam("channel_id"), ctx.formParam("user_id"));
                        String subscribe_channels = GetUserSubscribedChannels(ctx.formParam("token"), ctx.formParam("user_id"));

//                        JSONObject obj = new JSONObject();
//                        obj.put("data", subscribe_channels);

                        ctx.result(subscribe_channels);
                        ctx.status(201);
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/channel/delete/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(DeleteChannel(ctx.formParam("channel_id"), ctx.formParam("owner_id")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/user/all/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        String result = getUsers(ctx.formParam("token"));
                        if (!result.equals("FORBIDDEN")) {
                            ctx.result(result);
                            ctx.status(202); // ACCEPTED
                        } else {
                            ctx.status(403); // FORBIDDEN
                        }
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/channel/all/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        String result = GetChannels(ctx.formParam("token"));
                        if (!result.equals("FORBIDDEN")) {
                            ctx.result(result);
                            ctx.status(202); // ACCEPTED
                        } else {
                            ctx.status(403); // FORBIDDEN
                        }
                     } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/token/test/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    ExpireSessionTest(ctx.formParam("token"));
                })
                .post("/token/refresh/", ctx -> {
                    if (!Base.hasConnection()) {
                        DbConnection.BaseConnection();
                    }
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(202); // ACCEPTED
                    } else {
                        ctx.status(403); // FORBIDDEN
                    }
                })
                .start();
    }

    private static void onlineUsers(String type) {
        Connections.repairClosed(sessionList);
        sessionList.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.send(
                        new JSONObject()
                                .put("type", type)
                                .put("onlineUsers", activeUsersList.values()).toString()

                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void initBroadcast(WsSession session, String type, String token, String login) {
        if (!Base.hasConnection()) {
            DbConnection.BaseConnection();
        }
        if (session.isOpen()) {
            try {
                session.send(
                        new JSONObject()
                                .put("type", type)
                                .put("channels", GetChannels(token))
                                .put("onlineUsers", activeUsersList.values()).toString()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Sends a message from one user to all users, along with a list of current usernames
    private static void broadcastMessage(Map<WsSession, String> channel, String channel_id, String sender, String message, String type, String history) {
        Connections.repairClosed(channelMap.get(channel_id).users);
        channel.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.send(
                        new JSONObject()
                                .put("channelId", channel_id)
                                .put("type", type)
                                .put("author", sender)
                                .put("text", message)
                                .put("date", new SimpleDateFormat("HH:mm:ss").format(new Date()))
                                .put("history", history)
                                .put("onlineUsers", activeUsersList.values())
                                .put("onlineChannelUsers", channel.values()).toString()

                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Builds a HTML element with a sender-name, a message, and a timestamp
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article(
                b(sender + " says:"),
                span(attrs(".timestamp"), new SimpleDateFormat("HH:mm:ss").format(new Date())),
                p(message)
        ).render();
    }

}
