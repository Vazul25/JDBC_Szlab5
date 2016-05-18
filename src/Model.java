/**
 * This JavaFX skeleton is provided for the Software Laboratory 5 course. Its structure
 * should provide a general guideline for the students.
 * As suggested by the JavaFX model, we'll have a GUI (view),
 * a controller class and a model (this one).
 */

package application;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Random;

// Model class
public class Model {

	// Database driver and URL
	protected static final String driverName = "oracle.jdbc.driver.OracleDriver";
	protected static final String databaseUrl = "jdbc:oracle:thin:@rapid.eik.bme.hu:1521:szglab";

	// Product name and product version of the database
	protected String databaseProductName = null;
	protected String databaseProductVersion = null;

	// Connection object
	protected Connection connection = null;

	// Enum structure for Exercise #2
	protected enum ModifyResult {
		InsertOccured, UpdateOccured, Error
	}

	// String containing last error message
	protected String lastError = "";

	/**
	 * Model constructor
	 */
	public Model() {
	}

	/**
	 * Gives product name of the database
	 *
	 * @return Product name of the database
	 */
	public String getDatabaseProductName() {

		return databaseProductName;

	}

	/**
	 * Gives product version of the database
	 *
	 * @return Product version of the database
	 */
	public String getDatabaseProductVersion() {

		return databaseProductVersion;

	}

	/**
	 * Gives database URL
	 *
	 * @return Database URL
	 */
	public String getDatabaseUrl() {

		return databaseUrl;

	}

	/**
	 * Gives the message of last error
	 *
	 * @return Message of last error
	 */
	public String getLastError() {

		return lastError;

	}

	/**
	 * Tries to connect to the database
	 *
	 * @param userName
	 *            User who has access to the database
	 * @param password
	 *            User's password
	 * @return True on success, false on fail
	 */
	public boolean connect(String userName, String password) {

		try {

			// If connection status is disconnected
			if (connection == null || !connection.isValid(30)) {

				if (connection == null) {

					// Load the specified database driver
					Class.forName(driverName);

					// Driver is for Oracle 12cR1 (certified with JDK 7 and JDK
					// 8)
					if (java.lang.System.getProperty("java.vendor").equals("Microsoft Corp.")) {
						DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
					}
				} else {

					connection.close();

				}

				// Create new connection and get metadata
				connection = DriverManager.getConnection(databaseUrl, userName, password);
				DatabaseMetaData dbmd = connection.getMetaData();

				databaseProductName = dbmd.getDatabaseProductName();
				databaseProductVersion = dbmd.getDatabaseProductVersion();

			}

			return true;

		} catch (SQLException e) {

			// !TODO: More user friendly error handling
			// use 'error' String beginning of the error string
			lastError = "error ".concat(e.toString());
			return false;

		} catch (ClassNotFoundException e) {
			// !TODO: More user friendly error handling
			// use 'error' String beginning of the error string
			lastError = "error ".concat(e.toString());
			return false;

		}

	}

	/**
	 * Tests the database connection by submitting a query
	 *
	 * @return True on success, false on fail
	 */
	public String testConnection() {

		try {

			// Create SQL query and execute it
			// If user input has to be processed, use PreparedStatement instead!
			Statement stmt = connection.createStatement();
			ResultSet rset = stmt.executeQuery("SELECT count(*) FROM oktatas.igazolvanyok");

			// Process the results
			String result = null;
			while (rset.next()) {
				result = String.format("Total number of rows in 'Igazolvanyok' table in 'Oktatas' schema: %s",
						rset.getString(1));
			}

			// Close statement
			stmt.close();

			return result;

		} catch (SQLException e) {
			// !TODO: More user friendly error handling
			// use 'error' String beginning of the error string
			lastError = "error ".concat(e.toString());
			return null;

		}
	}

	/**
	 * Method for Exercise #1
	 * 
	 * @param Search
	 *            keyword
	 * @return Result of the query
	 */
	public ResultSet search(String keyword) {
		// task 1

		try {

			ResultSet rs = null;

			if (keyword == null) {
				// if keyword wasnt given we list out all tranzactions
				Statement searchStatement = connection.createStatement();
				rs = searchStatement.executeQuery("SELECT b.NEV,rt.CEGNEV,t.MENNYISEG,t.EGYSEGAR "
						+ "FROM Tranzakcio t inner join Reszvenytipus rt on rt.RTID=t.RTID"
						+ " inner join Befekteto b on b.BEFEKTETOID=t.BEFEKTETOID");

			} else {
				// if keyword wasa given, we list out tranzactions in a given
				// year
				// parse prepstatement
				int intKeyword = Integer.parseInt(keyword);
				PreparedStatement searchStatementWithDate = connection
						.prepareStatement("SELECT b.NEV,rt.CEGNEV,t.MENNYISEG,t.EGYSEGAR" + " FROM Tranzakcio t "
								+ "inner join Reszvenytipus rt on rt.RTID=t.RTID "
								+ "inner join Befekteto b on b.BEFEKTETOID=t.BEFEKTETOID"
								+ " where EXTRACT(YEAR FROM t.Datum)=?");

				searchStatementWithDate.setInt(1, intKeyword);
				rs = searchStatementWithDate.executeQuery();

			}
			return rs;

		} catch (SQLException e) {
			e.printStackTrace();
			lastError = " ERROR in search() querry failed".concat(e.toString());
			return null;
		}

	}

	/**
	 * Method for Exercise #2-#3
	 *
	 * @param data
	 *            New or modified data
	 * @param AutoCommit
	 *            set the connection type (use default true, and 4.1 use false
	 * @return Type of action has been performed
	 */
	public ModifyResult modifyData(Map data, boolean AutoCommit) {
		ModifyResult result = ModifyResult.Error;
		// task 2,3,4.1
		String cegnev, megjegyzes;
		int rtid, nevertek, arfolyam;
		Date kibocsatas;
		int befektetoid = 0;
		// Parse data and return error if it failed
		try {
			cegnev = data.get("cegnev").toString();
			rtid = Integer.parseInt(data.get("rtid").toString());
			nevertek = Integer.parseInt(data.get("nevertek").toString());
			arfolyam = Integer.parseInt(data.get("arfolyam").toString());
			megjegyzes = data.get("megjegyzes").toString();
			kibocsatas = Date.valueOf(data.get("kibocsatas").toString());

		} catch (Exception e) {
			e.printStackTrace();
			lastError = "error could not parse data in model.modifyData " + e.toString();
			return ModifyResult.Error;
		}

		if (!AutoCommit) {
			try {
				// Turn off autocommit
				connection.setAutoCommit(false);

			} catch (SQLException e) {
				e.printStackTrace();
				lastError = "error model.modifyData cant turn off autocommit " + e.toString();
				return ModifyResult.Error;
			}
		}
		//
		// Insert/modify reszvenytipus record
		try {
			ResultSet rs = null;
			// checking if a record with the same rtid is alredy in the table
			PreparedStatement searchIfUniqStatement = connection
					.prepareStatement("select rtid from reszvenytipus rt where rtid=?");
			searchIfUniqStatement.setInt(1, rtid);

			rs = searchIfUniqStatement.executeQuery();

			// If the result set is empty, we need to insert
			if (!rs.next()) {
				try {
					// preparing statement with params
					PreparedStatement insertStatement = connection
							.prepareStatement(" insert into reszvenytipus  values(? ,?, ?, ? ,? ,?)");
					insertStatement.setInt(1, rtid);
					insertStatement.setString(2, cegnev);
					insertStatement.setDate(3, kibocsatas);
					insertStatement.setInt(4, nevertek);
					insertStatement.setInt(5, arfolyam);
					insertStatement.setString(6, megjegyzes);

					insertStatement.execute();
					// changing return to insertoccured
					result = ModifyResult.InsertOccured;
					insertStatement.close();

				} catch (SQLException e) {
					e.printStackTrace();
					lastError = "Insert failed " + e.toString();
					return ModifyResult.Error;
				}
			} else {
				// If result set isnt empty we need to update
				PreparedStatement updateStatement = connection
						.prepareStatement("UPDATE reszvenytipus  set cegnev=?,kibocsatas=?,"
								+ "nevertek=?,arfolyam=?,megjegyzes=? where rtid=?");

				updateStatement.setString(1, cegnev);
				updateStatement.setDate(2, kibocsatas);
				updateStatement.setInt(3, nevertek);
				updateStatement.setInt(4, arfolyam);
				updateStatement.setString(5, megjegyzes);
				updateStatement.setInt(6, rtid);

				updateStatement.execute();
				result = ModifyResult.UpdateOccured;
				updateStatement.close();
			}
			rs.close();
		} catch (SQLException e) {
			// log error
			e.printStackTrace();
			lastError = "error in model.modifyData likely searchIfUniqStatement is bad querry or rs.next called on closed resultset  "
					+ e.toString();
			return ModifyResult.Error;
		}

		// Task4 if befektetoid is given, autocommit is off, and we inserted the
		// given row( not
		// updated), we insert another row into tranzakcio with given id

		if (!AutoCommit && !data.get("befektetoid").toString().isEmpty() && result == ModifyResult.InsertOccured) {
			// Insert into tranzakcio
			PreparedStatement insertTStatement;
			try {

				// parse id
				befektetoid = Integer.parseInt(data.get("befektetoid").toString());
				// get max of transaction ids to generate the next
				Statement getIndexStatement = connection.createStatement();
				ResultSet rs = getIndexStatement.executeQuery("select max(trid) from tranzakcio ");
				rs.next();
				int trid = rs.getInt(1);
				try {
					rs.close();
					getIndexStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
					lastError = "error could not close getIndexStatement " + e.toString();
					// no to big of a problem
				}
				// generate trid
				trid++;

				// insert statement
				insertTStatement = connection.prepareStatement(
						"insert into TRANZAKCIO (RTID,BEFEKTETOID,MENNYISEG,EGYSEGAR,DATUM,TRID) values(? ,?, ?, ?,SYSDATE,?  )");
				insertTStatement.setInt(1, rtid);

				insertTStatement.setInt(4, arfolyam);
				insertTStatement.setInt(2, befektetoid);
				insertTStatement.setInt(3, 1);
				insertTStatement.setInt(5, trid);

				insertTStatement.executeUpdate();
			} catch (SQLException e) {
				// rollback if error happens
				e.printStackTrace();
				lastError = "error cant insert into tranzakcio, Rollback " + e.toString();
				rollback();
				return ModifyResult.Error;
			} catch (NumberFormatException e) {
				lastError = "error befektetoid in wrong format, Rollback " + e.toString();
				rollback();
				return ModifyResult.Error;
			}

		}
		return result;

	}

	/**
	 * Method for Exercise #4
	 *
	 * @return True on success, false on fail
	 */
	public boolean commit() {
		// task 4

		try {
			if (!connection.getAutoCommit()) {
				// if auto commit was off, then we commit and set autocommit and
				// log
				connection.commit();
				connection.setAutoCommit(true);
				lastError = "Commit ok ";
			}

			return true;
		} catch (SQLException e) {

			e.printStackTrace();
			lastError = "Commit failed ";
		}
		return false;
	}

	/**
	 * Method for Exercise #4
	 */
	public void rollback() {
		// task 4
		try {
			// call rollback and log
			connection.rollback();
			lastError = "Rollback occured";
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = "Rollback failed";
		}
	}

	/**
	 * Method for Exercise #5
	 *
	 * @return Result of the query
	 */
	public ResultSet getStatistics() {
		// task 5

		ResultSet rs = null;
		// Selecting monthly statistics of tranzasctions with rich investors
		String sql = "Select rt.rtid,rt.cegnev,nvl(round(count(*)/"
				+ "Decode(round(MONTHS_BETWEEN(sysdate,MIN(t.datum))), 0, 1,MONTHS_BETWEEN(sysdate,MIN(t.datum))),4),0) "
				+ "as tranzakcioszam  FROM Tranzakcio t right join Reszvenytipus rt on t.RTID=rt.RTID "
				+ "left Outer join Befekteto b on t.BEFEKTETOID=b.BEFEKTETOID "
				+ "where nvl(b.KESZPENZ,10000000)>1000000 group by rt.rtid,rt.cegnev order by 3";
		try {
			// try to create statement and execute querry
			Statement statisticQuerry = connection.createStatement();
			rs = statisticQuerry.executeQuery(sql);

		} catch (SQLException e) {
			e.printStackTrace();
			// log error
			lastError = " ERROR in getStatistics creating statement or executing querry failed";

		}
		// return the resultset
		return rs;

	}

}
