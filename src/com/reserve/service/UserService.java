package com.reserve.service;

import com.reserve.vo.User;

public interface UserService {
	public User getUserOrSave(User user);
}