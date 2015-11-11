package com.reserve.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.reserve.dao.TicketDAO;
import com.reserve.dao.UserDAO;
import com.reserve.exception.TicketingException;
import com.reserve.service.TicketService;
import com.reserve.utilities.DateUtils;
import com.reserve.vo.LevelMaster;
import com.reserve.vo.Reservation;
import com.reserve.vo.SeatHold;
import com.reserve.vo.SeatMap;
import com.reserve.vo.User;

public class TicketServiceImpl implements TicketService {
	final static Logger logger = Logger.getLogger(TicketServiceImpl.class);

	private TicketDAO ticketDAO;
	private UserDAO userDAO;

	public TicketServiceImpl() {
	}

	public TicketServiceImpl(TicketDAO ticketDAOIn, UserDAO userDAOIn) {
		ticketDAO = ticketDAOIn;
		userDAO = userDAOIn;
	}

	@Override
	public int numSeatsAvailable(Optional venueLevel) throws TicketingException {
		logger.debug("getting number of seats available for level " + venueLevel);
		int noOfSeats = 0;
		LevelMaster total = null;
		List<SeatMap> list = null;
		try {
			total = ticketDAO.getTotalNumberOfSeats(venueLevel);
			list = ticketDAO.getFilledSeats(venueLevel);
			if (total != null) {
				noOfSeats = total.getNoOfRows() * total.getNoOfseats();
			}
			if (list != null) {
				noOfSeats = noOfSeats - list.size();
			}
			if (logger.isInfoEnabled()) {
				logger.info("Number of seats available in level " + venueLevel + " as of " + new Date() + " is - "
						+ noOfSeats);
			}
		} catch (TicketingException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new TicketingException(100, exception.getMessage());
		}
		return noOfSeats;
	}

	@Override
	public SeatHold findAndHoldSeats(int numSeats, Optional minLevel, Optional maxLevel, String customerEmail)
			throws TicketingException {
		logger.debug("Hold seats Service");
		User user = new User();
		SeatHold hold = null;
		try {
			user.setEmail(customerEmail);
			user = userDAO.getUserOrCreate(user);
			logger.debug("User saved / loaded");
			// try existing held out dated (not confirmed seats) which may be
			// available now
			hold = ticketDAO.holdPreviouslyHeldExpiredSeats(numSeats, minLevel, maxLevel, user);
			logger.debug("Previously held & timedout seats reheld for a new user");
			if (hold == null || hold.getNoOfseats() == 0) {
				// means notickets booked in previously held category
				// hold new available seats
				logger.debug("trying to hold seats with new seat map entries");
				hold = ticketDAO.holdAvailableSeats(numSeats, minLevel, maxLevel, user);
			}

			if (hold == null || hold.getNoOfseats() == 0) {
				// this is a rarest case, where marginal seats are left in new
				// category or expired category
				// means not enough tickets in previously held category
				// seperately and new seperately
				// if both of these 2 are combined, the seats count may match.
				logger.debug("trying to hold seats with new and expired seat map entries");
				hold = ticketDAO.holdRemainBeforeFullSeats(numSeats, minLevel, maxLevel, user);
			}

			if (hold == null || hold.getNoOfseats() == 0) {
				// Tried the both cat 1 and 2 - no tickets available..
				throw new TicketingException(103, "Not Enough Tickets available at this time");
			}

			hold = ticketDAO.getHeldSeats(hold);// for all map ids
			if (logger.isInfoEnabled()) {
				logger.info("Held seats, details - " + hold.toString() + ". Tickets will be held until "
						+ DateUtils.getValidUntilDate(hold.getCreatedDate()));
			}
		} catch (TicketingException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new TicketingException(100, exception.getMessage());
		}
		return hold;
	}

	@Override
	public Reservation reserveSeats(int seatHoldId, String customerEmail) throws TicketingException {
		logger.debug("Confirm seats Service");
		User user = new User();
		Reservation reservation = null;
		SeatHold hold = new SeatHold();
		List<SeatHold> list = null;
		try {
			user.setEmail(customerEmail);
			logger.debug("Validate email and hold id combination");
			user = userDAO.getUserOrCreate(user);
			list = ticketDAO.getHeldSeats(user);
			if (list == null || list.size() == 0) {
				logger.info("Validate email and hold id combination - failed");
				throw new TicketingException(100, "Seat hold id not pertaining to the Email entered.");
			}
			logger.debug("Validatation successful");
			hold.setHoldId(seatHoldId);
			hold.setUser(user);
			hold = ticketDAO.getHeldSeats(hold);
			reservation = ticketDAO.confirmSeats(hold, user);
			if (logger.isInfoEnabled()) {
				logger.info("Reserved seats, details - " + reservation.toString());
			}
		} catch (TicketingException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new TicketingException(100, exception.getMessage());
		}
		return reservation;
	}
}