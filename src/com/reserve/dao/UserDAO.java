package com.reserve.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.reserve.vo.User;

public class UserDAO {
	private SessionFactory sessionFactory;

	public UserDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public UserDAO() {
		// TODO Auto-generated constructor stub
	}

	public User getUserOrCreate(User user) {
		Session session = sessionFactory.getCurrentSession();
		List<User> list = null;
		try {
			session.beginTransaction();
			list = (List<User>) session.createCriteria(User.class)
					.add(Restrictions.eq("email", user.getEmail())).list();
			if (list != null && list.size() > 0) {
				user = list.get(0);
			} else {
				session.save(user);
			}
			session.getTransaction().commit();
		} catch (HibernateException e) {
			session.getTransaction().rollback();
		}
		return user;
	}

}
