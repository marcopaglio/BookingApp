package io.github.marcopaglio.booking.view.swing;

import java.awt.EventQueue;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.view.BookingView;
import java.awt.Window.Type;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.SwingConstants;

public class BookingSwingView extends JFrame implements BookingView {

	private JPanel contentPane;
	private JTextField nameFormTxt;
	private JButton addClientBtn;
	private JLabel dateLbl;
	private JTextField dateFormTxt;
	private JButton addReservationBtn;
	private JLabel lastNameLbl;
	private JTextField surnameFormTxt;
	private JButton renameBtn;
	private JButton rescheduleBtn;
	private JLabel formErrorMsgLbl;
	private JButton removeClientBtn;
	private JButton removeReservationBtn;
	private JLabel clientErrorMsgLbl;
	private JLabel reservationErrorMsgLbl;
	private JScrollPane clientScrollPane;
	private JList clientList;
	private JScrollPane reservationScrollPane;
	private JList reservationList;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BookingSwingView frame = new BookingSwingView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BookingSwingView() {
		setTitle("BookingApp");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		// events
		final KeyAdapter clientBtnEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				addClientBtn.setEnabled(
					!nameFormTxt.getText().trim().isEmpty() &&
					!surnameFormTxt.getText().trim().isEmpty()
				);
			}
		};
		
		// First row
		JLabel firstNameLbl = new JLabel("First Name");
		firstNameLbl.setName("firstNameLbl");
		GridBagConstraints gbc_firstNameLbl = new GridBagConstraints();
		gbc_firstNameLbl.anchor = GridBagConstraints.EAST;
		gbc_firstNameLbl.insets = new Insets(0, 0, 5, 5);
		gbc_firstNameLbl.gridx = 0;
		gbc_firstNameLbl.gridy = 0;
		contentPane.add(firstNameLbl, gbc_firstNameLbl);
		
		nameFormTxt = new JTextField();
		nameFormTxt.addKeyListener(clientBtnEnabler);
		nameFormTxt.setName("nameFormTxt");
		nameFormTxt.setToolTipText("Names must not contain numbers (e.g. 0-9) or any type of symbol or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		GridBagConstraints gbc_nameFormTxt = new GridBagConstraints();
		gbc_nameFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameFormTxt.insets = new Insets(0, 0, 5, 5);
		gbc_nameFormTxt.gridx = 1;
		gbc_nameFormTxt.gridy = 0;
		contentPane.add(nameFormTxt, gbc_nameFormTxt);
		nameFormTxt.setColumns(10);
		
		addClientBtn = new JButton("Add Client");
		addClientBtn.setEnabled(false);
		addClientBtn.setName("addClientBtn");
		addClientBtn.setToolTipText("");
		GridBagConstraints gbc_addClientBtn = new GridBagConstraints();
		gbc_addClientBtn.insets = new Insets(0, 0, 5, 5);
		gbc_addClientBtn.gridx = 2;
		gbc_addClientBtn.gridy = 0;
		contentPane.add(addClientBtn, gbc_addClientBtn);
		
		dateLbl = new JLabel("Date");
		dateLbl.setName("dateLbl");
		GridBagConstraints gbc_dateLbl = new GridBagConstraints();
		gbc_dateLbl.anchor = GridBagConstraints.EAST;
		gbc_dateLbl.insets = new Insets(0, 0, 5, 5);
		gbc_dateLbl.gridx = 3;
		gbc_dateLbl.gridy = 0;
		contentPane.add(dateLbl, gbc_dateLbl);
		
		dateFormTxt = new JTextField();
		dateFormTxt.setToolTipText("Dates must be in format aaaa-mm-dd (e.g. 2022-04-24)");
		dateFormTxt.setName("dateFormTxt");
		GridBagConstraints gbc_dateFormTxt = new GridBagConstraints();
		gbc_dateFormTxt.insets = new Insets(0, 0, 5, 5);
		gbc_dateFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateFormTxt.gridx = 4;
		gbc_dateFormTxt.gridy = 0;
		contentPane.add(dateFormTxt, gbc_dateFormTxt);
		dateFormTxt.setColumns(10);
		
		addReservationBtn = new JButton("Add Reservation");
		addReservationBtn.setEnabled(false);
		addReservationBtn.setName("addReservationBtn");
		GridBagConstraints gbc_addReservationBtn = new GridBagConstraints();
		gbc_addReservationBtn.insets = new Insets(0, 0, 5, 0);
		gbc_addReservationBtn.gridx = 5;
		gbc_addReservationBtn.gridy = 0;
		contentPane.add(addReservationBtn, gbc_addReservationBtn);
		
		// Second row
		lastNameLbl = new JLabel("Last Name");
		lastNameLbl.setVerticalAlignment(SwingConstants.BOTTOM);
		lastNameLbl.setName("lastNameLbl");
		GridBagConstraints gbc_lastNameLbl = new GridBagConstraints();
		gbc_lastNameLbl.anchor = GridBagConstraints.EAST;
		gbc_lastNameLbl.insets = new Insets(0, 0, 5, 5);
		gbc_lastNameLbl.gridx = 0;
		gbc_lastNameLbl.gridy = 1;
		contentPane.add(lastNameLbl, gbc_lastNameLbl);
		
		surnameFormTxt = new JTextField();
		surnameFormTxt.addKeyListener(clientBtnEnabler);
		surnameFormTxt.setToolTipText("Surnames must not contain numbers (e.g. 0-9) or any type of symbol or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		surnameFormTxt.setName("surnameFormTxt");
		GridBagConstraints gbc_surnameFormTxt = new GridBagConstraints();
		gbc_surnameFormTxt.insets = new Insets(0, 0, 5, 5);
		gbc_surnameFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbc_surnameFormTxt.gridx = 1;
		gbc_surnameFormTxt.gridy = 1;
		contentPane.add(surnameFormTxt, gbc_surnameFormTxt);
		surnameFormTxt.setColumns(10);
		
		renameBtn = new JButton("Rename");
		renameBtn.setEnabled(false);
		renameBtn.setName("renameBtn");
		GridBagConstraints gbc_renameBtn = new GridBagConstraints();
		gbc_renameBtn.insets = new Insets(0, 0, 5, 5);
		gbc_renameBtn.gridx = 2;
		gbc_renameBtn.gridy = 1;
		contentPane.add(renameBtn, gbc_renameBtn);
		
		rescheduleBtn = new JButton("Reschedule");
		rescheduleBtn.setName("rescheduleBtn");
		rescheduleBtn.setEnabled(false);
		GridBagConstraints gbc_rescheduleBtn = new GridBagConstraints();
		gbc_rescheduleBtn.insets = new Insets(0, 0, 5, 0);
		gbc_rescheduleBtn.gridx = 5;
		gbc_rescheduleBtn.gridy = 1;
		contentPane.add(rescheduleBtn, gbc_rescheduleBtn);
		
		// Third row
		formErrorMsgLbl = new JLabel(" ");
		formErrorMsgLbl.setName("formErrorMsgLbl");
		GridBagConstraints gbc_formErrorMsgLbl = new GridBagConstraints();
		gbc_formErrorMsgLbl.insets = new Insets(0, 0, 5, 0);
		gbc_formErrorMsgLbl.gridwidth = 6;
		gbc_formErrorMsgLbl.gridx = 0;
		gbc_formErrorMsgLbl.gridy = 2;
		contentPane.add(formErrorMsgLbl, gbc_formErrorMsgLbl);
		
		// Fourth row
		clientScrollPane = new JScrollPane();
		clientScrollPane.setName("clientScrollPane");
		GridBagConstraints gbc_clientScrollPane = new GridBagConstraints();
		gbc_clientScrollPane.gridwidth = 3;
		gbc_clientScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_clientScrollPane.fill = GridBagConstraints.BOTH;
		gbc_clientScrollPane.gridx = 0;
		gbc_clientScrollPane.gridy = 3;
		contentPane.add(clientScrollPane, gbc_clientScrollPane);
		
		clientList = new JList();
		clientList.setName("clientList");
		clientScrollPane.setViewportView(clientList);
		
		reservationScrollPane = new JScrollPane();
		reservationScrollPane.setName("reservationScrollPane");
		GridBagConstraints gbc_reservationScrollPane = new GridBagConstraints();
		gbc_reservationScrollPane.gridwidth = 3;
		gbc_reservationScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_reservationScrollPane.fill = GridBagConstraints.BOTH;
		gbc_reservationScrollPane.gridx = 3;
		gbc_reservationScrollPane.gridy = 3;
		contentPane.add(reservationScrollPane, gbc_reservationScrollPane);
		
		reservationList = new JList();
		reservationList.setName("reservationList");
		reservationScrollPane.setViewportView(reservationList);
		
		// Fifth row
		removeClientBtn = new JButton("Remove Client");
		removeClientBtn.setEnabled(false);
		removeClientBtn.setName("removeClientBtn");
		GridBagConstraints gbc_removeClientBtn = new GridBagConstraints();
		gbc_removeClientBtn.insets = new Insets(0, 0, 5, 5);
		gbc_removeClientBtn.gridx = 2;
		gbc_removeClientBtn.gridy = 4;
		contentPane.add(removeClientBtn, gbc_removeClientBtn);
		
		removeReservationBtn = new JButton("Remove Reservation");
		removeReservationBtn.setName("removeReservationBtn");
		removeReservationBtn.setEnabled(false);
		GridBagConstraints gbc_removeReservationBtn = new GridBagConstraints();
		gbc_removeReservationBtn.insets = new Insets(0, 0, 5, 0);
		gbc_removeReservationBtn.gridx = 5;
		gbc_removeReservationBtn.gridy = 4;
		contentPane.add(removeReservationBtn, gbc_removeReservationBtn);
		
		// Sixth row
		clientErrorMsgLbl = new JLabel(" ");
		clientErrorMsgLbl.setName("clientErrorMsgLbl");
		GridBagConstraints gbc_clientErrorMsgLbl = new GridBagConstraints();
		gbc_clientErrorMsgLbl.gridwidth = 3;
		gbc_clientErrorMsgLbl.insets = new Insets(0, 0, 0, 5);
		gbc_clientErrorMsgLbl.gridx = 0;
		gbc_clientErrorMsgLbl.gridy = 5;
		contentPane.add(clientErrorMsgLbl, gbc_clientErrorMsgLbl);
		
		reservationErrorMsgLbl = new JLabel(" ");
		reservationErrorMsgLbl.setName("reservationErrorMsgLbl");
		GridBagConstraints gbc_reservationErrorMsgLbl = new GridBagConstraints();
		gbc_reservationErrorMsgLbl.gridwidth = 3;
		gbc_reservationErrorMsgLbl.gridx = 3;
		gbc_reservationErrorMsgLbl.gridy = 5;
		contentPane.add(reservationErrorMsgLbl, gbc_reservationErrorMsgLbl);
	}

	@Override
	public void showAllClients(List<Client> clients) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showAllReservations(List<Reservation> reservations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reservationAdded(Reservation reservation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientAdded(Client client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reservationRemoved(Reservation reservation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientRemoved(Client client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientRenamed(Client renamedClient) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reservationRescheduled(Reservation rescheduleReservation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showReservationError(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showClientError(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showFormError(String message) {
		// TODO Auto-generated method stub
		
	}

}
