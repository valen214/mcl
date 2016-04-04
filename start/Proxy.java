
package start;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class StartProxy implements Runnable
{
    public static final int PORT = 8080;
    
    public static final SSLServerSocketFactory SERVER_SOCKET_FACTORY =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            
    // Supplier<T> can be changed to ServerSocket
    public static final SSLServerSocket SERVER_SOCKET = ((
            java.util.function.Supplier<SSLServerSocket>)() ->{
                try{ return (SSLServerSocket)
                SERVER_SOCKET_FACTORY.createServerSocket(PORT);
                } catch(IOException ioe){} return null;
            }).get();
            
    public static final SocketAddress ADDRESS = (SERVER_SOCKET == null ?
            null : SERVER_SOCKET.getLocalSocketAddress());
            
    public static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, ADDRESS);
    
    private static boolean running = false;
    public static void start(){
        if(!running){
            new Thread(new StartProxy()).start();
            running = true;
        }
    }
    
    @Override public void run(){
        System.out.println("server socket listening for new request");
        try(Socket s = SERVER_SOCKET.accept();
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream()){
            System.out.println("-----     -----");
            System.out.println("request received");
            
            int read;
            byte buffer[] = new byte[1024];
            StringBuilder req = new StringBuilder();
            try{
                while(!req.toString().endsWith("\r\n\r\n")
                && ( read = in.read(buffer) ) >= 0){
                    req.append(new String(buffer, 0, read));
                }
            } catch(IOException ioe){
                ioe.printStackTrace();
            }
            
            String lines[] = req.toString().split("\\r?\\n");
            
            System.out.print("client: ");
            System.out.println(String.join("\nclient: ", lines));
            
            String uri = lines[0].split(" ", 3)[1];
            String host = null;
            int port = 80;
            
            // to be modified
            if(lines[0].startsWith("CONNECT")){
                if(uri.contains(":")){
                    host = uri.substring(0, uri.indexOf(":"));
                    port = Integer.parseInt(
                            uri.substring(uri.indexOf(":") + 1));
                }
                for(String ln : lines){
                    if(ln.startsWith("Host: ") && host == null){
                        host = ln.replaceFirst("Host: ", "");
                    } else if(ln.equals("Connection: keep-alive")
                    || ln.equals("Proxy-Connection: keep-alive")){
                        try{
                            this.client.setKeepAlive(true);
                        } catch(SocketException se){
                            se.printStackTrace();
                        }
                    }
                }
            } else{
                if(uri.startsWith("/")){
                    for(String ln : lines){
                        if(ln.startsWith("Host: ")){
                            host = ln.replaceFirst("Host: ", "") + uri;
                        }
                    }
                } else{
                    host = uri;
                }
                // out.write("HTTP/1.1 501 Not Implemented");
            }
            
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}