package com.reserve.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.reserve.exception.TicketingException;
import com.reserve.vo.LevelMaster;

public class ReferenceDAO {
	
	final static Logger logger = Logger.getLogger(ReferenceDAO.class);
	
	private SessionFactory sessionFactory;

	public ReferenceDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ReferenceDAO() {
		// TODO Auto-generated constructor stub
	}

	public List getLevels() throws TicketingException {
		Session session = sessionFactory.getCurrentSession();
		List list = null;
		try {
			session.beginTransaction();
			list = (List<LevelMaster>) session.createQuery("from LevelMaster")
					.list();
			logger.debug("Loaded ref data");
		} catch (HibernateException e) {
			throw new TicketingException(102, "Failure connecting to DB due to techincal issues");
		}
		return list;
	}

}