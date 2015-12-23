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
    String dataFolder;
    String baseURL = null;
    if (args.length == 1) {
      baseURL = args[0];
    } else if (args.length == 2) {
      dataFolder = args[1];
      baseURL = args[0];
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
        db.importBugs(file.getCanonicalPath(), baseURL);
      }
      System.out.println("Bugs successfully imported!");
    } else {
      System.out.println("Wrong number of arguments.");
      System.exit(0);
    }
    ResultSet bugs = db.getPendingBugs(baseURL);
    ResultSet totalBugsCount = db.getTotalBugsCount(baseURL);
    ResultSet pendingBugsCount = db.getPendingBugsCount(baseURL);
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

}
