package classes;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

public class Edit_Meter_Window extends Container {
	protected static GUI_Window frame;
	//MySQL query and data structure setup
	private final static String[] genericTypes = new String[]{"", "Gas", "Electricity", "Water"};
	private final static String insertBase = "INSERT INTO Meter(custID, EnergyType, Tariff) VALUES (%d, \'%s\', %f);";
	private final static String updateBase = "UPDATE Meter SET EnergyType = \'%s\', Tariff = %f WHERE MeterID = %d";
	protected int accessedMeter = -1;
	private ResultSet meterInfo = null;
	private Statement stmt;
	protected int cID;
	protected String cName;
	
	//GUI component setup
	private JLabel header1Label, header2Label, typeLabel, tariffLabel, warning1Label, warning2Label;
	private JComboBox<String> typeBox;
	private JTextField tariffField;
	private JButton confirmButton, backButton;
	
	/**
	 * Constructs a container for GUI_Window that allows user to edit the information of an existing meter or enter the 
	 * information of a new meter. This is accessed via a Meter_Window and uses the customer associated with that window. 
	 * If the user selects the "Confirm" button and the information meets all the requirements, commits the changes or the 
	 * new meter to the database and returns to Customer_Window or Meter_Window.
	 * @param frameInput The GUI_Window that display this container.
	 * @param inputMeter The meterNumber of the meter being accessed. If making a new meter, this is -1.
	 * @param inputCID The accountNumber of the customer this meter is linked to. Required.
	 * @param inputName The name of the customer this meter is linked to. Required.
	 */
	Edit_Meter_Window(GUI_Window frameInput, int inputMeter, int inputCID, String inputName) {
		//Stores parameters into instance variables.
		frame = frameInput;
		accessedMeter = inputMeter;
		cID = inputCID;
		cName = inputName;
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(new GridBagLayout());
		
		//Set frame title and some labels depending on whether the user is creating or editing a meter.
		if (accessedMeter == -1) {
			frame.setTitle("KC Energy - New Meter for " + cName);
			header1Label = new JLabel("Creating New Meter for");
			header2Label = new JLabel("Account #" + cID + ": " + cName);
			warning1Label = new JLabel();
			warning2Label = new JLabel();
		}
		else {
			frame.setTitle("KC Energy - Modifiying Meter #" + accessedMeter);
			header1Label = new JLabel("Modifying Meter #" + accessedMeter, JLabel.CENTER);
			header2Label = new JLabel("For " + cName, JLabel.CENTER);
			warning1Label = new JLabel("WARNING: Only new charges to this meter will", JLabel.CENTER);
			warning1Label.setFont(new Font(Font.SERIF, Font.ITALIC, 16));
			warning2Label = new JLabel("automatically use an updated tariff value.", JLabel.CENTER);
			warning2Label.setFont(new Font(Font.SERIF, Font.ITALIC, 16));
			try {
				stmt = frame.connection.createStatement();
				meterInfo = stmt.executeQuery(String.format("SELECT * FROM Meter WHERE MeterID = %d;", accessedMeter));
				meterInfo.next();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
		
		header1Label.setFont(new Font(Font.SERIF, Font.BOLD, 36));
		header2Label.setFont(new Font(Font.SERIF, Font.BOLD, 36));
		typeLabel = new JLabel("Energy Type:");
		tariffLabel = new JLabel("Energy Tariff:");
		
		//Allows user to select from generic energy types or enter their own.
		typeBox = new JComboBox<String>(genericTypes);
		typeBox.setEditable(true);
		
		if (accessedMeter == -1) {
			//If creating a new meter, make tariffField blank.
			tariffField = new JTextField(null, 10);
		}
		else {
			//If editing an existing meter, make tariffField be pre-filled with values from meterInfo.
			try {
				typeBox.setSelectedItem(meterInfo.getString("energyType"));
				BigDecimal currentTariff = meterInfo.getBigDecimal("tariff");
				tariffField = new JTextField(currentTariff.toString(), 10);
				meterInfo.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
		
		confirmButton = new JButton("Confirm");
		confirmButton.setMnemonic(KeyEvent.VK_C);
		confirmButton.addActionListener(new confirmListener());
		backButton = new JButton("Back");
		backButton.setMnemonic(KeyEvent.VK_B);
		backButton.addActionListener(new backListener());
		
		//Adds all elements to the Edit_Customer_Window and resets frame's content.
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = .2;
		gbc.gridwidth = 4;
		add(header1Label, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 4;
		add(header2Label, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weighty = .2;
		gbc.gridwidth = 1;
		add(typeLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		add(typeBox, gbc);
		gbc.gridx = 2;
		gbc.gridy = 2;
		add(tariffLabel, gbc);
		gbc.gridx = 3;
		gbc.gridy = 2;
		add(tariffField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weighty = 0;
		gbc.gridwidth = 4;
		add(warning1Label, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weighty = 0;
		gbc.gridwidth = 4;
		add(warning2Label, gbc);
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		add(confirmButton, gbc);
		gbc.gridx = 3;
		gbc.gridy = 5;
		add(backButton, gbc);
		frame.resetContent();
	}
	
	//Checks if entered information is valid, then either creates new customer or updates existing customer in database.
	public class confirmListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String errorMessages = "";
			BigDecimal tr = new BigDecimal(0);
			
			String et = (String)typeBox.getSelectedItem();
			//Energy type must be filled in and at most 100 characters.
			if (et.trim().length() < 1 || et.trim().length() > 100) {
				errorMessages += "Energy Type must be filled in (max 100 characters).\n";
			}
			//Backslashes will not be allowed in the text field since it could mess with the MySQL command.
			else if (et.contains("\\")) {
				errorMessages += "Energy Type cannot contain backslashes.\n";
			}
			//Tariff value must be a valid, positive double less than 1000.
			try {
				tr = BigDecimal.valueOf(Double.parseDouble(tariffField.getText()));
				if (tr.compareTo(new BigDecimal(.01)) == -1 || tr.compareTo(new BigDecimal(999.99)) == 1) {
					errorMessages += "Tariff rate must be positive and less than $1000.\n";
				}
			}
			catch (NumberFormatException nfe) {
				errorMessages += "A valid tariff rate between $0 and $1000 must be specified.\n";
			}
			
			//If no errors were detected, collect values from GUI components and perform appropriate operation for MySQL database.
			if (errorMessages.equals("")) {
				try {
					stmt = frame.connection.createStatement();
					if (accessedMeter == -1) {
						//Makes a new entry in Meter.
						stmt.executeUpdate(String.format(insertBase, cID, et.trim().replace("'", "''"), tr));
					}
					else {
						//Updates an entry in Meter with meterID equal to accessedMeter.
						stmt.executeUpdate(String.format(updateBase, et.trim().replace("'", "''"), tr, accessedMeter));
					}
					
					frame.connection.commit();
					stmt.close();
					if (accessedMeter == -1) {
						//If a new meter was made, return to Meter_Window.
						frame.setContentPane(new Meter_Window(frame, cID));
					}
					else {
						//If pre-existing meter was updated, return to Charge_Window for that customer.
						frame.setContentPane(new Charge_Window(frame, accessedMeter));
					}
				}
				catch (SQLException sqle){
					JOptionPane.showMessageDialog(null, sqle.getMessage());
				}
			}
			else {
				//If any entries were invalid, shows a message box with details on what needs to be changed.
				JOptionPane.showMessageDialog(null, errorMessages);
			}
		}
	}
	
	//If making a new meter, goes back to Meter_Window without making changes to the database.
	//If editing a meter, goes back to Charge_Window for this meter without making any changes to the database.
	public class backListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (accessedMeter == -1) {
				frame.setContentPane(new Meter_Window(frame, cID));
			}
			else {
				frame.setContentPane(new Charge_Window(frame, accessedMeter));
				try {
					stmt.close();
				} catch (SQLException sqle) {
					JOptionPane.showMessageDialog(null, sqle.getMessage());
				}
			}
			
		}
	}
}
