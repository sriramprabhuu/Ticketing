package com.reserve.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.reserve.vo.LevelMaster;

public class ReferenceDAO {
	private SessionFactory sessionFactory;

	public ReferenceDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public ReferenceDAO() {
		// TODO Auto-generated constructor stub
	}

	public List getLevels() {
		Session session = sessionFactory.getCurrentSession();
		List list = null;
		try {
			session.beginTransaction();
			list = (List<LevelMaster>) session.createQuery("from LevelMaster")
					.list();
		} catch (HibernateException e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		session.getTransaction().commit();
		return list;
	}

}