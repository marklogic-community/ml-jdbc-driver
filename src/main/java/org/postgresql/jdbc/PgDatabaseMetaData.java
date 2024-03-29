/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.postgresql.jdbc;

import org.postgresql.core.BaseStatement;
import org.postgresql.core.Field;
import org.postgresql.core.Oid;
import org.postgresql.core.ServerVersion;
import org.postgresql.util.GT;
import org.postgresql.util.JdbcBlackHole;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class PgDatabaseMetaData implements DatabaseMetaData {

  public PgDatabaseMetaData(PgConnection conn) {
    this.connection = conn;
  }

/*
  private static final String keywords = "abort,acl,add,aggregate,append,archive,"
      + "arch_store,backward,binary,boolean,change,cluster,"
      + "copy,database,delimiter,delimiters,do,extend,"
      + "explain,forward,heavy,index,inherits,isnull,"
      + "light,listen,load,merge,nothing,notify,"
      + "notnull,oids,purge,rename,replace,retrieve,"
      + "returns,rule,recipe,setof,stdin,stdout,store,"
      + "vacuum,verbose,version";
*/

  private static final String keywords =
  "absolute,action,actor,add,after,alias"
+ ",all,allocate,alter"
+ ",and,any,are"
+ ",as,asc,assertion"
+ ",async,at"
+ ",attributes"
+ ",authorization,avg"
+ ",before,begin,between,bit,bit_length"
+ ",boolean,both,breadth,by"
+ ",cascade,cascaded,case,cast"
+ ",catalog"
+ ",char,character"
+ ",char_length,character_length,check,class,close,coalesce"
+ ",collate"
+ ",collation,column,commit,completion"
+ ",connect,connection,constraint"
+ ",constraints,constructor,continue,convert,corresponding"
+ ",count"
+ ",create,cross,current,current_date"
+ ",current_path"
+ ",current_time"
+ ",current_timestamp,current_user,cursor,cycle"
+ ",data,date,day,deallocate"
+ ",dec,decimal,declare,default"
+ ",deferrable,deferred,delete,depth"
+ ",desc,describe"
+ ",descriptor"
+ ",designator"
+ ",destroy,destructor,dictionary"
+ ",diagnostics,disconnect,distinct,domain"
+ ",double,drop"
+ ",each"
+ ",element"
+ ",else"
+ ",end,end-exec,equals"
+ ",escape,except"
+ ",exec,execute,exists,external,extract"
+ ",factor"
+ ",false,fetch,first,float,for,foreign"
+ ",found,from,full"
+ ",function"
+ ",general,get,global,go,goto"
+ ",grant,group"
+ ",having,hour"
+ ",identity,ignore,immediate"
+ ",in,indicator"
+ ",initially,inner,inout"
+ ",input,insensitive,insert"
+ ",instead"
+ ",int,integer,intersect,interval"
+ ",into,is,isolation"
+ ",join"
+ ",key"
+ ",language,last,leading,left"
+ ",less,level,like,limit"
+ ",list"
+ ",local,lower"
+ ",match,max,min,minute,modify,module"
+ ",month"
+ ",move,multiset"
+ ",names,national,natural,nchar,new"
+ ",new_table"
+ ",next,no"
+ ",none,not,null,nullif,numeric"
+ ",octet_length,of,off"
+ ",oid,old"
+ ",old_table"
+ ",on,only,open,operation"
+ ",operator"
+ ",operators"
+ ",option,or,order"
+ ",out,outer,output,overlaps"
+ ",pad,parameters,partial"
+ ",path"
+ ",pendant,position,postfix,precision,prefix"
+ ",preorder,prepare,preserve,primary,prior,private"
+ ",privileges,procedure,protected,public"
+ ",read,real,recursive"
+ ",references,referencing,relative,representation"
+ ",restrict,revoke,right"
+ ",role,rollback"
+ ",routine"
+ ",row,rows"
+ ",savepoint,schema,scroll,search,second,section"
+ ",select"
+ ",sensitive"
+ ",sequence"
+ ",session,session_user,set"
+ ",similar,size,smallint,some,space,specific"
+ ",sql,sqlcode"
+ ",sqlerror,sqlexception,sqlstate,sqlwarning"
+ ",start,state"
+ ",structure"
+ ",substring,sum"
+ ",symbol"
+ ",system_user"
+ ",table,template,temporary"
+ ",term"
+ ",test,than"
+ ",then,there,time,timestamp"
+ ",timezone_hour"
+ ",timezone_minute,to,trailing,transaction"
+ ",translate,translation"
+ ",trigger,trim,true,type"
+ ",under,union,unique,unknown"
+ ",update,upper,usage,user,using"
+ ",value,values,varchar,variable,variant"
+ ",varying,view"
+ ",virtual,visible"
+ ",wait,when,whenever,where"
+ ",with,without"
+ ",work,write"
+ ",year"
+ ",zone";

  protected final PgConnection connection; // The connection association

  private int NAMEDATALEN = 0; // length for name datatype
  private int INDEX_MAX_KEYS = 0; // maximum number of keys in an index.

  protected int getMaxIndexKeys() throws SQLException {
    if (INDEX_MAX_KEYS == 0) {
      String sql;
//      sql = "SELECT setting FROM pg_catalog.pg_settings WHERE name='max_index_keys'";
      sql = "SHOW max_index_keys";

      Statement stmt = connection.createStatement();
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sql);
        if (!rs.next()) {
          stmt.close();
          throw new PSQLException(
              GT.tr(
                  "Unable to determine a value for MaxIndexKeys due to missing system catalog data."),
              PSQLState.UNEXPECTED_ERROR);
        }
        INDEX_MAX_KEYS = rs.getInt(1);
      } finally {
        JdbcBlackHole.close(rs);
        JdbcBlackHole.close(stmt);
      }
    }
    return INDEX_MAX_KEYS;
  }

  protected int getMaxNameLength() throws SQLException {
    if (NAMEDATALEN == 0) {
      String sql;
//      sql = "SELECT t.typlen FROM pg_catalog.pg_type t, pg_catalog.pg_namespace n "
//            + "WHERE t.typnamespace=n.oid AND t.typname='name' AND n.nspname='pg_catalog'";
      sql = "SHOW max_identifier_length";

      Statement stmt = connection.createStatement();
      ResultSet rs = null;
      try {
        rs = stmt.executeQuery(sql);
        if (!rs.next()) {
          throw new PSQLException(GT.tr("Unable to find name datatype in the system catalogs."),
              PSQLState.UNEXPECTED_ERROR);
        }
//        NAMEDATALEN = rs.getInt("typlen");
        NAMEDATALEN = rs.getInt(1);
      } finally {
        JdbcBlackHole.close(rs);
        JdbcBlackHole.close(stmt);
      }
    }
    return NAMEDATALEN - 1;
  }


  public boolean allProceduresAreCallable() throws SQLException {
//    return true; // For now...
    return false; // For now...
  }

  public boolean allTablesAreSelectable() throws SQLException {
    return true; // For now...
  }

  public String getURL() throws SQLException {
    return connection.getURL();
  }

  public String getUserName() throws SQLException {
    return connection.getUserName();
  }

  public boolean isReadOnly() throws SQLException {
    return connection.isReadOnly();
  }

// depends on collations
  public boolean nullsAreSortedHigh() throws SQLException {
    return true;
  }

  public boolean nullsAreSortedLow() throws SQLException {
    return false;
  }

  public boolean nullsAreSortedAtStart() throws SQLException {
    return false;
  }

  public boolean nullsAreSortedAtEnd() throws SQLException {
    return false;
  }

  /**
   * Retrieves the name of this database product. We hope that it is PostgreSQL, so we return that
   * explicitly.
   *
   * @return "PostgreSQL"
   */
  @Override
  public String getDatabaseProductName() throws SQLException {
//    return "PostgreSQL";
    return "MarkLogic";
  }

  @Override
  public String getDatabaseProductVersion() throws SQLException {
    return connection.getDBVersionNumber();
  }

  @Override
  public String getDriverName() {
    return org.postgresql.util.DriverInfo.DRIVER_NAME;
  }

  @Override
  public String getDriverVersion() {
    return org.postgresql.util.DriverInfo.DRIVER_VERSION;
  }

  @Override
  public int getDriverMajorVersion() {
    return org.postgresql.util.DriverInfo.MAJOR_VERSION;
  }

  @Override
  public int getDriverMinorVersion() {
    return org.postgresql.util.DriverInfo.MINOR_VERSION;
  }

  /**
   * Does the database store tables in a local file? No - it stores them in a file on the server.
   *
   * @return true if so
   * @throws SQLException if a database access error occurs
   */
  public boolean usesLocalFiles() throws SQLException {
    return false;
  }

  /**
   * Does the database use a file for each table? Well, not really, since it doesn't use local files.
   *
   * @return true if so
   * @throws SQLException if a database access error occurs
   */
  public boolean usesLocalFilePerTable() throws SQLException {
    return false;
  }

  /**
   * Does the database treat mixed case unquoted SQL identifiers as case sensitive and as a result
   * store them in mixed case? A JDBC-Compliant driver will always return false.
   *
   * @return true if so
   * @throws SQLException if a database access error occurs
   */
  public boolean supportsMixedCaseIdentifiers() throws SQLException {
    return false;
  }

  public boolean storesUpperCaseIdentifiers() throws SQLException {
    return false;
  }

  public boolean storesLowerCaseIdentifiers() throws SQLException {
    return true;
  }

  public boolean storesMixedCaseIdentifiers() throws SQLException {
    return false;
  }

  /**
   * Does the database treat mixed case quoted SQL identifiers as case sensitive and as a result
   * store them in mixed case? A JDBC compliant driver will always return true.
   *
   * @return true if so
   * @throws SQLException if a database access error occurs
   */
  public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
    return true;
  }

  public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
    return false;
  }

  public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
    return false;
  }

  public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
    return false;
  }

  /**
   * What is the string used to quote SQL identifiers? This returns a space if identifier quoting
   * isn't supported. A JDBC Compliant driver will always use a double quote character.
   *
   * @return the quoting string
   * @throws SQLException if a database access error occurs
   */
  public String getIdentifierQuoteString() throws SQLException {
    return "\"";
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * Within PostgreSQL, the keywords are found in src/backend/parser/keywords.c
   *
   * <p>
   * For SQL Keywords, I took the list provided at
   * <a href="http://web.dementia.org/~shadow/sql/sql3bnf.sep93.txt"> http://web.dementia.org/~
   * shadow/sql/sql3bnf.sep93.txt</a> which is for SQL3, not SQL-92, but it is close enough for this
   * purpose.
   *
   * @return a comma separated list of keywords we use
   * @throws SQLException if a database access error occurs
   */
  public String getSQLKeywords() throws SQLException {
    return keywords;
  }

  public String getNumericFunctions() throws SQLException {
    return EscapedFunctions.ABS
        + ',' + EscapedFunctions.ACOS
        + ',' + EscapedFunctions.ASIN
        + ',' + EscapedFunctions.ATAN
        + ',' + EscapedFunctions.ATAN2
        + ',' + EscapedFunctions.CEILING
        + ',' + EscapedFunctions.COS
        + ',' + EscapedFunctions.COT
        + ',' + EscapedFunctions.DEGREES
        + ',' + EscapedFunctions.EXP
        + ',' + EscapedFunctions.FLOOR
        + ',' + EscapedFunctions.LOG
        + ',' + EscapedFunctions.LOG10
        + ',' + EscapedFunctions.MOD
        + ',' + EscapedFunctions.PI
//        + ',' + EscapedFunctions.MAX + ',' + EscapedFunctions.MIN
        + ',' + EscapedFunctions.POWER
        + ',' + EscapedFunctions.RADIANS
        + ',' + EscapedFunctions.ROUND
        + ',' + EscapedFunctions.SIGN
        + ',' + EscapedFunctions.SIN
        + ',' + EscapedFunctions.SQRT
        + ',' + EscapedFunctions.TAN
        + ',' + EscapedFunctions.TRUNCATE;

  }

  public String getStringFunctions() throws SQLException {
    String funcs = EscapedFunctions.ASCII
        + ',' + "bitlength"
        + ',' + EscapedFunctions.CHAR
        + ',' + EscapedFunctions.CHAR_LENGTH
        + ',' + EscapedFunctions.CONCAT
        + ',' + "convert"
        + ',' + "difference"
        + ',' + EscapedFunctions.INSERT
        + ',' + EscapedFunctions.LCASE
        + ',' + EscapedFunctions.LEFT
        + ',' + EscapedFunctions.LOCATE
        + ',' + "lower"
        + ',' + EscapedFunctions.LTRIM
        + ',' + "octet_length"
        + ',' + "position"
        + ',' + EscapedFunctions.REPEAT
        + ',' + EscapedFunctions.REPLACE
        + ',' + EscapedFunctions.RIGHT
        + ',' + EscapedFunctions.RTRIM
//not supported!        + ',' + EscapedFunctions.SPACE
        + ',' + EscapedFunctions.SOUNDEX
        + ',' + EscapedFunctions.SUBSTRING
//?        + ',' "SQL92 SUBSTRING extensions"
        + ',' + "translate"
        + ',' + "trim both"
        + ',' + "trim leading"
        + ',' + "trim trailing"
        + ',' + EscapedFunctions.UCASE
        + ',' + "upper";

    // Currently these don't work correctly with parameterized
    // arguments, so leave them out. They reorder the arguments
    // when rewriting the query, but no translation layer is provided,
    // so a setObject(N, obj) will not go to the correct parameter.
    // ','+EscapedFunctions.INSERT+','+EscapedFunctions.LOCATE+
    // ','+EscapedFunctions.RIGHT+

    return funcs;
  }

  public String getSystemFunctions() throws SQLException {
//    return EscapedFunctions.DATABASE + ',' + EscapedFunctions.IFNULL + ',' + EscapedFunctions.USER;
    return EscapedFunctions.IFNULL;
  }

  public String getTimeDateFunctions() throws SQLException {
    String timeDateFuncs =
        "current_date"
        + ',' + "current_time"
        + ',' + "current_timestamp"
        + ',' + EscapedFunctions.CURDATE
        + ',' + EscapedFunctions.DAYNAME
        + ',' + EscapedFunctions.DAYOFMONTH
        + ',' + EscapedFunctions.DAYOFWEEK
        + ',' + EscapedFunctions.DAYOFYEAR
        + ',' + "extract"
        + ',' + EscapedFunctions.HOUR
        + ',' + EscapedFunctions.MINUTE
        + ',' + EscapedFunctions.MONTH
        + ',' + EscapedFunctions.MONTHNAME
        + ',' + EscapedFunctions.NOW
        + ',' + EscapedFunctions.QUARTER
        + ',' + EscapedFunctions.SECOND
        + ',' + EscapedFunctions.WEEK
        + ',' + EscapedFunctions.YEAR;

    timeDateFuncs += ',' + EscapedFunctions.TIMESTAMPADD +','+EscapedFunctions.TIMESTAMPDIFF;

    return timeDateFuncs;
  }

  public String getSearchStringEscape() throws SQLException {
    // This method originally returned "\\\\" assuming that it
    // would be fed directly into pg's input parser so it would
    // need two backslashes. This isn't how it's supposed to be
    // used though. If passed as a PreparedStatement parameter
    // or fed to a DatabaseMetaData method then double backslashes
    // are incorrect. If you're feeding something directly into
    // a query you are responsible for correctly escaping it.
    // With 8.2+ this escaping is a little trickier because you
    // must know the setting of standard_conforming_strings, but
    // that's not our problem.

    return "\\";
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * Postgresql allows any high-bit character to be used in an unquoted identifier, so we can't
   * possibly list them all.
   *
   * From the file src/backend/parser/scan.l, an identifier is ident_start [A-Za-z\200-\377_]
   * ident_cont [A-Za-z\200-\377_0-9\$] identifier {ident_start}{ident_cont}*
   *
   * @return a string containing the extra characters
   * @throws SQLException if a database access error occurs
   */
  public String getExtraNameCharacters() throws SQLException {
    return "";
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 6.1+
   */
  public boolean supportsAlterTableWithAddColumn() throws SQLException {
//    return true;
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.3+
   */
  public boolean supportsAlterTableWithDropColumn() throws SQLException {
//    return true;
    return false;
  }

  public boolean supportsColumnAliasing() throws SQLException {
    return true;
  }

  public boolean nullPlusNonNullIsNull() throws SQLException {
    return true;
  }

  public boolean supportsConvert() throws SQLException {
    return true;
  }

  public boolean supportsConvert(int fromType, int toType) throws SQLException {
    return true;
  }

  public boolean supportsTableCorrelationNames() throws SQLException {
    return true;
  }

  public boolean supportsDifferentTableCorrelationNames() throws SQLException {
    return false;
  }

  public boolean supportsExpressionsInOrderBy() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 6.4+
   */
  public boolean supportsOrderByUnrelated() throws SQLException {
    return true;
  }

  public boolean supportsGroupBy() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 6.4+
   */
  public boolean supportsGroupByUnrelated() throws SQLException {
    return true;
  }

  /*
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 6.4+
   */
  public boolean supportsGroupByBeyondSelect() throws SQLException {
    return true;
  }

  /*
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.1+
   */
  public boolean supportsLikeEscapeClause() throws SQLException {
    return true;
  }

  public boolean supportsMultipleResultSets() throws SQLException {
    return true;
  }

  public boolean supportsMultipleTransactions() throws SQLException {
//    return true;
    return false;
  }

  public boolean supportsNonNullableColumns() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * This grammar is defined at:
   *
   * <p>
   * <a href="http://www.microsoft.com/msdn/sdk/platforms/doc/odbc/src/intropr.htm">http://www.
   * microsoft.com/msdn/sdk/platforms/doc/odbc/src/intropr.htm</a>
   *
   * <p>
   * In Appendix C. From this description, we seem to support the ODBC minimal (Level 0) grammar.
   *
   * @return true
   */
  public boolean supportsMinimumSQLGrammar() throws SQLException {
    return true;
  }

  /**
   * Does this driver support the Core ODBC SQL grammar. We need SQL-92 conformance for this.
   *
   * @return false
   * @throws SQLException if a database access error occurs
   */
  public boolean supportsCoreSQLGrammar() throws SQLException {
    return false;
  }

  /**
   * Does this driver support the Extended (Level 2) ODBC SQL grammar. We don't conform to the Core
   * (Level 1), so we can't conform to the Extended SQL Grammar.
   *
   * @return false
   * @throws SQLException if a database access error occurs
   */
  public boolean supportsExtendedSQLGrammar() throws SQLException {
    return false;
  }

  /**
   * Does this driver support the ANSI-92 entry level SQL grammar? All JDBC Compliant drivers must
   * return true. We currently report false until 'schema' support is added. Then this should be
   * changed to return true, since we will be mostly compliant (probably more compliant than many
   * other databases) And since this is a requirement for all JDBC drivers we need to get to the
   * point where we can return true.
   *
   * @return true if connected to PostgreSQL 7.3+
   * @throws SQLException if a database access error occurs
   */
  public boolean supportsANSI92EntryLevelSQL() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return false
   */
  public boolean supportsANSI92IntermediateSQL() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return false
   */
  public boolean supportsANSI92FullSQL() throws SQLException {
    return false;
  }

  /*
   * Is the SQL Integrity Enhancement Facility supported? Our best guess is that this means support
   * for constraints
   *
   * @return true
   *
   * @exception SQLException if a database access error occurs
   */
  public boolean supportsIntegrityEnhancementFacility() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.1+
   */
  public boolean supportsOuterJoins() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.1+
   */
  public boolean supportsFullOuterJoins() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.1+
   */
  public boolean supportsLimitedOuterJoins() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   * <p>
   * PostgreSQL doesn't have schemas, but when it does, we'll use the term "schema".
   *
   * @return {@code "schema"}
   */
  public String getSchemaTerm() throws SQLException {
    return "schema";
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code "function"}
   */
  public String getProcedureTerm() throws SQLException {
    return "function";
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code "database"}
   */
  public String getCatalogTerm() throws SQLException {
    return "database";
  }

  public boolean isCatalogAtStart() throws SQLException {
    return true;
  }

  public String getCatalogSeparator() throws SQLException {
    return ".";
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.3+
   */
  public boolean supportsSchemasInDataManipulation() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.3+
   */
  public boolean supportsSchemasInProcedureCalls() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.3+
   */
  public boolean supportsSchemasInTableDefinitions() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.3+
   */
  public boolean supportsSchemasInIndexDefinitions() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.3+
   */
  public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
    return true;
  }

  public boolean supportsCatalogsInDataManipulation() throws SQLException {
    return false;
  }

  public boolean supportsCatalogsInProcedureCalls() throws SQLException {
    return false;
  }

  public boolean supportsCatalogsInTableDefinitions() throws SQLException {
    return false;
  }

  public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
    return false;
  }

  public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
    return false;
  }

  /**
   * We support cursors for gets only it seems. I dont see a method to get a positioned delete.
   *
   * @return false
   * @throws SQLException if a database access error occurs
   */
  public boolean supportsPositionedDelete() throws SQLException {
    return false; // For now...
  }

  public boolean supportsPositionedUpdate() throws SQLException {
    return false; // For now...
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 6.5+
   */
  public boolean supportsSelectForUpdate() throws SQLException {
//    return true;
    return false;
  }

  public boolean supportsStoredProcedures() throws SQLException {
//    return true;
    return false;
  }

  public boolean supportsSubqueriesInComparisons() throws SQLException {
    return true;
  }

  public boolean supportsSubqueriesInExists() throws SQLException {
    return true;
  }

  public boolean supportsSubqueriesInIns() throws SQLException {
    return true;
  }

  public boolean supportsSubqueriesInQuantifieds() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.1+
   */
  public boolean supportsCorrelatedSubqueries() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 6.3+
   */
  public boolean supportsUnion() throws SQLException {
    return true; // since 6.3
  }

  /**
   * {@inheritDoc}
   *
   * @return true if connected to PostgreSQL 7.1+
   */
  public boolean supportsUnionAll() throws SQLException {
    return true;
  }

  /**
   * {@inheritDoc} In PostgreSQL, Cursors are only open within transactions.
   */
  public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
    return false;
  }

  public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
    return false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Can statements remain open across commits? They may, but this driver cannot guarantee that. In
   * further reflection. we are talking a Statement object here, so the answer is yes, since the
   * Statement is only a vehicle to ExecSQL()
   *
   * @return true
   */
  public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
//    return true;
    return false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Can statements remain open across rollbacks? They may, but this driver cannot guarantee that.
   * In further contemplation, we are talking a Statement object here, so the answer is yes, since
   * the Statement is only a vehicle to ExecSQL() in Connection
   *
   * @return true
   */
  public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
//    return true;
    return false;
  }

  public int getMaxCharLiteralLength() throws SQLException {
    return 0; // no limit
  }

  public int getMaxBinaryLiteralLength() throws SQLException {
    return 0; // no limit
  }

  public int getMaxColumnNameLength() throws SQLException {
    return getMaxNameLength();
  }

  public int getMaxColumnsInGroupBy() throws SQLException {
    return 0; // no limit
  }

  public int getMaxColumnsInIndex() throws SQLException {
    return getMaxIndexKeys();
  }

  public int getMaxColumnsInOrderBy() throws SQLException {
    return 0; // no limit
  }

  public int getMaxColumnsInSelect() throws SQLException {
    return 0; // no limit
  }

  /**
   * {@inheritDoc} What is the maximum number of columns in a table? From the CREATE TABLE reference
   * page...
   *
   * <p>
   * "The new class is created as a heap with no initial data. A class can have no more than 1600
   * attributes (realistically, this is limited by the fact that tuple sizes must be less than 8192
   * bytes)..."
   *
   * @return the max columns
   * @throws SQLException if a database access error occurs
   */
  public int getMaxColumnsInTable() throws SQLException {
    return 1600;
  }

  /**
   * {@inheritDoc} How many active connection can we have at a time to this database? Well, since it
   * depends on postmaster, which just does a listen() followed by an accept() and fork(), its
   * basically very high. Unless the system runs out of processes, it can be 65535 (the number of
   * aux. ports on a TCP/IP system). I will return 8192 since that is what even the largest system
   * can realistically handle,
   *
   * @return the maximum number of connections
   * @throws SQLException if a database access error occurs
   */
  public int getMaxConnections() throws SQLException {
    return 8192;
  }

  public int getMaxCursorNameLength() throws SQLException {
    return getMaxNameLength();
  }

  public int getMaxIndexLength() throws SQLException {
    return 0; // no limit (larger than an int anyway)
  }

  public int getMaxSchemaNameLength() throws SQLException {
    return getMaxNameLength();
  }

  public int getMaxProcedureNameLength() throws SQLException {
    return getMaxNameLength();
  }

  public int getMaxCatalogNameLength() throws SQLException {
    return getMaxNameLength();
  }

  public int getMaxRowSize() throws SQLException {
    return 1073741824; // 1 GB
  }

  public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
    return false;
  }

  public int getMaxStatementLength() throws SQLException {
    return 0; // actually whatever fits in size_t
  }

  public int getMaxStatements() throws SQLException {
    return 0;
  }

  public int getMaxTableNameLength() throws SQLException {
    return getMaxNameLength();
  }

  public int getMaxTablesInSelect() throws SQLException {
    return 0; // no limit
  }

  public int getMaxUserNameLength() throws SQLException {
    return getMaxNameLength();
  }

  public int getDefaultTransactionIsolation() throws SQLException {
    return Connection.TRANSACTION_READ_COMMITTED;
  }

  public boolean supportsTransactions() throws SQLException {
//    return true;
    return false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * We only support TRANSACTION_SERIALIZABLE and TRANSACTION_READ_COMMITTED before 8.0; from 8.0
   * READ_UNCOMMITTED and REPEATABLE_READ are accepted aliases for READ_COMMITTED.
   */
  public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
    switch (level) {
      case Connection.TRANSACTION_READ_UNCOMMITTED:
      case Connection.TRANSACTION_READ_COMMITTED:
      case Connection.TRANSACTION_REPEATABLE_READ:
//      case Connection.TRANSACTION_SERIALIZABLE:
        return true;
      default:
        return false;
    }
  }

  public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
//    return true;
    return false;
  }

  public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
    return false;
  }

  /**
   * Does a data definition statement within a transaction force the transaction to commit? It seems
   * to mean something like:
   *
   * <pre>
   * CREATE TABLE T (A INT);
   * INSERT INTO T (A) VALUES (2);
   * BEGIN;
   * UPDATE T SET A = A + 1;
   * CREATE TABLE X (A INT);
   * SELECT A FROM T INTO X;
   * COMMIT;
   * </pre>
   *
   * does the CREATE TABLE call cause a commit? The answer is no.
   *
   * @return true if so
   * @throws SQLException if a database access error occurs
   */
  public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
    return false;
  }

  public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
    return false;
  }

  /**
   * Turn the provided value into a valid string literal for direct inclusion into a query. This
   * includes the single quotes needed around it.
   *
   * @param s input value
   *
   * @return string literal for direct inclusion into a query
   * @throws SQLException if something wrong happens
   */
  protected String escapeQuotes(String s) throws SQLException {
    StringBuilder sb = new StringBuilder();
    if (!connection.getStandardConformingStrings()) {
      sb.append("E");
    }
    sb.append("'");
    sb.append(connection.escapeString(s));
    sb.append("'");
    return sb.toString();
  }

  public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
      throws SQLException {
    String sql;
 /*
    sql = "SELECT NULL AS PROCEDURE_CAT, n.nspname AS PROCEDURE_SCHEM, p.proname AS PROCEDURE_NAME, "
          + "NULL, NULL, NULL, d.description AS REMARKS, "
          + DatabaseMetaData.procedureReturnsResult + " AS PROCEDURE_TYPE, "
          + " p.proname || '_' || p.oid AS SPECIFIC_NAME "
          + " FROM pg_catalog.pg_namespace n, pg_catalog.pg_proc p "
          + " LEFT JOIN pg_catalog.pg_description d ON (p.oid=d.objoid) "
          + " LEFT JOIN pg_catalog.pg_class c ON (d.classoid=c.oid AND c.relname='pg_proc') "
          + " LEFT JOIN pg_catalog.pg_namespace pn ON (c.relnamespace=pn.oid AND pn.nspname='pg_catalog') "
          + " WHERE p.pronamespace=n.oid ";
    if (schemaPattern != null && !schemaPattern.isEmpty()) {
      sql += " AND n.nspname LIKE " + escapeQuotes(schemaPattern);
    }
    if (procedureNamePattern != null && !procedureNamePattern.isEmpty()) {
      sql += " AND p.proname LIKE " + escapeQuotes(procedureNamePattern);
    }
    sql += " ORDER BY PROCEDURE_SCHEM, PROCEDURE_NAME, p.oid::text ";
*/
    sql = "SELECT '' AS PROCEDURE_CAT, '' AS PROCEDURE_SCHEM, '' AS PROCEDURE_NAME, "
          + "'', '', '', '' AS REMARKS, "
          + "0 AS PROCEDURE_TYPE, "
          + "'' AS SPECIFIC_NAME LIMIT 0";

    return createMetaDataStatement().executeQuery(sql);
  }

  public ResultSet getProcedureColumns(String catalog, String schemaPattern,
      String procedureNamePattern, String columnNamePattern) throws SQLException {
    int columns = 20;

    Field[] f = new Field[columns];
    List<byte[][]> v = new ArrayList<byte[][]>(); // The new ResultSet tuple stuff

    f[0] = new Field("PROCEDURE_CAT", Oid.VARCHAR);
    f[1] = new Field("PROCEDURE_SCHEM", Oid.VARCHAR);
    f[2] = new Field("PROCEDURE_NAME", Oid.VARCHAR);
    f[3] = new Field("COLUMN_NAME", Oid.VARCHAR);
    f[4] = new Field("COLUMN_TYPE", Oid.INT2);
    f[5] = new Field("DATA_TYPE", Oid.INT2);
    f[6] = new Field("TYPE_NAME", Oid.VARCHAR);
    f[7] = new Field("PRECISION", Oid.INT4);
    f[8] = new Field("LENGTH", Oid.INT4);
    f[9] = new Field("SCALE", Oid.INT2);
    f[10] = new Field("RADIX", Oid.INT2);
    f[11] = new Field("NULLABLE", Oid.INT2);
    f[12] = new Field("REMARKS", Oid.VARCHAR);
    f[13] = new Field("COLUMN_DEF", Oid.VARCHAR);
    f[14] = new Field("SQL_DATA_TYPE", Oid.INT4);
    f[15] = new Field("SQL_DATETIME_SUB", Oid.INT4);
    f[16] = new Field("CHAR_OCTECT_LENGTH", Oid.INT4);
    f[17] = new Field("ORDINAL_POSITION", Oid.INT4);
    f[18] = new Field("IS_NULLABLE", Oid.VARCHAR);
    f[19] = new Field("SPECIFIC_NAME", Oid.VARCHAR);

    String sql;
    sql = "SELECT n.nspname,p.proname,p.prorettype,p.proargtypes, t.typtype,t.typrelid, "
          + " p.proargnames, p.proargmodes, p.proallargtypes, p.oid "
          + " FROM pg_catalog.pg_proc p, pg_catalog.pg_namespace n, pg_catalog.pg_type t "
          + " WHERE p.pronamespace=n.oid AND p.prorettype=t.oid ";
    if (schemaPattern != null && !schemaPattern.isEmpty()) {
      sql += " AND n.nspname LIKE " + escapeQuotes(schemaPattern);
    }
    if (procedureNamePattern != null && !procedureNamePattern.isEmpty()) {
      sql += " AND p.proname LIKE " + escapeQuotes(procedureNamePattern);
    }
    sql += " ORDER BY n.nspname, p.proname, p.oid::text ";

    byte[] isnullableUnknown = new byte[0];

    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    while (rs.next()) {
      byte[] schema = rs.getBytes("nspname");
      byte[] procedureName = rs.getBytes("proname");
      byte[] specificName =
                connection.encodeString(rs.getString("proname") + "_" + rs.getString("oid"));
      int returnType = (int) rs.getLong("prorettype");
      String returnTypeType = rs.getString("typtype");
      int returnTypeRelid = (int) rs.getLong("typrelid");

      String strArgTypes = rs.getString("proargtypes");
      StringTokenizer st = new StringTokenizer(strArgTypes);
      List<Long> argTypes = new ArrayList<Long>();
      while (st.hasMoreTokens()) {
        argTypes.add(Long.valueOf(st.nextToken()));
      }

      String[] argNames = null;
      Array argNamesArray = rs.getArray("proargnames");
      if (argNamesArray != null) {
        argNames = (String[]) argNamesArray.getArray();
      }

      String[] argModes = null;
      Array argModesArray = rs.getArray("proargmodes");
      if (argModesArray != null) {
        argModes = (String[]) argModesArray.getArray();
      }

      int numArgs = argTypes.size();

      Long[] allArgTypes = null;
      Array allArgTypesArray = rs.getArray("proallargtypes");
      if (allArgTypesArray != null) {
        allArgTypes = (Long[]) allArgTypesArray.getArray();
        numArgs = allArgTypes.length;
      }

      // decide if we are returning a single column result.
      if (returnTypeType.equals("b") || returnTypeType.equals("d") || returnTypeType.equals("e")
          || (returnTypeType.equals("p") && argModesArray == null)) {
        byte[][] tuple = new byte[columns][];
        tuple[0] = null;
        tuple[1] = schema;
        tuple[2] = procedureName;
        tuple[3] = connection.encodeString("returnValue");
        tuple[4] = connection
            .encodeString(Integer.toString(java.sql.DatabaseMetaData.procedureColumnReturn));
        tuple[5] = connection
            .encodeString(Integer.toString(connection.getTypeInfo().getSQLType(returnType)));
        tuple[6] = connection.encodeString(connection.getTypeInfo().getPGType(returnType));
        tuple[7] = null;
        tuple[8] = null;
        tuple[9] = null;
        tuple[10] = null;
        tuple[11] = connection
            .encodeString(Integer.toString(java.sql.DatabaseMetaData.procedureNullableUnknown));
        tuple[12] = null;
        tuple[17] = connection.encodeString(Integer.toString(0));
        tuple[18] = isnullableUnknown;
        tuple[19] = specificName;

        v.add(tuple);
      }

      // Add a row for each argument.
      for (int i = 0; i < numArgs; i++) {
        byte[][] tuple = new byte[columns][];
        tuple[0] = null;
        tuple[1] = schema;
        tuple[2] = procedureName;

        if (argNames != null) {
          tuple[3] = connection.encodeString(argNames[i]);
        } else {
          tuple[3] = connection.encodeString("$" + (i + 1));
        }

        int columnMode = DatabaseMetaData.procedureColumnIn;
        if (argModes != null && argModes[i].equals("o")) {
          columnMode = DatabaseMetaData.procedureColumnOut;
        } else if (argModes != null && argModes[i].equals("b")) {
          columnMode = DatabaseMetaData.procedureColumnInOut;
        } else if (argModes != null && argModes[i].equals("t")) {
          columnMode = DatabaseMetaData.procedureColumnReturn;
        }

        tuple[4] = connection.encodeString(Integer.toString(columnMode));

        int argOid;
        if (allArgTypes != null) {
          argOid = allArgTypes[i].intValue();
        } else {
          argOid = argTypes.get(i).intValue();
        }

        tuple[5] =
            connection.encodeString(Integer.toString(connection.getTypeInfo().getSQLType(argOid)));
        tuple[6] = connection.encodeString(connection.getTypeInfo().getPGType(argOid));
        tuple[7] = null;
        tuple[8] = null;
        tuple[9] = null;
        tuple[10] = null;
        tuple[11] =
            connection.encodeString(Integer.toString(DatabaseMetaData.procedureNullableUnknown));
        tuple[12] = null;
        tuple[17] = connection.encodeString(Integer.toString(i + 1));
        tuple[18] = isnullableUnknown;
        tuple[19] = specificName;

        v.add(tuple);
      }

      // if we are returning a multi-column result.
      if (returnTypeType.equals("c") || (returnTypeType.equals("p") && argModesArray != null)) {
        String columnsql = "SELECT a.attname,a.atttypid FROM pg_catalog.pg_attribute a "
                           + " WHERE a.attrelid = " + returnTypeRelid
                           + " AND NOT a.attisdropped AND a.attnum > 0 ORDER BY a.attnum ";
        Statement columnstmt = connection.createStatement();
        ResultSet columnrs = columnstmt.executeQuery(columnsql);
        while (columnrs.next()) {
          int columnTypeOid = (int) columnrs.getLong("atttypid");
          byte[][] tuple = new byte[columns][];
          tuple[0] = null;
          tuple[1] = schema;
          tuple[2] = procedureName;
          tuple[3] = columnrs.getBytes("attname");
          tuple[4] = connection
              .encodeString(Integer.toString(java.sql.DatabaseMetaData.procedureColumnResult));
          tuple[5] = connection
              .encodeString(Integer.toString(connection.getTypeInfo().getSQLType(columnTypeOid)));
          tuple[6] = connection.encodeString(connection.getTypeInfo().getPGType(columnTypeOid));
          tuple[7] = null;
          tuple[8] = null;
          tuple[9] = null;
          tuple[10] = null;
          tuple[11] = connection
              .encodeString(Integer.toString(java.sql.DatabaseMetaData.procedureNullableUnknown));
          tuple[12] = null;
          tuple[17] = connection.encodeString(Integer.toString(0));
          tuple[18] = isnullableUnknown;
          tuple[19] = specificName;

          v.add(tuple);
        }
        columnrs.close();
        columnstmt.close();
      }
    }
    rs.close();
    stmt.close();

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }

  @Override
  public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern,
                             String[] types) throws SQLException {
    String select;
    String orderby;
    String useSchemas = "SCHEMAS";
/*    select = "SELECT NULL AS TABLE_CAT, n.nspname AS TABLE_SCHEM, c.relname AS TABLE_NAME, "
             + " CASE n.nspname ~ '^pg_' OR n.nspname = 'information_schema' "
             + " WHEN true THEN CASE "
             + " WHEN n.nspname = 'pg_catalog' OR n.nspname = 'information_schema' THEN CASE c.relkind "
             + "  WHEN 'r' THEN 'SYSTEM TABLE' "
             + "  WHEN 'v' THEN 'SYSTEM VIEW' "
             + "  WHEN 'i' THEN 'SYSTEM INDEX' "
             + "  ELSE NULL "
             + "  END "
             + " WHEN n.nspname = 'pg_toast' THEN CASE c.relkind "
             + "  WHEN 'r' THEN 'SYSTEM TOAST TABLE' "
             + "  WHEN 'i' THEN 'SYSTEM TOAST INDEX' "
             + "  ELSE NULL "
             + "  END "
             + " ELSE CASE c.relkind "
             + "  WHEN 'r' THEN 'TEMPORARY TABLE' "
             + "  WHEN 'p' THEN 'TEMPORARY TABLE' "
             + "  WHEN 'i' THEN 'TEMPORARY INDEX' "
             + "  WHEN 'S' THEN 'TEMPORARY SEQUENCE' "
             + "  WHEN 'v' THEN 'TEMPORARY VIEW' "
             + "  ELSE NULL "
             + "  END "
             + " END "
             + " WHEN false THEN CASE c.relkind "
             + " WHEN 'r' THEN 'TABLE' "
             + " WHEN 'p' THEN 'TABLE' "
             + " WHEN 'i' THEN 'INDEX' "
             + " WHEN 'S' THEN 'SEQUENCE' "
             + " WHEN 'v' THEN 'VIEW' "
             + " WHEN 'c' THEN 'TYPE' "
             + " WHEN 'f' THEN 'FOREIGN TABLE' "
             + " WHEN 'm' THEN 'MATERIALIZED VIEW' "
             + " ELSE NULL "
             + " END "
             + " ELSE NULL "
             + " END "
             + " AS TABLE_TYPE, d.description AS REMARKS "
             + " FROM pg_catalog.pg_namespace n, pg_catalog.pg_class c "
             + " LEFT JOIN pg_catalog.pg_description d ON (c.oid = d.objoid AND d.objsubid = 0) "
             + " LEFT JOIN pg_catalog.pg_class dc ON (d.classoid=dc.oid AND dc.relname='pg_class') "
             + " LEFT JOIN pg_catalog.pg_namespace dn ON (dn.oid=dc.relnamespace AND dn.nspname='pg_catalog') "
             + " WHERE c.relnamespace = n.oid ";
*/
    select = "SELECT '' AS TABLE_CAT, schema AS TABLE_SCHEM, name AS TABLE_NAME, "
             + "CASE schema WHEN 'sys' THEN 'SYSTEM TABLE' ELSE UPPER(type) END AS TABLE_TYPE, "
             + "'' AS REMARKS "
             + " FROM sys.sys_tables "
             + " WHERE 1 ";

    if (schemaPattern != null && !schemaPattern.isEmpty()) {
/*      select += " AND n.nspname LIKE " + escapeQuotes(schemaPattern); */
      select += " AND schema LIKE " + escapeQuotes(schemaPattern);
    }
    orderby = " ORDER BY TABLE_TYPE,TABLE_SCHEM,TABLE_NAME ";

    if (tableNamePattern != null && !tableNamePattern.isEmpty()) {
/*      select += " AND c.relname LIKE " + escapeQuotes(tableNamePattern);*/
      select += " AND name LIKE " + escapeQuotes(tableNamePattern);
    }
/*
    if (types != null) {
      select += " AND (false ";
      StringBuilder orclause = new StringBuilder();
      for (String type : types) {
        Map<String, String> clauses = tableTypeClauses.get(type);
        if (clauses != null) {
          String clause = clauses.get(useSchemas);
          orclause.append(" OR ( ").append(clause).append(" ) ");
        }
      }
      select += orclause.toString() + ") ";
    }
*/
    if (types != null) {
      select += " AND (fn_false() ";
      StringBuilder orclause = new StringBuilder();
      for (String type : types) {
          orclause.append(" OR ( CASE schema WHEN 'sys' THEN 'SYSTEM TABLE' ELSE UPPER(type) END = '").append(type).append("' ) ");
      }
      select += orclause.toString() + ") ";
    }

    String sql = select + orderby;

    return createMetaDataStatement().executeQuery(sql);
  }

  private static final Map<String, Map<String, String>> tableTypeClauses;

  static {
    tableTypeClauses = new HashMap<String, Map<String, String>>();
    Map<String, String> ht = new HashMap<String, String>();
    tableTypeClauses.put("TABLE", ht);
    ht.put("SCHEMAS",
        "c.relkind IN ('r','p') AND n.nspname !~ '^pg_' AND n.nspname <> 'information_schema'");
    ht.put("NOSCHEMAS", "c.relkind IN ('r','p') AND c.relname !~ '^pg_'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("TDE", ht);
    ht.put("SCHEMAS",
        "c.relkind = 'v' AND n.nspname <> 'pg_catalog' AND n.nspname <> 'information_schema'");
    ht.put("NOSCHEMAS", "c.relkind = 'v' AND c.relname !~ '^pg_'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("QBV", ht);
    ht.put("SCHEMAS",
        "c.relkind = 'v' AND n.nspname <> 'pg_catalog' AND n.nspname <> 'information_schema'");
    ht.put("NOSCHEMAS", "c.relkind = 'v' AND c.relname !~ '^pg_'");
/*
    ht = new HashMap<String, String>();
    tableTypeClauses.put("VIEW", ht);
    ht.put("SCHEMAS",
        "c.relkind = 'v' AND n.nspname <> 'pg_catalog' AND n.nspname <> 'information_schema'");
    ht.put("NOSCHEMAS", "c.relkind = 'v' AND c.relname !~ '^pg_'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("INDEX", ht);
    ht.put("SCHEMAS",
        "c.relkind = 'i' AND n.nspname !~ '^pg_' AND n.nspname <> 'information_schema'");
    ht.put("NOSCHEMAS", "c.relkind = 'i' AND c.relname !~ '^pg_'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("SEQUENCE", ht);
    ht.put("SCHEMAS", "c.relkind = 'S'");
    ht.put("NOSCHEMAS", "c.relkind = 'S'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("TYPE", ht);
    ht.put("SCHEMAS",
        "c.relkind = 'c' AND n.nspname !~ '^pg_' AND n.nspname <> 'information_schema'");
    ht.put("NOSCHEMAS", "c.relkind = 'c' AND c.relname !~ '^pg_'");
*/
/* should this be called 'CATALOG' ? */
    ht = new HashMap<String, String>();
    tableTypeClauses.put("SYSTEM TABLE", ht);
    ht.put("SCHEMAS",
        "c.relkind = 'r' AND (n.nspname = 'pg_catalog' OR n.nspname = 'information_schema')");
    ht.put("NOSCHEMAS",
        "c.relkind = 'r' AND c.relname ~ '^pg_' AND c.relname !~ '^pg_toast_' AND c.relname !~ '^pg_temp_'");
/*
    ht = new HashMap<String, String>();
    tableTypeClauses.put("SYSTEM TOAST TABLE", ht);
    ht.put("SCHEMAS", "c.relkind = 'r' AND n.nspname = 'pg_toast'");
    ht.put("NOSCHEMAS", "c.relkind = 'r' AND c.relname ~ '^pg_toast_'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("SYSTEM TOAST INDEX", ht);
    ht.put("SCHEMAS", "c.relkind = 'i' AND n.nspname = 'pg_toast'");
    ht.put("NOSCHEMAS", "c.relkind = 'i' AND c.relname ~ '^pg_toast_'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("SYSTEM VIEW", ht);
    ht.put("SCHEMAS",
        "c.relkind = 'v' AND (n.nspname = 'pg_catalog' OR n.nspname = 'information_schema') ");
    ht.put("NOSCHEMAS", "c.relkind = 'v' AND c.relname ~ '^pg_'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("SYSTEM INDEX", ht);
    ht.put("SCHEMAS",
        "c.relkind = 'i' AND (n.nspname = 'pg_catalog' OR n.nspname = 'information_schema') ");
    ht.put("NOSCHEMAS",
        "c.relkind = 'v' AND c.relname ~ '^pg_' AND c.relname !~ '^pg_toast_' AND c.relname !~ '^pg_temp_'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("TEMPORARY TABLE", ht);
    ht.put("SCHEMAS", "c.relkind IN ('r','p') AND n.nspname ~ '^pg_temp_' ");
    ht.put("NOSCHEMAS", "c.relkind IN ('r','p') AND c.relname ~ '^pg_temp_' ");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("TEMPORARY INDEX", ht);
    ht.put("SCHEMAS", "c.relkind = 'i' AND n.nspname ~ '^pg_temp_' ");
    ht.put("NOSCHEMAS", "c.relkind = 'i' AND c.relname ~ '^pg_temp_' ");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("TEMPORARY VIEW", ht);
    ht.put("SCHEMAS", "c.relkind = 'v' AND n.nspname ~ '^pg_temp_' ");
    ht.put("NOSCHEMAS", "c.relkind = 'v' AND c.relname ~ '^pg_temp_' ");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("TEMPORARY SEQUENCE", ht);
    ht.put("SCHEMAS", "c.relkind = 'S' AND n.nspname ~ '^pg_temp_' ");
    ht.put("NOSCHEMAS", "c.relkind = 'S' AND c.relname ~ '^pg_temp_' ");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("FOREIGN TABLE", ht);
    ht.put("SCHEMAS", "c.relkind = 'f'");
    ht.put("NOSCHEMAS", "c.relkind = 'f'");
    ht = new HashMap<String, String>();
    tableTypeClauses.put("MATERIALIZED VIEW", ht);
    ht.put("SCHEMAS", "c.relkind = 'm'");
    ht.put("NOSCHEMAS", "c.relkind = 'm'");
*/
  }

  @Override
  public ResultSet getSchemas() throws SQLException {
    return getSchemas(null, null);
  }

  @Override
  public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
    String sql;
//    sql = "SELECT nspname AS TABLE_SCHEM, NULL AS TABLE_CATALOG FROM pg_catalog.pg_namespace "
//          + " WHERE nspname <> 'pg_toast' AND (nspname !~ '^pg_temp_' "
//          + " OR nspname = (pg_catalog.current_schemas(true))[1]) AND (nspname !~ '^pg_toast_temp_' "
//          + " OR nspname = replace((pg_catalog.current_schemas(true))[1], 'pg_temp_', 'pg_toast_temp_')) ";
    sql = "SELECT name AS TABLE_SCHEM, '' AS TABLE_CATALOG FROM sys.sys_schemas WHERE 1";
    if (schemaPattern != null && !schemaPattern.isEmpty()) {
      sql += " AND name LIKE " + escapeQuotes(schemaPattern);
    }
    sql += " ORDER BY TABLE_SCHEM";

    return createMetaDataStatement().executeQuery(sql);
  }

  /**
   * PostgreSQL does not support multiple catalogs from a single connection, so to reduce confusion
   * we only return the current catalog. {@inheritDoc}
   */
  @Override
  public ResultSet getCatalogs() throws SQLException {
    Field[] f = new Field[1];
    List<byte[][]> v = new ArrayList<byte[][]>();
    f[0] = new Field("TABLE_CAT", Oid.VARCHAR);
    byte[][] tuple = new byte[1][];
//    tuple[0] = connection.encodeString(connection.getCatalog());
      tuple[0] = connection.encodeString("__MarkLogic"); // Catalog name, not supported
    v.add(tuple);

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }

  @Override
  public ResultSet getTableTypes() throws SQLException {
    String[] types = tableTypeClauses.keySet().toArray(new String[0]);
    Arrays.sort(types);

    Field[] f = new Field[1];
    List<byte[][]> v = new ArrayList<byte[][]>();
    f[0] = new Field("TABLE_TYPE", Oid.VARCHAR);
    for (String type : types) {
      byte[][] tuple = new byte[1][];
      tuple[0] = connection.encodeString(type);
      v.add(tuple);
    }

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }
/*
  public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern,
                              String columnNamePattern) throws SQLException {

    int numberOfFields = 23; // JDBC4
    List<byte[][]> v = new ArrayList<byte[][]>(); // The new ResultSet tuple stuff
    Field[] f = new Field[numberOfFields]; // The field descriptors for the new ResultSet

    f[0] = new Field("TABLE_CAT", Oid.VARCHAR);
    f[1] = new Field("TABLE_SCHEM", Oid.VARCHAR);
    f[2] = new Field("TABLE_NAME", Oid.VARCHAR);
    f[3] = new Field("COLUMN_NAME", Oid.VARCHAR);
    f[4] = new Field("DATA_TYPE", Oid.INT2);
    f[5] = new Field("TYPE_NAME", Oid.VARCHAR);
    f[6] = new Field("COLUMN_SIZE", Oid.INT4);
    f[7] = new Field("BUFFER_LENGTH", Oid.VARCHAR);
    f[8] = new Field("DECIMAL_DIGITS", Oid.INT4);
    f[9] = new Field("NUM_PREC_RADIX", Oid.INT4);
    f[10] = new Field("NULLABLE", Oid.INT4);
    f[11] = new Field("REMARKS", Oid.VARCHAR);
    f[12] = new Field("COLUMN_DEF", Oid.VARCHAR);
    f[13] = new Field("SQL_DATA_TYPE", Oid.INT4);
    f[14] = new Field("SQL_DATETIME_SUB", Oid.INT4);
    f[15] = new Field("CHAR_OCTET_LENGTH", Oid.VARCHAR);
    f[16] = new Field("ORDINAL_POSITION", Oid.INT4);
    f[17] = new Field("IS_NULLABLE", Oid.VARCHAR);
    f[18] = new Field("SCOPE_CATLOG", Oid.VARCHAR);
    f[19] = new Field("SCOPE_SCHEMA", Oid.VARCHAR);
    f[20] = new Field("SCOPE_TABLE", Oid.VARCHAR);
    f[21] = new Field("SOURCE_DATA_TYPE", Oid.INT2);
    f[22] = new Field("IS_AUTOINCREMENT", Oid.VARCHAR);

    String sql;
    // a.attnum isn't decremented when preceding columns are dropped,
    // so the only way to calculate the correct column number is with
    // window functions, new in 8.4.
    //
    // We want to push as much predicate information below the window
    // function as possible (schema/table names), but must leave
    // column name outside so we correctly count the other columns.
    //
    if (connection.haveMinimumServerVersion(ServerVersion.v8_4)) {
      sql = "SELECT * FROM (";
    } else {
      sql = "";
    }

    sql += "SELECT n.nspname,c.relname,a.attname,a.atttypid,a.attnotnull "
           + "OR (t.typtype = 'd' AND t.typnotnull) AS attnotnull,a.atttypmod,a.attlen,";

    if (connection.haveMinimumServerVersion(ServerVersion.v8_4)) {
      sql += "row_number() OVER (PARTITION BY a.attrelid ORDER BY a.attnum) AS attnum, ";
    } else {
      sql += "a.attnum,";
    }

    if (connection.haveMinimumServerVersion(ServerVersion.v10)) {
      sql += "nullif(a.attidentity, '') as attidentity,";
    } else {
      sql += "null as attidentity,";
    }
    sql += "pg_catalog.pg_get_expr(def.adbin, def.adrelid) AS adsrc,dsc.description,t.typbasetype,t.typtype "
           + " FROM pg_catalog.pg_namespace n "
           + " JOIN pg_catalog.pg_class c ON (c.relnamespace = n.oid) "
           + " JOIN pg_catalog.pg_attribute a ON (a.attrelid=c.oid) "
           + " JOIN pg_catalog.pg_type t ON (a.atttypid = t.oid) "
           + " LEFT JOIN pg_catalog.pg_attrdef def ON (a.attrelid=def.adrelid AND a.attnum = def.adnum) "
           + " LEFT JOIN pg_catalog.pg_description dsc ON (c.oid=dsc.objoid AND a.attnum = dsc.objsubid) "
           + " LEFT JOIN pg_catalog.pg_class dc ON (dc.oid=dsc.classoid AND dc.relname='pg_class') "
           + " LEFT JOIN pg_catalog.pg_namespace dn ON (dc.relnamespace=dn.oid AND dn.nspname='pg_catalog') "
           + " WHERE c.relkind in ('r','p','v','f','m') and a.attnum > 0 AND NOT a.attisdropped ";

    if (schemaPattern != null && !schemaPattern.isEmpty()) {
      sql += " AND n.nspname LIKE " + escapeQuotes(schemaPattern);
    }
    if (tableNamePattern != null && !tableNamePattern.isEmpty()) {
      sql += " AND c.relname LIKE " + escapeQuotes(tableNamePattern);
    }
    if (connection.haveMinimumServerVersion(ServerVersion.v8_4)) {
      sql += ") c WHERE true ";
    }
    if (columnNamePattern != null && !columnNamePattern.isEmpty()) {
      sql += " AND attname LIKE " + escapeQuotes(columnNamePattern);
    }
    sql += " ORDER BY nspname,c.relname,attnum ";

    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    while (rs.next()) {
      byte[][] tuple = new byte[numberOfFields][];
      int typeOid = (int) rs.getLong("atttypid");
      int typeMod = rs.getInt("atttypmod");

      tuple[0] = null; // Catalog name, not supported
      tuple[1] = rs.getBytes("nspname"); // Schema
      tuple[2] = rs.getBytes("relname"); // Table name
      tuple[3] = rs.getBytes("attname"); // Column name

      String typtype = rs.getString("typtype");
      int sqlType;
      if ("c".equals(typtype)) {
        sqlType = Types.STRUCT;
      } else if ("d".equals(typtype)) {
        sqlType = Types.DISTINCT;
      } else if ("e".equals(typtype)) {
        sqlType = Types.VARCHAR;
      } else {
        sqlType = connection.getTypeInfo().getSQLType(typeOid);
      }

      tuple[4] = connection.encodeString(Integer.toString(sqlType));
      String pgType = connection.getTypeInfo().getPGType(typeOid);
      tuple[5] = connection.encodeString(pgType); // Type name
      tuple[7] = null; // Buffer length


      String defval = rs.getString("adsrc");

      if (defval != null) {
        if (pgType.equals("int4")) {
          if (defval.contains("nextval(")) {
            tuple[5] = connection.encodeString("serial"); // Type name == serial
          }
        } else if (pgType.equals("int8")) {
          if (defval.contains("nextval(")) {
            tuple[5] = connection.encodeString("bigserial"); // Type name == bigserial
          }
        }
      }
      String identity = rs.getString("attidentity");

      int decimalDigits = connection.getTypeInfo().getScale(typeOid, typeMod);
      int columnSize = connection.getTypeInfo().getPrecision(typeOid, typeMod);
      if (columnSize == 0) {
        columnSize = connection.getTypeInfo().getDisplaySize(typeOid, typeMod);
      }

      tuple[6] = connection.encodeString(Integer.toString(columnSize));
      tuple[8] = connection.encodeString(Integer.toString(decimalDigits));

      // Everything is base 10 unless we override later.
      tuple[9] = connection.encodeString("10");

      if (pgType.equals("bit") || pgType.equals("varbit")) {
        tuple[9] = connection.encodeString("2");
      }

      tuple[10] = connection.encodeString(Integer.toString(rs.getBoolean("attnotnull")
          ? java.sql.DatabaseMetaData.columnNoNulls : java.sql.DatabaseMetaData.columnNullable)); // Nullable
      tuple[11] = rs.getBytes("description"); // Description (if any)
      tuple[12] = rs.getBytes("adsrc"); // Column default
      tuple[13] = null; // sql data type (unused)
      tuple[14] = null; // sql datetime sub (unused)
      tuple[15] = tuple[6]; // char octet length
      tuple[16] = connection.encodeString(String.valueOf(rs.getInt("attnum"))); // ordinal position
      // Is nullable
      tuple[17] = connection.encodeString(rs.getBoolean("attnotnull") ? "NO" : "YES");

      int baseTypeOid = (int) rs.getLong("typbasetype");

      tuple[18] = null; // SCOPE_CATLOG
      tuple[19] = null; // SCOPE_SCHEMA
      tuple[20] = null; // SCOPE_TABLE
      tuple[21] = baseTypeOid == 0
                  ? null
                  : connection.encodeString(Integer.toString(connection.getTypeInfo().getSQLType(baseTypeOid))); // SOURCE_DATA_TYPE

      String autoinc = "NO";
      if (defval != null && defval.contains("nextval(") || identity != null) {
        autoinc = "YES";
      }
      tuple[22] = connection.encodeString(autoinc);

      v.add(tuple);
    }
    rs.close();
    stmt.close();

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }
*/
  public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern,
                              String columnNamePattern) throws SQLException {
    int numberOfFields = 23; // JDBC4
    List<byte[][]> v = new ArrayList<byte[][]>(); // The new ResultSet tuple stuff
    Field[] f = new Field[numberOfFields]; // The field descriptors for the new ResultSet

    f[0] = new Field("TABLE_CAT", Oid.VARCHAR);
    f[1] = new Field("TABLE_SCHEM", Oid.VARCHAR);
    f[2] = new Field("TABLE_NAME", Oid.VARCHAR);
    f[3] = new Field("COLUMN_NAME", Oid.VARCHAR);
    f[4] = new Field("DATA_TYPE", Oid.INT2);
    f[5] = new Field("TYPE_NAME", Oid.VARCHAR);
    f[6] = new Field("COLUMN_SIZE", Oid.INT4);
    f[7] = new Field("BUFFER_LENGTH", Oid.VARCHAR);
    f[8] = new Field("DECIMAL_DIGITS", Oid.INT4);
    f[9] = new Field("NUM_PREC_RADIX", Oid.INT4);
    f[10] = new Field("NULLABLE", Oid.INT4);
    f[11] = new Field("REMARKS", Oid.VARCHAR);
    f[12] = new Field("COLUMN_DEF", Oid.VARCHAR);
    f[13] = new Field("SQL_DATA_TYPE", Oid.INT4);
    f[14] = new Field("SQL_DATETIME_SUB", Oid.INT4);
    f[15] = new Field("CHAR_OCTET_LENGTH", Oid.VARCHAR);
    f[16] = new Field("ORDINAL_POSITION", Oid.INT4);
    f[17] = new Field("IS_NULLABLE", Oid.VARCHAR);
    f[18] = new Field("SCOPE_CATLOG", Oid.VARCHAR);
    f[19] = new Field("SCOPE_SCHEMA", Oid.VARCHAR);
    f[20] = new Field("SCOPE_TABLE", Oid.VARCHAR);
    f[21] = new Field("SOURCE_DATA_TYPE", Oid.INT2);
    f[22] = new Field("IS_AUTOINCREMENT", Oid.VARCHAR);

    String sql;
    sql = "SELECT ";
    sql += " c.schema AS TABLE_SCHEM,";
    sql += " c.table AS TABLE_NAME,";
    sql += " c.name AS COLUMN_NAME,";
    sql += " c.type AS TYPE_NAME,";
    sql += " pg_type_id(c.type) as atttypid,";
    sql += " -1 as atttypmod,";
    sql += " c.dflt_value AS COLUMN_DEF,";
    sql += " c.cid AS ORDINAL_POSITION,";
    sql += " c.notnull AS attnotnull";
    sql += " FROM sys.sys_columns AS c";
    sql += " WHERE 1";
    if (schemaPattern != null && !schemaPattern.isEmpty()) {
      sql += " AND c.schema LIKE " + escapeQuotes(schemaPattern);
    }
    if (tableNamePattern != null && !tableNamePattern.isEmpty()) {
      sql += " AND c.table LIKE " + escapeQuotes(tableNamePattern);
    }
    if (columnNamePattern != null && !columnNamePattern.isEmpty()) {
      sql += " AND c.name LIKE " + escapeQuotes(columnNamePattern);
    }
    sql += " ORDER BY c.schema, c.table, c.cid ";

    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    while (rs.next()) {
      byte[][] tuple = new byte[numberOfFields][];
      int typeOid = (int) rs.getLong("atttypid");
      int typeMod = rs.getInt("atttypmod");

      tuple[0] = null; // Catalog name, not supported
      tuple[1] = rs.getBytes("TABLE_SCHEM"); // Schema
      tuple[2] = rs.getBytes("TABLE_NAME"); // Table name
      tuple[3] = rs.getBytes("COLUMN_NAME"); // Column name
      String typtype = rs.getString("TYPE_NAME");
      // MarkLogic pg_type_id() can return 0
      if (typeOid == 0) {
		  if (typtype.equals("integer")) {
			  typeOid = 23;
		  } else if (typtype.equals("short")) {
			  typeOid = 21;
		  } else if (typtype.equals("unsignedShort")) {
			  typeOid = 21;
		  } else {
			  typeOid = 1043;
		  }
	  }
      int sqlType;

      sqlType = connection.getTypeInfo().getSQLType(typeOid);

      tuple[4] = connection.encodeString(Integer.toString(sqlType)); //DATA_TYPE as int
      String pgType = connection.getTypeInfo().getPGType(typeOid);
      tuple[5] = connection.encodeString(pgType); // Type name
//      tuple[6] = connection.encodeString(Integer.toString(connection.getTypeInfo().getDisplaySize(Oid.VARCHAR, -1))); // Column size
      tuple[7] = null; // Buffer length (unused)
      int decimalDigits = connection.getTypeInfo().getScale(typeOid, typeMod);
      int columnSize = connection.getTypeInfo().getPrecision(typeOid, typeMod);
      if (columnSize == 0) {
        columnSize = connection.getTypeInfo().getDisplaySize(typeOid, typeMod);
      }

      tuple[6] = connection.encodeString(Integer.toString(columnSize)); // pg_type_size
      tuple[8] = connection.encodeString(Integer.toString(decimalDigits));
//      int decimalDigits = connection.getTypeInfo().getScale(Oid.VARCHAR, 0);
//      tuple[8] = connection.encodeString(Integer.toString(decimalDigits));
//      tuple[8] = null;
      // Everything is base 10 unless we override later.
      tuple[9] = connection.encodeString("10");
//      tuple[10] = connection.encodeString(Integer.toString(rs.getBoolean("attnotnull")
//          ? java.sql.DatabaseMetaData.columnNoNulls : java.sql.DatabaseMetaData.columnNullable)); // Nullable
      tuple[10] = connection.encodeString(Integer.toString((rs.getInt("attnotnull") != 0)
          ? java.sql.DatabaseMetaData.columnNoNulls : java.sql.DatabaseMetaData.columnNullable)); // Nullable
      tuple[11] = null; // Description (if any)
      tuple[12] = rs.getBytes("COLUMN_DEF"); // Column default
      tuple[13] = null; // sql data type (unused)
      tuple[14] = null; // sql datetime sub (unused)
      tuple[15] = tuple[6]; // char octet length
      tuple[16] = connection.encodeString(String.valueOf(rs.getInt("ORDINAL_POSITION"))); // ordinal position
//      tuple[17] = connection.encodeString(rs.getBoolean("attnotnull") ? "NO" : "YES");       // Is nullable
      tuple[17] = connection.encodeString((rs.getInt("attnotnull") != 0) ? "NO" : "YES");       // Is nullable
      tuple[18] = null; // SCOPE_CATLOG(unused)
      tuple[19] = null; // SCOPE_SCHEMA(unused)
      tuple[20] = null; // SCOPE_TABLE(unused)
//      tuple[21] = null;// SOURCE_DATA_TYPE
      tuple[21] = connection.encodeString(Integer.toString(sqlType));// SOURCE_DATA_TYPE
      tuple[22] = connection.encodeString("NO"); //IS_AUTOINCREMENT

      v.add(tuple);
    }
    rs.close();
    stmt.close();

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }

  @Override
  public ResultSet getColumnPrivileges(String catalog, String schema, String table,
      String columnNamePattern) throws SQLException {
    Field[] f = new Field[8];
    List<byte[][]> v = new ArrayList<byte[][]>();

    f[0] = new Field("TABLE_CAT", Oid.VARCHAR);
    f[1] = new Field("TABLE_SCHEM", Oid.VARCHAR);
    f[2] = new Field("TABLE_NAME", Oid.VARCHAR);
    f[3] = new Field("COLUMN_NAME", Oid.VARCHAR);
    f[4] = new Field("GRANTOR", Oid.VARCHAR);
    f[5] = new Field("GRANTEE", Oid.VARCHAR);
    f[6] = new Field("PRIVILEGE", Oid.VARCHAR);
    f[7] = new Field("IS_GRANTABLE", Oid.VARCHAR);

    String sql;
    sql = "SELECT n.nspname,c.relname,r.rolname,c.relacl, "
          + (connection.haveMinimumServerVersion(ServerVersion.v8_4) ? "a.attacl, " : "")
          + " a.attname "
          + " FROM pg_catalog.pg_namespace n, pg_catalog.pg_class c, "
          + " pg_catalog.pg_roles r, pg_catalog.pg_attribute a "
          + " WHERE c.relnamespace = n.oid "
          + " AND c.relowner = r.oid "
          + " AND c.oid = a.attrelid "
          + " AND c.relkind = 'r' "
          + " AND a.attnum > 0 AND NOT a.attisdropped ";

    if (schema != null && !schema.isEmpty()) {
      sql += " AND n.nspname = " + escapeQuotes(schema);
    }
    if (table != null && !table.isEmpty()) {
      sql += " AND c.relname = " + escapeQuotes(table);
    }
    if (columnNamePattern != null && !columnNamePattern.isEmpty()) {
      sql += " AND a.attname LIKE " + escapeQuotes(columnNamePattern);
    }
    sql += " ORDER BY attname ";

    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    while (rs.next()) {
      byte[] schemaName = rs.getBytes("nspname");
      byte[] tableName = rs.getBytes("relname");
      byte[] column = rs.getBytes("attname");
      String owner = rs.getString("rolname");
      String relAcl = rs.getString("relacl");

      // For instance: SELECT -> user1 -> list of [grantor, grantable]
      Map<String, Map<String, List<String[]>>> permissions = parseACL(relAcl, owner);

      if (connection.haveMinimumServerVersion(ServerVersion.v8_4)) {
        String acl = rs.getString("attacl");
        Map<String, Map<String, List<String[]>>> relPermissions = parseACL(acl, owner);
        permissions.putAll(relPermissions);
      }
      String[] permNames = permissions.keySet().toArray(new String[0]);
      Arrays.sort(permNames);
      for (String permName : permNames) {
        byte[] privilege = connection.encodeString(permName);
        Map<String, List<String[]>> grantees = permissions.get(permName);
        for (Map.Entry<String, List<String[]>> userToGrantable : grantees.entrySet()) {
          List<String[]> grantor = userToGrantable.getValue();
          String grantee = userToGrantable.getKey();
          for (String[] grants : grantor) {
            String grantable = owner.equals(grantee) ? "YES" : grants[1];
            byte[][] tuple = new byte[8][];
            tuple[0] = null;
            tuple[1] = schemaName;
            tuple[2] = tableName;
            tuple[3] = column;
            tuple[4] = connection.encodeString(grants[0]);
            tuple[5] = connection.encodeString(grantee);
            tuple[6] = privilege;
            tuple[7] = connection.encodeString(grantable);
            v.add(tuple);
          }
        }
      }
    }
    rs.close();
    stmt.close();

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }

  @Override
  public ResultSet getTablePrivileges(String catalog, String schemaPattern,
      String tableNamePattern) throws SQLException {
    Field[] f = new Field[7];
    List<byte[][]> v = new ArrayList<byte[][]>();

    f[0] = new Field("TABLE_CAT", Oid.VARCHAR);
    f[1] = new Field("TABLE_SCHEM", Oid.VARCHAR);
    f[2] = new Field("TABLE_NAME", Oid.VARCHAR);
    f[3] = new Field("GRANTOR", Oid.VARCHAR);
    f[4] = new Field("GRANTEE", Oid.VARCHAR);
    f[5] = new Field("PRIVILEGE", Oid.VARCHAR);
    f[6] = new Field("IS_GRANTABLE", Oid.VARCHAR);

    String sql;
    sql = "SELECT n.nspname,c.relname,r.rolname,c.relacl "
          + " FROM pg_catalog.pg_namespace n, pg_catalog.pg_class c, pg_catalog.pg_roles r "
          + " WHERE c.relnamespace = n.oid "
          + " AND c.relowner = r.oid "
          + " AND c.relkind IN ('r','p') ";

    if (schemaPattern != null && !schemaPattern.isEmpty()) {
      sql += " AND n.nspname LIKE " + escapeQuotes(schemaPattern);
    }

    if (tableNamePattern != null && !tableNamePattern.isEmpty()) {
      sql += " AND c.relname LIKE " + escapeQuotes(tableNamePattern);
    }
    sql += " ORDER BY nspname, relname ";

    sql = "SELECT '' as nspname, '' as relname, '' as rolname, '' as relacl limit 0";

    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    while (rs.next()) {
      byte[] schema = rs.getBytes("nspname");
      byte[] table = rs.getBytes("relname");
      String owner = rs.getString("rolname");
      String acl = rs.getString("relacl");
      Map<String, Map<String, List<String[]>>> permissions = parseACL(acl, owner);
      String[] permNames = permissions.keySet().toArray(new String[0]);
      Arrays.sort(permNames);
      for (String permName : permNames) {
        byte[] privilege = connection.encodeString(permName);
        Map<String, List<String[]>> grantees = permissions.get(permName);
        for (Map.Entry<String, List<String[]>> userToGrantable : grantees.entrySet()) {
          List<String[]> grants = userToGrantable.getValue();
          String granteeUser = userToGrantable.getKey();
          for (String[] grantTuple : grants) {
            // report the owner as grantor if it's missing
            String grantor = grantTuple[0] == null ? owner : grantTuple[0];
            // owner always has grant privileges
            String grantable = owner.equals(granteeUser) ? "YES" : grantTuple[1];
            byte[][] tuple = new byte[7][];
            tuple[0] = null;
            tuple[1] = schema;
            tuple[2] = table;
            tuple[3] = connection.encodeString(grantor);
            tuple[4] = connection.encodeString(granteeUser);
            tuple[5] = privilege;
            tuple[6] = connection.encodeString(grantable);
            v.add(tuple);
          }
        }
      }
    }
    rs.close();
    stmt.close();

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }

  /**
   * Parse an String of ACLs into a List of ACLs.
   */
  private static List<String> parseACLArray(String aclString) {
    List<String> acls = new ArrayList<String>();
    if (aclString == null || aclString.isEmpty()) {
      return acls;
    }
    boolean inQuotes = false;
    // start at 1 because of leading "{"
    int beginIndex = 1;
    char prevChar = ' ';
    for (int i = beginIndex; i < aclString.length(); i++) {

      char c = aclString.charAt(i);
      if (c == '"' && prevChar != '\\') {
        inQuotes = !inQuotes;
      } else if (c == ',' && !inQuotes) {
        acls.add(aclString.substring(beginIndex, i));
        beginIndex = i + 1;
      }
      prevChar = c;
    }
    // add last element removing the trailing "}"
    acls.add(aclString.substring(beginIndex, aclString.length() - 1));

    // Strip out enclosing quotes, if any.
    for (int i = 0; i < acls.size(); i++) {
      String acl = acls.get(i);
      if (acl.startsWith("\"") && acl.endsWith("\"")) {
        acl = acl.substring(1, acl.length() - 1);
        acls.set(i, acl);
      }
    }
    return acls;
  }

  /**
   * Add the user described by the given acl to the Lists of users with the privileges described by
   * the acl.
   */
  private static void addACLPrivileges(String acl, Map<String, Map<String, List<String[]>>> privileges) {
    int equalIndex = acl.lastIndexOf("=");
    int slashIndex = acl.lastIndexOf("/");
    if (equalIndex == -1) {
      return;
    }

    String user = acl.substring(0, equalIndex);
    String grantor = null;
    if (user.isEmpty()) {
      user = "PUBLIC";
    }
    String privs;
    if (slashIndex != -1) {
      privs = acl.substring(equalIndex + 1, slashIndex);
      grantor = acl.substring(slashIndex + 1, acl.length());
    } else {
      privs = acl.substring(equalIndex + 1, acl.length());
    }

    for (int i = 0; i < privs.length(); i++) {
      char c = privs.charAt(i);
      if (c != '*') {
        String sqlpriv;
        String grantable;
        if (i < privs.length() - 1 && privs.charAt(i + 1) == '*') {
          grantable = "YES";
        } else {
          grantable = "NO";
        }
        switch (c) {
          case 'a':
            sqlpriv = "INSERT";
            break;
          case 'r':
          case 'p':
            sqlpriv = "SELECT";
            break;
          case 'w':
            sqlpriv = "UPDATE";
            break;
          case 'd':
            sqlpriv = "DELETE";
            break;
          case 'D':
            sqlpriv = "TRUNCATE";
            break;
          case 'R':
            sqlpriv = "RULE";
            break;
          case 'x':
            sqlpriv = "REFERENCES";
            break;
          case 't':
            sqlpriv = "TRIGGER";
            break;
          // the following can't be granted to a table, but
          // we'll keep them for completeness.
          case 'X':
            sqlpriv = "EXECUTE";
            break;
          case 'U':
            sqlpriv = "USAGE";
            break;
          case 'C':
            sqlpriv = "CREATE";
            break;
          case 'T':
            sqlpriv = "CREATE TEMP";
            break;
          default:
            sqlpriv = "UNKNOWN";
        }

        Map<String, List<String[]>> usersWithPermission = privileges.get(sqlpriv);
        String[] grant = {grantor, grantable};

        if (usersWithPermission == null) {
          usersWithPermission = new HashMap<String, List<String[]>>();
          List<String[]> permissionByGrantor = new ArrayList<String[]>();
          permissionByGrantor.add(grant);
          usersWithPermission.put(user, permissionByGrantor);
          privileges.put(sqlpriv, usersWithPermission);
        } else {
          List<String[]> permissionByGrantor = usersWithPermission.get(user);
          if (permissionByGrantor == null) {
            permissionByGrantor = new ArrayList<String[]>();
            permissionByGrantor.add(grant);
            usersWithPermission.put(user, permissionByGrantor);
          } else {
            permissionByGrantor.add(grant);
          }
        }
      }
    }
  }

  /**
   * Take the a String representing an array of ACLs and return a Map mapping the SQL permission
   * name to a List of usernames who have that permission.
   * For instance: {@code SELECT -> user1 -> list of [grantor, grantable]}
   *
   * @param aclArray ACL array
   * @param owner owner
   * @return a Map mapping the SQL permission name
   */
  public Map<String, Map<String, List<String[]>>> parseACL(String aclArray, String owner) {
    if (aclArray == null) {
      // arwdxt -- 8.2 Removed the separate RULE permission
      // arwdDxt -- 8.4 Added a separate TRUNCATE permission
      String perms = connection.haveMinimumServerVersion(ServerVersion.v8_4) ? "arwdDxt" : "arwdxt";

      aclArray = "{" + owner + "=" + perms + "/" + owner + "}";
    }

    List<String> acls = parseACLArray(aclArray);
    Map<String, Map<String, List<String[]>>> privileges =
        new HashMap<String, Map<String, List<String[]>>>();
    for (String acl : acls) {
      addACLPrivileges(acl, privileges);
    }
    return privileges;
  }

  public ResultSet getBestRowIdentifier(String catalog, String schema, String table,
      int scope, boolean nullable) throws SQLException {
    Field[] f = new Field[8];
    List<byte[][]> v = new ArrayList<byte[][]>(); // The new ResultSet tuple stuff

    f[0] = new Field("SCOPE", Oid.INT2);
    f[1] = new Field("COLUMN_NAME", Oid.VARCHAR);
    f[2] = new Field("DATA_TYPE", Oid.INT2);
    f[3] = new Field("TYPE_NAME", Oid.VARCHAR);
    f[4] = new Field("COLUMN_SIZE", Oid.INT4);
    f[5] = new Field("BUFFER_LENGTH", Oid.INT4);
    f[6] = new Field("DECIMAL_DIGITS", Oid.INT2);
    f[7] = new Field("PSEUDO_COLUMN", Oid.INT2);

    /*
     * At the moment this simply returns a table's primary key, if there is one. I believe other
     * unique indexes, ctid, and oid should also be considered. -KJ
     */

    String sql;
/*
    sql = "SELECT a.attname, a.atttypid, atttypmod "
          + "FROM pg_catalog.pg_class ct "
          + "  JOIN pg_catalog.pg_attribute a ON (ct.oid = a.attrelid) "
          + "  JOIN pg_catalog.pg_namespace n ON (ct.relnamespace = n.oid) "
          + "  JOIN (SELECT i.indexrelid, i.indrelid, i.indisprimary, "
          + "             information_schema._pg_expandarray(i.indkey) AS keys "
          + "        FROM pg_catalog.pg_index i) i "
          + "    ON (a.attnum = (i.keys).x AND a.attrelid = i.indrelid) "
          + "WHERE true ";

    if (schema != null && !schema.isEmpty()) {
      sql += " AND n.nspname = " + escapeQuotes(schema);
    }

    sql += " AND ct.relname = " + escapeQuotes(table)
        + " AND i.indisprimary "
        + " ORDER BY a.attnum ";
*/

    sql = "SELECT name as attname, pg_type_id(type) as atttypid, -1 as atttypmod "
        + " from sys.sys_columns "
        + " WHERE 1 ";

    if (schema != null && !schema.isEmpty()) {
      sql += " AND schema = " + escapeQuotes(schema);
    }

    sql += " AND table = " + escapeQuotes(table)
        + " ORDER BY cid";


    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    while (rs.next()) {
      byte[][] tuple = new byte[8][];
      int typeOid = (int) rs.getLong("atttypid");
      int typeMod = rs.getInt("atttypmod");
      int decimalDigits = connection.getTypeInfo().getScale(typeOid, typeMod);
      int columnSize = connection.getTypeInfo().getPrecision(typeOid, typeMod);
      if (columnSize == 0) {
        columnSize = connection.getTypeInfo().getDisplaySize(typeOid, typeMod);
      }
      tuple[0] = connection.encodeString(Integer.toString(scope));
      tuple[1] = rs.getBytes("attname");
      tuple[2] =
          connection.encodeString(Integer.toString(connection.getTypeInfo().getSQLType(typeOid)));
      tuple[3] = connection.encodeString(connection.getTypeInfo().getPGType(typeOid));
      tuple[4] = connection.encodeString(Integer.toString(columnSize));
      tuple[5] = null; // unused
      tuple[6] = connection.encodeString(Integer.toString(decimalDigits));
      tuple[7] =
          connection.encodeString(Integer.toString(java.sql.DatabaseMetaData.bestRowNotPseudo));
      v.add(tuple);
    }
    rs.close();
    stmt.close();

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }

  public ResultSet getVersionColumns(String catalog, String schema, String table)
      throws SQLException {
    Field[] f = new Field[8];
    List<byte[][]> v = new ArrayList<byte[][]>(); // The new ResultSet tuple stuff

    f[0] = new Field("SCOPE", Oid.INT2);
    f[1] = new Field("COLUMN_NAME", Oid.VARCHAR);
    f[2] = new Field("DATA_TYPE", Oid.INT2);
    f[3] = new Field("TYPE_NAME", Oid.VARCHAR);
    f[4] = new Field("COLUMN_SIZE", Oid.INT4);
    f[5] = new Field("BUFFER_LENGTH", Oid.INT4);
    f[6] = new Field("DECIMAL_DIGITS", Oid.INT2);
    f[7] = new Field("PSEUDO_COLUMN", Oid.INT2);

    byte[][] tuple = new byte[8][];

    /*
     * Postgresql does not have any column types that are automatically updated like some databases'
     * timestamp type. We can't tell what rules or triggers might be doing, so we are left with the
     * system columns that change on an update. An update may change all of the following system
     * columns: ctid, xmax, xmin, cmax, and cmin. Depending on if we are in a transaction and
     * whether we roll it back or not the only guaranteed change is to ctid. -KJ
     */

    tuple[0] = null;
    tuple[1] = connection.encodeString("ctid");
    tuple[2] =
        connection.encodeString(Integer.toString(connection.getTypeInfo().getSQLType("tid")));
    tuple[3] = connection.encodeString("tid");
    tuple[4] = null;
    tuple[5] = null;
    tuple[6] = null;
    tuple[7] =
        connection.encodeString(Integer.toString(java.sql.DatabaseMetaData.versionColumnPseudo));
    v.add(tuple);

    /*
     * Perhaps we should check that the given catalog.schema.table actually exists. -KJ
     */
    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }

  public ResultSet getPrimaryKeys(String catalog, String schema, String table)
      throws SQLException {
    String sql;
/*
    sql = "SELECT NULL AS TABLE_CAT, n.nspname AS TABLE_SCHEM, "
          + "  ct.relname AS TABLE_NAME, a.attname AS COLUMN_NAME, "
          + "  (i.keys).n AS KEY_SEQ, ci.relname AS PK_NAME "
          + "FROM pg_catalog.pg_class ct "
          + "  JOIN pg_catalog.pg_attribute a ON (ct.oid = a.attrelid) "
          + "  JOIN pg_catalog.pg_namespace n ON (ct.relnamespace = n.oid) "
          + "  JOIN (SELECT i.indexrelid, i.indrelid, i.indisprimary, "
          + "             information_schema._pg_expandarray(i.indkey) AS keys "
          + "        FROM pg_catalog.pg_index i) i "
          + "    ON (a.attnum = (i.keys).x AND a.attrelid = i.indrelid) "
          + "  JOIN pg_catalog.pg_class ci ON (ci.oid = i.indexrelid) "
          + "WHERE true ";
*/
    sql = "SELECT '' AS TABLE_CAT, schema AS TABLE_SCHEM, "
          + "  table AS TABLE_NAME, name AS COLUMN_NAME, "
          + "  0 AS KEY_SEQ, name AS PK_NAME "
          + "FROM sys.sys_columns "
          + "WHERE fn_string(pk) = '1' ";

    if (schema != null && !schema.isEmpty()) {
      sql += " AND schema = " + escapeQuotes(schema);
    }

    if (table != null && !table.isEmpty()) {
      sql += " AND table = " + escapeQuotes(table);
    }

//    sql += " AND pk "
    sql += " ORDER BY table_name, pk_name, key_seq";

    return createMetaDataStatement().executeQuery(sql);
  }

  /**
   * @param primaryCatalog primary catalog
   * @param primarySchema primary schema
   * @param primaryTable if provided will get the keys exported by this table
   * @param foreignCatalog foreign catalog
   * @param foreignSchema foreign schema
   * @param foreignTable if provided will get the keys imported by this table
   * @return ResultSet
   * @throws SQLException if something wrong happens
   */
  protected ResultSet getImportedExportedKeys(String primaryCatalog, String primarySchema,
      String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable)
          throws SQLException {

    /*
     * The addition of the pg_constraint in 7.3 table should have really helped us out here, but it
     * comes up just a bit short. - The conkey, confkey columns aren't really useful without
     * contrib/array unless we want to issues separate queries. - Unique indexes that can support
     * foreign keys are not necessarily added to pg_constraint. Also multiple unique indexes
     * covering the same keys can be created which make it difficult to determine the PK_NAME field.
     */

/*
    String sql =
        "SELECT NULL::text AS PKTABLE_CAT, pkn.nspname AS PKTABLE_SCHEM, pkc.relname AS PKTABLE_NAME, pka.attname AS PKCOLUMN_NAME, "
            + "NULL::text AS FKTABLE_CAT, fkn.nspname AS FKTABLE_SCHEM, fkc.relname AS FKTABLE_NAME, fka.attname AS FKCOLUMN_NAME, "
            + "pos.n AS KEY_SEQ, "
            + "CASE con.confupdtype "
            + " WHEN 'c' THEN " + DatabaseMetaData.importedKeyCascade
            + " WHEN 'n' THEN " + DatabaseMetaData.importedKeySetNull
            + " WHEN 'd' THEN " + DatabaseMetaData.importedKeySetDefault
            + " WHEN 'r' THEN " + DatabaseMetaData.importedKeyRestrict
            + " WHEN 'p' THEN " + DatabaseMetaData.importedKeyRestrict
            + " WHEN 'a' THEN " + DatabaseMetaData.importedKeyNoAction
            + " ELSE NULL END AS UPDATE_RULE, "
            + "CASE con.confdeltype "
            + " WHEN 'c' THEN " + DatabaseMetaData.importedKeyCascade
            + " WHEN 'n' THEN " + DatabaseMetaData.importedKeySetNull
            + " WHEN 'd' THEN " + DatabaseMetaData.importedKeySetDefault
            + " WHEN 'r' THEN " + DatabaseMetaData.importedKeyRestrict
            + " WHEN 'p' THEN " + DatabaseMetaData.importedKeyRestrict
            + " WHEN 'a' THEN " + DatabaseMetaData.importedKeyNoAction
            + " ELSE NULL END AS DELETE_RULE, "
            + "con.conname AS FK_NAME, pkic.relname AS PK_NAME, "
            + "CASE "
            + " WHEN con.condeferrable AND con.condeferred THEN "
            + DatabaseMetaData.importedKeyInitiallyDeferred
            + " WHEN con.condeferrable THEN " + DatabaseMetaData.importedKeyInitiallyImmediate
            + " ELSE " + DatabaseMetaData.importedKeyNotDeferrable
            + " END AS DEFERRABILITY "
            + " FROM "
            + " pg_catalog.pg_namespace pkn, pg_catalog.pg_class pkc, pg_catalog.pg_attribute pka, "
            + " pg_catalog.pg_namespace fkn, pg_catalog.pg_class fkc, pg_catalog.pg_attribute fka, "
            + " pg_catalog.pg_constraint con, "
            + " pg_catalog.generate_series(1, " + getMaxIndexKeys() + ") pos(n), "
            + " pg_catalog.pg_class pkic";
    // Starting in Postgres 9.0, pg_constraint was augmented with the conindid column, which
    // contains the oid of the index supporting the constraint. This makes it unnecessary to do a
    // further join on pg_depend.
    if (!connection.haveMinimumServerVersion(ServerVersion.v9_0)) {
      sql += ", pg_catalog.pg_depend dep ";
    }
    sql +=
        " WHERE pkn.oid = pkc.relnamespace AND pkc.oid = pka.attrelid AND pka.attnum = con.confkey[pos.n] AND con.confrelid = pkc.oid "
            + " AND fkn.oid = fkc.relnamespace AND fkc.oid = fka.attrelid AND fka.attnum = con.conkey[pos.n] AND con.conrelid = fkc.oid "
            + " AND con.contype = 'f' AND pkic.relkind = 'i' ";
    if (!connection.haveMinimumServerVersion(ServerVersion.v9_0)) {
      sql += " AND con.oid = dep.objid AND pkic.oid = dep.refobjid AND dep.classid = 'pg_constraint'::regclass::oid AND dep.refclassid = 'pg_class'::regclass::oid ";
    } else {
      sql += " AND pkic.oid = con.conindid ";
    }

    if (primarySchema != null && !primarySchema.isEmpty()) {
      sql += " AND pkn.nspname = " + escapeQuotes(primarySchema);
    }
    if (foreignSchema != null && !foreignSchema.isEmpty()) {
      sql += " AND fkn.nspname = " + escapeQuotes(foreignSchema);
    }
    if (primaryTable != null && !primaryTable.isEmpty()) {
      sql += " AND pkc.relname = " + escapeQuotes(primaryTable);
    }
    if (foreignTable != null && !foreignTable.isEmpty()) {
      sql += " AND fkc.relname = " + escapeQuotes(foreignTable);
    }

    if (primaryTable != null) {
      sql += " ORDER BY fkn.nspname,fkc.relname,con.conname,pos.n";
    } else {
      sql += " ORDER BY pkn.nspname,pkc.relname, con.conname,pos.n";
    }
*/
    String sql =
        "SELECT '' AS PKTABLE_CAT, '' AS PKTABLE_SCHEM, '' AS PKTABLE_NAME, '' AS PKCOLUMN_NAME, "
            + " '' AS FKTABLE_CAT, '' AS FKTABLE_SCHEM, '' AS FKTABLE_NAME, '' AS FKCOLUMN_NAME, "
            + " 0 AS KEY_SEQ, "
            + " 0 AS UPDATE_RULE, "
            + " 0 AS DELETE_RULE, "
            + " '' AS FK_NAME, '' AS PK_NAME, "
            + " 0 AS DEFERRABILITY limit 0";

    return createMetaDataStatement().executeQuery(sql);
  }

  public ResultSet getImportedKeys(String catalog, String schema, String table)
      throws SQLException {
    return getImportedExportedKeys(null, null, null, catalog, schema, table);
  }

  public ResultSet getExportedKeys(String catalog, String schema, String table)
      throws SQLException {
    return getImportedExportedKeys(catalog, schema, table, null, null, null);
  }

  public ResultSet getCrossReference(String primaryCatalog, String primarySchema,
      String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable)
          throws SQLException {
    return getImportedExportedKeys(primaryCatalog, primarySchema, primaryTable, foreignCatalog,
        foreignSchema, foreignTable);
  }

  public ResultSet getTypeInfo() throws SQLException {

    Field[] f = new Field[18];
    List<byte[][]> v = new ArrayList<byte[][]>(); // The new ResultSet tuple stuff

    f[0] = new Field("TYPE_NAME", Oid.VARCHAR);
    f[1] = new Field("DATA_TYPE", Oid.INT2);
    f[2] = new Field("PRECISION", Oid.INT4);
    f[3] = new Field("LITERAL_PREFIX", Oid.VARCHAR);
    f[4] = new Field("LITERAL_SUFFIX", Oid.VARCHAR);
    f[5] = new Field("CREATE_PARAMS", Oid.VARCHAR);
    f[6] = new Field("NULLABLE", Oid.INT2);
    f[7] = new Field("CASE_SENSITIVE", Oid.BOOL);
    f[8] = new Field("SEARCHABLE", Oid.INT2);
    f[9] = new Field("UNSIGNED_ATTRIBUTE", Oid.BOOL);
    f[10] = new Field("FIXED_PREC_SCALE", Oid.BOOL);
    f[11] = new Field("AUTO_INCREMENT", Oid.BOOL);
    f[12] = new Field("LOCAL_TYPE_NAME", Oid.VARCHAR);
    f[13] = new Field("MINIMUM_SCALE", Oid.INT2);
    f[14] = new Field("MAXIMUM_SCALE", Oid.INT2);
    f[15] = new Field("SQL_DATA_TYPE", Oid.INT4);
    f[16] = new Field("SQL_DATETIME_SUB", Oid.INT4);
    f[17] = new Field("NUM_PREC_RADIX", Oid.INT4);

    String sql;
//    sql = "SELECT t.typname,t.oid FROM pg_catalog.pg_type t"
//          + " JOIN pg_catalog.pg_namespace n ON (t.typnamespace = n.oid) "
//          + " WHERE n.nspname  != 'pg_toast'";

/*
CHAR	String
VARCHAR	String
LONGVARCHAR	String
NUMERIC	java.math.BigDecimal
DECIMAL	java.math.BigDecimal
BIT	boolean
TINYINT	byte
SMALLINT	short
INTEGER	int
BIGINT	long
REAL	float
FLOAT	double
DOUBLE	double
BINARY	byte[]
VARBINARY	byte[]
LONGVARBINARY	byte[]
DATE	java.sql.Date
TIME	java.sql.Time
TIMESTAMP	java.sql.Timestamp
*/

    sql = ""
          + "       SELECT 'bool'," + Oid.BOOL
          + " UNION SELECT 'bytea'," + Oid.BYTEA
          + " UNION SELECT 'char'," + Oid.CHAR
          + " UNION SELECT 'date'," + Oid.DATE
          + " UNION SELECT 'float4'," + Oid.FLOAT4
          + " UNION SELECT 'float8'," + Oid.FLOAT8
          + " UNION SELECT 'int2'," + Oid.INT2
          + " UNION SELECT 'int4'," + Oid.INT4
          + " UNION SELECT 'int8'," + Oid.INT8
          + " UNION SELECT 'numeric'," + Oid.NUMERIC
          + " UNION SELECT 'text'," + Oid.TEXT
          + " UNION SELECT 'time'," + Oid.TIME
          + " UNION SELECT 'timestamptz'," + Oid.TIMESTAMPTZ
          + " UNION SELECT 'uuid'," + Oid.UUID
          + " UNION SELECT 'varchar'," + Oid.VARCHAR;



    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(sql);
    // cache some results, this will keep memory usage down, and speed
    // things up a little.
    byte[] bZero = connection.encodeString("0");
    byte[] b10 = connection.encodeString("10");
    byte[] bf = connection.encodeString("f");
    byte[] bt = connection.encodeString("t");
    byte[] bliteral = connection.encodeString("'");
    byte[] bNullable =
              connection.encodeString(Integer.toString(java.sql.DatabaseMetaData.typeNullable));
    byte[] bSearchable =
              connection.encodeString(Integer.toString(java.sql.DatabaseMetaData.typeSearchable));

    while (rs.next()) {
      byte[][] tuple = new byte[18][];
      String typname = rs.getString(1);
      int typeOid = (int) rs.getLong(2);

      tuple[0] = connection.encodeString(typname);
      int sqlType = connection.getTypeInfo().getSQLType(typname);
      tuple[1] =
          connection.encodeString(Integer.toString(sqlType));
      tuple[2] = connection
          .encodeString(Integer.toString(connection.getTypeInfo().getMaximumPrecision(typeOid)));

      // Using requiresQuoting(oid) would might trigger select statements that might fail with NPE
      // if oid in question is being dropped.
      // requiresQuotingSqlType is not bulletproof, however, it solves the most visible NPE.
      if (connection.getTypeInfo().requiresQuotingSqlType(sqlType)) {
        tuple[3] = bliteral;
        tuple[4] = bliteral;
      }

      tuple[6] = bNullable; // all types can be null
      tuple[7] = connection.getTypeInfo().isCaseSensitive(typeOid) ? bt : bf;
      tuple[8] = bSearchable; // any thing can be used in the WHERE clause
      tuple[9] = connection.getTypeInfo().isSigned(typeOid) ? bf : bt;
      tuple[10] = bf; // false for now - must handle money
      tuple[11] = bf; // false - it isn't autoincrement
      tuple[13] = bZero; // min scale is zero
      // only numeric can supports a scale.
      tuple[14] = (typeOid == Oid.NUMERIC) ? connection.encodeString("1000") : bZero;

      // 12 - LOCAL_TYPE_NAME is null
      // 15 & 16 are unused so we return null
      tuple[17] = b10; // everything is base 10
      v.add(tuple);

      // add pseudo-type serial, bigserial
      if (typname.equals("int4")) {
        byte[][] tuple1 = tuple.clone();

        tuple1[0] = connection.encodeString("serial");
        tuple1[11] = bt;
        v.add(tuple1);
      } else if (typname.equals("int8")) {
        byte[][] tuple1 = tuple.clone();

        tuple1[0] = connection.encodeString("bigserial");
        tuple1[11] = bt;
        v.add(tuple1);
      }

    }
    rs.close();
    stmt.close();

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }

  public ResultSet getIndexInfo(String catalog, String schema, String tableName,
      boolean unique, boolean approximate) throws SQLException {
    /*
     * This is a complicated function because we have three possible situations: <= 7.2 no schemas,
     * single column functional index 7.3 schemas, single column functional index >= 7.4 schemas,
     * multi-column expressional index >= 8.3 supports ASC/DESC column info >= 9.0 no longer renames
     * index columns on a table column rename, so we must look at the table attribute names
     *
     * with the single column functional index we need an extra join to the table's pg_attribute
     * data to get the column the function operates on.
     */
    String sql;
/*
    if (connection.haveMinimumServerVersion(ServerVersion.v8_3)) {
      sql = "SELECT NULL AS TABLE_CAT, n.nspname AS TABLE_SCHEM, "
            + "  ct.relname AS TABLE_NAME, NOT i.indisunique AS NON_UNIQUE, "
            + "  NULL AS INDEX_QUALIFIER, ci.relname AS INDEX_NAME, "
            + "  CASE i.indisclustered "
            + "    WHEN true THEN " + java.sql.DatabaseMetaData.tableIndexClustered
            + "    ELSE CASE am.amname "
            + "      WHEN 'hash' THEN " + java.sql.DatabaseMetaData.tableIndexHashed
            + "      ELSE " + java.sql.DatabaseMetaData.tableIndexOther
            + "    END "
            + "  END AS TYPE, "
            + "  (i.keys).n AS ORDINAL_POSITION, "
            + "  trim(both '\"' from pg_catalog.pg_get_indexdef(ci.oid, (i.keys).n, false)) AS COLUMN_NAME, "
            + (connection.haveMinimumServerVersion(ServerVersion.v9_6)
               ? "  CASE am.amname "
                 + "    WHEN 'btree' THEN CASE i.indoption[(i.keys).n - 1] & 1 "
                 + "      WHEN 1 THEN 'D' "
                 + "      ELSE 'A' "
                 + "    END "
                 + "    ELSE NULL "
                 + "  END AS ASC_OR_DESC, "
               : "  CASE am.amcanorder "
                 + "    WHEN true THEN CASE i.indoption[(i.keys).n - 1] & 1 "
                 + "      WHEN 1 THEN 'D' "
                 + "      ELSE 'A' "
                 + "    END "
                 + "    ELSE NULL "
                 + "  END AS ASC_OR_DESC, ")
            + "  ci.reltuples AS CARDINALITY, "
            + "  ci.relpages AS PAGES, "
            + "  pg_catalog.pg_get_expr(i.indpred, i.indrelid) AS FILTER_CONDITION "
            + "FROM pg_catalog.pg_class ct "
            + "  JOIN pg_catalog.pg_namespace n ON (ct.relnamespace = n.oid) "
            + "  JOIN (SELECT i.indexrelid, i.indrelid, i.indoption, "
            + "          i.indisunique, i.indisclustered, i.indpred, "
            + "          i.indexprs, "
            + "          information_schema._pg_expandarray(i.indkey) AS keys "
            + "        FROM pg_catalog.pg_index i) i "
            + "    ON (ct.oid = i.indrelid) "
            + "  JOIN pg_catalog.pg_class ci ON (ci.oid = i.indexrelid) "
            + "  JOIN pg_catalog.pg_am am ON (ci.relam = am.oid) "
            + "WHERE true ";

      if (schema != null && !schema.isEmpty()) {
        sql += " AND n.nspname = " + escapeQuotes(schema);
      }
    } else {
      String select;
      String from;
      String where;

      select = "SELECT NULL AS TABLE_CAT, n.nspname AS TABLE_SCHEM, ";
      from = " FROM pg_catalog.pg_namespace n, pg_catalog.pg_class ct, pg_catalog.pg_class ci, "
             + " pg_catalog.pg_attribute a, pg_catalog.pg_am am ";
      where = " AND n.oid = ct.relnamespace ";
      from += ", pg_catalog.pg_index i ";

      if (schema != null && !schema.isEmpty()) {
        where += " AND n.nspname = " + escapeQuotes(schema);
      }

      sql = select
            + " ct.relname AS TABLE_NAME, NOT i.indisunique AS NON_UNIQUE, NULL AS INDEX_QUALIFIER, ci.relname AS INDEX_NAME, "
            + " CASE i.indisclustered "
            + " WHEN true THEN " + java.sql.DatabaseMetaData.tableIndexClustered
            + " ELSE CASE am.amname "
            + " WHEN 'hash' THEN " + java.sql.DatabaseMetaData.tableIndexHashed
            + " ELSE " + java.sql.DatabaseMetaData.tableIndexOther
            + " END "
            + " END AS TYPE, "
            + " a.attnum AS ORDINAL_POSITION, "
            + " CASE WHEN i.indexprs IS NULL THEN a.attname "
            + " ELSE pg_catalog.pg_get_indexdef(ci.oid,a.attnum,false) END AS COLUMN_NAME, "
            + " NULL AS ASC_OR_DESC, "
            + " ci.reltuples AS CARDINALITY, "
            + " ci.relpages AS PAGES, "
            + " pg_catalog.pg_get_expr(i.indpred, i.indrelid) AS FILTER_CONDITION "
            + from
            + " WHERE ct.oid=i.indrelid AND ci.oid=i.indexrelid AND a.attrelid=ci.oid AND ci.relam=am.oid "
            + where;
    }

    sql += " AND ct.relname = " + escapeQuotes(tableName);

    if (unique) {
      sql += " AND i.indisunique ";
    }
    sql += " ORDER BY NON_UNIQUE, TYPE, INDEX_NAME, ORDINAL_POSITION ";
*/
      sql = "SELECT '' AS TABLE_CAT, '' AS TABLE_SCHEM, "
            + " '' AS TABLE_NAME, 0 AS NON_UNIQUE, '' AS INDEX_QUALIFIER, '' AS INDEX_NAME, "
            + " 0 AS TYPE, "
            + " 0 AS ORDINAL_POSITION, "
            + " '' AS COLUMN_NAME, "
            + " 0 AS ASC_OR_DESC, "
            + " 0 AS CARDINALITY, "
            + " 0 AS PAGES, "
            + " '' AS FILTER_CONDITION LIMIT 0";

    return createMetaDataStatement().executeQuery(sql);
  }

  // ** JDBC 2 Extensions **

  public boolean supportsResultSetType(int type) throws SQLException {
    // The only type we don't support
    return type != ResultSet.TYPE_SCROLL_SENSITIVE;
  }


  public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
    // These combinations are not supported!
    if (type == ResultSet.TYPE_SCROLL_SENSITIVE) {
      return false;
    }

    // We do support Updateable ResultSets
    if (concurrency == ResultSet.CONCUR_UPDATABLE) {
      return true;
    }

    // Everything else we do
    return true;
  }


  /* lots of unsupported stuff... */
  public boolean ownUpdatesAreVisible(int type) throws SQLException {
    return true;
  }

  public boolean ownDeletesAreVisible(int type) throws SQLException {
    return true;
  }

  public boolean ownInsertsAreVisible(int type) throws SQLException {
    // indicates that
    return true;
  }

  public boolean othersUpdatesAreVisible(int type) throws SQLException {
    return false;
  }

  public boolean othersDeletesAreVisible(int i) throws SQLException {
    return false;
  }

  public boolean othersInsertsAreVisible(int type) throws SQLException {
    return false;
  }

  public boolean updatesAreDetected(int type) throws SQLException {
    return false;
  }

  public boolean deletesAreDetected(int i) throws SQLException {
    return false;
  }

  public boolean insertsAreDetected(int type) throws SQLException {
    return false;
  }

  public boolean supportsBatchUpdates() throws SQLException {
//    return true;
    return false;
  }

  public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern,
      int[] types) throws SQLException {
/*
    String sql = "select "
        + "null as type_cat, n.nspname as type_schem, t.typname as type_name,  null as class_name, "
        + "CASE WHEN t.typtype='c' then " + java.sql.Types.STRUCT + " else "
        + java.sql.Types.DISTINCT
        + " end as data_type, pg_catalog.obj_description(t.oid, 'pg_type')  "
        + "as remarks, CASE WHEN t.typtype = 'd' then  (select CASE";

    StringBuilder sqlwhen = new StringBuilder();
    for (Iterator<String> i = connection.getTypeInfo().getPGTypeNamesWithSQLTypes(); i.hasNext(); ) {
      String pgType = i.next();
      int sqlType = connection.getTypeInfo().getSQLType(pgType);
      sqlwhen.append(" when typname = ").append(escapeQuotes(pgType)).append(" then ").append(sqlType);
    }
    sql += sqlwhen.toString();

    sql += " else " + java.sql.Types.OTHER + " end from pg_type where oid=t.typbasetype) "
        + "else null end as base_type "
        + "from pg_catalog.pg_type t, pg_catalog.pg_namespace n where t.typnamespace = n.oid and n.nspname != 'pg_catalog' and n.nspname != 'pg_toast'";


    StringBuilder toAdd = new StringBuilder();
    if (types != null) {
      toAdd.append(" and (false ");
      for (int type : types) {
        switch (type) {
          case Types.STRUCT:
            toAdd.append(" or t.typtype = 'c'");
            break;
          case Types.DISTINCT:
            toAdd.append(" or t.typtype = 'd'");
            break;
        }
      }
      toAdd.append(" ) ");
    } else {
      toAdd.append(" and t.typtype IN ('c','d') ");
    }
    // spec says that if typeNamePattern is a fully qualified name
    // then the schema and catalog are ignored

    if (typeNamePattern != null) {
      // search for qualifier
      int firstQualifier = typeNamePattern.indexOf('.');
      int secondQualifier = typeNamePattern.lastIndexOf('.');

      if (firstQualifier != -1) {
        // if one of them is -1 they both will be
        if (firstQualifier != secondQualifier) {
          // we have a catalog.schema.typename, ignore catalog
          schemaPattern = typeNamePattern.substring(firstQualifier + 1, secondQualifier);
        } else {
          // we just have a schema.typename
          schemaPattern = typeNamePattern.substring(0, firstQualifier);
        }
        // strip out just the typeName
        typeNamePattern = typeNamePattern.substring(secondQualifier + 1);
      }
      toAdd.append(" and t.typname like ").append(escapeQuotes(typeNamePattern));
    }

    // schemaPattern may have been modified above
    if (schemaPattern != null) {
      toAdd.append(" and n.nspname like ").append(escapeQuotes(schemaPattern));
    }
    sql += toAdd.toString();
    sql += " order by data_type, type_schem, type_name";
*/

    String sql = "select '' as type_cat, '' as type_schem, '' as type_name,  '' as class_name, 0 as data_type, 0 as base_type limit 0 ";
    return createMetaDataStatement().executeQuery(sql);
  }


  @Override
  public Connection getConnection() throws SQLException {
    return connection;
  }

  protected Statement createMetaDataStatement() throws SQLException {
    return connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY);
  }

  public long getMaxLogicalLobSize() throws SQLException {
    return 0;
  }

  public boolean supportsRefCursors() throws SQLException {
    return true;
  }

  @Override
  public RowIdLifetime getRowIdLifetime() throws SQLException {
    throw org.postgresql.Driver.notImplemented(this.getClass(), "getRowIdLifetime()");
  }

  @Override
  public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
//    return true;
    return false;
  }

  @Override
  public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
    return false;
  }

  @Override
  public ResultSet getClientInfoProperties() throws SQLException {
    Field[] f = new Field[4];
    f[0] = new Field("NAME", Oid.VARCHAR);
    f[1] = new Field("MAX_LEN", Oid.INT4);
    f[2] = new Field("DEFAULT_VALUE", Oid.VARCHAR);
    f[3] = new Field("DESCRIPTION", Oid.VARCHAR);

    List<byte[][]> v = new ArrayList<byte[][]>();

    if (connection.haveMinimumServerVersion(ServerVersion.v9_0)) {
      byte[][] tuple = new byte[4][];
      tuple[0] = connection.encodeString("ApplicationName");
      tuple[1] = connection.encodeString(Integer.toString(getMaxNameLength()));
      tuple[2] = connection.encodeString("");
      tuple[3] = connection
          .encodeString("The name of the application currently utilizing the connection.");
      v.add(tuple);
    }

    return ((BaseStatement) createMetaDataStatement()).createDriverResultSet(f, v);
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isAssignableFrom(getClass());
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isAssignableFrom(getClass())) {
      return iface.cast(this);
    }
    throw new SQLException("Cannot unwrap to " + iface.getName());
  }

  public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern)
      throws SQLException {
    return getProcedures(catalog, schemaPattern, functionNamePattern);
  }

  public ResultSet getFunctionColumns(String catalog, String schemaPattern,
      String functionNamePattern, String columnNamePattern)
      throws SQLException {
    return getProcedureColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern);
  }

  public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern,
      String columnNamePattern) throws SQLException {
    throw org.postgresql.Driver.notImplemented(this.getClass(),
        "getPseudoColumns(String, String, String, String)");
  }

  public boolean generatedKeyAlwaysReturned() throws SQLException {
    return true;
  }

  public boolean supportsSavepoints() throws SQLException {
//    return true;
    return false;
  }

  public boolean supportsNamedParameters() throws SQLException {
    return false;
  }

  public boolean supportsMultipleOpenResults() throws SQLException {
    return false;
  }

  public boolean supportsGetGeneratedKeys() throws SQLException {
    // We don't support returning generated keys by column index,
    // but that should be a rarer case than the ones we do support.
    //
    return true;
  }

  public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern)
      throws SQLException {
    throw org.postgresql.Driver.notImplemented(this.getClass(),
        "getSuperTypes(String,String,String)");
  }

  public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern)
      throws SQLException {
    throw org.postgresql.Driver.notImplemented(this.getClass(),
        "getSuperTables(String,String,String,String)");
  }

  public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
      String attributeNamePattern) throws SQLException {
    throw org.postgresql.Driver.notImplemented(this.getClass(),
        "getAttributes(String,String,String,String)");
  }

  public boolean supportsResultSetHoldability(int holdability) throws SQLException {
    return true;
  }

  public int getResultSetHoldability() throws SQLException {
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
  }

  @Override
  public int getDatabaseMajorVersion() throws SQLException {
    return connection.getServerMajorVersion();
  }

  @Override
  public int getDatabaseMinorVersion() throws SQLException {
    return connection.getServerMinorVersion();
  }

  @Override
  public int getJDBCMajorVersion() {
    return org.postgresql.util.DriverInfo.JDBC_MAJOR_VERSION;
  }

  @Override
  public int getJDBCMinorVersion() {
    return org.postgresql.util.DriverInfo.JDBC_MINOR_VERSION;
  }

  public int getSQLStateType() throws SQLException {
    return sqlStateSQL;
  }

  public boolean locatorsUpdateCopy() throws SQLException {
    /*
     * Currently LOB's aren't updateable at all, so it doesn't matter what we return. We don't throw
     * the notImplemented Exception because the 1.5 JDK's CachedRowSet calls this method regardless
     * of whether large objects are used.
     */
    return true;
  }

  public boolean supportsStatementPooling() throws SQLException {
    return false;
  }
}