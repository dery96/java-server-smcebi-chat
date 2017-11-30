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
`/account/change/password/ require: login, password, newPassword` <br>
`/account/change/nickname/ require: login, password, nickname` <br>
`/user/all/ require: login, password` <br>
`/channel/all/ require: login, password` <br>
`/channel/new/ require: name, owner_id, size` <br>
`/channel/delete/ require: login, password, user_id, channel_id` <br>
`/token/ require: login, password ` <br>

#### GET <br>
