package db61b;
import org.junit.Test;
import static org.junit.Assert.*;
/** Tests for Database.java
 *  @author Casper Yang*/

public class DatabaseTest {
    /** Tests the Get and Put functions in Database.java. */
    @Test
    public void testDatabase() {
        String[] c = new String[]{"Who", "Are", "You"};
        Table t = new Table(c);
        t.add(new String[]{"I", "am", "IronMan"});
        t.add(new String[]{"I", "am", "SpiderMan"});
        t.add(new String[]{"I", "am", "CaptainAmerica"});

        String[] c1 = new String[]{"We", "are", "Avengers"};
        Table t1 = new Table(c1);
        t1.add(new String[]{"IronMan", "is", "rich"});
        t1.add(new String[]{"SpiderMan", "is", "Poor"});
        t1.add(new String[]{"CaptainAmerica", "is", "Old"});

        Database d1 = new Database();
        d1.put("Superhero", t);
        d1.put("Avengers", t1);

        assertEquals(t, d1.get("Superhero"));
        assertEquals(t1, d1.get("Avengers"));
    }

}
