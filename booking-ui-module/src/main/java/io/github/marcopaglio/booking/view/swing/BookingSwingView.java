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

public class BookingSwingView extends JFrame implements BookingView {

	private JPanel contentPane;
	private JTextField nameInputTextBox;
	private JButton addClientButton;
	private JLabel dateLabel;
	private JTextField dateInputTextBox;
	private JButton addReservationButton;
	private JLabel surnameLabel;
	private JTextField surnameInputTextBox;
	private JButton renameButton;
	private JButton rescheduleButton;
	private JLabel formErrorMessageLabel;
	private JButton removeClientButton;
	private JButton removeReservationButton;
	private JLabel clientErrorMessageLabel;
	private JLabel reservationErrorMessageLabel;
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
		
		JLabel nameLabel = new JLabel("Name");
		nameLabel.setName("nameLabel");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.anchor = GridBagConstraints.EAST;
		gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nameLabel.gridx = 0;
		gbc_nameLabel.gridy = 0;
		contentPane.add(nameLabel, gbc_nameLabel);
		
		nameInputTextBox = new JTextField();
		nameInputTextBox.setName("nameInputTextBox");
		nameInputTextBox.setToolTipText("Names must not contain numbers (e.g. 0-9) or any type of symbol or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		GridBagConstraints gbc_nameInputTextBox = new GridBagConstraints();
		gbc_nameInputTextBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameInputTextBox.insets = new Insets(0, 0, 5, 5);
		gbc_nameInputTextBox.gridx = 1;
		gbc_nameInputTextBox.gridy = 0;
		contentPane.add(nameInputTextBox, gbc_nameInputTextBox);
		nameInputTextBox.setColumns(10);
		
		addClientButton = new JButton("Add Client");
		addClientButton.setEnabled(false);
		addClientButton.setName("addClientButton");
		addClientButton.setToolTipText("");
		GridBagConstraints gbc_addClientButton = new GridBagConstraints();
		gbc_addClientButton.insets = new Insets(0, 0, 5, 5);
		gbc_addClientButton.gridx = 2;
		gbc_addClientButton.gridy = 0;
		contentPane.add(addClientButton, gbc_addClientButton);
		
		dateLabel = new JLabel("Date");
		dateLabel.setName("dateLabel");
		GridBagConstraints gbc_dateLabel = new GridBagConstraints();
		gbc_dateLabel.anchor = GridBagConstraints.EAST;
		gbc_dateLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dateLabel.gridx = 3;
		gbc_dateLabel.gridy = 0;
		contentPane.add(dateLabel, gbc_dateLabel);
		
		dateInputTextBox = new JTextField();
		dateInputTextBox.setToolTipText("Dates must be in format aaaa-mm-dd (e.g. 2022-04-24)");
		dateInputTextBox.setName("dateInputTextBox");
		GridBagConstraints gbc_dateInputTextBox = new GridBagConstraints();
		gbc_dateInputTextBox.insets = new Insets(0, 0, 5, 5);
		gbc_dateInputTextBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateInputTextBox.gridx = 4;
		gbc_dateInputTextBox.gridy = 0;
		contentPane.add(dateInputTextBox, gbc_dateInputTextBox);
		dateInputTextBox.setColumns(10);
		
		addReservationButton = new JButton("Add Reservation");
		addReservationButton.setEnabled(false);
		addReservationButton.setName("addReservationButton");
		GridBagConstraints gbc_addReservationButton = new GridBagConstraints();
		gbc_addReservationButton.insets = new Insets(0, 0, 5, 0);
		gbc_addReservationButton.gridx = 5;
		gbc_addReservationButton.gridy = 0;
		contentPane.add(addReservationButton, gbc_addReservationButton);
		
		surnameLabel = new JLabel("Surname");
		surnameLabel.setName("surnameLabel");
		GridBagConstraints gbc_surnameLabel = new GridBagConstraints();
		gbc_surnameLabel.anchor = GridBagConstraints.EAST;
		gbc_surnameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_surnameLabel.gridx = 0;
		gbc_surnameLabel.gridy = 1;
		contentPane.add(surnameLabel, gbc_surnameLabel);
		
		surnameInputTextBox = new JTextField();
		surnameInputTextBox.setToolTipText("Surnames must not contain numbers (e.g. 0-9) or any type of symbol or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		surnameInputTextBox.setName("surnameInputTextBox");
		GridBagConstraints gbc_surnameInputTextBox = new GridBagConstraints();
		gbc_surnameInputTextBox.insets = new Insets(0, 0, 5, 5);
		gbc_surnameInputTextBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_surnameInputTextBox.gridx = 1;
		gbc_surnameInputTextBox.gridy = 1;
		contentPane.add(surnameInputTextBox, gbc_surnameInputTextBox);
		surnameInputTextBox.setColumns(10);
		
		renameButton = new JButton("Rename");
		renameButton.setEnabled(false);
		renameButton.setName("renameButton");
		GridBagConstraints gbc_renameButton = new GridBagConstraints();
		gbc_renameButton.insets = new Insets(0, 0, 5, 5);
		gbc_renameButton.gridx = 2;
		gbc_renameButton.gridy = 1;
		contentPane.add(renameButton, gbc_renameButton);
		
		rescheduleButton = new JButton("Reschedule");
		rescheduleButton.setName("rescheduleButton");
		rescheduleButton.setEnabled(false);
		GridBagConstraints gbc_rescheduleButton = new GridBagConstraints();
		gbc_rescheduleButton.insets = new Insets(0, 0, 5, 0);
		gbc_rescheduleButton.gridx = 5;
		gbc_rescheduleButton.gridy = 1;
		contentPane.add(rescheduleButton, gbc_rescheduleButton);
		
		formErrorMessageLabel = new JLabel(" ");
		formErrorMessageLabel.setName("formErrorMessageLabel");
		GridBagConstraints gbc_formErrorMessageLabel = new GridBagConstraints();
		gbc_formErrorMessageLabel.insets = new Insets(0, 0, 5, 0);
		gbc_formErrorMessageLabel.gridwidth = 6;
		gbc_formErrorMessageLabel.gridx = 0;
		gbc_formErrorMessageLabel.gridy = 2;
		contentPane.add(formErrorMessageLabel, gbc_formErrorMessageLabel);
		
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
		
		removeClientButton = new JButton("Remove Client");
		removeClientButton.setEnabled(false);
		removeClientButton.setName("removeClientButton");
		GridBagConstraints gbc_removeClientButton = new GridBagConstraints();
		gbc_removeClientButton.insets = new Insets(0, 0, 5, 5);
		gbc_removeClientButton.gridx = 2;
		gbc_removeClientButton.gridy = 4;
		contentPane.add(removeClientButton, gbc_removeClientButton);
		
		removeReservationButton = new JButton("Remove Reservation");
		removeReservationButton.setName("removeReservationButton");
		removeReservationButton.setEnabled(false);
		GridBagConstraints gbc_removeReservationButton = new GridBagConstraints();
		gbc_removeReservationButton.insets = new Insets(0, 0, 5, 0);
		gbc_removeReservationButton.gridx = 5;
		gbc_removeReservationButton.gridy = 4;
		contentPane.add(removeReservationButton, gbc_removeReservationButton);
		
		clientErrorMessageLabel = new JLabel(" ");
		clientErrorMessageLabel.setName("clientErrorMessageLabel");
		GridBagConstraints gbc_clientErrorMessageLabel = new GridBagConstraints();
		gbc_clientErrorMessageLabel.gridwidth = 3;
		gbc_clientErrorMessageLabel.insets = new Insets(0, 0, 0, 5);
		gbc_clientErrorMessageLabel.gridx = 0;
		gbc_clientErrorMessageLabel.gridy = 5;
		contentPane.add(clientErrorMessageLabel, gbc_clientErrorMessageLabel);
		
		reservationErrorMessageLabel = new JLabel(" ");
		reservationErrorMessageLabel.setName("reservationErrorMessageLabel");
		GridBagConstraints gbc_reservationErrorMessageLabel = new GridBagConstraints();
		gbc_reservationErrorMessageLabel.gridwidth = 3;
		gbc_reservationErrorMessageLabel.gridx = 3;
		gbc_reservationErrorMessageLabel.gridy = 5;
		contentPane.add(reservationErrorMessageLabel, gbc_reservationErrorMessageLabel);
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
