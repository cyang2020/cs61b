package db61b;
import org.junit.Test;
import static org.junit.Assert.*;

/** /** Tests for Table.java
 *  @author Casper Yang*/

public class TableTest {

    /**
     * Sample tests to make sure that size method
     * works properly.
     */
    @Test
    /** Tests the columns function in Table.java. */
    public void testColumn() {
        Table t = new Table(new String[]{"Iron", "Man"});
        assertEquals(2, t.columns());
        Table td = new Table(new String[]{"I", "am", "iron", "man"});
        assertEquals(4, td.columns());
    }
    @Test
    /** Tests the getTitle function in Table.java. */
    public void testGet() {
        Table t = new Table(new String[] {"Iron", "Man", "Yay"});
        assertEquals("Yay", t.getTitle(2));
    }
    @Test
    /** Tests the Add function in Table.java. */
    public void testAdd() {
        String[] c = new String[]{"Who", "Are", "You"};
        Table t = new Table(c);
        t.add(new String[]{"I", "am", "IronMan"});
        t.add(new String[]{"I", "am", "SpiderMan"});
        t.add(new String[]{"I", "am", "CaptainAmerica"});
        assertEquals(3, t.size());
    }
    @Test
    /** Tests the Print function in Table.java. */
    public void testPrint() {
        String[] c = new String[]{"Who", "Are", "You"};
        Table t = new Table(c);
        t.add(new String[]{"I", "am", "IronMan"});
        t.add(new String[]{"I", "am", "SpiderMan"});
        t.add(new String[]{"I", "am", "CaptainAmerica"});
    }


}

