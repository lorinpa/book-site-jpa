# book-site-jpa

Generates a web site customized for mobile devices.

Project is part of a series on Polyglot programming. Each article in the series describes
the process of implementing and enhancing the project application, in a different computer
language. This is an implementation of the "book club" project using Java, Spring, Hibernate, 
Java Persistence API (JPA),H2 Database, Jetty Web Server and Maven/JUnit automated tests.

A corresponding repository will house a Clojurescript client fot the "book club" project.

## Usage

$> mvn clean install cargo:deploy

## License
Code licensed under [GNU General Public License, version 2] (http://www.gnu.org/licenses/gpl-2.0.html")

## License
Written by Lorin M Klugman

## H2 Database
Installation and documentiation for H2 is located here:
[H2 Database](http://h2database.com/html/main.html)

The H2 console is an interactive gui application. Run:

$ h2> sh bin/h2.sh

You can use the H2 console to restore a corrupted database file, backup, restore or simply view your database contents.

To backup the database schema only:

1) Run the h2 interactive console

2) Complete the connect dialog
THe H2 console is displayed

3) Paste or type the following command into the h2 console 

    Script nodata to 'PATH-TO-SCHEMA-SQL-FILE'

Note: PATH-TO-SCHEMA-SQL-FILE is a new file location, determined by you. E.G. $HOME/backup/book-jpa-db-sql


4) Click "Run"
The h2 console displays the execution details


To recreate a database instance from the backup schema sql file:

1) Run the h2 interative console

2) Complete the connect dialog by entering the new database path location. Note the
path must exist, a new file is created.

Example:
 
JDBC URL: jdbc:h2:~/temp/sample

Creates a "sample.mv.db" file in the $HOME/temp directory.

3) Now recreate the schema (tables, constraints, etc). The H2 console is displayed.
Enter the following command:

RUNSCRIPT from 'PATH-TO-SCHEMA-SQL-FILE'


or 

Simply from the linux command line:
$> java -cp h2*.jar org.h2.tools.RunScript -url "jdbc:h2:~PATH-TO-DATABASE-FILE" -script "~PATH-TO-SCHEMAL-SQL-FILE"
