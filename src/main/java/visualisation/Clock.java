package visualisation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JPanel;

/** 
 * Single Clock class instantiates a global variable - Timer which can print and update time in the format:
 * minutes:seconds:milliseconds.
 * @author Pulkit
 */
@SuppressWarnings("serial")
public class Clock extends JPanel {

    private Timer _timer = new Timer();
    private JLabel _timeLabel = new JLabel(" ", JLabel.CENTER);
    private int _minutes; 
    private int _seconds;
    private int _milliseconds;
    private ProcessStatus _processStatus;
    private static Clock instance = null;
    
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
    /**
     * sets up the clock label, sets up the timer
     */
    private void initClock() {
        setLayout(new BorderLayout());
        _timeLabel.setFont(new Font("Trebuchet MS", Font.BOLD, 35));
        add(_timeLabel,BorderLayout.NORTH);
        setProcessStatus(ProcessStatus.INPROGRESS);

        _minutes = 0; _seconds = 0; _milliseconds = 0;
        _timer.schedule(new UpdateUITask(), 0, 10);
    }
    /**
     * Private inner class is used to specify the update code that is run by the timer periodically.
     * @author Pulkit
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
    /**
     * Returns an instance of the singleton instance of the timer
     * @return Timer singleton object.
     */
    public Timer getTimer(){
    	return _timer;
    }
    
    /**
     * returns the JLabel that contains the color and text of the JLabel.
     * @return JLabel for the clock
     */
    public JLabel getTimeLabel(){
    	return _timeLabel;
    }
    
    /**
     * Method used to stop the clock when program is finished.
     */
    public void stopClock(){
    	setProcessStatus(ProcessStatus.COMPLETED);
    	Clock.getInstance().getTimer().cancel();
		Clock.getInstance().getTimer().purge();
		_timeLabel.setForeground(new Color(37, 200, 19));
    }
    /**
     * Gets the current status of the process.
     * @return Enum for process status.
     */
	public ProcessStatus getProcessStatus() {
		return _processStatus;
	}
	
	/**
	 * Set the status of the process.
	 * @param processStatus
	 */
	public void setProcessStatus(ProcessStatus processStatus) {
		_processStatus = processStatus;
	}
}