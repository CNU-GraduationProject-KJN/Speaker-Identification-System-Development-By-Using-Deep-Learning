
const express = require('express')
const app = express()
const port = 3000;
var bodyParser = require('body-parser');
var fileUpload = require('express-fileupload');
var fs = require('fs');
var mysql = require("mysql");
var server = require('http').createServer(app);
var io = require('socket.io').listen(server);
var JSZip = require('jszip');

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
	
	var obj = req.body;
	var fileName = obj.fileName+".zip";
	var str = obj.file;
	var idx = null;
	console.log(obj);
	
	if(str == null) return res.status(500).json({ result: "ERROR"});
	
	fs.writeFile(__dirname+"/zipfiles/"+fileName, str, {encoding:'base64'}, function(err){
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
				//c.end();
				if(mysqlerr){
					console.error(mysqlerr);
					return res.status(500).json({ result: "ERROR", err: mysqlerr});
            			}
				console.log("idx row : "+ typeof row);
         
            			idx = row[0];
            			idx *= 1;
				idx = idx +1;
				if(isNaN(idx)) idx = 1;
				console.log("idx : "+ idx);
			
				c.query("INSERT INTO member_list (member_key, name, path, idx) VALUES ( ?, ?, ?, ?)",
				[obj.fileName, obj.userName, __dirname+"/zipfiles/"+obj.fileName, idx],
					function(sql_inserr, mysqlres){
						c.end();
						if(sql_inserr){
						console.error(sql_inserr);
						return res.status(500).json({ result: "ERROR", err: sql_inserr});
					}
					io.emit('message', JSON.stringify( [ obj.fileName, 
					JSON.stringify({ headers:req.headers, body: req.body})]));
					return res.json({ result : "OK"});
				});	

		});
		//python function 호출
		        

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
/*app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`)
})*/
