package classes;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Charge_Window extends Container {
	protected static GUI_Window frame;
	//MySQL query and data structure setup
	private final static String countQuery = "SELECT COUNT(*) FROM charge WHERE metID = ";
	private final static String custNameQuery = "SELECT Name FROM customer WHERE AccountNumber = ";
	private final static String meterInfoQuery = "SELECT * FROM meter WHERE meterID = ";
	private final static String selectionQueryBase = "SELECT chargeID, chargeAmount, Cost, ChargeDate, Paid FROM charge WHERE metID = ";
	private final static String[] columnNames = {"Charge ID", "Usage", "Cost", "Date Made", "Paid Status"};
	protected Object[] meterDataArray;
	protected Object[][] sqlData;
	private ResultSet results, sizeSet, meterInfo, custNameSet;
	private Statement stmt;
	protected String cName;
	
	//GUI component setup
	private JLabel headerLabel, customerInfoLabel, typeLabel, usageLabel, tariffLabel, balanceLabel;
	private JTable chargeTable;
	private JScrollPane tableScrollPane;
	private JButton addChargeButton, setPaidButton, deleteChargeButton, backButton, editMeterButton;
	private JPanel buttonPanel;
	
	/**
	 * Constructs a container for GUI_Window that displays a table filled with information on the charge MySQL table that are 
	 * associated with a certain meter. This window lets the user edit the meter, add charges, delete charges, and set the charges
	 * to "paid" or "unpaid".
	 * @param frameInput frameInput The GUI_Window that display this container.
	 * @param metNum The accountNumber of the customer being accessed. Required.
	 */
	Charge_Window(GUI_Window frameInput, int metNum) {
		try {
			//Fetches information about meter and associated charges from MySQL database.
			frame = frameInput;
			stmt = frame.connection.createStatement();
			
			//Fetches the number of charges associated with this meter.
			sizeSet = stmt.executeQuery(countQuery + metNum + ";");
			sizeSet.next();
			int dataSize = sizeSet.getInt(1);
			//To fill out window, table must have at least 24 rows.
			if (dataSize < 24) {
				dataSize = 24;
			}
			sizeSet.close();
			//Fetches all information regarding meter and stores it in meterInfo.
			meterInfo = stmt.executeQuery(meterInfoQuery + metNum + ";");
			meterInfo.next();
			meterDataArray = new Object[]{meterInfo.getInt("MeterID"), meterInfo.getInt("custID"), meterInfo.getString("EnergyType"), 
					meterInfo.getBigDecimal("EnergyUsage"), meterInfo.getBigDecimal("Tariff"), meterInfo.getBigDecimal("Balance")};
			meterInfo.close();
			//Fetches name of customer linked to meter.
			custNameSet = stmt.executeQuery(custNameQuery + meterDataArray[1] + ";");
			custNameSet.next();
			cName = custNameSet.getString("Name");
			custNameSet.close();
			//Makes an Object 2-D array with a row for each charge associated with accessedMeter, then fills it with a ResultSet.
			sqlData = new Object[dataSize][5];
			results = stmt.executeQuery(selectionQueryBase + metNum + " ORDER BY ChargeID DESC;");
			int row = 0;
			while (results.next()) {
				sqlData[row][0] = results.getInt("chargeID");
				sqlData[row][1] = results.getBigDecimal("chargeAmount");
				sqlData[row][2] = "$" + results.getBigDecimal("Cost");
				sqlData[row][3] = results.getDate("ChargeDate").toString();
				sqlData[row][4] = results.getBoolean("Paid");
				//Converts Boolean "Paid" to Strings for human use.
				if (sqlData[row][4].equals(false)) {
					sqlData[row][4] = "Unpaid";
				}
				if (sqlData[row][4].equals(true)) {
					sqlData[row][4] = "Paid";
				}
				row++;
			}
			results.close();
		}
		catch (SQLException sqle) {
			JOptionPane.showMessageDialog(null, sqle.getMessage());
		}
		
		//Set frame title and layout with information from arrays.
		frame = frameInput;
		frame.setTitle("KC Energy -  Meter #" + meterDataArray[0]);
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(new GridBagLayout());
		
		//Uses information from custDataArray to format labels.
		headerLabel = new JLabel("Meter #" + meterDataArray[0], JLabel.CENTER);
		headerLabel.setFont(new Font(Font.SERIF, Font.BOLD, 36));
		customerInfoLabel = new JLabel("Customer: #" + meterDataArray[1] + ", " + cName, JLabel.LEFT);
		typeLabel = new JLabel("Energy Type: " + meterDataArray[2] + " ", JLabel.RIGHT);
		usageLabel = new JLabel("Energy Usage: " + meterDataArray[3]);
		tariffLabel = new JLabel("Tariff Rate: $" + meterDataArray[4], JLabel.CENTER);
		balanceLabel = new JLabel("Current Balance: $" + meterDataArray[5]);
		balanceLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		
		//Creates a table with the info from sqlData, then adds it to a JScrollPane.
		chargeTable = new JTable(sqlData, columnNames);
		chargeTable.setDefaultEditor(Object.class, null);
		chargeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chargeTable.setCellSelectionEnabled(false);
		chargeTable.setRowSelectionAllowed(true);
		tableScrollPane = new JScrollPane(chargeTable);
		tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		//Sets up JButtons to manipulate charges in a panel.
		buttonPanel = new JPanel(new GridLayout(2, 3, 50, 0));
		buttonPanel.setBackground(new Color(244, 222, 173));
		addChargeButton = new JButton("Add Charge");
		addChargeButton.setMnemonic(KeyEvent.VK_A);
		addChargeButton.addActionListener(new addChargeListener());
		setPaidButton = new JButton("Set Paid/Unpaid");
		setPaidButton.setMnemonic(KeyEvent.VK_S);
		setPaidButton.addActionListener(new setPaidListener());
		deleteChargeButton = new JButton("Delete Charge");
		deleteChargeButton.setMnemonic(KeyEvent.VK_D);
		deleteChargeButton.addActionListener(new deleteChargeListener());
		editMeterButton = new JButton("Edit Meter");
		editMeterButton.setMnemonic(KeyEvent.VK_E);
		editMeterButton.addActionListener(new editMeterListener());
		backButton = new JButton("Back");
		backButton.setMnemonic(KeyEvent.VK_B);
		backButton.addActionListener(new backListener());
		buttonPanel.add(addChargeButton);
		buttonPanel.add(setPaidButton);
		buttonPanel.add(deleteChargeButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(backButton);
		
		//Adds all elements to the Customer_Window and resets frame's content.
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		add(headerLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		add(customerInfoLabel, gbc);
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		add(typeLabel, gbc);
		gbc.ipadx = 5;
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(usageLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		add(tariffLabel, gbc);
		gbc.gridx = 2;
		gbc.gridy = 2;
		add(editMeterButton, gbc);
		gbc.ipadx = 0;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		add(balanceLabel, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weighty = 1.0;
		gbc.gridwidth = 3;
		add(tableScrollPane, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weighty = 0;
		add(buttonPanel, gbc);
		frame.resetContent();
	}
	
	//Sets frame's content to Edit_Meter_Window for selected meter, passing the necessary parameters on.
	public class editMeterListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			frame.setContentPane(new Edit_Meter_Window(frame, (int)meterDataArray[0], (int)meterDataArray[1], cName));
			try {
				stmt.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Sets frame's content to Add_Charge_Window for selected meter, passing the necessary parameters on.
	public class addChargeListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			frame.setContentPane(new Add_Charge_Window(frame, (int)meterDataArray[0]));
			try {
				stmt.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Sets a selected charge to "Paid" or "Unpaid" via dialog box.
	public class setPaidListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int selectedRow = chargeTable.getSelectedRow();
				if (selectedRow != -1) {
					int chID = (int)chargeTable.getValueAt(selectedRow, 0);
					int option = JOptionPane.showOptionDialog(null, "What is the payment status of Charge #" + chID + "?", 
							"Charge #" + chID, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
							new String[]{"Paid", "Unpaid", "Cancel"}, null);
					//Sets charge to "Paid" and recalculates balance of associated customer.
					if (option == 0) {
						stmt = frame.connection.createStatement();
						stmt.executeUpdate("UPDATE charge SET Paid = true WHERE chargeID = " + chID + ";");
						CallableStatement call = frame.connection.prepareCall("{call calculate_customer_balance(?)}");
						call.setInt(1, ((int)meterDataArray[1]));
						call.execute();
						call.close();
						frame.connection.commit();
						frame.setContentPane(new Charge_Window(frame, (int)meterDataArray[0]));
						stmt.close();
					}
					//Sets charge to "Unpaid" and recalculates balance of associated customer.
					else if (option == 1) {
						stmt = frame.connection.createStatement();
						stmt.executeUpdate("UPDATE charge SET Paid = false WHERE chargeID = " + chID + ";");
						CallableStatement call = frame.connection.prepareCall("{call calculate_customer_balance(?)}");
						call.setInt(1, ((int)meterDataArray[1]));
						call.execute();
						call.close();
						frame.connection.commit();
						frame.setContentPane(new Charge_Window(frame, (int)meterDataArray[0]));
						stmt.close();
					}
				}
				else {
					//If no row is selected, shows message.
					JOptionPane.showMessageDialog(null, "No charge selected.");
				}
			}
			catch (NullPointerException npe) {
				//If row is blank, shows message.
				JOptionPane.showMessageDialog(null, "No charge selected.");
			}
			catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Deletes selected charge after user confirms selection.
	public class deleteChargeListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int selectedRow = chargeTable.getSelectedRow();
				if (selectedRow != -1) {
					int cID = (int)chargeTable.getValueAt(selectedRow, 0);
					int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete charge #" + cID + 
							"?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					//Only deletes charge if user selects YES in confirmation box.
					if (confirmation == 0) {
						stmt = frame.connection.createStatement();
						stmt.executeUpdate("DELETE FROM charge WHERE chargeID = " + cID + ";");
						//Recalculates balance of associated customer.
						CallableStatement call = frame.connection.prepareCall("{call calculate_customer_balance(?)}");
						call.setInt(1, ((int)meterDataArray[1]));
						call.execute();
						call.close();
						frame.connection.commit();
						frame.setContentPane(new Charge_Window(frame, (int)meterDataArray[0]));
						stmt.close();
					}
				}
				else {
					//If no row is selected, shows message.
					JOptionPane.showMessageDialog(null, "No charge selected.");
				}
			}
			catch (NullPointerException npe) {
				//If row is blank, shows message.
				JOptionPane.showMessageDialog(null, "No charge selected.");
			}
			catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Sets frame's content back to Meter_Window for this meter.
	public class backListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			frame.setContentPane(new Meter_Window(frame, (int)meterDataArray[1]));
			try {
				stmt.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
}