package gitlet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;



/** Main class representing a repository with lots of data.
 @author Dylan Goetting **/
@SuppressWarnings("unchecked")
public class Repo implements Serializable {
    /** Initializes the repo, called when the user inputs init
     * Initializes the data structures, and creates an initial commit to
     * add to the commit area.**/

    public Repo() {
        path = Utils.join(cwd, ".gitlet");
        if (path.exists()) {
            System.out.println("Gitlet version-control system already exists "
                    + "in the current directory.");
            return;
        }
        addStagingArea = new HashMap<>();
        rmStagingArea = new HashMap<>();
        commitArea = new HashMap<>();
        branches = new HashMap<>();

        Commit initial = new Commit();
        branches.put("master", initial);
        currentBranch = "master";

        String uid = initial.uid();
        commitArea.put(uid, initial);
        path.mkdir();

        Utils.writeObject(Utils.join(cwd, ".gitlet/dummyRepo"), this);
        Utils.writeObject(Utils.join(cwd, ".gitlet/commits"), commitArea);
        Utils.writeObject(Utils.join(cwd, ".gitlet/addStaging"),
                addStagingArea);
        Utils.writeObject(Utils.join(cwd, ".gitlet/rmStaging"), rmStagingArea);
        Utils.writeObject(Utils.join(cwd, ".gitlet/branches"), branches);
        Utils.writeObject(Utils.join(cwd, ".gitlet/currentBranch"),
                currentBranch);
    }

    /** Add all the files in the staging area into a new commit, add this
     * commit to the commit tree/hashmap, advance the head and master
     * pointers and clear the staging area MESSAGE.
     */
    public void commit(String message) {
        commitArea = Utils.readObject(Utils.join(cwd, ".gitlet/commits"),
                HashMap.class);
        addStagingArea = Utils.readObject(Utils.join(cwd,
                ".gitlet/addStaging"), HashMap.class);
        rmStagingArea = Utils.readObject(Utils.join(cwd,
                ".gitlet/rmStaging"), HashMap.class);
        branches = Utils.readObject(Utils.join(cwd, ".gitlet/branches"),
                HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                ".gitlet/currentBranch"), String.class);
        Commit head = branches.get(currentBranch);

        if (message .equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }

        if (addStagingArea.isEmpty() && rmStagingArea.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        Date date = new Date();
        String time = dateFormat.format(date);

        String parent = head.uid();

        HashMap<String, String> files = head.getFiles();
        files.putAll(addStagingArea);

        for (String k : rmStagingArea.keySet()) {
            files.remove(k);
        }

        Commit n = new Commit(message, parent, time + " -0800", files);
        String nuid = n.uid();
        commitArea.put(nuid, n);
        branches.put(currentBranch, n);
        addStagingArea.clear();
        rmStagingArea.clear();

        Utils.writeObject(Utils.join(cwd, ".gitlet/rmStaging"),
                rmStagingArea);
        Utils.writeObject(Utils.join(cwd, ".gitlet/addStaging"),
                addStagingArea);
        Utils.writeObject(Utils.join(cwd, ".gitlet/branches"), branches);
        Utils.writeObject(Utils.join(cwd, ".gitlet/commits"), commitArea);
    }

    /** Add FILENAME name to the staging area and saves staging area.
     */
    public void add(String fileName) {
        branches = Utils.readObject(Utils.join(cwd, ".gitlet/branches"),
                HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                ".gitlet/currentBranch"), String.class);
        Commit head = branches.get(currentBranch);
        addStagingArea = Utils.readObject(Utils.join(cwd,
                ".gitlet/addStaging"), HashMap.class);
        rmStagingArea = Utils.readObject(Utils.join(cwd, ".gitlet/rmStaging"),
                HashMap.class);
        if (rmStagingArea.keySet().contains(fileName)) {
            rmStagingArea.remove(fileName);
            Utils.writeObject(Utils.join(cwd, ".gitlet/rmStaging"),
                    rmStagingArea);
            return;
        }

        File blob = Utils.join(cwd, fileName);
        if (!blob.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String temp = Utils.readContentsAsString(blob);
        String uid2 = Utils.sha1(temp);

        if (head.getFiles().containsKey(fileName)
                && Utils.sha1(head.getFiles().get(fileName)).equals(uid2)) {
            addStagingArea.remove(fileName);
        } else {
            addStagingArea.put(fileName, temp);
        }

        Utils.writeObject(Utils.join(cwd, ".gitlet/addStaging"),
                addStagingArea);
        Utils.writeObject(Utils.join(cwd, ".gitlet/rmStaging"), rmStagingArea);
    }

    /**
     * Starts with the head and prints out details of each commit until
     * the initial commit.
     */
    public void log() {
        commitArea = Utils.readObject(Utils.join(cwd, ".gitlet/commits"),
                HashMap.class);
        branches = Utils.readObject(Utils.join(cwd, ".gitlet/branches"),
                HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                        ".gitlet/currentBranch"), String.class);

        Commit start = branches.get(currentBranch);
        while (start != null) {
            if (start.parent2() == null) {
                System.out.println(
                        "=== \ncommit " + start.uid() + "\nDate: "
                                + start.time() + "\n" + start.message() + "\n"
                );
                start = commitArea.get(start.parent());
            } else {
                System.out.println(
                        "=== \ncommit " + start.uid() + "\nMerge: "
                                + start.parent().substring(0, 7) + " "
                                + start.parent2().substring(0, 7)
                                + "\nDate: " + start.time()
                                + "\n" + start.message() + "\n"
                );
                start = commitArea.get(start.parent());
            }
        }
    }

    /** Shows the history of all commits ever made, not just the ones
     * from the current head. **/
    public void glog() {
        commitArea = Utils.readObject(Utils.join(cwd, ".gitlet/commits"),
                HashMap.class);
        for (Commit c: commitArea.values()) {
            if (c.parent2() == null) {
                System.out.println(
                        "=== \ncommit " + c.uid() + "\nDate: "
                                + c.time() + "\n" + c.message() + "\n"
                );
            } else {
                System.out.println(
                        "=== \ncommit " + c.uid() + "\nMerge: "
                                + c.parent().substring(0, 7) + " "
                                + c.parent2().substring(0, 7)
                                + "\nDate: " + c.time() + "\n"
                                + c.message() + "\n"
                );
            }
        }
    }

    /** If the entered file is currently in the staging area, we remove it.
     * If the file is being tracked by the head commit, then we add it to the
     * staging area and delete the file from the directory FILE.
     */
    public void rm(String file) {
        addStagingArea = Utils.readObject(Utils.join(cwd,
                        ".gitlet/addStaging"), HashMap.class);
        rmStagingArea = Utils.readObject(Utils.join(cwd, ".gitlet/rmStaging"),
                HashMap.class);
        branches = Utils.readObject(Utils.join(cwd,
                ".gitlet/branches"), HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                        ".gitlet/currentBranch"), String.class);
        Commit head = branches.get(currentBranch);

        if (!addStagingArea.containsKey(file)
                && !head.getFiles().containsKey(file)) {
            System.out.println("No reason to remove the file");
            return;
        }
        addStagingArea.remove(file);
        if (head.getFiles().containsKey(file)) {
            rmStagingArea.put(file, "default");
            File blob = new File(file);
            if (blob.exists()) {
                Utils.restrictedDelete(blob);
            }
        }
        Utils.writeObject(Utils.join(cwd, ".gitlet/rmStaging"), rmStagingArea);
        Utils.writeObject(Utils.join(cwd, ".gitlet/addStaging"),
                addStagingArea);
    }

    /** Does a few different things depending on ARGS. **/
    public void checkout(String[] args) {
        commitArea = Utils.readObject(Utils.join(cwd, ".gitlet/commits"),
                HashMap.class);
        branches = Utils.readObject(Utils.join(cwd, ".gitlet/branches"),
                HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                        ".gitlet/currentBranch"), String.class);
        Commit head = branches.get(currentBranch);
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            checkoutHelper(head, args[2]);
        }
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            String id = args[1];
            String fileName = args[3];
            Commit c = idHelper(id);
            if (c == null) {
                System.out.println("No commit with that id exists.");
            } else {
                checkoutHelper(c, fileName);
            }
        }
        if (args.length == 2) {
            if (args[1].equals(currentBranch)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            if (!branches.containsKey(args[1])) {
                System.out.println("No such branch exists");
                return;
            }
            Commit c = branches.get(args[1]);
            for (String fileName : c.getFiles().keySet()) {
                File f =  Utils.join(cwd, fileName);
                if (!head.getFiles().containsKey(fileName) && f.exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }
            for (String fileName : c.getFiles().keySet()) {
                checkoutHelper(c, fileName);
            }
            for (String fileName : head.getFiles().keySet()) {
                if (!c.getFiles().containsKey(fileName)) {
                    Utils.restrictedDelete(fileName);
                }
            }
            currentBranch = args[1];
            Utils.writeObject(Utils.join(cwd, ".gitlet/currentBranch"),
                    currentBranch);
        }
    }

    /** Helper method that checks out a specific file from a
     * specific commit. C FILENAME. **/
    public void checkoutHelper(Commit c, String fileName) {
        HashMap<String, String> files = c.getFiles();
        String blob = files.get(fileName);
        if (blob == null) {
            System.out.println("File does not exist in that commit");
            return;
        }
        File f = new File(fileName);
        Utils.writeContents(f, blob);
    }

    /** Helper method that searches through the commits and finds any commits
     * that might be abbreviated by the given input ID.
     * @return the commit with this id. */
    public Commit idHelper(String id) {
        int len = id.length();
        if (commitArea.get(id) != null || len < 6) {
            return commitArea.get(id);
        }
        for (String i:commitArea.keySet()) {
            if (id.equals(i.substring(0, len))) {
                return commitArea.get(i);
            }
        }
        return null;
    }

    /**
     * Checks out all the files tracked by the given commit. Removes tracked
     * files that are not present in that commit. Also moves the current
     * branch's head to that commit node. See the intro for an example of
     * what happens to the head pointer after using reset. The [commit id]
     * may be abbreviated as for checkout. The staging area is cleared. The
     * command is essentially checkout of an arbitrary commit that also
     * changes the current branch head UID.
     */
    public void reset(String uid) {
        branches = Utils.readObject(Utils.join(cwd, ".gitlet/branches"),
                HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                        ".gitlet/currentBranch"), String.class);
        commitArea = Utils.readObject(Utils.join(cwd, ".gitlet/commits"),
                HashMap.class);

        Commit head = branches.get(currentBranch);
        Commit c = idHelper(uid);

        if (c == null) {
            System.out.println("No commit with that id exists");
            return;
        }

        for (String fileName : c.getFiles().keySet()) {
            File f = new File(fileName);
            if (!head.getFiles().containsKey(fileName) && f.exists()) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        for (String fileName : c.getFiles().keySet()) {
            checkoutHelper(c, fileName);
        }

        for (String fileName : head.getFiles().keySet()) {
            if (!c.getFiles().containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        addStagingArea.clear();
        rmStagingArea.clear();
        branches.put(currentBranch, c);
        Utils.writeObject(Utils.join(cwd, ".gitlet/branches"), branches);
        Utils.writeObject(Utils.join(cwd, ".gitlet/addStaging"),
                addStagingArea);
        Utils.writeObject(Utils.join(cwd, ".gitlet/rmStaging"), rmStagingArea);
    }

    /** Searches through the commit tree to and prints out the UID of any
     * commits with the given MESSAGE.
     */
    public void find(String message) {
        commitArea = Utils.readObject(Utils.join(cwd, ".gitlet/commits"),
                HashMap.class);

        boolean found = false;
        for (Commit c: commitArea.values()) {
            if (c.message().equals(message)) {
                found = true;
                System.out.println(c.uid());
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Creates a new branch pointer, and points it at the current head.
     * @param name the name of the new branch. */
    public void branch(String name) {
        branches = Utils.readObject(Utils.join(cwd, ".gitlet/branches"),
                HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                        ".gitlet/currentBranch"), String.class);
        Commit head = branches.get(currentBranch);

        if (branches.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        branches.put(name, head);

        Utils.writeObject(Utils.join(cwd, ".gitlet/branches"), branches);
    }

    /** Removes the branch pointer of the specified branch NAME.*/
    public void rmbranch(String name) {
        branches = Utils.readObject(Utils.join(cwd, ".gitlet/branches"),
                HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                        ".gitlet/currentBranch"), String.class);

        if (!branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if (name.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        branches.remove(name);
        Utils.writeObject(Utils.join(cwd, ".gitlet/branches"), branches);
    }

    /**
     * Shows what branches exist in this repo, and what the current addition
     * and removal staging area looks like.
     */
    public void status() {
        branches = Utils.readObject(Utils.join(cwd, ".gitlet/branches"),
                HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                        ".gitlet/currentBranch"), String.class);
        addStagingArea = Utils.readObject(Utils.join(cwd, ".gitlet/addStaging"),
                HashMap.class);
        rmStagingArea = Utils.readObject(Utils.join(cwd, ".gitlet/rmStaging"),
                HashMap.class);

        String s = "=== Branches === \n";

        List<String> sorted1 = new ArrayList<>(branches.keySet());
        Collections.sort(sorted1);
        for (String b : sorted1) {
            if (currentBranch.equals(b)) {
                s = s + "*" + b + "\n";
            } else {
                s = s + b + "\n";
            }
        }

        List<String> sorted2 = new ArrayList<>(addStagingArea.keySet());
        Collections.sort(sorted2);
        s += "\n=== Staged Files === \n";
        for (String f: sorted2) {
            s += f + "\n";
        }

        List<String> sorted3 = new ArrayList<>(rmStagingArea.keySet());
        Collections.sort(sorted3);
        s += "\n=== Removed Files === \n";
        for (String f : sorted3) {
            s += f + "\n";
        }

        s += "\n=== Modifications Not Staged For Commit === \n";
        s += "\n=== Untracked Files === \n";

        System.out.println(s);
    }

    /** check BRANCH.
     * @return if problem. **/
    public boolean check(String branch) {
        if (!addStagingArea.isEmpty() || !rmStagingArea.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (!branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist");
            return true;
        }
        if (currentBranch.equals(branch)) {
            System.out.println("Cannot merge a branch with itself");
            return true;
        }
        return false;
    }

    /** merge stuff into BRANCH. **/
    public void merge(String branch) {
        commitArea = Utils.readObject(Utils.join(cwd, ".gitlet/commits"),
                HashMap.class);
        addStagingArea = Utils.readObject(Utils.join(cwd,
                ".gitlet/addStaging"), HashMap.class);
        branches = Utils.readObject(Utils.join(cwd, ".gitlet/branches"),
                HashMap.class);
        currentBranch = Utils.readObject(Utils.join(cwd,
                ".gitlet/currentBranch"), String.class);
        rmStagingArea = Utils.readObject(Utils.join(cwd, ".gitlet/rmStaging"),
                HashMap.class);
        Commit head = branches.get(currentBranch);
        if (check(branch)) {
            return;
        }
        Commit split = split(head, branches.get(branch));
        HashMap<String, String> headFiles = head.getFiles();
        HashMap<String, String> branchFiles = branches.get(branch).getFiles();
        HashMap<String, String> splitFiles = split.getFiles();
        hasConflict = false;
        String branchID = branches.get(branch).uid();
        for (String file : branchFiles.keySet()) {
            File f = new File(file);
            if (!head.getFiles().containsKey(file) && f.exists()) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return;
            }
        }
        if (branches.get(branch).uid().equals(split.uid())) {
            System.out.println("Given branch is an ancestor of "
                    + "the current branch.");
            return;
        }
        if (branches.get(currentBranch).uid().equals(split.uid())) {
            System.out.println("Current branch fast-forwarded.");
            checkout(new String[] {"checkout", branch});
            return;
        }
        Set<String> allFiles = new HashSet<>(headFiles.keySet());
        allFiles.addAll(branchFiles.keySet());
        for (String id : allFiles) {
            mergeHelper(id, branchFiles, headFiles, splitFiles, branchID);
        }
        Utils.writeObject(Utils.join(cwd, ".gitlet/rmStaging"),
                rmStagingArea);
        Utils.writeObject(Utils.join(cwd, ".gitlet/addStaging"),
                addStagingArea);
        commit("Merged " + branch + " into " + currentBranch + ".");
        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        Utils.readObject(Utils.join(cwd, ".gitlet/branches"), HashMap.class);
        Commit merge = branches.get(currentBranch);
        merge.setParent2(branchID);
        Utils.writeObject(Utils.join(cwd, ".gitlet/branches"),
                branches);
        Utils.writeObject(Utils.join(cwd, ".gitlet/commits"),
                commitArea);
    }

    /** merge helper ID BRANCHFILES HEADFILES SPLITFILES BRANCHID. **/
    public void mergeHelper(String id, HashMap<String, String> branchFiles,
                            HashMap<String, String> headFiles, HashMap<String,
            String> splitFiles, String branchID) {
        if (modified(branchFiles.get(id), splitFiles.get(id))
                && notModified(headFiles.get(id), splitFiles.get(id))) {
            checkout(new String[]{"checkout", branchID, "--", id});
            addStagingArea.put(id, branchFiles.get(id));
        }
        if (branchFiles.containsKey(id) && !splitFiles.containsKey(id)
                && !headFiles.containsKey(id)) {
            checkout(new String[]{"checkout", branchID, "--", id});
            addStagingArea.put(id, branchFiles.get(id));
        }
        if (splitFiles.containsKey(id) && !branchFiles.containsKey(id)
                && notModified(headFiles.get(id), splitFiles.get(id))) {
            Utils.restrictedDelete(id);
            rmStagingArea.put(id, "default");
            Utils.restrictedDelete(Utils.join(cwd, id));
        }
        boolean conflict = (modified(branchFiles.get(id), splitFiles.get(id))
                && modified(headFiles.get(id), splitFiles.get(id))
                && modified(branchFiles.get(id), headFiles.get(id)))
                || ((modified(branchFiles.get(id), splitFiles.get(id))
                && !headFiles.containsKey(id))
                || (modified(headFiles.get(id),
                splitFiles.get(id)) && !branchFiles.containsKey(id)))
                || (!splitFiles.containsKey(id)
                && modified(branchFiles.get(id), headFiles.get(id)));
        if (conflict) {
            hasConflict = true;
            String h = "", b = "";
            if (headFiles.get(id) != null) {
                h = headFiles.get(id);
            }
            if (branchFiles.get(id) != null) {
                b = branchFiles.get(id);
            }
            String conflictFile = "<<<<<<< HEAD\n";
            conflictFile += h;
            conflictFile += "=======\n";
            conflictFile += b;
            conflictFile += ">>>>>>>\n";
            Utils.writeContents(Utils.join(cwd, id), conflictFile);
            addStagingArea.put(id, conflictFile);
        }
    }
    /** return the split point of the given B with H. **/
    public Commit split(Commit h, Commit b) {
        marks.clear();
        visit(b);
        return findLCA(h);
    }

    /** visit a node in a graph, then visit its parents. Starting at C. **/
    public void visit(Commit c) {
        if (c == null || marks.contains(c.uid())) {
            return;
        }
        marks.add(c.uid());
        visit(commitArea.get(c.parent()));
        visit(commitArea.get(c.parent2()));
    }

    /** traverses back from the commit C, and returns the first commit
     * that is contained in marks. */
    public Commit findLCA(Commit c) {
        LinkedList<Commit> s = new LinkedList<>();
        s.add(c);

        while (!s.isEmpty()) {
            Commit pop = s.pop();
            if (marks.contains(pop.uid())) {
                return pop;
            }
            s.add(commitArea.get(pop.parent()));
            if (pop.parent2() != null) {
                s.add(commitArea.get(pop.parent2()));
            }
        }
        System.out.println("Error, didn't find a commit we marked");
        return new Commit();
    }

    /** returns whether the F1 and F2 exist AND are different. **/
    public Boolean modified(String f1, String f2) {
        if (f1 == null || f2 == null) {
            return false;
        }
        return !Utils.sha1(f1).equals(Utils.sha1(f2));
    }

    /** returns whether the F1 and F2 exist AND are the same. **/
    public Boolean notModified(String f1, String f2) {
        if (f1 == null || f2 == null) {
            return false;
        }
        return Utils.sha1(f1).equals(Utils.sha1(f2));
    }

    /** Hashmap that maps the name of the file to the contents of the file
     * represented as a string. */
    private HashMap<String, String> addStagingArea;

    /** Hashmap that maps the name of the file to the contents of the file
     * represented as a string. */
    private HashMap<String, String> rmStagingArea;

    /** Hashmap that maps the UID hash of each commit to each commit.**/
    private HashMap<String, Commit> commitArea;

    /** hashmap that maps the name of each branch to the head commit of each
     * branch. **/
    private HashMap<String, Commit> branches;

    /** name of the current (head) branch. **/
    private String currentBranch;

    /** File pointing to the .gitlet directory. **/
    private File path;

    /** file for the current directory. **/
    private File cwd = new File(System.getProperty("user.dir"));

    /** used solely for finding the split point every time. **/
    private HashSet<String> marks = new HashSet<>();

    /** for merge only. **/
    private boolean hasConflict;
}
