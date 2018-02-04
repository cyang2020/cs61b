package gitlet.commands;
import gitlet.Command;
import gitlet.Commit;
import gitlet.Main;
import gitlet.Utils;

import java.io.File;
import java.io.IOException;


/** The rm command.
 * @author Casper Yang
 */

public class RmCommand implements Command {
    @Override
    public void run(String[] args) throws ClassNotFoundException, IOException {
        if (Main.gitlet.stageRemove.contains(args[0])) {
            System.out.println("Already in remove list");
            return;
        }

        if (Main.gitlet.stageAdd.containsKey(args[0])) {
            Main.gitlet.stageAdd.remove(args[0]);
            Main.saveState(Main.gitlet);
            return;
        }
        File temp = new File(Main.gitlet.workingDir, ".gitlet/" + Main.gitlet.head);
        Commit target = (Commit) Main.deserialize(Utils.readContents(temp));

        File fileToDelete = new File(Main.gitlet.workingDir, args[0]);

        if (target.getFileMap().containsKey(args[0])) {
            Main.gitlet.stageRemove.add(args[0]);

            if (fileToDelete.exists()) {

                fileToDelete.delete();
            }
            if (Main.gitlet.stageAdd.containsKey(args[0])) {
                Main.gitlet.stageAdd.remove(args[0]);
            }
            Main.saveState(Main.gitlet);
            return;
        } else if (Main.gitlet.stageAdd.containsKey(args[0])) {
            Main.gitlet.stageAdd.remove(args[0]);
            Main.saveState(Main.gitlet);
            return;
        }
        System.out.println("No reason to remove the file.");
        return;
    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 1;
    }
}
