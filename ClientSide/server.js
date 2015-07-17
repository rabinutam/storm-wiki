var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var redis = require("redis");
// redis.createClient() needs redis server
var client = redis.createClient();
redis.debug_mode = false;

app.use(express.static(__dirname + '/public'));
app.use('/bower_components', express.static(__dirname + '/bower_components'));

app.get('/', function(req, res){
	res.sendFile(__dirname + '/public/index.html');
});

io.on('connection', function(socket) {
	socket.on('wiki:client', function(data){
		io.emit('wiki:client', data);
	});
});

// error Handler
// Example -- Error: Redis connection
client.on("error", function (err) {
        console.log("Redis Client Error:\n" + err);
    });

client.on("subscribe", function(channel, count) {
	console.log("client1 subscribed to " + channel + ", " + count + " total subscriptions");
	});
	
client.on("ready", function() {
	console.log("Client connected and ready");
	client.subscribe("wikichangestopology");
	client.subscribe("wikichangespercountry");
	});
	
client.on("message", function(channel, data) {
	console.log("***************************************************");
	console.log("channel = " + channel + ", data =  " + data);
	
	var now = new Date();
	var data_json = JSON.parse(data);
	if (channel == "wikichangestopology") {
		var datax = {time: now.getTime()};
		for (var item in data_json) {
			if (item.indexOf('wikipedia') > -1) {
				datax[item.slice(0,2)] = data_json[item];
			};
		};
		io.emit('wiki:data', datax);
		console.log(datax);
	} else if (channel == "wikichangespercountry") {
		io.emit('wiki:data1', data_json);	
		console.log(data_json);
	};
	});
/*
setInterval(function() {
	var now = new Date();
	var x0 = Math.floor((Math.random() * 50) + 1);
	var y0 = Math.floor((Math.random() * 50) + 1);
	var data0 = {time: now.getTime(),
				'India' : x0,
				'Russia' : y0
				};
	io.emit('wiki:data1', data0);
	console.log(data0)
}, 5000);
*/


http.listen(3000, function(){
	console.log('listening on :: 3000');
});
