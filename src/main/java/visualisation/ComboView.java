package visualisation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import implementations.structures.DAGImp;
import interfaces.structures.DAG;
import net.miginfocom.swing.MigLayout;
import scala.xml.include.sax.Main;
/**
 * Common GUI Interface to be used to display all visualisation components.
 * @author Pulkit
 *
 */
@SuppressWarnings("serial")
public class ComboView extends JFrame {
	
	private JPanel _contentPane;
	private JPanel _panelTop;
	private JPanel _panelLeft;
	private JPanel _panelMiddle;
	private JPanel _panelRight;
	private TableModel _tableModel;
	private int _cores;
	private JLabel statusLabel;
	private static String _fileName;

	/**
	 * Create the frame.
	 * @param _numberOfCores 
	 * @param _dag 
	 */
	public ComboView(TableModel tableModel, DAG dag, int numberOfCores, BarChartModel chart) {
		_tableModel = TableModel.getInstance();
		_cores = numberOfCores;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1300, 700);
		_contentPane = buildPanel();
		_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(_contentPane);
		
		//Clock.getInstance().setProcessStatus(ProcessStatus.INPROGRESS);
		// Adding GraphStream
		GraphStreamView gv = new GraphStreamView(_cores);
		JPanel p = gv.getPanel();
		p.setVisible(true);
		_panelLeft.add(p);
		
		// Adding JTable
		GraphViewImp table = new GraphViewImp(_tableModel);
		JScrollPane pane = table.getPane();
		JFrame tableFrame = new JFrame();
		tableFrame.setSize(500, 520);
		tableFrame.add(pane);
		tableFrame.setLocationRelativeTo(null);

		// Adding Bar Chart
		_panelMiddle.add(chart);
		
		// Setting up the "Console" Panel
		// Button to open schedule:
		JButton openSchedule = new JButton("See Schedule");
		openSchedule.addActionListener(new ActionListener() 
		{
			@Override
		    public void actionPerformed(ActionEvent e) {
		        tableFrame.setVisible(true); 
		    }
		});
		// Other info here:
		JLabel randomText = new JLabel();
		randomText.setText("Some random text here");
		
		// Add components to Panel
		_panelRight.add(openSchedule,BorderLayout.NORTH);
		_panelRight.add(randomText,BorderLayout.SOUTH);

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
    	_panelTop = new JPanel();
    	_panelTop.setLayout(new GridLayout(1,4));
    	Clock c = Clock.getInstance();
    	JLabel titleLabel = c.getTimeLabel();
    	statusLabel = new JLabel(Clock.getInstance().getProcessStatus().toString());
    	_panelTop.add(new JLabel(""+ _fileName));
    	_panelTop.add(titleLabel);
    	_panelTop.add(statusLabel);
    	//titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));

        JLabel progressLabel = new JLabel("Progress Label");
        JProgressBar progressBar = new JProgressBar(0,100);
        progressBar.setValue(20);
        
        // JPanel for Graph Stream:
        _panelLeft = new JPanel();
        _panelLeft.setLayout(new BorderLayout());
        
        // JPanel for Bar Chart:
        _panelMiddle = new JPanel();
        _panelMiddle.setLayout(new BorderLayout());
        
        // JPanel for "console":
        _panelRight = new JPanel();
        _panelRight.setLayout(new BorderLayout());

        JButton stopBttn = new JButton("Stop Process");
        JButton helpBttn = new JButton("Help");
        JButton quitBttn = new JButton("Quit");
    	
        panel.add(_panelTop, "span, center, height 10%");

        //wrap keyword starts a new row
        panel.add(progressLabel, "align label, height 10%");
        panel.add(progressBar, "wrap");

        //align label triggers platform-specific label alignment
        panel.add(_panelLeft, "width 40%,height 70%");
        panel.add(_panelMiddle, "width 45%, height 70%");
        panel.add(_panelRight, "wrap, width 15%, height 70%");

        //tag identifies the type of button
        panel.add(stopBttn, "tag ok, span, split 3, sizegroup bttn, height 10%");

        //sizegroups set all members to the size of the biggest member
        panel.add(helpBttn, "tag help2, sizegroup bttn");
        panel.add(quitBttn, "tag ok, sizegroup bttn");
    }
    
    public static void setFileName(String name){
    	_fileName = name;
    }
}
