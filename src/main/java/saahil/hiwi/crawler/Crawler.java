package saahil.hiwi.crawler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import saahil.hiwi.launcher.DB;

public class Crawler {
  public static Document crawl(String URL, DB db) {
    try {
      // check if the given URL is already in database
      String sql = "select * from Record where URL = '" + URL + "'";
      ResultSet rs = db.runSql(sql);
      if (rs.next()) {
        return null;
      } else {
        // store the URL to database to avoid parsing again
        sql = "INSERT INTO  `Crawler`.`Record` " + "(`URL`) VALUES " + "(?);";
        PreparedStatement stmt = db.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, URL);
        stmt.execute();
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
