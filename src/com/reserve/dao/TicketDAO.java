package com.reserve.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.reserve.constants.TicketConstants;
import com.reserve.exception.TicketingException;
import com.reserve.utilities.DateUtils;
import com.reserve.vo.LevelMaster;
import com.reserve.vo.Reservation;
import com.reserve.vo.SeatHold;
import com.reserve.vo.SeatMap;
import com.reserve.vo.SeatMapId;
import com.reserve.vo.Status;
import com.reserve.vo.User;

public class TicketDAO {

	final static Logger logger = Logger.getLogger(TicketDAO.class);

	private SessionFactory sessionFactory;

	public TicketDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public TicketDAO() {
	}

	public LevelMaster getTotalNumberOfSeats(Optional<Integer> levelId) throws TicketingException {
		logger.debug("getTotalNumberOfSeats - start");
		Session session = sessionFactory.getCurrentSession();
		List list = null;
		LevelMaster levelMaster = null;
		try {
			session.beginTransaction();
			list = session.createQuery("from LevelMaster where levelId= :levelId").setInteger("levelId", levelId.get())
					.list();
			if (list != null && list.size() > 0) {
				levelMaster = (LevelMaster) list.get(0);
			}
			session.getTransaction().commit();
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102, "Problem getting available seats due to techincal issues");
		}
		logger.debug("getTotalNumberOfSeats - end");
		return levelMaster;
	}

	public List<SeatMap> getFilledSeats(Optional<Integer> venueLevel) throws TicketingException {
		logger.debug("getFilledSeats for a level");
		Session session = sessionFactory.getCurrentSession();
		List<SeatMap> list = null;
		try {
			session.beginTransaction();
			list = session.createQuery("from SeatMap where status=1 and id.level = :venueLevel")
					.setInteger("venueLevel", venueLevel.get()).list();
			session.getTransaction().commit();
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102, "Problem getting available seats due to techincal issues");
		}
		logger.debug("getFilledSeats for a level - end");
		return list;
	}

	public SeatHold getHeldSeats(SeatHold hold) throws TicketingException {
		logger.debug("getHeldSeats");
		Session session = sessionFactory.getCurrentSession();
		List<SeatHold> list = null;
		try {
			session.beginTransaction();
			list = session.createQuery("from SeatHold where holdId = :holdIdNo")
					.setInteger("holdIdNo", hold.getHoldId()).list();
			if (list != null && list.size() > 0) {
				hold = list.get(0);
			}
			session.getTransaction().commit();
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102, "Problem getting available seats due to techincal issues");
		}
		logger.debug("getHeldSeats  - end");
		return hold;
	}

	public SeatHold holdPreviouslyHeldExpiredSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel,
			User user) throws TicketingException {
		logger.debug("holdExistingHeldOutdatedSeats - start");
		Session session = sessionFactory.getCurrentSession();
		SeatHold seatHold = null;
		List<SeatMap> list = null;
		int insertedSeats = 0;
		try {
			session.beginTransaction();
			list = session
					.createQuery(
							"from SeatMap where status = 0 and createdDate <= :date and id.level between :maxLevel and :minLevel order by id.level, createdDate")
					.setInteger("minLevel", minLevel.get()).setInteger("maxLevel", maxLevel.get())
					.setParameter("date", DateUtils.getOutdatedDate()).setMaxResults(numSeats).list();
			if (list != null && list.size() >= numSeats) {
				seatHold = new SeatHold(user, numSeats, new Date());
				session.save(seatHold);
				for (SeatMap seatMap : list) {
					seatMap.setHold(seatHold); // set new hold id for existing
												// seat records which were
												// booking timedout
					seatMap.setCreatedDate(new Date()); // update date to
														// current
					session.update(seatMap);
					insertedSeats++;
				}
				logger.debug(insertedSeats + " - seats changed to a different hold id as they were expired");
			}
			seatHold = bookAllOrCancel(numSeats, session, seatHold, insertedSeats);
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102, "Problem booking tickets due to techincal issues");
		}
		logger.debug("holdExistingHeldOutdatedSeats - end");
		return seatHold;
	}

	private SeatHold bookAllOrCancel(int numSeats, Session session, SeatHold seatHold, int insertedSeats) {
		if (numSeats == insertedSeats) {
			logger.debug("All necessary Seats booked.");
			session.getTransaction().commit();
		} else {
			logger.debug("Not enough seats which were previously held");
			if (seatHold == null) {
				seatHold = new SeatHold();
			}
			seatHold.setNoOfseats(0);
			session.getTransaction().rollback();
		}
		return seatHold;
	}

	public SeatHold holdAvailableSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel, User user)
			throws TicketingException {
		logger.debug("holdAvailableSeats - Hold seats which were not held / left by someone before");
		Session session = sessionFactory.openSession();
		SeatHold seatHold = null;
		SeatMap seatMap = null;
		int tempRowId, levelCurrent, tempSeatId, bookedTickets = 0;
		LevelMaster levelMaster = null;
		List<SeatMap> list = null;

		// if any one of these are zero, then there is only one level option
		// so assign each other's value - just in case
		if (maxLevel.get() == 0) {
			maxLevel = minLevel;
		}
		if (minLevel.get() == 0) {
			minLevel = maxLevel;
		}
		// initialize levelCurrent for iterating
		levelCurrent = maxLevel.get();
		try {
			session.beginTransaction();

			// first create a hold entity, even if no seats are booked, this
			// would be rolledback finally
			seatHold = new SeatHold(user, numSeats, new Date());
			session.save(seatHold);
			while (bookedTickets != numSeats) {
				// get master entry for level - to get max row number and get
				// max row numbers
				seatMap = null;
				levelMaster = getTotalNumberOfSeats(Optional.of(levelCurrent));

				// get seat records with max row id and max seat id, to take
				// next seat
				list = session
						.createQuery("from SeatMap where id.level = :level order by id.rowid desc, id.seatid desc")
						.setInteger("level", levelCurrent).setMaxResults(1).list();

				if (list == null || list.size() == 0) {
					// day zero entry in a level
					seatMap = new SeatMap(new SeatMapId(levelCurrent, TicketConstants.SHOW_ID, TicketConstants.NEXT_ROW,
							TicketConstants.NEXT_SEAT), new Status(), seatHold, new Date());
					logger.debug("First seat in level booked");
					session.save(seatMap);
					bookedTickets++;
				} else {
					// seat with max row number and seat number
					seatMap = list.get(0);
					seatMap.setHold(seatHold);
					if (seatMap.getId().getSeatid() < levelMaster.getNoOfseats()) {
						tempSeatId = seatMap.getId().getSeatid() + TicketConstants.NEXT_SEAT;
						tempRowId = seatMap.getId().getRowid();

						// seat available in same row
						blockSeat(session, seatHold, tempRowId, levelCurrent, tempSeatId);
						bookedTickets++;
					} else if (seatMap.getId().getRowid() < levelMaster.getNoOfRows()) {
						tempRowId = seatMap.getId().getRowid() + TicketConstants.NEXT_ROW;
						tempSeatId = TicketConstants.NEXT_SEAT;

						// next row available in same level
						blockSeat(session, seatHold, tempRowId, levelCurrent, tempSeatId);
						bookedTickets++;
						logger.debug("First seat in row " + tempRowId + " booked.");
					} else if (seatMap.getId().getRowid() >= levelMaster.getNoOfRows()
							&& seatMap.getId().getSeatid() >= levelMaster.getNoOfseats()
							&& levelCurrent < minLevel.get()) {
						levelCurrent++;
						// if rows and seats are done in one level
						// swith next min level
						logger.debug("Switching to next selected level, as seats done in current level.");
					} else if (seatMap.getId().getRowid() >= levelMaster.getNoOfRows()
							&& seatMap.getId().getSeatid() >= levelMaster.getNoOfseats()
							&& levelCurrent == minLevel.get()) {
						// if in selected levels, rows and seats are done then
						// no more seats, break the loop
						logger.debug("All seats in all levels covered.");
						break;
					}
				}
			}
			seatHold = bookOrRevert(numSeats, session, seatHold, bookedTickets);
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102, "Problem booking tickets due to techincal issues");
		} catch (Exception e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102, "Problem booking tickets due to techincal issues");
		}
		logger.debug("holdAvailableSeats - finished");
		return seatHold;
	}

	private void blockSeat(Session session, SeatHold seatHold, int tempRowId, int levelCurrent, int tempSeatId) {
		SeatMap seatMap;
		seatMap = new SeatMap(new SeatMapId(levelCurrent, TicketConstants.SHOW_ID, tempRowId, tempSeatId), new Status(),
				seatHold, new Date());
		session.save(seatMap);
	}

	private SeatHold bookOrRevert(int numSeats, Session session, SeatHold seatHold, int bookedTickets) {
		if (bookedTickets == numSeats) {
			logger.debug("All seats booked");
			session.getTransaction().commit();
		} else {
			// if not enough tickets available, do not create any records
			// no partial booking
			logger.debug("Not enough seats");
			if (seatHold == null) {
				seatHold = new SeatHold();
			}
			seatHold.setNoOfseats(0);
			session.getTransaction().rollback();
		}
		return seatHold;
	}

	public List<SeatHold> getHeldSeats(User user) throws TicketingException {
		logger.debug("getHeldSeats - Started");
		Session session = sessionFactory.openSession();
		List<SeatHold> list = null;
		try {
			list = session.createQuery("from SeatHold where user.userId = :userIdNo")
					.setInteger("userIdNo", user.getUserId()).list();
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102, "Problem booking tickets due to techincal issues");
		} catch (Exception e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102, "Problem booking tickets due to techincal issues");
		}
		logger.debug("getHeldSeats - finished");
		return list;
	}

	public Reservation confirmSeats(SeatHold seatHold, User user) throws TicketingException {
		logger.debug("confirmSeats - Started");
		Session session = sessionFactory.openSession();
		SeatMap seatMap = null;
		int tempRowId, levelCurrent, tempSeatId, bookedTickets = 0;
		Reservation reservation = null;
		List<SeatHold> list = null;
		Set<SeatMap> map = null;
		Status statusName = null;
		SeatHold hold = null;
		int noOfSeats = 0, confirmedSeats = 0;
		try {
			list = session.createQuery("from SeatHold where holdId = :holdIdNo")
					.setInteger("holdIdNo", seatHold.getHoldId()).list();
			if (list != null && list.size() > 0) {
				statusName = new Status();
				statusName.setStatusId(TicketConstants.STATUS_CONFIRMED);
				hold = list.get(0);
				noOfSeats = hold.getNoOfseats();
				map = list.get(0).getSeatmaps();
				for (SeatMap seat : map) {
					// confirm only booked within allowed hold time
					if (seat.getCreatedDate().after(DateUtils.getOutdatedDate())) {
						seat.setStatus(statusName);
						session.save(seat);
						confirmedSeats++;
					} else {
						// Break the loop as held timeout happened for just one
						// ticket
						break;
					}
				}
			}
			if (confirmedSeats == noOfSeats) {
				// if all seats are confirmed, create a new reservation entity
				reservation = new Reservation(user, hold, new Date());
				session.save(reservation);
				session.getTransaction().commit();
				logger.info("Reservation confirmed");
			} else {
				// if not enough tickets available, do not confirm any records
				// no partial confirmation
				session.getTransaction().rollback();
				logger.error("Held Seats were expired for hold id" + seatHold.getHoldId());
				throw new TicketingException(101,
						"Since you delayed, Held Seats were expired, please try to hold seats once again");
			}
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102, "Problem booking tickets due to techincal issues");
		}
		logger.debug("confirmSeats - done");
		return reservation;
	}
}