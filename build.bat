rem Be careful of build order and static final values

set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_144"

rem cd C:\MarkLogic\LACare\jdbc
rem cd org\postgresql\jdbc
rem cd C:\MarkLogic\GITHub\ml-jdbc-driver

rem Copy baseline jar for updates
copy postgresql-42.1.4.jar mljdbc-42.1.4.jar

rem preferQueryMode=simple
cd org\postgresql
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\mljdbc-42.1.4.jar  PGProperty.java
cd ..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\PGProperty.class

rem getStringFunctions, getDateFunctions, getNumericFunctions, getSystemFunctions
cd org\postgresql\jdbc
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\..\mljdbc-42.1.4.jar  EscapedFunctions.java
cd ..\..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\jdbc\EscapedFunctions.class

rem DriverInfo DRIVER_NAME, DRIVER_SHORT_NAME
cd org\postgresql\util
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\..\mljdbc-42.1.4.jar  DriverInfo.java
cd ..\..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\util\DriverInfo.class

rem       setReadOnly(true); does not work
cd org\postgresql\jdbc
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\..\mljdbc-42.1.4.jar  PgConnection.java
cd ..\..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\jdbc\PgConnection.class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\jdbc\PgConnection$1.class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\jdbc\PgConnection$AbortCommand.class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\jdbc\PgConnection$TransactionCommandHandler.class



rem cd org\postgresql\jdbc
rem rem compile modification class
rem %JAVA_HOME%\bin\javac -cp ..\..\..\mljdbc-42.1.4.jar  TypeInfoCache.java
rem cd ..\..\..
rem rem update jar with modified class
rem %JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\jdbc\TypeInfoCache.class

rem DatabaseMetaData getSchemas, getTables, getColumns
cd org\postgresql\jdbc
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\..\mljdbc-42.1.4.jar  PgDatabaseMetaData.java
cd ..\..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\jdbc\PgDatabaseMetaData.class

rem datetimetz from ISO-8601
cd org\postgresql\jdbc
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\..\mljdbc-42.1.4.jar  TimestampUtils.java
cd ..\..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\jdbc\TimestampUtils.class


rem ResultSetMetaData fetchFieldMetaData
cd org\postgresql\jdbc
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\..\mljdbc-42.1.4.jar  PgResultSetMetaData.java
cd ..\..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\jdbc\PgResultSetMetaData.class


rem Driver jdbc:marklogic:, connection thread name
cd com\marklogic
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\mljdbc-42.1.4.jar  Driver.java
cd ..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar com\marklogic\Driver$ConnectThread.class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar com\marklogic\Driver$1.class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar com\marklogic\Driver.class


rem remove the unwanted classs
rem 7z d mljdbc-42.1.4.jar org\postgresql\Driver.class



rem MD5 Digest 
cd org\postgresql\util
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\..\mljdbc-42.1.4.jar  MD5Digest.java
cd ..\..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\util\MD5Digest.class


cd org\postgresql\core\v3
rem compile modification class
%JAVA_HOME%\bin\javac -cp ..\..\..\..\mljdbc-42.1.4.jar  ConnectionFactoryImpl.java
cd ..\..\..\..
rem update jar with modified class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\core\v3\ConnectionFactoryImpl$UnsupportedProtocolException.class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\core\v3\ConnectionFactoryImpl$1.class
%JAVA_HOME%\bin\jar -uf mljdbc-42.1.4.jar org\postgresql\core\v3\ConnectionFactoryImpl.class

