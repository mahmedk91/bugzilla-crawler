# bugzilla-crawler
**A Java based Bugzilla crawler that can download all bugs and respective patches of a software.**
- Built on top of Jsoup HTML Parser API which is a very convinient lib to parse HTML.
- Import bugs from csv file.
- Patches that resolve the bug are stored and obsolete patches are ignored.
- Run the program with -url flag to crawl bugzilla of specific project.
- Stop and resume the crawler anytime.
- Easily configure and switch between MySql, PostgreSql or Sqlite database. 
- Currently supports bugzilla of apache, gentoo, gnome, kde, libre-office, linux-kernel, mozilla, novell and open-office. A list of bugzilla websites can be found [here](https://www.bugzilla.org/installation-list/), but not all of them are supported.  
 
## Importing SSL certificates
Most of the bugzilla websites use HTTPS to transfer their content. Java uses a SSL Truststore to look for the SSL certificate of the target bugzilla website. In order to crawl such a site, you first need to add the SSL certificate to the truststore. 

##### Downloading SSL of a bugzilla website
Downloading the SSL certificate of a bugzilla website is fairly easy. Just open the website and click on the **Green lock icon** in the address bar of the browser. Go to details of the certificate and export the certificate to a .cer file.  
You can also download certificate via [OpenSSL](http://superuser.com/questions/97201/how-to-save-a-remote-server-ssl-certificate-locally-as-a-file)

##### Importing .cer file to Java Truststore
Use keytool to add a certificate to the Java Truststore. By default Java uses **cacert** file located in **&lt;path_to_java&gt;/Java/jdk&lt;version&gt;/jre/lib/security/**  
The password of the cacert truststore is **changeit** by default. Use the following command to import the certificate followed by verifying the password and conforming the import of the certificate with a **yes** -  
``` bash
keytool -import -alias "example" -file "<path_to_certificate>/example.cer" -keystore "<path_to_java>/Java/jdk<version>/jre/lib/security/cacerts"
```

*Note - "&lt;path_to_java&gt;\Java\jdk&lt;version&gt;\bin\" must be added to your PATH environment variable before using keytool.*

## Preparing csv Buglist
The result of a search page in any bugzilla website is limited to 10000 records. That means, you cannot see more than 10000 issues at a time. However, a bugzilla site could have more than 10000 documented bugs.  
To get all bugs you could refine your search for fewer products (instead of all products) and get less than 10000 bugs in the result. Keep doing this and you can find all bugs in the bugzilla website.
##### Downloading csv Buglist
Each time you search, go to the end of result page and download the list as a csv file.
##### Combining csv Buglist
If you want to combine several csv files into one then - 
- Put all csv that you want to combine in a folder.
- Go to that folder
- Make a newFolder.
- Command for Windows -
  ``` bash
  type *.csv > newFolder/mergedBugList.csv
  ```
  
  Command for Linux - 
  ``` bash
  cat *.csv > newFolder/mergedBugList.csv
  ```
  
##### Manually re-formatting csv Buglist
The csv files need a little bit of reformatting in order to be compatible with all 3 kind of databases supported by bugzilla-crawler.
- There must be no header in csv file, i.e, No first line as column label.
- Update all columns of the csv to empty string, just retain Bug Id column as it is.
- Store the url of bugzilla website in second column without trailing "/".
- Store **PENDING** in 8th column (especially, if you use sqlite database).
- Finally your re-formatted csv would look something like this - 
  ```
  41142,https://bz.apache.org/bugzilla,,,,,,PENDING
  29901,https://bz.apache.org/bugzilla,,,,,,PENDING
  ...
  ```

  *Note - This csv format is compatible with all 3 kinds of database.*
- If you use **MySql** or **PostgreSql**, your csv files should atleast have data till **second column**. For example - 
  ```
  41142,https://bz.apache.org/bugzilla
  29901,https://bz.apache.org/bugzilla
  ...
  ```

  *Note - This csv format is not compatible with sqlite database.*  
  
## Creating database
##### MySQL
- Download MySql from [here](http://dev.mysql.com/downloads/mysql/)
- Create a username and password
- Use the following command to create schema - 
  ``` bash
  mysql --host=<hostname> --user=<username> --password=<password> < "<path_to_cloned_github_directory>/Database/MySQL Database Schema.sql"
  ```
  
  *Note - If you are creating database on local machine, then hostname is "locahost".*  
  
##### SqLite
- Download sqlite shell from [here](https://www.sqlite.org/download.html)
- Run sqlite3 shell
- Make a new database
  ``` bash
  .open "<path_to_database>/<database_name>.db"
  ```  
  
- Use the following command to create schema - 
  ``` bash
  .read "<path_to_cloned_github_directory>/Database/SQLite Database Schema.sql"
  ```  
  
- Exit the shell
  ``` bash
  .exit
  ```  
  
## Importing buglist in database
*Note - If you use MySql or PostgreSql, you can skip this section.*  
SqLite doesn't have statements to import data from csv. However, its shell provide "limited" features to import data directly from csv (only if you have data in correct format, Thanks to the steps above ;P).   
- Run sqlite3 shell
- Open your database
  ``` bash
  .open "<path_to_database>/<database_name>.db"
  ``` 
  
- Change mode to csv
  ``` bash
  .mode csv
  ```  
  
- Import all csv files one by one using
  ``` bash
  .import "<path_to_csvFolder>/file.csv"
  ```  
  
  *Note - Take advantage of above steps to combine all csv files in one. That way, you can import all data in a single go.*
- Exit the shell

  ``` bash
  .exit
  ```
  
## Configuring bugzilla-crawler
This is the final step where you tell bugzilla-crawler type of database, hostname, user, location of truststore, etc. Following are the params which need to be specified in **Config.java** -
- ***SSL_TRUSTSTORE*** - Location of truststore where SSL certificates of bugzilla site are stored. If you are using the default Java truststore (cacert), then location of your truststore is **&lt;path_to_java&gt;/Java/jdk&lt;version&gt;/jre/lib/security/cacerts**
- ***DB_TYPE*** - Type of database you are using with bugzilla-crawler. It can be set to either **mysql**, **postgresql** or **sqlite**.
- ***DB_LOC*** - Location of sqlite database file.  
*Note - This is only important if you use sqlite database else ignore it. You can ignore rest of the following settings as they don't concern SQLite.*
- ***DB_HOST*** - Host of your database. Use **localhost** if your database exist on the local machine or **hostname of your database server** if your database exists on external database server.
- ***DB_PORT*** - Port number on which your database server is running. By default port of MySql is **3306** and PostgreSql is **5432** (Unless Ofcourse, if you have changed it).
- ***DB_NAME*** - Name of the database. In our schema creation sql we have used **CRAWLER**. Specify different name if you have changed the database name.
- ***DB_USER*** - Username of the database user. This user must atleast have the privilege to SELECT and UPDATE the data and ALTER the schema.
- ***DB_PASSWORD*** - Password of the database user.  

## Download dependencies and build using maven
The following maven command would download all the dependencies first and then build bugzilla-crawler with the config data you specified in previous step.
``` bash
mvn clean install
```

## Start crawling
You can specify 2 **optional** flags to start the crawler in different modes -  
- ***-f &lt;path_to_csv_folder&gt;***&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; To upload bugs using csv files before starting to crawl.
- ***-url &lt;bugzilla_website_url_without_trailing_/&gt;***&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; To crawl bugs of only this bugzilla project.  

*Note - "-f" flag doesn't work for sqlite as bugs can only be imported manually in that case. Refer to the previous section on how to import bugs.*  

Use the following maven command to start the crawler - 
``` bash
mvn exec:java -Dexec.mainClass=saahil.hiwi.launcher.Launcher -Dexec.args="-f 'path_to_csvFolder' -url 'bugzilla_website_url_without_trailing_/'"
```
