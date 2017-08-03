package testInput;

import static org.junit.Assert.*;

import org.junit.Test;

import implementations.SchedulerImp;
import interfaces.Scheduler;

public class TestSchedulerImp {

    /**
     * Simple test that tests the current setter.
     */
    @Test
    public void testCurrentSetter() {
    	Scheduler s = new SchedulerImp();
    	s.setCurrentStartTimeAndProcessor(2, 10);
    	assertEquals(s.getCurrentStartTime(), 2);
    	assertEquals(s.getCurrentProcessor(), 10);
    }
    
    /**
     * Simple test that tests the setter for setting the current best.
     */
    @Test
    public void testBestSetter() {
    	Scheduler s = new SchedulerImp();
    	s.setCurrentStartTimeAndProcessor(4, 1);
    	s.setCurrentAsBest();
    	assertEquals(s.getBestProcessor(), 1);
    	assertEquals(s.getBestStartTime(), 4);
    }
}
