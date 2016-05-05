
package start;

import static start.StartConstants.*;

import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.function.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class Console extends PrintStream
{
    public static final PrintStream SYSTEM_OUT = System.out;
    public static final PrintStream SYSTEM_ERR = System.err;
    public static final OutputStream EMPTY = new ByteArrayOutputStream();
    /*
    private static final PrintStream LOG = ((
            java.util.function.Supplier<PrintStream>)() ->{
                    if(DEBUG){ try{ return new PrintStream(
                    new File(PARENT_DIRECTORY, "start.log"));
                    } catch(FileNotFoundException fnfe){
                    } catch(SecurityException se){}
                    System.out.println("logging creation failed");}
                    return null;}).get();
    */
    public static final DateFormat DATE_TIME_INSTANCE =
            DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
    public static final Function<String, String> TIME =
        (str) -> String.format("[%s]: %s\n",
                DATE_TIME_INSTANCE.format(new Date()), str);
    private static final Font MONOSPACED = new Font("Monospaced", 0, 16);
    
    private static final Map<String, Console> INSTANCES =
            new HashMap<String, Console>();
    private static final JFrame FRAME = new JFrame("Console");
    private static final JTabbedPane TAB = new JTabbedPane();
    static{
        double silver = 1 + Math.pow(2, 0.5);
        double gold = (1 + Math.pow(5, 0.5)) / 2;
        FRAME.setSize((int)(600 * gold), 600);
        FRAME.setPreferredSize(FRAME.getSize());
        FRAME.setLocationRelativeTo(null);
        
        TAB.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        FRAME.add(TAB, BorderLayout.CENTER);
        FRAME.setVisible(true);
    }
    
    public static Console createSystemOut(){
        if(!INSTANCES.containsKey("System.out")){
        	Console out = getInstance("System.out");
            out.changeFunc((str) ->{
                SYSTEM_OUT.print(str); return str;});
            System.setOut(out);
        }
        return getInstance("System.out");
    }
    public static Console createSystemErr(){
        if(!INSTANCES.containsKey("System.err")){
        	Console err = getInstance("System.err");
            err.changeFunc((str) ->{
                SYSTEM_ERR.print(str); return str;});
            System.setErr(err);
        }
        return getInstance("System.err");
    }
    
    public static Console getInstance(String title){
        return (INSTANCES.containsKey(title) ?
                INSTANCES.get(title) : new Console(title));
    }
    
    private final JScrollPane scroll = new JScrollPane();
    private final JTextArea text = new JTextArea();
    private Function<String, String> func = Function.identity();
    private Console(String title){
        super(EMPTY);
        assert !INSTANCES.containsKey(title) : "multiple instances";
        this.text.setEditable(false);
        this.text.setFont(MONOSPACED);
        this.text.setTabSize(4);
        this.scroll.setViewportView(this.text);
    
        DefaultCaret caret = new DefaultCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.text.setCaret(caret);
        if(TAB.indexOfTab(title) < 0
        && TAB.indexOfTabComponent(this.scroll) < 0)
            TAB.addTab(title, this.scroll);
        INSTANCES.put(title, this);
    }
    public Console changeFunc(Function<String, String> func){
        if(func != null) this.func = func;
        return this;
    }
    
    @Override public void write(int b){
    	this.text.append(this.func.apply(Character.toString((char)b)));
    }
    @Override public void write(byte b[]){
    	this.text.append(this.func.apply(new String(b)));
    }
    @Override public void write(byte b[], int off, int len){
    	this.text.append(this.func.apply(new String(b, off, len)));
    }
    @Override public PrintStream printf(
            String format, Object... args){
        this.text.append(this.func.apply(String.format(format, args)));
        // this.text.setCaretPosition(this.text.getDocument().getLength());
        return this;
    }
    @Override public void print(String s){
        this.text.append(this.func.apply(s));
    }
    @Override public void println(){
        this.text.append(this.func.apply("\n"));
    }
    @Override public void println(Object obj){
        this.text.append(this.func.apply("" + obj + "\n"));
    }
    @Override public void println(String ln){
        this.text.append(this.func.apply("" + ln + "\n"));
    }
}