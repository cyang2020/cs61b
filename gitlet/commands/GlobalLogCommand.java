package gitlet.commands;
import gitlet.Command;
import gitlet.Main;
import java.io.IOException;
import java.io.File;
import gitlet.Commit;
import gitlet.Utils;
/** The global log command.
 * @author Casper Yang
 */
public class GlobalLogCommand implements Command {

    @Override
    public void run(String[] args)throws ClassNotFoundException, IOException {
        for (String commmitID : Main.gitlet.allCommitsIDs) {
            File temp = new File(Main.gitlet.workingDir
                    , ".gitlet/" + commmitID);
            Commit curr = (Commit) Main.deserialize(Utils.readContents(temp));
            System.out.println("===");
            System.out.println("commit " + curr.getHashName());
            System.out.print("Date: ");
            System.out.println(curr.getDate());
            System.out.println(curr.getLog());
            System.out.println("");

        }
        Main.saveState(Main.gitlet);
    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 0;
    }
}
