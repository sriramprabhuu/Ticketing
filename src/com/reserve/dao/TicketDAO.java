package com.reserve.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.reserve.constants.TicketConstants;
import com.reserve.vo.LevelMaster;
import com.reserve.vo.SeatHold;
import com.reserve.vo.SeatMap;
import com.reserve.vo.SeatMapId;
import com.reserve.vo.Status;
import com.reserve.vo.User;

public class TicketDAO {
	private SessionFactory sessionFactory;

	public TicketDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public TicketDAO() {
		// TODO Auto-generated constructor stub
	}

	public LevelMaster getTotalNumberOfSeats(Optional<Integer> levelId) {
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
			session.getTransaction().rollback();
		}
		return levelMaster;
	}

	public List<SeatMap> getFilledSeats(Optional<Integer> venueLevel) {
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
			session.getTransaction().rollback();
		}
		return list;
	}

	public SeatHold holdExistingHeldOutdatedSeats(int numSeats,
			Optional<Integer> minLevel, Optional<Integer> maxLevel, User user) {
		Session session = sessionFactory.getCurrentSession();
		SeatHold seatHold = null;
		List<SeatMap> list = null;
		int insertedSeats = 0;
		try {
			session.beginTransaction();
			list = session
					.createQuery(
							"from SeatMap where status = 0 and createdDate <= :date and id.level between :maxLevel and :minLevel order by id.level, createdDate")
					.setInteger("minLevel", minLevel.get())
					.setInteger("maxLevel", maxLevel.get())
					.setParameter("date", getOutdatedDate()).setMaxResults(numSeats).list();
			if (list != null && list.size() >= numSeats) {
				seatHold = new SeatHold(user, numSeats, new Date());
				session.save(seatHold);
				for (SeatMap seatMap : list) {
					seatMap.setHold(seatHold);
					seatMap.setCreatedDate(new Date());
					session.update(seatMap);
					insertedSeats++;
				}
			}
			if (numSeats == insertedSeats) {
				session.getTransaction().commit();
			} else {
				if(seatHold == null)
				{
					seatHold = new SeatHold();
				}
				seatHold.setNoOfseats(0);;
				session.getTransaction().rollback();
			}
		} catch (HibernateException e) {
			session.getTransaction().rollback();
		}
		return seatHold;
	}

	private Date getOutdatedDate() {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, -(TicketConstants.HOLD_MINUTES));
		return calendar.getTime();
	}

	public SeatHold holdAvailableSeats(int numSeats,
			Optional<Integer> minLevel, Optional<Integer> maxLevel, User user) {
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
						.setInteger("level", levelCurrent).setMaxResults(1).list();

				if (list == null || list.size() == 0) {
					// day zero entry in a level
					seatMap = new SeatMap(
							new SeatMapId(levelCurrent,
									TicketConstants.SHOW_ID,
									TicketConstants.NEXT_ROW,
									TicketConstants.NEXT_SEAT), new Status(),
							seatHold, new Date());
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
						seatMap = new SeatMap(
								new SeatMapId(
										levelCurrent, TicketConstants.SHOW_ID,
										tempRowId, tempSeatId), new Status(),
								seatHold, new Date());
						session.save(seatMap);
						bookedTickets++;
					} else if (seatMap.getId().getRowid() < levelMaster
							.getNoOfRows()) {
						// row available in same level
						tempRowId = seatMap.getId().getRowid()
								+ TicketConstants.NEXT_ROW;
						tempSeatId = TicketConstants.NEXT_SEAT;
						seatMap = new SeatMap(
								new SeatMapId(
										levelCurrent, TicketConstants.SHOW_ID,
										tempRowId, tempSeatId), new Status(),
								seatHold, new Date());
						session.save(seatMap);
						bookedTickets++;
					} else if (seatMap.getId().getRowid() >= levelMaster
							.getNoOfRows()
							&& seatMap.getId().getSeatid() >= levelMaster
									.getNoOfseats()
							&& levelCurrent < minLevel.get()) {
						levelCurrent++;
						// if rows and seats are done in one level
						// swith next min level
					} else if (seatMap.getId().getRowid() >= levelMaster
							.getNoOfRows()
							&& seatMap.getId().getSeatid() >= levelMaster
									.getNoOfseats()
							&& levelCurrent == minLevel.get()) {
						// if in selected levels, rows and seats are done then
						// no more seats, break the loop
						break;
					}
				}
			}
			if (bookedTickets == numSeats) {
				session.getTransaction().commit();
			} else {
				// if not enough tickets available, do not create any records
				// no partial booking
				if(seatHold == null)
				{
					seatHold = new SeatHold();
				}
				seatHold.setNoOfseats(0);;
				session.getTransaction().rollback();
			}
		} catch (HibernateException e) {
			session.getTransaction().rollback();
		} catch (Exception e) {
			session.getTransaction().rollback();
		}
		return seatHold;
	}
}