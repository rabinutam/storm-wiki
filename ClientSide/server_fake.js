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

setInterval(function() {
	var now = new Date();
	var x0 = Math.floor((Math.random() * 200) + 1);
	var y0 = Math.floor((Math.random() * 50) + 1);
	var z0 = Math.floor((Math.random() * 100) + 1);
	var a0 = Math.floor((Math.random() * 50) + 1);
	var b0 = Math.floor((Math.random() * 10) + 1);
	
	var data0 = {time: now.getTime(),
				'en' : x0,
				'fr' : y0,
				'de' : z0,
				'es' : a0,
				'ru' : b0,
				};
	io.emit('wiki:data', data0);
	
	var data1 = {time: now.getTime(),
				'United States': x0,
				'Russia': y0,
				'India': z0,
				'Brazil': a0,
				'France': b0				
				};				
	io.emit('wiki:data1', data1);
	console.log(data0)
	console.log(data1)
}, 5000);



http.listen(3000, function(){
	console.log('listening on :: 3000');
});
