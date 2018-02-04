package gitlet.commands;
import gitlet.Command;
import gitlet.Commit;
import gitlet.Main;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import static gitlet.Utils.writeContents;

/** The init command.
 * @author Casper Yang
 */
public class InitCommand implements Command {
    @Override
    public void run(String[] args) throws IOException {
        HashMap<String, String> empty = new HashMap<>();
        Commit initialCommit = new Commit(null, "initial commit", empty);
        File getHeader = new File(Main.gitlet.head, ".gitlet");
        File intro = new File(".gitlet");
        if (intro.exists()) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        getHeader.mkdir();
        File newCommit = new File(Main.gitlet.workingDir
                , ".gitlet/" + initialCommit.getHashName());
        writeContents(newCommit, Main.serialize(initialCommit));
        Main.gitlet.branches.put("master", initialCommit.getHashName());
        Main.gitlet.head = Main.gitlet.branches.get("master");
        Main.gitlet.headBranch = "master";
        Main.gitlet.allCommitsIDs.add(initialCommit.getHashName());

    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 0;
    }

}
