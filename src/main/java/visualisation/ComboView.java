package visualisation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	
	private TableModel _tableModel;
	private int _cores;
	private JLabel _statusLabel;
	private static String _fileName;
	
	private JFrame _tableFrame;

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
    	ImageIcon image = new ImageIcon("sinnen-logo.png");
    	Image largeLogo = image.getImage();
    	Image smallLogo = largeLogo.getScaledInstance(200, 50, java.awt.Image.SCALE_SMOOTH);
    	ImageIcon newLogo = new ImageIcon(smallLogo);
    	JLabel logo = new JLabel("", newLogo, JLabel.LEFT);
    	
    	//Timer for program runtime count
    	Clock c = Clock.getInstance();
    	JLabel titleLabel = c.getTimeLabel();   
    	
    	//Label for displaying the program's current status
    	_statusLabel = new JLabel(Clock.getInstance().getProcessStatus().toString(),JLabel.RIGHT);
    	
        //Button to open schedule
        JButton openScheduleBtn = new JButton("See Schedule");
        openScheduleBtn.addActionListener(new ActionListener() 
        {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		_tableFrame.setVisible(true); 
        	}
        });
        
        //Button to stop algorithm
        JButton stopBtn = new JButton("Stop Process");
        
        //Label to represent the .dot file that is being processed
        JLabel fileNameLabel = new JLabel(""+ _fileName, JLabel.CENTER);
        
        // First row of top panel
        _panelTop.add(logo);
    	_panelTop.add(fileNameLabel);
    	_panelTop.add(_statusLabel);
    	
    	// Second row of top panel
    	_panelTop.add(openScheduleBtn);
    	_panelTop.add(titleLabel);
    	_panelTop.add(stopBtn);
    	
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
		JButton button1 = new JButton();
		button1.setText("Scheduling Cores: "+ (_tableModel.getColumnCount()-1));
		button1.setFont(new Font("Trebuchet MS", Font.BOLD, 18));
		button1.setBackground(new Color(255, 193, 193));
	
		/*ImageIcon icon = new ImageIcon("sinnen-logo.png","a pretty but meaningless splat");
		button1.setIcon(icon);*/
		
		// This will be organe when parellel = off, and green when parellel = on.
		JButton button2 = new JButton("Parallelisation: \nFALSE");
		button2.setBackground(new Color(255, 190, 79));
		button2.setFont(new Font("Trebuchet MS", Font.BOLD, 18));

		
		_callsLabel = new JButton("Recursive Calls");
		_callsLabel.setBackground(new Color(178, 219, 255));
		_callsLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 18));

		
		_bestTimeLabel = new JButton("Best Schedule Time");
		_bestTimeLabel.setBackground(new Color(209, 168, 255));
		_bestTimeLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 18));
		
		
		_panelBottom.add(button1);
		_panelBottom.add(button2);
		_panelBottom.add(_callsLabel);
		_panelBottom.add(_bestTimeLabel);
		
		//JLabel randomText = new JLabel();
		//randomText.setText("Some random text here");

        setLocationRelativeTo(null);
    	setVisible(true);
	}
	
	
	private JPanel buildPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fill"));
        buildView(panel);
        return panel;
    }

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
}
