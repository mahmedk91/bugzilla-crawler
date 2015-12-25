package saahil.hiwi.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import saahil.hiwi.crawler.ShowBugParser;
import saahil.hiwi.entities.Bug;

public class Launcher {
  public static int totalPendingBugs;
  public static int bugsProcessed;
  public static DB db = new DB();

  public static void main(String[] args) throws SQLException, IOException {
    System.setProperty("javax.net.ssl.trustStore", Config.SSL_TRUSTSTORE);
    ResultSet bugs = null, totalBugsCount = null, pendingBugsCount = null;
    String dataFolder;
    String baseURL = null;
    if (args.length % 2 != 0) {
      System.out.println("Wrong number of arguments." + "\nProgram needs atleast baseURL to run."
          + "\nCorrect usage is java saahil.hiwi.crawler.Launcher \"baseURL\" \"csvFolder\""
          + "\nFor sqlite there can be only baseURL");
      System.exit(0);
    } else if (args.length == 2) {
      if (args[0].equals("-f") && !Config.DB_TYPE.equals("sqlite")) {
        dataFolder = args[1];
        uploadBugs(dataFolder);
        bugs = db.getPendingBugs();
        totalBugsCount = db.getTotalBugsCount();
        pendingBugsCount = db.getPendingBugsCount();
      } else if (args[0].equals("-url")) {
        baseURL = args[1];
        bugs = db.getPendingBugs(baseURL);
        totalBugsCount = db.getTotalBugsCount(baseURL);
        pendingBugsCount = db.getPendingBugsCount(baseURL);
      } else {
        System.out.println("Wrong arguments");
        System.exit(0);
      }
    } else if (args.length == 4 && !Config.DB_TYPE.equals("sqlite")) {
      if (args[0].equals("-f") && args[2].equals("-url")) {
        dataFolder = args[1];
        baseURL = args[3];
        uploadBugs(dataFolder);
        bugs = db.getPendingBugs(baseURL);
        totalBugsCount = db.getTotalBugsCount(baseURL);
        pendingBugsCount = db.getPendingBugsCount(baseURL);
      } else if (args[0].equals("-url") && args[2].equals("-f")) {
        baseURL = args[1];
        dataFolder = args[3];
        uploadBugs(dataFolder);
        bugs = db.getPendingBugs(baseURL);
        totalBugsCount = db.getTotalBugsCount(baseURL);
        pendingBugsCount = db.getPendingBugsCount(baseURL);
      } else {
        System.out.println("Wrong arguments");
        System.exit(0);
      }
    } else {
      bugs = db.getPendingBugs();
      totalBugsCount = db.getTotalBugsCount();
      pendingBugsCount = db.getPendingBugsCount();
    }
    pendingBugsCount.next();
    totalBugsCount.next();
    totalPendingBugs = totalBugsCount.getInt("TOTAL_BUGS");
    bugsProcessed = pendingBugsCount.getInt("PENDING_BUGS");
    pendingBugsCount.close();
    totalBugsCount.close();
    while (bugs.next()) {
      Bug bug = new Bug(bugs.getInt("BUG_ID"), bugs.getString("BUGZILLA_PRODUCT"));
      ShowBugParser.parse(bug, db);
      bugsProcessed--;
    }
    bugs.close();
  }

  private static void uploadBugs(String dataFolder) throws SQLException, IOException {
    System.out.println("Begin importing bugs...");
    File f = new File(dataFolder);
    FilenameFilter textFilter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".csv");
      }
    };
    int fileCount = 0;
    File[] files = f.listFiles(textFilter);
    for (File file : files) {
      fileCount++;
      System.out
          .println("Processing... [" + fileCount + "/" + files.length + "] - " + file.getName());
      db.importBugs(file.getCanonicalPath());
    }
    System.out.println("Bugs successfully imported!");
  }

}
