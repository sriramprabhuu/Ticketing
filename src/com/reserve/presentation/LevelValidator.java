package com.reserve.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.reserve.vo.RegisterVO;
import com.reserve.vo.SeatMapId;

public class LevelValidator implements Validator {

	@Override
	public boolean supports(Class<?> seatMapId) {
		return RegisterVO.class.isAssignableFrom(seatMapId);
	}

	@Override
	public void validate(Object arg0, Errors errors) {
		RegisterVO mapId = (RegisterVO) arg0;
		if (mapId.getSelectedLevelId() < 1) {
			errors.rejectValue("selectedLevelId", "selectedLevelId.invalid",
					"Level not selected, Please select a Level.");
		}
	}

	@Override
	public void validateBeforeHolding(Object arg0, Errors errors) {
		RegisterVO mapId = (RegisterVO) arg0;
		if (mapId.getEmail() == null || mapId.getEmail().equals("")
				|| !isValidEmailAddress(mapId.getEmail())) {
			errors.rejectValue("email", "email.invalid",
					"Invalid email, please give a valid email.");
		}
		if (mapId.getMobileNo() != null
				&& !mapId.getMobileNo().trim().equals("")
				&& !mapId.getMobileNo().trim()
						.matches("(001|0|\\+1)[7-9][0-9]{9}")) {
			errors.rejectValue("mobileNo", "mobileNo.invalid",
					"Invalid Mobile number, please enter a valid entry.");
		}
		if (mapId.getNoOfSeats() < 1) {
			errors.rejectValue("noOfSeats", "noOfSeats.invalid",
					"Enter a valid number of seats.");
		}
		if (mapId.getMaxLevel() < 1 && mapId.getMinLevel() < 1) {
			errors.rejectValue("minLevel", "minLevel.invalid",
					"Level not selected, Please select appropriate level.");
		}

	}

	public boolean isValidEmailAddress(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}

}
