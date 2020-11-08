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

var resultJSON = null;

var flag = false;

mysqlConfig = {
    host: "127.0.0.1",
    port: "3306",
    user: "root",
    password: "kjn",
    database: "cnu_kjn_graduate",
    multipleStatements: true
};

app.use(bodyParser.json({ limit: '10000000mb'}));
app.use(bodyParser.urlencoded({limit: '50000000', extended: true, parameterLimit:'500000'}));
app.use(fileUpload());
app.use(express.json())

app.post('/upload/:id', function(req,res,next){
	
	var dir_path = "/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/zipfiles/";
	var target_path = "/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/unzipfiles/";
	
	var obj = req.body;
	var fileName = obj.fileName+".zip";
	var str = obj.file;
	var idx = null;
	
	var options = {
		mode: 'text',
		encoding: 'utf8',
		pythonOptions: ['-u'],
		scriptPath: '/home/una/Music/Speaker-Identification-System-Development-By-Using-Deep-Learning/System/SpeakerIdentificationSystem',
		args: [obj.fileName, 1],
		pythonPath:''
	}	
	
	if(str == null) return res.status(500).json({ result: "ERROR"});
	
	console.log("Upload Files from "+obj.userName);
	fs.writeFile(dir_path+fileName, str, {encoding:'base64'}, function(err){
		if(err) {
			console.error(err);
			return res.status(500).json({ result : "ERROR", err : err});
		}

		console.log("File Created : "+ obj.fileName);
		

		var c = mysql.createConnection(mysqlConfig);
        
        	c.query("SELECT idx FROM member_list ORDER BY idx DESC LIMIT 1",
			function(mysqlerr, row){
				if(mysqlerr){
					console.error(mysqlerr);
					return res.status(500).json({ result: "ERROR", err: mysqlerr});
            			}
				if(row[0] == null) idx = 1;
				else{

					idx = row[0].idx;
            				idx *= 1;
					idx = idx +1;
				}
				console.log("idx : "+ idx);
			
				c.query("INSERT INTO member_list (member_key, name, path, idx) VALUES ( ?, ?, ?, ?)",
				[obj.fileName, obj.userName, target_path+obj.fileName, idx],
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
					function checkFlag() {
    						if(flag == false) {
       							setTimeout(checkFlag, 100); /* this checks the flag every 100 milliseconds*/
    						} else {
     							 /* do something*/
    							console.log("Send result JSON");
							io.emit('message', JSON.stringify( [ obj.fileName, 
							JSON.stringify({ headers:req.headers, body: req.body})]));
							res.json(resultJSON);
							flag = false;
							
							//test.childProcess.kill('SIGINT');
						}	
					}
					checkFlag();
				
				});

				return;	

		});
			        

	});
	
	
});
app.post('/upload/result/:id', function(req, res, next){
	
	console.log("Receive POST upload result");
	resultJSON = req.body;
	flag = true;

});
app.post('/identify/:id', function(req,res,next){
	
	var dir_path = "/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/val/zipfiles/";
	var model_path = "/home/una/Music/Speaker-Identification-System-Development-By-Using-Deep-Learning/System/SpeakerIdentificationSystem/"
	var obj = req.body;
	var fileName = obj.fileName+".zip";
	var str = obj.file;
	var idx = null;
	
	var options = {
		mode: 'text',
		encoding: 'utf8',
		pythonOptions: ['-u'],
		scriptPath: '/home/una/Music/Speaker-Identification-System-Development-By-Using-Deep-Learning/System/SpeakerIdentificationSystem/',
		args: [obj.fileName, 2],
		pythonPath:''
	}	
	
	if(str == null) return res.status(500).json({ result: "ERROR"});
	
	console.log("Identify User");
	fs.writeFile(dir_path+fileName, str, {encoding:'base64'}, function(err){
		if(err) {
			console.error(err);
			return res.status(500).json({ result : "ERROR", err : err});
		}
		console.log("File Created");
		if(fs.existsSync(model_path)){
			// train
			var test = new PythonShell('zip_file_listener.py', options);
			test.on('message', function(message){
				console.log(message);
			
			});

			function checkFlag() {
    				if(flag == false) {
       					setTimeout(checkFlag, 100); /* this checks the flag every 100 milliseconds*/
    				} else {
    					console.log("Send result JSON");
					io.emit('message', JSON.stringify( [ obj.fileName, 
					JSON.stringify({ headers:req.headers, body: req.body})]));
					res.json(resultJSON);
					flag = false;
					//test.childProcess.kill('SIGINT');
				
				}	
			}
			checkFlag();
		}else{
			return res.status(503).json({result : "TRAINING"});
		}

	});

	
});
app.post('/identify/result/:id', function(req, res, next){
	
	console.log("Receive POST identify result");
	resultJSON = req.body;
	flag = true;

});
app.post('/modifyName/:id', function(req,res,next){
	
	var obj = req.body;
	var dir_path = "/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/unzipfiles/";

	var fileName = obj.fileName;
	var changedName = obj.changedName;
	
		
	if(!fs.existsSync(dir_path+fileName)) return res.status(501).json({result : "ERROR"});
	console.log("Modify User Name "+ changedName +" origini key : "+fileName);
		
	var c = mysql.createConnection(mysqlConfig);
        
	c.query("UPDATE member_list SET name = ? WHERE member_key = ?",
		[changedName, fileName],
		function(sql_inserr, mysqlres){
			c.end();
			if(sql_inserr){
				console.error(sql_inserr);
				return res.status(500).json({ result: "ERROR", err: sql_inserr});
			}
					
			io.emit('message', JSON.stringify( [ obj.fileName, 
			JSON.stringify({ headers:req.headers, body: req.body})]));
			return res.json({ id : obj.fileName ,result : "OK"});
		});

	
});

app.post('/modifyVoice/:id', function(req,res,next){
	
	var dir_path = "/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/zipfiles/";
	var target_path = "/home/una/Audio_For_Speaker-Identification-System-Development-By-Using-Deep-Learning/train/unzipfiles/";
	
	var obj = req.body;
	var fileName = obj.fileName+".zip";
	var str = obj.file;
	var idx = null;
	var options = {
		mode: 'text',
		encoding: 'utf8',
		pythonOptions: ['-u'],
		scriptPath: '/home/una/Music/Speaker-Identification-System-Development-By-Using-Deep-Learning/System/SpeakerIdentificationSystem/',
		args: [obj.fileName, 3],
		pythonPath:''
	}	
	
	if(str == null) return res.status(500).json({ result: "ERROR"});
	console.log("Modify User Voice");
	fs.writeFile(dir_path+fileName, str, {encoding:'base64'}, function(err){
		if(err) {
			console.error(err);
			return res.status(500).json({ result : "ERROR", err : err});
		}

		console.log("File Created : "+ obj.fileName);
		

		var test = new PythonShell('zip_file_listener.py', options);
		test.on('message', function(message){
			console.log(message);
		});
		function checkFlag() {
    			if(flag == false) {
       				setTimeout(checkFlag, 100); /* this checks the flag every 100 milliseconds*/
    			} else {
    				console.log("Send result JSON");
				io.emit('message', JSON.stringify( [ obj.fileName, 
				JSON.stringify({ headers:req.headers, body: req.body})]));
				res.json(resultJSON);
				flag = false;
				//test.childProcess.kill('SIGINT');

			}	
		}
		checkFlag();

		return; 

	});
	
	
});
app.post('/modifyVoice/result/:id', function(req, res, next){
	
	console.log("Receive POST modify voice result");
	resultJSON = req.body;
	flag = true;

});
app.post('/delete/:id', function(req,res,next){
	
	var obj = req.body;
	var options = {
		mode: 'text',
		encoding: 'utf8',
		pythonOptions: ['-u'],
		scriptPath: '/home/una/Music/Speaker-Identification-System-Development-By-Using-Deep-Learning/System/SpeakerIdentificationSystem/',
		args: [obj.fileName],
		pythonPath:''
	}	
	
	console.log("Delete User"+obj.fileName);

	var test = new PythonShell('delete_user.py', options);
	
	test.on('message', function(message){
		console.log(message);
	});
	function checkFlag() {
    		if(flag == false) {
       			setTimeout(checkFlag, 100); /* this checks the flag every 100 milliseconds*/
    		} else {
    			console.log("Send result JSON");
			io.emit('message', JSON.stringify( [ obj.fileName, 
			JSON.stringify({ headers:req.headers, body: req.body})]));
			res.json(resultJSON);
			flag = false;
			//test.childProcess.kill('SIGINT');

		}		
	}
	checkFlag(); 
	
	
});
app.post('/delete/result/:id', function(req, res, next){
	
	console.log("Receive POST delete result");
	resultJSON = req.body;
	flag = true;

});

app.get('/', (req, res) => {
  console.log('web server sending');
  res.sendFile(__dirname+'/index.html');
  
})
io.sockets.on('connection', function(socket){
 console.log('user Connected');
});

server.listen(port);
