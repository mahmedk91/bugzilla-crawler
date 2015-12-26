package saahil.hiwi.launcher;

public class Config {
  /**
   * Location of SSL Truststore where SSL certificate of target site is stored.<br>
   * Crawling of HTTPS sites are only possible after adding their certificate to the SSL Truststore.
   * By default java uses cacert truststore with password "changeit". cacert is located in
   * "jdk&lt;version&gt;\jre\lib\security"<br>
   * Download the target site SSL certificate via Chrome/Firefox and add it to keystore using - <br>
   * <b>keytool -import -alias "&lt;alias_name&gt;" -file "Example.cer" -keystore "&lt;path to jdk&gt;/jdk&lt;version&gt;/jre/lib/security/cacerts"</b><br>
   * Keytool program is located in jdk&lt;version&gt;\bin
   */
  public static final String SSL_TRUSTSTORE =
      "";

  /**
   * Type of Database.<br>
   * Set DB_SERVER to either <b>mysql</b>, <b>postgresql</b> or <b>sqlite</b> to specify type of db.
   */
  public static final String DB_TYPE = "mysql";

  /**
   * <b>Only for sqlite</b>.<br> Location of sqlite database file
   */
  public static final String DB_LOC = "";
  
  /**
   * Name of Database host.
   */
  public static final String DB_HOST = "localhost";

  /**
   * Port number of database server
   */
  public static final String DB_PORT = "3306";

  /**
   * Name of Database.
   */
  public static final String DB_NAME = "CRAWLER";

  /**
   * Name of database user
   */
  public static final String DB_USER = "";

  /**
   * Database password of specified user
   */
  public static final String DB_PASSWORD = "";

}
