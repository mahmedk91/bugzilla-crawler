package saahil.hiwi.entities;

import java.util.ArrayList;
import java.util.List;

public class Bug {
  private int id;
  private String bugzillaProduct;
  private String title;
  private String description;
  private String importance;
  private String product;
  private String status;
  private List<Diff> patches;

  public Bug(int id, String bugzillaProduct) {
    super();
    this.id = id;
    this.bugzillaProduct = bugzillaProduct;
    this.patches = new ArrayList<Diff>();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
  
  public String getBugzillaProduct() {
    return bugzillaProduct;
  }

  public void setBugzillaProduct(String bugzillaProduct) {
    this.bugzillaProduct = bugzillaProduct;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getImportance() {
    return importance;
  }

  public void setImportance(String importance) {
    this.importance = importance;
  }

  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<Diff> getPatches() {
    return patches;
  }

  public void setPatches(List<Diff> patches) {
    this.patches = patches;
  }
}
