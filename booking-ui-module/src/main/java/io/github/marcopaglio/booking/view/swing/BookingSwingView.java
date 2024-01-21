package io.github.marcopaglio.booking.view.swing;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Component;

/**
 * A concrete implementation of the view for the booking application using Swing.
 */
public class BookingSwingView extends JFrame implements BookingView {
	private static final long serialVersionUID = 1L;

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
	private JLabel operationErrorMsgLbl;
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

	/**
	 * Presenter of booking application used by the view for managing user requests.
	 */
	private transient BookingPresenter bookingPresenter;

	// METHODS
	/**
	 * Sets the presenter called to carry out the actions of the controls.
	 * 
	 * @param bookingPresenter	the presenter of the booking application.
	 */
	@Generated
	public void setBookingPresenter(BookingPresenter bookingPresenter) {
		this.bookingPresenter = bookingPresenter;
	}

	/**
	 * Displays the clients of the given list on the graphical user interface through Swing.
	 * Additionally, this method resets the client list selection and disables any buttons
	 * that fire when a client is selected in the list.
	 * 
	 * @param clients	the {@code List} of clients to show.
	 */
	@Override
	public void showAllClients(List<Client> clients) {
		SwingUtilities.invokeLater(() -> {
			clientListModel.clear();
			clients.stream().forEach(clientListModel::addElement);
			
			addReservationBtn.setEnabled(false);
			renameBtn.setEnabled(false);
			removeClientBtn.setEnabled(false);
		});
	}

	/**
	 * Displays the reservations of the given list on the user interface through Swing.
	 * Additionally, this method resets the reservation list selection and disables any buttons
	 * that fire when a reservation is selected in the list.
	 * 
	 * @param reservations	the {@code List} of reservations to show.
	 */
	@Override
	public void showAllReservations(List<Reservation> reservations) {
		SwingUtilities.invokeLater(() -> {
			reservationListModel.clear();
			reservations.stream().forEach(reservationListModel::addElement);
			
			rescheduleBtn.setEnabled(false);
			removeReservationBtn.setEnabled(false);
		});
	}

	/**
	 * Displays the reservation just inserted into the repository on the user interface
	 * through Swing. Additionally, this method resets client forms and disables any
	 * buttons that fire when those forms are filled out.
	 * 
	 * @param reservation	the {@code Reservation} to show.
	 */
	@Override
	public void reservationAdded(Reservation reservation) {
		SwingUtilities.invokeLater(() -> {
			reservationListModel.addElement(reservation);
			
			resetErrorMsg();
			resetDateForm();
			addReservationBtn.setEnabled(false);
			rescheduleBtn.setEnabled(false);
		});
	}

	/**
	 * Displays the client just inserted into the repository on the user interface through Swing.
	 * Additionally, this method resets reservation forms and disables any buttons that
	 * fire when those forms are filled out.
	 * 
	 * @param client	the {@code Client} to show.
	 */
	@Override
	public void clientAdded(Client client) {
		SwingUtilities.invokeLater(() -> {
			clientListModel.addElement(client);
			
			resetErrorMsg();
			resetFullNameForm();
			addClientBtn.setEnabled(false);
			renameBtn.setEnabled(false);
		});
	}

	/**
	 * Makes the just deleted reservation disappear from the user interface through Swing.
	 * If the deleted reservation was selected, this method disables related buttons.
	 * 
	 * @param reservation	the {@code Reservation} to remove from the view.
	 */
	@Override
	public void reservationRemoved(Reservation reservation) {
		SwingUtilities.invokeLater(() -> {
			reservationListModel.removeElement(reservation);
			
			resetErrorMsg();
			if (reservationList.isSelectionEmpty()) {
				rescheduleBtn.setEnabled(false);
				removeReservationBtn.setEnabled(false);
			}
		});
	}

	/**
	 * Makes the just deleted client disappear from the user interface through Swing.
	 * If the deleted client was selected, this method disables related buttons.
	 * 
	 * @param client	the {@code Client} to remove from the view.
	 */
	@Override
	public void clientRemoved(Client client) {
		SwingUtilities.invokeLater(() -> {
			clientListModel.removeElement(client);
			
			resetErrorMsg();
			if (clientList.isSelectionEmpty()) {
				renameBtn.setEnabled(false);
				removeClientBtn.setEnabled(false);
				addReservationBtn.setEnabled(false);
			}
		});
	}

	/**
	 * Displays the changes of the client just renamed on the user interface through Swing.
	 * Additionally, this method resets client forms and disables any
	 * buttons that fire when those forms are filled out.
	 * 
	 * @param oldClient		the {@code Client} to replace from the view.
	 * @param renamedClient	the {@code Client} that replaces the old one.
	 */
	@Override
	public void clientRenamed(Client oldClient, Client renamedClient) {
		SwingUtilities.invokeLater(() -> {
			int clientPosition = clientListModel.indexOf(oldClient);
			int selectedIndex = clientList.getSelectedIndex();
			
			clientListModel.removeElement(oldClient);
			clientListModel.add(clientPosition, renamedClient);
			if (selectedIndex == clientPosition)
				clientList.setSelectedIndex(clientPosition);
			
			resetErrorMsg();
			resetFullNameForm();
			addClientBtn.setEnabled(false);
			renameBtn.setEnabled(false);
		});
	}

	/**
	 * Displays the changes of the reservation just rescheduled on the user interface
	 * through Swing. Additionally, this method resets reservation forms and disables any
	 * buttons that fire when those forms are filled out.
	 * 
	 * @param oldReservation			the {@code Reservation} to replace from the view.
	 * @param rescheduledReservation	the {@code Reservation} that replaces the old one.
	 */
	@Override
	public void reservationRescheduled(Reservation oldReservation, Reservation rescheduledReservation) {
		SwingUtilities.invokeLater(() -> {
			int reservationPosition = reservationListModel.indexOf(oldReservation);
			int selectedIndex = reservationList.getSelectedIndex();
			
			reservationListModel.add(reservationPosition, rescheduledReservation);
			reservationListModel.removeElement(oldReservation);
			if (reservationPosition == selectedIndex)
				reservationList.setSelectedIndex(reservationPosition);
			
			resetErrorMsg();
			resetDateForm();
			addReservationBtn.setEnabled(false);
			rescheduleBtn.setEnabled(false);
		});
	}

	/**
	 * Displays an error message that involves operation results through Swing.
	 * 
	 * @param message	the message to show.
	 */
	@Override
	public void showOperationError(String message) {
		SwingUtilities.invokeLater(() -> operationErrorMsgLbl.setText(message));
	}

	/**
	 * Displays an error message that involves input forms through Swing.
	 * 
	 * @param message	the message to show.
	 */
	@Override
	public void showFormError(String message) {
		SwingUtilities.invokeLater(() -> formErrorMsgLbl.setText(message));
	}

	/**
	 * Resets full-name forms.
	 */
	private void resetFullNameForm() {
		nameFormTxt.setText("");
		surnameFormTxt.setText("");
	}

	/**
	 * Resets date forms.
	 */
	private void resetDateForm() {
		yearFormTxt.setText("");
		monthFormTxt.setText("");
		dayFormTxt.setText("");
	}

	/**
	 * Resets error messages.
	 */
	private void resetErrorMsg() {
		formErrorMsgLbl.setText(" ");
		operationErrorMsgLbl.setText(" ");
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the addClientBtn.
	 */
	@Generated
	JButton getAddClientBtn() {
		return addClientBtn;
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the rescheduleBtn.
	 */
	@Generated
	JButton getRescheduleBtn() {
		return rescheduleBtn;
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the removeReservationBtn.
	 */
	@Generated
	JButton getRemoveReservationBtn() {
		return removeReservationBtn;
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the addReservationBtn.
	 */
	@Generated
	JButton getAddReservationBtn() {
		return addReservationBtn;
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the renameBtn.
	 */
	@Generated
	JButton getRenameBtn() {
		return renameBtn;
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the removeClientBtn.
	 */
	@Generated
	JButton getRemoveClientBtn() {
		return removeClientBtn;
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the clientListModel.
	 */
	@Generated
	DefaultListModel<Client> getClientListModel() {
		return clientListModel;
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the reservationListModel.
	 */
	@Generated
	DefaultListModel<Reservation> getReservationListModel() {
		return reservationListModel;
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the formErrorMsgLbl.
	 */
	@Generated
	JLabel getFormErrorMsgLbl() {
		return formErrorMsgLbl;
	}

	/**
	 * Used for tests purpose.
	 * 
	 * @return the operationErrorMsgLbl.
	 */
	@Generated
	JLabel getOperationErrorMsgLbl() {
		return operationErrorMsgLbl;
	}

	// EVENT HANDLERS
	/**
	 * Adapter activated on key releasing on {@code nameFormTxt} and {@code surnameFormTxt}.
	 * This handler enables/disables:
	 * 1) {@code addClientBtn} if full-name forms aren't blank;
	 * 2) {@code renameBtn} if full-name forms aren't blank and a client is selected.
	 */
	private final transient KeyAdapter nameFormAdapter = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			addClientBtn.setEnabled(checkAddClientBtnRequirements());
			renameBtn.setEnabled(checkRenameBtnRequirements());
		}

		/**
		 * Checks if {@code addClientBtn} enabling requirements are met.
		 * 
		 * @return	{@code true} if requirements are met; {@code false} otherwise.
		 */
		private boolean checkAddClientBtnRequirements() {
			return areFullNameFormsNotBlank();
		}
	};

	/**
	 * Adapter activated on key releasing on {@code yearFormTxt}, {@code monthFormTxt} and
	 * {@code surnameFormTxt}. This handler enables:
	 * 1) {@code addReservationBtn} if date forms aren't blank and a client is selected;
	 * 2) {@code rescheduleBtn} if date forms aren't blank and a reservation is selected.
	 */
	private final transient KeyAdapter dateFormAdapter = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			addReservationBtn.setEnabled(checkAddReservationBtnRequirements());
			rescheduleBtn.setEnabled(checkRescheduleBtnRequirements());
		}
	};

	/**
	 * Listener activated on selection changing on {@code clientList}.
	 * This handler enables:
	 * 1) {@code renameBtn} if full-name forms aren't blank and a client is selected;
	 * 2) {@code removeClientBtn} if a client is selected;
	 * 3) {@code addReservationBtn} if date forms aren't blank and a client is selected.
	 */
	private final transient ListSelectionListener clientListListener = e -> {
		renameBtn.setEnabled(checkRenameBtnRequirements());
		removeClientBtn.setEnabled(checkRemoveClientRequirements());
		addReservationBtn.setEnabled(checkAddReservationBtnRequirements());
	};

	/**
	 * Listener activated on selection changing on {@code reservationList}.
	 * This handler enables:
	 * 1) {@code rescheduleBtn} if date forms aren't blank and a reservation is selected;
	 * 2) {@code removeReservationBtn} if a reservation is selected.
	 * And, if a reservation is selected, it selects the associated client in the client list.
	 */
	private final transient ListSelectionListener reservationListListener = e -> {
		rescheduleBtn.setEnabled(checkRescheduleBtnRequirements());
		removeReservationBtn.setEnabled(checkRemoveReservationRequirements());
		selectAssociatedClient();
	};

	/**
	 * Selects the client of the client list associated to the reservation selected, if it exists,
	 * or clears the selection, otherwise.
	 */
	private void selectAssociatedClient() {
		if (!reservationList.isSelectionEmpty()) {
			Reservation reservationSelected = reservationList.getSelectedValue();
			if (reservationSelected != null) {
				clientList.setSelectedValue(
					getClientToSelect(
						reservationSelected.getClientId(),
						clientListModel.toArray())
					, true);
			} else
				clientList.clearSelection();
		}
	}

	/**
	 * Retrieves the client of the list with the specified identifier, if it exists.
	 * 
	 * @param clientId		the identifier of the client to retrieve.
	 * @param clientsInList	the list in which to search for the client to retrieve.
	 * @return				the {@code Client} with the specified identifier, if it exists;
	 * 						a {@code null} object, otherwise.
	 */
	private Client getClientToSelect(UUID clientId, Object[] clientsInList) {
		return (Client) Arrays.stream(clientsInList)
			.filter(c -> c != null && Objects.equals(((Client) c).getId(), clientId))
			.findFirst()
			.orElse(null);
	}

	/**
	 * Action activated on clicking on {@code addClientBtn}. This handler disables
	 * {@code addClientBtn} and delegates the operation to the presenter.
	 */
	private final transient ActionListener addClientAction = e -> {
		addClientBtn.setEnabled(false);
		new Thread(() -> bookingPresenter
				.addClient(nameFormTxt.getText(), surnameFormTxt.getText())
		).start();
		
	};

	/**
	 * Action activated on clicking on {@code renameBtn}. This handler disables
	 * {@code renameBtn} and delegates the operation to the presenter.
	 */
	private final transient ActionListener renameAction = e -> {
		renameBtn.setEnabled(false);
		new Thread(() -> bookingPresenter.renameClient(
				clientList.getSelectedValue(), nameFormTxt.getText(), surnameFormTxt.getText())
		).start();
	};

	/**
	 * Action activated on clicking on {@code removeClientBtn}. This handler disables
	 * {@code removeClientBtn} and delegates the operation to the presenter.
	 */
	private final transient ActionListener removeClientAction = e -> {
		removeClientBtn.setEnabled(false);
		new Thread(() -> bookingPresenter
				.deleteClient(clientList.getSelectedValue())
		).start();
	};

	/**
	 * Action activated on clicking on {@code addReservationBtn}. This handler disables
	 * {@code addReservationBtn} and delegates the operation to the presenter.
	 */
	private final transient ActionListener addReservationAction = e -> {
		addReservationBtn.setEnabled(false);
		new Thread(() -> bookingPresenter
				.addReservation(clientList.getSelectedValue(), getDateViaForms())
		).start();
	};

	/**
	 * Action activated on clicking on {@code rescheduleBtn}. This handler disables
	 * {@code rescheduleBtn} and delegates the operation to the presenter.
	 */
	private final transient ActionListener rescheduleAction = e -> {
		rescheduleBtn.setEnabled(false);
		new Thread(() -> bookingPresenter
				.rescheduleReservation(reservationList.getSelectedValue(), getDateViaForms())
		).start();
	};

	/**
	 * Action activated on clicking on {@code removeReservationBtn}. This handler disables
	 * {@code removeReservationBtn} and delegates the operation to the presenter.
	 */
	private final transient ActionListener removeReservationAction = e -> {
		removeReservationBtn.setEnabled(false);
		new Thread(() -> bookingPresenter
				.deleteReservation(reservationList.getSelectedValue())
		).start();
	};

	/**
	 * Generates a date via {@code yearFormTxt}, {@code monthFormTxt} and {@code dayFormTxt}.
	 * 
	 * @return	a {@code String} date in the format yyyy-mm-dd.
	 */
	private String getDateViaForms() {
		return yearFormTxt.getText() + "-" + monthFormTxt.getText() + "-" + dayFormTxt.getText();
	}

	// BUTTONS REQUIREMENTS
	/**
	 * Checks if {@code renameBtn} enabling requirements are met.
	 * 
	 * @return	{@code true} if requirements are met; {@code false} otherwise.
	 */
	private boolean checkRenameBtnRequirements() {
		return areFullNameFormsNotBlank() && isClientListSelectionNotEmpty();
	}

	/**
	 * Checks if {@code addReservationBtn} enabling requirements are met.
	 * 
	 * @return	{@code true} if requirements are met; {@code false} otherwise.
	 */
	private boolean checkAddReservationBtnRequirements() {
		return areDateFormsNotBlank() && isClientListSelectionNotEmpty();
	}

	/**
	 * Checks if {@code rescheduleBtn} enabling requirements are met.
	 * 
	 * @return	{@code true} if requirements are met; {@code false} otherwise.
	 */
	private boolean checkRescheduleBtnRequirements() {
		return areDateFormsNotBlank() && isReservationListSelectionNotEmpty();
	}

	/**
	 * Checks if {@code removeClientBtn} enabling requirements are met.
	 * 
	 * @return	{@code true} if requirements are met; {@code false} otherwise.
	 */
	private boolean checkRemoveClientRequirements() {
		return isClientListSelectionNotEmpty();
	}

	/**
	 * Checks if {@code removeReservationBtn} enabling requirements are met.
	 * 
	 * @return	{@code true} if requirements are met; {@code false} otherwise.
	 */
	private boolean checkRemoveReservationRequirements() {
		return isReservationListSelectionNotEmpty();
	}

	/**
	 * Checks if {@code nameFormTxt} and {@code surnameFormTxt} are not blank.
	 * 
	 * @return	{@code true} if forms are not blank; {@code false} otherwise.
	 */
	private boolean areFullNameFormsNotBlank() {
		return !nameFormTxt.getText().isBlank() &&
				!surnameFormTxt.getText().isBlank();
	}

	/**
	 * Checks if {@code yearFormTxt}, {@code monthFormTxt} and {@code surnameFormTxt} are not blank.
	 * 
	 * @return	{@code true} if forms are not blank; {@code false} otherwise.
	 */
	private boolean areDateFormsNotBlank() {
		return !yearFormTxt.getText().isBlank() &&
				!monthFormTxt.getText().isBlank() &&
				!dayFormTxt.getText().isBlank();
	}

	/**
	 * Checks if there is a selected item in {@code clientList}.
	 * 
	 * @return	{@code true} if there is a selected item; {@code false} otherwise.
	 */
	private boolean isClientListSelectionNotEmpty() {
		return !clientList.isSelectionEmpty();
	}

	/**
	 * Checks if there is a selected item in {@code reservationList}.
	 * 
	 * @return	{@code true} if there is a selected item; {@code false} otherwise.
	 */
	private boolean isReservationListSelectionNotEmpty() {
		return !reservationList.isSelectionEmpty();
	}

	/**
	 * Create the frame.
	 */
	public BookingSwingView() {
		setTitle("BookingApp");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		GridBagLayout gblContentPane = new GridBagLayout();
		gblContentPane.columnWidths = new int[] {60, 40, 60, 40, 40, 40, 0, 20, 0, 20, 0};
		gblContentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gblContentPane.columnWeights = new double[]{1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gblContentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gblContentPane);
		
		// First row
		JLabel firstNameLbl = new JLabel("First Name");
		firstNameLbl.setName("firstNameLbl");
		GridBagConstraints gbcFirstNameLbl = new GridBagConstraints();
		gbcFirstNameLbl.anchor = GridBagConstraints.EAST;
		gbcFirstNameLbl.insets = new Insets(0, 0, 5, 5);
		gbcFirstNameLbl.gridx = 0;
		gbcFirstNameLbl.gridy = 0;
		contentPane.add(firstNameLbl, gbcFirstNameLbl);
		
		nameFormTxt = new JTextField();
		nameFormTxt.addKeyListener(nameFormAdapter);
		nameFormTxt.setName("nameFormTxt");
		nameFormTxt.setToolTipText("Names must not contain numbers (e.g. 0-9) or any type of symbol or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		GridBagConstraints gbcNameFormTxt = new GridBagConstraints();
		gbcNameFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbcNameFormTxt.insets = new Insets(0, 0, 5, 5);
		gbcNameFormTxt.gridx = 1;
		gbcNameFormTxt.gridy = 0;
		contentPane.add(nameFormTxt, gbcNameFormTxt);
		nameFormTxt.setColumns(10);
		
		lastNameLbl = new JLabel("Last Name");
		lastNameLbl.setVerticalAlignment(SwingConstants.BOTTOM);
		lastNameLbl.setName("lastNameLbl");
		GridBagConstraints gbcLastNameLbl = new GridBagConstraints();
		gbcLastNameLbl.anchor = GridBagConstraints.EAST;
		gbcLastNameLbl.insets = new Insets(0, 0, 5, 5);
		gbcLastNameLbl.gridx = 2;
		gbcLastNameLbl.gridy = 0;
		contentPane.add(lastNameLbl, gbcLastNameLbl);
		
		surnameFormTxt = new JTextField();
		surnameFormTxt.addKeyListener(nameFormAdapter);
		surnameFormTxt.setToolTipText("Surnames must not contain numbers (e.g. 0-9) or any type of symbol or special character (e.g. ~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
		surnameFormTxt.setName("surnameFormTxt");
		GridBagConstraints gbcSurnameFormTxt = new GridBagConstraints();
		gbcSurnameFormTxt.insets = new Insets(0, 0, 5, 5);
		gbcSurnameFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbcSurnameFormTxt.gridx = 3;
		gbcSurnameFormTxt.gridy = 0;
		contentPane.add(surnameFormTxt, gbcSurnameFormTxt);
		surnameFormTxt.setColumns(10);
		
		dateLbl = new JLabel("Date");
		dateLbl.setName("dateLbl");
		GridBagConstraints gbcDateLbl = new GridBagConstraints();
		gbcDateLbl.anchor = GridBagConstraints.EAST;
		gbcDateLbl.insets = new Insets(0, 0, 5, 5);
		gbcDateLbl.gridx = 4;
		gbcDateLbl.gridy = 0;
		contentPane.add(dateLbl, gbcDateLbl);
		
		yearFormTxt = new JTextField();
		yearFormTxt.addKeyListener(dateFormAdapter);
		yearFormTxt.setToolTipText("year");
		yearFormTxt.setName("yearFormTxt");
		GridBagConstraints gbcYearFormTxt = new GridBagConstraints();
		gbcYearFormTxt.insets = new Insets(0, 0, 5, 5);
		gbcYearFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbcYearFormTxt.gridx = 5;
		gbcYearFormTxt.gridy = 0;
		contentPane.add(yearFormTxt, gbcYearFormTxt);
		yearFormTxt.setColumns(10);
		
		dash1Lbl = new JLabel("-");
		dash1Lbl.setName("dash1Lbl");
		GridBagConstraints gbcDash1Lbl = new GridBagConstraints();
		gbcDash1Lbl.anchor = GridBagConstraints.EAST;
		gbcDash1Lbl.insets = new Insets(0, 0, 5, 5);
		gbcDash1Lbl.gridx = 6;
		gbcDash1Lbl.gridy = 0;
		contentPane.add(dash1Lbl, gbcDash1Lbl);
		
		monthFormTxt = new JTextField();
		monthFormTxt.addKeyListener(dateFormAdapter);
		monthFormTxt.setName("monthFormTxt");
		monthFormTxt.setToolTipText("month");
		GridBagConstraints gbcMonthFormTxt = new GridBagConstraints();
		gbcMonthFormTxt.insets = new Insets(0, 0, 5, 0);
		gbcMonthFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbcMonthFormTxt.gridx = 7;
		gbcMonthFormTxt.gridy = 0;
		contentPane.add(monthFormTxt, gbcMonthFormTxt);
		monthFormTxt.setColumns(10);
		
		dash2Lbl = new JLabel("-");
		dash2Lbl.setName("dash2Lbl");
		GridBagConstraints gbcDash2Lbl = new GridBagConstraints();
		gbcDash2Lbl.anchor = GridBagConstraints.EAST;
		gbcDash2Lbl.insets = new Insets(0, 0, 5, 5);
		gbcDash2Lbl.gridx = 8;
		gbcDash2Lbl.gridy = 0;
		contentPane.add(dash2Lbl, gbcDash2Lbl);
		
		dayFormTxt = new JTextField();
		dayFormTxt.addKeyListener(dateFormAdapter);
		dayFormTxt.setName("dayFormTxt");
		dayFormTxt.setToolTipText("day");
		GridBagConstraints gbcDayFormTxt = new GridBagConstraints();
		gbcDayFormTxt.insets = new Insets(0, 0, 5, 5);
		gbcDayFormTxt.fill = GridBagConstraints.HORIZONTAL;
		gbcDayFormTxt.gridx = 9;
		gbcDayFormTxt.gridy = 0;
		contentPane.add(dayFormTxt, gbcDayFormTxt);
		dayFormTxt.setColumns(10);
		
		// Second row
		formErrorMsgLbl = new JLabel(" ");
		formErrorMsgLbl.setForeground(new Color(255, 0, 0));
		formErrorMsgLbl.setName("formErrorMsgLbl");
		GridBagConstraints gbcFormErrorMsgLbl = new GridBagConstraints();
		gbcFormErrorMsgLbl.insets = new Insets(0, 0, 5, 5);
		gbcFormErrorMsgLbl.gridwidth = 10;
		gbcFormErrorMsgLbl.gridx = 0;
		gbcFormErrorMsgLbl.gridy = 1;
		contentPane.add(formErrorMsgLbl, gbcFormErrorMsgLbl);
		
		// Third row
		addClientBtn = new JButton("Add Client");
		addClientBtn.addActionListener(addClientAction);
		addClientBtn.setEnabled(false);
		addClientBtn.setName("addClientBtn");
		GridBagConstraints gbcAddClientBtn = new GridBagConstraints();
		gbcAddClientBtn.gridwidth = 2;
		gbcAddClientBtn.insets = new Insets(0, 0, 5, 5);
		gbcAddClientBtn.gridx = 0;
		gbcAddClientBtn.gridy = 2;
		contentPane.add(addClientBtn, gbcAddClientBtn);
		
		renameBtn = new JButton("Rename");
		renameBtn.addActionListener(renameAction);
		renameBtn.setEnabled(false);
		renameBtn.setName("renameBtn");
		GridBagConstraints gbcRenameBtn = new GridBagConstraints();
		gbcRenameBtn.gridwidth = 2;
		gbcRenameBtn.insets = new Insets(0, 0, 5, 5);
		gbcRenameBtn.gridx = 2;
		gbcRenameBtn.gridy = 2;
		contentPane.add(renameBtn, gbcRenameBtn);
		
		addReservationBtn = new JButton("Add Reservation");
		addReservationBtn.addActionListener(addReservationAction);
		addReservationBtn.setEnabled(false);
		addReservationBtn.setName("addReservationBtn");
		GridBagConstraints gbcAddReservationBtn = new GridBagConstraints();
		gbcAddReservationBtn.gridwidth = 2;
		gbcAddReservationBtn.insets = new Insets(0, 0, 5, 5);
		gbcAddReservationBtn.gridx = 4;
		gbcAddReservationBtn.gridy = 2;
		contentPane.add(addReservationBtn, gbcAddReservationBtn);
		
		rescheduleBtn = new JButton("Reschedule");
		rescheduleBtn.addActionListener(rescheduleAction);
		rescheduleBtn.setName("rescheduleBtn");
		rescheduleBtn.setEnabled(false);
		GridBagConstraints gbcRescheduleBtn = new GridBagConstraints();
		gbcRescheduleBtn.gridwidth = 4;
		gbcRescheduleBtn.insets = new Insets(0, 0, 5, 0);
		gbcRescheduleBtn.gridx = 6;
		gbcRescheduleBtn.gridy = 2;
		contentPane.add(rescheduleBtn, gbcRescheduleBtn);
		
		// Fourth row
		clientScrollPane = new JScrollPane();
		clientScrollPane.setName("clientScrollPane");
		GridBagConstraints gbcClientScrollPane = new GridBagConstraints();
		gbcClientScrollPane.gridwidth = 4;
		gbcClientScrollPane.insets = new Insets(0, 0, 5, 5);
		gbcClientScrollPane.fill = GridBagConstraints.BOTH;
		gbcClientScrollPane.gridx = 0;
		gbcClientScrollPane.gridy = 3;
		contentPane.add(clientScrollPane, gbcClientScrollPane);
		
		clientListModel = new DefaultListModel<>();
		clientList = new JList<>(clientListModel);
		clientList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list,
						getDisplayString((Client) value),
						index, isSelected, cellHasFocus);
			}
		});
		clientList.addListSelectionListener(clientListListener);
		clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientList.setName("clientList");
		clientScrollPane.setViewportView(clientList);
		
		reservationScrollPane = new JScrollPane();
		reservationScrollPane.setName("reservationScrollPane");
		GridBagConstraints gbcReservationScrollPane = new GridBagConstraints();
		gbcReservationScrollPane.gridwidth = 6;
		gbcReservationScrollPane.insets = new Insets(0, 0, 5, 0);
		gbcReservationScrollPane.fill = GridBagConstraints.BOTH;
		gbcReservationScrollPane.gridx = 4;
		gbcReservationScrollPane.gridy = 3;
		contentPane.add(reservationScrollPane, gbcReservationScrollPane);
		
		reservationListModel = new DefaultListModel<>();
		reservationList = new JList<>(reservationListModel);
		reservationList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				return super.getListCellRendererComponent(list,
						getDisplayString((Reservation) value),
						index, isSelected, cellHasFocus);
			}
		});
		reservationList.addListSelectionListener(reservationListListener);
		reservationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reservationList.setName("reservationList");
		reservationScrollPane.setViewportView(reservationList);
		
		// Fifth row
		removeClientBtn = new JButton("Remove Client");
		removeClientBtn.addActionListener(removeClientAction);
		removeClientBtn.setEnabled(false);
		removeClientBtn.setName("removeClientBtn");
		GridBagConstraints gbcRemoveClientBtn = new GridBagConstraints();
		gbcRemoveClientBtn.gridwidth = 4;
		gbcRemoveClientBtn.insets = new Insets(0, 0, 5, 5);
		gbcRemoveClientBtn.gridx = 0;
		gbcRemoveClientBtn.gridy = 4;
		contentPane.add(removeClientBtn, gbcRemoveClientBtn);
		
		removeReservationBtn = new JButton("Remove Reservation");
		removeReservationBtn.addActionListener(removeReservationAction);
		removeReservationBtn.setName("removeReservationBtn");
		removeReservationBtn.setEnabled(false);
		GridBagConstraints gbcRemoveReservationBtn = new GridBagConstraints();
		gbcRemoveReservationBtn.gridwidth = 6;
		gbcRemoveReservationBtn.insets = new Insets(0, 0, 5, 0);
		gbcRemoveReservationBtn.gridx = 4;
		gbcRemoveReservationBtn.gridy = 4;
		contentPane.add(removeReservationBtn, gbcRemoveReservationBtn);
		
		operationErrorMsgLbl = new JLabel(" ");
		operationErrorMsgLbl.setForeground(new Color(255, 0, 0));
		operationErrorMsgLbl.setName("operationErrorMsgLbl");
		GridBagConstraints gbcReservationErrorMsgLbl = new GridBagConstraints();
		gbcReservationErrorMsgLbl.insets = new Insets(0, 0, 0, 5);
		gbcReservationErrorMsgLbl.gridwidth = 10;
		gbcReservationErrorMsgLbl.gridx = 0;
		gbcReservationErrorMsgLbl.gridy = 5;
		contentPane.add(operationErrorMsgLbl, gbcReservationErrorMsgLbl);
	}

	/**
	 * Generates a description string of the reservation.
	 *  
	 * @param reservation	reservation for which the string is generated.
	 * @return				a descriptor {@code String} of the reservation.
	 */
	private String getDisplayString(Reservation reservation) {
		if (reservation != null)
			return "Reservation [" + reservation.getDate() + "]";
		return String.valueOf(reservation);
	}

	/**
	 * Generates a description string of the client.
	 *  
	 * @param client	client for which the string is generated.
	 * @return			a descriptor {@code String} of the client.
	 */
	private String getDisplayString(Client client) {
		if (client != null)
			return "Client [" + client.getFirstName() + " " + client.getLastName() + "]";
		return String.valueOf(client);
	}
}