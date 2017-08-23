package visualisation;

// This code demonstrates the implementation of the depricated suspend() and
//resume() methods of class Thread. To make it possible, the StopWatch
//thread should be implemented as Runnable rather than extending the class
//Thread. 

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;

public class StopWatch extends JApplet implements ActionListener
{
	private JButton suspendB = new JButton("Suspend");
	private JButton resumeB = new JButton("Resume");
	private JLabel display = new JLabel("", JLabel.CENTER);
	private StopWatchThread stopWatch;

	public void init()
	{
		Container pane = getContentPane();        // build GUI
		pane.setLayout(new BorderLayout());
		pane.setBackground(Color.orange);
		display.setFont(new Font("Monospace", Font.BOLD, 24));
		pane.add(display, BorderLayout.CENTER);
		JPanel controlPanel = new JPanel();
		controlPanel.add(suspendB);
		controlPanel.add(resumeB);
		controlPanel.setBackground(Color.lightGray);
		pane.add(controlPanel, BorderLayout.SOUTH);

		suspendB.addActionListener(this);         // button listeners
		resumeB.addActionListener(this);
		stopWatch = new StopWatchThread(display); // create and start stopwatch
	}

	public void actionPerformed(ActionEvent e)  // stopwatch control center
	{
		if (e.getSource() == suspendB)
			stopWatch.suspend();
		else if (e.getSource() == resumeB)
			stopWatch.resume();
	}     
}

class StopWatchThread implements Runnable     // stopwatch thread 
{
	private boolean suspended = false;          // stopwatch state
	private JLabel display;                     // applet's time display

	public StopWatchThread(JLabel l)            // thread constructor
	{
		display = l;                              // ref. to applet's display
		(new Thread(this)).start();               // create and start thread
	}

	public void run()
	{
		while(true)
		{
			Calendar calendar = Calendar.getInstance();      // get time instance
			int hours = calendar.get(Calendar.HOUR_OF_DAY);
			int minutes =  calendar.get(Calendar.MINUTE);
			int seconds = calendar.get(Calendar.SECOND);
			display.setText(hours + ":" +                    // update clock display
					((minutes > 9) ? minutes : "0" + minutes) + ":" +
					((seconds > 9) ? seconds : "0" + seconds));

			try                                      // wait 1 sec for the       
			{                                        // next update
				Thread.sleep(1000);
				waitIfSuspended();                     // proceed if not suspended
			}
			catch(InterruptedException e){}
		}
	}

	public synchronized void suspend()           
	{                                         
		suspended = true;
	}

	public synchronized void resume()
	{
		if (suspended)
		{
			suspended = false;
			notifyAll();                             // move the thread back to 
		}                                          // the runnable state
	}

	private synchronized void waitIfSuspended() throws InterruptedException
	{
		while (suspended)                          // moving the thread to
			wait();                                  // the blocked state
	} 
}