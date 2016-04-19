
package start;

import java.awt.Font;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import javax.swing.*;

public class StartConstants
{
    static{
        System.out.println("start.StartConstants referenced");
    }
    public static final URL LAUNCHER_URL = ((
            java.util.function.Supplier<URL>)() ->{
                try{ return new URL("https://s3.amazonaws.com/" +
                        "Minecraft.Download/launcher/launcher.pack.lzma");
                } catch(MalformedURLException murle){
                    murle.printStackTrace();
                }
                return null;
            }).get();
    
    public static final String CUSTOM_URL_STRING =
            "https://mcl-xvalen214x.c9users.io/";
    
    //this
    public static final URL LOCATION_URL =
            Start.class.getProtectionDomain()
            .getCodeSource().getLocation();
    // this
    public static final File LOCATION_FILE = ((
            java.util.function.Supplier<File>)() ->{
                try{ return new File(LOCATION_URL.toURI());
                } catch(URISyntaxException urise){
                    urise.printStackTrace();
                }
                return null;
            }).get();

    // parent of this
    public static final File PARENT_DIRECTORY =
            (LOCATION_FILE.isFile() ?
            LOCATION_FILE.getParentFile() : LOCATION_FILE);
    // config
    private static final String CONFIG_FILE_NAME = "start_config.ini";
    public static final Map<String, String> CONFIG_MAP = ((
            java.util.function.Supplier<Map<String, String>>)() ->{
                Map<String, String> map = new HashMap<String, String> ();
                try{
                    for(String line : Files.readAllLines(
                            new File(PARENT_DIRECTORY,
                            CONFIG_FILE_NAME).toPath())){
                        String elem[] = line.trim().replaceAll(
                                "\\r", "").split("=", 2);
                        if(elem.length == 2){
                            map.put(elem[0].toLowerCase(), elem[1]);
                            System.out.printf("key: %s, value: %s\n",
                                    elem[0], elem[1]);
                        }
                    }
                } catch(IOException ioe){}
                return map;
            }).get();
    
    /*
    start_config.ini
        work_dir
        download
        debug
        name
    */
    public static final boolean DEBUG =
            "true".equals(CONFIG_MAP.get("debug"));
    public static final boolean DOWNLOAD =
            !"false".equals(CONFIG_MAP.get("download"));
    public static final String WORK_DIR = CONFIG_MAP.get("work_dir");
    public static final String NAME = CONFIG_MAP.get("name");
    
    public static final File DATA_DIRECTORY = ((
            java.util.function.Supplier<File>)() ->{
                File work_dir = null;
                if(WORK_DIR != null){
                    work_dir = new File(WORK_DIR);
                } else{
                    String userHome = System.getProperty("user.home", ".");
                    String osName = System.getProperty(
                            "os.name").toLowerCase();
                    if(osName.matches("(linux|unix)")){
                        work_dir = new File(userHome, ".minecraft/");
                    } else if(osName.contains("mac")){
                        work_dir = new File(userHome,
                                "Library/Application Support/minecraft/");
                    } else if(osName.contains("win")){
                        String appData = System.getenv("APPDATA");
                        work_dir = new File((appData != null ?
                                appData : userHome), ".minecraft/");
                    } else{
                        work_dir = new File(userHome, "minecraft/");
                    }
                }
                return work_dir;
            }).get();
    public static final File LAUNCHER_PACK = new File(
                DATA_DIRECTORY, "launcher.pack.lzma");
    public static final File LAUNCHER_JAR = new File(
                DATA_DIRECTORY, "launcher.jar");
}