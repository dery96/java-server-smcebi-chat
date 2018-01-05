
// Server packages

import Helpers.DbConnection;
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
    private static Map<String, String> activeUsersList = new ConcurrentHashMap<>();
    private static Map<String, Map> channelUsernameMap = new ConcurrentHashMap<>();
    //    private static Map<WsSession, String> userChannelSession = new ConcurrentHashMap<>();
    private static Map<WsSession, String> sessionList = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Javalin.create()
                .port(7171)
                .enableStaticFiles("/public")
                .enableCorsForAllOrigins()
                .enableCorsForOrigin("origin")
                .enableCorsForOrigin("*")
                .ws("/chat/*", ws -> {
                    ws.onConnect(session -> {
//                        if (!activeUsersList.values().contains(session.queryParam("name"))) {
////                            activeUsersList.put(session, session.queryParam("name"));
//
//                        }
                        if (session.queryParam("channel") == null) {
                            sessionList.put(session, session.queryParam("name"));
                            onlineUsers("ONLINE_USERS");
                            initBroadcast(session, "CHANNELS", session.queryParam("token"), session.queryParam("name"));
                        } else {
                            if (!channelUsernameMap.containsKey(session.queryParam("channel"))) {
                                // Jezeli Haszmapa tego kanalu juz jest utworzona
                                Map<WsSession, String> usersInChannels = new ConcurrentHashMap<>();
                                usersInChannels.put(session, session.queryParam("name"));

                                String msg = (session.queryParam("name") + " joined the chat");
                                System.out.println(msg);
                                broadcastMessage(usersInChannels, "Server", msg, "MESSAGE");

                                channelUsernameMap.put(session.queryParam("channel"), usersInChannels);
                            } else {
                                if (!channelUsernameMap.get(session.queryParam("channel"))
                                        .values().contains(session.queryParam("name"))) {
                                    // Jezeli sesja uzytkownika nie znajduje sie w Haszmapie Kanału do którego użytkownik
                                    // chce dolaczyć to dodajemy go.
//                                    channels.
                                    channelUsernameMap.get(session.queryParam("channel")).put(session, session.queryParam("name"));

                                    String msg = "Witaj ziemniaczku :*";
                                    System.out.println(msg);
                                    broadcastMessage(channelUsernameMap.get(session.queryParam("channel")), "Server", msg, "MESSAGE");
                                }
                            }
//                            onlineUsers("ONLINE_USERS");
//                            broadcastMessage(  );
//                        }
//                            else {
//                                if (!channelList.values().contains(session.queryParam("channel"))) {
//                                    channelList.put( ???????????,session.queryParam("channel"));
//                                    channelUsernameMap.put(session, session.queryParam("name"));
//                                }
                        }
//                        broadcastMessage("Server", (session.queryParam("name")     + " joined the chat"), "MESSAGE");
                    });
                    ws.onClose((session, status, message) -> {
                        String nickname = sessionList.get(session);
                        sessionList.remove(session);
//                        onlineUsers("ONLINE_USERS");
//                        broadcastMessage("Server", (username + " left the chat"));
                    });
                    ws.onMessage((session, message) -> {
//                        broadcastMessage(channelUsernameMap.get(session.queryParam("channel")), message);
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
                        System.out.println(GetUserSubscribedChannels(token, ctx.formParam("id")));
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

                        ctx.status(201); // SUCCESS
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
//        System.out.println(GetUserSubscribedChannels(token, login));
        if (session.isOpen()) {
            try {
                session.send(
                        new JSONObject()
                                .put("type", type)
                                .put("channels", GetChannels(token)).toString()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Sends a message from one user to all users, along with a list of current usernames
    private static void broadcastMessage(Map<WsSession, String> channel, String sender, String message, String type) {
        channel.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.send(
                        new JSONObject()
                                .put("type", type)
                                .put("author", sender)
                                .put("text", message)
                                .put("date", new SimpleDateFormat("HH:mm:ss").format(new Date()))
                                .put("onlineUsers", channel.values()).toString()

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
