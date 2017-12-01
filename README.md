## Java Smcebi Chat Server

this is simple server chat application for university java project using:
* Websocket for chat communication
* WebRTC for microphone and video communication
* HTTP requests

#### Package that we use:

* Javalin (Server that handles websocket and http requests)
* sqlite-jdbc for sqlite database communication
* active-jdbc sqlite3 ORM

#### Api: 

#### POST <br>

`/account/login/ require: login, password` <br>
`/account/new/ require: name, password, nickname gender (M or F)` <br>
`/account/change/password/ require: token, user_id, password, newPassword` <br>
`/account/change/nickname/ require: token, user_id, password, newNickname` <br>
`/user/all/ require: token` <br>
`/channel/all/ require: token` <br>
`/channel/new/ require: token, name, owner_id, size` <br>
`/channel/delete/ require: token, user_id, channel_id` <br>
`/token/test/ require: token (ExpireSessionTest if expired then RefreshToken)` <br>
`/token/refresh/ require: token` <br>

#### GET <br>
