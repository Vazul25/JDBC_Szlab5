package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import application.Model.ModifyResult;

import java.sql.*;

public class Controller {
	// model instance, controller communicate just with the model
	// Don't use javaFX imports classes, etc.
	private Model model;

	public Controller() {
		model = new Model();
	}

	/**
	 * Connect to DB with model
	 * 
	 * @param userName
	 *            Your DB username
	 * @param password
	 *            Your DB password
	 * @param log
	 *            Log container
	 * @return true if connect success else false
	 */
	public boolean connect(String userName, String password, List<String> log) {
		if (model.connect(userName, password)) {
			// Test the connection
			String results = model.testConnection();
			if (results != null) {
				log.add("Connection seems to be working.");
				log.add("Connected to: '" + model.getDatabaseUrl() + "'");
				log.add(String.format("DBMS: %s, version: %s", model.getDatabaseProductName(),
						model.getDatabaseProductVersion()));
				log.add(results);
				return true;
			}
		}
		// always log
		log.add(model.getLastError());
		return false;
	}

	/**
	 * Task 1: Search with keyword USE: model.search Don't forget close the
	 * statement!
	 * 
	 * @param keyword
	 *            the search keyword
	 * @param log
	 *            Log container
	 * @return every row in a String[],and the whole table in List<String[]>
	 */
	public List<String[]> search(String keyword, List<String> log) {
		
		List<String[]> result = new ArrayList<>();

		ResultSet rs;
		//keyword must be a 4 digit year ex:2011
		if (keyword.matches("\\d{4}")) {
			
			rs = model.search(keyword);
			if (rs == null)
				log.add(model.lastError);

		} else {
			//if keyword isnt given, we call search with null, to list out every T
			if (keyword == null || keyword.equals("")) {

				rs = model.search(null);
				if (rs == null)
					log.add(model.lastError);

			} else {
				//if keyword wasnt null then it was in bad format
				log.add("keyword syntax error: keyword must be a 4 digit year (ex:2011)  ");
				return null;

			}

		}
		try {
			
			while (rs.next()) {

				String s[] = { rs.getString(1), rs.getString(2), Integer.toString(rs.getInt(3)),
						Integer.toString(rs.getInt(4)) };
				result.add(s);

			}

		} catch (SQLException e) {

			
			log.add("error SQLException catched in Controller.search() at Resultset.next()" + e.toString());
			e.printStackTrace();

		}

		if (result.isEmpty())
			log.add("error there wasn't any transaction in the given year  ");
		else
			log.add("Querry Sucessfull ");
		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.add("ERROR cant close resultset " + e.toString());
		}
		return result;

	}

	/**
	 * Task 2 and 3: Modify data (task 2) and (before) verify(task 3) it, and
	 * disable autocommit (task 4.1) USE: model.modifyData and
	 * Model.ModifyResult
	 * 
	 * @param data
	 *            Modify data
	 * @param AutoCommit
	 *            autocommit parameter
	 * @param log
	 *            Log container
	 * @return true if verify ok else false
	 */
	public boolean modifyData(Map data, boolean AutoCommit, List<String> log) {
		Model.ModifyResult result = Model.ModifyResult.Error;
		//Task 2,3,4.1
		if (verifyData(data, log)) {
			result = model.modifyData(data, AutoCommit);
			
			switch (result) {

			case Error:
				log.add(model.lastError);
				return false;

			case InsertOccured:
				log.add("insert occured ");
				return true;

			case UpdateOccured:

				log.add("update occured  ");
				return true;
			default:
				log.add("ERROR Unkown state, default case in modifyData  ");
				return false;

			}
		} else
			return false;

	}

	/**
	 * Task 5: get statistics USE: model.getStatistics Don't forget close the
	 * statement!
	 * 
	 * @param log
	 *            Log container
	 * @return every row in a String[],and the whole table in List<String[]>
	 */
	public List<String[]> getStatistics(List<String> log) {
		List<String[]> result = new ArrayList<>();
		// task 5
		//get ruslts from model
		ResultSet rs = model.getStatistics();
		try {
			//iterate over result set
			while (rs.next()) {
				//parse results into a string array
				String s[] = { Integer.toString(rs.getInt(1)),rs.getString(2), Double.toString(rs.getDouble(3)), };
				//add it to return
				result.add(s);

			}

		} catch (SQLException e) {

			//log error
			log.add("Error SQLException catched in Controller.search() at Resultset.next()" + e.toString());
			e.printStackTrace();

		}

		return result;
	}

	/**
	 * Commit all uncommitted changes USE: model.commit
	 * 
	 * @param log
	 *            Log container
	 * @return true if model.commit true else false
	 */
	public boolean commit(List<String> log) {
		// comit and log
		if (model.commit()) {
			log.add(model.lastError);
			return true;
		}
		log.add(model.lastError);
		return false;
	}

	/**
	 * Verify all fields value USE it to modifyData function USE regular
	 * expressions, try..catch
	 * 
	 * @param data
	 *            Modify data
	 * @param log
	 *            Log container
	 * @return true if all fields in Map is correct else false
	 */
	private boolean verifyData(Map data, List<String> log) {
		// TODO task 3
		boolean ret = true;
		try {
			if (data.get("cegnev").toString().equals("")||data.get("cegnev").toString().trim().isEmpty()) {
				ret = false;
				log.add("cegnev fieldname syntax error: cegnev must be given  ");

			}
			if (data.get("megjegyzes").toString().equals("")||data.get("megjegyzes").toString().trim().isEmpty()) {
				ret = false;
				log.add("megjegyzes fieldname syntax error: megjegyzes must be given  ");

			}
			if (!data.get("arfolyam").toString().matches("\\d+")) {
				ret = false;
				log.add("arfolyam fieldname syntax error: arfolyam must be a positive number  ");

			}
			String a = data.get("nevertek").toString();

			if (!data.get("nevertek").toString().matches("\\d+")) {
				ret = false;
				log.add(" nevertek fieldname syntax error: nevertek must be a positive number  ");
			}
			if (!data.get("kibocsatas").toString().matches("\\d{4}-[0-1]\\d-[0-3]\\d")) {
				ret = false;
				log.add("kibocsatas fieldname syntax error: kibocsatas must be a date in YYYY-MM-DD format  ");
			}
			if (!data.get("rtid").toString().matches("\\d+")) {
				ret = false;
				log.add("rtid fieldname syntax error: rtid must be a positive number");
			}
			String befektetoid = data.get("befektetoid").toString();
			if ((!befektetoid.isEmpty() && !data.get("befektetoid").toString().matches("\\d+"))) {
				ret = false;
				log.add("befektetoiD fieldname syntax error: must be a positive number ");
			}
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
			ret = false;
			log.add(" ERROR wrong regex pattern in Controller.verifyData" + e.toString());

		} catch (NullPointerException e) {
			e.printStackTrace();
			ret = false;
			log.add(" ERROR null in TEXTFIELD  " + e.toString());
		}
		return ret;
	}

}
