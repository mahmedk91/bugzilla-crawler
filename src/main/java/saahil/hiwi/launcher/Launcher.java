package saahil.hiwi.launcher;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import saahil.hiwi.crawler.ShowBugParser;
import saahil.hiwi.entities.Bug;

public class Launcher {
  public static int totalPendingBugs;
  public static int bugsProcessed;
  public static DB db = new DB();

  public static void main(String[] args) throws SQLException, IOException {
    System.setProperty("javax.net.ssl.trustStore", Config.SSL_TRUSTSTORE);
    db.runSql2("TRUNCATE Record;");
    // For SQLite
    // db.runSql2("DELETE FROM record;");
    String csvFile;
    String baseURL;
    if (args.length < 2) {
      Scanner input = new Scanner(System.in);
      System.out.println("No program arguments given...");
      System.out.print("Enter full path of bug list CSV file: ");
      csvFile = input.nextLine();
      System.out.print("Enter base URL of program's bugzilla website: ");
      baseURL = input.nextLine();
      input.close();
    } else {
      csvFile = args[0];
      baseURL = args[1];
    }
    db.importBugs(csvFile, baseURL);
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
      Bug bug = new Bug(bugs.getInt("BUG_ID"));
      ShowBugParser.parse(baseURL, bug, db);
      bugsProcessed--;
    }
    bugs.close();
  }

}
