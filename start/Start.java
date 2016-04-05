
package start;

import static start.StartConstants.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import javax.swing.*;
import javax.net.ssl.*;

import LZMA.*;

public class Start
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
    public static void main(String args[]){
        Console.RESTRICT = false;
        System.out.println("LOCATION_URL: " + LOCATION_URL);
        System.out.println("LOCATION_FILE: " + LOCATION_FILE);
        System.out.println("PARENT_DIRECTORY: " + PARENT_DIRECTORY);
        System.out.println("DATA_DIRECTORY: " + DATA_DIRECTORY);
        System.out.println();
        Start.CLIENT_TOKEN[0] = "87584590-7ab6-40e7-9c0e-5f5a8501937a";
        Start.ACCESS_TOKEN[0] = "aff3101b2d8d43bbb42fab2e6e993e28";
        
        if(DEBUG) try{
            Files.write(new File(PARENT_DIRECTORY,
                    "start.log").toPath(), ("arguments: \"" +
                    String.join("\" \"", args)).getBytes(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);
        } catch(IOException ioe){}
        
        // test existance of instance
        try{
            new ServerSocket(65535, 5);
        } catch(BindException be){
            System.out.println("launcher already started");
            Start.exit();
        } catch(IOException ioe){}
        try{
            if(DOWNLOAD){
                HttpsURLConnection connection = (HttpsURLConnection)
                        LAUNCHER_URL.openConnection(
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
                
                if(!LAUNCHER_PACK.exists()){
                    LAUNCHER_PACK.getParentFile().mkdirs();
                    LAUNCHER_PACK.createNewFile();
                }
                
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream =
                        new FileOutputStream(LAUNCHER_PACK);
                
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
                            new FileInputStream(LAUNCHER_PACK));
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
            
            System.out.println("adding & deleting files in launcher.jar");
            /*
            java.util.jar.JarFile launcher_jar =
                    new java.util.jar.JarFile(LAUNCHER_JAR);
            */
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
            
            System.out.println("starting launcher.");
            try{
                URLClassLoader cl = new URLClassLoader(new URL[] {
                        LAUNCHER_JAR.toURI().toURL()
                });//, Thread.currentThread().getContextClassLoader());
                
                Class<?> c = Class.forName("start.Start", false, cl);
                
                c.getMethod("start", ClassLoader.class, JFrame.class,
                        File.class, String.class).invoke(
                        null, cl, Console.FRAME, DATA_DIRECTORY,
                        getName(DATA_DIRECTORY));
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
            JFrame frm, File data_dir, String name){
        System.out.println("LOCATION_URL: " + LOCATION_URL);
        System.out.println("LOCATION_FILE: " + LOCATION_FILE);
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
                    constants.getPackage().getImplementationVersion() +
                    "\",\n    \"format\": " + constants.getField(
                    "VERSION_FORMAT").getInt(null) + "\n  }\n}").getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        /*
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
            if(StartProxy.SERVER_SOCKET != null
            && StartProxy.PROXY != null){
                System.out.println("use custom proxy at " +
                        StartProxy.ADDRESS);
                Console.RESTRICT = true;
                StartProxy.start();
            }
                
            
            cl.loadClass("net.minecraft.launcher.Launcher").getConstructor(
                    JFrame.class, File.class, java.net.Proxy.class,
                    PasswordAuthentication.class, String[].class,
                    Integer.class).newInstance(frm, data_dir,
                    StartProxy.PROXY, null, new String[0],
                    constants.getField( "SUPER_COOL_BOOTSTRAP_VERSION"
                    ).getInt(null));
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
    
    private static void exit(){
        System.out.println("programme exit");
        if(Console.FRAME != null) Console.FRAME.dispose();
        if(StartProxy.SERVER_SOCKET != null) try{
            StartProxy.SERVER_SOCKET.close();
        } catch(IOException ioe){}
        System.exit(0);
    }
}