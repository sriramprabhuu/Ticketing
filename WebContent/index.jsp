<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"    "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Ticketing</title>
</head>
<script type="text/javascript">
	function loadRefValues() {
		var xmlHttpRequest = getXMLHttpRequest();
		//xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest);
		xmlHttpRequest.open("POST", "/ReferenceValues", true);
		xmlHttpRequest.setRequestHandler("Content-Type",
				"application/x-www-form-urlencoded");
		xmlHttpRequest.send(null);
	}

	function getReadyStateHandler(xmlHttpRequest) {
		return function() {
			if (xmlHttpRequest.readyState == 4) {
				if (xmlHttpRequest.status == 200) {
				}
			}
			;
		}
	}
</script>
<body>
	<form:form action="findSeats.do" commandName="seatmapId">
		<img alt="Booking" src="imgTick1.png" />
		<table>
			<tr>
				<td><form:select path="level">
						<form:option value="" label="--- Select Level---" />
						<form:options items="${levelList}" />
					</form:select></td>
			</tr>
			<tr>
				<td><input value="Find Availability" type="submit"
					name="submit"></td>
			</tr>
		</table>
	</form:form>
</body>
</html>