
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

public class Start extends OutputStream
implements java.awt.event.WindowListener, Runnable
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
                
                System.setOut(new PrintStream(THIS));
                System.setErr(new PrintStream(THIS));
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
            
            float elapsedSeconds = (float)(1L + elapsedDownload) / 1.0E9F;
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
                        OutputStream out = new FileOutputStream(unpacked)){
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
                Files.write(fs.getPath("META-INF/MANIFEST.MF"),
                        //*
                        ("Manifest-Version: 1.0\n" +
                        "Created-By: 1.8.0_72 (Oracle Corporation)\n" +
                        "Main-Class: Start\n\n\n").getBytes(),
                        /*/
                        javax.xml.bind.DatatypeConverter.parseHexBinary(
                        "4d616e69666573742d56657273696f6e3a20312e300d0a4" +
                        "37265617465642d42793a20312e382e305f323020284f72" +
                        "61636c6520436f72706f726174696f6e290d0a4d61696e2" +
                        "d436c6173733a2053746172740d0a0d0a"),
                        /*****/
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);
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
                    JOptionPane.PLAIN_MESSAGE).replaceAll(
                    "[^\\p{L}\\p{Nd}]+", "");
            if((name == null) || (name.isEmpty())){
                name="explorer";
            } else{
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
    private static ServerSocket LOCAL_SERVER = null;
    private static SSLServerSocket LOCAL_SERVER_SSL = null;
    private static final Map<String, String> HANDLER =
            new HashMap<String, String> ();
    static{
        HANDLER.put("launchermeta.mojang.com", String.join("\r\n",
                "HTTP/1.1 200 OK",
                "Content-Length: 238",
                "Content-Type: text/html",
                "Connection: close", "",
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD " +
                "HTML 4.01 Transitional//EN\" " +
                "\"http://www.w3.org/TR/html4/loose.dtd\">",
                "<HTML><HEAD>",
                "<META HTTP-EQUIV=\"Content-Type\" " +
                "CONTENT=\"text/html; charset=iso-8859-1\">",
                "</HEAD><BODY></BODY></HTML>", ""));
        HANDLER.put("", "");
    }
    @Override public void run(){
        System.out.println("server socket listening");
        while(Start.LOCAL_SERVER != null){
            try{
                // throws IOException
                // try-with-resources
                //*
                try(Socket s = Start.LOCAL_SERVER.accept();
                        InputStream in = s.getInputStream();
                        OutputStream out = s.getOutputStream()){
                /*/
                        Socket s = Start.LOCAL_SERVER.accept();
                        InputStream in = s.getInputStream();
                        OutputStream out = s.getOutputStream();
                /*****/
                    System.out.println("-----     -----");
                    System.out.println("start reading");
                    
                    s.setSoTimeout(10000);
                    
                    int read;
                    byte buffer[] = new byte[1024];
                    String req = new String();
                    Map<String, String> request =
                            new HashMap<String, String> ();
                    while(( read = in.read(buffer) ) >= 0){
                        req += (new String(buffer, 0, read));
                        if(req.endsWith("\r\n\r\n")
                        || req.endsWith("\n\n")){
                            break;
                        }
                    }
                    System.out.println("-----     -----");
                    System.out.println("request: ");
                    for(String line : req.split("\\r?\\n")){
                        String pair[] = line.split(" ", 2);
                        request.put(pair[0], pair[1]);
                        System.out.printf("%s %s\n", pair[0], pair[1]);
                    }
                    System.out.println("-----     -----");
                    
                    String host = request.get("Host:");
                    if("launchermeta.mojang.com".equals(host)){
                        // https://launchermeta.mojang.com
                        // /mc/game/version_manifest.json
                        System.out.println(
                                "launchermeta.mojang.com requested");
                        System.out.println(
                                    "redirect request with response:");
                        StringBuilder result = new StringBuilder();
                        URL url = new URL("https://" +
                                "launchermeta.mojang.com" +
                                "/mc/game/version_manifest.json");
                        HttpURLConnection con = (HttpURLConnection)
                                url.openConnection();
                        con.setRequestMethod("GET");
                        BufferedReader rd = new BufferedReader(
                                new InputStreamReader(
                                con.getInputStream()));
                        String line;
                        
                        result.append(String.join("\r\n",
                                "HTTP/1.1 200 OK",
                                "Content-Length: " +
                                con.getContentLength(),
                                "Content-Type: application/json",
                                "Connection: close", ""));
                        while((line = rd.readLine()) != null){
                            result.append(line);
                        }
                        rd.close();
                        out.write(result.toString().getBytes(),
                                0, result.length());
                        out.flush();
                    } else try(Socket ser = new Socket(host, 443);
                            OutputStream out_ser = ser.getOutputStream();
                            InputStream in_ser = ser.getInputStream()){
                        ser.setSoTimeout(10000);
                        out_ser.write(req.getBytes(), 0, req.length());
                        out_ser.flush();
                        String res = getInputString(in_ser);
                        System.out.println("response: ");
                        System.out.println(res);
                        out.write(res.getBytes(), 0, res.length());
                        out.flush();
                    }
                    /*
                    String req = getInputString(in);
                    log(req);
                    Map<String, String> request =
                            new HashMap<String, String> ();
                    for(String line : req.split("\\r?\\n")){
                        String pair[] = line.split(" ", 2);
                        request.put(pair[0], pair[1]);
                    }
                    try(Socket ser =
                            new Socket(request.get("Host:"), 443);
                            OutputStream out_ser = ser.getOutputStream();
                            InputStream in_ser = ser.getInputStream()){
                        out_ser.write(req.getBytes(), 0, req.length());
                        out_ser.flush();
                        String res = getInputString(in_ser);
                        out.write(res.getBytes(), 0, res.length());
                        out.flush();
                    }
                    */
                    /*
                    byte buff[] = new byte[65536];
                    int read = in.read(buff); // do while? nah
                    while(read >= 1){
                        log(new String(buff, 0, read));
                        read = in.read(buff);
                    }
                    */
                }
            } catch(IOException ioe){
                ioe.printStackTrace();
            } catch(NullPointerException npe){
                System.out.println("empty request");
            } catch(Exception e){}
        }
        // new Thread(THIS).start(); // replaced while loop
        
        /*
        try{
            Thread.currentThread().getContextClassLoader().loadClass(
                    "Start").getMethod("start", JFrame.class, File.class
                    ).invoke(null, Start.FRAME, Start.DATA_DIRECTORY);
        } catch(Exception e){
            e.printStackTrace();
        }
        */
    }
    public static String getInputString(InputStream in){
        try(Scanner scan = new Scanner(in).useDelimiter("\\A")){
            return (scan.hasNext() ? scan.next() : null);
        }
    }
    
    // profile related
	public static final String[] UUID = new String[] {
        "CB3F55D3-645C-4F38-A497-9C13A33DB5CF",
		"0a857290-5877-41ae-a48c-d0d9777b2ef3",
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
	public static final String[] ACCESS_TOKEN = new String[1];
    /*
    {
		"aff3101b2d8d43bbb42fab2e6e993e28",
		"07b2bed918164d8ab3c2c7a2206127f1",
		"aabd49d956d74f4da4eb22b6821fdf85",
		"ddbba250e9f7400895f1bafcb3361e60"
	};
    */
    
    
    
    
    
    /**
     * start
     */
    public static void start(ClassLoader cl,
            JFrame frm, File data_dir, String name){
        Thread.currentThread().setContextClassLoader(cl);
        ClassPool cp = ClassPool.getDefault();
        LoaderClassPath lcp = new LoaderClassPath(cl);
        cp.insertClassPath(lcp);
        System.out.println(lcp.find(
                "com.mojang.authlib.HttpAuthenticationService"));
        




        System.out.println("programme start at " + data_dir.getPath());
        Class<?> constants = null;
        try{
            constants = cl.loadClass(
                    "net.minecraft.launcher.LauncherConstants");
        } catch(Exception e){
            e.printStackTrace();
        }
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
                    constants.getPackage().getImplementationVersion() +
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
            /*
            try{
                Start.LOCAL_SERVER_SSL = (SSLServerSocket)
                        SSLServerSocketFactory.getDefault()
                        .createServerSocket(8081);
                SocketAddress address =
                        LOCAL_SERVER_SSL.getLocalSocketAddress();
                LOCAL_SERVER.bind(address);
                Start.LOCAL_SERVER = new ServerSocket(8080);
                
                proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP,
                        Start.LOCAL_SERVER.getLocalSocketAddress());
                System.out.println(LOCAL_SERVER.getLocalSocketAddress());
                System.out.println("use custom proxy");
                if(Start.LOCAL_SERVER != null){
                    Thread t = new Thread(THIS);
                    t.start();
                    ALLOWED.add(t);
                }
            } catch(IOException ioe){
                ioe.printStackTrace();
            }
            */
            
            RESTRICT = false;
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
//* OutputStream
    private static Thread MAIN;
    private static List<Thread> ALLOWED = new LinkedList<Thread> ();
    private static boolean RESTRICT = false;
    private static final OutputStream LOG = ((
            java.util.function.Supplier<OutputStream>)() ->{
                    if(Start.DEBUG) try{ return new FileOutputStream(
                    new File(Start.PARENT_DIRECTORY, "start.log"));
                    } catch(FileNotFoundException fnfe){}
                    return null;}).get();
    @Override public synchronized void write(int b){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        Start.OUT.print(new String(Character.toChars(b)));
        if(Start.FRAME.isDisplayable()){
            Start.TEXT.append(new String(Character.toChars(b)));
        }
        if(Start.DEBUG && LOG != null) try{
            Start.LOG.write(b);
        } catch(IOException ioe){}
    }
//*/

    private static void exit(){
        System.out.println("programme exit");
        if(Start.FRAME != null) Start.FRAME.dispose();
        if(Start.LOCAL_SERVER != null) try{ Start.LOCAL_SERVER.close();
        } catch(IOException ioe){}
        System.exit(0);
    }
}