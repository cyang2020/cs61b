package gitlet;

import java.io.IOException;

/** Abstract class for all commands.
 *  @author Casper Yang
 */


public interface Command {
    /** Run the command.
     * @param args arguments.
     */
    void run(String[] args) throws IOException, ClassNotFoundException;
    /** Conditions to meet.
     * @param args arguments.
     * @return a boolean.
     */
    boolean validOperands(String[] args);
}
