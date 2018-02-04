package gitlet.commands;
import gitlet.Command;
import gitlet.Main;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.IOException;

/** The status command.
 * @author Casper Yang
 */

public class StatusCommand implements Command {
    @Override
    public void run(String[] args) throws IOException {
        System.out.println("=== Branches ===");
        List<String> array = new ArrayList<String>(Main.gitlet.branches.keySet());
        java.util.Collections.sort(array);
        for (String i : array) {
            if (i.equals(Main.gitlet.headBranch)) {
                System.out.println("*" + i);
            } else {
                System.out.println(i);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        array = new ArrayList<String>(Main.gitlet.stageAdd.keySet());
        java.util.Collections.sort(array);
        for (String i : array) {
            System.out.println(i);
        }

        System.out.println();

        System.out.println("=== Removed Files ===");
        Collections.sort(Main.gitlet.stageRemove);
        for (String i : Main.gitlet.stageRemove) {
            System.out.println(i);
        }

        System.out.println();


        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");


        Main.saveState(Main.gitlet);
    }

    @Override
    public boolean validOperands(String[] args) {
        return args.length == 0;
    }
}
