package com.chen.crawler;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.chen.crawler.utils.DbUtils;

/**
 * WebCrawler
 * <p/>
 * Using TrackingExecutorService to save unfinished tasks for later execution
 *
 * @author chen
 */
public abstract class WebCrawler extends JFrame implements ActionListener {
    private volatile TrackingExecutor exec;
    private Set<URL> urlsToCrawl =  Collections.synchronizedSet(new HashSet<URL>()) ;

    private final ConcurrentMap<URL, Boolean> seen = new ConcurrentHashMap<URL, Boolean>();
    protected static final int TIMEOUT = 500;
    private static final TimeUnit UNIT = MILLISECONDS;
    protected  boolean isStarted = false;
    
    private JButton exitButton = null;
	private JButton stopButton = null;
	private JButton startButton = null;
	
    public JTabbedPane tabbedPane = null;
    
    private static final int width = 30;
    private static final int height = 15;
    
    public WebCrawler(URL startUrl) {
        urlsToCrawl.add(startUrl);
    }
    
    public void init(){
    	try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        //init constant info
        Constant.reflush();

        InitToolBar();
        //InitHomePanel();
        
        setTitle("爬虫程序");
        setVisible(true); 
        
        final ImageIcon backgoundIcon = new ImageIcon(getClass().getResource("/images/crawler.png"));
        ((JPanel)this.getContentPane()).setOpaque(false);
        JLabel background = new JLabel(backgoundIcon);
        background.setBounds(0, 48, backgoundIcon.getIconWidth(), backgoundIcon.getIconHeight());
       // this.getLayeredPane().add(background, Integer.MIN_VALUE);
        
     // JFrame打开后居中。 
        setLocationRelativeTo(getContentPane());
      //设置JFrame的长和宽。 
        setSize(600, 400); 
        //居中
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        
        this.getContentPane().setBackground(Color.BLACK);
        int left = (int) ((screenWidth-this.getWidth())/2);
        int top = (int) ((screenHeight-this.getHeight())/2);
        this.setLocation(left,top);
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/crawler-logo.gif"));
        setIconImage(logoIcon.getImage());
        
//        this.setContentPane(new JPanel() {
//            @Override
//            protected void paintComponent(Graphics g) {
//                // TODO Auto-generated method stub
//                super.paintComponent(g);
//                g.drawImage(backgoundIcon.getImage(), 0, 0, this);
//            }
//        });
        
        //response close window event
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	closeWindow();
            }
        });
    }
    
    public void closeWindow(){
    	 int result = JOptionPane.showConfirmDialog(tabbedPane, "确认关闭程序吗？",
				"关闭程序", JOptionPane.YES_NO_OPTION);
		if (result == 0) {
			
			boolean saveResult = DbUtils.saveCountInfo(Constant.count.get());
			boolean stopResult = false;
			this.setVisible(false);
			try {
				stopResult = stop();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			//延迟一段时间退出，等将一些变量保存到数据
			if(saveResult && stopResult){
				System.exit(0);
			}
			try {
				Thread.sleep(1000 * 1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
    }
    @SuppressWarnings("unchecked")
	public synchronized void start() {
    	isStarted = true;
        exec = new TrackingExecutor(Executors.newCachedThreadPool());
        if(urlsToCrawl!=null){
        	for (URL url : urlsToCrawl) {
        		submitCrawlTask(url);
        	}
         	urlsToCrawl.clear();
        }
      
    }

    public synchronized boolean stop() throws InterruptedException {
    	isStarted = false;
    	boolean result = false;
        try {
        	if(exec!=null){
        		 saveUncrawled(exec.shutdownNow());
                 if (exec.awaitTermination(TIMEOUT, UNIT)){
                	 saveUncrawled(exec.getCancelledTasks());
                 }
        	}
        } finally {
        	result = true;
            exec = null;
        }
        return result;
    }

    protected abstract List<URL> processPage(URL url);

    private void saveUncrawled(List<Runnable> uncrawled) {
        for (Runnable task : uncrawled){
            urlsToCrawl.add(((CrawlTask) task).getPage());
        }
    }
    
    private void submitCrawlTask(URL u) {
    	if(exec!=null)
        exec.execute(new CrawlTask(u));
    }

    protected class CrawlTask implements Runnable {
        private final URL url;

        CrawlTask(URL url) {
            this.url = url;
        }

        private int count = 1;

        boolean alreadyCrawled() {
            return seen.putIfAbsent(url, true) != null;
        }

        void markUncrawled() {
            seen.remove(url);
            System.out.printf("marking %s uncrawled%n", url);
        }

        public void run() {
            for (URL link : processPage(url)) {
                if (Thread.currentThread().isInterrupted())
                    return;
                submitCrawlTask(link);
                System.err.println("submit crawlTask");
            }
        }

        public URL getPage() {
            return url;
        }
    }
    
    public void actionPerformed(ActionEvent e) {
    	if (e.getActionCommand().trim().equals("启动")){
    		if(!isStarted){
    			this.startButton.setEnabled(false);
    			this.stopButton.setEnabled(true);
    			start();
    		}
    	}else if (e.getActionCommand().trim().equals("停止")){
    		if(isStarted){
    			this.startButton.setEnabled(true);
    			this.stopButton.setEnabled(false);
    			try {
					stop();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
    		}
    	}else if (e.getActionCommand().trim().equals("退出")) {
    		closeWindow();
    	}
    }

    private void InitToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        this.startButton = new JButton("启动 ");
        this.startButton.setSize(width,height);
        // 到classpath目录下面去找，需要加/
        ImageIcon startIcon = new ImageIcon(getClass().getResource("/images/stop.png"));
        startIcon = scaleImageIcon(startIcon);
        this.startButton.setIcon(startIcon);
        this.startButton.addActionListener(this);

        this.stopButton = new JButton("停止");
        this.startButton.setSize(width,height);
        
        
        ImageIcon stopIcon = new ImageIcon(getClass().getResource("/images/stop.png"));
        stopIcon = scaleImageIcon(stopIcon);
        this.stopButton.setIcon(stopIcon);
        this.stopButton.addActionListener(this);
        
        
        this.exitButton = new JButton("退出 ");
        this.startButton.setSize(width,height);
        
        ImageIcon exitIcon = new ImageIcon(getClass().getResource("/images/exit.jpg"));
        exitIcon = scaleImageIcon(exitIcon);
        this.exitButton.setIcon(exitIcon);
        this.exitButton.addActionListener(this);

        toolbar.add(this.startButton);
        toolbar.add(this.stopButton);
        toolbar.add(this.exitButton);
        
        getContentPane().add(toolbar, BorderLayout.NORTH);
    }

    private ImageIcon scaleImageIcon(ImageIcon source){
    	if(source==null){
    		return null;
    	}
        Image temp1 = source.getImage().getScaledInstance(startButton.getWidth(),  
        		startButton.getHeight(), source.getImage().SCALE_DEFAULT);  
        source = new ImageIcon(temp1);
        return source;
    }
    
    private ConsoleTextArea getConsoleText(){
    	
    	 ConsoleTextArea consoleTextArea = null;
    	  try {
    	   consoleTextArea = new ConsoleTextArea();
    	  }
    	  catch(IOException e) {
    	   System.err.println(
    	    "cannot create LoopedStreams" + e);
    	  }
    	  consoleTextArea.setFont(java.awt.Font.decode("monospaced"));
          consoleTextArea.setColumns(15);
          consoleTextArea.setRows(5);
    	  return consoleTextArea;
    }
    
    private void InitHomePanel() {
    	
    	JPanel contentPanel = new JPanel();
    	contentPanel.setLayout(new BorderLayout());
    	
    	
        JLabel label1=new JLabel("终端信息：");
        JPanel panel1=new JPanel();
        panel1.add(label1);
        
        contentPanel.add(panel1,BorderLayout.PAGE_START);
        final JTextArea consoleTextArea = getConsoleText();
        consoleTextArea.setEditable(false);
        
        JPanel cosolePanel = new JPanel();
        cosolePanel.add(consoleTextArea);
        final JScrollPane scrollPane=new JScrollPane(cosolePanel);
        contentPanel.add(scrollPane,BorderLayout.CENTER);
        scrollPane.setViewportView(consoleTextArea);
        scrollPane.setAutoscrolls(true);
        //实现自动滚屏
        ScheduledExecutorService autoScrollTask = Executors.newSingleThreadScheduledExecutor();
        autoScrollTask.scheduleAtFixedRate(new Runnable(){

			@Override
			public void run() {
				JScrollBar sBar = scrollPane.getVerticalScrollBar();
				sBar.setValue(consoleTextArea.getDocument().getLength()); 
			}
        	
        }, 2, 1, TimeUnit.SECONDS);
       
        getContentPane().add(contentPanel,BorderLayout.CENTER);
    }
    
   // 设置Frame的背景图
//    public void paint(Graphics g) { 
//    	final ImageIcon backgoundIcon = new ImageIcon(getClass().getResource("/images/crawler.png"));
//    	g.drawImage(backgoundIcon.getImage(), 0, 0, this.getWidth(), this .getHeight(), this); // 设置适应窗口 super.paint(g); } };
//    }

}
