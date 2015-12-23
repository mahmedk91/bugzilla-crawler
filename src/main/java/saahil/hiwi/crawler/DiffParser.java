package saahil.hiwi.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import saahil.hiwi.entities.Bug;
import saahil.hiwi.launcher.DB;

public class DiffParser {

  public static void parse(Bug bug, DB db) {
    for (int i = 0; i < bug.getPatches().size(); i++) {
      try {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request =
            new HttpGet(bug.getBugzillaProduct() + "/attachment.cgi?id=" + bug.getPatches().get(i).getId()
                + "&action=diff&context=patch&collapsed=&headers=1&format=raw");
        HttpResponse response;
        response = client.execute(request);
        BufferedReader rd =
            new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        String diff = "";
        while ((line = rd.readLine()) != null) {
          diff = diff + line + "\n";
        }
        bug.getPatches().get(i).setDiff(diff);
        db.saveDiff(bug, i);
      } catch (ClientProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
