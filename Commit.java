package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** Class representing a commit object, containing its files, references
 * to its parent, metadata and a commit message.
 * @author Dylan Goetting
 */
public class Commit implements Serializable {

    /** commit message. **/
    private final String message;
    /** main parent. **/
    private final String parent;
    /** 2nd parent in the case of a merge. **/
    private String parent2 = null;
    /** commit timestamp. **/
    private final String time;

    /** Hashmap that maps file names to contents represented as a string.**/
    private HashMap<String, String> files = new HashMap<>();

    /** Constructor for the init commit. **/
    public Commit() {
        message = "initial commit";
        parent = null;
        time = "Wed Dec 31 16:00:00 1969 -0800";
    }

    /** Constructor for the general commits. M is the message, P is the
     * parent, T, F etc. **/
    public Commit(String m, String p, String t, HashMap<String, String> f) {
        message = m;
        parent = p;
        time = t;
        files.putAll(f);
    }

    /** sets the parent 2 to ID in the case of a merge. **/
    public void setParent2(String id) {
        parent2 = id;
    }

    /** returns a hash based on the time and message. **/
    public String uid() {
        if (parent == null) {
            return Utils.sha1(message, time);
        }
        return Utils.sha1(message, time, parent);
    }

    /** Getter method returning the files in this. **/
    public HashMap<String, String> getFiles() {
        return new HashMap<>(files);
    }

    /** Getter method.
     * @return PARENT.**/
    public String parent() {
        return this.parent;
    }
    /** Getter method.
     * @return PARENT2. **/
    public String parent2() {
        return this.parent2;
    }
    /** Getter method.
     * @return TIME. **/
    public String time() {
        return this.time;
    }
    /** Getter method.
     * @return MESSAGE. **/
    public String message() {
        return this.message;
    }
}
