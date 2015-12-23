package saahil.hiwi.crawler;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Crawler {
  public static Document crawl(String URL) {
    try {
        Connection jsoupConnection = Jsoup.connect(URL);
        jsoupConnection.timeout(30000);
        try {
          return jsoupConnection.get();
        } catch (SocketTimeoutException e) {
          System.out.print(" Timeout on " + URL);
          return null;
        } catch (ConnectException e){
          System.out.print(" Timeout on " + URL);
          return null;
        }
    }catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
