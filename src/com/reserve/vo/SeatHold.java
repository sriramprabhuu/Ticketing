package com.reserve.vo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Hold generated by hbm2java
 */
public class SeatHold implements java.io.Serializable {

	private Integer holdId;
	private User user;
	private int noOfseats;
	private Date createdDate;
	private Set seatmaps = new HashSet(0);
	private Set reservations = new HashSet(0);

	public SeatHold() {
	}

	public SeatHold(User user, int noOfseats, Date createdDate) {
		this.user = user;
		this.noOfseats = noOfseats;
		this.createdDate = createdDate;
	}

	public SeatHold(User user, int noOfseats, Date createdDate, Set seatmaps, Set reservations) {
		this.user = user;
		this.noOfseats = noOfseats;
		this.createdDate = createdDate;
		this.seatmaps = seatmaps;
		this.reservations = reservations;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Seats are - ");
		for (Object object : seatmaps) {
			buffer.append(object.toString()).append(", ");
		}
		return buffer.toString();
	}

	public Integer getHoldId() {
		return this.holdId;
	}

	public void setHoldId(Integer holdId) {
		this.holdId = holdId;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getNoOfseats() {
		return this.noOfseats;
	}

	public void setNoOfseats(int noOfseats) {
		this.noOfseats = noOfseats;
	}

	public Date getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Set getSeatmaps() {
		return this.seatmaps;
	}

	public void setSeatmaps(Set seatmaps) {
		this.seatmaps = seatmaps;
	}

	public Set getReservations() {
		return this.reservations;
	}

	public void setReservations(Set reservations) {
		this.reservations = reservations;
	}

}
