<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"    "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Ticketing - Confirm</title>
</head>
<script type="text/javascript">
	function confirmSeats() {
		document.forms[0].action = "confirmSeats.do";
		document.forms[0].submit();
	}
</script>
<link rel="stylesheet" href="Style.css" type="text/css" media="screen">
<body>
	<form:form action="findSeats.do" commandName="registerVO">
		<table align="center">
			<tr>
				<td colspan="2"><img alt="Booking" src="imgTick.png" /></td>
			</tr>
			<tr>
				<td><b>Email</b></td>
				<td><c:out value="${registerVO.email}" /></td>
			</tr>
			<tr>
				<td><b>Mobile</b></td>
				<td><c:out value="${registerVO.mobileNo}" /></td>
			</tr>
			<tr>
				<td><b>Selected Seats</b></td>
				<td><c:out value="${registerVO.noOfSeats}" /></td>
			</tr>
			<tr>
				<td><b>Hold Id</b></td>
				<td><form:input type="text" path="holdId" /></td>
			</tr>
			<tr>
				<td colspan="2"><input value="Confirm Seats" type="button"
					name="submit" onclick="javascript:confirmSeats();"></td>
			</tr>
		</table>
	</form:form>
</body>
</html>