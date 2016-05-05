
package start;

import java.net.*;
import java.util.*;

public class CustomURLStreamHandler extends URLStreamHandler
implements URLStreamHandlerFactory
{
    // thought of Arrays.asList,
    // but actually a linkedlist implementation is better
    private static final List<String> PACKAGE_PREFIX_LIST =
            new LinkedList<String> ();
    private static final ClassLoader SYSTEM_CLASS_LOADER =
            ClassLoader.getSystemClassLoader();
    private static final Map<String, CustomURLStreamHandler> INSTANCES =
            new HashMap<String, CustomURLStreamHandler> ();
    private static final Console CONSOLE =
            Console.getInstance("CustomURLStreamHandler");
    static{
        PACKAGE_PREFIX_LIST.add("java.protocol.handler.pkgs");
        PACKAGE_PREFIX_LIST.add("sun.net.www.protocol");
        PACKAGE_PREFIX_LIST.add("com.sun.net.sll");
    }
    public static final CustomURLStreamHandler FACTORY = getInstance(null);
    
    private final String protocol;
    private CustomURLStreamHandler(String protocol){
        assert !INSTANCES.containsKey(protocol) : "multiple instances";
        this.protocol = protocol;
        INSTANCES.put(protocol, this);
    }
    public static CustomURLStreamHandler getInstance(String protocol){
        CustomURLStreamHandler handler = null;
        if(INSTANCES.containsKey(protocol)){
            handler = INSTANCES.get(protocol);
        } else{
            handler = new CustomURLStreamHandler(protocol);
        }
        return handler;
    }
    @Override public URLConnection openConnection(URL url, Proxy proxy){
        CONSOLE.println("openConnection(" + url + ")");
        URLConnection connection = null;
        assert url.getProtocol().equalsIgnoreCase(
                this.protocol) : "wrong protocol";
        try{
            if("http".equalsIgnoreCase(this.protocol)){
                connection = (URLConnection)Class.forName(
                        "sun.net.www.protocol.http.HttpURLConnection"
                        ).getConstructor(java.net.URL.class,
                        java.net.Proxy.class).newInstance(url, proxy);
            } else if("https".equalsIgnoreCase(this.protocol)){
                java.lang.reflect.Constructor<?> constr = Class.forName(
                        "sun.net.www.protocol.https.HttpsURLConnectionImpl"
                        ).getDeclaredConstructor(java.net.URL.class,
                        java.net.Proxy.class, 
                        sun.net.www.protocol.https.Handler.class);
                constr.setAccessible(true);
                InetSocketAddress address =
                        (InetSocketAddress)proxy.address();
                connection = (URLConnection) constr.newInstance(url, proxy,
                        new sun.net.www.protocol.https.Handler(
                        address.getHostString(), address.getPort()));
            } else{
                
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return connection;
    }
    @Override public URLConnection openConnection(URL url){
        return this.openConnection(url, null);
    }
    
    public URLStreamHandler createURLStreamHandler(String protocol){
        URLStreamHandler handler = null;
        if("http".equalsIgnoreCase(protocol)
        || "https".equalsIgnoreCase(protocol)){
            handler = getInstance(protocol);
        } else{
            String name = "";
            Class<?> cls = null;
            for(String prefix : PACKAGE_PREFIX_LIST){
                try{
                    name = prefix + "." + protocol + ".Handler";
                    try{
                        cls = Class.forName(name); // current class loader
                    } catch(ClassNotFoundException cnfe){
                        if(SYSTEM_CLASS_LOADER != null){
                                cls = SYSTEM_CLASS_LOADER.loadClass(name);
                        }
                    }
                    if(cls != null){
                        handler = (URLStreamHandler) cls.newInstance();
                        break;
                    }
                } catch(Exception e){
                    
                }
            }
        }
        return handler;
    }
}