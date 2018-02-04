package gitlet;

import java.io.IOException;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import gitlet.commands.CheckoutCommand;
import gitlet.commands.AddCommand;
import gitlet.commands.BranchCommand;
import gitlet.commands.CommitCommand;
import gitlet.commands.FindCommand;
import gitlet.commands.GlobalLogCommand;
import gitlet.commands.InitCommand;
import gitlet.commands.LogCommand;
import gitlet.commands.MergeCommand;
import gitlet.commands.ResetCommand;
import gitlet.commands.RmCommand;
import gitlet.commands.RmBranchCommand;
import gitlet.commands.StatusCommand;
import java.nio.file.Paths;
import static gitlet.Utils.writeContents;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Casper Yang
 */
public class Main {
    /** A Gitlet object. */
    public static Gitlet gitlet = new Gitlet();
    /** Get Gitlet object.
     * @return a gitlet.*/
    public static Gitlet getGitlet() {
        return gitlet;
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args)
            throws IOException, ClassNotFoundException {
        Main.gitlet.workingDir = System.getProperty("user.dir");
        File prevState = new
                File(Main.gitlet.workingDir, ".gitlet/gitletState");
        if (prevState.exists()) {
            Gitlet prevGitlet =
                    (Gitlet) deserialize(Utils.readContents(prevState));
            Main.gitlet.branches = prevGitlet.branches;
            Main.gitlet.head = prevGitlet.head;
            Main.gitlet.headBranch = prevGitlet.headBranch;
            Main.gitlet.workingDir = prevGitlet.workingDir;
            Main.gitlet.stageAdd = prevGitlet.stageAdd;
            Main.gitlet.stageRemove = prevGitlet.stageRemove;
            Main.gitlet.allCommitsIDs = prevGitlet.allCommitsIDs;
            Main.gitlet.shortCommitsIDs = prevGitlet.shortCommitsIDs;
            Main.gitlet.branchToIntCom = prevGitlet.branchToIntCom;
            Main.gitlet.count = prevGitlet.count;
        }

        if (args == null || args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        try {
            Command command = allCommands(args);
            if (command == null) {
                return;
            }
            String[] operands = Arrays.copyOfRange(args, 1, args.length);
            if (!command.validOperands(operands)) {
                System.out.println("Incorrect operands.");
                return;
            }
            String git = Paths.get(System.getProperty("user.dir")).
                    resolve(".gitlet").toString();
            File gitletDir = new File(git);
            String a = args[0];

            if (!a.equals("init")  && !gitletDir.exists()) {
                System.out.println("Not in an initialized gitlet directory.");
                return;
            }
            command.run(operands);
            saveState(gitlet);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    /** Save all changes.
     * @param git a Gitlet object.
     */
    public static void saveState(Gitlet git) throws IOException {
        File newGitlet = new File(gitlet.workingDir
                , ".gitlet/gitletState");
        writeContents(newGitlet, serialize(git));

    }
    /** Serialize all changes.
     * @param obj an object object.
     * @return byte.
     */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    /** Deserialize all changes.
     * @param data an byte object.
     * @return object.
     */
    public static Object deserialize(byte[] data)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
    /** All commands.
     * @param args argument.
     * @return a command
     */
    private static Command allCommands(String[] args) {
        Command command;
        String cnmd = args[0];
        switch (cnmd) {
        case "init":
            command = new InitCommand();
            break;

        case "add":
            command = new AddCommand();
            break;

        case "commit":
            command = new CommitCommand();
            break;

        case "rm":
            command = new RmCommand();
            break;

        case "log":
            command = new LogCommand();
            break;

        case "global-log":
            command = new GlobalLogCommand();
            break;

        case "find":
            command = new FindCommand();
            break;

        case "status":
            command = new StatusCommand();
            break;

        case "checkout":
            command = new CheckoutCommand();
            break;

        case "branch":
            command = new BranchCommand();
            break;
        case "rm-branch":
            command = new RmBranchCommand();
            break;

        case "reset":
            command = new ResetCommand();
            break;
        case "merge":
            command = new MergeCommand();
            break;
        default:
            System.out.println("No command with that name exists.");
            return null;
        }

        return command;
    }


}
