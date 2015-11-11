package com.reserve.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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

	@SuppressWarnings("rawtypes")
	public LevelMaster getTotalNumberOfSeats(Optional<Integer> levelId)
			throws TicketingException {
		logger.debug("getTotalNumberOfSeats - start");
		Session session = sessionFactory.getCurrentSession();
		List list = null;
		LevelMaster levelMaster = null;
		try {
			session.beginTransaction();
			list = session
					.createQuery("from LevelMaster where levelId= :levelId")
					.setInteger("levelId", levelId.get()).list();
			if (list != null && list.size() > 0) {
				levelMaster = (LevelMaster) list.get(0);
			}
			session.getTransaction().commit();
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem getting available seats due to techincal issues");
		}
		logger.debug("getTotalNumberOfSeats - end");
		return levelMaster;
	}

	@SuppressWarnings("unchecked")
	public List<SeatMap> getFilledSeats(Optional<Integer> venueLevel)
			throws TicketingException {
		logger.debug("getFilledSeats for a level");
		Session session = sessionFactory.getCurrentSession();
		List<SeatMap> list = null;
		try {
			session.beginTransaction();
			list = session
					.createQuery(
							"from SeatMap where status=1 and id.level = :venueLevel")
					.setInteger("venueLevel", venueLevel.get()).list();
			session.getTransaction().commit();
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem getting available seats due to techincal issues");
		}
		logger.debug("getFilledSeats for a level - end");
		return list;
	}

	@SuppressWarnings("unchecked")
	public SeatHold getHeldSeats(SeatHold hold) throws TicketingException {
		logger.debug("getHeldSeats");
		Session session = sessionFactory.getCurrentSession();
		List<SeatMap> list = null;
		try {
			session.beginTransaction();
			list = session
					.createQuery(
							"from SeatMap where hold.holdId = :holdIdNo order by id.level, id.rowid, id.seatid")
					.setInteger("holdIdNo", hold.getHoldId()).list();
			hold.setSeatmaps(list);
			session.getTransaction().commit();
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem getting available seats due to techincal issues");
		}
		logger.debug("getHeldSeats  - end");
		return hold;
	}

	public SeatHold holdPreviouslyHeldExpiredSeats(int numSeats,
			Optional<Integer> minLevel, Optional<Integer> maxLevel, User user)
			throws TicketingException {
		logger.debug("holdExistingHeldOutdatedSeats - start");
		SeatHold seatHold = shiftExpiredSeatsToNewHold(numSeats, minLevel,
				maxLevel, user, null);
		logger.debug("holdExistingHeldOutdatedSeats - end");
		return seatHold;
	}

	@SuppressWarnings("unchecked")
	private SeatHold shiftExpiredSeatsToNewHold(int numSeats,
			Optional<Integer> minLevel, Optional<Integer> maxLevel, User user,
			SeatHold seatHold) throws TicketingException {
		Session session = sessionFactory.getCurrentSession();
		List<SeatMap> list = null;
		int insertedSeats = 0;
		try {
			session.beginTransaction();
			list = session
					.createQuery(
							"from SeatMap where status = 0 and createdDate <= :date and id.level between :maxLevel and :minLevel order by id.level, createdDate")
					.setInteger("minLevel", minLevel.get())
					.setInteger("maxLevel", maxLevel.get())
					.setParameter("date", DateUtils.getOutdatedDate())
					.setMaxResults(numSeats).list();
			if (list != null && list.size() >= numSeats) {
				if (seatHold == null) {
					seatHold = new SeatHold(user, numSeats, new Date());
					session.save(seatHold);
				}
				for (SeatMap seatMap : list) {
					if (seatMap != null) {
						seatMap.setHold(seatHold);
						// set new hold id for existing seat records which were
						// booking timed out
						seatMap.setCreatedDate(new Date());
						// update date to current
						session.update(seatMap);
						insertedSeats++;
					}
				}
				logger.debug(insertedSeats
						+ " - seats changed to a different hold id as they were expired");
			}
			seatHold = bookAllOrCancel(numSeats, session, seatHold,
					insertedSeats);
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem booking tickets due to techincal issues");
		}
		return seatHold;
	}

	private SeatHold bookAllOrCancel(int numSeats, Session session,
			SeatHold seatHold, int insertedSeats) {
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

	public SeatHold holdAvailableSeats(int numSeats,
			Optional<Integer> minLevel, Optional<Integer> maxLevel, User user)
			throws TicketingException {
		logger.debug("holdAvailableSeats - Hold seats which were not held / left by someone before");
		SeatHold seatHold = holdSeatsOnCondition(numSeats, minLevel, maxLevel,
				user, false);
		logger.debug("holdAvailableSeats - finished");
		return seatHold;
	}

	private SeatHold holdSeatsOnCondition(int numSeats,
			Optional<Integer> minLevel, Optional<Integer> maxLevel, User user,
			boolean commitIfLessSeats) throws TicketingException {
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
						.createQuery(
								"from SeatMap where id.level = :level order by id.rowid desc, id.seatid desc")
						.setInteger("level", levelCurrent).setMaxResults(1)
						.list();

				if (list == null || list.size() == 0) {
					// day zero entry in a level
					seatMap = new SeatMap(new SeatMapId(levelCurrent,
							TicketConstants.SHOW_ID, TicketConstants.NEXT_ROW,
							TicketConstants.NEXT_SEAT), new Status(), seatHold,
							new Date());
					logger.debug("First seat in level booked");
					session.save(seatMap);
					bookedTickets++;
				} else {
					// seat with max row number and seat number
					seatMap = list.get(0);
					seatMap.setHold(seatHold);
					if (seatMap.getId().getSeatid() < levelMaster
							.getNoOfseats()) {
						tempSeatId = seatMap.getId().getSeatid()
								+ TicketConstants.NEXT_SEAT;
						tempRowId = seatMap.getId().getRowid();

						// seat available in same row
						blockSeat(session, seatHold, tempRowId, levelCurrent,
								tempSeatId);
						bookedTickets++;
					} else if (seatMap.getId().getRowid() < levelMaster
							.getNoOfRows()) {
						tempRowId = seatMap.getId().getRowid()
								+ TicketConstants.NEXT_ROW;
						tempSeatId = TicketConstants.NEXT_SEAT;

						// next row available in same level
						blockSeat(session, seatHold, tempRowId, levelCurrent,
								tempSeatId);
						bookedTickets++;
						logger.debug("First seat in row " + tempRowId
								+ " booked.");
					} else if (seatMap.getId().getRowid() >= levelMaster
							.getNoOfRows()
							&& seatMap.getId().getSeatid() >= levelMaster
									.getNoOfseats()
							&& levelCurrent < minLevel.get()) {
						levelCurrent++;
						// if rows and seats are done in one level
						// swith next min level
						logger.debug("Switching to next selected level, as seats done in current level.");
					} else if (seatMap.getId().getRowid() >= levelMaster
							.getNoOfRows()
							&& seatMap.getId().getSeatid() >= levelMaster
									.getNoOfseats()
							&& levelCurrent == minLevel.get()) {
						// if in selected levels, rows and seats are done then
						// no more seats, break the loop
						logger.debug("All seats in all levels covered.");
						break;
					}
				}
			}
			seatHold = bookOrRevert(numSeats, session, seatHold, bookedTickets,
					commitIfLessSeats);
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem booking tickets due to techincal issues");
		} catch (Exception e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem booking tickets due to techincal issues");
		}
		return seatHold;
	}

	private void blockSeat(Session session, SeatHold seatHold, int tempRowId,
			int levelCurrent, int tempSeatId) {
		SeatMap seatMap;
		seatMap = new SeatMap(new SeatMapId(levelCurrent,
				TicketConstants.SHOW_ID, tempRowId, tempSeatId), new Status(),
				seatHold, new Date());
		session.save(seatMap);
	}

	private SeatHold bookOrRevert(int numSeats, Session session,
			SeatHold seatHold, int bookedTickets, boolean commitIfLessSeats) {
		if (bookedTickets == numSeats || commitIfLessSeats) {
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

	@SuppressWarnings("unchecked")
	public List<SeatHold> getHeldSeats(User user) throws TicketingException {
		logger.debug("getHeldSeats - Started");
		Session session = sessionFactory.openSession();
		List<SeatHold> list = null;
		try {
			session.beginTransaction();
			list = session
					.createQuery("from SeatHold where user.userId = :userIdNo")
					.setInteger("userIdNo", user.getUserId()).list();
			session.getTransaction().commit();
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem booking tickets due to techincal issues");
		} catch (Exception e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem booking tickets due to techincal issues");
		}
		logger.debug("getHeldSeats - finished");
		return list;
	}

	@SuppressWarnings("unchecked")
	public Reservation confirmSeats(SeatHold seatHold, User user)
			throws TicketingException {
		logger.debug("confirmSeats - Started");
		Session session = sessionFactory.openSession();
		Reservation reservation = null;
		List<SeatHold> list = null;
		List<SeatMap> map = null;
		Status statusName = null;
		SeatHold hold = null;
		int noOfSeats = 0, confirmedSeats = 0;
		try {
			session.beginTransaction();
			list = session
					.createQuery("from SeatHold where holdId = :holdIdNo")
					.setInteger("holdIdNo", seatHold.getHoldId()).list();
			if (list != null && list.size() > 0) {
				statusName = new Status();
				statusName.setStatusId(TicketConstants.STATUS_CONFIRMED);
				hold = list.get(0);
				noOfSeats = hold.getNoOfseats();
				map = list.get(0).getSeatmaps();
				for (SeatMap seat : map) {
					// confirm only booked within allowed hold time
					if (seat != null
							&& seat.getCreatedDate() != null
							&& seat.getCreatedDate().after(
									DateUtils.getOutdatedDate())) {
						if (seat != null
								&& seat.getStatus().getStatusId() == TicketConstants.STATUS_CONFIRMED) {
							throw new TicketingException(105,
									"Ticket already confirmed.");
						}
						seat.setStatus(statusName);
						session.save(seat);
						confirmedSeats++;
					} else {
						// Break the loop as held timeout happened for just one
						// ticket
						// break;
					}
				}
				if (confirmedSeats == noOfSeats) {
					// if all seats are confirmed, create a new reservation
					// entity
					reservation = new Reservation(user, hold, new Date());
					session.save(reservation);
					session.getTransaction().commit();
					logger.info("Reservation confirmed");
				} else {
					businessException(seatHold, session);
				}
			} else {
				businessException(seatHold, session);
			}
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem booking tickets due to techincal issues");
		}
		logger.debug("confirmSeats - done");
		return reservation;
	}

	private void businessException(SeatHold seatHold, Session session)
			throws TicketingException {
		session.getTransaction().rollback();
		logger.error("Held Seats were expired for hold id"
				+ seatHold.getHoldId());
		throw new TicketingException(
				101,
				"Since you delayed, Held Seats were expired, please try to hold seats once again");
	}

	public SeatHold holdRemainBeforeFullSeats(int numSeats,
			Optional<Integer> minLevel, Optional<Integer> maxLevel, User user)
			throws TicketingException {
		SeatHold hold = null;
		int remainingToBeBooked = 0;
		if (getAllAvailableValidSeats(minLevel, maxLevel) >= numSeats) {
			// category 1 - SeatMaps - that are not touched / held - seatmap -
			// category 2 - Held seats , which are expired
			// category 3 - Both 1 and 2
			// somehow in Cat 1 or 2, fill seats
			hold = holdSeatsOnCondition(numSeats, minLevel, maxLevel, user,
					true);
			remainingToBeBooked = numSeats - hold.getNoOfseats();
			hold = shiftExpiredSeatsToNewHold(remainingToBeBooked, minLevel,
					maxLevel, user, hold);
		}
		return hold;
	}

	@SuppressWarnings("unchecked")
	private int getAllAvailableValidSeats(Optional<Integer> minLevel,
			Optional<Integer> maxLevel) throws TicketingException {
		Session session = sessionFactory.openSession();
		List<LevelMaster> list = null;
		int totalSeats = 0;
		int validOrBookedSeatsCount = 0;
		List<SeatMap> validOrBookedSeats = null;
		try {
			session.beginTransaction();
			list = session
					.createQuery(
							"from LevelMaster where level between :maxLevel and :minLevel")
					.setInteger("maxLevel", maxLevel.get())
					.setInteger("minLevel", minLevel.get()).list();
			for (LevelMaster levelMaster : list) {
				totalSeats += levelMaster.getNoOfRows()
						* levelMaster.getNoOfRows();
			}

			validOrBookedSeats = session
					.createQuery(
							"from SeatMap where status=1 and id.level between :maxLevel and :minLevel and createdDate > :date")
					.setInteger("maxLevel", maxLevel.get())
					.setInteger("minLevel", minLevel.get())
					.setParameter("date", DateUtils.getOutdatedDate()).list();
			if (validOrBookedSeats != null) {
				validOrBookedSeatsCount = validOrBookedSeats.size();

			}
			totalSeats = totalSeats - validOrBookedSeatsCount;
			session.getTransaction().commit();
		} catch (HibernateException e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem booking tickets due to techincal issues");
		} catch (Exception e) {
			logger.fatal(e.getMessage());
			session.getTransaction().rollback();
			throw new TicketingException(102,
					"Problem booking tickets due to techincal issues");
		}
		logger.debug("getAllAvailableValidSeats - finished");
		return totalSeats;
	}
}