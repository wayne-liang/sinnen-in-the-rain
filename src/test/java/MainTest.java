import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainTest {
    @Test
    public void mainTest() {
        Main main = new Main();
        assertEquals("Hello World!", main.test);
    }
}