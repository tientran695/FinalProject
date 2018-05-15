const PORT = 3000;
const ENTITIES = "channel";
var ip = require('ip');
var express = require("express");
var exp = express();
var app = require("http").createServer(exp);

var socketio = require('socket.io');
var io = socketio(app);

var esp8266 = io.of('/esp8266'); //Namespace của Esp8266
var mobileApp = io.of('/mobileApp'); //Namespace của Mobile App
var raspi = io.of('/raspi'); //Namespace của Raspberry

//Update entities Dialogflow
var request = require('request');

//Get post request from Dialogflow
var myParser = require("body-parser");

app.listen(process.env.PORT || PORT);
console.log("Server running at IP: " + ip.address() + ":" + PORT);

/**
 * Bắt sự kiện khi esp8266 kết nối tới
 */
esp8266.on('connection', function(socket){
    console.log("Esp8266 connected!");
    //Khi esp8266 ngắt kết nối
    socket.on('disconnect', function(){
        console.log("Esp8266 disconnected!");
    })
})

/**
 * Bắt sự kiện khi Mobile App kết nối tới
 */
mobileApp.on('connection', function(socket){
    console.log("Mobile app connected!");
    //Sự kiện App gửi cho Esp8266
    socket.on("APP_ESP", function(data){
        console.log("App send to Esp8266: " + JSON.stringify(data));
        esp8266.emit("APP_ESP", data);
    })
    //Sự kiện App gửi cho Raspberry
    socket.on("APP_RASPI", function(data){
        console.log("App send to Raspberry: " + JSON.stringify(data));
        raspi.emit("APP_RASPI", data);
    })
    //Sự kiện App gửi chuỗi Json chứa số kênh
    socket.on("CHANNEL", function(data){
        console.log("App send Server list channel: " + JSON.stringify(data));
        UpdateEntities(data);
    })
    //Khi Mobile app ngắt kết nối
    socket.on('disconnect', function(){
        console.log("Mobile app disconnected!");
    })
})

/**
 * Bắt sự kiện khi Raspberry kết nối tới
 */
raspi.on('connection', function(socket){
    console.log("Raspberry connected!");
    //Khi Raspberry ngắt kết nối
    socket.on('disconnect', function(){
        console.log("Raspberry disconnected!");
    })
})

//Update entities cho Dialogflow
function UpdateEntities(data){
    // Set the headers
    var headers = {
        'Authorization':    'Bearer 8c766230610a43d08e6c61e5d1f74e1d',
        'Content-Type':     'application/json'
    }
    //Json to update entities, if not create -> create new entities
    var bodyJson =
    [
        {
            "entries": data,
            "name": ENTITIES
        }
    ]
    //Sap xep lai bodyJson
    var body = JSON.stringify(bodyJson);
    // Configure the request
    var options = {
        url: 'https://api.dialogflow.com/v1/entities?v=20150910&lang=en',
        method: 'PUT',
        headers: headers,
        body: body
    }
    // Start the request
    request(options, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            // Print out the response body
            console.log("Dialogflow response: " + JSON.stringify(body));
        }
    })
}

//Get post request from Dialogflow
exp.use(bodyParser.urlencoded({extended: true}));
exp.use(bodyParser.json());
exp.post("/api", function(request, response){
    var channel = request.body.result.parameters.channel;
    var controltv = request.body.result.parameters.controltv;
    console.log("Dialogflow send channel: " + channel);
    console.log("Dialogflow send control: " + controltv);
})