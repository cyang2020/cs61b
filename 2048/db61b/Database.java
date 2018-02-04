package db61b;
import java.util.HashMap;

/** A collection of Tables, indexed by name.
 *  @author Casper Yang*/
class Database {
    /** An empty database. */
    public Database() {
        base = new HashMap<>();
    }


    /** Return the Table whose name is NAME stored in this database, or null
     *  if there is no such table. */
    public Table get(String name) {
        if (base.containsKey(name)) {
            return base.get(name);
        }
        return null;
    }

    /** Set or replace the table named NAME in THIS to TABLE.  TABLE and
     *  NAME must not be null, and NAME must be a valid name for a table. */
    public void put(String name, Table table) {
        if (name == null || table == null) {
            throw new IllegalArgumentException("null argument");
        }
        base.put(name, table);
    }
    /** Creating a database. */
    private HashMap<String, Table> base;
}
