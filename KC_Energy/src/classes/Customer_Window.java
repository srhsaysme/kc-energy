package classes;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JOptionPane;

public class Customer_Window extends Container {
	protected static GUI_Window frame;
	//MySQL query and data structure setup
	private final static String countQuery = "SELECT COUNT(*) FROM customer";
	private final static String selectionQueryBase = "SELECT * FROM customer";
	private final static String[] columnNames = {"Name", "Account Number", "Outstanding Balance"};
	protected Object[][] sqlData;
	private Statement stmt;
	private ResultSet sizeSet, results;
	
	//GUI component setup
	private ButtonGroup searchOptionBG;
	private JPanel searchOptionPanel, buttonPanel;
	private JLabel headerLabel1, headerLabel2, searchByLabel;
	private JTextField searchField;
	private JRadioButton nameRB, idRB;
	private JTable customerTable;
	private JScrollPane tableScrollPane;
	private JButton addCustomerButton, viewCustomerButton, deleteCustomerButton, searchButton;
	
	/**
	 * Constructs a container for GUI_Window that displays a table filled with information on customer MySQL table and lets user
	 * add, delete, or view customers from the selection.
	 * @param frameInput The GUI_Window that display this container.
	 * @param searchTerm The term that will be searched for in the Customer schema in the MySQL database.
	 * @param searchType The type of search that is being used. 0 means the Customer schema will be searched by Name, 
	 * 1 by AccountNumber, and -1 means there is no search
	 */
	Customer_Window(GUI_Window frameInput, String searchTerm, int searchType) {
		//Set frame title and layout.
		frame = frameInput;
		frame.setTitle("KC Energy");
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(new GridBagLayout());
		
		headerLabel1 = new JLabel("Kansas City Energy", JLabel.CENTER);
		headerLabel1.setFont(new Font(Font.SERIF, Font.BOLD, 36));
		headerLabel2 = new JLabel("Customer Management", JLabel.CENTER);
		headerLabel2.setFont(new Font(Font.SERIF, Font.BOLD, 30));
		searchField = new JTextField(searchTerm, 100);
		
		//Sets up components for searching for customers. Elements are organized in searchOptionPanel.
		searchOptionBG = new ButtonGroup();
		searchOptionPanel = new JPanel(new GridLayout(1, 4, 10, 0));
		searchOptionPanel.setBackground(new Color(244, 222, 173));
		searchByLabel = new JLabel("Search By:");
		//If nameRB is selected when search is made, then program searches for names that contain the text in searchField. If idRB is selected, then program searches for AccountNumbers.
		nameRB = new JRadioButton("Name");
		nameRB.setBackground(new Color(244, 222, 173));
		searchOptionBG.add(nameRB);
		idRB = new JRadioButton("Account Number");
		idRB.setBackground(new Color(244, 222, 173));
		searchOptionBG.add(idRB);
		//If the previous search is an AccountNumber search, start with idRB selected. Else, have nameRB be selected by default.
		if (searchType == 1) {
			idRB.setSelected(true);
		}
		else {
			nameRB.setSelected(true);
		}
		
		searchButton = new JButton("Search");
		searchButton.setMnemonic(KeyEvent.VK_S);
		searchButton.addActionListener(new searchListener());
		searchOptionPanel.add(searchByLabel);
		searchOptionPanel.add(nameRB);
		searchOptionPanel.add(idRB);
		searchOptionPanel.add(searchButton);
		
		//Fills out results by fetching customer data from database, using passed parameters to apply search if necessary.
		try {
			stmt = frame.connection.createStatement();
			int dataSize;
			//If this panel uses a name search, then executes query for rows where Name includes the searchTerm.
			if (searchType == 0) {
				sizeSet = stmt.executeQuery(countQuery + " WHERE Name LIKE '%" + searchTerm.replace("'", "''") + "%';");
				sizeSet.next();
				dataSize = sizeSet.getInt(1);
				//To fill out window, table must have at least 24 rows.
				if (dataSize < 24) {
					dataSize = 24;
				}
				sizeSet.close();
				results = stmt.executeQuery(selectionQueryBase + " WHERE Name LIKE '%" + searchTerm.replace("'", "''") + "%' ORDER BY AccountNumber;");
			}
			//If this panel uses an ID search, then executes query for rows where AccountNumber includes the searchTerm.
			else if (searchType == 1) {
				sizeSet = stmt.executeQuery(countQuery + " WHERE CAST(AccountNumber as CHAR) LIKE '%" + searchTerm.replace("'", "''") + "%';");
				sizeSet.next();
				dataSize = sizeSet.getInt(1);
				if (dataSize < 24) {
					dataSize = 24;
				}
				sizeSet.close();
				results = stmt.executeQuery(selectionQueryBase + " WHERE CAST(AccountNumber as CHAR) LIKE '%" + searchTerm.replace("'", "''") + "%' ORDER BY AccountNumber;");
			}
			//If this panel includes no search parameters (i.e. searchType is -1), then executes query for all rows in Customer.
			else {
				sizeSet = stmt.executeQuery(countQuery + ";");
				sizeSet.next();
				dataSize = sizeSet.getInt(1);
				if (dataSize < 24) {
					dataSize = 24;
				}
				sizeSet.close();
				results = stmt.executeQuery(selectionQueryBase + " ORDER BY AccountNumber;");
			}
			//Fills in sqlData, a 2-D Object array, with the names, accountNumbers, and outstandingBalances retrieved from the query.
			sqlData = new Object[dataSize][3];
			int row = 0;
			while (results.next()) {
				sqlData[row][0] = results.getString("name");
				sqlData[row][1] = results.getInt("accountnumber");
				sqlData[row][2] = "$" + results.getBigDecimal("outstandingbalance");
				row++;
			}
			results.close();
		} catch (SQLException sqle) {
			JOptionPane.showMessageDialog(null, sqle.getMessage());
		}
		//Creates a table with the info from sqlData, then adds it to a JScrollPane.
		customerTable = new JTable(sqlData, columnNames);
		customerTable.setDefaultEditor(Object.class, null);
		customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		customerTable.setCellSelectionEnabled(false);
		customerTable.setRowSelectionAllowed(true);
		tableScrollPane = new JScrollPane(customerTable);
		tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		//Sets up JButtons to manipulate customers' info in a panel.
		buttonPanel = new JPanel(new GridLayout(1, 3, 20, 0));
		buttonPanel.setBackground(new Color(244, 222, 173));
		addCustomerButton = new JButton("Add Customer");
		viewCustomerButton = new JButton("View Customer Info");
		deleteCustomerButton = new JButton("Delete Customer");
		addCustomerButton.setMnemonic(KeyEvent.VK_A);
		viewCustomerButton.setMnemonic(KeyEvent.VK_V);
		deleteCustomerButton.setMnemonic(KeyEvent.VK_D);
		buttonPanel.add(addCustomerButton);
		buttonPanel.add(viewCustomerButton);
		buttonPanel.add(deleteCustomerButton);
		addCustomerButton.addActionListener(new addCustListener());
		viewCustomerButton.addActionListener(new viewCustListener());
		deleteCustomerButton.addActionListener(new deleteCustListener());
		
		//Adds all elements to the Customer_Window and resets frame's content.
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(headerLabel1, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(headerLabel2, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(searchField, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 3;
		add(searchOptionPanel, gbc);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weighty = 1.0;
		add(tableScrollPane, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weighty = 0;
		add(buttonPanel, gbc);
		frame.resetContent();
	}
	
	//Sets frame's content to Edit_Customer_Window with no selected customer.
	private class addCustListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			frame.setContentPane(new Edit_Customer_Window(frame, -1));
			try {
				stmt.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
		
	}
	
	//Sets frame's content to Meter_Window for the selected customer. If no valid row is selected, shows message instead.
	private class viewCustListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int selectedRow = customerTable.getSelectedRow();
				if (selectedRow != -1) {
					int accNum = (int)customerTable.getValueAt(selectedRow, 1);
					frame.setContentPane(new Meter_Window(frame, accNum));
					stmt.close();
				}
				else {
					//If no row is selected, shows message.
					JOptionPane.showMessageDialog(null, "Select a customer to view.");
				}
			}
			catch (NullPointerException npe) {
				//If row is blank, shows message.
				JOptionPane.showMessageDialog(null, "Select a customer to view.");
			}
			catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Deletes selected customer after user confirms selection. Deletion cascades in the database, removing all meters and charges 
	//associated with the customer.
	private class deleteCustListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int selectedRow = customerTable.getSelectedRow();
				if (selectedRow != -1) {
					String accName = (String)customerTable.getValueAt(selectedRow, 0);
					int accNum = (int)customerTable.getValueAt(selectedRow, 1);
					int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete customer " + accName + 
							", number " + accNum + "? Any meters and charges linked to this account will also be deleted!", 
							"Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					//Only deletes customer if user selects YES in confirmation box.
					if (confirmation == 0) {
						try {
							stmt = frame.connection.createStatement();
							stmt.executeUpdate("DELETE FROM customer WHERE AccountNumber = " + accNum + ";");
							frame.connection.commit();
							stmt.close();
						} catch (SQLException sqle) {
							JOptionPane.showMessageDialog(null, sqle.getMessage());
						}
						//Resets Customer_Window with no search parameters and with customer deletion complete.
						frame.setContentPane(new Customer_Window(frame, "", -1));
						stmt.close();
					}
				}
				else {
					//If no row is selected, shows message.
					JOptionPane.showMessageDialog(null, "No customer selected.");
				}
			}
			catch (NullPointerException npe) {
				//If row is blank, shows message.
				JOptionPane.showMessageDialog(null, "No customer selected.");
			}
			catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Reloads Customer_Window with search parameters based on the contents of searchField and what radio button is selected.
	private class searchListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String searchInput = searchField.getText().trim();
			if (searchInput.contains("\\")) {
				//Backslashes will not be allowed in the search query since it could mess with the MySQL command.
				JOptionPane.showMessageDialog(null, "Please do not include backslashes in the search bar.");
			}
			else if (searchInput.equals("")) {
				//If searchField is blank, use a fresh Customer_Window without any search parameters.
				frame.setContentPane(new Customer_Window(frame, "", -1));
			}
			else if (nameRB.isSelected()) {
				frame.setContentPane(new Customer_Window(frame, searchInput, 0));
			}
			else {
				frame.setContentPane(new Customer_Window(frame, searchInput, 1));
			}
			try {
				stmt.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
}
