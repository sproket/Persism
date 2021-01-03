/*=====================================================================
File: 	 SpatialDataTypes.java
Summary: This Microsoft JDBC Driver for SQL Server Sample application
         demonstrates how to use prepared statement setter methods to
		 set values for SQL Server Spatial Datatypes 'Geography' and 'Geometry'.
		 It also demonstrates how to use result set getter methods to read
		 and parse SQL Server Spatial data type values.
---------------------------------------------------------------------
This file is part of the Microsoft JDBC Driver for SQL Server Code Samples.
Copyright (C) Microsoft Corporation.  All rights reserved.
 
This source code is intended only as a supplement to Microsoft
Development Tools and/or on-line documentation.  See these other
materials for detailed information regarding Microsoft code samples.
 
THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF
ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
PARTICULAR PURPOSE.
=====================================================================*/
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.microsoft.sqlserver.jdbc.Geography;
import com.microsoft.sqlserver.jdbc.Geometry;
import com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement;
import com.microsoft.sqlserver.jdbc.SQLServerResultSet;


public class SpatialDataTypes {

	private static String tableName = "SpatialDataTypesTable_JDBC_Sample";

	public static void main(String[] args) {

		// Create a variable for the connection string.
		String connectionUrl = "jdbc:sqlserver://<server>:<port>;databaseName=<database>;user=<user>;password=<password>";
		// Establish the connection.
		try (Connection con = DriverManager.getConnection(connectionUrl);
				Statement stmt = con.createStatement();) {
			dropAndCreateTable(stmt);

			// TODO: Implement Sample code
			String geoWKT = "POINT(3 40 5 6)";
			Geometry geomWKT = Geometry.STGeomFromText(geoWKT, 0);
			Geography geogWKT = Geography.STGeomFromText(geoWKT, 4326);

			try (SQLServerPreparedStatement pstmt = (SQLServerPreparedStatement) con
					.prepareStatement(
							"insert into " + tableName + " values (?, ?)");) {
				pstmt.setGeometry(1, geomWKT);
				pstmt.setGeography(2, geogWKT);
				pstmt.execute();

				SQLServerResultSet rs = (SQLServerResultSet) stmt
						.executeQuery("select * from " + tableName);
				rs.next();

				System.out.println("Geometry data: " + rs.getGeometry(1));
				System.out.println("Geography data: " + rs.getGeography(2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void dropAndCreateTable(Statement stmt) throws SQLException {
		stmt.executeUpdate("if object_id('" + tableName + "','U') is not null"
				+ " drop table " + tableName);

		stmt.executeUpdate(
				"Create table " + tableName + " (c1 geometry, c2 geography)");
	}
}
