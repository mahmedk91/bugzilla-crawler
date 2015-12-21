package saahil.hiwi.entities;

public class Diff {
  private int id;
  private String diff;

  public Diff(int id) {
    super();
    this.id = id;
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
}
