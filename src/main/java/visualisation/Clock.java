package visualisation;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** 
 * Clock class instantiates a global variable - Timer which can print and update time in the format:
 * minutes:seconds:milliseconds.
 * @author Pulkit
 */
public class Clock extends JPanel {

    private Timer _timer = new Timer();
    private JLabel _timeLabel = new JLabel(" ", JLabel.CENTER);
    private int _minutes; 
    private int _seconds;
    private int _milliseconds;
    private static Clock instance = null;
    public static int lastUpdate = 0;
    
    /**
     * Constructor initialises the JPanel and the associated JLabel.
     */
    protected Clock(){
    	// exists only to defeat instantiation.
    }
    
    
    public static Clock getInstance(){
    	if(instance == null) {
			instance = new Clock();
			instance.initClock();
		}
		return instance;
    }
    
    private void initClock() {
        setLayout(new BorderLayout());
        _timeLabel.setFont(new Font("Serif", Font.BOLD, 20)); 
        add(_timeLabel,BorderLayout.NORTH);
        /*
        // button to stop timer.
        JButton btnStop = new JButton("Stop");
        btnStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				timer.cancel();
				timer.purge();
			}
        });
        
        f.add(btnStop,BorderLayout.SOUTH);
        
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);*/
        _minutes = 0; _seconds = 0; _milliseconds = 0;
        _timer.schedule(new UpdateUITask(), 0, 10);
        // stopping the timer:
        //timer.cancel();
        //timer.purge();
    }
    /**
     * Private inner class is used to specify the update code that is run by the timer periodically.
     * @author Pulkit
     *
     */
    private class UpdateUITask extends TimerTask {

        @Override
        public void run() {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    _timeLabel.setText(formatTime());
                }
            });
        }
    }
    
    /**
     * Increments and formats the time based on specific formating rules. 
     * Start time: 00:00:0
     * @return String representing the current time.
     */
    
    public String formatTime(){
    	_milliseconds++;
    	if (_milliseconds > 99 && _seconds == 59){
    		_minutes++;
    		_milliseconds = 0;
    		_seconds = 0;
    	} else if (_milliseconds > 99){
    		_seconds++;
    		_milliseconds = 0;
    	}
    	
    	String secString;
    	if (_seconds < 10){
    		secString = "0" + _seconds;
    	} else {
    		secString = _seconds + "";
    	}
    	
    	String milString;
    	if (_milliseconds < 10){
    		milString = "0" + _milliseconds;
    	} else {
    		milString = _milliseconds   + "";
    	}
    	
    	return _minutes+":"+secString+":"+milString;
    }
    
    /**
     * Gets the number of milliseconds for which the timer has been running.
     * @return integer representing the duration for which the timer has been running.
     */
    public int getMilliseconds(){
    	int time = _milliseconds + _seconds*1000 + _minutes*60*1000;
    	return time;
    }
    
    public Timer getTimer(){
    	return _timer;
    }
    
    public JLabel getTimeLabel(){
    	return _timeLabel;
    }
}