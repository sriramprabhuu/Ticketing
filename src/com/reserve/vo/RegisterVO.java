package com.reserve.vo;

public class RegisterVO {
	private String email;
	private int minLevel;
	private int maxLevel;
	private int noOfSeats;
	private int selectedLevelId;
	private String mobileNo;
	private int holdId;
	private int confirmId;
	private int flagShow;

	private String errorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public int getFlagShow() {
		return flagShow;
	}

	public void setFlagShow(int flagShow) {
		this.flagShow = flagShow;
	}

	public int getConfirmId() {
		return confirmId;
	}

	public void setConfirmId(int confirmId) {
		this.confirmId = confirmId;
	}

	public int getHoldId() {
		return holdId;
	}

	public void setHoldId(int holdId) {
		this.holdId = holdId;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public int getNoOfSeats() {
		return noOfSeats;
	}

	public void setNoOfSeats(int noOfSeats) {
		this.noOfSeats = noOfSeats;
	}

	public int getSelectedLevelId() {
		return selectedLevelId;
	}

	public void setSelectedLevelId(int selectedLevelId) {
		this.selectedLevelId = selectedLevelId;
	}

}