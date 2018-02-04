package gitlet.commands;
import gitlet.Command;
import gitlet.Main;
import java.io.File;
import gitlet.Utils;
import gitlet.Commit;
import java.io.IOException;
import static gitlet.Utils.sha1;


/** The checkout command.
 * @author Casper Yang
 */
public class CheckoutCommand implements Command {

    @Override
    public void run(String[] args) {
        try {
            if (args.length == 1) {
                checkoutBranch(args[0]);
                Main.saveState(Main.gitlet);
            }
            if (args.length == 2) {
                checkoutFile(args[1]);
                Main.saveState(Main.gitlet);
            }
            if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                checkoutCommit(args[0], args[2]);
                Main.saveState(Main.gitlet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length == 1 && !args[0].equals("--")
                || args.length == 2 && args[0].equals("--")
                || args.length == 3 && args[1].equals("--");
    }

    /** Helper function to check out branch.
     * @param branch a String.
     */
    public void checkoutBranch(String branch)
            throws IOException, ClassNotFoundException {
        if (!Main.gitlet.branches.containsKey(branch)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (Main.gitlet.headBranch.equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File temp = new File(Main.gitlet.workingDir
                , ".gitlet/"
                + Main.gitlet.branches.get(branch));
        Commit branchCommit = (Commit)
                Main.deserialize(Utils.readContents(temp));
        File temp2 = new File(Main.gitlet.workingDir
                , ".gitlet/" + Main.gitlet.head);
        Commit currBranch = (Commit)
                Main.deserialize(Utils.readContents(temp2));

        for (String a : Utils.plainFilenamesIn(Main
                .gitlet.workingDir)) {
            File file = new File(Main.gitlet.workingDir, a);
            String fileHash = a + sha1(Utils.readContents(file));
            if (branchCommit.getFileMap().containsKey(a)
                    && !branchCommit.getFileMap().get(a).equals(fileHash)
                    && !currBranch.getFileMap().containsKey(a)
                    && !a.equals(".gitignore") && !a.equals("proj2.iml")) {
                System.out.println("There is an untracked"
                        + " file in the way; delete it or add it first.");
                return;
            }
        }
        for (String a : currBranch.getFileMap().keySet()) {
            if (branchCommit.getFileMap().containsKey(a)) {
                String blobHash = branchCommit.getFileMap().get(a);
                File blobFile = new File(Main
                        .gitlet.workingDir + "/.gitlet", blobHash);
                File fileToReplace = new File(Main.gitlet.workingDir, a);
                Utils.writeContents(fileToReplace
                        , Utils.readContents(blobFile));
            }
            File fileToDelete = new File(Main.gitlet.workingDir, a);
            fileToDelete.delete();
        }
        for (String a : branchCommit.getFileMap().keySet()) {
            String blobHash = branchCommit.getFileMap().get(a);
            File blobFile = new File(Main
                    .gitlet.workingDir + "/.gitlet", blobHash);
            File fileToReplace = new File(Main.gitlet.workingDir, a);
            Utils.writeContents(fileToReplace
                    , Utils.readContents(blobFile));
        }

        Main.gitlet.head = Main.gitlet.branches.get(branch);
        Main.gitlet.headBranch = branch;

        Main.saveState(Main.gitlet);
    }
    /** Commit.
     * @param commit a String.
     * @param filename a String.
     */
    public static  void checkoutCommit(String commit, String filename)
            throws IOException, ClassNotFoundException {
        if (Main.gitlet.allCommitsIDs.contains(commit)
                || Main.gitlet.shortCommitsIDs.containsKey(commit)) {
            if (Main.gitlet.shortCommitsIDs.containsKey(commit)) {
                commit = Main.gitlet.shortCommitsIDs.get(commit);
            }
            File temp = new File(Main
                    .gitlet.workingDir, ".gitlet/" + commit);
            Commit target = (Commit) Main
                    .deserialize(Utils.readContents(temp));
            for (String file : target.getFileMap().keySet()) {
                if (file.equals(filename)) {
                    String blobHash = target.getFileMap().get(filename);
                    File blobFile = new File(Main
                            .gitlet.workingDir + "/.gitlet", blobHash);
                    File fileToReplace = new
                            File(Main.gitlet.workingDir, filename);
                    Utils.writeContents(fileToReplace
                            , Utils.readContents(blobFile));
                    return;
                }
            }
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            System.out.println("No commit with that id exists.");
            return;
        }
    }
    /** checkout File.
     * @param filename a String.
     */
    public void checkoutFile(String filename)
            throws IOException, ClassNotFoundException {
        File temp = new File(Main.gitlet.workingDir
                , ".gitlet/" + Main.gitlet.head);
        Commit target = (Commit) Main
                .deserialize(Utils.readContents(temp));
        if (!target.getFileMap().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobHash = target.getFileMap().get(filename);
        File blobFile = new File(Main
                .gitlet.workingDir + "/.gitlet", blobHash);

        File fileToReplace = new File(Main
                .gitlet.workingDir, filename);
        Utils.writeContents(fileToReplace
                ,  Utils.readContents(blobFile));
    }
}
