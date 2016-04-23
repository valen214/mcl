
package start;

import static start.StartConstants.*;

import java.awt.Font;
import java.io.*;
import java.util.*;
import java.util.function.*;

import javax.swing.*;

//TODO: rewrite Console as each instance of Console is a new frame

public class Console extends PrintStream
{
    public static final PrintStream SYSTEM_OUT = System.out;
    public static final PrintStream SYSTEM_ERR = System.err;
    private static final Font MONOSPACED = new Font("Monospaced", 0, 12);
    public static final OutputStream EMPTY = new ByteArrayOutputStream();
    private static final PrintStream LOG = ((
            java.util.function.Supplier<PrintStream>)() ->{
                    if(DEBUG){ try{ return new PrintStream(
                    new File(PARENT_DIRECTORY, "start.log"));
                    } catch(FileNotFoundException fnfe){
                    } catch(SecurityException se){}
                    System.out.println("logging creation failed");}
                    return null;}).get();

    private static final Console THIS = new Console();
    public static final JFrame FRAME = new JFrame("Start");
    public static final JTextArea TEXT = new JTextArea();
    public static final Function<String, String> TIME =
            (Function<String, String>)(str) ->{
                return str;
            };
    
    private static final Map<String, Console> INSTANCES =
            new HashMap<String, Console> ();

    static{
        System.out.println("start.Console referenced");
        initialize(FRAME, TEXT);
        INSTANCES.put("this", THIS);
    }
    
    private JFrame frm = new JFrame();
    private JTextArea text = new JTextArea();
    private JTextPane text = new JTextPane();
    private Function<String, String> func = Function.identity();
    private Console(){
        super(EMPTY);
    }
    private Console(String title){
        super(EMPTY);
        initialize(frm, text);
        frm.setTitle(title);
    }
    private Console(String title, Function<String, String> func){
        this(title);
        if(func != null) this.func = func;
    }
    
    public static Console getInstance(){
        return THIS;
    }
    public static Console getInstance(String title){
        if(!INSTANCES.containsKey(title)){
            INSTANCES.put(title, new Console(title));
        }
        return INSTANCES.get(title);
    }
    public static Console getInstance(String title,
            Function<String, String> func){
        if(!INSTANCES.containsKey(title)){
            INSTANCES.put(title, new Console(title, func));
        }
        return INSTANCES.get(title);
    }
    
    public static Console createSystemOutput(){
        Console out = getInstance("System.out", (str) ->{
            SYSTEM_OUT.print(str);
            return str;
        });
        System.setOut(out);
        return out;
    }
    public static Console createSystemError(){
        Console err = getInstance("System.err", (str) ->{
            SYSTEM_ERR.print(str);
            return str;
        });
        System.setErr(err);
        return err;
    }
    private static void initialize(JFrame frm, JTextArea text){
        frm.setSize(900, 580);
        
        text.setLineWrap(true);
        text.setEditable(false);
        text.setFont(MONOSPACED);
        ((javax.swing.text.DefaultCaret)
                text.getCaret()).setUpdatePolicy(1);
                
        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(22);
        
        frm.add(scroll);
        frm.setLocationRelativeTo(null);
        //if(INST != null) FRAME.setVisible(true);
        frm.setVisible(true);
    }

    @Override public PrintStream printf(
            String format, Object... args){
        this.text.append(this.func.apply(String.format(format, args)));
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