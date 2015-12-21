package saahil.hiwi.crawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import saahil.hiwi.launcher.DB;

public class Crawler {
  public static Document crawl(String URL, DB db) {
    try {
      // check if the given URL is already in database
      ResultSet rs = db.checkURL(URL);
      if (rs.next()) {
        return null;
      } else {
        // store the URL to database to avoid parsing again
        db.storeURL(URL);
        Connection jsoupConnection = Jsoup.connect(URL);
        jsoupConnection.timeout(30000);
        try {
          return jsoupConnection.get();
        } catch (SocketTimeoutException e) {
          System.out.println("Timeout on " + URL);
          return null;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
