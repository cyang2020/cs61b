
package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Utils.sha1;

/** Class for all commits.
 *  @author Casper Yang
 */
public class Commit implements Serializable {
    /** The name. */
    private String hashName;
    /** The parentHash. */
    private String parentHash;
    /** The date. */
    private String date;
    /** The logMessage. */
    private String logMessage;
    /** The fileMap. */
    private HashMap<String, String> fileMap;

    /** Constructor for Commit.
     * @param parentHas a String.
     * @param log a String.
     * @param fileMa a HashMap.
      */
    public Commit(String parentHas, String log, HashMap<String
            , String> fileMa) {
        this.parentHash = parentHas;
        this.logMessage = log;
        SimpleDateFormat mydate
                = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        Date now = new Date();
        this.date = mydate.format(now);
        this.fileMap = fileMa;
        String hash = "";
        if (parentHash != null) {
            hash += parentHash + date + log;
        } else {
            hash += date + log;
        }
        this.hashName = sha1(hash);
    }
    /** getHashName.
     * @return hashName.*/
    public String getHashName() {
        return hashName;
    }
    /** getParentHash.
     * @return parentHash.*/
    public String getParentHash() {
        return this.parentHash;
    }
    /** setParentHash.
     * @param parentHas a String.
     */
    public void setParentHash(String parentHas) {
        this.parentHash = parentHas;
    }

    /** getDate.
     * @return date.*/
    public String getDate() {
        return date;
    }
    /** getLog.
     * @return logMessage.*/
    public String getLog() {
        return logMessage;
    }
    /** getFileMap.
     * @return fileMap.*/
    public HashMap<String, String> getFileMap() {
        return fileMap;
    }
}
