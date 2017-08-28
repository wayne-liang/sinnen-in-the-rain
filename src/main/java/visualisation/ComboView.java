package visualisation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import implementations.structures.DAGImp;
import interfaces.structures.DAG;
import interfaces.structures.NodeSchedule;
import net.miginfocom.swing.MigLayout;
import scala.xml.include.sax.Main;
/**
 * Common GUI Interface to be used to display all visualisation components.
 * @author Pulkit AND Darius.
 *
 */
@SuppressWarnings("serial")
public class ComboView extends JFrame {
	//Panels:
	private JPanel _contentPane;
	private JPanel _panelTop;
	private JPanel _panelLeft;
	private JPanel _panelRight;
	private JPanel _panelBottom;
	// Button-Labels:
	private JButton _callsLabel;
	private JButton _bestTimeLabel; 
	// Model:
	private TableModel _tableModel;
	private int _cores;
	// Labels
	private JLabel _statusLabel;
	private static String _fileName;
	// Extra JFrame:
	private JFrame _tableFrame;
	// Icons
	private ImageIcon _iconDone;
	// Buttons
	private JButton _stopBtn;

	/**
	 * Create the frame.
	 * @param _numberOfCores 
	 * @param _dag 
	 */
	public ComboView(TableModel tableModel, DAG dag, int numberOfCores, BarChartModel chart) {
		
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
			e.printStackTrace();
		}
		
		_tableModel = TableModel.getInstance();
		_cores = numberOfCores;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1300, 700);
		_contentPane = buildPanel();
		_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(_contentPane);
		
		//Clock.getInstance().setProcessStatus(ProcessStatus.INPROGRESS);
		
    	//Sinnen-in-the-rain group logo
    	ImageIcon image = new ImageIcon("images/sinnen-logo.png");
    	Image largeLogo = image.getImage();
    	Image smallLogo = largeLogo.getScaledInstance(200, 50, java.awt.Image.SCALE_SMOOTH);
    	ImageIcon newLogo = new ImageIcon(smallLogo);
    	JLabel logo = new JLabel("", newLogo, JLabel.LEFT);
    	
    	//Timer for program runtime count
    	Clock c = Clock.getInstance();
    	JLabel titleLabel = c.getTimeLabel();   
    	
    	// Add Label for displaying the program's current status inside a JPanel.
    	
    	JPanel statusPanel = new JPanel(new BorderLayout());
    	statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    	
    	// setting up image icons
    	ImageIcon iconLoader = new ImageIcon("images/ajax-loader.gif");
    	// Icon made by Maxim Basinski from www.flaticon.com
    	_iconDone = new ImageIcon("images/checked.png");
    	// Icon made by Freepik from www.flaticon.com
    	ImageIcon iconSchedule = new ImageIcon("images/tableIcon.png");
    	// Icon made by Madebyoliver from www.flaticon.com
    	ImageIcon iconStop = new ImageIcon("images/stopIcon.png"); 
    	
    	_statusLabel = new JLabel("In Progress");
    	_statusLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 18));
    	_statusLabel.setIcon(iconLoader);
      	_statusLabel.setHorizontalTextPosition(JLabel.CENTER);
    	_statusLabel.setVerticalTextPosition(JLabel.TOP);
    	
    	statusPanel.add(_statusLabel, BorderLayout.EAST);
    	
    	// Panels to put buttons in:
    	JPanel panelScheduleBtn = new JPanel(new BorderLayout());
    	panelScheduleBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    	JPanel panelStopBtn = new JPanel(new BorderLayout());
    	panelStopBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    	
        //Button to open schedule
        JButton openScheduleBtn = new JButton("See Schedule");
        openScheduleBtn.setFont(new Font("Trebuchet MS", Font.PLAIN, 16));
        openScheduleBtn.setIcon(iconSchedule);
        openScheduleBtn.setIconTextGap(10);
        openScheduleBtn.setPreferredSize(new Dimension(200,15));
        openScheduleBtn.addActionListener(new ActionListener() 
        {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		_tableFrame.setVisible(true); 
        	}
        });
        panelScheduleBtn.add(openScheduleBtn, BorderLayout.WEST);
        
        //Button to stop algorithm
        _stopBtn = new JButton("Stop Process");
        _stopBtn.setFont(new Font("Trebuchet MS", Font.PLAIN, 16));
        _stopBtn.setIcon(iconStop);
        _stopBtn.setIconTextGap(10);
        _stopBtn.setPreferredSize(new Dimension(200,15));
        _stopBtn.addActionListener(new ActionListener() 
        {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		int confirmDialog = JOptionPane.YES_NO_OPTION;
        		confirmDialog = JOptionPane.showConfirmDialog (null, "Are you sure you want to stop the"
        				+ " process? \nThis will end the current program and exit the interface.","Warning",confirmDialog);
        		if(confirmDialog == JOptionPane.YES_OPTION){
        			System.exit(0);
        		}
        	}
        });
        panelStopBtn.add(_stopBtn, BorderLayout.EAST);
        
        
        //Label to represent the .dot file that is being processed
        JLabel fileNameLabel = new JLabel(""+ _fileName, JLabel.CENTER);
        fileNameLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 20));
        
        // First row of top panel
        _panelTop.add(logo);
    	_panelTop.add(fileNameLabel);
    	_panelTop.add(statusPanel);
    	
    	// Second row of top panel
    	_panelTop.add(panelScheduleBtn);
    	_panelTop.add(titleLabel);
    	_panelTop.add(panelStopBtn);
    	
		// Adding GraphStream to left side
		GraphStreamView gv = new GraphStreamView(_cores);
		JPanel p = gv.getPanel();
		p.setVisible(true);
		_panelLeft.add(p);
		
		// Making JFrame to display the schedule
		GraphViewImp table = new GraphViewImp(_tableModel);
		JScrollPane pane = table.getPane();
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		_tableFrame = new JFrame();
		_tableFrame.setSize(500, 520);
		_tableFrame.add(pane);
		_tableFrame.setLocationRelativeTo(null);

		// Adding Bar Chart
		_panelRight.add(chart);
		
		// Populating Bottom Panel:
		JButton coresLabel = new JButton();
		coresLabel.setText("Scheduling Cores: "+ (_tableModel.getColumnCount()-1));
		coresLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 18));
		coresLabel.setBackground(new Color(255, 193, 193));

		// TODO: This will be orange when parallel = off, and green when parallel = on.
		JButton parallelLabel = new JButton("Parallelisation: \nFALSE");
		parallelLabel.setBackground(new Color(255, 190, 79));
		parallelLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 18));

		
		_callsLabel = new JButton("Recursive Calls");
		_callsLabel.setBackground(new Color(178, 219, 255));
		_callsLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 18));

		
		_bestTimeLabel = new JButton("Best Schedule Time");
		_bestTimeLabel.setBackground(new Color(209, 168, 255));
		_bestTimeLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 18));
		
		
		_panelBottom.add(coresLabel);
		_panelBottom.add(parallelLabel);
		_panelBottom.add(_callsLabel);
		_panelBottom.add(_bestTimeLabel);
		
		//JLabel randomText = new JLabel();
		//randomText.setText("Some random text here");

        setLocationRelativeTo(null);
    	setVisible(true);
	}
	
	/**
	 * Method called by constructor to specify main panel and it's layout.
	 * @return JPanel with MigLayout.
	 */
	private JPanel buildPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fill"));
        buildView(panel);
        return panel;
    }
	/**
	 * Method called by buildPanel to contruct the main panel with the several components
	 * @param panel
	 */
    private void buildView(JPanel panel) {
    	//Creating the panel for the top part. This will contain the logo, the file name, the 
    	//program status, the schedule button, the timer and the stop button.
    	_panelTop = new JPanel();
    	_panelTop.setLayout(new GridLayout(2,3, 20,20));
        
        // JPanel for Graph Stream:
        _panelLeft = new JPanel();
        _panelLeft.setLayout(new BorderLayout());
        
        // JPanel for Bar Chart:
        _panelRight = new JPanel();
        _panelRight.setLayout(new BorderLayout());
        
        // JPanel for additional information at the bottom
        _panelBottom = new JPanel();
        _panelBottom.setLayout(new GridLayout());
 
        panel.add(_panelTop, "span, center, width 100%, height 10%, wrap");

        //align label triggers platform-specific label alignment
        panel.add(_panelLeft, "width 50%,height 67%");
        panel.add(_panelRight, "width 50%, height 67%, wrap");

        
        panel.add(_panelBottom, "span, center, width 100%, height 13%");
    }

    public static void setFileName(String name){
    	_fileName = name;
    }
    
    public void setCallsButtonText(int calls){
    	_callsLabel.setText("Recursive Calls: " + calls);
    }
    
    public void setBestTimeText(int time){
    	_bestTimeLabel.setText("Best Schedule Time: "+time);
    }
    
    public void setStatusLabel(ProcessStatus status){
    	_stopBtn.setEnabled(false);
    	_statusLabel.setIcon(_iconDone);
    	_statusLabel.setText(status.toString());
    	_statusLabel.setHorizontalTextPosition(JLabel.LEFT);
    	_statusLabel.setVerticalTextPosition(JLabel.TOP);
    	_statusLabel.setIconTextGap(10);
    }
}
