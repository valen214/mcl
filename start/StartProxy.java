
package start;

import java.io.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;

public class StartProxy implements Runnable
{
    static{
        System.out.println("start.StartProxy referenced");
        // System.setProperty("https.protocols", "TLSv1.2");
    }
    public static final int PORT = 8080;
    public static final int SSL_PORT = 8081;
    
    public static final ServerSocketFactory SERVER_SOCKET_FACTORY =
            ServerSocketFactory.getDefault();
    public static final SocketFactory SOCKET_FACTORY =
            SocketFactory.getDefault();
    
    public static final SSLServerSocketFactory SSL_SERVER_SOCKET_FACTORY =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    public static final SSLSocketFactory SSL_SOCKET_FACTORY =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
            
    public static final ServerSocket SERVER_SOCKET = ((
            java.util.function.Supplier<ServerSocket>)() ->{
                try{ return SERVER_SOCKET_FACTORY.createServerSocket(
                        PORT, 0, InetAddress.getByName(null));
                } catch(IOException ioe){} return null;
            }).get();
            
    // Supplier<T> can be changed to ServerSocket
    public static final SSLServerSocket SSL_SERVER_SOCKET = ((
            java.util.function.Supplier<SSLServerSocket>)() ->{
                try{ return (SSLServerSocket)
                SSL_SERVER_SOCKET_FACTORY.createServerSocket(
                        SSL_PORT, 0, InetAddress.getByName(null));
                } catch(IOException ioe){} return null;
            }).get();
            
    public static final SocketAddress ADDRESS = (
            SERVER_SOCKET != null ?
            SERVER_SOCKET.getLocalSocketAddress() : 
            SSL_SERVER_SOCKET != null ?
            SSL_SERVER_SOCKET.getLocalSocketAddress() : null);
    
    static{
        System.out.println("SERVER_SOCKET address: " +
                SERVER_SOCKET.getLocalSocketAddress());
        System.out.println("SSL_SERVER_SOCKET address: " +
                SSL_SERVER_SOCKET.getLocalSocketAddress());
        System.out.println("address: " +
                SERVER_SOCKET.getInetAddress());
        // SSL_SERVER_SOCKET.setSocketFactory(SSL_SOCKET_FACTORY);
    }
    
    public static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, ADDRESS);
    
    private static boolean RUNNING = false;
    public static void start(){
        if(!RUNNING){
            new Thread(new StartProxy(SERVER_SOCKET)).start();
            new Thread(new StartProxy(SSL_SERVER_SOCKET)).start();
            RUNNING = true;
        }
    }
    
    private final ServerSocket ss;
    public StartProxy(ServerSocket ss){
        this.ss = ss;
    }
    @Override public void run(){
        Console.allow();
        System.out.println("server socket listening for new request");
        try(Socket s = this.ss.accept();
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream()){
            System.out.println("-----     -----");
            System.out.println("request received " + s);
            
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
                            s.setKeepAlive(true);
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
            
            System.out.printf("uri identified: %s:%d\n", host, port);
            
            Console.RESTRICT = false;
            if("launchermeta.mojang.com".equals(host)){
                out.write(("HTTP/1.1 200 Connection Established\r\n" +
                        "\r\n").getBytes());
                SSLSocket channel = (SSLSocket)
                        SSL_SOCKET_FACTORY.createSocket(host, port);
                System.out.println("socket created: " + channel);
                try(InputStream in_ser = channel.getInputStream();
                        OutputStream out_ser = channel.getOutputStream()){
                    /*
                    out_ser.write(("CONNECT / HTTP/1.1\r\n" +
                            "Host: " + host + "\r\n" +
                            "User-Agent: Java/1.8.0_77\r\n" +
                            "Connection: keep-alive\r\n\r\n").getBytes());
                    out_ser.flush();
                    while(( read = in_ser.read(buffer) ) >= 0){
                        System.out.println("server: " +
                                new String(buffer, 0, read));
                    }
                    */
                    StreamPipe sp1 = new StreamPipe(
                            in, out_ser, 1024, (buffer1, read1) ->{
                        System.out.print("client: ");
                        System.out.println(new String(buffer1, 0, read1));
                        return buffer1;
                    });
                    StreamPipe sp2 = new StreamPipe(
                            in_ser, out, 1024, (buffer1, read1) ->{
                        System.out.print("server: ");
                        System.out.println(new String(buffer1, 0, read1));
                        return buffer1;
                    });
                    sp1.start();
                    sp2.start();
                    while(sp1.isAlive() || sp2.isAlive()){
                        try{
                            Thread.sleep(1000);
                        } catch(InterruptedException ie){}
                    }
                }
            }
            
        } catch(Exception e){
            e.printStackTrace();
        }
        Console.remove();
    }
}