
// Server packages

import Helpers.DbConnection;
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
import static Controllers.ChannelController.DeleteChannel;
import static Controllers.SessionController.ExpireSessionTest;
import static Controllers.TokenController.CreateUserToken;
import static Controllers.TokenController.GetToken;
import static Controllers.TokenController.RefreshToken;
import static j2html.TagCreator.article;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.b;
import static j2html.TagCreator.p;
import static j2html.TagCreator.span;

// Models, Controlles
import static Controllers.UserController.CreateUser;
import static Controllers.ChannelController.CreateChannel;
import static Controllers.ChannelController.GetChannels;
import static Controllers.UserController.getUsers;

public class Server {
    private static Map<WsSession, String> userUsernameMap = new ConcurrentHashMap<>();
    private static int nextUserNumber = 1; // Assign to username for next connecting user

    //    // wiadomosci zapsywac do pliku i jak ktos sie loguje do sesji ma wczytywać mu zawartosc tego
    public static void main(String[] args) {
        Javalin.create()
                .port(7171)
                .enableStaticFiles("/public")
                .ws("/chat", ws -> {
                    ws.onConnect(session -> {
                        String username = "User" + nextUserNumber++;
                        userUsernameMap.put(session, username);
                        broadcastMessage("Server", (username + " joined the chat"));
                    });
                    ws.onClose((session, status, message) -> {
                        String username = userUsernameMap.get(session);
                        userUsernameMap.remove(session);
                        broadcastMessage("Server", (username + " left the chat"));
                    });
                    ws.onMessage((session, message) -> {
                        broadcastMessage(userUsernameMap.get(session), message);
                    });
                })
                .post("/account/login/", ctx -> {
                    DbConnection.BaseConnection();
                    Integer accountLogin = AccountLogin(ctx.formParam("login"), ctx.formParam("password"));
                    if (accountLogin.equals(202)) {

                        String token = GetToken(ctx.formParam("login"));
                        String id = GetId(ctx.formParam("login"));

                        RefreshToken(token);
                        JSONObject obj = new JSONObject();
                        obj.put("token", token);
                        obj.put("id", id);

                        ctx.result(obj.toString());
                        ctx.status(202); // ACCEPTED
                    }
                    ctx.status(accountLogin); // UNAUTHORIZED
                })
                .post("/account/new/", ctx -> {
                    DbConnection.BaseConnection();
                    Integer createStatus = CreateUser(ctx.formParam("login"), ctx.formParam("password"), ctx.formParam("nickname"), ctx.formParam("gender"));
                    if (createStatus == 201) {
                        ctx.status(CreateUserToken(ctx.formParam("login")));
                    } else {
                        ctx.status(createStatus);
                    }
                })
                .post("/account/change/password/", ctx -> {
                    DbConnection.BaseConnection();
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(ChangePassword(ctx.formParam("id"), ctx.formParam("password"), ctx.formParam("newPassword")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/account/change/nickname/", ctx -> {
                    DbConnection.BaseConnection();
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(ChangeNickname(ctx.formParam("id"), ctx.formParam("password"), ctx.formParam("newNickname")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/channel/new/", ctx -> {
                    DbConnection.BaseConnection();
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(CreateChannel(ctx.formParam("name"), ctx.formParam("owner_id"), ctx.formParam("size")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/channel/delete/", ctx -> {
                    DbConnection.BaseConnection();
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(DeleteChannel(ctx.formParam("channel_id"), ctx.formParam("owner_id")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/user/all/", ctx -> {
                    DbConnection.BaseConnection();
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
                    DbConnection.BaseConnection();
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
                    DbConnection.BaseConnection();
                    ExpireSessionTest(ctx.formParam("token"));
                })
                .post("/token/refresh/", ctx -> {
                    DbConnection.BaseConnection();
                    if (!ExpireSessionTest(ctx.formParam("token"))) {
                        RefreshToken(ctx.formParam("token"));
                        ctx.status(202); // ACCEPTED
                    } else {
                        ctx.status(403); // FORBIDDEN
                    }
                })
                .start();
    }

    // Sends a message from one user to all users, along with a list of current usernames
    private static void broadcastMessage(String sender, String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.send(
                        new JSONObject()
                                .put("userMessage", createHtmlMessageFromSender(sender, message))
                                .put("userlist", userUsernameMap.values()).toString()
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
