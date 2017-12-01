
// Server packages

import io.javalin.Javalin;
import io.javalin.embeddedserver.jetty.websocket.WsSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.javalite.activejdbc.Base;
import org.json.JSONObject;

import static Controllers.AccountControllers.ChangeNickname;
import static Controllers.AccountControllers.ChangePassword;
import static Controllers.ChannelController.DeleteChannel;
import static Controllers.SessionController.ExpireSessionTest;
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
import static Controllers.AccountControllers.AccountLogin;
import static Controllers.UserController.getUsers;

public class Server {
    private static Map<WsSession, String> userUsernameMap = new ConcurrentHashMap<>();
    private static int nextUserNumber = 1; // Assign to username for next connecting user

    //    // wiadomosci zapsywac do pliku i jak ktos sie loguje do sesji ma wczytywać mu zawartosc tego
    public static void main(String[] args) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
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
                    ctx.status(AccountLogin(ctx.formParam("login"), ctx.formParam("password")));
                })
                .post("/account/new/", ctx -> {
                    ctx.status(CreateUser(ctx.formParam("login"), ctx.formParam("password"), ctx.formParam("nickname"), ctx.formParam("gender")));
                })
                .post("/account/change/password/", ctx -> {
                    if (!ExpireSessionTest(ctx.formParam("login"), ctx.formParam("password"))) {
                        ctx.status(ChangePassword(ctx.formParam("login"), ctx.formParam("password"), ctx.formParam("newPassword")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/account/change/nickname/", ctx -> {
                    if (!ExpireSessionTest(ctx.formParam("login"), ctx.formParam("password"))) {
                        ctx.status(ChangeNickname(ctx.formParam("login"), ctx.formParam("password"), ctx.formParam("newNickname")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/channel/new/", ctx -> {
                    if (!ExpireSessionTest(ctx.formParam("login"), ctx.formParam("password"))) {
                        ctx.status(CreateChannel(ctx.formParam("name"), ctx.formParam("owner_id"), ctx.formParam("size")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/channel/delete/", ctx -> {
                    if (!ExpireSessionTest(ctx.formParam("login"), ctx.formParam("password"))) {
                        ctx.status(DeleteChannel(ctx.formParam("channel_id"), ctx.formParam("owner_id")));
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/user/all/", ctx -> {
                    if (!ExpireSessionTest(ctx.formParam("login"), ctx.formParam("password"))) {
                        String result = getUsers(ctx.formParam("login"), ctx.formParam("password"));
                        if (!result.equals("FORBIDDEN")){
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
                    if (!ExpireSessionTest(ctx.formParam("login"), ctx.formParam("password"))) {
                        String result = GetChannels(ctx.formParam("login"), ctx.formParam("password"));
                        if (!result.equals("FORBIDDEN")){
                            ctx.result(result);
                            ctx.status(202); // ACCEPTED
                        } else {
                            ctx.status(403); // FORBIDDEN
                        }
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/token/get/", ctx -> {
                    if (!ExpireSessionTest(ctx.formParam("login"), ctx.formParam("password"))) {
                        ctx.result(GetToken(ctx.formParam("login"), ctx.formParam("password")));
                        ctx.status(202); // ACCEPTED
                    } else {
                        ctx.status(401); // UNAUTHORIZED
                    }
                })
                .post("/token/test/", ctx -> {
                    ExpireSessionTest(ctx.formParam("login"), ctx.formParam("password"));
                })
                .post("/token/refresh/", ctx -> {
                    if (AccountLogin(ctx.formParam("login"), ctx.formParam("password")).equals(202)) {
                        RefreshToken(ctx.formParam("login"));
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
