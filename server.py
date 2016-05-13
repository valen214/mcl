#!/usr/bin/env python

from datetime import datetime, timedelta

import http.server
import os
import mimetypes
import urllib
mimetypes.init()

parent_dir = os.path.dirname(os.path.realpath(__file__))

def default_get(self):
    file_path = parent_dir + self.path.split("?")[0]
    if os.path.isfile(file_path):
        self.send_response(200, "OK");
        self.send_header("Content-Type", mimetypes.guess_type(file_path))
        self.end_headers()
        with open(file_path, mode="r") as f:
            self.wfile.write(bytes(f.read(), "utf-8"))
    else:
        self.send_header("Content-Type", "text/plain")
        self.end_headers()
    



GET = {
    "/": default_get,
    "/server.py": default_get
}
POST = {
    
}
HEAD = {
    
}

class CustomHandler(http.server.BaseHTTPRequestHandler):
    """handler for GET, POST, HEAD requests
        as specified in https://docs.python.org/3/
        library/http.server.html#http.server.BaseHTTPRequestHandler
        __init__() is omitted"""
    
    def do_CONNECT(self):
        print("connect received")
    
    def do_GET(self):
        # super(self.__class__, self).do_GET()
        current_time = (datetime.now() + timedelta(hours=8))
        print()
        print("time: {:%d %b,%Y %H:%M:%S}".format(current_time))
        print("ip: {}".format(self.headers["x-forwarded-for"]))
        print("client addr: {}".format(self.client_address))
        print("url: {}, method: {}".format(self.path, self.command))
        print()
        path = self.path.split("?")[0]
        # or use self.path.partition("?")[0]
        GET[path if path in GET else "/"](self)

if __name__ == "__main__":
    address = (os.environ["IP"], int(os.environ["PORT"]))
    print("server.py location: {}/{}".format(parent_dir, __file__))
    print("server started at {0}:{1}".format(*address))
    http.server.HTTPServer(address, CustomHandler).serve_forever();