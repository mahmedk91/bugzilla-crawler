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
      String url = "jdbc:" + Config.DB_TYPE + "://" + Config.DB_HOST + ":" + Config.DB_PORT + "/"
          + Config.DB_NAME;
      switch (Config.DB_TYPE) {
        case "sqlite":
          Class.forName("org.sqlite.JDBC");
          conn = DriverManager.getConnection("jdbc:sqlite:"+Config.DB_LOC);
          break;
        case "postgresql":
          Class.forName("org.postgresql.Driver");
          conn = DriverManager.getConnection(url, Config.DB_USER, Config.DB_PASSWORD);
          break;
        case "mysql":
          Class.forName("com.mysql.jdbc.Driver");
          conn = DriverManager.getConnection(url, Config.DB_USER, Config.DB_PASSWORD);
          break;
        default:
          throw new BadConfigException(Config.DB_TYPE
              + " db server is not supported by the application. Please read documentation of DB_SERVER");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (BadConfigException e) {
      e.printStackTrace();
    }
  }

  public ResultSet runSql(String sql) throws SQLException {
    Statement sta = conn.createStatement();
    return sta.executeQuery(sql);
  }

  public void runSql2(String sql) throws SQLException {
    Statement sta = conn.createStatement();
    sta.execute(sql);
  }

  public void importBugs(String csvFile) throws SQLException {
    String sql = "LOAD DATA LOCAL INFILE ? INTO TABLE BUGS " + "FIELDS TERMINATED BY ',' "
        + "ENCLOSED BY '\"' " + "LINES TERMINATED BY '\n' (@col1, @col2) "
        + "SET BUG_ID=@col1, BUGZILLA_PRODUCT=@col2;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, csvFile);
    stmt.execute();
  }

  @Override
  protected void finalize() throws Throwable {
    if (conn != null || !conn.isClosed()) {
      conn.close();
    }
  }

  public void saveDiff(Bug bug, int i) throws SQLException {
    String sql = "INSERT INTO `DIFFS` "
        + "(`BUG_ID`, `BUGZILLA_PRODUCT`, `DIFF_ID`, `DIFF`) VALUES " + "(?, ?, ?, ?);";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setInt(1, bug.getId());
    stmt.setString(2, bug.getBugzillaProduct());
    stmt.setInt(3, bug.getPatches().get(i).getId());
    stmt.setString(4, bug.getPatches().get(i).getDiff());
    stmt.execute();
  }

  public void updateBug(Bug bug, String parseStatus) throws SQLException {
    String sql = "UPDATE `BUGS` SET `PARSE_STATUS`=?, `PRODUCT`=?, `DESCRIPTION`=?, "
        + "`TITLE`=?, `IMPORTANCE`=?, `STATUS`=? WHERE `BUG_ID`=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, parseStatus);
    stmt.setString(2, bug.getProduct());
    stmt.setString(3, bug.getDescription());
    stmt.setString(4, bug.getTitle());
    stmt.setString(5, bug.getImportance());
    stmt.setString(6, bug.getStatus());
    stmt.setInt(7, bug.getId());
    stmt.execute();
  }

  public ResultSet getPendingBugs(String baseURL) throws SQLException {
    String sql =
        "SELECT BUG_ID, BUGZILLA_PRODUCT FROM BUGS WHERE PARSE_STATUS='PENDING' AND BUGZILLA_PRODUCT=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, baseURL);
    return stmt.executeQuery();
  }

  public ResultSet getPendingBugsCount(String baseURL) throws SQLException {
    String sql =
        "SELECT COUNT(*) AS PENDING_BUGS FROM BUGS WHERE PARSE_STATUS!='PENDING' AND BUGZILLA_PRODUCT=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, baseURL);
    return stmt.executeQuery();
  }

  public ResultSet getTotalBugsCount(String baseURL) throws SQLException {
    String sql = "SELECT COUNT(*) AS TOTAL_BUGS FROM BUGS WHERE BUGZILLA_PRODUCT=?;";
    PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    stmt.setString(1, baseURL);
    return stmt.executeQuery();
  }

  public ResultSet getPendingBugs() throws SQLException {
    return runSql("SELECT BUG_ID, BUGZILLA_PRODUCT FROM BUGS WHERE PARSE_STATUS='PENDING';");
  }

  public ResultSet getTotalBugsCount() throws SQLException {
    return runSql("SELECT COUNT(*) AS TOTAL_BUGS FROM BUGS;");
  }

  public ResultSet getPendingBugsCount() throws SQLException {
    return runSql("SELECT COUNT(*) AS PENDING_BUGS FROM BUGS WHERE PARSE_STATUS!='PENDING';");
  }

  public void dropConstraints() throws SQLException {
    runSql2("ALTER TABLE DIFFS DROP FOREIGN KEY DIFFS_IBFK_1;");
    runSql2("ALTER TABLE BUGS MODIFY BUG_ID INT NOT NULL;");
    runSql2("ALTER TABLE BUGS MODIFY BUGZILLA_PRODUCT VARCHAR(50) NOT NULL;");
    runSql2("ALTER TABLE BUGS DROP PRIMARY KEY;");
  }

  public void reCreateConstraints() throws SQLException {
    runSql2("ALTER TABLE BUGS ADD PRIMARY KEY(BUG_ID, BUGZILLA_PRODUCT);");
    runSql2("ALTER TABLE DIFFS ADD FOREIGN KEY (`BUG_ID`,`BUGZILLA_PRODUCT`) "
        + "REFERENCES `BUGS`(`BUG_ID`,`BUGZILLA_PRODUCT`) "
        + "ON DELETE CASCADE ON UPDATE CASCADE;");
  }
}


