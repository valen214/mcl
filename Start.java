
import java.awt.Font;
import java.lang.instrument.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.net.ssl.*;
import java.security.*;
import javassist.*;

/*
extends OutputStream removed as implementation of
java.net.Proxy is required
using invocationhandler for the implementation of redirecting system.out
*/

public class Start extends PrintStream
implements java.awt.event.WindowListener
// , ClassFileTransformer
// , InvocationHandler
{
    /*
    private static volatile Instrumentation INST = null;
    public static void premain(String args, Instrumentation inst){
        INST = inst;
        INST.addTransformer(THIS);
    }
    public byte[] transform(ClassLoader cl, String name, Class c,
            ProtectionDomain pd, byte buffer[])
            throws IllegalClassFormatException{
        String c1 = "com.mojang.authlib.HttpAuthenticationService";
        if(name.equals(c1.replace('.', '/'))){
            try{
                ClassPool cp = ClassPool.getDefault();
                CtClass cc = cp.get(c1);
                CtMethod m = cc.getDeclaredMethod("performGetRequest");
                m.insertBefore("System.out.println(url)");
                buffer = cc.toBytecode();
                cc.detach();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return buffer;
    }
    */
    
    private static final URL LAUNCHER_URL = ((
            java.util.function.Supplier<URL>)() ->{
                try{ return new URL("https://s3.amazonaws.com/" +
                        "Minecraft.Download/launcher/launcher.pack.lzma");
                } catch(MalformedURLException murle){
                    murle.printStackTrace();
                }
                return null;
            }).get();
    
    public static final String CUSTOM_URL =
            "https://image-uploader-xvalen214x.c9users.io/";
    public static final Start THIS = new Start();
    public static final PrintStream OUT = System.out;
    public static final PrintStream ERR = System.err;
    
    //this
    public static final URL START_URL =
            Start.class.getProtectionDomain()
            .getCodeSource().getLocation();
    // this
    public static final File START = ((
            java.util.function.Supplier<File>)() ->{
                try{ return new File(java.net.URLDecoder.decode(
                        Start.START_URL.getFile(), "utf-8"));
                } catch(java.io.UnsupportedEncodingException uee){
                    uee.printStackTrace();
                }
                return null;
            }).get();
    
    public static final JFrame FRAME = ((
            java.util.function.Supplier<JFrame>)() ->{
                JFrame frm = new JFrame();
                frm.setSize(900, 580);
                frm.addWindowListener(THIS);
                return frm;
            }).get();
    private static final Font MONOSPACED = new Font("Monospaced", 0, 12);
    public static final JTextArea TEXT = ((
            java.util.function.Supplier<JTextArea>)() ->{
                JTextArea text = new JTextArea();
                
                text.setLineWrap(true);
                text.setEditable(false);
                text.setFont(MONOSPACED);
                ((javax.swing.text.DefaultCaret)
                        text.getCaret()).setUpdatePolicy(1);
                        
                JScrollPane scroll = new JScrollPane(text);
                scroll.setBorder(null);
                scroll.setVerticalScrollBarPolicy(22);
                
                Start.FRAME.add(scroll);
                Start.FRAME.setLocationRelativeTo(null);
                //if(INST != null) Start.FRAME.setVisible(true);
                Start.FRAME.setVisible(true);
                
                System.setOut(new Start(OUT, false));
                System.setErr(new Start(ERR, true));
                /*
                System.setOut((PrintStream)
                        java.lang.reflect.Proxy.newProxyInstance(
                        PrintStream.class.getClassLoader(),
                        new Class[] { PrintStream.class }, THIS
                ));
                System.setErr((PrintStream)
                        java.lang.reflect.Proxy.newProxyInstance(
                        PrintStream.class.getClassLoader(),
                        new Class[] { PrintStream.class }, THIS
                ));
                */
                return text;
            }).get();
    
    // parent of this
    public static final File PARENT_DIRECTORY =
            (Start.START.isFile() ?
            Start.START.getParentFile() : START);
    // config
    public static final Map<String, String> CONFIG = ((
            java.util.function.Supplier<Map<String, String>>)() ->{
                Map<String, String> map = new HashMap<String, String> ();
                try{
                    for(String line : Files.readAllLines(
                            new File(Start.PARENT_DIRECTORY,
                            "start_config.ini").toPath())){
                        String elem[] = line.split("=", 2);
                        map.put(elem[0].toLowerCase(), elem[1]);
                        System.out.printf("key: %s, value: %s\n",
                                elem[0], elem[1]);
                    }
                } catch(IOException ioe){}
                return map;
            }).get();
    
    private static boolean DEBUG = "true".equals(CONFIG.get("debug"));
    private static boolean DOWNLOAD =
            "true".equals(CONFIG.get("download"));
    
    public static final File DATA_DIRECTORY = ((
            java.util.function.Supplier<File>)() ->{
                File defaultDirectory = null;
                if(Start.CONFIG.get("work_dir") != null){
                    defaultDirectory =
                            new File(Start.CONFIG.get("work_dir"));
                } else{
                    String userHome = System.getProperty("user.home", ".");
                    String osName = System.getProperty(
                            "os.name").toLowerCase();
                    if(osName.matches("(linux|unix)")){
                        defaultDirectory =
                                new File(userHome, ".minecraft/");
                    } else if(osName.contains("mac")){
                        defaultDirectory = new File(userHome,
                                "Library/Application Support/minecraft/");
                    } else if(osName.contains("win")){
                        String appData = System.getenv("APPDATA");
                        defaultDirectory = new File((appData != null ?
                                appData : userHome), ".minecraft/");
                    } else{
                        defaultDirectory =
                                new File(userHome, "minecraft/");
                    }
                }
                return defaultDirectory;
            }).get();
    public static final File LAUNCHER_PACK = new File(
                Start.DATA_DIRECTORY, "launcher.pack.lzma");
    public static final File LAUNCHER_JAR = new File(
                Start.DATA_DIRECTORY, "launcher.jar");
    
    
    
    
    /*
    MAIN
    */
    public static void main(String args[]){
        /*
        if(INST == null){
            try{
                Process p = Runtime.getRuntime().exec(
                        "java -javaagent:Start.jar -cp . -jar Start.jar",
                        null, PARENT_DIRECTORY);
                System.setOut(new PrintStream(p.getOutputStream()));
                // System.setErr(new PrintStream(p.getErrorStream()));
                p.waitFor();
            } catch(Exception e){
                e.printStackTrace();
            }
            
            //exit();
            return;
        }
        */
        Start.MAIN = Thread.currentThread();
        System.out.println("\nSTART_URL: " + START_URL.toExternalForm());
        System.out.println("\nSTART: " + START);
        System.out.println("\nPARENT_DIRECTORY: " + PARENT_DIRECTORY);
        System.out.println();
        Start.CLIENT_TOKEN[0] = "87584590-7ab6-40e7-9c0e-5f5a8501937a";
        Start.ACCESS_TOKEN[0] = "aff3101b2d8d43bbb42fab2e6e993e28";
        
        if(Start.DEBUG) try{
            Files.write(new File(Start.PARENT_DIRECTORY,
                    "start.log").toPath(), ("arguments: \"" +
                    String.join("\" \"", args)).getBytes(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);
        } catch(IOException ioe){}
        
        try{
            new ServerSocket(65535, 5);
        } catch(BindException be){
            System.out.println("launcher already started");
            Start.exit();
        } catch(IOException ioe){}
        try{
            if(DOWNLOAD){
                HttpsURLConnection connection = (HttpsURLConnection)
                        Start.LAUNCHER_URL.openConnection(
                            java.net.Proxy.NO_PROXY);
                connection.setUseCaches(false);
                connection.setDefaultUseCaches(false);
                connection.setRequestProperty("Cache-Control",
                        "no-store,max-age=0,no-cache");
                connection.setRequestProperty("Expires", "0");
                connection.setRequestProperty("Pragma", "no-cache");
                
                long start = System.nanoTime();
                connection.connect();
                long elapsed = System.nanoTime() - start;
                System.out.println("Got reply in: " +
                        elapsed / 1000000L + "ms");
                
                if(!Start.LAUNCHER_PACK.exists()){
                    Start.LAUNCHER_PACK.getParentFile().mkdirs();
                    Start.LAUNCHER_PACK.createNewFile();
                }
                
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream =
                        new FileOutputStream(Start.LAUNCHER_PACK);
                
                long startDownload = System.nanoTime();
                long bytesRead = 0L;
                byte[] buffer = new byte[65536];
                try {
                    int read = inputStream.read(buffer);
                        while (read >= 1) {
                        bytesRead += read;
                        outputStream.write(buffer, 0, read);
                        read = inputStream.read(buffer);
                    }
                } finally {
                    inputStream.close();
                    outputStream.close();
                }
                long elapsedDownload = System.nanoTime() - startDownload;
                
                float elapsedSeconds = (float)(1L +
                        elapsedDownload) / 1.0E9F;
                float kbRead = (float)bytesRead / 1024.0F;
                System.out.printf("Downloaded %.1fkb in %ds at %.1fkb/s",
                        Float.valueOf(kbRead), (int)elapsedSeconds,
                        Float.valueOf(kbRead / elapsedSeconds)
                );
                
                String packPath = Start.LAUNCHER_PACK.getAbsolutePath();
                if(packPath.endsWith(".lzma")){
                    packPath = packPath.substring(0, packPath.length() - 5);
                }
                File unpacked = new File(packPath);
                if(!unpacked.exists()){
                    unpacked.getParentFile().mkdirs();
                    unpacked.createNewFile();
                }
                
                System.out.println("reversing LZMA on " +
                        Start.LAUNCHER_PACK + " to " + packPath);
                Class<?> lzma_in = null;
                try{
                    lzma_in = Start.class.getClassLoader().loadClass(
                            "LZMA.LzmaInputStream");
                    /*
                    Class.forName("LZMA.LzmaInputStream",
                            true, Start.class.getClassLoader());
                    */
                    if(lzma_in == null)
                        System.out.println("class not initialized");
                    else
                        System.out.println(lzma_in.getConstructor(
                                InputStream.class));
                } catch(ClassNotFoundException|NoSuchMethodException e){
                    e.printStackTrace();
                }
                try(InputStream in = (InputStream)lzma_in.getConstructor(
                            InputStream.class).newInstance(
                            new FileInputStream(Start.LAUNCHER_PACK));
                            OutputStream out =
                            new FileOutputStream(unpacked)){
                    byte buff[] = new byte[65536];
                    int read = in.read(buff); // do while? nah
                    while(read >= 1){
                        out.write(buff, 0, read);
                        read = in.read(buff);
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
                System.out.println("unpacking " +
                        unpacked + " to " + Start.LAUNCHER_JAR);
                try(java.util.jar.JarOutputStream jar_out =
                        new java.util.jar.JarOutputStream(
                        new FileOutputStream(Start.LAUNCHER_JAR))){
                    java.util.jar.Pack200.newUnpacker()
                            .unpack(unpacked, jar_out);
                } catch(Exception e){
                    e.printStackTrace();
                }
                Files.deleteIfExists(unpacked.toPath());
            } else{
                System.out.println("config: download=false");
                System.out.println(
                        "download & unpack launcher.jar skipped");
            }
            
            System.out.println("adding & deleting files in launcher.jar");
            /*
            java.util.jar.JarFile launcher_jar =
                    new java.util.jar.JarFile(Start.LAUNCHER_JAR);
            */
            URI uri = URI.create("jar:" + Start.LAUNCHER_JAR.toURI());
            // fs: launcher.jar
            // start_jar: class or jar file
            try(FileSystem fs = FileSystems.newFileSystem(uri,
                    new HashMap<String,String> ());
                FileSystem start_jar = FileSystems.newFileSystem(
                    (Start.START.isFile() ?
                    URI.create("jar:" + Start.START.toURI()) :
                    Start.START.toURI()), new HashMap<String, String>())){
                Files.deleteIfExists(fs.getPath("META-INF/MOJANGCS.RSA"));
                Files.deleteIfExists(fs.getPath("META-INF/MOJANGCS.SF"));
                Path start_source = (Start.START.isFile() ?
                        start_jar.getPath("Start.class") :
                        Paths.get(Start.PARENT_DIRECTORY.toString(),
                        "Start.class"));
                if(Files.exists(start_source))
                    Files.copy(start_source, fs.getPath("Start.class"),
                            StandardCopyOption.REPLACE_EXISTING);
                /*
                if(Files.exists(start_jar.getPath(
                        "version_manifest.json")))
                    Files.copy(start_jar.getPath("version_manifest.json"),
                            fs.getPath("version_manifest.json"),
                            StandardCopyOption.REPLACE_EXISTING);
                */
                /*
                Files.write(fs.getPath("META-INF/MANIFEST.MF"),
                        //*
                        ("Manifest-Version: 1.0\n" +
                        "Created-By: 1.8.0_72 (Oracle Corporation)\n" +
                        "Main-Class: Start\n\n\n").getBytes(),
                        /* /
                        javax.xml.bind.DatatypeConverter.parseHexBinary(
                        "4d616e69666573742d56657273696f6e3a20312e300d0a4" +
                        "37265617465642d42793a20312e382e305f323020284f72" +
                        "61636c6520436f72706f726174696f6e290d0a4d61696e2" +
                        "d436c6173733a2053746172740d0a0d0a"),
                        /***** /
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);
                */
            } catch(FileSystemException fse){
                System.out.println("launcher.jar already started");
                exit();
            } catch(IOException ioe){
                ioe.printStackTrace();
            }
            
            System.out.println("starting launcher.");
            //*
            try{
                URLClassLoader cl = new URLClassLoader(new URL[] {
                        Start.LAUNCHER_JAR.toURI().toURL()
                });//, Thread.currentThread().getContextClassLoader());
                
                Class<?> c =
                        // cl.loadClass("Start");
                        Class.forName("Start", false, cl);
                
                c.getMethod("start", ClassLoader.class, JFrame.class,
                        File.class, String.class).invoke(
                        null, cl, Start.FRAME, Start.DATA_DIRECTORY,
                        Start.getName(Start.DATA_DIRECTORY));
            } catch(Exception e){
                e.printStackTrace();
            }
            /*/
            Runtime.getRuntime().exec("java -jar \"" +
                    Start.LAUNCHER_JAR.toPath() +
                    "\" \"work_dir\" \"" +
                    Start.DATA_DIRECTORY.toPath() +
                    "\" \"name\" \"" + Start.getName(
                    Start.DATA_DIRECTORY) + "\"");
            /****/
        } catch(IOException ioe){
            ioe.printStackTrace();
        } finally{
            // Start.exit();
        }
    }
    private static final String getName(File data_dir){
        String name = CONFIG.get("name");
        File first = new File(data_dir, "FirstRun");
        if(name == null){
            try{
                name = new String(Files.readAllBytes(first.toPath()));
            } catch(IOException ioe){
                if(!(ioe instanceof NoSuchFileException))
                    ioe.printStackTrace();
            }
        }
        if((name == null) || (name.isEmpty())){
            name = JOptionPane.showInputDialog(
                    null, "input your name:", "name input",
                    JOptionPane.PLAIN_MESSAGE);
            if((name == null) || (name.isEmpty())){
                name = "explorer";
            } else{
                name = name.replaceAll(
                        "[^\\p{L}\\p{Nd}]+", "");
                //*
                try{
                    Files.write(first.toPath(), name.getBytes(),
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE_NEW,
                            StandardOpenOption.SPARSE);
                } catch(IOException ioe){}
                /*/
                first.createNewFile();
                try(OutputStream out = new FileOutputStream(first))
                    out.write(name.getBytes());
                /*****/
            }
        }
        return name;
    }
    
    
    
    
    
/**
 * 
 * run
 * 
 */
    private static SSLSocketFactory SSL_FACTORY =
            (SSLSocketFactory)SSLSocketFactory.getDefault();
    private static ServerSocket LOCAL_SERVER = null;
    private static SSLServerSocket LOCAL_SERVER_SSL = null;
    private static final Map<String, String> HANDLER =
            new HashMap<String, String> ();
            
    private Socket client = null;
    private InputStream in = null;
    private OutputStream out = null;
    private Socket server = null;
    private boolean processing = false, processed = false;
    
    private boolean isErr;
    public Start(){
        super(new ByteArrayOutputStream());
    }
    public Start(Socket client, InputStream in, OutputStream out){
        super(new ByteArrayOutputStream());
        this.client = client;
        this.in = in;
        this.out = out;
    }
    public Start(PrintStream ps, boolean isErr){
        super(ps);
        this.isErr = isErr;
    }
    static{
        /*
        HANDLER.put("launchermeta.mojang.com", String.join("\r\n",
                "GET /mc/game/version_manifest.json HTTP/1.1",
                "Host: launchermeta.mojang.com", ""));
        */
        HANDLER.put("launchermeta.mojang.com",
                "https://launchermeta.mojang.com" +
                "/mc/game/version_manifest.json");
        HANDLER.put("", "");
    }
    @Override public void handshakeCompleted(
            HandshakeCompletedEvent event) {
        System.out.println("Handshake finished!");
        System.out.println(
            "CipherSuite:" + event.getCipherSuite());
        System.out.println(
            "SessionId " + event.getSession());
        System.out.println(
            "PeerHost " + event.getSession().getPeerHost());
    }
    @Override public void run(){
        if(this.client != null){
            Start.ALLOWED.add(Thread.currentThread());
            
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
            
            System.out.println("-----     -----");
            System.out.println("request headers:");
            System.out.println(req);
            
            String lines[] = req.toString().split("\\r?\\n");
            String uri = lines[0].split(" ", 3)[1];
            String host = null;
            int port = 80;
            
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
            System.out.println("host: " + host);
            if("launchermeta.mojang.com".equals(host)){
                this.processing = true;
                System.out.println("launchermeta.mojang.com requested");
                System.out.println("redirect request with response:");
                StringBuilder result = new StringBuilder();
                try{
                    System.out.println(this.client);
                    out.write(("HTTP/1.1 200 " +
                            "Connection Established\r\n\r\n").getBytes());
                    out.flush();
                    
                    buffer = new byte[1024];
                    System.out.println("follow-up request:");
                    while(( read = in.read(buffer) ) >= 0){
                        System.out.println(new String(buffer, 0, read));
                    }
                    
                    /*
                    this.client.setReuseAddress(true);
                    SSLSocket sslclient = (SSLSocket)SSL_FACTORY
                            .createSocket(this.client, in, true);
                    System.out.println(sslclient);
                    sslclient.addHandshakeCompletedListener(THIS);
                    sslclient.startHandshake();
                    
                    buffer = new byte[1024];
                    req = new StringBuilder();
                    try{
                        while(( read = in.read(buffer) ) >= 0){
                            req.append(new String(buffer, 0, read));
                        }
                    } catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                    System.out.println("request: ");
                    System.out.println(req);
                    
                    /*/
                    URL url = new URL(Start.HANDLER.get(host));
                    HttpsURLConnection con = (HttpsURLConnection)
                            url.openConnection();
                    
                    con.setRequestMethod("GET");
                    
                    for(String key : con.getRequestProperties().keySet()){
                        System.out.printf("headers: %s: %s\n", key,
                                con.getRequestProperty(key).toString());
                    }
                    
                    
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                            con.getInputStream()));
                    String line;
                    
                    result.append(String.join("\r\n",
                            "HTTP/1.1 200 OK",
                            "Content-Length: " +
                            con.getContentLength(),
                            "Content-Type: application/json",
                            "Connection: close", ""));
                    while((line = br.readLine()) != null){
                        result.append(line);
                    }
                    result.append("\r\n\r\n");
                    
                    for(String key : con.getHeaderFields().keySet()){
                        System.out.printf("headers: %s: %s\n",
                                key, con.getHeaderField(key).toString());
                    }
                    br.close();
                    out.write(result.toString().getBytes(),
                            0, result.length());
                    out.flush();
                    /*****/
                } catch(IOException ioe){
                    ioe.printStackTrace();
                }
                this.processed = true;
            } else{
                try{
                    this.server = new Socket(host, port);
                } catch(IOException ioe){
                    System.out.println("cannot create host socket");
                    System.out.println("host processed: " + host);
                    ioe.printStackTrace();
                    Start.ALLOWED.remove(Thread.currentThread());
                    return;
                }
                System.out.println("host socket created: " + this.server);
                try(OutputStream out_ser = this.server.getOutputStream()){
                    System.out.println("start writing to host");
                    this.out.write(("HTTP/1.1 200 connection " + 
                            "established\r\n\r\n").getBytes("ASCII7"));
                    this.out.flush();
                    System.out.println("to client: HTTP/1.1 200 " +
                            "connection established\\r\\n\\r\\n");
                    try{
                        out_ser.write(req.toString().getBytes("ASCII7"));
                        System.out.println("request headers written");
                        while(( read = in.read(buffer) ) >= 0){
                            out_ser.write(buffer, 0, read);
                        }
                        out_ser.flush();
                    } catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                } catch(IOException ioe){
                    ioe.printStackTrace();
                }
            }
            
            Start.ALLOWED.remove(Thread.currentThread());
        } else{
            System.out.println("server socket listening for new request");
            try(Socket s = Start.LOCAL_SERVER.accept();
                        InputStream in = s.getInputStream();
                        OutputStream out = s.getOutputStream()){
                System.out.println("-----     -----");
                System.out.println("request received");
                System.out.println("start processing");
                
                new Thread(THIS).start();
                
                // s.setSoTimeout(10000);
                
                Start agent = new Start(s, in, out);
                new Thread(agent).start();
                int i = 0;
                do{
                    try{
                        Thread.sleep(1000);
                    } catch(InterruptedException ie){
                        ie.printStackTrace();
                    }
                } while(i++ < 5
                        && agent.server == null
                        && !agent.processed);
                if(agent.processing){
                    i = 0;
                    while(!agent.processed && i++ < 5){
                        try{
                            Thread.sleep(1000);
                        } catch(InterruptedException ie){
                            ie.printStackTrace();
                        }
                    }
                } else if(agent.server == null){
                    System.out.println("host socket creation failed");
                    return;
                } else{
                    try(InputStream in_ser = agent.server.getInputStream()){
                        System.out.println("start writing to client");
                        int read;
                        byte buffer[] = new byte[1024];
                        System.out.println("response:");
                        while(( read = in_ser.read(buffer) ) >= 0){
                            System.out.println(new String(buffer, 0, read));
                            out.write(buffer, 0, read);
                            out.flush();
                        }
                    }
                }
            } catch(IOException ioe){
                ioe.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        System.out.println("finished");
    }
    public static String getInputString(InputStream in){
        try(Scanner scan = new Scanner(in).useDelimiter("\\A")){
            return (scan.hasNext() ? scan.next() : null);
        }
    }
    
    // profile related
	public static final String[] UUID = new String[] {
		"0a857290-5877-41ae-a48c-d0d9777b2ef3",
        "CB3F55D3-645C-4F38-A497-9C13A33DB5CF",
	    "9dba38e9fc67f72c458fdac8ecd7cabaed3eb83737143a0128350a1ab381e3e",
		"7b881183-0eac-404f-a983-218694bc2150",
		"4566e69f-c907-48ee-8d71-d7ba5aa00d20"
	};
	public static final String[] CLIENT_TOKEN = new String[1];
    /*
    {
		"87584590-7ab6-40e7-9c0e-5f5a8501937a",
		"2cf8f4e0-f6ee-4e5b-9d44-04d51861fa12"
	};
    */
	public static final String[] ACCESS_TOKEN = {
		"aff3101b2d8d43bbb42fab2e6e993e28",
		"07b2bed918164d8ab3c2c7a2206127f1",
		"aabd49d956d74f4da4eb22b6821fdf85",
		"ddbba250e9f7400895f1bafcb3361e60"
	};
    
    
    
    
    
    /**
     * start
     */
    public static void start(ClassLoader cl,
            JFrame frm, File data_dir, String name){
        Thread.currentThread().setContextClassLoader(cl);
        String class_name = "com.mojang.authlib.HttpAuthenticationService";
        String method_name = "constantURL";
        Class<?> constants = null;
        
        try{
            ClassPool cp = ClassPool.getDefault();
            LoaderClassPath lcp = new LoaderClassPath(cl);
            ClassClassPath ccp = new ClassClassPath(Start.class);
            cp.insertClassPath(lcp);
            cp.insertClassPath(ccp);
            System.out.println(lcp.find(class_name));
            System.out.println(ccp.find(class_name));
            System.out.println(cp.find(class_name));
            
            CtClass ctc = cp.get(class_name);
            CtMethod m = ctc.getDeclaredMethod(method_name);
            // m.setName(method_name + "$impl");
            // CtMethod m1 = CtNewMethod.copy(m, method_name, auth, null);
            m.insertBefore("{System.out.println(\"HttpAS: \" + $1);}");
            // auth.writeFile();
            
            method_name = "performGetRequest";
            m = ctc.getDeclaredMethod(method_name);
            m.insertBefore("{System.out.println(\"GET(AS): \" + $1);}");
            
            method_name = "performPostRequest";
            m = ctc.getDeclaredMethod(method_name);
            m.insertBefore("{System.out.println(\"POST(AS): \" + $1);}");
            
            ctc.toClass();
            ctc.detach();
            
            class_name = "net.minecraft.launcher.LauncherConstants";
            method_name = "constantURL";
            
            ctc = cp.get(class_name);
            m = ctc.getDeclaredMethod(method_name);
            m.insertBefore(
                    "{System.out.println(\"Constants: \" + $1);}");
            constants = ctc.toClass();
            ctc.detach();
            
            class_name = "com.mojang.launcher.Http";
            method_name = "performGet";
            ctc = cp.get(class_name);
            m = ctc.getDeclaredMethod(method_name);
            String target = "https://launchermeta.mojang.com/" +
                    "mc/game/version_manifest.json";
            m.insertBefore("{" +
                    "System.out.println(\"GET(Http): \" + $1);" +
                    "if(\"" + target + "\".equals($1.toString()))" +
                    "$2 = java.net.Proxy.NO_PROXY;" +
            "}");
            ctc.toClass();
            ctc.detach();
        } catch(Exception e){
            e.printStackTrace();
        }
        
        /*
        try{
            constants = cl.loadClass(
                    "net.minecraft.launcher.LauncherConstants");
        } catch(Exception e){
            e.printStackTrace();
        }
        */



        System.out.println("programme start at " + data_dir.getPath());
        
        
        try{
            java.nio.file.Files.write(new File(
                    data_dir, "launcher_profiles.json").toPath(),
                    ("{\n  \"profiles\": {\n" +
                    "    \"(Default)\": {\n" +
                    "      \"name\": \"(Default)\"\n" +
                    "    }\n  },\n  \"selectedProfile\": " +
                    "\"(Default)\",\n  \"clientToken\": \"" +
                    Start.CLIENT_TOKEN[0] +"\",\n" +
                    "  \"authenticationDatabase\": {\n" +
                    "    \"" + Start.UUID[0].replace("-", "") +
                    "\": {\n" +
                    "      \"displayName\": \"" + name + "\",\n" +
                    "      \"accessToken\": \"" + Start.ACCESS_TOKEN[0] + 
                    "\",\n      \"userid\": \"info@cutom.com\",\n" +
                    "      \"uuid\": \"" + Start.UUID[0] + "\",\n" +
                    "      \"username\": \"cstmpublic@gmail.com\"\n" +
                    "    }\n  },\n" +
                    "  \"selectedUser\": \"" +
                    Start.UUID[0].replace("-", "") +
                    "\",\n  \"launcherVersion\": {\n    \"name\": \"" + 
                    // constants.getPackage().getImplementationVersion() +
                    "1.6.61" +
                    "\",\n    \"format\": " + constants.getField(
                    "VERSION_FORMAT").getInt(null) +
                    "\n  }\n}").getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch(Exception e){
            e.printStackTrace();
        }
        //*
        try{
            Class<?> c = null;
            // session service modification
            c = cl.loadClass("com.mojang.authlib.yggdrasil" +
                    ".YggdrasilMinecraftSessionService");
            // "https://sessionserver.mojang.com/session/minecraft/";
            Start.setStaticFieldValue(c, "BASE_URL", Start.CUSTOM_URL);
            Start.setStaticFieldValue(c, "JOIN_URL",
                    new URL(Start.CUSTOM_URL + "join"));
            Start.setStaticFieldValue(c, "CHECK_URL",
                    new URL(Start.CUSTOM_URL + "hasJoined"));
            // authlib modification
            c = cl.loadClass("com.mojang.authlib.yggdrasil" +
                    ".YggdrasilUserAuthentication");
            // "https://authserver.mojang.com/"
            Start.setStaticFieldValue(c, "BASE_URL", Start.CUSTOM_URL);
            Start.setStaticFieldValue(c, "ROUTE_AUTHENTICATE",
                    new URL(Start.CUSTOM_URL + "authenticate"));
            Start.setStaticFieldValue(c, "ROUTE_REFRESH",
                    new URL(Start.CUSTOM_URL + "refresh"));
            Start.setStaticFieldValue(c, "ROUTE_VALIDATE",
                    new URL(Start.CUSTOM_URL + "validate"));
            Start.setStaticFieldValue(c, "ROUTE_INVALIDATE",
                    new URL(Start.CUSTOM_URL + "invalidate"));
            Start.setStaticFieldValue(c, "ROUTE_SIGNOUT",
                    new URL(Start.CUSTOM_URL + "signout"));
            c = cl.loadClass("com.mojang.authlib.yggdrasil" +
                    ".YggdrasilGameProfileRepository");
            Start.setStaticFieldValue(c, "BASE_URL",
                    "https://image-uploader-xvalen214x.c9users.io");
            Start.setStaticFieldValue(c, "SEARCH_PAGE_URL",
                    "https://image-uploader-xvalen214x.c9users.io");
            System.out.println("have I changed anything?");
        } catch(Exception e){
            e.printStackTrace();
        }
        /*****/
        
        try{
            java.net.Proxy proxy = java.net.Proxy.NO_PROXY;
            try{
                Start.LOCAL_SERVER = new ServerSocket(8080);
                
                proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP,
                        Start.LOCAL_SERVER.getLocalSocketAddress());
                System.out.println(LOCAL_SERVER.getLocalSocketAddress());
                System.out.println("use custom proxy");
                if(Start.LOCAL_SERVER != null){
                    Start.RESTRICT = true;
                    Thread t = new Thread(THIS);
                    t.start();
                    ALLOWED.add(t);
                }
                
            } catch(IOException ioe){
                ioe.printStackTrace();
            }
            
            cl.loadClass("net.minecraft.launcher.Launcher").getConstructor(
                    JFrame.class, File.class, java.net.Proxy.class,
                    PasswordAuthentication.class, String[].class,
                    Integer.class).newInstance(frm, data_dir,
                    proxy, null, new String[0], constants.getField(
                    "SUPER_COOL_BOOTSTRAP_VERSION").getInt(null));
        } catch(Exception e){
            System.out.println("Unable to start: ");
            e.printStackTrace();
            e.fillInStackTrace().printStackTrace();
            exit();
        }
        System.out.println("launcher started");
    }
    public static void setStaticFieldValue(
            Class<?> c, String name, Object value){
        try{
            Field field = c.getDeclaredField(name);
            field.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() &
                    ~java.lang.reflect.Modifier.FINAL);
            field.set(null, value);
            modifiers.setInt(field, field.getModifiers() &
                    java.lang.reflect.Modifier.FINAL);
            modifiers.setAccessible(false);
            field.setAccessible(false);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    

/**
 * java.awt.event.WindowListener
 */
    @Override public void windowActivated(java.awt.event.WindowEvent we){}
    @Override public void windowClosed(java.awt.event.WindowEvent we){}
    @Override public void windowClosing(java.awt.event.WindowEvent we){
        System.exit(0);
    }
    @Override public void windowDeactivated(
                java.awt.event.WindowEvent we){}
    @Override public void windowDeiconified(
                java.awt.event.WindowEvent we){}
    @Override public void windowIconified(java.awt.event.WindowEvent we){}
    @Override public void windowOpened(java.awt.event.WindowEvent we){}

/* InvocationHandler
// failed as this method only allow interfaces
    @Override public Object invoke(
            Object proxy, Method method, Object[] args) throws Throwable {
        OUT.println("HELLO");
        return method.invoke(Start.OUT, args);
    }
*/
//* PrintStream
    private static Thread MAIN;
    private static List<Thread> ALLOWED = new LinkedList<Thread> ();
    private static boolean RESTRICT = false;
    private static final PrintStream LOG = ((
            java.util.function.Supplier<PrintStream>)() ->{
                    if(Start.DEBUG){ try{ return new PrintStream(
                    new File(Start.PARENT_DIRECTORY, "start.log"));
                    } catch(FileNotFoundException fnfe){
                    } catch(SecurityException se){}
                    OUT.println("logging creation failed");} // if ends
                    return null;}).get();
    @Override public synchronized PrintStream printf(
            String format, Object... args){
        if(RESTRICT && !ALLOWED.contains(
                Thread.currentThread())) return this;
        (isErr ? ERR : OUT).printf(format, args);
        if(TEXT.isDisplayable()) TEXT.append(String.format(format, args));
        if(Start.DEBUG && LOG != null) LOG.printf(format, args);
        return this;
    }
    // primary methods are print(String) and println()
    // avoided super to prevent unpredicted behaviour
    @Override public synchronized void print(String s){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        (isErr ? ERR : OUT).print(s);
        if(Start.TEXT.isDisplayable()) Start.TEXT.append(s);
        if(Start.DEBUG && LOG != null) LOG.print(s);
    }
    @Override public synchronized void println(){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        (isErr ? ERR : OUT).println();
        if(Start.TEXT.isDisplayable()) Start.TEXT.append("\n");
        if(Start.DEBUG && LOG != null) LOG.println();
    }
    @Override public synchronized void println(Object obj){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        (isErr ? ERR : OUT).print("" + obj + "\n");
        if(Start.TEXT.isDisplayable()) Start.TEXT.append("" + obj + "\n");
        if(Start.DEBUG && LOG != null) LOG.println(obj);
    }
    @Override public synchronized void println(String ln){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        (isErr ? ERR : OUT).print(ln + "\n");
        if(Start.TEXT.isDisplayable()) Start.TEXT.append(ln + "\n");
        if(Start.DEBUG && LOG != null) LOG.println(ln);
    }
    /* OutputStream
    @Override public synchronized void write(int b){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        Start.OUT.print(Character.toString((char)b));
        if(Start.TEXT.isDisplayable()){
            Start.TEXT.append(Character.toString((char)b));
        }
        if(Start.DEBUG && LOG != null) try{
            Start.LOG.write(b);
        } catch(IOException ioe){}
    }
    */
//*/

    private static void exit(){
        System.out.println("programme exit");
        if(Start.FRAME != null) Start.FRAME.dispose();
        if(Start.LOCAL_SERVER != null) try{ Start.LOCAL_SERVER.close();
        } catch(IOException ioe){}
        System.exit(0);
    }
}