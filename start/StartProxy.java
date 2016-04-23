
package start;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import javax.net.*;
import javax.net.ssl.*;

public class StartProxy implements Runnable
{
    public static final Console CONSOLE =
            Console.getInstance("StartProxy");
    static{
        CONSOLE.println("start.StartProxy referenced");
        // System.setProperty("javax.net.debug", "all");
        System.setProperty("https.protocols",
                "TLSv1.2,TLSv1.1,TLSv1,SSLv3");
        System.setProperty("jdk.tls.client.protocols",
                "TLSv1.2,TLSv1.1,TLSv1,SSLv3");
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
        CONSOLE.println("SERVER_SOCKET address: " +
                SERVER_SOCKET.getLocalSocketAddress());
        CONSOLE.println("SSL_SERVER_SOCKET address: " +
                SSL_SERVER_SOCKET.getLocalSocketAddress());
        CONSOLE.println("address: " +
                SERVER_SOCKET.getInetAddress());
        // SSL_SERVER_SOCKET.setSocketFactory(SSL_SOCKET_FACTORY);
    }
    
    public static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, ADDRESS);
    
    private static boolean RUNNING = false;
    public static void start(){
        if(!RUNNING){
            new Thread(new StartProxy(SERVER_SOCKET)).start();
            // new Thread(new StartProxy(SSL_SERVER_SOCKET)).start();
            RUNNING = true;
        }
    }
    
    private final ServerSocket ss;
    public StartProxy(ServerSocket ss){
        this.ss = ss;
    }
    @Override public void run(){
        CONSOLE.println("server socket listening for new request");
        try(Socket s = this.ss.accept();
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream()){
            CONSOLE.println("-----     -----");
            CONSOLE.println("request received " + s);
            new Thread(this).start();
            
            ByteArrayOutputStream req = new ByteArrayOutputStream();
            {
                int read;
                byte buffer[] = new byte[1024];
                while(!req.toString().endsWith("\r\n\r\n")
                && (read = in.read(buffer)) >= 0){
                    req.write(buffer, 0, read);
                }
            }
            
            String lines[] = req.toString().split("\\r?\\n");
            
            CONSOLE.println("client(1st line): " + lines[0]);
            if(!lines[0].contains(" ")){
                CONSOLE.println(req.toString().endsWith("\r\n\r\n"));
                CONSOLE.print("invalid request: ");
                CONSOLE.println(javax.xml.bind.DatatypeConverter
                        .printHexBinary(lines[0].getBytes()));
            }
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
            
            if(!host.startsWith("http://resources.download.minecraft.net")){
                CONSOLE.print("client: ");
                CONSOLE.println(String.join("\nclient: ",
                        Arrays.copyOfRange(lines, 1, lines.length)));
            }
            
            CONSOLE.printf("uri identified: %s:%d\n", host, port);
            
            if(lines[0].startsWith("CONNECT")){
                try(Socket channel = new Socket(
                        InetAddress.getByName(host), port);
                        InputStream in_ser = channel.getInputStream();
                        OutputStream out_ser = channel.getOutputStream();){
                    
                    CONSOLE.println("socket created: " + channel);
                    out.write(("HTTP/1.1 200 Connection Established\r\n" +
                            "\r\n").getBytes());
                    out.flush();
                    
                    StreamPipe sp1 = new StreamPipe(
                            in, out_ser, 4096, (buffer, read) ->{
                        return Arrays.copyOf(buffer, read);
                    });
                    StreamPipe sp2 = new StreamPipe(
                            in_ser, out, 4096, (buffer, read) ->{
                        return Arrays.copyOf(buffer, read);
                    });
                    sp1.start();
                    sp2.start();
                    /*
                    while(sp1.isAlive() || sp2.isAlive()){
                        try{
                            Thread.sleep(1000);
                        } catch(InterruptedException ie){
                            ie.printStackTrace();
                        }
                    }
                    */
                    sp1.join();
                    if(sp1.isAlive() || sp2.isAlive()){
                        CONSOLE.println("unexpected end of thread");
                        CONSOLE.println("*****");
                    }
                }
            } else if(lines[0].startsWith("GET")){
                URLConnection con = new URL(uri).openConnection();
                InputStream in_ser = con.getInputStream();
                out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                new StreamPipe(in_ser, out, 1024).run();
                in_ser.close();
            } else{
                CONSOLE.println("unsupported http method");
                CONSOLE.println(lines[0]);
            }
            
        } catch(Exception e){
            e.printStackTrace();
        }
        CONSOLE.println("request ended");
    }
}