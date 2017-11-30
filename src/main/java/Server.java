
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
import static Controllers.TokenController.getTokens;
import static j2html.TagCreator.article;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.b;
import static j2html.TagCreator.p;
import static j2html.TagCreator.span;

// Models, Controlles
import static Controllers.UserController.CreateUser;
import static Controllers.ChannelController.CreateChannel;
import static Controllers.ChannelController.getChannels;
import static Controllers.AccountControllers.AccountLogin;
import static Controllers.UserController.getUsers;

public class Server {
    private static Map<WsSession, String> userUsernameMap = new ConcurrentHashMap<>();
    private static int nextUserNumber = 1; // Assign to username for next connecting user

    //    // wiadomosci zapsywac do pliku i jak ktos sie loguje do sesji ma wczytywać mu zawartosc tego
    public static void main(String[] args) {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:src/main/resources/public/chat.db", "root", "p@ssw0rd");
//        System.out.println(User.where("id = '1'")); // Wynikowym typem z Quary jest LazyList!
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
                .post("/account/password/change", ctx -> {
                    ctx.status(ChangePassword(ctx.formParam("password"), ctx.formParam("id")));
                })
                .post("/account/nickname/change/", ctx -> {
                    ctx.status(ChangeNickname(ctx.formParam("nickname"), ctx.formParam("id")));
                })
                .post("/channel/new/", ctx -> {
                    ctx.status(CreateChannel(ctx.formParam("name"), ctx.formParam("owner_id"), ctx.formParam("size")));
                })
                .get("/user/all", ctx -> {
                    ctx.result(getUsers());
                })
                .get("/channel/all", ctx -> {
                    ctx.result(getChannels());
                })
                .get("/token/all", ctx -> {
                    ctx.result(getTokens());
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
