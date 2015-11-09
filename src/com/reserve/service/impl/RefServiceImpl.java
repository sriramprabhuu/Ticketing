package com.reserve.service.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.reserve.dao.ReferenceDAO;
import com.reserve.exception.TicketingException;
import com.reserve.service.RefService;
import com.reserve.vo.LevelMaster;

public class RefServiceImpl implements RefService {

	final static Logger logger = Logger.getLogger(RefServiceImpl.class);

	private ReferenceDAO refDAO;

	public RefServiceImpl() {
	}

	public RefServiceImpl(ReferenceDAO refDAOIn) {
		refDAO = refDAOIn;
	}

	@Override
	public List<LevelMaster> getLevels() throws TicketingException {
		logger.debug("getLevels - ref data service");
		return refDAO.getLevels();
	}
}