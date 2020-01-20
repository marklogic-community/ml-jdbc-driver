// %JAVA_HOME%\bin\javac Test.java
// %JAVA_HOME%\bin\java  -cp .;\MarkLogic\GITHub\ml-jdbc-driver\build\libs\mljdbc-jre7-nobegin-1.1.jar Test

import java.sql.*;
public class Test {
    public static void main (String[] args) {
        try {
            Class myClass = Class.forName("com.marklogic.Driver");
            System.out.println("Before getConnection");
            DriverManager.setLogStream(System.out);
            Connection conn = DriverManager.getConnection("jdbc:marklogic://localhost:8077/?loggerLevel=DEBUG","admin","admin");
            System.out.println("Connected successfully");
            // JAVA SQL code

            conn.setAutoCommit(false);

			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs;
			int columnsNumber;
			ResultSetMetaData rsmd;

			rs = md.getSchemas();
			rsmd = rs.getMetaData();
			columnsNumber = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					if (i > 1) System.out.print(",  ");
					String columnValue = rs.getString(i);
					System.out.print(columnValue + " " + rsmd.getColumnName(i));
				}
				System.out.println("");
			}
			rs = md.getTables(null, null, "%", null);
			rsmd = rs.getMetaData();
			columnsNumber = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					if (i > 1) System.out.print(",  ");
					String columnValue = rs.getString(i);
					System.out.print(columnValue + " " + rsmd.getColumnName(i));
				}
				System.out.println("");
			}
			rs = md.getColumns(null, null, null, null);
			rsmd = rs.getMetaData();
			columnsNumber = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					if (i > 1) System.out.print(",  ");
					String columnValue = rs.getString(i);
					System.out.print(columnValue + " " + rsmd.getColumnName(i));
				}
				System.out.println("");
			}

            Statement stmt = conn.createStatement();
            String sql = "SELECT SCHEMA, NAME FROM SYS_TABLES";
            System.out.println("Before executeQuery statement...");
            rs = stmt.executeQuery(sql);
            System.out.println("Extract data from ResultSet");
            while(rs.next()){
                String schema = rs.getString("SCHEMA");
                String name = rs.getString("NAME");

                System.out.print("SCHEMA: " + schema);
                System.out.print(", NAME: " + name);
                System.out.print("\n");
            }
            rs.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
