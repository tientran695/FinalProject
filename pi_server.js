/**
 * Server nodejs run on Raspberry
 * for Android App client
 * Control lirc command when App send command
 * */
const PORT = 3000;
const TIME_PRESS_MILI = 200;
const NUMBER_PRESS_VOLUME = 5;
const VOLUME_UP = "VOLUMEUP";
const VOLUME_DOWN = "VOLUMEDOWN";
var http = require('http');
var ip = require('ip');
var socketio = require('socket.io');
var lirc = require('lirc_node');
const delay = require('delay');
var app = http.createServer();
var io = socketio(app);

//Init server
app.listen(process.env.PORT || PORT);
console.log("IP Server: " + ip.address() + ":" + PORT);

//Init Lirc
lirc.init();

//App connect
io.on('connection', function(socket){
    console.log("App connected!");
    //Listen "APPEMIT"
    socket.on('APPEMIT', function(data){
        console.log("Data receive: " + data);
        IRSend(data);
    });

    //App disconnect
    socket.on('disconnect', function(){
        console.log("App disconnect!");
    })
})

//Send command to lirc
function IRSend(data){
    if(isNaN(data)){//data is KEY_CONTROL
		if(data == VOLUME_UP || data == VOLUME_DOWN)
			IRSendVolume(data);
		else{
			lirc.irsend.send_once("PANASONIC", "KEY_" + data, function() {
				console.log("Sent PANASONIC KEY_" + data);
			  });
		}
    }else{ //data is KEY_CHANNEL
        IRSendChannel(data);
    }
}

//Send KEY_CHANNEL to lirc
function IRSendChannel(data, index = 0){
	lirc.irsend.send_once("PANASONIC", "KEY_" + data[index]);
	console.log("Sent PANASONIC KEY_" + data[index]);
	index++;
	if(index < data.length){
		setTimeout(function(){
			IRSendChannel(data, index);
		}, TIME_PRESS_MILI);
	}
}

//Send KEY_VOLUME_xxx to lirc
function IRSendVolume(data, index = 0){
	lirc.irsend.send_once("PANASONIC", "KEY_" + data);
	console.log("Sent PANASONIC KEY_" + data);
	index++;
	if(index < NUMBER_PRESS_VOLUME){
		setTimeout(function(){
			IRSendVolume(data, index);
		}, TIME_PRESS_MILI);
	}
}
