package saahil.hiwi.entities;

public class Diff {
  private int id;
  private String diff;
  private String Uri;

  public Diff(int id, String Uri) {
    super();
    this.id = id;
    this.Uri = Uri;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDiff() {
    return diff;
  }

  public void setDiff(String diff) {
    this.diff = diff;
  }

  public String getUri() {
    return Uri;
  }

  public void setUri(String uri) {
    Uri = uri;
  }
}
