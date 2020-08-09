
const express = require('express')
const app = express()
const port = 3000
var bodyParser = require('body-parser');
var fileUpload = require('express-fileupload');
var fs = require('fs');
var mysql = require("mysql");
var http = require('http').Server(app);
var io = require('socket.io').http;

mysqlConfig = {
    host: "168.188.126.212",
    port: 3306,
    user: "root",
    password: "kjn",
    database: "main",
    multipleStatements: true
};

app.use(bodyParser.json({ limit: '1000mb'}));
app.use(fileUpload());

app.post('/upload/:id', function(req,res,next){
    	
	var file = req.files.file; 
	if(file == null) return res.status(500).json({ result: "ERROR"});

	var t = new Date().getTime();
    var fileName = req.params.fileName;
    var label_num;
	
	file.mv(__dirname+"/zipfiles/"+fileName, function(err){
		if(err){
			console.error(err);
			return res.status(500).json({ result : "ERROR", err : err});
		}
		var c = mysql.createConnection(mysqlConfig);
        
        c.query("SELECT label_num FROM zipfiles ORDER BY label_num DESC LIMIT 1",
		function(mysqlerr, row){
			c.end();
			if(mysqlerr){
				console.error(mysqlerr);
				return res.status(500).json({ result: "ERROR", err: mysqlerr});
            }
            
            label_num = row[0];
            label_num *= 1;
		});
        
        c.query("INSERT INTO zipfiles (key, userName, path, label_num) VALUES ( ?, ?, ?, ?)",
		[fileName, req.params.userName, __dirname+"/zipfiles/"+fileName, label_num],
		function(mysqlerr, mysqlres){
			c.end();
			if(mysqlerr){
				console.error(mysqlerr);
				return res.status(500).json({ result: "ERROR", err: mysqlerr});
			}
			io.emit('message', JSON.stringify( [req.params.id, fileName, 
			JSON.stringify({ headers:req.headers, body: req.body})]));
			return res.json({ result : "OK"});
		});
	});
});

app.get('/', (req, res) => {
  res.sendFile(__dirname+'/index.html');
})

app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`)
})
