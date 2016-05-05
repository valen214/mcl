
package start;

import static start.StartConstants.*;

import java.awt.Font;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import javax.swing.*;
import javax.net.ssl.*;

import LZMA.*;

public class Start implements
        Thread.UncaughtExceptionHandler,
        java.awt.event.WindowListener
{
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
    private static final Font MONOSPACED = new Font("Monospaced", 0, 12);
	public static JFrame FRAME = new JFrame("Start");
	public static final Start THIS = new Start();
	public static Console OUT;
	public static Console ERR;
	static{
        FRAME.setSize(900, 580);
        
        FRAME.setFont(MONOSPACED);
        FRAME.setLocationRelativeTo(null);
        FRAME.setVisible(true);
        
	    OUT = Console.createSystemOut();
	    ERR = Console.createSystemErr();
	    URL.setURLStreamHandlerFactory(CustomURLStreamHandler.FACTORY);
	    System.out.println("start.Start referenced");
	    Thread.currentThread().setUncaughtExceptionHandler(THIS);
	}
	public void uncaughtException(Thread t, Throwable e){
	    e.printStackTrace();
	}
    public static void main(String args[]) throws Exception{
	    Thread.currentThread().setUncaughtExceptionHandler(THIS);
        System.out.println("LOCATION_URL: " + LOCATION_URL);
        System.out.println("LOCATION_FILE: " + LOCATION_FILE);
        System.out.println("PARENT_DIRECTORY: " + PARENT_DIRECTORY);
        System.out.println("DATA_DIRECTORY: " + DATA_DIRECTORY);
        System.out.println();
        Start.CLIENT_TOKEN[0] = "87584590-7ab6-40e7-9c0e-5f5a8501937a";
        Start.ACCESS_TOKEN[0] = "aff3101b2d8d43bbb42fab2e6e993e28";
        
        String arg_str = "arguments: \"" +
                String.join("\" \"", args) + "\"\n";
        System.out.printf(arg_str);
        
        if(DEBUG) try{
            Files.write(new File(PARENT_DIRECTORY,
                    "start.log").toPath(), arg_str.getBytes(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);
        } catch(IOException ioe){}
        
        if(StartProxy.SERVER_SOCKET != null
        && StartProxy.PROXY != null){
            System.out.println("use custom proxy at " +
                    StartProxy.ADDRESS);
            StartProxy.start();
        }
        // test existance of instance
        try{
            new ServerSocket(65535, 5).close();
        } catch(BindException be){
            System.out.println("launcher already started");
            Start.exit();
        } catch(IOException ioe){}
        try{
            if(DOWNLOAD || !LAUNCHER_JAR.exists()
                    || !LAUNCHER_JAR.isFile()){
                HttpsURLConnection connection = (HttpsURLConnection)
                        LAUNCHER_URL.openConnection(
                        /*
                        StartProxy.PROXY);
                        /*/java.net.Proxy.NO_PROXY);//*/
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
                
                if(!LAUNCHER_PACK.exists()){
                    LAUNCHER_PACK.getParentFile().mkdirs();
                    LAUNCHER_PACK.createNewFile();
                }
                
                
                
                long startDownload = System.nanoTime();
                long bytesRead[] = new long[] {0L};
                try(InputStream in = connection.getInputStream();
                        FileOutputStream out =
                        new FileOutputStream(LAUNCHER_PACK);){
                    new StreamPipe(
                            in, out, 65536, (buffer, read) ->{
                        if(bytesRead[0] == 0L){
                            System.out.println("first 200 bytes: " +
                                    javax.xml.bind.DatatypeConverter
                                    .printHexBinary(Arrays.copyOf(buffer,
                                    200)));
                        }
                        bytesRead[0] += read;
                        return Arrays.copyOf(buffer, read);
                    }).run();
                }
                long elapsedDownload = System.nanoTime() - startDownload;
                
                float elapsedSeconds = (float)(1L +
                        elapsedDownload) / 1.0E9F;
                float kbRead = (float)bytesRead[0] / 1024.0F;
                System.out.printf("Downloaded %.1fkb in %ds at %.1fkb/s\n",
                        Float.valueOf(kbRead), (int)elapsedSeconds,
                        Float.valueOf(kbRead / elapsedSeconds)
                );
                
                String packPath = LAUNCHER_PACK.getAbsolutePath();
                if(packPath.endsWith(".lzma")){
                    packPath = packPath.substring(0, packPath.length() - 5);
                }
                File unpacked = new File(packPath);
                if(!unpacked.exists()){
                    unpacked.getParentFile().mkdirs();
                    unpacked.createNewFile();
                }
                
                System.out.println("reversing LZMA on " +
                        LAUNCHER_PACK + " to " + packPath);
                bytesRead[0] = 0L;
                try(InputStream in = (InputStream) new LzmaInputStream(
                            new FileInputStream(LAUNCHER_PACK));
                            OutputStream out =
                            new FileOutputStream(unpacked)){
                    new StreamPipe(
                            in, out, 4096, (buffer, read) ->{
                        if(bytesRead[0]++ == 0L){
                            System.out.println("first 200 bytes: " +
                                    javax.xml.bind.DatatypeConverter
                                    .printHexBinary(Arrays.copyOf(buffer,
                                    200)));
                        }
                        return Arrays.copyOf(buffer, read);
                    }).run();
                } catch(Exception e){
                    e.printStackTrace();
                }
                System.out.println("unpacking " +
                        unpacked + " to " + LAUNCHER_JAR);
                
                try(java.util.jar.JarOutputStream jar_out =
                        new java.util.jar.JarOutputStream(
                        new FileOutputStream(LAUNCHER_JAR))){
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
            
            /*
            System.out.println("adding & deleting files in launcher.jar");
            
            java.util.jar.JarFile launcher_jar =
                    new java.util.jar.JarFile(LAUNCHER_JAR);
            
            URI launcher_uri = URI.create("jar:" + LAUNCHER_JAR.toURI());
            URI start_uri = URI.create(LOCATION_URL.toExternalForm());
            // fs: launcher.jar
            // fs_start: jar file or parent directory of class file
            System.out.println(start_uri);
            try(FileSystem fs = FileSystems.newFileSystem(launcher_uri,
                    new HashMap<String,String> ());
                    FileSystem fs_start = FileSystems.newFileSystem(
                    (LOCATION_FILE.isFile() ?
                    URI.create("jar:" + start_uri) : start_uri),
                    new HashMap<String, String>())){
                Files.deleteIfExists(fs.getPath("META-INF/MOJANGCS.RSA"));
                Files.deleteIfExists(fs.getPath("META-INF/MOJANGCS.SF"));
                
                if(!Files.exists(fs.getPath("start/"))){
                    Files.createDirectory(fs.getPath("start/"));
                }
                try(Stream<Path> s = Files.list(fs.getPath("start/"))){
                    System.out.println(s);
                    s.forEach((path) -> {
                        try{
                            Files.copy(path, fs.getPath("start",
                                    path.getFileName().toString()),
                                    StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("file: " + path + " copied");
                        } catch(IOException ioe){
                            ioe.printStackTrace();
                        }
                    });
                }
            } catch(FileSystemException fse){
                fse.printStackTrace();
                System.out.println("launcher.jar already started");
                exit();
            } catch(IOException ioe){
                ioe.printStackTrace();
            }
            */
            
            System.out.println("starting launcher.");
            try{
                URLClassLoader cl = new URLClassLoader(new URL[] {
                        LAUNCHER_JAR.toURI().toURL()
                });
                Start.start(cl, FRAME, DATA_DIRECTORY);
            } catch(Exception e){
                e.printStackTrace();
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        } finally{
            // Start.exit();
        }
    }
    private static final String getName(File data_dir){
        String name = CONFIG_MAP.get("name");
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
     * start
     */
    public static void start(ClassLoader cl,
            JFrame frm, File data_dir){
        String name = getName(data_dir);
        
        Thread.currentThread().setContextClassLoader(cl);
        Class<?> constants = null;
        try{
            constants = cl.loadClass(
                    "net.minecraft.launcher.LauncherConstants");
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



        System.out.println("launcher start at " + data_dir.getPath());
        
        
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
                    "VERSION_FORMAT").getInt(null) + "\n  }\n}").getBytes(),
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
            Start.setStaticFieldValue(c, "BASE_URL", CUSTOM_URL_STRING);
            Start.setStaticFieldValue(c, "JOIN_URL",
                    new URL(CUSTOM_URL_STRING + "join"));
            setStaticFieldValue(c, "CHECK_URL",
                    new URL(CUSTOM_URL_STRING + "hasJoined"));
            // authlib modification
            c = cl.loadClass("com.mojang.authlib.yggdrasil" +
                    ".YggdrasilUserAuthentication");
            // "https://authserver.mojang.com/"
            setStaticFieldValue(c, "BASE_URL", CUSTOM_URL_STRING);
            setStaticFieldValue(c, "ROUTE_AUTHENTICATE",
                    new URL(CUSTOM_URL_STRING + "authenticate"));
            setStaticFieldValue(c, "ROUTE_REFRESH",
                    new URL(CUSTOM_URL_STRING + "refresh"));
            setStaticFieldValue(c, "ROUTE_VALIDATE",
                    new URL(CUSTOM_URL_STRING + "validate"));
            setStaticFieldValue(c, "ROUTE_INVALIDATE",
                    new URL(CUSTOM_URL_STRING + "invalidate"));
            setStaticFieldValue(c, "ROUTE_SIGNOUT",
                    new URL(CUSTOM_URL_STRING + "signout"));
            c = cl.loadClass("com.mojang.authlib.yggdrasil" +
                    ".YggdrasilGameProfileRepository");
            setStaticFieldValue(c, "BASE_URL", CUSTOM_URL_STRING);
            setStaticFieldValue(c, "SEARCH_PAGE_URL", CUSTOM_URL_STRING);
            System.out.println("have I changed anything?");
        } catch(Exception e){
            e.printStackTrace();
        }
        /*****/
        
        try{
            String host = "proxy-xvalen214x.c9users.io";
            /*
            setStaticFieldValue(StartProxy.class, "PROXY",
                    new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                            InetAddress.getByName(host), 80)));
            //*/
            Proxy proxy = 
            /*
            StartProxy.PROXY;/*/
            new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(host, 8080)); //*/
            System.setProperty("https.proxyHost", "https://" + host);
            System.setProperty("https.proxyPort", "8080");
            System.setProperty("http.proxyHost", "http://" + host);
            System.setProperty("http.proxyPort", "8080");
            cl.loadClass("net.minecraft.launcher.Launcher").getConstructor(
                    JFrame.class, File.class, java.net.Proxy.class,
                    PasswordAuthentication.class, String[].class,
                    Integer.class).newInstance(frm, data_dir,
                    proxy, null, new String[0],
                    constants.getField("SUPER_COOL_BOOTSTRAP_VERSION"
                    ).getInt(null));
        } catch(Exception e){
            System.out.println("Unable to start: ");
            e.printStackTrace();
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
        exit();
    }
    @Override public void windowDeactivated(
                java.awt.event.WindowEvent we){}
    @Override public void windowDeiconified(
                java.awt.event.WindowEvent we){}
    @Override public void windowIconified(java.awt.event.WindowEvent we){}
    @Override public void windowOpened(java.awt.event.WindowEvent we){}
    
    private static void exit(){
        System.out.println("programme exit");
        if(StartProxy.SERVER_SOCKET != null) try{
            StartProxy.SERVER_SOCKET.close();
        } catch(IOException ioe){}
        System.exit(0);
    }
}