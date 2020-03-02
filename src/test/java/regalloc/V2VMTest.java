package regalloc;

import java.io.*;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class V2VMTest {

    private static final InputStream DEFAULT_STDIN = System.in;
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void rollbackChangesToStdin() {
        try {
            outContent.reset();
            errContent.reset();
        } catch (Exception e) {

        }

        System.setIn(DEFAULT_STDIN);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void basicTest() {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/1-Basic.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }
        V2VM.allocRegs();

        assertEquals("ALWAYS FAIL", outContent.toString());
    }

    @Test
    public void loopTest() {
        try {
            File inputFile = new File("./tester/Phase3Tester/SelfTestCases/2-Loop.vapor");
            System.setIn(new FileInputStream(inputFile));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            fail();
        }
        V2VM.allocRegs();

        assertEquals("ALWAYS FAIL", outContent.toString());
    }
}
