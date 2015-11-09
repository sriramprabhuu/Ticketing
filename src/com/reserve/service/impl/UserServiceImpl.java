package com.reserve.service.impl;

import org.apache.log4j.Logger;

import com.reserve.dao.UserDAO;
import com.reserve.exception.TicketingException;
import com.reserve.service.UserService;
import com.reserve.vo.User;

public class UserServiceImpl implements UserService {
	final static Logger logger = Logger.getLogger(UserServiceImpl.class);

	private UserDAO userDAO;

	public UserServiceImpl() {
	}

	public UserServiceImpl(UserDAO userDAOIn) {
		userDAO = userDAOIn;
	}

	@Override
	public User getUserOrSave(User user) throws TicketingException {
		logger.debug("getUserOrSave Impl");
		return userDAO.getUserOrCreate(user);
	}

}