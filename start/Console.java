
package start;

import static start.StartConstants.*;

import java.awt.Font;
import java.io.*;
import java.util.*;

import javax.swing.*;

//TODO: rewrite Console as each instance of Console is a new frame

public class Console extends PrintStream
{
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

    static{
        System.out.println("start.Console referenced");
        initialize(FRAME, TEXT);
    }
    
    private JFrame frm = new JFrame();
    private JTextArea text = new JTextArea();
    public Console(){
        super(EMPTY);
    }
    public Console(String title){
        super(EMPTY);
        initialize(frm, text);
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
        this.text.append(String.format(format, args));
        return this;
    }
    @Override public void print(String s){
        this.text.append(s);
    }
    @Override public void println(){
        this.text.append("\n");
    }
    @Override public void println(Object obj){
        this.text.append("" + obj + "\n");
    }
    @Override public void println(String ln){
        this.text.append("" + ln + "\n");
    }
}