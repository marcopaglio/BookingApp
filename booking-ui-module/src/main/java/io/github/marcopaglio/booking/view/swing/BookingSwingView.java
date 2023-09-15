package io.github.marcopaglio.booking.view.swing;

import java.awt.EventQueue;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import io.github.marcopaglio.booking.annotation.Generated;
import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.presenter.BookingPresenter;
import io.github.marcopaglio.booking.view.BookingView;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Insets;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.SwingConstants;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class BookingSwingView extends JFrame implements BookingView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BookingPresenter setBookingPresenter;

	private JPanel contentPane;
	private JTextField nameFormTxt;
	private JButton addClientBtn;
	private JLabel dateLbl;
	private JTextField yearFormTxt;
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
	private JList<Client> clientList;
	private DefaultListModel<Client> clientListModel;
	private JScrollPane reservationScrollPane;
	private JList<Reservation> reservationList;
	private DefaultListModel<Reservation> reservationListModel;
	private JLabel dash1Lbl;
	private JTextField dayFormTxt;
	private JLabel dash2Lbl;
	private JTextField monthFormTxt;

	// EVENT HANDLERS

	private final KeyAdapter clientBtnEnabler = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			addClientBtn.setEnabled(
				!nameFormTxt.getText().trim().isEmpty() &&
				!surnameFormTxt.getText().trim().isEmpty()
			);
			
			renameBtn.setEnabled(
				!nameFormTxt.getText().trim().isEmpty() &&
				!surnameFormTxt.getText().trim().isEmpty() &&
				clientList.getSelectedIndex() != -1
			);
		}
	};

	private final KeyAdapter reservationBtnEnabler = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			addReservationBtn.setEnabled(
				!yearFormTxt.getText().trim().isEmpty() &&
				!monthFormTxt.getText().trim().isEmpty() &&
				!dayFormTxt.getText().trim().isEmpty() &&
				clientList.getSelectedIndex() != -1
			);
			
			rescheduleBtn.setEnabled(
				!yearFormTxt.getText().trim().isEmpty() &&
				!monthFormTxt.getText().trim().isEmpty() &&
				!dayFormTxt.getText().trim().isEmpty() &&
				reservationList.getSelectedIndex() != -1
			);
		}
	};

	private final ListSelectionListener clientListListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			renameBtn.setEnabled(
				!nameFormTxt.getText().trim().isEmpty() &&
				!surnameFormTxt.getText().trim().isEmpty() &&
				clientList.getSelectedIndex() != -1
			);
			
			removeClientBtn.setEnabled(clientList.getSelectedIndex() != -1);
			
			addReservationBtn.setEnabled(
				!yearFormTxt.getText().trim().isEmpty() &&
				!monthFormTxt.getText().trim().isEmpty() &&
				!dayFormTxt.getText().trim().isEmpty() &&
				clientList.getSelectedIndex() != -1
			);
		}
	};

	private final ListSelectionListener reservationListListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			rescheduleBtn.setEnabled(
				!yearFormTxt.getText().trim().isEmpty() &&
				!monthFormTxt.getText().trim().isEmpty() &&
				!dayFormTxt.getText().trim().isEmpty() &&
				reservationList.getSelectedIndex() != -1
			);
			
			removeReservationBtn.setEnabled(reservationList.getSelectedIndex() != -1);
		}
	};

	// METHODS

	public void setBookingPresenter(BookingPresenter bookingPresenter) {
		this.setBookingPresenter = bookingPresenter;
	}

	/**
	 * @return the rescheduleBtn
	 */
	JButton getRescheduleBtn() {
		return rescheduleBtn;
	}

	/**
	 * @return the removeReservationBtn
	 */
	JButton getRemoveReservationBtn() {
		return removeReservationBtn;
	}

	/**
	 * @return the addReservationBtn
	 */
	@Generated
	JButton getAddReservationBtn() {
		return addReservationBtn;
	}

	/**
	 * @return the renameBtn
	 */
	@Generated
	JButton getRenameBtn() {
		return renameBtn;
	}

	/**
	 * @return the removeClientBtn
	 */
	@Generated
	JButton getRemoveClientBtn() {
		return removeClientBtn;
	}

	/**
	 * @return the clientListModel
	 */
	@Generated
	DefaultListModel<Client> getClientListModel() {
		return clientListModel;
	}

	/**
	 * @return the reservationListModel
	 */
	@Generated
	DefaultListModel<Reservation> getReservationListModel() {
		return reservationListModel;
	}

	/**
	 * Displays the clients of the given list on the graphical user interface through Swing.
	 * Additionally, this method resets the client list selection and disables all buttons
	 * that fire when a client is selected in the list.
	 * 
	 * @param clients	the {@code List} of clients to show.
	 */
	@Override
	public void showAllClients(List<Client> clients) {
		clientListModel.removeAllElements();
		clients.stream().forEach(clientListModel::addElement);
		
		clientList.clearSelection();
		addReservationBtn.setEnabled(false);
		renameBtn.setEnabled(false);
		removeClientBtn.setEnabled(false);
	}

	/**
	 * Displays the reservations of the given list on the user interface through Swing.
	 * Additionally, this method resets the reservation list selection and disables all buttons
	 * that fire when a reservation is selected in the list.
	 * 
	 * @param reservations	the {@code List} of reservations to show.
	 */
	@Override
	public void showAllReservations(List<Reservation> reservations) {
		reservationListModel.removeAllElements();
		reservations.stream().forEach(reservationListModel::addElement);
		
		reservationList.clearSelection();
		rescheduleBtn.setEnabled(false);
		removeReservationBtn.setEnabled(false);
	}

	/**
	 * Displays the reservation just inserted into the repository on the user interface
	 * through Swing.
	 * 
	 * @param reservation	the {@code Reservation} to show.
	 */
	@Override
	public void reservationAdded(Reservation reservation) {
		reservationListModel.addElement(reservation);
	}

	/**
	 * Displays the client just inserted into the repository on the user interface through Swing.
	 * 
	 * @param client	the {@code Client} to show.
	 */
	@Override
	public void clientAdded(Client client) {
		clientListModel.addElement(client);
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
		gbl_contentPane.columnWidths = new int[] {60, 40, 60, 40, 40, 40, 0, 20, 0, 20, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
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
		
		lastNameLbl = new JLabel("Last Name");
		lastNameLbl.setVerticalAlignment(SwingConstants.BOTTOM);
		lastNameLbl.setName("lastNameLbl");
		GridBagConstraints gbc_lastNameLbl = new GridBagConstraints();
		gbc_lastNameLbl.anchor = GridBagConstraints.EAST;
		gbc_lastNameLbl.insets = new Insets(0, 0, 5, 5);
		gbc_lastNameLbl.gridx = 2;
		gbc_lastNameLbl.gridy = 0;
		contentPane.add(lastNameLbl, gbc_lastNameLbl);
		
		surnameFormTxt = new JTextField();
		surnameFormTxt.addKeyListener(clientBtnEnabler);
		surnameFormTxt.setToolTipText("Surnames must not contain numbers (e.g. 0-9) or any type of symbol or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		surnameFormTxt.setName("surnameFormTxt");
		GridBagConstraints gbc_surnameFormTxt = new GridBagConstraints();
		gbc_surnameFormTxt.insets = new Insets(0, 0, 5, 5);
		gbc_surnameFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbc_surnameFormTxt.gridx = 3;
		gbc_surnameFormTxt.gridy = 0;
		contentPane.add(surnameFormTxt, gbc_surnameFormTxt);
		surnameFormTxt.setColumns(10);
		
		dateLbl = new JLabel("Date");
		dateLbl.setName("dateLbl");
		GridBagConstraints gbc_dateLbl = new GridBagConstraints();
		gbc_dateLbl.anchor = GridBagConstraints.EAST;
		gbc_dateLbl.insets = new Insets(0, 0, 5, 5);
		gbc_dateLbl.gridx = 4;
		gbc_dateLbl.gridy = 0;
		contentPane.add(dateLbl, gbc_dateLbl);
		
		yearFormTxt = new JTextField();
		yearFormTxt.addKeyListener(reservationBtnEnabler);
		yearFormTxt.setToolTipText("yyyy");
		yearFormTxt.setName("yearFormTxt");
		GridBagConstraints gbc_yearFormTxt = new GridBagConstraints();
		gbc_yearFormTxt.insets = new Insets(0, 0, 5, 5);
		gbc_yearFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbc_yearFormTxt.gridx = 5;
		gbc_yearFormTxt.gridy = 0;
		contentPane.add(yearFormTxt, gbc_yearFormTxt);
		yearFormTxt.setColumns(10);
		
		dash1Lbl = new JLabel("-");
		dash1Lbl.setName("dash1Lbl");
		dash1Lbl.setToolTipText("");
		GridBagConstraints gbc_dash1Lbl = new GridBagConstraints();
		gbc_dash1Lbl.anchor = GridBagConstraints.EAST;
		gbc_dash1Lbl.insets = new Insets(0, 0, 5, 5);
		gbc_dash1Lbl.gridx = 6;
		gbc_dash1Lbl.gridy = 0;
		contentPane.add(dash1Lbl, gbc_dash1Lbl);
		
		monthFormTxt = new JTextField();
		monthFormTxt.addKeyListener(reservationBtnEnabler);
		monthFormTxt.setName("monthFormTxt");
		monthFormTxt.setToolTipText("mm");
		GridBagConstraints gbc_monthFormTxt = new GridBagConstraints();
		gbc_monthFormTxt.insets = new Insets(0, 0, 5, 0);
		gbc_monthFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbc_monthFormTxt.gridx = 7;
		gbc_monthFormTxt.gridy = 0;
		contentPane.add(monthFormTxt, gbc_monthFormTxt);
		monthFormTxt.setColumns(10);
		
		dash2Lbl = new JLabel("-");
		dash2Lbl.setName("dash2Lbl");
		GridBagConstraints gbc_dash2Lbl = new GridBagConstraints();
		gbc_dash2Lbl.anchor = GridBagConstraints.EAST;
		gbc_dash2Lbl.insets = new Insets(0, 0, 5, 5);
		gbc_dash2Lbl.gridx = 8;
		gbc_dash2Lbl.gridy = 0;
		contentPane.add(dash2Lbl, gbc_dash2Lbl);
		
		dayFormTxt = new JTextField();
		dayFormTxt.addKeyListener(reservationBtnEnabler);
		dayFormTxt.setName("dayFormTxt");
		dayFormTxt.setToolTipText("dd");
		GridBagConstraints gbc_dayFormTxt = new GridBagConstraints();
		gbc_dayFormTxt.insets = new Insets(0, 0, 5, 5);
		gbc_dayFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbc_dayFormTxt.gridx = 9;
		gbc_dayFormTxt.gridy = 0;
		contentPane.add(dayFormTxt, gbc_dayFormTxt);
		dayFormTxt.setColumns(10);
		
		// Second row
		addClientBtn = new JButton("Add Client");
		addClientBtn.setEnabled(false);
		addClientBtn.setName("addClientBtn");
		addClientBtn.setToolTipText("");
		GridBagConstraints gbc_addClientBtn = new GridBagConstraints();
		gbc_addClientBtn.gridwidth = 2;
		gbc_addClientBtn.insets = new Insets(0, 0, 5, 5);
		gbc_addClientBtn.gridx = 0;
		gbc_addClientBtn.gridy = 1;
		contentPane.add(addClientBtn, gbc_addClientBtn);
		
		renameBtn = new JButton("Rename");
		renameBtn.setEnabled(false);
		renameBtn.setName("renameBtn");
		GridBagConstraints gbc_renameBtn = new GridBagConstraints();
		gbc_renameBtn.gridwidth = 2;
		gbc_renameBtn.insets = new Insets(0, 0, 5, 5);
		gbc_renameBtn.gridx = 2;
		gbc_renameBtn.gridy = 1;
		contentPane.add(renameBtn, gbc_renameBtn);
		
		addReservationBtn = new JButton("Add Reservation");
		addReservationBtn.setEnabled(false);
		addReservationBtn.setName("addReservationBtn");
		GridBagConstraints gbc_addReservationBtn = new GridBagConstraints();
		gbc_addReservationBtn.gridwidth = 2;
		gbc_addReservationBtn.insets = new Insets(0, 0, 5, 5);
		gbc_addReservationBtn.gridx = 4;
		gbc_addReservationBtn.gridy = 1;
		contentPane.add(addReservationBtn, gbc_addReservationBtn);
		
		rescheduleBtn = new JButton("Reschedule");
		rescheduleBtn.setName("rescheduleBtn");
		rescheduleBtn.setEnabled(false);
		GridBagConstraints gbc_rescheduleBtn = new GridBagConstraints();
		gbc_rescheduleBtn.gridwidth = 4;
		gbc_rescheduleBtn.insets = new Insets(0, 0, 5, 0);
		gbc_rescheduleBtn.gridx = 6;
		gbc_rescheduleBtn.gridy = 1;
		contentPane.add(rescheduleBtn, gbc_rescheduleBtn);
		
		// Third row
		formErrorMsgLbl = new JLabel(" ");
		formErrorMsgLbl.setName("formErrorMsgLbl");
		GridBagConstraints gbc_formErrorMsgLbl = new GridBagConstraints();
		gbc_formErrorMsgLbl.insets = new Insets(0, 0, 5, 5);
		gbc_formErrorMsgLbl.gridwidth = 10;
		gbc_formErrorMsgLbl.gridx = 0;
		gbc_formErrorMsgLbl.gridy = 2;
		contentPane.add(formErrorMsgLbl, gbc_formErrorMsgLbl);
		
		// Fourth row
		clientScrollPane = new JScrollPane();
		clientScrollPane.setName("clientScrollPane");
		GridBagConstraints gbc_clientScrollPane = new GridBagConstraints();
		gbc_clientScrollPane.gridwidth = 4;
		gbc_clientScrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_clientScrollPane.fill = GridBagConstraints.BOTH;
		gbc_clientScrollPane.gridx = 0;
		gbc_clientScrollPane.gridy = 3;
		contentPane.add(clientScrollPane, gbc_clientScrollPane);
		
		clientListModel = new DefaultListModel<>();
		clientList = new JList<>(clientListModel);
		clientList.addListSelectionListener(clientListListener);
		clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientList.setName("clientList");
		clientScrollPane.setViewportView(clientList);
		
		reservationScrollPane = new JScrollPane();
		reservationScrollPane.setName("reservationScrollPane");
		GridBagConstraints gbc_reservationScrollPane = new GridBagConstraints();
		gbc_reservationScrollPane.gridwidth = 6;
		gbc_reservationScrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_reservationScrollPane.fill = GridBagConstraints.BOTH;
		gbc_reservationScrollPane.gridx = 4;
		gbc_reservationScrollPane.gridy = 3;
		contentPane.add(reservationScrollPane, gbc_reservationScrollPane);
		
		reservationListModel = new DefaultListModel<>();
		reservationList = new JList<>(reservationListModel);
		reservationList.addListSelectionListener(reservationListListener);
		reservationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reservationList.setName("reservationList");
		reservationScrollPane.setViewportView(reservationList);
		
		// Fifth row
		removeClientBtn = new JButton("Remove Client");
		removeClientBtn.setEnabled(false);
		removeClientBtn.setName("removeClientBtn");
		GridBagConstraints gbc_removeClientBtn = new GridBagConstraints();
		gbc_removeClientBtn.gridwidth = 4;
		gbc_removeClientBtn.insets = new Insets(0, 0, 5, 5);
		gbc_removeClientBtn.gridx = 0;
		gbc_removeClientBtn.gridy = 4;
		contentPane.add(removeClientBtn, gbc_removeClientBtn);
		
		removeReservationBtn = new JButton("Remove Reservation");
		removeReservationBtn.setName("removeReservationBtn");
		removeReservationBtn.setEnabled(false);
		GridBagConstraints gbc_removeReservationBtn = new GridBagConstraints();
		gbc_removeReservationBtn.gridwidth = 6;
		gbc_removeReservationBtn.insets = new Insets(0, 0, 5, 0);
		gbc_removeReservationBtn.gridx = 4;
		gbc_removeReservationBtn.gridy = 4;
		contentPane.add(removeReservationBtn, gbc_removeReservationBtn);
		
		// Sixth row
		clientErrorMsgLbl = new JLabel(" ");
		clientErrorMsgLbl.setName("clientErrorMsgLbl");
		GridBagConstraints gbc_clientErrorMsgLbl = new GridBagConstraints();
		gbc_clientErrorMsgLbl.gridwidth = 4;
		gbc_clientErrorMsgLbl.insets = new Insets(0, 0, 0, 5);
		gbc_clientErrorMsgLbl.gridx = 0;
		gbc_clientErrorMsgLbl.gridy = 5;
		contentPane.add(clientErrorMsgLbl, gbc_clientErrorMsgLbl);
		
		reservationErrorMsgLbl = new JLabel(" ");
		reservationErrorMsgLbl.setName("reservationErrorMsgLbl");
		GridBagConstraints gbc_reservationErrorMsgLbl = new GridBagConstraints();
		gbc_reservationErrorMsgLbl.insets = new Insets(0, 0, 0, 5);
		gbc_reservationErrorMsgLbl.gridwidth = 6;
		gbc_reservationErrorMsgLbl.gridx = 4;
		gbc_reservationErrorMsgLbl.gridy = 5;
		contentPane.add(reservationErrorMsgLbl, gbc_reservationErrorMsgLbl);
	}
}
