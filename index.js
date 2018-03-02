var express = require("express");
var app = express();
var server = require("http").createServer(app);
var io = require("socket.io").listen(server);
server.listen(process.env.PORT || 3000);

io.sockets.on('connection', function(client){
    console.log("Android app connected");
    client.on('APPEMIT', function(data){
        console.log(data);
    })
})