
var http = require("http");
var https = require("https");
var fs = require("fs");
var path = require("path");
var url = require("url");
var cprocess = require("child_process");
var net = require("net");

var handler = {
    "GET": {},
    "POST": {},
    "HEAD": {},
    "CONNECT": {}
};
handler["GET"] = {
    "/": default_get,
    "/Test.class": function(req, res){
        returnFile(res, path.join(__dirname, "test/Test.class"));
    },
    "/connection": function(req, res){
        var host = req.content;
    }
};
handler["HEAD"] = {
    "/": function(req, res){
        var pathname = require("url").parse(req.url).pathname;
        if(pathname == "/" || pathname == "/index.html"){
            pathname = "/index.htm";
        }
        // console.log(req.url);
        var file_path = path.join(process.cwd(), pathname);
        if(fs.existsSync(file_path)){
            res.writeHead(200, {
                "Content-Type": (
                    req.url.endsWith(".zip") ? "application/zip" :
                    req.url.endsWith(".png") ? "image/png" :
                    req.url.endsWith(".jpg") ? "image/jpeg" :
                    req.url.endsWith(".js") ? "application/javascript" :
                    req.url.endsWith(".css") ? "text/css" :
                    req.url.endsWith(".pdf") ? "application/pdf" :
                    req.url.endsWith(".jar") ? "application/java-archive" :
                    "text/html; char-set=utf-8"),
                "Content-Length": fs.statSync(file_path).size
            });
            res.end();
            // file.toString("utf8")
        } else{
            res.writeHead(404, {"Content-Length": "0"});
            res.end();
        }
    }
};
handler["POST"] = {
    "/": default_post,
    "/validate": function(req, res, payload){
        console.log("payload: " + payload);
        res.writeHead(200);
        res.end();
    },
    "/command": function(req, res, payload){
        if(payload == "pack"){
            console.log("packing jar requested");
            cprocess.exec("sh bin/pack.sh", {
                        "cwd": __dirname
                    }, (err, stdout, stderr) => {
                        if(err){
                            console.log(`err: ${err}`);
                        }
                        console.log(`stdout: ${stdout}`);
                        console.log();
                        res.writeHead(200);
                        res.end(err ? stderr : "");
            });
        } else if (payload == "test"){
            console.log("compile Test.java requested");
            cprocess.exec("sh bin/test.sh", {
                        "cwd": __dirname
                    }, (err, stdout, stderr) => {
                        if(err){
                            console.log(`err: ${err}`);
                        }
                        console.log(`stdout: ${stdout}`);
                        console.log();
                        res.writeHead(200);
                        res.end(err ? stderr : "");
            });
        } else{
            res.writeHead(200);
            res.end();
        }
    },
    "/push": function(req, res, payload){
        console.log("git add .");
        cprocess.exec("git add .", (err, stdout, stderr) => {
        if(!err){
        console.log(stdout);
        console.log("git commit -m " + payload);
        cprocess.exec("git commit -m " + payload,
        (err, stdout, stderr) => {
        if(!err){
        console.log(stdout);
        console.log("git push");
        cprocess.exec("git push", (err, stdout, stderr) => {
        if(!err) console.log(stdout); 
        });}});}});
        res.writeHead(200);
        res.end();
    }
};
handler["CONNECT"] = {
    "/": function(req, res){
        console.log(req);
        console.log("\n\n\n\n\n");
        res.writeHead(200, {
                "Content-Type": "text/html",
                "Connection": "close"
        });
        // res.end();
    }
};




/*****************/
console.log("server start at %s:%s", process.env.IP, process.env.PORT);
var server = http.createServer(function(req, res){
    var str = "";
    print_req(req);
    // console.log('\033[2J');
    req.on("data", function(chunk){
        if(chunk) str += chunk.toString();
    });
    req.on("end", function(){
        var pathname = url.parse(req.url).pathname;
        if(req.method in handler){
            handler[req.method][(pathname in handler[req.method]) ?
                    pathname : "/"](req, res, str);
        } else{
            console.log("unsupported method");
            res.writeHead(501, {"Content-Type": "text/plain"});
            res.end("unsupported method");
        }
        // code here will be executed every time
    });
});
server.on("connect", (req, cltSocket, head) =>{
    console.log("connect received");
    var srvUrl = url.parse(`http://${req.url}`);
    var srvSocket = net.connect(srvUrl.port, srvUrl.hostname, () =>{
        cltSocket.write('HTTP/1.1 200 Connection Established\r\n' +
                        'Proxy-agent: Node.js-Proxy\r\n' +
                        '\r\n');
        srvSocket.write(head);
        srvSocket.pipe(cltSocket);
        cltSocket.pipe(srvSocket);
    });
});
server.listen(process.env.PORT || 8080, process.env.IP);
/*****************/




function print_req(req){
    console.log();
    // console.log("time: %s;", new Date().toISOString());
    // console.log("time: %s;", new Date(new Date().getTime() +
    //        8 * 3600000).toUTCString());
    console.log("time: %s;", new Date(new Date().getTime() +
            new Date().getTimezoneOffset() * 60000 +
            8 * 3600000).toLocaleString());
    console.log("ip: %s;", req.headers["x-forwarded-for"],
            req.connection.remoteAddress);
    console.log("url: %s, method: %s;",
            req.url.toString(), req.method.toString());
    /*
    console.log("headers: %s;", JSON.stringify(req.headers,
            function(key, value){
                if(key == "cookie") return ".....";
                return value;
            }));
    */
}

function default_get(req, res){
    var pathname = require("url").parse(req.url).pathname;
    if(pathname == "/" || pathname == "/index.html"){
        pathname = "/index.htm";
    }
    // console.log(req.url);
    var file_path = path.join(process.cwd(), pathname);
    if(fs.existsSync(file_path)){
        fs.readFile(file_path, "binary", function(err, file){
            if(err != null){
                
            }
            res.writeHead(200, {
                "Content-Type": (
                    req.url.endsWith(".zip") ? "application/zip" :
                    req.url.endsWith(".png") ? "image/png" :
                    req.url.endsWith(".jpg") ? "image/jpeg" :
                    req.url.endsWith(".js") ? "application/javascript" :
                    req.url.endsWith(".css") ? "text/css" :
                    req.url.endsWith(".pdf") ? "application/pdf" :
                    req.url.endsWith(".jar") ? "application/java-archive" :
                    "text/html; char-set=utf-8"),
                "Content-Length": fs.statSync(file_path).size
            });
            res.write(file, "binary");
            res.end();
            // file.toString("utf8")
        });
    } else{
        res.writeHead(200, {"Content-Type": "text/plain"});
        res.end("cannot find " + req.url);
    }
}
function returnFile(res, file_path){
    fs.readFile(file_path, "binary", function(err, file){
        if(err != null){
            
        }
        res.writeHead(200, {
            "Content-Type": (
                file_path.endsWith(".zip") ? "application/zip" :
                file_path.endsWith(".png") ? "image/png" :
                file_path.endsWith(".jpg") ? "image/jpeg" :
                file_path.endsWith(".js") ? "application/javascript" :
                file_path.endsWith(".css") ? "text/css" :
                file_path.endsWith(".pdf") ? "application/pdf" :
                file_path.endsWith(".jar") ? "application/java-archive" :
                "text/html; char-set=utf-8"),
            "Content-Length": fs.statSync(file_path).size
        });
        res.write(file, "binary");
        res.end();
        // file.toString("utf8")
    });
}
function default_post(req, res, payload){
    res.writeHead(200);
    res.end();
}