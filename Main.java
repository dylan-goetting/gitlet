package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Dylan Goetting
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("init")) {
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
            } else {
                new Repo();
            }
        } else {
            File repoPath = new File(".gitlet/dummyRepo");
            try {
                Repo r = Utils.readObject(repoPath, Repo.class);
                switch (args[0]) {
                case "add":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                    } else {
                        r.add(args[1]);
                    }
                    break;
                case "commit":
                    if (args.length > 2) {
                        System.out.println("Incorrect operands.");
                    } else if (args.length == 1) {
                        System.out.println("Please enter a commit message");
                    } else {
                        r.commit(args[1]);
                    }
                    break;
                case "rm":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                    } else {
                        r.rm(args[1]);
                    }
                    break;
                case "log":
                    if (args.length > 1) {
                        System.out.println("Incorrect operands.");
                    } else {
                        r.log();
                    }
                    break;
                case "global-log":
                    if (args.length > 1) {
                        System.out.println("Incorrect operands.");
                    } else {
                        r.glog();
                    }
                    break;
                default:
                    helper(args, r);
                }
            } catch (IllegalArgumentException i) {
                System.out.println("Not in an initialized Gitlet directory.");
            }
        }
    }
    /** helper ARGS R. **/
    public static void helper(String[] args, Repo r) {
        switch (args[0]) {
        case "find":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                r.find(args[1]);
            }
            break;
        case "status":
            if (args.length > 1) {
                System.out.println("Incorrect operands.");
            } else {
                r.status();
            }
            break;
        case "checkout":
            if (args.length < 2 || args.length > 4) {
                System.out.println("Incorrect operands.");
            } else {
                r.checkout(args);
            }
            break;
        case "branch":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                r.branch(args[1]);
            }
            break;
        case "rm-branch":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                r.rmbranch(args[1]);
            }
            break;
        case "reset":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                r.reset(args[1]);
            }
            break;
        case "merge":
            if (args.length != 2) {
                System.out.println("Incorrect operands.");
            } else {
                r.merge(args[1]);
            }
            break;
        default:
            System.out.println("No command with that name exists.");
            break;
        }
    }
}
