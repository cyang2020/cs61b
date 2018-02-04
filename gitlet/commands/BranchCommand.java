package gitlet.commands;
import gitlet.Command;
import gitlet.Main;

import java.io.IOException;


/** The branch command.
 * @author Casper Yang
 */
public class BranchCommand implements Command {

    @Override
    public void run(String[] args) throws IOException {
        if (Main.gitlet.branches.containsKey(args[0])) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Main.gitlet.branches.put(args[0], Main.gitlet.head);
        Main.gitlet.branchToIntCom.put(args[0], Main.gitlet.head);
        Main.saveState(Main.gitlet);
    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 1;
    }

}
