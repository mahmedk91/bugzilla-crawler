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
  static ResultSet bugs;
  static ResultSet totalBugsCount;
  static ResultSet pendingBugsCount;
  public static DB db = new DB();

  public static void main(String[] args) throws SQLException, IOException {
    System.setProperty("javax.net.ssl.trustStore", Config.SSL_TRUSTSTORE);
    String dataFolder;
    String baseURL = null;
    if (args.length % 2 != 0) {
      System.out.println("Wrong number of arguments. Use -\n"
          + "-f     folderlocation -----> to upload bugs from csv\n"
          + "-url   baseUrl        -----> to crawl specific project");
      System.exit(0);
    } else if (args.length == 2) {
      if (args[0].equals("-f") && !Config.DB_TYPE.equals("sqlite")) {
        dataFolder = args[1];
        uploadBugs(dataFolder);
        collectBugsAndMetaData();
      } else if (args[0].equals("-url")) {
        baseURL = args[1];
        collectBugsAndMetaData(baseURL);
      } else {
        System.out.println("Wrong arguments. Use -\n"
          + "-f     folderlocation -----> to upload bugs from csv\n"
          + "-url   baseUrl        -----> to crawl specific project");
        System.exit(0);
      }
    } else if (args.length == 4 && !Config.DB_TYPE.equals("sqlite")) {
      if (args[0].equals("-f") && args[2].equals("-url")) {
        dataFolder = args[1];
        baseURL = args[3];
        uploadBugs(dataFolder);
        collectBugsAndMetaData(baseURL);
      } else if (args[0].equals("-url") && args[2].equals("-f")) {
        baseURL = args[1];
        dataFolder = args[3];
        uploadBugs(dataFolder);
        collectBugsAndMetaData(baseURL);
      } else {
        System.out.println("Wrong arguments.Use -\n"
          + "-f     folderlocation -----> to upload bugs from csv\n"
          + "-url   baseUrl        -----> to crawl specific project");
        System.exit(0);
      }
    } else {
      collectBugsAndMetaData();
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
      bugsProcessed++;
    }
    bugs.close();
  }

  private static void collectBugsAndMetaData() throws SQLException {
    System.out.println("\nCollecting bugs and meta data...");
    bugs = db.getPendingBugs();
    totalBugsCount = db.getTotalBugsCount();
    pendingBugsCount = db.getPendingBugsCount();
    System.out.println("\n-----------------------\nCrawling - All Projects\n-----------------------");
  }

  private static void collectBugsAndMetaData(String baseURL) throws SQLException {
    System.out.println("\nCollecting bugs and meta data...");
    bugs = db.getPendingBugs(baseURL);
    totalBugsCount = db.getTotalBugsCount(baseURL);
    pendingBugsCount = db.getPendingBugsCount(baseURL);
    System.out.println();
    for (int i=0;i<baseURL.length()+11;i++){
      System.out.print("-");
    }
    System.out.print("\nCrawling - "+baseURL+"\n");
    for (int i=0;i<baseURL.length()+11;i++){
      System.out.print("-");
    }
    System.out.println();
  }

  private static void uploadBugs(String dataFolder) throws SQLException, IOException {
    System.out.println("-----------\nBugs Import\n-----------");
    System.out.println("Dropping constraints for faster import..");
    System.out.println("Begin importing bugs...");
    db.dropConstraints();
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
    System.out.println("Re-creating constraints...");
    db.reCreateConstraints();
    System.out.println("Bugs successfully imported!");
  }

}
