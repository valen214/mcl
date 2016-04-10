
var http = require("http");
var https = require("https");
var fs = require("fs");
var path = require("path");
var url = require("url");
var cprocess = require("child_process");

var handler = {
    "GET": {},
    "POST": {},
    "HEAD": {},
    "CONNECT": {}
};
handler["GET"] = {
    "/": default_get
    
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
    "/": default_post
};
handler["CONNECT"] = {
    "/": function(req, res){
        console.log(req);
        console.log("\n\n\n\n\n");
        res.writeHead(200, {
                "Content-Type": "text/html",
                "Connection": "close"
        });
        res.end();
    }
};




/*****************/
console.log("server start at %s", process.env.IP);
http.createServer(function(req, res){
    var str = "";
    print_req(req);
    // console.log('\033[2J');
    req.on("data", function(chunk){
        if(chunk) str += chunk.toString();
    });
    req.on("end", function(){
        var pathname = require("url").parse(req.url).pathname;
        handler[req.method][(pathname in handler[req.method]) ?
                pathname : "/"](req, res, str);
        // code here will be executed every time
    });
}).listen(process.env.PORT, process.env.IP);
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

function default_post(req, res, payload){
    if(payload == "pack"){
        console.log("packing jar requested");
        cprocess.exec("sh pack.sh",
                (err, stdout, stderr) => {
                    if(err){
                        console.log(`err: ${err}`);
                        console.log(`stderr: ${stderr}`);
                        res.writeHead(400);
                    } else{
                        res.writeHead(200);
                    }
                    console.log(`stdout: ${stdout}`);
                    console.log();
                    res.end();
        });
    } else{
        res.writeHead(200);
        res.end();
    }
}