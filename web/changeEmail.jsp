<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<!doctype html>
<html>
<head>
    <c:if test="${not gcum:isLogin(sessionScope.sessionId)}">
        <meta http-equiv="refresh" content="0; url=index.jsp"/>
    </c:if>
    <title>GCUM Changer d'adresse email</title>
    <meta name="viewport" content="width=device-width"/>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/changeEmail.js"></script>
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
            <a href="extract.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-cloud-download"></i><span class="hideOnMobile link">Extrait</span></a>
        </c:if>
        <a href="info.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-info-sign"></i></a>
        <div class="dropdown" style="display: inline; margin: 0; padding: 0;top: -1px">
            <button class="btn btn-outline-primary btn-sm dropdown-toggle" type="button" data-toggle="dropdown">
                <i class="glyphicon glyphicon-user"></i>
                <span>${gcum:username(sessionScope.sessionId)}</span>
                <span class="caret"></span>
            </button>
            <ul class="dropdown-menu dropdown-menu-right">
                <li><a href="changePassword.jsp">Changer le mot de passe</a></li>
                <li><a id="disconnect" href="#">Se déconnecter</a></li>
            </ul>
        </div>
    </div>
</div>
<div class="well">
    <form class="form-horizontal">
        <div class="form-group" id="emailGroup">
            <label class="control-label col-sm-2" for="email">Email (facultatif) :</label>
            <div class="col-sm-10">
                <input type="email" class="form-control" id="email" placeholder="Entrer votre email pour pouvoir réinitialiser votre mot de passe"
                       value="${gcum:email(sessionScope.sessionId)}">
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-2"></div>
            <div class="col-sm-10">
                L'adresse email n'est pas obligatoire mais si vous perdez votre mot de passe, vous perdrez votre pseudo.
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="button" class="btn btn-success" id="submit">Changer</button>
                <span class="alert" id="status"></span>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <a href="#" class="btn btn-link" id="remove">Effacer mon adresse email</a>
            </div>
        </div>
    </form>
</div>

<div class="modal fade" id="successModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <p>L'adresse email a été changée</p>
            </div>
            <div class="modal-footer">
                <a href="index.jsp" class="btn btn-success">Ok</a>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="removeSuccessModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <p>L'adresse email a été effacée</p>
            </div>
            <div class="modal-footer">
                <a href="index.jsp" class="btn btn-success">Ok</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
