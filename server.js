const PORT = 3000;
var http = require('http');
var ip = require('ip');
var socketio = require('socket.io');
var app = http.createServer();
var io = socketio(app);
var esp8266 = io.of('/esp8266'); //Namespace của Esp8266
var mobileApp = io.of('/mobileApp'); //Namespace của Mobile App
var raspi = io.of('/raspi'); //Namespace của Raspberry

app.listen(process.env.PORT || PORT);
console.log("Server running at IP: " + ip.address() + ":" + PORT);

/**
 * Bắt sự kiện khi esp8266 kết nối tới
 */
esp8266.on('connection', function(socket){
    console.log("Esp8266 connected!");
    socket.on("ESP_APP", function(data){
        console.log("Esp8266 send to App: " + JSON.stringify(data));
        //Gửi data nhận được cho Mobile App
        mobileApp.emit("ESP_APP", data);
    })
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