package classes;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Meter_Window extends Container {
	protected static GUI_Window frame;
	//MySQL query and data structure setup
	private final static String countQuery = "SELECT COUNT(*) FROM meter WHERE custID = ";
	private final static String custInfoQuery = "SELECT * FROM customer WHERE AccountNumber = ";
	private final static String selectionQueryBase = "SELECT * FROM meter WHERE custID = ";
	private final static String[] columnNames = {"Meter ID", "Energy Type", "Usage", "Balance"};
	protected static Object[] custDataArray;
	protected Object[][] sqlData;
	private ResultSet results, sizeSet, custInfo;
	private Statement stmt;
	
	//GUI component setup
	private JPanel buttonPanel;
	private JLabel headerLabel, phoneLabel, emailLabel, addressLabel, balanceLabel;
	private JTable meterTable;
	private JScrollPane tableScrollPane;
	private JButton addMeterButton, viewMeterButton, deleteMeterButton, printBillButton, backButton, editCustButton;
	
	/**
	 * Constructs a container for GUI_Window that displays a table filled with information on the meter MySQL table that are 
	 * associated with a certain account. This window lets the user edit the customer's profile and add, view, and delete meters 
	 * from this customer.
	 * @param frameInput The GUI_Window that display this container.
	 * @param accNum The accountNumber of the customer being accessed. Required.
	 */
	Meter_Window(GUI_Window frameInput, int accNum) {
		try {
			//Fetches information about customer and meters from MySQL database.
			frame = frameInput;
			stmt = frame.connection.createStatement();
			
			//Fetches the number of meters associated with this account.
			sizeSet = stmt.executeQuery(countQuery + accNum + ";");
			sizeSet.next();
			int dataSize = sizeSet.getInt(1);
			//To fill out window, table must have at least 24 rows.
			if (dataSize < 24) {
				dataSize = 24;
			}
			sizeSet.close();
			//Fetches all information regarding customer and stores it in custInfo.
			custInfo = stmt.executeQuery(custInfoQuery + accNum + ";");
			custInfo.next();
			custDataArray = new Object[]{custInfo.getString("Name"), custInfo.getInt("AccountNumber"), 
					custInfo.getString("PhoneNumber"), custInfo.getString("EmailAddress"), custInfo.getString("StreetAddress"), 
					custInfo.getString("City"), custInfo.getString("State"), custInfo.getString("Country"), 
					custInfo.getString("ZipCode"), custInfo.getBigDecimal("OutstandingBalance")};
			custInfo.close();
			//Makes an Object 2-D array with a row for each meter associated with accNum customer, then fills it with a ResultSet.
			sqlData = new Object[dataSize][4];
			results = stmt.executeQuery(selectionQueryBase + accNum + " ORDER BY MeterID;");
			int row = 0;
			while (results.next()) {
				sqlData[row][0] = results.getInt("meterID");
				sqlData[row][1] = results.getString("energyType");
				sqlData[row][2] = results.getBigDecimal("energyUsage");
				sqlData[row][3] = "$" + results.getBigDecimal("balance").toString();
				row++;
			}
			results.close();
		}
		catch (SQLException sqle) {
			JOptionPane.showMessageDialog(null, sqle.getMessage());
		}
		
		//Set frame title and layout with information from arrays.
		frame = frameInput;
		frame.setTitle("KC Energy - User " + custDataArray[0]);
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(new GridBagLayout());
		
		//Uses information from custDataArray to format labels.
		headerLabel = new JLabel("Customer #" + custDataArray[1].toString() + ": " + custDataArray[0].toString(), JLabel.CENTER);
		headerLabel.setFont(new java.awt.Font(java.awt.Font.SERIF, java.awt.Font.BOLD, 36));
		phoneLabel = new JLabel("Phone #: " + custDataArray[2].toString(), JLabel.LEFT);
		emailLabel = new JLabel("'" + custDataArray[3].toString() + "'", JLabel.RIGHT);
		balanceLabel = new JLabel("Outstanding Balance: $" + custDataArray[9].toString(), JLabel.CENTER);
		balanceLabel.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 16));
		
		String fullAddress = custDataArray[4].toString() + ", " + custDataArray[5].toString() + ", ";
		if (custDataArray[6] != null) {
			fullAddress += custDataArray[6].toString() + ", ";
		}
		fullAddress += custDataArray[7].toString();
		if (custDataArray[8] != null) {
			fullAddress += " " + custDataArray[8].toString();
		}
		addressLabel = new JLabel("Address: " + fullAddress, JLabel.CENTER);
		
		//Creates a table with the info from sqlData, then adds it to a JScrollPane.
		meterTable = new JTable(sqlData, columnNames);
		meterTable.setDefaultEditor(Object.class, null);
		meterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		meterTable.setCellSelectionEnabled(false);
		meterTable.setRowSelectionAllowed(true);
		tableScrollPane = new JScrollPane(meterTable);
		tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		//Sets up JButtons to manipulate customers' info in a panel.
		buttonPanel = new JPanel(new GridLayout(2, 3, 20, 0));
		buttonPanel.setBackground(new Color(244, 222, 173));
		addMeterButton = new JButton("Add Meter");
		addMeterButton.setMnemonic(KeyEvent.VK_A);
		addMeterButton.addActionListener(new addMeterListener());
		viewMeterButton = new JButton("View Meter");
		viewMeterButton.setMnemonic(KeyEvent.VK_V);
		viewMeterButton.addActionListener(new viewMeterListener());
		deleteMeterButton = new JButton("Delete Meter");
		deleteMeterButton.setMnemonic(KeyEvent.VK_D);
		deleteMeterButton.addActionListener(new deleteMeterListener());
		editCustButton = new JButton("Edit Customer Settings");
		editCustButton.setMnemonic(KeyEvent.VK_E);
		editCustButton.addActionListener(new editCustListener());
		printBillButton = new JButton("Print Bill to PDF");
		printBillButton.setMnemonic(KeyEvent.VK_P);
		printBillButton.addActionListener(new printPDFListener());
		backButton = new JButton("Back");
		backButton.setMnemonic(KeyEvent.VK_B);
		backButton.addActionListener(new backListener());
		buttonPanel.add(addMeterButton);
		buttonPanel.add(viewMeterButton);
		buttonPanel.add(deleteMeterButton);
		buttonPanel.add(printBillButton);
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(backButton);
		
		//Adds all elements to the Customer_Window and resets frame's content.
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		add(headerLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		add(phoneLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		add(emailLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		add(addressLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		add(balanceLabel, gbc);
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		add(editCustButton, gbc);
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
		gbc.gridwidth = 3;
		add(buttonPanel, gbc);
		frame.resetContent();
	}
	
	//Sets frame's content to Edit_Meter_Window with no selected meter.
	private class addMeterListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			frame.setContentPane(new Edit_Meter_Window(frame, -1, (int)custDataArray[1], (String)custDataArray[0]));
			try {
				stmt.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Sets frame's content to Charge_Window for the selected meter. If no valid row is selected, shows message instead.
	private class viewMeterListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int selectedRow = meterTable.getSelectedRow();
				if (selectedRow != -1) {
					int mNum = (int)meterTable.getValueAt(selectedRow, 0);
					frame.setContentPane(new Charge_Window(frame, mNum));
					stmt.close();
				}
				else {
					//If no row is selected, shows message.
					JOptionPane.showMessageDialog(null, "Select a meter to view.");
				}
			}
			catch (NullPointerException npe) {
				//If row is blank, shows message.
				JOptionPane.showMessageDialog(null, "Select a meter to view.");
			}
			catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Deletes selected meter after user confirms selection. Deletion cascades in the database, removing all charges associated 
	//with the meter.
	private class deleteMeterListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int selectedRow = meterTable.getSelectedRow();
				if (selectedRow != -1) {
					int mID = (int)meterTable.getValueAt(selectedRow, 0);
					int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete meter #" + mID + 
							" from customer " + custDataArray[0] + "? Any charges linked to this meter will also be deleted!", 
							"Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					//Only deletes meter if user selects YES in confirmation box.
					if (confirmation == 0) {
						try {
							stmt = frame.connection.createStatement();
							stmt.executeUpdate("DELETE FROM meter WHERE meterID = " + mID + ";");
							CallableStatement call = frame.connection.prepareCall("{call calculate_customer_balance(?)}");
							call.setInt(1, (int)custDataArray[1]);
							call.execute();
							call.close();
							frame.connection.commit();
							stmt.close();
						} catch (SQLException sqle) {
							JOptionPane.showMessageDialog(null, sqle.getMessage());
						}
						//Resets Customer_Window with same customer and with meter deletion complete.
						frame.setContentPane(new Meter_Window(frame, (int)custDataArray[1]));
					}
				}
				else {
					//If no row is selected, shows message.
					JOptionPane.showMessageDialog(null, "No meter selected.");
				}
			}
			catch (NullPointerException npe) {
				//If row is blank, shows message.
				JOptionPane.showMessageDialog(null, "No meter selected.");
			}
		}
	}
	
	private class printPDFListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int confirmation = JOptionPane.showConfirmDialog(null, "Create a PDF for customer " + custDataArray[0] + "?", 
					"Confirm PDF Creation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			//Only prints PDF if user selects YES in confirmation box.
			if (confirmation == 0) {
				try {
					//Calls PDF_Printer.writeCustomerPDF(), sending the Container in getContentPane(), which is this Meter_Window, as the parameter.
					writeCustomerPDF();
					JOptionPane.showMessageDialog(null, "Document Created!");
				}
				//If error occurs in PDF_Printer.writeCustomerPDF(), shows message detailing what went wrong.
				catch (FileNotFoundException | DocumentException de) {
					JOptionPane.showMessageDialog(null, "Document creation failed!\n" + de.getMessage());
				}
			}
		}
	}
	
	//Sets frame's content back to Customer_Window.
	private class backListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			frame.setContentPane(new Customer_Window(frame, "", -1));
			try {
				stmt.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	//Sets frame's content to Edit_Customer_Window, passing the accountNumber.
	private class editCustListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			frame.setContentPane(new Edit_Customer_Window(frame, (int)custDataArray[1]));
			try {
				stmt.close();
			} catch (SQLException sqle) {
				JOptionPane.showMessageDialog(null, sqle.getMessage());
			}
		}
	}
	
	/**
	 * Method that creates a PDF listing the information of the customer focused on customer with accNum, including their 
	 * personal information, the meters associated with them, the charges in each meter, and the amount owed for energy 
	 * to the end user.
	 */
	private static void writeCustomerPDF() throws FileNotFoundException, DocumentException {
		//Font templates for paragraphs.
		final Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD);
		final Font infoFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
		final Font header1Font = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
		final Font header2Font = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
	
		//MySQL query setup
		final String countMeterQuery = "SELECT COUNT(*) FROM meter WHERE custID = ";
		final String countChargeQuery = "SELECT COUNT(*) FROM charge WHERE metID = ";
		final String meterQueryBase = "SELECT * FROM meter WHERE custID = ";
		final String chargeQueryBase = "SELECT * FROM charge WHERE metID = ";
		
		//Opens document containing customer's account number and current date.
        Document document = new Document();
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy");
        String fileName = "Customer_" + custDataArray[1].toString() + "_" + dateFormat.format(date) + ".pdf";
        //Sets up Statements and ResultSets to retrieve data from the MySQL database, as well as some integers for later.
        ResultSet countResults = null, meterResults = null, chargeResults = null;
        Statement countStmt = null, meterStmt = null, chargeStmt = null;
        int numOfMeters = -1, numOfCharges = -1, currentMeter = -1;

        try {
        	//Each Statement uses the MySQL Connection from GUI_Window linked to inputFrame and tracks is linked to one of the ResultSets.
        	countStmt = frame.connection.createStatement();
        	meterStmt = frame.connection.createStatement();
        	chargeStmt = frame.connection.createStatement();
        	//countResults and countStmt get total number of meters attached to this account and stores it in numOfMeters.
        	countResults = countStmt.executeQuery(countMeterQuery + custDataArray[1].toString() + ";");
        	countResults.next();
        	numOfMeters = countResults.getInt(1);
        	//meterResults and meterStmt get ResultSet containing info on all meters connected to account stored in custDataArray.
        	meterResults = meterStmt.executeQuery(meterQueryBase + custDataArray[1].toString() + ";");
        }
        catch (SQLException sqle) {
        	JOptionPane.showMessageDialog(null, sqle.getMessage());
        }
        
        //Open document with name created above.
        PdfWriter.getInstance(document, new FileOutputStream(new File(fileName)));
        
        document.open();
        
        document.addTitle("Report for Customer #" + custDataArray[1].toString() + ": " + 
        		custDataArray[0].toString() + " (" + dateFormat.format(date).replace("_", "/") + ")");

        //Inserts Paragraphs starting from top of new document. Below is Paragraph detailing customer account number and name.
        Paragraph nextParagraph = new Paragraph("Customer #" + custDataArray[1].toString() + ": " + 
        		custDataArray[0].toString(), titleFont);
        nextParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(nextParagraph);
        
        //Date of invoice creation.
        nextParagraph = new Paragraph("Energy Invoice, Created " + dateFormat.format(date).replace("_", "/"), header1Font);
        nextParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(nextParagraph);
        
        //Total outstanding balance across all meters for this customer.
        nextParagraph = new Paragraph("Outstanding Balance: $" + custDataArray[9].toString(), header1Font);
        nextParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(nextParagraph);

        nextParagraph = new Paragraph("Customer Info:", header1Font);
        nextParagraph.setAlignment(Element.ALIGN_LEFT);
        document.add(nextParagraph);
        document.add(new Paragraph(" "));

        //Creates a PdfPTable with customer information and places it in document.
        PdfPTable custInfoTable = new PdfPTable(2);
        PdfPCell tempCell = new PdfPCell(new Phrase("Phone Number:", header2Font));
        custInfoTable.addCell(tempCell);
        tempCell = new PdfPCell(new Phrase(custDataArray[2].toString(), infoFont));
        custInfoTable.addCell(tempCell);
        tempCell = new PdfPCell(new Phrase("Email Address:", header2Font));
        custInfoTable.addCell(tempCell);
        tempCell = new PdfPCell(new Phrase(custDataArray[3].toString(), infoFont));
        custInfoTable.addCell(tempCell);
        tempCell = new PdfPCell(new Phrase("Saved Address:", header2Font));
        custInfoTable.addCell(tempCell);
        String fullAddress = custDataArray[4].toString() + ", " + custDataArray[5].toString() + ", ";
		if (custDataArray[6] != null) {
			fullAddress += custDataArray[6].toString() + ", ";
		}
		fullAddress += custDataArray[7].toString();
		if (custDataArray[8] != null) {
			fullAddress += " " + custDataArray[8].toString();
		}
        tempCell = new PdfPCell(new Phrase(fullAddress, infoFont));
        custInfoTable.addCell(tempCell);
        document.add(custInfoTable);
        document.add(new Phrase(" "));
        
        //If no meters are attached to this account, outputs this paragraph and skips to closing the document.
        if (numOfMeters == 0) {
        	nextParagraph = new Paragraph("No meters.", header1Font);
        	nextParagraph.setAlignment(Element.ALIGN_CENTER);
        	document.add(nextParagraph);
        }
        else {
        	try {
        		//If there is at least one meter, iterates over meterResults and outputs the meterID, EnergyType, and currently listed Tariff.
        		while (meterResults.next()) {
        			//Store meterID of meter currently being examined.
        			currentMeter = meterResults.getInt("MeterID");
        			nextParagraph = new Paragraph("Meter #" + meterResults.getInt("MeterID") + ": " + meterResults.getString("EnergyType")
        					+ " (Current Tariff: $" + meterResults.getBigDecimal("Tariff") + ")", header1Font);
        			nextParagraph.setAlignment(Element.ALIGN_LEFT);
        			document.add(nextParagraph);
        			
        			//countResults and countStmt gets number of charges linked to current peter.
        			countResults = countStmt.executeQuery(countChargeQuery + currentMeter + ";");
        			countResults.next();
        			numOfCharges = countResults.getInt(1);
        			//If meter has no charges, outputs this paragraph and moves back to beginning of loop.
        			if (numOfCharges == 0) {
        				nextParagraph = new Paragraph("No charges.", header2Font);
        	        	nextParagraph.setAlignment(Element.ALIGN_CENTER);
        	        	document.add(nextParagraph);
        			}
        			else {
        				//If charges exist for this meter, chargeResults and chargeStmt get ResultSet containing info on all 
        				//charges connected to current meter, in descending order by ChargeID.
        				document.add(new Paragraph(" "));
        				chargeResults = chargeStmt.executeQuery(chargeQueryBase + currentMeter + " ORDER BY ChargeID DESC;");
        				//Creates PdfPTable detailing each charge connected to current meter. The below cells create the header row.
        				PdfPTable chargeTable = new PdfPTable(6);
        				tempCell = new PdfPCell(new Phrase("Charge ID", header2Font));
        				chargeTable.addCell(tempCell);
        				tempCell = new PdfPCell(new Phrase("Amount", header2Font));
        				chargeTable.addCell(tempCell);
        				tempCell = new PdfPCell(new Phrase("Tariff", header2Font));
        				chargeTable.addCell(tempCell);
        				tempCell = new PdfPCell(new Phrase("Cost", header2Font));
        				chargeTable.addCell(tempCell);
        				tempCell = new PdfPCell(new Phrase("Date", header2Font));
        				chargeTable.addCell(tempCell);
        				tempCell = new PdfPCell(new Phrase("Paid", header2Font));
        				chargeTable.addCell(tempCell);
        				//For each charge in chargeResults, put each value into a cell and add it to table.
        				while (chargeResults.next()) {
        					tempCell = new PdfPCell(new Phrase(String.valueOf(chargeResults.getInt("ChargeID")), infoFont));
        					chargeTable.addCell(tempCell);
        					tempCell = new PdfPCell(new Phrase(String.valueOf(chargeResults.getBigDecimal("ChargeAmount")), infoFont));
        					chargeTable.addCell(tempCell);
        					tempCell = new PdfPCell(new Phrase(String.valueOf("$" + chargeResults.getBigDecimal("AppliedTariff")), infoFont));
        					chargeTable.addCell(tempCell);
        					tempCell = new PdfPCell(new Phrase(String.valueOf("$" + chargeResults.getBigDecimal("Cost")), infoFont));
        					chargeTable.addCell(tempCell);
        					tempCell = new PdfPCell(new Phrase(String.valueOf(chargeResults.getDate("ChargeDate")), infoFont));
        					chargeTable.addCell(tempCell);
        					//Translates the Boolean variable in ResultSet into "Paid" or "Unpaid";
        					if (chargeResults.getBoolean("Paid") == true) {
        						tempCell = new PdfPCell(new Phrase("Paid", infoFont));
        					}
        					else {
        						tempCell = new PdfPCell(new Phrase("Unpaid", infoFont));
        					}
        					chargeTable.addCell(tempCell);
        				}
        				
        				//Adds table to document, then add total usage and outstanding balance for this meter.
        				document.add(chargeTable);
        				nextParagraph = new Paragraph(new Phrase("Total Usage: " + meterResults.getBigDecimal("EnergyUsage"), infoFont));
        				nextParagraph.setAlignment(Element.ALIGN_RIGHT);
        				document.add(nextParagraph);
        				nextParagraph = new Paragraph(new Phrase("Outstanding Balance: $" + meterResults.getBigDecimal("Balance"), header2Font));
        				nextParagraph.setAlignment(Element.ALIGN_RIGHT);
        				document.add(nextParagraph);
        			}
        			
        		}
        		//When all meters are accounted for, document creation is done. Close Statements.
        		countStmt.close();
        		meterStmt.close();
        		chargeStmt.close();
        	}
        	catch (SQLException sqle) {
        		JOptionPane.showMessageDialog(null, sqle.getMessage());
        	}
        	
        }

        //Close document.
        document.close();

        System.out.println("Done with " + fileName);
    }
}
