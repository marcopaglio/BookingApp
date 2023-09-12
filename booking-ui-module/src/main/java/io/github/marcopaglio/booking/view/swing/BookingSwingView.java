package io.github.marcopaglio.booking.view.swing;

import java.awt.EventQueue;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import io.github.marcopaglio.booking.model.Client;
import io.github.marcopaglio.booking.model.Reservation;
import io.github.marcopaglio.booking.view.BookingView;

public class BookingSwingView extends JFrame implements BookingView {

	private JPanel contentPane;

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
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
