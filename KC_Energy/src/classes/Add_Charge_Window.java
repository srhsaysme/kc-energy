package classes;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.math.*;

public class Add_Charge_Window extends Container {
	protected static GUI_Window frame;
	//MySQL query and data structure setup
	protected int cID, mID;
	private ResultSet meterSet = null;
	private Statement stmt;
	private final static String meterQueryBase = "SELECT * FROM Meter WHERE MeterID = %d;";
	private BigDecimal meterTariff;
	
	//GUI component setup
	private JLabel headerLabel, tariffLabel, currentCostLabel, inputLabel;
	private JTextField amountField;
	private JCheckBox paidBox;
	private JButton confirmButton, backButton;
	
	/**
	 * Constructs a container for GUI_Window that allows user to create a charge connected to inputMeter. This is accessed via a 
	 * Charge_Window and uses the meter from that window. If the user selects the "Confirm" button and the charge amount is valid, 
	 * commits a new charge to the database, recalculates the customer's balance, and returns to Customer_Window.
	 * @param frameInput The GUI_Window that display this container.
	 * @param inputMeter The meterNumber of the meter being used. Required.
	 */
	Add_Charge_Window(GUI_Window frameInput, int inputMeter) {
		//Stores parameters into instance variables.
		frame = frameInput;
		mID = inputMeter;
		
		//Set frame title and gets information about inputMeter from the database.
		frame.setTitle("KC Energy - New Charge for Meter #" + mID);
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(new GridBagLayout());
		try {
			stmt = frame.connection.createStatement();
			meterSet = stmt.executeQuery(String.format(meterQueryBase, mID));
			meterSet.next();
			meterTariff = meterSet.getBigDecimal("Tariff");
			cID = meterSet.getInt("custID");
			meterSet.close();
		}
		catch (SQLException sqle) {
			JOptionPane.showMessageDialog(null, sqle.getMessage());
		}
		//Uses info from meterSet to set up labels.
		headerLabel = new JLabel("New Charge for Meter #" + mID, JLabel.CENTER);
		headerLabel.setFont(new Font(Font.SERIF, Font.BOLD, 36));
		tariffLabel = new JLabel("Current Tariff: $" + meterTariff + " per unit", JLabel.CENTER);
		currentCostLabel = new JLabel("Current Cost: $0", JLabel.CENTER);
		inputLabel = new JLabel("Usage:");
		
		amountField = new JTextField("0", 10);
		amountField.getDocument().addDocumentListener(new amountListener());
		paidBox = new JCheckBox("Paid?");
		paidBox.setBackground(new Color(244, 222, 173));
		confirmButton = new JButton("Confirm");
		confirmButton.setMnemonic(KeyEvent.VK_C);
		confirmButton.addActionListener(new confirmListener());
		backButton = new JButton("Back");
		backButton.setMnemonic(KeyEvent.VK_B);
		backButton.addActionListener(new backListener());
		
		//Adds all elements to the Edit_Customer_Window and resets frame's content.
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipady = 4;
		gbc.gridwidth = 3;
		add(headerLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(tariffLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		add(inputLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		add(amountField, gbc);
		gbc.gridx = 2;
		gbc.gridy = 2;
		add(paidBox, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		add(currentCostLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		add(confirmButton, gbc);
		gbc.gridx = 2;
		gbc.gridy = 4;
		add(backButton, gbc);
		frame.resetContent();
	}
	
	//Checks if entered amount is valid, then creates new charge and updates customer balance.
	public class confirmListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String errorMessage = "";
			//If paidBox is selected, sets new charge to "Paid". Else, it is created as "Unpaid".
			boolean paid = paidBox.isSelected();
			try {
				BigDecimal useAmount = new BigDecimal(Double.parseDouble(amountField.getText().trim()));
				//Usage amount must be between 0 and 1000.
				if (useAmount.compareTo(new BigDecimal(0.01)) <= 0 || useAmount.compareTo(new BigDecimal(999.99)) == 1) {
					JOptionPane.showMessageDialog(null, "Please enter a valid amount of used energy between 0 and 1000.");
				}
				else {
					//Creates charge using stored procedure in MySQL server.
					CallableStatement call = frame.connection.prepareCall("{call create_charge(?,?,?)}");
					call.setInt(1, mID);
					call.setBigDecimal(2, useAmount);
					call.setBoolean(3, paid);
					call.execute();
					//Recalculates customer's balance using stored procedure.
					call = frame.connection.prepareCall("{call calculate_customer_balance(?)}");
					call.setInt(1, cID);
					call.execute();
					call.close();
					frame.connection.commit();
					//Sets frame's content to Charge_Window for selected meter.
					frame.setContentPane(new Charge_Window(frame, mID));
					stmt.close();
				}
			}
			catch (NumberFormatException nfe) {
				//If usage amount is invalid, shows this message box.
				JOptionPane.showMessageDialog(null, "Please enter a valid amount of used energy between 0 and 1000.");
			}
			catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Sets frame's content to Charge_Window for selected meter.
	public class backListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			frame.setContentPane(new Charge_Window(frame, mID));
			try {
				stmt.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Automatically updates currentCostLabel to show cost of charge at present. Cost = content of amountField * current tariff.
	public class amountListener implements DocumentListener {
		public void update() {
			try {
				BigDecimal useAmount = new BigDecimal(Double.parseDouble(amountField.getText()));
				if (useAmount.compareTo(new BigDecimal(0)) >= 0 && useAmount.compareTo(new BigDecimal(999.99)) < 1) {
					currentCostLabel.setText(String.format("Current Cost: $%s", useAmount.multiply(meterTariff).setScale(2, RoundingMode.HALF_UP).toString()));
				}
				else {
					currentCostLabel.setText("Current Cost: $N/A");
				}
			}
			catch (NumberFormatException nfe) {
				currentCostLabel.setText("Current Cost: $N/A");
			}
		}
		
		public void insertUpdate(DocumentEvent e) {
			update();
		}

		public void removeUpdate(DocumentEvent e) {
			update();
		}

		public void changedUpdate(DocumentEvent e) {
			update();
		}
	}
}
