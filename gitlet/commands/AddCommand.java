package gitlet.commands;
import gitlet.Utils;
import gitlet.Command;
import gitlet.Main;
import gitlet.Commit;
import java.io.File;
import java.io.IOException;

import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;
import static gitlet.Utils.writeContents;

/** The add command.
 * @author Casper Yang
 */
public class AddCommand implements Command {
    @Override
    public void run(String[] args)  throws ClassNotFoundException, IOException {
        File file = new File(Main.gitlet.workingDir, args[0]);
        if (!file.existxxxxxxxxxxxxxs()) {
            System.out.println("File does not exist.");
            return;
        }
        if (Main.gitlet.stageRemove.contains(args[0])) {
            Main.gitlet.stageRemove.remove(args[0]);
            Main.saveState(Main.gitlet);
            return;
        }

        byte[] thisfile = readContents(file);
        String shaThis = sha1(thisfile);



        String hash = args[0] + shaThis;
        File temp = new File(Main.gitlet.workingDir
                , ".gitlet/" + Main.gitlet.head);
        Commit target = (Commit) Main.deserialize(Utils.readContents(temp));
        if (Main.gitlet.stageAdd.containsKey(args[0])
                && Main.gitlet.stageAdd.get(args[0]).equals(shaThis)) {
            System.out.println("No change added.");
            return;
        }

        if (hash.equals(target.getFileMap().get(args[0]))) {
            Main.saveState(Main.gitlet);
            return;
        }

        File blob = new File(Main.gitlet.workingDir, ".gitlet/" + hash);
        writeContents(blob, readContents(file));
        Main.gitlet.stageAdd.put(args[0], hash);


        Main.saveState(Main.gitlet);
    }

    @Override
    public boolean validOperands(String[] args) {
        return args.length == 1;
    }
}
