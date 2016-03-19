
import java.awt.Font;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.net.ssl.HttpsURLConnection;

public class Start extends OutputStream 
implements java.awt.event.WindowListener, Runnable
{
    private static final URL LAUNCHER_URL = ((
            java.util.function.Supplier<URL>)() ->{
                try{ return new URL("https://s3.amazonaws.com/" +
                        "Minecraft.Download/launcher/launcher.pack.lzma");
                } catch(MalformedURLException murle){
                    murle.printStackTrace();
                }
                return null;
            }).get();
    
    public static final String CUSTOM_URL = "https://www.google.com/";
    public static final Start THIS = new Start();
    public static final PrintStream OUT = System.out;
    public static final PrintStream ERR = System.err;
    
    //this
    public static final URL START_URL =
            Start.class.getProtectionDomain().getCodeSource().getLocation();
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
        
    // parent of this
    public static final File PARENT_DIRECTORY =
            (Start.START.isFile() ?
            Start.START.getParentFile() : START);
    // config
    public static final File CONFIG = new File(
            Start.PARENT_DIRECTORY, "mc_launcher_profiles.xml");
    public static final String CONFIG_CONTENT = ((
            java.util.function.Supplier<String>)() ->{
                try{ return new String(
                    java.nio.file.Files.readAllBytes(Start.CONFIG.toPath()));
                } catch(IOException ioe){return null;}}).get();
    
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
                Start.FRAME.setVisible(true);
                
                System.setOut(new PrintStream(THIS));
                System.setErr(new PrintStream(THIS));
                return text;
            }).get();
    
    public static void log(String ln){
        OUT.println(ln);
        if(Start.FRAME.isDisplayable()) Start.TEXT.append(ln + '\n');
    }
    
    public static final File DATA_DIRECTORY = ((
            java.util.function.Supplier<File>)() ->{
                String osName = System.getProperty("os.name").toLowerCase();
                String userHome = System.getProperty("user.home", ".");
                File defaultDirectory = null;
                if(osName.matches("(linux|unix)")){
                    defaultDirectory = new File(userHome, ".minecraft/");
                } else if(osName.contains("mac")){
                    defaultDirectory = new File(userHome,
                            "Library/Application Support/minecraft/");
                } else if(osName.contains("win")){
                    String appData = System.getenv("APPDATA");
                    defaultDirectory = new File((appData != null ?
                            appData : userHome), ".minecraft/");
                } else{
                    defaultDirectory = new File(userHome, "minecraft/");
                }
                return defaultDirectory;
            }).get();
    public static final File LAUNCHER_PACK = new File(
                Start.DATA_DIRECTORY, "launcher.pack.lzma");
    public static final File LAUNCHER_JAR = new File(
                Start.DATA_DIRECTORY, "launcher.jar");
    
    public static void main(String args[]){
        /*
        for(int i = 0; i < args.length; ++i){
            try{
                Files.write(new File(Start.DATA_DIRECTORY, "FirstRun").toPath(),
                        ("\n" + args[i] + "\n").getBytes(),
                        StandardOpenOption.APPEND,
                        StandardOpenOption.CREATE);
            } catch(IOException ioe){}
        }
        */
        if(args.length >= 2 && args[0] == "work_dir"){
            log("launcher start with working directory provided");
            Start.start(Start.FRAME, new File(args[1]));
            return;
        } else if(Start.START.getName().equals("launcher.jar")){
            log("launcher.jar detected, start with default config");
            Start.start(Start.FRAME, Start.DATA_DIRECTORY);
            return;
        }
        try{
            HttpsURLConnection connection = (HttpsURLConnection)
                    Start.LAUNCHER_URL.openConnection(java.net.Proxy.NO_PROXY);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setRequestProperty("Cache-Control",
                    "no-store,max-age=0,no-cache");
            connection.setRequestProperty("Expires", "0");
            connection.setRequestProperty("Pragma", "no-cache");
            
            long start = System.nanoTime();
            connection.connect();
            long elapsed = System.nanoTime() - start;
            log("Got reply in: " + elapsed / 1000000L + "ms");
            
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
            log(String.format("Downloaded %.1fkb in %ds at %.1fkb/s",
                    Float.valueOf(kbRead), (int)elapsedSeconds,
                    Float.valueOf(kbRead / elapsedSeconds)
            ));
            
            String packPath = Start.LAUNCHER_PACK.getAbsolutePath();
            if(packPath.endsWith(".lzma")){
                packPath = packPath.substring(0, packPath.length() - 5);
            }
            File unpacked = new File(packPath);
            if(!unpacked.exists()){
                unpacked.getParentFile().mkdirs();
                unpacked.createNewFile();
            }
            
            log("reversing LZMA on " +
                    Start.LAUNCHER_PACK + " to " + packPath);
            Class<?> lzma_in = null;
            try{
                lzma_in = Start.class.getClassLoader().loadClass(
                        "LZMA.LzmaInputStream");
                /*
                Class.forName("LZMA.LzmaInputStream",
                        true, Start.class.getClassLoader());
                */
                if(lzma_in == null) log("class not initialized");
                else log(lzma_in.getConstructor(InputStream.class).toString());
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
            log("unpacking " + unpacked + " to " + Start.LAUNCHER_JAR);
            try(java.util.jar.JarOutputStream jar_out =
                    new java.util.jar.JarOutputStream(
                    new FileOutputStream(Start.LAUNCHER_JAR))){
                java.util.jar.Pack200.newUnpacker().unpack(unpacked, jar_out);
            } catch(Exception e){
                e.printStackTrace();
            }
            Files.deleteIfExists(unpacked.toPath());
            
            log("adding & deleting files in launcher.jar");
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
                        new File(Start.PARENT_DIRECTORY,
                        "Start.class").toPath());
                if(start_source != null)
                    Files.copy(start_source, fs.getPath("Start.class"),
                            StandardCopyOption.REPLACE_EXISTING);
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
            } catch(IOException ioe){
                ioe.printStackTrace();
            }
            
            log("starting launcher.");
            /*
            try{
                URLClassLoader cl = new URLClassLoader(new URL[] {
                        Start.LAUNCHER_JAR.toURI().toURL()
                });//, Thread.currentThread().getContextClassLoader());
                Thread thread = new Thread(THIS);
                thread.setContextClassLoader(cl);
                thread.start();
                Class<?> c =
                new java.net.URLClassLoader(new URL[] {
                        Start.LAUNCHER_JAR.toURI().toURL()
                }).loadClass("Start");
                
                Class.forName("Start", false, new java.net.URLClassLoader(
                        new URL[] {Start.LAUNCHER_JAR.toURI().toURL()
                }));
                c.getMethod("start", JFrame.class, File.class).invoke(
                        null, Start.FRAME, Start.DATA_DIRECTORY);
            } catch(Exception e){
                e.printStackTrace();
            }
            /*/
            Runtime.getRuntime().exec("java -jar \"" +
                    Start.LAUNCHER_JAR.toPath() + "\" \"" +
                    "\"work_dir\" \"" +
                    Start.DATA_DIRECTORY.toPath() + "\"");
            /****/
        } catch(IOException ioe){
            ioe.printStackTrace();
        } finally{
            Start.FRAME.dispose();
        }
    }
    @Override public void run(){
        try{
            Thread.currentThread().getContextClassLoader().loadClass(
                    "Start").getMethod("start", JFrame.class, File.class
                    ).invoke(null, Start.FRAME, Start.DATA_DIRECTORY);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    // profile related
	public static final String[] UUID = new String[] {
        "CB3F55D3-645C-4F38-A497-9C13A33DB5CF",
		"0a857290-5877-41ae-a48c-d0d9777b2ef3",
		"7b881183-0eac-404f-a983-218694bc2150"
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
    
    static{
        log("\nSTART_URL: " + START_URL.toExternalForm());
        log("\nSTART: " + START);
        log("\nPARENT_DIRECTORY: " + PARENT_DIRECTORY);
        log("");
        Start.CLIENT_TOKEN[0] = "87584590-7ab6-40e7-9c0e-5f5a8501937a";
        Start.ACCESS_TOKEN[0] = "aff3101b2d8d43bbb42fab2e6e993e28";
    }
    public static void start(JFrame frm, File data_dir){
        log("HI");
        String name = "valen";
        try{
            java.nio.file.Files.write(new File(
                    data_dir, "launcher_profiles.json").toPath(),
                    ("{\n  \"profiles\": {\n" +
                    "    \"(Default)\": {\n      \"name\": \"(Default)\"\n" +
                    "    }\n  },\n  \"selectedProfile\": \"(Default)\",\n" +
                    "  \"clientToken\": \"" + Start.CLIENT_TOKEN[0] +"\",\n" +
                    "  \"authenticationDatabase\": {\n" +
                    "    \"" + Start.UUID[0].replace("-", "") + "\": {\n" +
                    "      \"displayName\": \"" + name + "\",\n" +
                    "      \"accessToken\": \"" + Start.ACCESS_TOKEN[0] + 
                    "\",\n      \"userid\": \"info@cutom.com\",\n" +
                    "      \"uuid\": \"" + Start.UUID[0] + "\",\n" +
                    "      \"username\": \"cstmpublic@gmail.com\"\n" +
                    "    }\n  },\n" +
                    "  \"selectedUser\": \"" + Start.UUID[0].replace("-", "") +
                    "\",\n  \"launcherVersion\": {\n    \"name\": \"" + 
                    net.minecraft.launcher.LauncherConstants.class.getPackage()
                    .getImplementationVersion() + "\",\n    \"format\": " +
                    net.minecraft.launcher.LauncherConstants.VERSION_FORMAT +
                    "\n  }\n}").getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch(IOException ioe){}
        try{
            Class<?> c = null;
            // session service modification
            c = com.mojang.authlib.yggdrasil
                    .YggdrasilMinecraftSessionService.class;
            // "https://sessionserver.mojang.com/session/minecraft/";
            Start.setStaticFieldValue(c, "BASE_URL", Start.CUSTOM_URL);
            Start.setStaticFieldValue(c, "JOIN_URL",
                    new URL(Start.CUSTOM_URL + "join"));
            Start.setStaticFieldValue(c, "CHECK_URL",
                    new URL(Start.CUSTOM_URL + "hasJoined"));
            // authlib modification
            c = com.mojang.authlib.yggdrasil
                    .YggdrasilUserAuthentication.class;
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
            OUT.println("have I changed anything?");
        } catch(MalformedURLException murle){}
        
        try
        {
            net.minecraft.launcher.Launcher launcher =
                    new net.minecraft.launcher.Launcher(frm, data_dir,
                    java.net.Proxy.NO_PROXY, null, new String[0],
                    net.minecraft.launcher.LauncherConstants.
                    SUPER_COOL_BOOTSTRAP_VERSION);
        } catch(Exception e){
            System.err.println("Unable to start: ");
            e.printStackTrace();
            e.fillInStackTrace().printStackTrace();
        }
    }
    public static void setStaticFieldValue(
            Class<?> c, String name, Object value){
        try{
            Field field = c.getDeclaredField(name);
            field.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, value);
            modifiers.setInt(field, field.getModifiers() & Modifier.FINAL);
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
    @Override public void windowDeactivated(java.awt.event.WindowEvent we){}
    @Override public void windowDeiconified(java.awt.event.WindowEvent we){}
    @Override public void windowIconified(java.awt.event.WindowEvent we){}
    @Override public void windowOpened(java.awt.event.WindowEvent we){}

    @Override public void write(int b){
        Start.TEXT.append(new String(Character.toChars(b)));
    }
}