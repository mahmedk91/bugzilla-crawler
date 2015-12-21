package saahil.hiwi.launcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import saahil.hiwi.entities.Bug;

public class DB {

  public Connection conn = null;

  public DB() {
    try {
      if (Config.DB_SERVER.equals("mysql")) {
        Class.forName("com.mysql.jdbc.Driver");
      } else if (Config.DB_SERVER.equals("postgresql")) {
        Class.forName("org.postgresql.Driver");
      } else {
        throw new BadConfigException(Config.DB_SERVER
            + " db server is not supported by the application. Please read documentation of DB_SERVER");
      }
      String url = "jdbc:" + Config.DB_SERVER + "://" + Config.DB_HOST + ":" + Config.DB_PORT + "/"
          + Config.DB_NAME;
      conn = DriverManager.getConnection(url, Config.DB_USER, Config.DB_PASSWORD);
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (BadConfigException e) {
      e.printStackTrace();
    }
  }

  public boolean runSql2(String sql) throws SQLException {
    Statement sta = conn.createStatement();
    return sta.execute(sql);
  }

  public void importBugs(String csvFile, String baseURL) throws SQLException {
    System.out.println("Begin importing bugs...");
    String sql = "LOAD DATA LOCAL INFILE ? INTO TABLE BUGS " + "FIELDS TERMINATED BY ',' "
        + "ENCLOSED BY '\"' " + "LINES TERMINATED BY '\n' " + "IGNORE 1 LINES " + "(@col1) "
        + "SET BUG_ID=@col1, BUGZILLA_PRODUCT=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, csvFile);
    stmt.setString(2, baseURL);
    stmt.execute();
    System.out.println("Bugs successfully imported!");
  }

  @Override
  protected void finalize() throws Throwable {
    if (conn != null || !conn.isClosed()) {
      conn.close();
    }
  }

  public void saveDiff(Bug bug, int i) throws SQLException {
    String sql =
        "INSERT INTO  `CRAWLER`.`Diffs` " + "(`BUG_ID`,`DIFF_ID`,`DIFF`) VALUES " + "(?,?,?);";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setInt(1, bug.getId());
    stmt.setInt(2, bug.getPatches().get(i).getId());
    stmt.setString(3, bug.getPatches().get(i).getDiff());
    stmt.execute();
  }

  public void updateBugToNoDiff(Bug bug) throws SQLException {
    String sql = "UPDATE `CRAWLER`.`BUGS` SET `PARSE_STATUS`='NO_DIFF' WHERE `BUG_ID`=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setInt(1, bug.getId());
    stmt.execute();
  }

  public void updateBugToDone(Bug bug) throws SQLException {
    String sql = "UPDATE `CRAWLER`.`BUGS` SET `PARSE_STATUS`='DONE', `PRODUCT`=?, `DESCRIPTION`=?, "
        + "`TITLE`=?, `IMPORTANCE`=?, `STATUS`=? WHERE `BUG_ID`=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, bug.getProduct());
    stmt.setString(2, bug.getDescription());
    stmt.setString(3, bug.getTitle());
    stmt.setString(4, bug.getImportance());
    stmt.setString(5, bug.getStatus());
    stmt.setInt(6, bug.getId());
    stmt.execute();
  }

  public ResultSet checkURL(String URL) throws SQLException {
    String sql = "SELECT * FROM RECORD WHERE URL =?";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, URL);
    return stmt.executeQuery();
  }

  public void storeURL(String URL) throws SQLException {
    String sql = "INSERT INTO  `CRAWLER`.`RECORD` " + "(`URL`) VALUES " + "(?);";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, URL);
    stmt.execute();
  }

  public ResultSet getPendingBugs(String baseURL) throws SQLException {
    String sql = "SELECT BUG_ID FROM BUGS WHERE PARSE_STATUS='PENDING' AND BUGZILLA_PRODUCT=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, baseURL);
    return stmt.executeQuery();
  }

  public ResultSet getPendingBugsCount(String baseURL) throws SQLException {
    String sql =
        "SELECT COUNT(*) AS PENDING_BUGS FROM BUGS WHERE PARSE_STATUS='PENDING' AND BUGZILLA_PRODUCT=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, baseURL);
    return stmt.executeQuery();
  }

  public ResultSet getTotalBugsCount(String baseURL) throws SQLException {
    String sql =
        "SELECT COUNT(*) AS TOTAL_BUGS FROM BUGS WHERE BUGZILLA_PRODUCT=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, baseURL);
    return stmt.executeQuery();
  }
}


