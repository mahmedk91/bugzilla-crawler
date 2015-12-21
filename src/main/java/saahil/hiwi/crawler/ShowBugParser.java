package saahil.hiwi.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import saahil.hiwi.entities.Bug;
import saahil.hiwi.entities.Diff;
import saahil.hiwi.launcher.DB;
import saahil.hiwi.launcher.Launcher;

public class ShowBugParser {

  public static void parse(String baseURL, Bug bug, DB db) {
    System.out.print("Processing... [" + Launcher.bugsProcessed + "/" + Launcher.totalPendingBugs
        + "] - BUG_ID: " + bug.getId());
    Document doc = Crawler.crawl(baseURL + "/show_bug.cgi?id=" + bug.getId(), db);
    if (doc == null) {
      System.out.print(" - PENDING\n");
      return;
    }
    Elements patches = doc.getElementsByClass("bz_patch").not(".bz_tr_obsolete");
    if (patches.size() < 1) {
      try {
        db.updateBugToNoDiff(bug);
        System.out.print(" - NO DIFF\n");
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return;
    }
    for (Element patch : patches) {
      Elements links = patch.select("a[href]");
      for (Element link : links) {
        if (link.attr("href").contains("action=diff")) {
          try {
            URL diffUrl = new URL(baseURL + "/" + link.attr("href"));
            String[] diffUrlParams = diffUrl.getQuery().split("&");
            String[] diffId = diffUrlParams[0].split("=");
            Diff diff = new Diff(Integer.parseInt(diffId[1]));
            List<Diff> diffs = bug.getPatches();
            diffs.add(diff);
            bug.setPatches(diffs);
          } catch (MalformedURLException e) {
            e.printStackTrace();
          }
        }
      }
    }
    DiffParser.parse(baseURL, bug, db);
    bug.setStatus(doc.getElementById("static_bug_status").text());
    bug.setProduct(doc.getElementById("field_container_product").text());
    bug.setTitle(doc.getElementById("short_desc_nonedit_display").text());
    Elements comments = doc.getElementsByClass("bz_first_comment");
    for (Element description : comments) {
      bug.setDescription(description.child(1).text());
    }
    Elements links = doc.select("a[href]");
    for (Element link : links) {
      if (link.attr("href").contains("#importance"))
        bug.setImportance(link.parent().parent().parent().child(1).text());
    }
    try {
      db.updateBugToDone(bug);
      System.out.print(" - DONE\n");
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

}
