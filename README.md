Making JDBC connections to MarkLogic SQL/ODBC Server
====================================================

Note Bene
---------
This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
This is not a MarkLogic product, but is an unsupported, unofficial tool developed by MarkLogic consultants in the field. 
You should test it in a development environment thoroughly before depending on it in production.

If possible you should use the ODBC drivers supported by MarkLogic. 
If you can only use JDBC (because for example the tool only supports JDBC) then you have two options for drivers.
You have a choice of using the JDBC driver from PostgreSQL with no modifications `postgresql-42.1.4.jar` or the enhanced driver `mljdbc-42.1.4.jar` contained in this project.

B.L.U.F. (Bottom Line Up Front) 
--------
A [JDBC](https://en.wikipedia.org/wiki/Java_Database_Connectivity) type 4 driver can be used to connect to the read only MarkLogic ODBC server. 
The [PostgreSQL JDBC driver](https://jdbc.postgresql.org/) supports SQL queries to MarkLogic Server from a wide variety of clients.
This project modifies the PostgreSQL driver to provide additional support for MarkLogic metadata functions. The metadata functions allow a client to ask for a list of schemas, tables and columns for reporting and data type mappings from SQL to Java. This driver provides read-only access to SQL views built in MarkLogic using the Template Drive Extraction (TDE) feature. See MarkLogic's SQL Modeling Guide for more details on constructing TDE views compatible with this JDBC Driver at https://docs.marklogic.com/guide/sql. 

tl;dr (too long; didn't read)
-----
```
mljdbc-42.1.4.jar
Driver Name: "com.marklogic.Driver"
Driver URL: "jdbc:marklogic://localhost:8077/"
```
Example: https://github.com/marklogic-community/ml-gradle/wiki/JDBC

Overview
--------
In ML9 MarkLogic provides a read only SQL engine and an application server over TCP/IP.
While the ML application server is called ODBC it is actually not specific to ODBC and the protocol can also support JDBC.
The server implements a database native communication protocol that can efficiently support both ODBC and JDBC clients.
The SQL supported by the core SQL engine is SQL92 SQLite, with the addition of some other [statements](http://docs.marklogic.com/guide/sql/SQLqueries). 
MarkLogic SQL enables you to connect Bussiness Intelligence (BI) anaysis and reporting tools to report your data.
Combinining template driven extraction [TDE](https://docs.marklogic.com/guide/app-dev/TDE)
and bussiness intellegence tools allows access to structured and unstructured data for analysis and reporting. Read and understand the ML support for SQL before assuming JDBC will work (such as SQL dialect and transaction support).

As of JDK 8 the JDBC/ODBC bridge is [no longer supported](https://docs.oracle.com/javase/7/docs/technotes/guides/jdbc/bridge.html)
The class sun.jdbc.odbc.JdbcOdbcDriver has been removed from JAVA. Work arounds on the Windows platform have been proposed, but are discouraged.
Commercial bridges are available at a cost.

This project contains the base driver code and enhancements to support the MarkLogic metadata functions and provide additional compatability like Digest authentication and default driver settings.

JDBC Driver
-----------
With a pure Java JDBC driver for MarkLogic you can write applications in JAVA or use applications that support JDBC for connecting to databases.
The PostgreSQL JDBC driver front end on the client connects with an ML "ODBC" server by means of the PostgreSQL network message protocol version 3 (PROTOCOL_VERSION). 
The server returns the relational-style data needed by the client such as BI applications to build reports.
A JDBC solution elminates the need for a client side ODBC manager and driver since the JDBC driver speaks directly to the native MarkLogic ODBC application server protocol.
This JDBC driver is based on code for the JDBC API specification 4.2, but the exposed implementation features are closer to version 1.20 January 10, 1997.

Architecture Layers
-------------------
```
User Application
JDBC Driver: MarkLogic JDBC "com.marklogic.Driver"
<<TCP/IP with PostgreSQL message protocol 3 (pg ver 7.4+)>>
MarkLogic ODBC Application Server
SQLCore
SQLite
MarkLogic server core
```

Setup
-----
First [Setup](http://docs.marklogic.com/guide/admin/odbc) an ODBC application server in MarkLogic. 

Then Download:
```
MarkLogic JDBC 4.2 Driver, 42.1.4
```
> [https://github.com/marklogic-community/ml-jdbc-driver/blob/master/jar/mljdbc-42.1.4.jar](https://github.com/marklogic-community/ml-jdbc-driver/blob/master/jar/mljdbc-42.1.4.jar)

or
```
PostgreSQL JDBC 4.2 Driver, 42.1.4
```
> [https://jdbc.postgresql.org/download/postgresql-42.1.4.jar](https://jdbc.postgresql.org/download/postgresql-42.1.4.jar)

Be sure to use the specific driver version.
Be sure to include the jar in your classpath.

Next Configure the connection driver name (aka Class.forName) and the connection string URL: `jdbc:<vendor>://<host>:<port>/`
```
Driver Name: "com.marklogic.Driver"
Driver URL: "jdbc:marklogic://localhost:8077/"
```
or
```
Driver Name: "org.postgresql.Driver"
Driver URL: "jdbc:postgresql://localhost:8077/?preferQueryMode=simple"
```

License
-------
- Be sure to include required [LICENSE.txt](https://github.com/marklogic-community/ml-jdbc-driver/blob/master/LICENSE.txt) and [NOTICE.txt](https://github.com/marklogic-community/ml-jdbc-driver/blob/master/NOTICE.txt) files with delivery. 

Documentation
-----------
Driver documentation https://jdbc.postgresql.org/documentation/head/index.html

GitHub https://github.com/pgjdbc/pgjdbc

ml-gradle: https://github.com/marklogic-community/ml-gradle/wiki/JDBC

preferQueryMode
------------
The MarkLogic custom driver now uses preferQueryMode=simple as default and does not require setting.
preferQueryMode value of simple mode matches the MarkLogic ODBC Server protocol.
preferQueryMode specifies which mode is used to execute queries to database: simple means ('Q' execute, no parse, no bind, text mode only),
extended means always use bind/execute messages, extendedForPrepared means extended for prepared statements only,
extendedCacheEverything means use extended protocol and try cache every statement (including Statement.execute(String sql)) in a query cache.

Connection
---------
Be sure to included the slash after the host colon port `jdbc:marklogic://<host>:<port>/`

The MarkLogic driver now defaults to preferQueryMode=simple (instead of extended).
If you are using the postgreSQL driver you must configure the query mode to use simple queries without parsing, bindings, and using text mode only.
`preferQueryMode=simple`

If you receive the error message: `ERROR: XDMP-INTERNAL: Internal error: 'D': no such portal ""`
Be sure to check the connection string for typos: preferQueryMode=simple

If you receive the stack trace message: org.postgresql.util.PSQLException: Protocol error.  Session setup failed.
And the error log contains: 
`Info: [Event:id=ODBCConnectionTask SendMessage] 71 [error response] <= E SERROR C08000 MXDMP-INTERNAL: Internal error: Password incorrect`
Be sure the password is correctly encoded in your JAVA for Digest authentication

`readOnly=true` can optionally be configured since MarkLogic only supports read-only operations over SQL.

Driver URL: `jdbc:marklogic://localhost:8077/?readOnly=true`

For logging and debugging in the driver loggerLevel=TRACE or loggerLevel=DEBUG can be configured to see driver-level messages on the client.
Use loggerFile=pgjdbc-trace.log for capturing log.

Allowed values: OFF, DEBUG or TRACE

For a full list of options supported by the driver see:
https://jdbc.postgresql.org/documentation/head/connect.html#connection-parameters

```java
import java.sql.*;
public class Test {
    public static void main (String[] args) {
        try {
            Class myClass = Class.forName("com.marklogic.Driver");
            System.out.println("Before getConnection");
            DriverManager.setLogStream(System.out);
            Connection conn = DriverManager.getConnection("jdbc:marklogic://localhost:8077/","user","pw");
            System.out.println("Connected successfully");
            // JAVA SQL code
            Statement stmt = conn.createStatement();
            String sql = "SELECT SCHEMA, NAME FROM SYS_TABLES";
            System.out.println("Before executeQuery");
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("Extract data from ResultSet");
            while(rs.next()){
                String schema = rs.getString("SCHEMA");
                String name = rs.getString("NAME");

                System.out.print("SCHEMA: " + schema);
                System.out.print(", NAME: " + name);
                System.out.print("\n");
            }
            rs.close();
            stmt.close();

            /* If using custom mljdbc driver */
            //DatabaseMetaData md = conn.getMetaData();
            //ResultSet mdrs = md.getTables(null, null, "%", null);
            //while (mdrs.next()) {
            //    System.out.println("SCHEMA: " + mdrs.getString(2) + ", NAME: " + mdrs.getString(3));
            //}
            //mdrs.close();
            //md.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

Authentication
--------------
The PostgreSQL driver's MD5 Digest hash method (in ConnectionFactoryImpl.java and MD5Digest.java) does not match the Digest method in MarkLogic.
PostgreSQL encodes user/password/salt information in the following way: MD5(MD5(password + user) + salt) where salt is realm ex. "public".
The custom driver's new approach uses user+":"+salt+":"+password like Apache with 32 bytes of salt.

Encription can be accomplished with `ssl=true` using certificates.
https://basildoncoder.com/blog/postgresql-jdbc-client-certificates.html

For SSL connections, you have several options:
- Import the self-signed cert into java's keystore,
- or use the non-validating ssl factory,
- or use LibPQFactory

The JDBC driver provides an option to establish a SSL connection without doing any validation.
For SSL connections with a non-validating certificate such as a self-signed server certificate generated by a MarkLogic certificate template.
First configure MarkLogic Server for SSL.
- Create a MarkLogic Server security certificate template.
- Set the SSL certificate template name on the ODBC application server.
Then use the connection string: `jdbc:marklogic://localhost:8077/?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory`

And add tracing for feedback: 
`jdbc:marklogic://localhost:8077/?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory&loggerLevel=TRACE`

The org.postgresql.ssl.jdbc4 package includes the LibPQFactory class which handles .crt files: 
`jdbc:marklogic://localhost:8077/?ssl=true&sslfactory=org.postgresql.ssl.jdbc4.LibPQFactory&sslmode=verify-ca&sslrootcert=certificate.crt&loggerLevel=TRACE`

Debugging
---------
To determine if the JDBC driver is in use for a connection, turn on diagnostic trace events for ODBCConnectionTask SendMessage ODBCConnectionTask ReceiveMessage then when a new connection is made the MarkLogic Server log for the ODBC port will contain the following indicating that the JDBC driver is in use (and not the ODBC driver):

`Info: [Event:id=ODBCConnectionTask ReceiveMessage] => Q SET application_name = 'MarkLogic JDBC Driver'` or 'PostgreSQL JDBC Driver'

For logging and debugging loggerLevel=TRACE or loggerLevel=DEBUG can be configured to see driver-level messages on the client.

For debugging SSL, add DEBUG -Djavax.net.debug=SSL to your JAVA command line.

On the client, using a connection string with loggerLevel TRACE `jdbc:marklogic://localhost:8077/?loggerLevel=TRACE`

```
FINEST:  FE=> StartupPacket(user=admin, database=, client_encoding=UTF8, DateStyle=ISO, TimeZone=America/New_York, extra_float_digits=2)
Jan 17, 2018 9:32:18 AM org.postgresql.core.v3.ConnectionFactoryImpl doAuthentication
FINEST:  <=BE AuthenticationReqMD5(salt=XXXXXXXX)
Jan 17, 2018 9:32:18 AM org.postgresql.core.v3.ConnectionFactoryImpl doAuthentication
FINEST:  FE=> Password(md5digest=md5XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX)
org.postgresql.util.PSQLException: Protocol error.  Session setup failed.
        at org.postgresql.core.v3.ConnectionFactoryImpl.doAuthentication(ConnectionFactoryImpl.java:622)
       at org.postgresql.core.v3.ConnectionFactoryImpl.openConnectionImpl(ConnectionFactoryImpl.java:222)
```

To help debug problems with JDBC connection to the ML ODBC server, I have found the diagnostic trace events to be very helpful.
`ODBCConnectionTask SendMessage`
`ODBCConnectionTask ReceiveMessage`

The PostgreSQL network message protocol will get logged with messages like "R", "S" and "Q" (receive, send and query).
For example,
On the server with diagnostic trace events turned on:
```
[Event:id=ODBCConnectionTask SendMessage] 41 [authentication] <= R auth=5(MD5) md5salt=public
[Event:id=ODBCConnectionTask ReceiveMessage] => p
[Event:id=ODBCConnectionTask SendMessage] 71 [error response] <= E SERROR C08000 MXDMP-INTERNAL: Internal error: Password incorrect
```

Transactions
-----------
ML is a read-only SQL interface (with some support for temporary views).

Watch out for any SQL that starts with `BEGIN`

The `BEGIN;` will produce an error from ML: `XDMP-UNEXPECTED: (err:XPST0003) Unexpected token syntax error, unexpected "<id>", expecting "<end of file>"`

With diagnostic trace events turned on examine the SQL in the ML server log.
`ODBCConnectionTask SendMessage`
`ODBCConnectionTask ReceiveMessage`
Queries that start with BEGIN are trying to do transactions which are not supported by ML. It may be as simple as turning on `autocommit`. It may be incompatible if the tool expects transaction support or writes to the database.

SQL Dialects and Extensions
---------------
MarkLogic supports a subset of the SQLite dialect. SQLServer and Oracle have extensions that are not supported. Pseudo columns like ROWID, ROWNUM or extension functions like INSTR, CHARINDEX are specific to Oracle or SQLServer and are not directly supported. On the other hand, MarkLogic provides the fn and xdmp functions (replace colons and dashes with underscore when used in SQL). For example, INSTR can be implemented with MarkLogic supported functions by `fn_subsequence(fn_index_of(fn_string_to_codepoints('a_bc_defg_hi'),fn_string_to_codepoints('_')),2,1)`. Dates are best handled in ML with [ISO-8601](https://en.wikipedia.org/wiki/ISO_8601). Various fn and xdmp functions can work with dates such as `xdmp_parse_dateTime('[D01]/[M01]/[Y0001] [h01]:[m01]:[s01]',create_datetime)`. Non conforming dates can be formatted into ISO-8601 using a TDE. `<val>xdmp_parse_dateTime('[D01]/[M01]/[Y0001] [h01]:[m01]:[s01]',./create_datetime)</val>`.

Useful functions
- fn_format_dateTime
- fn_index_of
- fn_string_to_codepoints
- fn_subsequence
- fn_tokenize
- xdmp_parse_dateTime
- xdmp_node_uri(__content) /* uri of source document */
- xdmp_document_timestamp(xdmp_node_uri(__content)) /* when was this doc updated */
- xdmp_document_get_collections(xdmp_node_uri(__content)) as collections /* collections of source document */
- math_stddev((122, 100, 23))

[Calling Built-in Functions from SQL](https://docs.marklogic.com/guide/sql/SQLqueries#id_13409)

Data Type Mappings
----------------
[Supported Types](https://docs.marklogic.com/guide/sql/SQLqueries#id_32736)

Troubles with dateTime are not a type mapping but a content problem: Trailing junk on timestamp: 'T00:00:00' at org.postgresql.jdbc.TimestampUtils.parseBackendTimestamp(TimestampUtils.java:339)

Java does not support ISO-8601 dateTime. Java datetimetz does not use the 'T' delimiter found in ISO-8601.
`'2013-01-02 03:04:05.060708 +9:00'` vs `2013-01-02T03:04:05.060708 +9:00` Work around does not work fn:format-dateTime(xs:dateTime("2013-01-02T03:04:05.060708 +9:00"),  "[Y0001]-[M01]-[D01] [H01]:[m01]:[s01].[f01] [z]","en","AD","US")

Modified TimestampUtils

Could look at cast(dateTime as timestamptz)

Problem with the Oid mapping values for unsignedShort, short, byte, unsignedByte are 0 from ML SQL function pg_type_id()

anyURI = 25 appears to be supported as text

Gradle Build
-----------
- Instructions for full build https://jdbc.postgresql.org/development/development.html
- Source is Java 1.8 compatible (refer to https://jdbc.postgresql.org/download.html)
- Be sure to match the protocol version of the driver to the server protocol version.
- Currently ML 9.0-3 uses PostgreSQL network message protocol version 3 (pg ver 7.4+) with preferQueryMode=simpile

A working build using Gradle has been created and pushed on the "gradle-build" branch. This build ensures that all static final variables are updated.

The jar can be built using:
              `gradle build -x test`

The unit tests can be run with:
              `gradle test`

Copied the original PostgreSQL test classes and found 519

```
9108 tests completed, 8589 failed, 6 skipped
```

The sources are now in:
```
              src/main/
              src/test/
```

Kept the files/directories for now, but we can/should remove it later.
```
              /https-github-com-pgjdbc
              /postgresql-42.1.4.jar
              /postgresql-jdbc-head-doc.tar.gz
```


Also needed to do the following to get a successful build:
- remove OSGI and SSPI related sources due to missing/unresolvable dependencies
- remove SharedTimerClassLoaderLeakTest (a unit test) due to missing/unresolvable dependencies


Why this version 42.1.4?
--------------------
No concrete reason. The latest version of the driver code did not work. I saw 7.4 in the ML ODBC doc and tried that driver and it at least connected. I moved forward to the more recent version 42.1.4 and then found preferQueryMode. The most current version of the driver is compatible with PostgreSQL 8.2 and higher which does not include 7.4.  Versions of the driver prior to 8.2 sent all PreparedStatement parameters to the server as untyped strings, and allowed the server to infer their types as appropriate. ML uses SQLite and has "manifest typing" which may explain the incompatibility with the latest version of the Postgres driver.

Modifications
-------------
Changes have been make in `org/postgresql` and `com/marklogic`. Original souce is in `https-github-com-pgjdbc/pgjdbc-REL42.1.4`.
- getTables to use sys.sys_tables
- getSchemas to use sys.sys_schemas
- getColumns to use sys.sys_columns
- Update getNumericFunctions, getStringFunctions, getTimeDateFunctions, getSystemFunctions added database, min, max, soundex for trial
- attempt mapping from escaped function to xdmp
- changed connection to jdbc:marklogic://localhost
- getTypeTypes constrained to tables and views
- NULL as col to '' as col
- getIndexInfo getTablePrivileges getImportedKeys
- Class.forName from org.postgresql.Driver to com.marklogic.Driver
- use sub select mechanism to map to pg_ column names select * from (select
- made preferQueryMode=simple default rather than extended PGProperty.java(371): PREFER_QUERY_MODE("preferQueryMode", "simple"
- Support Digest MD5 with modified algorithm
- use hash64 for table oid in PgResultSetMetaData
- remove serial, bigserial data types
- fix problem with MetaData IS_NULLABLE and NULLABLE in getColumns in both PgDatabaseMetaData and PgResultSetMetaData

com/marklogic/Driver
----------------
Added class for ML vendor branding

Class.forName com.marklogic.Driver

org/postgresql/jdbc/PgDatabaseMetaData
------------------
PgDatabaseMetaData has been modified to work the the MarkLogic schema tables: sys.sys_schemas, sys.sys_tables and sys.sys_columns.

getSchemas, getTables, getColumns, getTableTypes, getTypeInfo, getUDTs, getBestRowIdentifier, getIndexInfo, getImportedExportedKeys

getCatalog name in ML "__MarkLogic" to match ODBC (future: consider {fn xdmp_database_name(xdmp_database())})

modify "supports*" eg. supportsAlterTable*

Change "attnotnull" from rs.getBoolean("attnotnull") to (rs.getInt("attnotnull") != 0) for IS_NULLABLE and NULLABLE

Add support for integer, short, unsignedShort

org/postgresql/jdbc/TimestampUtils
------------------
Transform ISO-8601 dateTime to datetimetz by replacing 'T' with ' '

org/postgresql/jdbc/PgResultSetMetaData
------------------
PgResultSetMetaData has been modified to work the the MarkLogic sys.sys_columns.

fetchFieldMetaData

Change "attnotnull" from rs.getBoolean(6) to (rs.getInt(6) != 0) for IS_NULLABLE and NULLABLE

org/postgresql/jdbc/EscapedFunctions
----------------
Added ML specific string, numeric, date and system functions

getStringFunctions, getDateFunctions, getNumericFunctions, getSystemFunctions

escaped function syntax: select {fn xdmp_database_name(xdmp_database())}

org/postgresql/util/DriverInfo
----------
Additional ML vendor branding

Changed static DRIVER_NAME, DRIVER_SHORT_NAME for ML

Update to support version number from gradle

org/postgresql/jdbc/PgConnection
----------------
Future consider how to handle select current-schema()

isValid method change executeUpdate to execute (non DDL) triggered by checkClosed();

org/postgresql/PGProperty
----------------
PREFER_QUERY_MODE("preferQueryMode", "simple" changed from "extended" for default

org/postgresql/core/v3/ConnectionFactoryImpl
----------------
doAuthentication case AUTH_REQ_MD5

Configure salt buffer 32 for Digest MD5

org/postgresql/MD5Digest
----------------
Change encode algorithm to MD5(user:realm:password)

org/postgresql/jdbc/TypeInfoCache 
----------------
default length for varchar is 255

org/postgresql/core/Oid (no change)
----------------
data types to oid mappings which do not appear to match ML (time vs timetz)

org/postgresql/jdbc/PgResultSet
------------------
Change boolean test to handle true/false vs t/f

To Do (todo)
-----
- ! check columnSize vs pg_type_size(type)
- ! Oid mapping (unsignedShort, short, byte, unsignedByte) pg_type_id returns 0
- Why is dateTime to timestamptz a problem?
- Obtain legal approval to continue
- Request Enineering mentoring esp. datatype mapping and initial settings
- Identify test suite for targetted BI and client tools (marketing)
- Extend additional metadata functions as needed by clients
- Build from source with maven
- check nullsAreSortedHigh true and other settings
- validate PgDatabaseMetaData getSQLKeywords, getUDTs, getTypeInfo (getDataTypes)
- ! ML vs Postgres data type (oid) mappings are different like time (1083,1266) vs timetz (1266,0)
- figure out how to configure "supportsAddColumn", "supportsCreateTable", supportsDropIndex, supportsDropColumn, supportsAddPrimaryKey, supportsDropPrimaryKey et al. in Supported Refactorings https://github.com/krichter722/squirrel-sql/blob/master/sql12/core/src/net/sourceforge/squirrel_sql/fw/dialects/PostgreSQLDialectExt.java
- compare results with JDBC ODBC bridge https://www.easysoft.com/products/data_access/jdbc_odbc_bridge/index.html#section=tab-1
- verify UTF-8 vs cp1252 encoding
- ! "uuid" data type mapping to SQL type Oid seems to be wrong
- check PgResultSetMetaData.fetchFieldMetaData use of hash64 as table ID
- check TypeInfoCache.getSQLType, getOidStatement, getPGType, getArrayDelimiter, getPGArrayElement et al.
- update DriverInfo version number vs maven comment
- validate PgDatabaseMetaData mapping to SYSTEM TABLE (vs Catalogs in ODBC)
- check data type vs ODBC: "string(255) NOT NULL" vs varchar(-4) unlimited
- expose system columns like __content __docid?
- ! consider using current master PostgreSQL JDBC code base https://jdbc.postgresql.org/download.html by overriding server compatibility to lower than PostgreSQL 8.2 (7.4+)
- understand impact of SQLite manifest typing for users of JDBC

Testing and Validation
---------
Compared results of JDBC with ODBC using "Open ODBC Querytool" and JDBC with dbVisualizer and Squirrel

getTypeInfo : SQLGetTypeInfo 
getStringFunctions : SQLGetFunctions
getNumericFunctions
getSystemFunctions
getDateFunctions

Setup TDE with data types [TDE_JDBCTestDataTypes.xml](https://github.com/marklogic-community/ml-jdbc-driver/blob/master/TDE_JDBCTestDataTypes.xml) (string, int, unsignedInt, long, unsignedLong, decimal, float, double, boolean, date, time, dateTime, anyUri)

Reference
---------
This document specifies the minimum that a driver must implement to be compliant with the JDBC API.
- http://www.oracle.com/technetwork/java/driverdevs-137850.html

The MarkLogic SQL Guide describes the SQL query support in MarkLogic
- http://docs.marklogic.com/guide/sql/SQLqueries

Successful Connections
--------
- BIRT
- DBVisualizer
- Jaspersoft Studio
- PrestoDB
- Squirrel

Opportunities
--------
According to [wikipedia](https://en.wikipedia.org/wiki/Comparison_of_database_tools)

Open source JAVA tools often support only JDBC
- Yellowfin (The ODBC source must be setup on the same server as Yellowfin)
- JasperSoft (JDBC only AFAIK)
- Pentaho (recommendation to use JDBC drivers over ODBC drivers with Pentaho)
- Splunk
- Rapid Miner (JDBC only AFAIK)
- SpagoBI (JDBC only AFAIK)
- KNIME (JDBC only AFAIK)
- Karmasphere

Investigate http://www.jnosql.org/ jnosql-diana-driver

Squirrel
--------
[Squirrel Universal SQL Client](http://squirrel-sql.sourceforge.net/)

[Squirrel Doc](http://squirrel-sql.sourceforge.net/paper/SQuirreL_us.pdf)

Drivers - Extra Class Path - Add

Name: MarkLogicSQL

Example URL: jdbc:marklogic://localhost:8077/

Website URL: http://marklogic.com

C:\MarkLogic\jdbc\mljdbc-42.1.4.jar

Class Name: org.progresql.Driver

Test:
select database()

DBVisualizer
--------
DbVisualizer is the universal database tool for developers, DBAs and analysts.[site](https://www.dbvis.com/)

Tools - Tool Properties - Driver Manager - Search Path - Add

Tools - Tool Properties - Database - PostgreSQL 

Tools - Driver Manager - PostgreSQL

Tools - Connection Wizard - make MarkLogicSQL - add property use URL form

strange default of cp1252 instead of UTF8

ODBC Query Tool (aka EDO)
---------
Open ODBC Querytool. Query tool that specializes in working on a 3.5x ODBC database driver. Totally RDBMS independent as ODBC should be. Knows about every ODBC option, function and comes with full documentation.
[site](https://sourceforge.net/projects/odbcquerytool/)

Wireshark
--------
Wireshark with NPCap (instead of WinPCap) for localhost loopback
[site](https://www.wireshark.org/)
Protocol level debugging for preferQueryMode and MD5Digest
filter: tcp.port == 8077 || udp.port == 8077

Disclaimer
----------
SQL has inherent vice, like eggs they might break.
"An exclusion found in most property insurance policies eliminating coverage for loss caused by a quality in property that causes it to damage or destroy itself."

Notes
--------
Interesting page http://neo4j-contrib.github.io/neo4j-jdbc/#_tableau_via_jdbc2tde

ODBC trace:

```
[Event:id=ODBCConnectionTask SendMessage] 41 [authentication] <= R auth=5(MD5) md5salt=public
client_encoding value=UTF8
DateStyle value=ISO
integer_datetimes value=on
IntervalStyle value=iso_8601
server_encoding value=UTF8
server_version value=9.1.2
SET DateStyle = 'ISO';SET extra_float_digits = 2;show transaction_isolation
show transaction_isolation;SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ COMMITTED
```
```
select * from (select schema as nspname, table as relname, name as attname, pg_type_id(type) as atttypid, type as typname, cid as attnum, pg_type_size(type) as attlen, 0 as atttypmod, 'notnull' as attnotnull, 0 as relhasrules, type as relkind, 0 as oid, 0, 0, -1, 1, '' from sys.sys_columns) where 1 and relname like 'MHF' and nspname like 'Beacon' order by nspname, relname, attnum
```
```
select * from (select name as relname, schema as nspname, type as relkind from sys.sys_tables) where 1 and nspname like '%' and relname like '%'
```

- RFE (RFE-3047: JDBC driver for MarkLogic) https://project.marklogic.com/jira/browse/RFE-3047

- ENG (ENG-303: JDBC Connectivity)  https://project.marklogic.com/jira/browse/ENG-303

- ResultSetMetadata.getColumnType() https://lists.marklogic.com/thread/lqt6cuebux2tiu5r

Statement.setMaxRows()
supportsSubSelect = false
supportsColGrouping = false
Are not as critical as the above, but for more advanced functionality they are required (Freehand SQL Views, Virtual Tables in Views, and SubQueries in Reports, GROUP by with CASE)

SQLite has manifest typing: https://www.sqlite.org/different.html In manifest typing, the datatype is a property of the value itself, not of the column in which the value is stored.

https://www.progress.com/tutorials/jdbc/use-your-custom-jdbc-driver-with-tableau

Build your own Custom JDBC driver for REST API in 2 Hours
https://www.progress.com/tutorials/jdbc/build-your-own-custom-jdbc-driver-for-rest-api-in-2-hours

Apache Jena - Jena JDBC - A SPARQL over JDBC driver framework
https://jena.apache.org/documentation/jdbc/index.html

Using the ResultSet Interface
-------------------
(from pg doc)
The following must be considered when using the ResultSet interface:

Before reading any values, you must call next(). This returns true if there is a result, but more importantly, it prepares the row for processing.
You must close a ResultSet by calling close() once you have finished using it.
Once you make another query with the Statement used to create a ResultSet, the currently open ResultSet instance is closed automatically.

Using the Driver in a Multithreaded or a Servlet Environment
-----------------------------------
(from pg doc)
The PostgreSQL JDBC driver is not thread safe. The PostgreSQL server is not threaded. Each connection creates a new process on the server; as such any concurrent requests to the process would have to be serialized. The driver makes no guarantees that methods on connections are synchronized. It will be up to the caller to synchronize calls to the driver.

A notable exception is org/postgresql/jdbc/TimestampUtils.java which is threadsafe.

ODBC catalog queries
-----------------
-- [select * from (select NULL, name as nspname, NULL from sys.sys_schemas) where 1
and nspname not in ('sys') order by nspname]

-- [select * from (select name as relname, schema as nspname, type as relkind from
sys.sys_tables) where 1 and nspname not in ('sys')]

-- [select * from (select schema as nspname, table as relname, name as attname,
pg_type_id(type) as atttypid, type as typname, cid as attnum, pg_type_size(type)
as attlen, 0 as atttypmod, 'notnull' as attnotnull, 0 as relhasrules, type as
relkind, 0 as oid, 0, 0, -1 from sys.sys_columns) where 1 and relname like
'employees' and nspname like 'main' order by nspname, relname, attnum]

-- [select * from (select -1, type as relkind from sys.sys_tables) where 1 and name
= 'employees' and schema = 'main']

SQLGetTypeInfo 
-- [select * from (select schema as nspname, table as relname, name as attname,
pg_type_id(type) as atttypid, type as typname, cid as attnum, pg_type_size(type)
as attlen, 0 as atttypmod, 'notnull' as attnotnull, 0 as relhasrules, type as
relkind, 0 as oid, 0, 0, -1 from sys.sys_columns) where 1 and relname like
'employees' and nspname like 'main' order by nspname, relname, attnum]
[select * from (select -1, type as relkind from sys.sys_tables) where 1 and name
= 'employees' and schema = 'main']

TDE Types (tde.xsd) and pg_type_id and pg_type_size
--------
  <xs:simpleType name="scalar-type">
    <xs:restriction base="xs:NMTOKEN">
      <xs:enumeration value="int"/>
      <xs:enumeration value="unsignedInt"/>
      <xs:enumeration value="long"/>
      <xs:enumeration value="unsignedLong"/>
      <xs:enumeration value="float"/>
      <xs:enumeration value="double"/>
      <xs:enumeration value="decimal"/>
      <xs:enumeration value="dateTime"/>
      <xs:enumeration value="time"/>
      <xs:enumeration value="date"/>
      <xs:enumeration value="gYearMonth"/>
      <xs:enumeration value="gYear"/>
      <xs:enumeration value="gMonth"/>
      <xs:enumeration value="gDay"/>
      <xs:enumeration value="yearMonthDuration"/>
      <xs:enumeration value="dayTimeDuration"/>
      <xs:enumeration value="string"/>
      <xs:enumeration value="anyURI"/>
      <xs:enumeration value="point"/>
      <xs:enumeration value="longLatPoint"/>
      <xs:enumeration value="boolean"/>
      <xs:enumeration value="base64Binary"/>
      <xs:enumeration value="byte"/>
      <xs:enumeration value="duration"/>
      <xs:enumeration value="gMonthDay"/>
      <xs:enumeration value="hexBinary"/>
      <xs:enumeration value="integer"/>
      <xs:enumeration value="negativeInteger"/>
      <xs:enumeration value="nonNegativeInteger"/>
      <xs:enumeration value="nonPositiveInteger"/>
      <xs:enumeration value="positiveInteger"/>
      <xs:enumeration value="short"/>
      <xs:enumeration value="unsignedByte"/>
      <xs:enumeration value="unsignedShort"/> 
      <xs:enumeration value="IRI"/>
    </xs:restriction>
  </xs:simpleType>

[["sql:pg-type-id(\"int\")"],[23]]
[["sql:pg-type-id(\"unsignedInt\")"],[23]]
[["sql:pg-type-id(\"long\")"],[20]]
[["sql:pg-type-id(\"unsignedLong\")"],[20]]
[["sql:pg-type-id(\"float\")"],[700]]
[["sql:pg-type-id(\"double\")"],[701]]
[["sql:pg-type-id(\"decimal\")"],[1700]]
[["sql:pg-type-id(\"dateTime\")"],[1184]]
[["sql:pg-type-id(\"time\")"],[1266]]
[["sql:pg-type-id(\"date\")"],[1082]]
[["sql:pg-type-id(\"gYearMonth\")"],[0]]
[["sql:pg-type-id(\"gYear\")"],[21]]
[["sql:pg-type-id(\"gMonth\")"],[0]]
[["sql:pg-type-id(\"gDay\")"],[0]]
[["sql:pg-type-id(\"yearMonthDuration\")"],[0]]
[["sql:pg-type-id(\"dayTimeDuration\")"],[0]]
[["sql:pg-type-id(\"string\")"],[1043]]
[["sql:pg-type-id(\"anyURI\")"],[25]]
[["sql:pg-type-id(\"point\")"],[600]]
[["sql:pg-type-id(\"longLatPoint\")"],[0]]
[["sql:pg-type-id(\"boolean\")"],[16]]
[["sql:pg-type-id(\"base64Binary\")"],[0]]
[["sql:pg-type-id(\"byte\")"],[0]]
[["sql:pg-type-id(\"duration\")"],[0]]
[["sql:pg-type-id(\"gMonthDay\")"],[0]]
[["sql:pg-type-id(\"hexBinary\")"],[0]]
[["sql:pg-type-id(\"integer\")"],[0]]
[["sql:pg-type-id(\"negativeInteger\")"],[0]]
[["sql:pg-type-id(\"nonNegativeInteger\")"],[0]]
[["sql:pg-type-id(\"nonPositiveInteger\")"],[0]]
[["sql:pg-type-id(\"positiveInteger\")"],[0]]
[["sql:pg-type-id(\"short\")"],[0]]
[["sql:pg-type-id(\"unsignedByte\")"],[0]]
[["sql:pg-type-id(\"unsignedShort\")"],[0]]
[["sql:pg-type-id(\"IRI\")"],[0]]

[["sql:pg-type-size(\"int\")"],[4]]
[["sql:pg-type-size(\"unsignedInt\")"],[4]]
[["sql:pg-type-size(\"long\")"],[8]]
[["sql:pg-type-size(\"unsignedLong\")"],[8]]
[["sql:pg-type-size(\"float\")"],[4]]
[["sql:pg-type-size(\"double\")"],[8]]
[["sql:pg-type-size(\"decimal\")"],[4]]
[["sql:pg-type-size(\"dateTime\")"],[4]]
[["sql:pg-type-size(\"time\")"],[4]]
[["sql:pg-type-size(\"date\")"],[4]]
[["sql:pg-type-size(\"gYearMonth\")"],[-1]]
[["sql:pg-type-size(\"gYear\")"],[2]]
[["sql:pg-type-size(\"gMonth\")"],[-1]]
[["sql:pg-type-size(\"gDay\")"],[-1]]
[["sql:pg-type-size(\"yearMonthDuration\")"],[-1]]
[["sql:pg-type-size(\"dayTimeDuration\")"],[-1]]
[["sql:pg-type-size(\"string\")"],[-1]]
[["sql:pg-type-size(\"anyURI\")"],[-1]]
[["sql:pg-type-size(\"point\")"],[4]]
[["sql:pg-type-size(\"longLatPoint\")"],[-1]]
[["sql:pg-type-size(\"boolean\")"],[4]]
[["sql:pg-type-size(\"base64Binary\")"],[-1]]
[["sql:pg-type-size(\"byte\")"],[-1]]
[["sql:pg-type-size(\"duration\")"],[-1]]
[["sql:pg-type-size(\"gMonthDay\")"],[-1]]
[["sql:pg-type-size(\"hexBinary\")"],[-1]]
[["sql:pg-type-size(\"integer\")"],[-1]]
[["sql:pg-type-size(\"negativeInteger\")"],[-1]]
[["sql:pg-type-size(\"nonNegativeInteger\")"],[-1]]
[["sql:pg-type-size(\"nonPositiveInteger\")"],[-1]]
[["sql:pg-type-size(\"positiveInteger\")"],[-1]]
[["sql:pg-type-size(\"short\")"],[-1]]
[["sql:pg-type-size(\"unsignedByte\")"],[-1]]
[["sql:pg-type-size(\"unsignedShort\")"],[-1]]
[["sql:pg-type-size(\"IRI\")"],[-1]]
