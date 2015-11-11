<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"    "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Sriram's Ticketing - Confirm</title>
</head>
<link rel="stylesheet" href="Style.css" type="text/css" media="screen">
<body>
	<form:form action="confirmSeats.do" commandName="registerVO">
		<table align="center">

			<tr align="center">
				<td colspan="3"><font color="purple" size="6"><b>Ticket
							Booking</b></font></td>
			</tr>

		</table>
		<table align="center">
			<tr>
				<td colspan="2"><img alt="Booking" src="imgTick.png" /></td>
			</tr>
			<tr>
				<td><b>Email</b></td>
				<td><form:input type="text" path="email" /></td>
			</tr>
			<%-- <tr>
				<td><b>Mobile</b></td>
				<td><c:out value="${registerVO.mobileNo}" /></td>
			</tr> --%>
			<tr>
				<td><b>Hold Id</b></td>
				<td><form:input type="text" path="holdId" /></td>
			</tr>
			<tr>
				<td colspan="2" align="left"><b><font color="red"><c:if
								test="${message!=null}">${message}</c:if></font> <form:errors
							path="errorMessage" cssClass="errors" /> <BR> <form:errors
							path="email" cssClass="errors" /> <BR> <form:errors
							path="holdId" cssClass="errors" /> </b></td>
			</tr>
			<tr>
				<td colspan="2"><input value="Confirm Seats" type="submit"
					name="Submit"></td>
			</tr>
		</table>
		<table align="left" height="50px">

			<tr align="left">
				<td colspan="3"><I><B><font style="">Ticketing
								application working with functionality mentioned in the
								requirements document, built using Spring 4, Hibernate 4, MySQL.<BR>
								<BR>Logical Domain Model can be found at : <a
								href="ER_Diagram_Ticketing.pdf">ER_Ticketing.pdf</a> <BR> <BR>Every
								bit of code found here are created from scratch and not copied
								from any place. <BR> <BR>**Seat map will be printed
								here (with level , row number & Seat number) for each ticket.<BR>
								<BR>
						</font> <font size="3"><b> - Sriram Rajendran</b></font></B> </I></td>
			</tr>
		</table>
	</form:form>
</body>
</html>