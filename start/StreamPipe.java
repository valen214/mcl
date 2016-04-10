
package start;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiFunction;

public class StreamPipe implements Runnable
{
    private final InputStream in;
    private final OutputStream out;
    private final int size;
    private final BiFunction<byte[], Integer, byte[]> func;
    private Thread thread = null;
    public StreamPipe(InputStream in, OutputStream out){
        this(in, out, 1024, null);
    }
    public StreamPipe(InputStream in, OutputStream out, int size){
        this(in, out, size, null);
    }
    public StreamPipe(InputStream in, OutputStream out,
            int size, BiFunction<byte[], Integer, byte[]> func){
        this.in = in;
        this.out = out;
        this.size = size;
        if(func == null){
            this.func = (buffer, read) -> Arrays.copyOf(buffer, read);
        } else{
            this.func = func;
        }
    }
    
    public void start(){
        if(this.thread == null){
            this.thread = new Thread(this);
            this.thread.start();
        }
    }
    public boolean isAlive(){
        return this.thread.isAlive();
    }
    
    @Override public void run(){
        int read;
        byte buffer[] = new byte[this.size];
        try{
            while(( read = this.in.read(buffer) ) >= 0){
                this.out.write(this.func.apply(buffer, read));
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}