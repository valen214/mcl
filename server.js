
var http = require("http");
var https = require("https");
var fs = require("fs");
var path = require("path");
var url = require("url");

var handler = {
    "GET": {},
    "POST": {}
};
handler["GET"] = {
    "/": default_get,
    "/thumbnails": function(req, res){
        print_req(req);
        res.writeHead(200, {"Content-Type": "text/plain; char-set=utf-8"});
        res.write(fs.readdirSync(path.join(
                process.cwd(), "/thumbnails")).toString(), "utf8");
        res.end();
    }
    
};
handler["POST"] = {
    "/": default_post,
    "/images": post_image,
    "/images/": post_image,
    "/delete": function(req, res, payload){
        print_req(req);
        var name = url.parse(decodeURIComponent(
                payload.replace("image=", ""))).path;
        name = name.slice(name.lastIndexOf('/'), name.lastIndexOf('.'));
        console.log("payload: " + payload);
        try{
            fs.unlinkSync(path.join(process.cwd(),
                    "thumbnails" + name + ".jpg"));
            fs.unlinkSync(path.join(process.cwd(),
                    "images" + name + ".png"));
        } catch(e){
            console.log(e);
            res.writeHead(302, {
                    "Location": "http://image-uploader" +
                            "-xvalen214x.c9users.io/"
            });
            res.end();
        }
        console.log("images images%s.png & " +
                "thumbnails%s.jpg deleted", name, name);
        console.log();
        res.writeHead(204);
        res.end();
    }
};
handler["CONNECT"] = {
    "/": function(req, res){
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
    if(req.url.toString().startsWith("/thumbnails/")
    || req.url.toString().startsWith("/images/")){
        console.log("url: %s, method: %s",
                req.url.toString(), req.method.toString());
    } else print_req(req);
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
    print_req(req);
    res.writeHead(404);
    res.end();
}
function post_image(req, res, payload){
    print_req(req);
    if(payload){
        /*
        var arr = decodeURIComponent(payload).split(",", 2);
        console.log("payload: " + arr[0] + ", (data)... ");
        if(arr.length == 2){
            var ext = ".png";
            if((arr[0].indexOf("data:image/jpeg") >= 0)
            || (arr[0].indexOf("ext=jpg") >= 0)){
                ext = ".jpg";
            }
            var buffer = new Buffer(arr[1], "base64");
            fs.writeFileSync(path.join(
                    process.cwd(), "images/image" +
                    new Date().getTime() + ext), buffer);
            console.log("image saved\n");
        } else{
            console.log("invalid data uri");
        }
        /*/
        var arr = payload.split(/[=&]/);
        console.log("payload(0, 35): " + payload.substring(0, 100));
        var buffer1 = new Buffer(decodeURIComponent(
                arr[1].slice(arr[1].indexOf("%2C") + 3)), "base64");
        var buffer2 = new Buffer(decodeURIComponent(
                arr[3].slice(arr[3].indexOf("%2C") + 3)), "base64");
        var time = new Date().toISOString()
                .replace(/[T:]/g, '-').replace(/\..+/, "");
        var first;
        if((arr[0] == "thumbnail") && (arr[2] == "image"))
            first = "thumbnail";
        else if((arr[0] == "image") && (arr[2] == "thumbnail"))
            first = "image";
        
        if(first){
            console.log("payload: %s=(data uris)" +
                    "&%s=(data uris)", arr[0], arr[2]);
            fs.writeFileSync(path.join(process.cwd(),
                    (first == "images" ? "images/" : "thumbnails/") +
                    time + (first == "image" ? ".png" : ".jpg")), buffer1);
            fs.writeFileSync(path.join(process.cwd(), 
                    (first == "images" ? "thumbnails/" : "images/") +
                    time + (first == "image" ? ".jpg" : ".png")), buffer2);
                    
        } else{
            console.log("invalid payload for posting image");
        }
        /*****/
        //*
        res.writeHead(204);
        res.end();
        console.log();
        /*/
        var file_path = path.join(process.cwd(), "index.htm");
        fs.readFile(file_path, "binary", function(err, file){
            if(err != null){}
            res.writeHead(303, {
                "content-type": "text/html",
                "content-length": fs.statSync(file_path).size
            });
            res.write(file, "binary");
            console.log("end request\n");
            res.end();
            // file.toString("utf8")
        });
        /*****/
    } else{
        res.writeHead(200, {"Content-Type": "text/plain; char-set=utf-8"});
        console.log("end request\n");
        res.end("invalid payload for post");
    }
}