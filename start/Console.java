
package start;

import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.function.*;

import javax.swing.*;

public class Console extends PrintStream implements Runnable
{
	//ToDo:
	//add all required method, e.g. Listener
	//& add custom Action for those Listener
	private final JFrame mainFrm;
	private final JTabbedPane mainTab;
	private final JPanel startPage;
	private final JScrollPane consoleTab;
	private final JTextArea consoleText;
	private Component recentTabComp[];
	public Console(){
		this("DefaultTitle");
	}
	public Console(String title){
		this.mainFrm=new JFrame(title);
		this.initializeMainFrm();
		this.mainTab=new JTabbedPane();
		this.mainTab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		this.startPage=new JPanel();
		this.consoleTab=new JScrollPane();
		this.consoleText=new JTextArea();
		//"about Font: "
		//"FontName: Courier New,Monospaced,"
		//"Arial,Consolers"
		//"FontStyle: Font.PLAIN,ITALIC,BOLD"
		this.consoleText.setEditable(false);
		this.consoleText.setFont(new Font("Courier New",Font.PLAIN,12));
		this.consoleText.setTabSize(4);
		this.initializeConsoleText();
		
		this.mainFrm.add(this.mainTab,BorderLayout.CENTER);
		this.consoleTab.setViewportView(this.consoleText);
		this.addTab("Start",this.startPage);
		this.addTab("ConsoleTab",this.consoleTab);
	}

	/*initializeMainFrm**********/
	private void initializeMainFrm(){
		this.mainFrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int base=350;
		double silverRatio=1+Math.pow(2,0.5);
		double goldenRatio=(1+Math.pow(5,0.5))/2;
		this.mainFrm.setSize((int)(base*goldenRatio),base);
		this.mainFrm.setPreferredSize(this.mainFrm.getSize());
		this.mainFrm.setLocationRelativeTo((Component)null);
		this.mainFrm.setVisible(silverRatio>goldenRatio);
	}
	/*initializeConsoleText**********/
	private void initializeConsoleText(){
		String property[]=new String[] {
				"os.name","os.version","os.arch",
				"java.home","java.version","java.vendor",
				"java.class.path","sun.arch.data.model",};
		this.print("Current time is ");
		this.println(DateFormat
				.getDateTimeInstance(DateFormat.MEDIUM,
									DateFormat.MEDIUM,
									Locale.US).format(new Date()));
		for(String prop:property){
			if(prop.equals("java.class.path")){
				String[] arr=System.getProperty(prop).split(";");
				this.println(prop);
				for(String str:arr){
					this.println("\t".concat(str).concat(";"));
				}
				continue;
			}
			this.println(prop.concat(": ").concat(
					System.getProperty(prop)));
		}
		Dimension scr=this.getScreenResolution();
		this.print("Console.resolution: ");
		this.print(Integer.toString(scr.width).concat("*"));
		this.println(Integer.toString(scr.height));
		this.println();
	}
	public void run(){
		while(true){
			try{
				Thread.sleep(250);
			} catch(InterruptedException e){}
			this.mainFrm.setSize(this.mainFrm.getSize());
			this.mainFrm.revalidate();
			this.mainFrm.repaint();this.mainTab.repaint();
			for(int i=0;i<this.recentTabComp.length;++i){
				this.recentTabComp[i].repaint();
			}
			this.mainFrm.pack();
		}
	}
	public void revalidate(){
		this.mainFrm.setSize(this.mainFrm.getSize());
		this.mainFrm.revalidate();
		this.mainFrm.invalidate();this.mainFrm.validate();
	}

	public synchronized void addMainComp(Component comp,Object side){
		this.mainFrm.add(comp,side);
		this.refreshTabCompArray();
	}

	public synchronized void addTab(Component comp){
		this.mainTab.add(comp);
		this.refreshTabCompArray();
	}
	public synchronized void addTab(String title,Component comp){
		if(this.mainTab.indexOfTab(title)==-1
		&& this.mainTab.indexOfTabComponent(comp)==-1){
			this.mainTab.addTab(title,comp);
			this.refreshTabCompArray();
		}
		else{
			this.println("fail to add tab: "+title);
		}
	}
	public synchronized void addTab(String title,Icon ico,
									Component comp,String tip){
		if(this.mainTab.indexOfTab(title)==-1
		&& this.mainTab.indexOfTabComponent(comp)==-1){
			this.mainTab.addTab(title,ico,comp,tip);
			this.refreshTabCompArray();
		}
		else{
			this.println("fail to add tab: "+title);
		}
	}
	public synchronized void insertTab(String s0,Component c,int i){
		if(i<this.mainTab.getComponentCount()
		&& this.mainTab.indexOfTab(s0)==-1
		&& this.mainTab.indexOfTabComponent(c)==-1){
			this.mainTab.insertTab(s0,(Icon)null,c,(String)null,i);
			this.refreshTabCompArray();
		}
		else
			this.println("fail to add tab: "+s0);
	}
	public synchronized void insertTab(String title,Icon ico,
									Component c,String s1,int i){
		if(i<this.mainTab.getComponentCount()
		&& this.mainTab.indexOfTab(title)==-1
		&& this.mainTab.indexOfTabComponent(c)==-1){
			this.mainTab.insertTab(title,ico,c,s1,i);
			this.refreshTabCompArray();
		}
		else
			this.println("fail to add tab: "+title);
	}
	
	public synchronized void removeTab(int index){
		this.mainTab.remove(index);
		this.refreshTabCompArray();
	}
	public synchronized void removeTab(Component comp){
		this.mainTab.remove(comp);
		this.refreshTabCompArray();
	}
	
	public synchronized void setTabIconAt(int index,Icon icon){
		this.mainTab.setIconAt(index, icon);
	}
	
	public synchronized void setTabMnemonicAt(String title,int mne){
		this.mainTab.setMnemonicAt(this.mainTab.indexOfTab(title),mne);
		this.refreshTabCompArray();
	}
	
	public synchronized void refreshTabCompArray(){
		this.recentTabComp
				=new Component[this.mainTab.getComponentCount()];
		for(int i=0;i<this.recentTabComp.length;++i){
			this.recentTabComp[i]=this.mainTab.getComponent(i);
		}
	}
	
	/**get() method**/
	public Dimension getScreenResolution(){
		GraphicsDevice gd=GraphicsEnvironment
				 .getLocalGraphicsEnvironment()
				 .getDefaultScreenDevice();
		return new Dimension(gd.getDisplayMode().getWidth(),
							 gd.getDisplayMode().getHeight());
	}
	public JFrame getMainFrm(){
		return this.mainFrm;
	}
	public Dimension getFrmSize(){
		return this.mainFrm.getSize();
	}
	public int getFrmWidth(){
		return this.mainFrm.getSize().width;
	}
	public int getFrmHeight(){
		return this.mainFrm.getSize().height;
	}
	
	public Dimension getTabSize(){
		return this.mainTab.getSize();
	}
	public int getTabWidth(){
		return this.mainTab.getWidth();
	}
	public int getTabHeight(){
		return this.mainTab.getHeight();
	}
	public int getTabCount(){
		return this.mainTab.getTabCount();
	}
	public int getSelectedTabIndex(){
		return this.mainTab.getSelectedIndex();
	}
	
	public int getTabIndex(String title){
		return this.mainTab.indexOfTab(title);
	}
	public int getTabIndex(Component comp){
		return this.mainTab.indexOfComponent(comp);
	}
	
	public String getTabTitleAt(int index){
		return this.mainTab.getTitleAt(index);
	}
	public Component getTabComponentAt(int index){
		return this.mainTab.getTabComponentAt(index);
	}
	
	/**Console Output**/
	public void println(){
		try{
			this.consoleText.append("\n");
		} catch(NullPointerException npe){
			this.consoleText.append("null");
		}
		System.out.printf("%c",'\n');
        this.consoleText.setCaretPosition(
        		this.consoleText.getDocument().getLength());
	}
	public <T> void println(T str){
		try{
			this.consoleText.append(str.toString().concat("\n"));
		} catch(NullPointerException npe){
			this.consoleText.append("null");
		}
		System.out.println(str);
        this.consoleText.setCaretPosition(
        		this.consoleText.getDocument().getLength());
	}
	public <T> void print(T str){
		try{
			this.consoleText.append(str.toString());
		} catch(NullPointerException npe){
			this.consoleText.append("null");
		}
		System.out.print(str);
        this.consoleText.setCaretPosition(
        		this.consoleText.getDocument().getLength());
	}
	
	public Component[] getRecentTabComp(){
		return this.recentTabComp;
	}
	@Override
	public String toString(){
		return "Class Console,using JTabbedPane in BorderLayout";
	}
	public static int max(int... value){
		int max=value[0];
		for(int i=1;i<value.length;++i){
			max=(max>value[i]?max:value[i]);
		} return max;
	}
	public static double max(double... value){
		double max=value[0];
		for(int i=1;i<value.length;++i){
			max=(max>value[i]?max:value[i]);
		} return max;
	}

	public static int min(int... value){
		int min=value[0];
		for(int i=1;i<value.length;++i){
			min=(min<value[i]?min:value[i]);
		} return min;
	}
	public static double min(double... value){
		double min=value[0];
		for(int i=1;i<value.length;++i){
			min=(min<value[i]?min:value[i]);
		} return min;
	}
	public static <T extends Object> String toString(T obj){
		return obj.toString();
	}
}