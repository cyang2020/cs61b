package gitlet.commands;
import gitlet.Command;
import gitlet.Main;
import gitlet.Commit;
import gitlet.Utils;

import java.io.File;
import java.io.IOException;

/** The log command.
 * @author Casper Yang
 */
public class LogCommand implements Command {
    @Override
    public void run(String[] args) throws IOException, ClassNotFoundException {
        helper(Main.gitlet.head);
        Main.saveState(Main.gitlet);
    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 0;
    }

    /** helpr function for log.
     * @param arg a String.
     */
    public void helper(String arg) throws ClassNotFoundException, IOException {
        File temp = new File(Main.gitlet.workingDir + "/.gitlet", arg);
        Commit head = (Commit) Main.deserialize(Utils.readContents(temp));
        if (head.getParentHash() == null) {
            System.out.println("===");
            System.out.println("commit " + head.getHashName());
            System.out.print("Date: ");
            System.out.println(head.getDate());
            System.out.println(head.getLog());
            System.out.println("");
            return;
        }
        System.out.println("===");
        System.out.println("commit " + head.getHashName());
        System.out.print("Date: ");
        System.out.println(head.getDate());
        System.out.println(head.getLog());
        System.out.println("");
        helper(head.getParentHash());
    }
}
