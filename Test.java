
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.spec.*;

public class Test
{
    public static void main1(String args[]) throws Exception{
        //*
        URL url = new URL("https://mcl-xvalen214x.c9users.io/Start.java");
        /*/
        URL url = new URL("https://raw.githubusercontent.com/eitetu/" +
                "minecraft-server/master/src/main/java/com/eitetu/" +
                "minecraft/server/util/authlib/yggdrasil/" +
                "YggdrasilMinecraftSessionService.java");
        //*/
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setDoOutput(true);
        con.setDoInput(true);
        //*
        con.getOutputStream().write(
                "GET manifest.mf HTTP/1.1\r\n\r\n".getBytes());
        /*/
        InputStream in = con.getInputStream();
        byte buffer[] = new byte[1024];
        int read;
        while(( read = in.read(buffer) ) >= 0){
            System.out.println(new String(buffer, 0, read));
        }
        //*/
    }
    public static void main2(String args[]) throws Exception{
        System.setProperty("javax.net.ssl.trustStore", "clienttrust");
        System.setProperty("javax.net.ssl.trustAnchors", "clienttrust");
        SSLSocketFactory ssf = (SSLSocketFactory)
                SSLSocketFactory.getDefault();
        SSLSocket s = (SSLSocket)ssf.createSocket(InetAddress.getByName(
                "launchermeta.mojang.com"), 443);
        PublicKey pubkey = null;
        {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(
					Files.readAllBytes(Paths.get(Test.class.getResource(
				    "/yggdrasil_session_pubkey.der").toURI())));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			pubkey = keyFactory.generatePublic(spec);
        }
        SSLSession session = s.getSession();
        s.setEnableSessionCreation(true);
        session.putValue("trustAnchors", "clienttrust");
        s.startHandshake();
    
        OutputStream outs = s.getOutputStream();
        PrintStream out = new PrintStream(outs);
        InputStream ins = s.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(ins));
    
        out.println("GET /mc/game/version_manifest.json HTTP/1.1");
        out.println("");
        String line = null;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
    
        in.close();
        out.close();
    }
    
    public static void main(String args[]){
        SSLSocketFactory ssf = (SSLSocketFactory)
                SSLSocketFactory.getDefault();
        try(Socket s = ssf.createSocket(InetAddress.getByName(
                "mcl-xvalen214x.c9users.io"), 443);
                OutputStream out = s.getOutputStream()){
            //*
            out.write(("GET /Start.java HTTP/1.1\r\n" +
                    "User-Agent: Mozilla/5.0\r\n" +
                    "Accept-Encoding: *\r\n" +
                    "Accept: */*\r\n\r\n").getBytes("ASCII7"));
            /* /
            out.write(("GET /mc/game/version_manifest.json HTTP/1.1\r\n" +
                    "Host: launchermata.mojang.com\r\n" +
                    "Accept: application/json\r\n\r\n").getBytes());
            /* /
            out.write(("CONNECT launchermeta.mojang.com:443 " +
                    "HTTP/1.1\r\n\r\n").getBytes());
            //*/
            out.flush();
            InputStream in = s.getInputStream();
            byte buffer[] = new byte[1024];
            int read = 0;
            while(( read = in.read(buffer) ) >= 0){
                System.out.println(new String(buffer, 0, read));
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
        
    }
}