package gitlet.commands;
import gitlet.Command;
import gitlet.Main;
import java.io.File;
import gitlet.Commit;
import gitlet.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static gitlet.Utils.writeContents;
/** The commit command.
 * @author Casper Yang
 */
public class CommitCommand implements Command  {
    @Override
    public void run(String[] args) throws IOException, ClassNotFoundException {
        if (Main.gitlet.stageAdd.isEmpty()
                && Main.gitlet.stageRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        String message = args[0];

        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        HashMap<String, String> blobMap = new HashMap<>();
        File temp = new File(Main.gitlet.workingDir
                , ".gitlet/" + Main.gitlet.head);
        Commit parent = (Commit) Main.deserialize(Utils.readContents(temp));

        if (parent.getFileMap() != null) {
            blobMap.putAll(parent.getFileMap());
        }

        blobMap.putAll(Main.gitlet.stageAdd);
        for (String i: Main.gitlet.stageRemove) {
            blobMap.remove(i);
        }

        Main.gitlet.stageAdd = new HashMap<>();
        Main.gitlet.stageRemove = new ArrayList<>();

        Commit c = new Commit(Main.gitlet.head, args[0], blobMap);

        File newCommit = new File(Main.gitlet.workingDir
                , ".gitlet/" + c.getHashName());
        writeContents(newCommit, Main.serialize(c));

        Main.gitlet.allCommitsIDs.add(c.getHashName());
        String shortID = c.getHashName().substring(0, 8);
        Main.gitlet.shortCommitsIDs.put(shortID, c.getHashName());

        Main.gitlet.head = c.getHashName();

        Main.gitlet.branches.put(Main.gitlet.headBranch, Main.gitlet.head);

        Main.saveState(Main.gitlet);

    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 1;
    }
}
