package com.reserve.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.reserve.dao.TicketDAO;
import com.reserve.dao.UserDAO;
import com.reserve.service.TicketService;
import com.reserve.vo.LevelMaster;
import com.reserve.vo.SeatHold;
import com.reserve.vo.SeatMap;
import com.reserve.vo.User;

public class TicketServiceImpl implements TicketService {

	private TicketDAO ticketDAO;
	private UserDAO userDAO;

	public TicketServiceImpl() {
	}

	public TicketServiceImpl(TicketDAO ticketDAOIn, UserDAO userDAOIn) {
		ticketDAO = ticketDAOIn;
		userDAO = userDAOIn;
	}

	@Override
	public int numSeatsAvailable(Optional venueLevel) {
		int noOfSeats = 0;
		LevelMaster total = ticketDAO.getTotalNumberOfSeats(venueLevel);
		List<SeatMap> list = ticketDAO.getFilledSeats(venueLevel);
		if (total != null) {
			noOfSeats = total.getNoOfRows() * total.getNoOfseats();
		}
		if (list != null) {
			noOfSeats = noOfSeats - list.size();
		}
		return noOfSeats;
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, Optional minLevel,
			Optional maxLevel, String customerEmail) {
		User user = new User();
		SeatHold hold = null;
		user.setEmail(customerEmail);
		user = userDAO.getUserOrCreate(user);
		// try existing held out dated (not confirmed seats) which may be
		// available now
		hold = ticketDAO.holdExistingHeldOutdatedSeats(numSeats, minLevel,
				maxLevel, user);
		if (hold == null || hold.getNoOfseats() == 0) {
			// hold new available seats
			hold = ticketDAO.holdAvailableSeats(numSeats, minLevel, maxLevel,
					user);
		}
		return hold;
	}

	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) {
		return null;
	}
}