/**
 * This JavaFX skeleton is provided for the Software Laboratory 5 course. Its structure
 * should provide a general guideline for the students.
 * As suggested by the JavaFX model, we'll have a GUI (view),
 * a controller class (this one) and a model.
 */

package application;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.MapValueFactory;
import javafx.collections.ObservableList;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

// Controller class
public class View {

	private Controller controller;

	@FXML
	private ComboBox<String> comboSample;

	// Layouts
	@FXML
	private VBox rootLayout;
	@FXML
	private HBox connectionLayout;

	// Texts
	@FXML
	private TextField usernameField;
	@FXML
	private TextField passwordField;
	@FXML
	private TextField searchTextField;
	@FXML
	private TextField cegnevTFID;
	@FXML
	private TextField megjegyzesTFID;
	@FXML
	private TextField arfolyamTFID;
	@FXML
	private TextField befektetoidTFID;
	@FXML
	private TextField nevertekTFID;
	@FXML
	private TextField kibocsatasTFID;
	@FXML
	private TextField rtidTFID;
	@FXML
	private TextArea logTextArea;

	// Buttons
	@FXML
	private Button connectButton;
	@FXML
	private Button commitButton;
	@FXML
	private Button updateButton;
	@FXML
	private Button statisticsButton;
	@FXML
	private Button searchButton;

	// Labels
	@FXML
	private Label connectionStateLabel;

	// Tabs
	@FXML
	private Tab editTab;
	@FXML
	private Tab statisticsTab;
	@FXML
	private Tab logTab;
	@FXML
	private Tab searchTab;

	// Tables
	@FXML
	private TableView searchTable;
	@FXML
	private TableView statisticsTable;

	// Titles and map keys of table columns search
	String searchColumnTitles[] = new String[] { "BEFEKTETO ", "RESZVENY", "MENNYISEG", "EGYSEGAR" };
	String searchColumnKeys[] = new String[] { "befekteto", "reszveny", "mennyiseg", "egysegar" };

	// Titles and map keys of table columns statistics
	String statisticsColumnTitles[] = new String[] {"RTID" ,"CEGNEV", "ATLAGOS HAVI TRANZAKCIOSZAM" };
	String statisticsColumnKeys[] = new String[] { "rtid","cegnev", "tszam" };

	// Titles and map keys of table columns statistics
	String updateColumnKeys[] = new String[] { "cegnev", "kibocsatas", "nevertek", "arfolyam", "megjegyzes", "rtid",
			"befektetoid" };

	/**
	 * View constructor
	 */
	public View() {
		controller = new Controller();
	}

	/**
	 * View initialization, it will be called after view was prepared
	 */
	@FXML
	public void initialize() {

		// Clear username and password textfields and display status
		// 'disconnected'
		usernameField.setText("");
		passwordField.setText("");

		connectionStateLabel.setText("Connection: disconnected");
		connectionStateLabel.setTextFill(Color.web("#ee0000"));

		// Create table (search table) columns
		for (int i = 0; i < searchColumnTitles.length; i++) {
			// Create table column
			TableColumn<Map, String> column = new TableColumn<>(searchColumnTitles[i]);
			// Set map factory
			column.setCellValueFactory(new MapValueFactory(searchColumnKeys[i]));
			// Set width of table column
			column.prefWidthProperty().bind(searchTable.widthProperty().divide(4));
			// Add column to the table
			searchTable.getColumns().add(column);
		}

		// Create table (statistics table) columns
		for (int i = 0; i < statisticsColumnTitles.length; i++) {
			// Create table column
			TableColumn<Map, String> column = new TableColumn<>(statisticsColumnTitles[i]);
			// Set map factory
			column.setCellValueFactory(new MapValueFactory(statisticsColumnKeys[i]));
			// Set width of table column
			column.prefWidthProperty().bind(statisticsTable.widthProperty().divide(3));
			// Add column to the table
			statisticsTable.getColumns().add(column);
		}

	}

	/**
	 * Initialize controller with data from AppMain (now only sets stage)
	 *
	 * @param stage
	 *            The top level JavaFX container
	 */
	public void initData(Stage stage) {

		// Set 'onClose' event handler (of the container)
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent winEvent) {
				// Task 4.2
				List<String> log = new ArrayList<>();
				controller.commit(log);
			}
		});
	}

	/**
	 * This is called whenever the connect button is pressed
	 *
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void connectEventHandler(ActionEvent event) {
		// Log container
		List<String> log = new ArrayList<>();

		// Controller connect method will do everything for us, just call
		// it
		if (controller.connect(usernameField.getText(), passwordField.getText(), log)) {
			connectionStateLabel.setText("Connection created");
			connectionStateLabel.setTextFill(Color.web("#009900"));
		}

		// Write log to gui
		for (String string : log)
			logMsg(string);
	}

	/**
	 * This is called whenever the search button is pressed Task 1 USE
	 * controller search method
	 * 
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void searchEventHandler(ActionEvent event) {
		// always use log
		List<String> log = new ArrayList<>();

		// Get a reference to the row list of search table
		ObservableList<Map> allRows = searchTable.getItems();

		// Delete all the rows
		allRows.clear();

		//call search
		List<String[]> result = controller.search(searchTextField.getText(), log);
		
		if (result != null && !result.isEmpty()) {
			for (int j = 0; j < result.size(); j++) {
				// Create a map object from string array
				Map<String, String> dataRow = new HashMap<>();
				for (int i = 0; i < searchTable.getColumns().size(); i++) {
					
					dataRow.put(searchColumnKeys[i], result.get(j)[i]);

				}
				// Add the row to the table

				allRows.add(dataRow);
			}
		}

		// and write it to gui
		for (String string : log)
			logMsg(string);
	}

	/**
	 * This is called whenever the edit button is pressed Task 2,3,4 USE
	 * controller modify method (verify data in controller !!!)
	 * 
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void editEventHandler(ActionEvent event) {
		List<String> log = new ArrayList<>();
		// task 2,3,4
		// creating map for input data
		Map<String, String> dataRow = new HashMap<>();
		// reading texfields into the map
		dataRow.put(updateColumnKeys[0], cegnevTFID.getText());
		dataRow.put(updateColumnKeys[1], kibocsatasTFID.getText());
		dataRow.put(updateColumnKeys[2], nevertekTFID.getText());
		dataRow.put(updateColumnKeys[3], arfolyamTFID.getText());
		dataRow.put(updateColumnKeys[4], megjegyzesTFID.getText());
		dataRow.put(updateColumnKeys[5], rtidTFID.getText());
		dataRow.put(updateColumnKeys[6], befektetoidTFID.getText());

		// calling modifydata, if combobox was y then with autocommit
		if (comboSample.getValue().equals("Y"))
			controller.modifyData(dataRow, true, log);
		else
			controller.modifyData(dataRow, false, log);
		// write log
		for (String string : log)
			logMsg(string);
	}

	/**
	 * This is called whenever the commit button is pressed Task 4 USE
	 * controller commit method Don't forget SET the commit button disable state
	 * LOG: commit ok: if commit return true commit failed: if commit return
	 * false
	 * 
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void commitEventHandler(ActionEvent event) {
		List<String> log = new ArrayList<>();
		// task 4
		// calling commit if button pressed
		controller.commit(log);
		for (String string : log)
			logMsg(string);
	}

	/**
	 * This is called whenever the statistics button is pressed Task 5 USE
	 * controller getStatistics method
	 * 
	 * @param event
	 *            Contains details about the JavaFX event
	 */
	@FXML
	private void statisticsEventHandler(ActionEvent event) {
		List<String> log = new ArrayList<>();
		// task 5
		// Get a reference to the row list of statistic table
		ObservableList<Map> allRows = statisticsTable.getItems();

		// Delete all the rows
		allRows.clear();

		// get results from the controller
		List<String[]> result = controller.getStatistics(log);

		if (result != null && !result.isEmpty()) {

			for (int j = 0; j < result.size(); j++) {
				// Create a map object
				Map<String, String> dataRow = new HashMap<>();
				for (int i = 0; i < statisticsTable.getColumns().size(); i++) {
					// puting the actual column value into the datarow
					dataRow.put(statisticsColumnKeys[i], result.get(j)[i]);

				}
				// Add the row to the table

				allRows.add(dataRow);
			}
		}
		for (String string : log)
			logMsg(string);
	}

	/**
	 * Appends the message (with a line break added) to the log
	 *
	 * @param message
	 *            The message to be logged
	 */
	protected void logMsg(String message) {

		logTextArea.appendText(message + "\n");

	}

}
