package com.reserve.service;

import java.util.List;

import com.reserve.exception.TicketingException;
import com.reserve.vo.LevelMaster;

public interface RefService {
	public List<LevelMaster> getLevels() throws TicketingException;
}