package com.reserve.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.reserve.exception.TicketingException;
import com.reserve.vo.Reservation;
import com.reserve.vo.SeatHold;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TicketServiceTest {

	@Autowired
	private TicketService ticketService;
	@Autowired
	private UserService userService;
	@Autowired
	private RefService refService;

	@Test
	public void testServiceAvailability() {
		assertNotNull("ticketService - instance is good", ticketService);
		assertNotNull("userService - instance is good", userService);
		assertNotNull("refService - instance is good", refService);
	}

	@Test
	public void testNoOfSeatsAvailable() {
		try {
			assertNotNull(ticketService.numSeatsAvailable(Optional.of(1)));
		} catch (TicketingException e) {

		}
	}

	@Test
	public void testHoldSeats() {
		SeatHold hold = null;
		try {
			hold = ticketService.findAndHoldSeats(1, Optional.of(1), Optional.of(2), "test@test.com");
			assertNotNull(hold);
			assert (hold.getHoldId() > 1);
		} catch (TicketingException e) {

		}
	}

	@Test
	public void testConfirmSeats() {
		Reservation confCode = null;
		try {
			confCode = ticketService.reserveSeats(1, "test@test.com");
			assertNotNull(confCode);
			assertNotNull(confCode.getReservationId());
			assert (confCode.getReservationId() > 1);
		} catch (TicketingException e) {

		}
	}
}