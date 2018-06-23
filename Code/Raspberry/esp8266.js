var Gpio = require('onoff').Gpio;
var replay = new Gpio(24, 'out');
var io = require('socket.io-client');
var socket = io('https://bkhome-test.herokuapp.com/esp8266');

//Listen event connet to Server
socket.on('connect', function(){
    console.log("Connected to Server!");
})

socket.on('connect_error', (error) => {
    console.log("Connect error: " + error);
});

socket.on('connect_timeout', (timeout) => {
    console.log("Connect timeout: " + timeout);
});

//Listent event: App send to Esp8266
socket.on("UPDATE", function(){
    console.log("Mobile app send data: UPDATE");
    var status = replay.readSync();
    socket.emit("ESP_APP", {"Status": status});
})

socket.on("APP_ESP", function(data){
    console.log("Mobile app send control: " + JSON.stringify(data));
    var status = data.Status;
    replay.writeSync(status);
})

//Listent event: Esp8266 disconnect server
socket.on("disconnect", function(){
    console.log("Disconnected to Server!");
})