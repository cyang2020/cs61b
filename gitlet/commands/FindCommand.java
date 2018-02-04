package gitlet.commands;
import gitlet.Command;
import gitlet.Main;
import gitlet.Utils;
import java.io.File;
import gitlet.Commit;
import java.io.IOException;
/** The find command.
 * @author Casper Yang
 */
public class FindCommand implements Command {

    @Override
    public void run(String[] args) throws IOException, ClassNotFoundException {
        boolean found = false;
        for (String commmitID : Main.gitlet.allCommitsIDs) {
            File temp = new File(Main.gitlet.workingDir
                    , ".gitlet/" + commmitID);
            Commit commit = (Commit) Main.deserialize(Utils.readContents(temp));
            if (commit.getLog().equals(args[0])) {
                found = true;
                System.out.println(commit.getHashName());
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
        Main.saveState(Main.gitlet);
    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 1;
    }
}
