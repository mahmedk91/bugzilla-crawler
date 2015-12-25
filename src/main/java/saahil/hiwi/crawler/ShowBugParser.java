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
import saahil.hiwi.launcher.Projects;

public class ShowBugParser {

  public static void parse(Bug bug, DB db) {
    System.out.print("Processing... [" + Launcher.bugsProcessed + "/" + Launcher.totalPendingBugs
        + "] - BUG_ID: " + bug.getId() + " - " + bug.getBugzillaProduct());
    Document doc = Crawler.crawl(bug.getBugzillaProduct() + "/show_bug.cgi?id=" + bug.getId());
    if (doc == null) {
      System.out.print(" - PENDING\n");
      return;
    }
    Elements patches = doc.getElementsByClass("bz_patch").not(".bz_tr_obsolete");
    for (Element patch : patches) {
      switch (bug.getBugzillaProduct()) {
        case Projects.NOVELL:
        case Projects.LIBRE_OFFICE:
        case Projects.MOZILLA:
        case Projects.KDE:
          Elements spans = patch.getElementsByClass("bz_attach_extra_info");
          for (Element span : spans) {
            if (span.text().contains("patch")) {
              try {
                URL diffUrl = new URL(bug.getBugzillaProduct() + "/" + span.parent().child(0).attr("href"));
                String diffUrlParams = diffUrl.getQuery();
                String[] diffId = diffUrlParams.split("=");
                Diff diff = new Diff(Integer.parseInt(diffId[1]), diffUrl.toString());
                List<Diff> diffs = bug.getPatches();
                diffs.add(diff);
                bug.setPatches(diffs);
              } catch (MalformedURLException e) {
                e.printStackTrace();
              }
            }
          }
          break;
        case Projects.GNOME:
        case Projects.APACHE:
        case Projects.LINUX_KERNAL:
        case Projects.GENTOO:
        case Projects.OPEN_OFFICE:
        default:
          Elements links = patch.select("a[href]");
          for (Element link : links) {
            if (link.attr("href").contains("action=diff")) {
              try {
                URL diffUrl = new URL(bug.getBugzillaProduct() + "/" + link.attr("href"));
                String[] diffUrlParams = diffUrl.getQuery().split("&");
                String[] diffId = diffUrlParams[0].split("=");
                String diffUri = bug.getBugzillaProduct() + "/attachment.cgi?id=" + diffId[1]
                    + "&action=diff&context=patch&collapsed=&headers=1&format=raw";
                Diff diff = new Diff(Integer.parseInt(diffId[1]), diffUri);
                List<Diff> diffs = bug.getPatches();
                diffs.add(diff);
                bug.setPatches(diffs);
              } catch (MalformedURLException e) {
                e.printStackTrace();
              }
            }
          }
      }
    }
    bug.setStatus(doc.getElementById("static_bug_status").text());
    bug.setProduct(doc.getElementById("field_container_product").text());
    bug.setTitle(doc.getElementById("short_desc_nonedit_display").text());
    Elements comments = doc.getElementsByClass("bz_first_comment");
    for (Element description : comments) {
      bug.setDescription(description.child(1).text());
    }
    Elements links = doc.select("a[href]");
    for (Element link : links) {
      if (link.attr("href").contains("#importance") || link.attr("href").contains("#priority")){
        bug.setImportance(link.parent().parent().parent().child(1).text());
        break;
      }
    }
    if (patches.size() < 1) {
      try {
        db.updateBug(bug, "NO_DIFF");
        System.out.print(" - NO DIFF\n");
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return;
    }
    DiffParser.parse(bug, db);
    try {
      db.updateBug(bug, "DONE");
      System.out.print(" - DONE\n");
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

}
