package classes;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Edit_Customer_Window extends Container {
	protected static GUI_Window frame;
	private final static String[] states = new String[]{"", "AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "GA", "HI", "IA", 
			"ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", 
			"NV", "NY", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WI", "WV", "WY"};
	private final static String[] countries = new String[]{"", "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", 
			"Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", 
			"Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", 
			"Bosnia and Herzegowina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", "Brunei Darussalam", 
			"Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", 
			"Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", 
			"Comoros", "Congo", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Cyprus", "Czech Republic", 
			"Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "East Timor", "Ecuador", 
			"Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands", "Faroe Islands", 
			"Fiji", "Finland", "France", "France Metropolitan", "French Guiana", "French Polynesia", "French Southern Territories", 
			"Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", 
			"Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Heard and Mc Donald Islands", "Honduras", "Hong Kong", 
			"Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", 
			"Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", 
			"Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", 
			"Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", 
			"Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", 
			"Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", 
			"Nigeria", "Niue", "Norfolk Island", "North Korea", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", 
			"Palestine", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn", "Poland", "Portugal", 
			"Puerto Rico", "Qatar", "Réunion", "Romania", "Russian Federation", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia",
			"Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", 
			"Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", 
			"South Georgia and the South Sandwich Islands", "South Korea", "Spain", "Sri Lanka", "St. Helena", 
			"St. Pierre and Miquelon", "Sudan", "Suriname", "Svalbard and Jan Mayen Islands", "Swaziland", "Sweden", "Switzerland", 
			"Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", 
			"Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", 
			"United Kingdom", "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan", "Vatican City", 
			"Vanuatu", "Venezuela", "Vietnam", "Virgin Islands (British)", "Virgin Islands (U.S.)", "Wallis and Futuna Islands", 
			"Western Sahara", "Yemen", "Yugoslavia", "Zambia", "Zimbabwe"};
	//Country list slightly modified from https://gist.github.com/whit3hawks/3b507d7005448eebb9c9e78ce853c254
	
	//MySQL query and data structure setup
	private final static String insertBase = "INSERT INTO Customer(Name, PhoneNumber, EmailAddress, StreetAddress, City, State, Country, ZipCode) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);";
	private final static String updateBase = "UPDATE Customer SET Name = %s, PhoneNumber = %s, EmailAddress = %s, StreetAddress = %s, City = %s, State = %s, Country = %s, ZipCode = %s WHERE AccountNumber = %d";
	protected int accessedAccount = -1;
	private ResultSet customerInfo;
	private Statement stmt;
	
	//GUI component setup
	private JLabel headerLabel, nameLabel, phoneLabel, emailLabel, addressLabel, cityLabel, stateLabel, zipLabel, countryLabel;
	private JTextField nameField, phoneField, emailField, addressField, cityField, zipField;
	private JComboBox<String> stateBox, countryBox;
	private JButton confirmButton, backButton;
	
	/**
	 * Constructs a container for GUI_Window that allows user to edit the information of an existing customer or enter the 
	 * information of a new customer. If the user selects the "Confirm" button and the information meets all the requirements, 
	 * commits the changes to the database and returns to Customer_Window or Meter_Window.
	 * @param frameInput The GUI_Window that display this container.
	 * @param accNum The accountNumber of the customer being accessed. If making a new customer, this is -1.
	 */
	Edit_Customer_Window(GUI_Window frameInput, int accNum) {
		//Set frame title depending on whether the user is creating or editing a customer.
		frame = frameInput;
		accessedAccount = accNum;
		if (accessedAccount == -1) {
			frame.setTitle("KC Energy - Adding New Customer");
			customerInfo = null;
			headerLabel = new JLabel("Create New Customer", JLabel.CENTER);
		}
		else {
			frame.setTitle("KC Energy - Editing Customer #" + accNum);
			try {
				stmt = frame.connection.createStatement();
				customerInfo = stmt.executeQuery(String.format("SELECT * FROM Customer WHERE AccountNumber = %d;", accessedAccount));
				customerInfo.next();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
			headerLabel = new JLabel("Editing Account #" + accNum, JLabel.CENTER);
		}
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(new GridBagLayout());

		headerLabel.setFont(new Font(Font.SERIF, Font.BOLD, 36));
		nameLabel = new JLabel("Name:");
		phoneLabel = new JLabel("Phone Number:");
		emailLabel = new JLabel("Email Address:");
		addressLabel = new JLabel("Street Address:");
		cityLabel = new JLabel("City:");
		stateLabel = new JLabel("State:");
		zipLabel = new JLabel("ZIP Code:");
		countryLabel = new JLabel("Country:");
		
		if (accessedAccount == -1) {
			//If creating a new customer, sets textFields to have null text and the comboBoxes to be empty.
			nameField = new JTextField(null, 100);
			phoneField = new JTextField(null, 12);
			emailField = new JTextField(null, 100);
			addressField = new JTextField(null, 100);
			cityField = new JTextField(null, 50);
			zipField = new JTextField(null, 5);
			zipField.setEnabled(false);
			
			stateBox = new JComboBox<String>(states);
			stateBox.setEnabled(false);
			countryBox = new JComboBox<String>(countries);
			countryBox.addActionListener(new countryBoxListener());
		}
		else {
			try {
				//If editing an existing customer, pre-fills textFields and comboBoxes to have text matching current values in 
				//database.
				nameField = new JTextField(customerInfo.getString("Name"), 100);
				phoneField = new JTextField(customerInfo.getString("PhoneNumber"), 12);
				emailField = new JTextField(customerInfo.getString("EmailAddress"), 100);
				addressField = new JTextField(customerInfo.getString("StreetAddress"), 100);
				cityField = new JTextField(customerInfo.getString("City"), 50);
				zipField = new JTextField(customerInfo.getString("ZipCode"), 5);
			
				stateBox = new JComboBox<String>(states);
				if (customerInfo.getString("State") == null) {
					stateBox.setSelectedItem("");
				}
				else {
					stateBox.setSelectedItem(customerInfo.getString("State"));
				}
				countryBox = new JComboBox<String>(countries);
				countryBox.setSelectedItem(customerInfo.getString("Country"));
				countryBox.addActionListener(new countryBoxListener());
			
				if (!countryBox.getSelectedItem().equals("United States")) {
					zipField.setEnabled(false);
					stateBox.setEnabled(false);
				}
				customerInfo.close();
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
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1;
		gbc.gridwidth = 5;
		add(headerLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weighty = .2;
		gbc.gridwidth = 1;
		add(nameLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		add(nameField, gbc);
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		add(phoneLabel, gbc);
		gbc.gridx = 4;
		gbc.gridy = 1;
		add(phoneField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(emailLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		add(emailField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		add(addressLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 4;
		add(addressField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		add(zipLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 4;
		add(zipField, gbc);
		gbc.gridx = 2;
		gbc.gridy = 4;
		add(cityLabel, gbc);
		gbc.gridx = 3;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		add(cityField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		add(stateLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 5;
		add(stateBox, gbc);
		gbc.gridx = 2;
		gbc.gridy = 5;
		add(countryLabel, gbc);
		gbc.gridx = 3;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		add(countryBox, gbc);
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.weighty = 1.0;
		gbc.gridwidth = 1;
		add(confirmButton, gbc);
		gbc.gridx = 3;
		gbc.gridy = 6;
		add(backButton, gbc);
		frame.resetContent();
	}
	
	//If making a new customer, goes back to Customer_Window without making changes to the database. 
	//If editing a customer, goes back to Meter_Window for this customer without making any changes to the database.
	private class backListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (accessedAccount == -1)
			{
				frame.setContentPane(new Customer_Window(frame, "", -1));
			}
			else {
				frame.setContentPane(new Meter_Window(frame, accessedAccount));
				try {
					stmt.close();
				} catch (SQLException sqle) {
					JOptionPane.showMessageDialog(null, sqle.getMessage());
				}
			}		
		}
	}
	
	//Checks if entered information is valid, then either creates new customer or updates existing customer in database.
	private class confirmListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String errorMessages = "";
			boolean phoneIsNumber = true;
			
			//Name must be at least 5 and at most 100 characters long.
			if (nameField.getText().trim().length() < 5 || nameField.getText().trim().length() > 100) {
				errorMessages += "Name must be at least 5 characters and at most 100 characters long.\n";
			}
			//Backslashes will not be allowed in the strings since it could mess with the MySQL command.
			else if (nameField.getText().contains("\\")) {
				errorMessages += "Name cannot contain backslashes.\n";
			}
			//Phone number must be 9-12 digits.
			for (int i = 0; i < phoneField.getText().length(); i++) {
				if (!Character.isDigit(phoneField.getText().charAt(i))) {
					phoneIsNumber = false;
					break;
				}
			}
			if (!phoneIsNumber) {
				errorMessages += "Invalid phone number entered. Please enter a 9 to 12 digit number with no spaces or punctuation.\n";
			}
			else if (phoneField.getText().length() < 9 || phoneField.getText().length() > 12) {
				errorMessages += "Phone number must be at least 9 digits and at most 12 digits long.\n";
			}
			//Email must be at least 5 and at most 100 characters long. They must include a single @ as well.
			if (emailField.getText().indexOf('@') == -1) {
				errorMessages += "Invalid email entered. Make sure that email includes domain separated by '@'.\n";
			}
			else if (emailField.getText().indexOf('@') != emailField.getText().lastIndexOf('@')) {
				errorMessages += "Multiple '@'s detected. Invalid email address.\n";
			}
			else if (emailField.getText().trim().length() < 5 || emailField.getText().trim().length() > 100) {
				errorMessages += "Email must be at least 5 characters and at most 100 characters long.\n";
			}
			else if (emailField.getText().contains("\\")) {
				errorMessages += "Email cannot contain backslashes.\n";
			}
			//Address must be at least 10 and at most 100 characters long.
			if (addressField.getText().trim().length() < 10 || addressField.getText().trim().length() > 100) {
				errorMessages += "Address must be at least 10 characters and at most 100 characters long.\n";
			}
			else if (addressField.getText().contains("\\")) {
				errorMessages += "Address cannot contain backslashes.\n";
			}
			//City name must be at least 2 and at most 50 characters long.
			if (cityField.getText().trim().length() < 2 || cityField.getText().trim().length() > 50) {
				errorMessages += "City name must be at least 2 and at most 50 characters long.\n";
			}
			else if (cityField.getText().contains("\\")) {
				errorMessages += "Name cannot contain backslashes.\n";
			}
			//Country must be selected.
			if (countryBox.getSelectedItem().equals("")) {
				errorMessages += "Please select a country.\n";
			}
			//If the customer is in the United States, must have a state and ZIP Code as well.
			else if (countryBox.getSelectedItem().equals("United States")) {
				//A state must be selected.
				if (stateBox.getSelectedItem().equals("")) {
					errorMessages += "Please select a state.\n";
				}
				//ZIP Codes must be a 5-digit number.
				if (zipField.getText().length() != 5) {
					errorMessages += "Please enter a valid 5-digit ZIP Code.\n";
				}
				else {
					boolean zipIsNumber = true;
					for (int i = 0; i < zipField.getText().length(); i++) {
						if (!Character.isDigit(zipField.getText().charAt(i))) {
							zipIsNumber = false;
							break;
						}
					}
					if (!zipIsNumber) {
						errorMessages += "Invalid ZIP Code. The code must be all digits.\n";
					}
				}
			}
			
			//If no errors were detected, collect values from GUI components and perform appropriate operation for MySQL database.
			if (errorMessages.equals("")) {
				String name, phoneNumber, email, address, city, state, country, zipCode;
				//String values must have any instances of ' replaced with '' to work properly with MySQL.
				name = "'" + nameField.getText().trim().replace("'", "''") + "'";
				//Phone number is guaranteed to be only digits, so no replacement necessary.
				phoneNumber = "'" + phoneField.getText().trim() + "'";
				email = "'" + emailField.getText().trim().replace("'", "''") + "'";
				address = "'" + addressField.getText().trim().replace("'", "''") + "'";
				city = "'" + cityField.getText().trim().replace("'", "''") + "'";
				state = "'" + stateBox.getSelectedItem().toString() + "'";
				//If no state is needed, have null instead.
				if (state.equals("''")) {
					state = "null";
				}
				country = "'" + countryBox.getSelectedItem().toString().replace("'", "''") + "'";
				zipCode = "'" + zipField.getText() + "'";
				//If no ZIP code is required, have null instead.
				if (zipCode.equals("''")) {
					zipCode = "null";
				}
				try {
					stmt = frame.connection.createStatement();
					if (accessedAccount == -1) {
						//Makes a new entry in Customer.
						stmt.executeUpdate(String.format(insertBase, name, phoneNumber, email, address, city, state, country, zipCode));
					}
					else {
						//Updates the entry in Customer with AccountNumber equal to accessedAccount.
						stmt.executeUpdate(String.format(updateBase, name, phoneNumber, email, address, city, state, country, zipCode, accessedAccount));
					}
					frame.connection.commit();
					stmt.close();
				} catch (SQLException sqle) {
					JOptionPane.showMessageDialog(null, sqle.getMessage());
				}
				if (accessedAccount == -1)
				{
					//If a new customer was made, return to Customer_Window.
					frame.setContentPane(new Customer_Window(frame, "", -1));
				}
				else {
					//If pre-existing customer was updated, return to Meter_Window for that customer.
					frame.setContentPane(new Meter_Window(frame, accessedAccount));
				}
			}
			else {
				//If any entries were invalid, shows a message box with details on what needs to be changed.
				JOptionPane.showMessageDialog(null, errorMessages);
			}
		}
	}
	
	//Only activates stateBox and zipField if countryBox's value is currently "United States".
	private class countryBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (countryBox.getSelectedItem().equals("United States")) {
				stateBox.setEnabled(true);
				zipField.setEnabled(true);
			}
			else {
				stateBox.setSelectedItem("");
				stateBox.setEnabled(false);
				zipField.setText("");
				zipField.setEnabled(false);
			}
		}
	}
}