package com.reserve.service;

import java.util.Optional;

import com.reserve.exception.TicketingException;
import com.reserve.vo.Reservation;
import com.reserve.vo.SeatHold;

public interface TicketService {
	/**
	 * The number of seats in the requested level that are neither held nor
	 * reserved
	 * 
	 * @param venueLevel
	 *            a numeric venue level identifier to limit the search
	 * @return the number of tickets available on the provided level
	 * @throws TicketingException 
	 */
	public int numSeatsAvailable(Optional<Integer> venueLevel) throws TicketingException;

	/**
	 * Find and hold the best available seats for a customer
	 * 
	 * @param numSeats
	 *            the number of seats to find and hold
	 * @param minLevel
	 *            the minimum venue leve
	 * @param maxLevel
	 *            the maximum venue level
	 * @param customerEmail
	 *            unique identifier for the customer
	 * @return a SeatHold object identifying the specific seats and related
	 *         information
	 * @throws TicketingException 
	 */
	public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel,
			Optional<Integer> maxLevel, String customerEmail) throws TicketingException;

	/**
	 * Commit seats held for a specific customer
	 * 
	 * @param seatHoldId
	 *            the seat hold identifier
	 * @param customerEmail
	 *            the email address of the customer to which the seat hold is
	 *            assigned
	 * @return a reservation confirmation code
	 * @throws TicketingException
	 */
	public Reservation reserveSeats(int seatHoldId, String customerEmail)
			throws TicketingException;

}