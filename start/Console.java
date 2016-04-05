
package start;

import static start.StartConstants.*;

import java.awt.Font;
import java.io.*;
import java.util.*;

import javax.swing.*;

public class Console extends PrintStream
implements java.awt.event.WindowListener
{
    public static final PrintStream OUT = System.out;
    public static final PrintStream ERR = System.err;
    private static final Font MONOSPACED = new Font("Monospaced", 0, 12);
    public static List<Thread> ALLOWED = new LinkedList<Thread> ();
    public static boolean RESTRICT = false;

    private static final Console THIS = new Console(OUT, false);
    public static final JFrame FRAME = ((
            java.util.function.Supplier<JFrame>)() ->{
                JFrame frm = new JFrame();
                frm.setSize(900, 580);
                frm.addWindowListener(THIS);
                return frm;
            }).get();
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
                
                FRAME.add(scroll);
                FRAME.setLocationRelativeTo(null);
                //if(INST != null) FRAME.setVisible(true);
                FRAME.setVisible(true);
                
                System.setOut(THIS);
                // System.setErr(new Start(ERR, true));
                return text;
            }).get();

    
    

/**
 * java.awt.event.WindowListener
 */
    @Override public void windowActivated(java.awt.event.WindowEvent we){}
    @Override public void windowClosed(java.awt.event.WindowEvent we){}
    @Override public void windowClosing(java.awt.event.WindowEvent we){
        System.exit(0);
    }
    @Override public void windowDeactivated(
                java.awt.event.WindowEvent we){}
    @Override public void windowDeiconified(
                java.awt.event.WindowEvent we){}
    @Override public void windowIconified(java.awt.event.WindowEvent we){}
    @Override public void windowOpened(java.awt.event.WindowEvent we){}

/* InvocationHandler
// failed as this method only allow interfaces
    @Override public Object invoke(
            Object proxy, Method method, Object[] args) throws Throwable {
        OUT.println("HELLO");
        return method.invoke(OUT, args);
    }
*/
//* PrintStream
    private static final PrintStream LOG = ((
            java.util.function.Supplier<PrintStream>)() ->{
                    if(DEBUG){ try{ return new PrintStream(
                    new File(PARENT_DIRECTORY, "start.log"));
                    } catch(FileNotFoundException fnfe){
                    } catch(SecurityException se){}
                    OUT.println("logging creation failed");} // if ends
                    return null;}).get();
    private static final PrintStream LOG_ERR = ((
            java.util.function.Supplier<PrintStream>)() ->{
                    if(DEBUG){ try{ return new PrintStream(
                    new File(PARENT_DIRECTORY, "start.err.log"));
                    } catch(Exception e){}
                    OUT.println("err log stream creation failed");}
                    return null;}).get();
    @Override public synchronized PrintStream printf(
            String format, Object... args){
        if(RESTRICT && !ALLOWED.contains(
                Thread.currentThread())) return this;
        (isErr ? ERR : OUT).printf(format, args);
        if(TEXT.isDisplayable()) TEXT.append(String.format(format, args));
        if(DEBUG){
            if(isErr && LOG_ERR != null) LOG_ERR.printf(format, args);
            else if(!isErr && LOG != null) LOG.printf(format, args);
        }
        return this;
    }
    // primary methods are print(String) and println()
    // avoided super to prevent unpredicted behaviour
    @Override public synchronized void print(String s){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        (isErr ? ERR : OUT).print(s);
        if(TEXT.isDisplayable()) TEXT.append(s);
        if(DEBUG){
            if(isErr && LOG_ERR != null) LOG_ERR.print(s);
            else if(!isErr && LOG != null) LOG.print(s);
        }
    }
    @Override public synchronized void println(){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        (isErr ? ERR : OUT).println();
        if(TEXT.isDisplayable()) TEXT.append("\n");
        if(DEBUG){
            if(isErr && LOG_ERR != null) LOG_ERR.println();
            else if(!isErr && LOG != null) LOG.println();
        }
    }
    @Override public synchronized void println(Object obj){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        (isErr ? ERR : OUT).print("" + obj + "\n");
        if(TEXT.isDisplayable()) TEXT.append("" + obj + "\n");
        if(DEBUG){
            if(isErr && LOG_ERR != null) LOG_ERR.print(obj);
            else if(!isErr && LOG != null) LOG.print(obj);
        }
    }
    @Override public synchronized void println(String ln){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        (isErr ? ERR : OUT).print(ln + "\n");
        if(TEXT.isDisplayable()) TEXT.append(ln + "\n");
        if(DEBUG){
            if(isErr && LOG_ERR != null) LOG_ERR.println(ln);
            else if(!isErr && LOG != null) LOG.println(ln);
        }
    }
    /* OutputStream
    @Override public synchronized void write(int b){
        if(RESTRICT && !ALLOWED.contains(Thread.currentThread())) return;
        OUT.print(Character.toString((char)b));
        if(TEXT.isDisplayable()){
            TEXT.append(Character.toString((char)b));
        }
        if(DEBUG && LOG != null) try{
            LOG.write(b);
        } catch(IOException ioe){}
    }
    */
//*/

    private final boolean isErr;
    public Console(PrintStream ps, boolean isErr){
        super(ps, true);
        this.isErr = isErr;
    }
}