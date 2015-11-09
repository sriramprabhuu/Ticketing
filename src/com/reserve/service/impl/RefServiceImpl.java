package com.reserve.service.impl;

import java.util.List;

import com.reserve.dao.ReferenceDAO;
import com.reserve.service.RefService;
import com.reserve.vo.LevelMaster;
import com.reserve.vo.Status;

public class RefServiceImpl implements RefService {

	private ReferenceDAO refDAO;

	public RefServiceImpl() {
	}

	public RefServiceImpl(ReferenceDAO refDAOIn) {
		refDAO = refDAOIn;
	}

	@Override
	public List<LevelMaster> getLevels() {
		return refDAO.getLevels();
	}
}