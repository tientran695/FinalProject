const TIME_PRESS_MILI = 500;
const NUMBER_PRESS_VOLUME = 5;
const VOLUME_UP = "VOLUMEUP";
const VOLUME_DOWN = "VOLUMEDOWN";
var lirc = require('lirc_node');
var io = require('socket.io-client')
var socket = io('https://bkhome-test.herokuapp.com/raspi');
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

/**
 * Process data from Server send to
 * @param {JSON} data: example {'remote': 'TCL_TV', 'code': 'VOLUMEUP'}
 */
function IRSend(data){
	switch(data.remote){
		case "TCL_TV":
			TCL_TV(data.code);
		//Other remote:
	}
}
/**
 * Process data of remote TCL_TV
 * @param {String} code : code of TCL_TV remote
 */
function TCL_TV(code){
	if(isNaN(code)){//data is KEY_CONTROL
		if(code == VOLUME_UP || code == VOLUME_DOWN)
			IRSendVolume(code);
		else{
			lirc.irsend.send_once("TCL_TV", "KEY_" + code, function() {
				console.log("Sent TCL_TV KEY_" + code);
			  });
		}
	}else{ //data is KEY_CHANNEL
		IRSendChannel(code);
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
