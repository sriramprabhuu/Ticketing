package com.reserve.service.impl;

import com.reserve.dao.UserDAO;
import com.reserve.service.UserService;
import com.reserve.vo.User;

public class UserServiceImpl implements UserService {

	private UserDAO userDAO;

	public UserServiceImpl() {
	}

	public UserServiceImpl(UserDAO userDAOIn) {
		userDAO = userDAOIn;
	}

	@Override
	public User getUserOrSave(User user) {
		return userDAO.getUserOrCreate(user);
	}

}