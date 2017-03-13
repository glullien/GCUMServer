<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <title>GCUM Désabonnement</title>
</head>
<body>
<c:choose>
    <c:when test="${empty param.code}">
        Page invalide
    </c:when>
    <c:otherwise>
        <c:set var="username" value="${gcum:getRemoveFromMailsUserName(param.code)}"/>
        <c:choose>
            <c:when test="${empty username}">
                Page invalide
            </c:when>
            <c:otherwise>
                <p>Bonjour ${username},</p>
                <c:set var="removed" value="${gcum:removeFromNews(username)}"/>
                <c:choose>
                    <c:when test="${removed}">
                        <p>Vous ne recevrez plus de notifications à chaque nouvelle version</p>
                    </c:when>
                    <c:otherwise>
                        <p>Il y a eu une erreur...</p>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
</body>
</html>
