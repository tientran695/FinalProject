const TIME_PRESS_MILI = 200;
const NUMBER_PRESS_VOLUME = 5;
const VOLUME_UP = "VOLUMEUP";
const VOLUME_DOWN = "VOLUMEDOWN";
var lirc = require('lirc_node');
var io = require('socket.io-client')
var socket = io('http://192.168.0.101:3000/raspi');
lirc.init();

//Listen event connet to Server
socket.on('connect', function(){
    console.log("Connected to Server!");
})

//Listent event: App send to Raspi
socket.on("APP_RASPI", function(data){
    console.log("Mobile app send data: " + JSON.stringify(data));
    IRSend(data);
})

//Listent event: Raspi disconnect server
socket.on("disconnect", function(){
    console.log("Disconnected to Server!");
})

//Send command to lirc
function IRSend(data){
    if(isNaN(data)){//data is KEY_CONTROL
		if(data == VOLUME_UP || data == VOLUME_DOWN)
			IRSendVolume(data);
		else{
			lirc.irsend.send_once("TCL_TV", "KEY_" + data, function() {
				console.log("Sent TCL_TV KEY_" + data);
			  });
		}
    }else{ //data is KEY_CHANNEL
        IRSendChannel(data);
    }
}

//Send KEY_CHANNEL to lirc
function IRSendChannel(data, index = 0){
	lirc.irsend.send_once("TCL_TV", "KEY_" + data[index]);
	console.log("Sent TCL_TV KEY_" + data[index]);
	index++;
	if(index < data.length){
		setTimeout(function(){
			IRSendChannel(data, index);
		}, TIME_PRESS_MILI);
	}
}

//Send KEY_VOLUME_xxx to lirc
function IRSendVolume(data, index = 0){
	lirc.irsend.send_once("TCL_TV", "KEY_" + data);
	console.log("Sent TCL_TV KEY_" + data);
	index++;
	if(index < NUMBER_PRESS_VOLUME){
		setTimeout(function(){
			IRSendVolume(data, index);
		}, TIME_PRESS_MILI);
	}
}
