## Java Smcebi Chat Server

this is simple server chat application for university java project using:
* Websocket for chat communication
* WebRTC for microphone and video communication
* HTTP requests

#### Package that we use:

* Javalin (Server that handles websocket and http requests)
* sqlite-jdbc for sqlite database communication
* active-jdbc sqlite3 ORM

##### History

* 30-11-2017 Added: <br>
`/account/login/:login/:password` <br>
`/account/new/:name/:password/:nickname/:gender (user)` <br>
`/account/change/password/:password/:id` <br>
`/user/all` <br>
`/channel/all` <br>
`/channel/new/:name/:owner_id/:size` <br>