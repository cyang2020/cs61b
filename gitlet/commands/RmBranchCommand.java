package gitlet.commands;
import java.io.File;
import java.io.IOException;
import gitlet.Commit;
import gitlet.Command;
import gitlet.Main;
import gitlet.Utils;

/** The rm branch command.
 * @author Casper Yang
 */
public class RmBranchCommand implements Command {
    @Override
    public void run(String[] args) throws IOException, ClassNotFoundException {
        if (!Main.gitlet.branches.containsKey(args[0])) {
            System.out.println("branch with that name does not exist.");
            return;
        }
        if (Main.gitlet.head.equals(Main.gitlet.branches.get(args[0]))) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        helperBranch(Main.gitlet.branches.get(args[0]), args[0]);
        if (Main.gitlet.count == 0) {
            System.out.println("A branch with that name does not exist");
            return;
        }
        Main.saveState(Main.gitlet);
    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 1;
    }
    /** Helper function for Branch.
     * @param currCom a String.
     * @param branchName a String.
     */
    public void helperBranch(String currCom
            , String branchName) throws IOException,
            ClassNotFoundException {
        File temp = new File(Main.gitlet.workingDir
                , ".gitlet/" + currCom);
        Commit curr = (Commit) Main.deserialize(Utils.readContents(temp));
        if (curr.getParentHash()
                .equals(Main.gitlet.branchToIntCom.get(branchName))) {
            curr.setParentHash(null);
            Main.gitlet.branches.remove(branchName);
            Main.gitlet.branchToIntCom.remove(branchName);
            Main.gitlet.count++;
        } else {
            helperBranch(curr.getParentHash(), branchName);
        }
    }
}
