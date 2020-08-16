
const express = require('express')
const app = express()
const port = 3000;
var bodyParser = require('body-parser');
var fileUpload = require('express-fileupload');
var fs = require('fs');
var mysql = require("mysql");
var server = require('http').createServer(app);
var io = require('socket.io').listen(server);
var {PythonShell} = require('python-shell');


mysqlConfig = {
    host: "127.0.0.1",
    port: "3306",
    user: "root",
    password: "kjn",
    database: "cnu_kjn_graduate",
    multipleStatements: true
};

app.use(bodyParser.json({ limit: '1000mb'}));
app.use(fileUpload());
app.use(express.json())

app.post('/upload/:id', function(req,res,next){
	
	var dir_path = "/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/zipfiles/";
	var obj = req.body;
	var fileName = obj.fileName+".zip";
	var str = obj.file;
	var idx = null;
	
	var options = {
		mode: 'text',
		encoding: 'utf8',
		pythonOptions: ['-u'],
		scriptPath: '',
		args: [fileName],
		pythonPath:''
	}	
	
	if(str == null) return res.status(500).json({ result: "ERROR"});
	
	fs.writeFile(dir_path+fileName, str, {encoding:'base64'}, function(err){
		if(err) {
			console.error(err);
			return res.status(500).json({ result : "ERROR", err : err});
		}
		console.log("File Created");
		
		var t = new Date().getTime();
		console.log("Time : "+t);

    		var label_num;
		var c = mysql.createConnection(mysqlConfig);
        
        	c.query("SELECT idx FROM member_list ORDER BY idx DESC LIMIT 1",
			function(mysqlerr, row){
				if(mysqlerr){
					console.error(mysqlerr);
					return res.status(500).json({ result: "ERROR", err: mysqlerr});
            			}
            			if(isNaN(row[0])) idx = 1;
				else{

					idx = row[0].idx;
            				idx *= 1;
					idx = idx +1;
				}
				console.log("idx : "+ idx);
			
				c.query("INSERT INTO member_list (member_key, name, path, idx) VALUES ( ?, ?, ?, ?)",
				[obj.fileName, obj.userName, dir_path+obj.fileName, idx],
					function(sql_inserr, mysqlres){
						c.end();
						if(sql_inserr){
						console.error(sql_inserr);
						return res.status(500).json({ result: "ERROR", err: sql_inserr});
					}
					
					var test = new PythonShell('zip_file_listener.py', options);
					test.on('message', function(message){
						console.log(message);
					});
					io.emit('message', JSON.stringify( [ obj.fileName, 
					JSON.stringify({ headers:req.headers, body: req.body})]));
					return res.json({ result : "OK"});
				});

				return;	

		});
			        

	});
	
});

app.get('/', (req, res) => {
  console.log('web server sending');
  res.sendFile(__dirname+'/index.html');
  
})
io.sockets.on('connection', function(socket){
 console.log('user Connected');
});

server.listen(port);
