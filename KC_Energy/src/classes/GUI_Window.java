package classes;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUI_Window extends JFrame {
	//Window dimensions and database connection setup
	private final int WINDOW_WIDTH = 800;  // Window width in pixels
	private final int WINDOW_HEIGHT = 600;  // Window height in pixels	
	private final static String dbURL = "jdbc:mysql://localhost:3306";
	private final static String username = "root";
	private final static String password = "password";
	Connection connection;
	
	//Checks if a table by the name of tableName exists. Assists method createTables to tell if tables exist yet.
	private static String createTableExistenceStatement(String tableName) {
		return "SELECT EXISTS(\r\n"
				+ "SELECT * FROM information_schema.TABLES\r\n"
				+ "WHERE TABLE_NAME = \'" + tableName + "\');";
	}
	
	/*
	 * Tests if tables Customer, Meter, and Charge exist in the database accessed by conn. If any do not exist, runs SQL code to 
	 * create them.
	*/
	private static void createTables(Connection conn) {	
		final String tableCreationStatements[] = {"CREATE TABLE IF NOT EXISTS CUSTOMER\r\n"
				+ "(Name varchar(100) NOT NULL,\r\n"
				+ "AccountNumber int UNSIGNED NOT NULL AUTO_INCREMENT, \r\n"
				+ "PhoneNumber varchar(12) NOT NULL,\r\n"
				+ "EmailAddress varchar(100) NOT NULL,\r\n"
				+ "StreetAddress varchar(100) NOT NULL,\r\n"
				+ "City varchar(50) NOT NULL,\r\n"
				+ "State char(2),\r\n"
				+ "Country varchar(50) NOT NULL,\r\n"
				+ "ZipCode char(5),\r\n"
				+ "OutstandingBalance Decimal(12,2) NOT NULL DEFAULT 0,\r\n"
				+ "PRIMARY KEY(AccountNumber));",
				"ALTER TABLE CUSTOMER AUTO_INCREMENT = 1001;",
				"CREATE TABLE IF NOT EXISTS METER\r\n"
				+ "(MeterID int NOT NULL AUTO_INCREMENT,\r\n"
				+ "custID int UNSIGNED NOT NULL,\r\n"
				+ "FOREIGN KEY (custID) REFERENCES CUSTOMER(AccountNumber) ON DELETE CASCADE,\r\n"
				+ "EnergyType varchar(100) NOT NULL,\r\n"
				+ "EnergyUsage Decimal(7,2) NOT NULL DEFAULT 0,\r\n"
				+ "Tariff Decimal(5,2) NOT NULL,\r\n"
				+ "Balance Decimal(10,2) NOT NULL DEFAULT 0,\r\n"
				+ "PRIMARY KEY(MeterID));",
				"ALTER TABLE METER AUTO_INCREMENT = 1;",
				"CREATE TABLE IF NOT EXISTS CHARGE\r\n"
				+ "(ChargeID Int NOT NULL AUTO_INCREMENT,\r\n"
				+ "metID Int NOT NULL,\r\n"
				+ "FOREIGN KEY (metID) REFERENCES METER(MeterID) ON DELETE CASCADE,\r\n"
				+ "ChargeAmount Decimal(5,2) NOT NULL,\r\n"
				+ "AppliedTariff Decimal(5,2) NOT NULL,\r\n"
				+ "Cost Decimal(8,2) NOT NULL DEFAULT 0,\r\n"
				+ "ChargeDate Datetime NOT NULL,\r\n"
				+ "Paid Boolean NOT NULL DEFAULT false,\r\n"
				+ "PRIMARY KEY(ChargeID));",
				"ALTER TABLE CHARGE AUTO_INCREMENT = 1;"
		};
		
		//Used by createTableExistenceStatement to see if tables "Customer", "Meter, and "Charge" exist already in database.
		String tablesToBeChecked[] = {"customer", "meter", "charge"};
		int tablesFound = 0;
		
		try {
			//Creates statement via conn and uses kc_energy schema in database.
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("CREATE SCHEMA IF NOT EXISTS kc_energy;");
			stmt.executeUpdate("USE kc_energy;");
			//Checks that tables of each name in tablesToBeChecked exist. If they do, it is assumed that they are correct.
			for (int i = 0; i < tablesToBeChecked.length; i++) {
				ResultSet existence = stmt.executeQuery(createTableExistenceStatement(tablesToBeChecked[i]));
				existence.next();
				tablesFound += existence.getInt(1);
			}
			
			//If one of the tables is not found, runs all SQL code in tableCreationStatements to create tables in database.
			if (tablesFound < 3) {
				System.out.println("Not all tables present in database. Creating tables.");
				for (int i = 0; i < tableCreationStatements.length; i++) {
					stmt.executeUpdate(tableCreationStatements[i]);
				}
			}
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	GUI_Window() {
		try {
			// Create a connection to the database.
			connection = DriverManager.getConnection(dbURL, username, password);
			connection.setAutoCommit(false);
			System.out.println("Connection created to localhost.");
			
			//Checks if tables exist from connection and creates them if needed.
			createTables(connection);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		//Special listener that closes connection when window is closed.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				// Close database connection.
				try {
					connection.close();
					System.out.println("Connection closed.");
					System.exit(0);
				} catch (SQLException e) {
					System.out.println(e.getMessage());
				}
			}
		});
		
		//Sets basic properties of window and makes it non-resizable and visible.
		setBackground(new Color(244, 222, 173));
		//Creates first window: a Customer_Window with no search parameters.
		setContentPane(new Customer_Window(this, "", -1));
		setResizable(false);
		
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setVisible(true);
	}
	
	//START OF PROGRAM. Runs constructor and creates GUI_Window that is used throughout the program.
	public static void main(String[] args) {
		new GUI_Window();
	}
	
	//Quickly changes the size of the window to make sure new Containers display properly.
	public void resetContent() {
		setResizable(true);
		setSize(WINDOW_WIDTH - 1, WINDOW_HEIGHT);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setResizable(false);
	}

}

