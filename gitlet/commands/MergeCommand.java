package gitlet.commands;
import gitlet.Command;
import gitlet.Main;
import java.io.File;
import java.io.IOException;
import gitlet.Commit;
import gitlet.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static gitlet.Utils.writeContents;

/** The merge command.
 * @author Casper Yang
 */
public class MergeCommand implements Command {
    @Override
    public void run(String[] args) throws IOException
            , ClassNotFoundException {
        if (mergeChecks(args[0])) {
            return;
        }
        boolean conflict = false;
        File temp = new File(Main.gitlet.workingDir
                , ".gitlet/" + Main.gitlet.head);
        String branchPointer = Main.gitlet.head;
        String splitPoint = Main.gitlet.head;
        String newBranch = Main.gitlet.branches.get(args[0]);
        splitPoint = findingSplit(splitPoint, branchPointer, newBranch);
        if (splitPoint.equals(newBranch)) {
            System.out.println(
                    "Given branch is an ancestor of the current branch.");
            return;
        } else if (splitPoint.equals(Main.gitlet.head)) {
            Main.gitlet.head = newBranch;
            Main.gitlet.headBranch = args[0];
            System.out.println("Current branch fast-forwarded.");
            Main.saveState(Main.gitlet);
            return;
        } else {
            File stemp = new File(Main.gitlet.workingDir
                    , ".gitlet/" + Main.gitlet.head);
            Commit currBranch = (Commit) Main
                    .deserialize(Utils.readContents(stemp));
            stemp = new File(Main.gitlet.workingDir
                    , ".gitlet/" + Main.gitlet
                    .branches.get(args[0]));
            Commit givenBranch = (Commit) Main
                    .deserialize(Utils.readContents(stemp));
            stemp = new File(Main.gitlet.workingDir
                    , ".gitlet/" + splitPoint);
            Commit splitBranch = (Commit) Main
                    .deserialize(Utils.readContents(stemp));
            List<String> currentMod = new ArrayList<String>();
            List<String> otherMod = new ArrayList<String>();
            HashMap<String, String> currentFiles
                    = currBranch.getFileMap();
            HashMap<String, String> otherFiles
                    = givenBranch.getFileMap();
            HashMap<String, String> splitFiles
                    = splitBranch.getFileMap();
            for (String file : currentFiles.keySet()) {
                String fileHash = currentFiles.get(file);
                String splitHash = splitFiles.get(file);
                if (!fileHash.equals(splitHash)
                        && splitFiles.containsKey(file)) {
                    currentMod.add(file);
                }
            }
            for (String file : otherFiles.keySet()) {
                String fileHash = otherFiles.get(file);
                String splitHash = splitFiles.get(file);
                if (!fileHash.equals(splitHash)
                        && splitFiles.containsKey(file)) {
                    otherMod.add(file);
                }
            }
            for (String filename : otherMod) {
                if (currBranch.getFileMap().containsKey(filename)
                        && !currentMod.contains(filename)) {
                    CheckoutCommand.checkoutCommit(givenBranch.getHashName()
                            , filename);
                }
            }
            for (String filename : currentMod) {
                if (givenBranch.getFileMap().containsKey(filename)
                        && !otherMod.contains(filename)) {
                    CheckoutCommand.checkoutCommit(Main.gitlet.head, filename);
                }
            }
            for (String filename : currentFiles.keySet()) {
                if (!otherFiles.containsKey(filename)
                        && !splitFiles.containsKey(filename)) {
                    CheckoutCommand.checkoutCommit(Main.gitlet.head, filename);
                }
            }
            for (String filename : otherFiles.keySet()) {
                if (!currentFiles.containsKey(filename)
                        && !splitFiles.containsKey(filename)) {
                    CheckoutCommand.checkoutCommit(givenBranch
                            .getHashName(), filename);
                    Main.gitlet.stageAdd.put(filename
                            , otherFiles.get(filename));
                }
            }
            for (String filename : splitFiles.keySet()) {
                if (!otherFiles.containsKey(filename)
                        && currentFiles.containsKey(filename)
                        && !currentMod.contains(filename)) {
                    File temp2 = new File(Main.gitlet
                            .workingDir, filename);
                    if (temp2.exists()) {
                        rm(filename);
                    }
                }
            }
            for (String filename : splitFiles.keySet()) {
                if (!currentFiles.containsKey(filename)
                        && otherFiles.containsKey(filename)
                        && !otherMod.contains(filename)) {
                    File temp2 = new File(Main.gitlet
                            .workingDir, filename);
                    if (temp2.exists()) {
                        rm(filename);
                    }
                }
            }
            conflict = Utils.writeToFiles(conflict
                    , otherMod, currentMod, otherFiles,
                    currentFiles, givenBranch
                    , currBranch, splitBranch);

            commit("Merged "
                    + args[0]
                    + " into " + Main.gitlet.headBranch + ".");

            if (conflict) {
                System.out.println("Encountered a merge conflict.");
                return;
            }
            Main.saveState(Main.gitlet);
        }
    }
    @Override
    public boolean validOperands(String[] args) {
        return args.length >= 1;
    }

    /** Helper function for merge.
     * @param branchName a String.
     * @return a boolean.
     */
    public boolean mergeChecks(String branchName)
            throws IOException, ClassNotFoundException {
        File skoot = new File(Main.gitlet.workingDir
                , ".gitlet/" + Main.gitlet.head);
        Commit currenttBranch = (Commit) Main
                .deserialize(Utils.readContents(skoot));
        if (!Main.gitlet.stageAdd.isEmpty()
                || !Main.gitlet.stageRemove.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (!Main.gitlet.branches.containsKey(branchName)) {
            System.out
                    .println("A branch with that name does not exist.");
            return true;
        }
        if (Main.gitlet.headBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        for (String a : Utils.plainFilenamesIn(Main.gitlet.workingDir)) {
            if (!currenttBranch.getFileMap()
                    .containsKey(a) && !a.equals(".gitignore")
                    && !a.equals("proj2.iml")
                    || !Main.gitlet.stageAdd.isEmpty()) {
                System.out
                        .println("There is an untracked file in the way; "
                                + "delete it or add it first.");
                return true;
            }
        }
        return false;
    }

    /** Helper function to find split.
     * @param splitPoint a String.
     * @param branchPointer a String.
     * @param newBranch a String.
     * @return a String.
     */
    public String findingSplit(String splitPoint
            , String branchPointer, String newBranch) throws
            IOException, ClassNotFoundException {
        ArrayList<String> currentBranchHistory = new ArrayList<>();
        ArrayList<String> givenBranchHistory = new ArrayList<>();
        File temp3 = new File(Main.gitlet
                .workingDir, ".gitlet/" + Main.gitlet.head);
        while (branchPointer != null) {
            temp3 = new File(Main.gitlet.workingDir
                    , ".gitlet/" + branchPointer);
            Commit commit = (Commit) Main
                    .deserialize(Utils.readContents(temp3));
            currentBranchHistory.add(commit.getHashName());
            branchPointer = commit.getParentHash();
        }
        branchPointer = newBranch;
        while (branchPointer != null) {
            temp3 = new File(Main.gitlet.workingDir
                    , ".gitlet/" + branchPointer);
            Commit commit = (Commit) Main
                    .deserialize(Utils.readContents(temp3));
            givenBranchHistory.add(commit.getHashName());
            branchPointer = commit.getParentHash();
        }
        boolean found = false;
        for (int i = 0; i < currentBranchHistory.size()
                && !found; i++) {
            for (int j = 0; j < givenBranchHistory.size()
                    && !found; j++) {
                if (currentBranchHistory.get(i)
                        .equals(givenBranchHistory.get(j))) {
                    splitPoint = currentBranchHistory.get(i);
                    found = true;
                }
            }
        }
        return splitPoint;
    }

    /** Helper function to commit.
     * @param args a String.
     */
    public void commit(String args)
            throws IOException, ClassNotFoundException {
        if (Main.gitlet.stageAdd.isEmpty()
                && Main.gitlet.stageRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        String message = args;

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

        Commit c = new Commit(Main.gitlet.head, args, blobMap);

        File newCommit = new File(Main.gitlet.workingDir
                , ".gitlet/" + c.getHashName());
        writeContents(newCommit, Main.serialize(c));

        Main.gitlet.allCommitsIDs.add(c.getHashName());
        String shortID = c.getHashName().substring(0, 8);
        Main.gitlet.shortCommitsIDs.put(shortID
                , c.getHashName());

        Main.gitlet.head = c.getHashName();

        Main.gitlet.branches.put(Main.gitlet.headBranch
                , Main.gitlet.head);

        Main.saveState(Main.gitlet);

    }
    /** Helper function to rm.
     * @param args a String.
     */
    public void rm(String args)
            throws ClassNotFoundException, IOException {
        if (Main.gitlet.stageRemove.contains(args)) {
            System.out.println("Already in remove list");
            return;
        }

        if (Main.gitlet.stageAdd.containsKey(args)) {
            Main.gitlet.stageAdd.remove(args);
            Main.saveState(Main.gitlet);
            return;
        }
        File temp = new File(Main.gitlet.workingDir
                , ".gitlet/" + Main.gitlet.head);
        Commit target = (Commit) Main
                .deserialize(Utils.readContents(temp));

        File fileToDelete = new File(Main
                .gitlet.workingDir, args);

        if (target.getFileMap().containsKey(args)) {
            Main.gitlet.stageRemove.add(args);

            if (fileToDelete.exists()) {

                fileToDelete.delete();
            }
            if (Main.gitlet.stageAdd.containsKey(args)) {
                Main.gitlet.stageAdd.remove(args);
            }
            Main.saveState(Main.gitlet);
            return;
        } else if (Main.gitlet.stageAdd.containsKey(args)) {
            Main.gitlet.stageAdd.remove(args);
            Main.saveState(Main.gitlet);
            return;
        }
        System.out.println("No reason to remove the file.");
        return;
    }
}
