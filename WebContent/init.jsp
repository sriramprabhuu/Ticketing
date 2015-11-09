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
	function loadReferenceValues() {
		document.forms[0].action = "loadHome.do";
		document.forms[0].submit();
	}
</script>
<body onload="javascript:loadReferenceValues();">
	<form:form name="refForm" method="post">

	</form:form>
</body>
</html>