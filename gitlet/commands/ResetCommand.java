package gitlet.commands;
import gitlet.Command;
import gitlet.Main;
import gitlet.Utils;
import gitlet.Commit;
import java.io.File;
import java.io.IOException;
import static gitlet.Utils.sha1;
import java.util.HashMap;

/** The branch command.
 * @author Casper Yang
 */

public class ResetCommand implements Command {
    @Override
    public void run(String[] args) throws IOException, ClassNotFoundException {
        if (!Main.gitlet.allCommitsIDs.contains(args[0])) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File temp2 = new File(Main.gitlet.workingDir
                , ".gitlet/" + Main.gitlet.head);
        Commit currBranch = (Commit) Main
                .deserialize(Utils.readContents(temp2));
        File temp = new File(Main
                .gitlet.workingDir, ".gitlet/" + args[0]);
        Commit resetCommit
                = (Commit) Main.deserialize(Utils.readContents(temp));
        for (String a : Utils.plainFilenamesIn(Main.gitlet.workingDir)) {
            File file = new File(Main.gitlet.workingDir, a);
            String fileHash = a + sha1(Utils.readContents(file));
            if (resetCommit.getFileMap().containsKey(a)
                    && !resetCommit.getFileMap().get(a).equals(fileHash)
                    && !currBranch.getFileMap().containsKey(a)) {
                System.out.println("There is an untracked "
                        + "file in the way; delete it or add it first.");
                return;
            }
        }
        for (String a : resetCommit.getFileMap().keySet()) {
            CheckoutCommand.checkoutCommit(args[0], a);
        }
        Main.gitlet.stageAdd = new HashMap<>();
        Main.gitlet.head = args[0];
        Main.gitlet.branches.remove(Main.gitlet.headBranch);
        Main.gitlet.branches.put(Main.gitlet.headBranch, args[0]);
        Main.saveState(Main.gitlet);
    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 1;
    }
}
