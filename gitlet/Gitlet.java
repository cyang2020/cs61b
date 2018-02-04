package gitlet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;


/**Object for Gitlet, the tiny stupid version-control system.
 *  @author Casper Yang
 */
public class Gitlet implements Serializable {
    /** A Hashmap Branches. */
    public HashMap<String, String> branches = new HashMap<>();
    /** A String head. */
    public String head;
    /** A String headBranch. */
    public String headBranch;
    /** A String workingDir. */
    public String workingDir;
    /** A Hashmap branchToIntCom. */
    public HashMap<String, String> branchToIntCom = new HashMap<>();
    /** A int count. */
    public int count;
    /** A Hashmap stageAdd. */
    public HashMap<String, String> stageAdd = new HashMap<>();
    /** A ArrayList stageRemove. */
    public ArrayList<String> stageRemove = new ArrayList<String>();
    /** A ArrayList allCommitsIDs. */
    public ArrayList<String> allCommitsIDs = new ArrayList<>();
    /** A Hashmap shortCommitsIDs. */
    public HashMap<String, String> shortCommitsIDs = new HashMap<>();
    /** Init a gitlet. */
    public Gitlet() {
        workingDir = System.getProperty("user.dir");
    }
}
