<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"    "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Sriram's Ticketing</title>
</head>
<script type="text/javascript">
	function showOrHide() {
		if (document.getElementById("showOfHide").value == '1') {
			document.getElementById("userDetails").style.display = 'block';
		}
		if (document.getElementById("showOfHide").value == '0') {
			document.getElementById("userDetails").style.display = 'none';
		}
	}
	function show() {
		document.getElementById("showOfHide").value = '1';
		document.getElementById("userDetails").style.display = 'block';
	}

	function holdSeats() {
		document.forms[0].action = "holdSeats.do";
		document.forms[0].submit();
	}

	function ConfirmTickets() {
		window.location.href = 'goToConfirm.do';
	}
</script>
<link rel="stylesheet" href="Style.css" type="text/css" media="screen">
<body onload="javascript:showOrHide()">
	<form:form action="findSeats.do" commandName="registerVO">

		<table align="center">

			<tr align="center">
				<td colspan="3"><font color="purple" size="6"><b>Ticket
							Booking</b></font></td>
			</tr>

		</table>
		<table align="center">
			<tr align="center">
				<td colspan="3"><img alt="Booking" src="imgTick.png" /></td>
			</tr>
			<tr>
				<td><b>Levels :</b></td>
				<td colspan="2"><form:select path="selectedLevelId">
						<form:option value="0" label="--- Select Level---" />
						<form:options items="${sessionScope.levelMasterList}" />
					</form:select></td>
			</tr>
			<tr>
				<td colspan="3" align="left"><b><form:errors
							path="errorMessage" cssClass="errors" /> <BR> <form:errors
							path="selectedLevelId" cssClass="errors" /> <font color="red"><c:if
								test="${message!=null}">${message}</c:if></font></b></td>
			</tr>
			<tr>
				<td><input value="Find Availability" type="submit"
					name="submit"></td>
				<td><input value="Proceed to Booking" type="button"
					name="submit" onclick="javascript:show();"></td>
				<td><input value="Confirm Tickets" type="button" name="submit"
					onclick="javascript:ConfirmTickets();"></td>
			</tr>
		</table>
	</form:form>
	<div id="userDetails" style="display: block">
		<form:form action="holdSeats.do" commandName="registerVO">
			<form:hidden path="flagShow" id="showOfHide" />
			<table align="center">
				<tr>
					<td><b>Email</b></td>
					<td><form:input type="text" path="email" /></td>
				</tr>

				<!-- <tr>
					<td><b>Mobile</b></td>
					<td><form:input type="text" path="mobileNo" /></td>
				</tr> -->
				<tr>
					<td><b>Number of Seats</b></td>
					<td><form:input type="text" path="noOfSeats" /></td>
				</tr>
				<tr>
					<td><b>Maximum Level</b></td>
					<td><form:select path="maxLevel">
							<form:option value="0" label="--- Select Level---" />
							<form:options items="${sessionScope.levelMasterList}" />
						</form:select></td>
				</tr>
				<tr>
					<td><b>Minimum Level</b></td>
					<td><form:select path="minLevel">
							<form:option value="0" label="--- Select Level---" />
							<form:options items="${sessionScope.levelMasterList}" />
						</form:select></td>
				</tr>

				<tr>
					<td colspan="2" align="left"><b><form:errors
								path="errorMessage" cssClass="errors" /> <BR> <form:errors
								path="email" cssClass="errors" /> <BR> <!-- <form:errors
								path="mobileNo" cssClass="errors" /> <BR> --> <form:errors
								path="noOfSeats" cssClass="errors" /> <BR> <form:errors
								path="minLevel" cssClass="errors" /> <font color="red"><c:if
									test="${errorMessage!=null}">${errorMessage}</c:if></font></b></td>
				</tr>
				<tr>
					<td colspan="2"><input value="Hold Seats" type="submit"
						name="submit"></td>
				</tr>
			</table>

		</form:form>
	</div>

	<table align="left" height="50px">

		<tr align="left">
			<td colspan="3"><I><B><font style="">Ticketing
							application working with functionality mentioned in the
							requirements document <a href="TicketServiceHomework.pdf">
								(TicketService)</a>, built using Spring 4, Hibernate 4, MySQL.<BR>
							<BR>Logical Domain Model can be found at : <a
							href="ER_Diagram_Ticketing.pdf">ER_Ticketing.pdf</a> <BR> <BR>Every
							bit of code found here are created from scratch and not copied
							from any place. <BR> <BR>**Seat map will be printed
							here (with level , row number & Seat number) for each ticket.<BR>
							<BR>
					</font> <font size="3"><b> - Sriram Rajendran</b></font></B> </I></td>
		</tr>
	</table>
</body>
</html>