<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <c:if test="${not gcum:isLogin(sessionScope.sessionId)}">
        <meta http-equiv="refresh" content="0; url=index.jsp"/>
    </c:if>
    <title>Changer les notifications</title>
    <meta name="viewport" content="width=device-width"/>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" type="text/css" href="stylesheets/notifications.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/notifications.js"></script>
    <script type="text/javascript" src="scripts/shared.js"></script>
    <c:if test="${not gcum:isLogin(sessionScope.sessionId)}">
        <script type="text/javascript">
			autoLogin();
        </script>
    </c:if>
</head>
<body>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="links">
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-map-marker"></i><span class="hideOnMobile link">Carte</span></a>
        <a href="list.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-align-justify"></i><span class="hideOnMobile link">Liste</span></a>
        <a href="add.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-upload"></i><span class="hideOnMobile link">Ajouter</span></a>
        <c:if test="${gcum:isAdmin(sessionScope.sessionId)}">
            <a href="extract.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-download"></i><span
                    class="hideOnMobile link">Extrait</span></a>
        </c:if>
        <a href="info.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-info-sign"></i></a>
        <div class="dropdown" style="display: inline; margin: 0; padding: 0;top: -1px">
            <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                <i class="glyphicon glyphicon-user"></i>
                <span class="hideOnMobile">${gcum:username(sessionScope.sessionId)}</span>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu dropdown-menu-right">
                <li class="showOnMobile"><a href="#" id="accountName">Compte de ${gcum:username(sessionScope.sessionId)}</a></li>
                <li><a href="changeEmail.jsp">Changer d'adresse email</a></li>
                <li><a href="changePassword.jsp">Changer le mot de passe</a></li>
                <li><a id="disconnect" href="#">Se déconnecter</a></li>
            </ul>
        </div>
    </div>
</div>
<div class="well">
    <c:if test="${gcum:isLogin(sessionScope.sessionId)}">
        <form class="form-horizontal">
            <c:if test="${not gcum:hasEmail(sessionScope.sessionId)}">
                <div class="form-group notificationGroup">
                    <div class="checkbox col-sm-7">
                        Vous n'avez renseigné aucune adresse Email, vous ne pouvez pas recevoir de notification.
                        <a href="changeEmail.jsp">Pour ajouter un email.</a>
                    </div>
                </div>
            </c:if>
            <div class="form-group notificationGroup" id="emailGroup">
                <span class="col-sm-3 notificationName">Lorsqu'une de vos photos est aimée</span>
                <div class="checkbox col-sm-7">
                    <c:if test="${gcum:hasEmail(sessionScope.sessionId)}">
                        <label class="notificationCheckBox"><input type="checkbox"
                                                                   id="likedEmail" ${gcum:isReceivingMailForLiked(sessionScope.sessionId)?"checked":""}>
                            Recevoir un
                            email</label>
                    </c:if>
                </div>
            </div>
            <div class="form-group" id="newsGroup">
                <span class="col-sm-3 notificationName">Pour les informations générales sur le site</span>
                <div class="checkbox col-sm-7">
                    <c:if test="${gcum:hasEmail(sessionScope.sessionId)}">
                        <label class="notificationCheckBox"><input type="checkbox"
                                                                   id="newsEmail" ${gcum:isReceivingMailForNews(sessionScope.sessionId)?"checked":""}> Recevoir
                            un
                            email</label>
                    </c:if>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                    <button type="button" class="btn btn-success" id="submit">Changer</button>
                    <span class="alert" id="status"></span>
                </div>
            </div>
        </form>
    </c:if>
</div>

<div class="modal fade" id="successModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <p>La configuration a été changée</p>
            </div>
            <div class="modal-footer">
                <a href="index.jsp" class="btn btn-success">Ok</a>
            </div>
        </div>
    </div>
</div>

</body>
</html>
