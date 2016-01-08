package saahil.hiwi.crawler;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLHandshakeException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import saahil.hiwi.launcher.Config;

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
    } catch (SSLHandshakeException e){
      System.out.println("\nUnable to get bug info. You might have forgotten to add SSL Certificate in "+Config.SSL_TRUSTSTORE);
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } 
  }
}
