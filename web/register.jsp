<!doctype html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<html>
<head>
    <title>GCUM Créer un compte</title>
    <meta name="viewport" content="width=device-width"/>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/register.js"></script>
</head>
<body>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="links">
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-map-marker"></i><span class="hideOnMobile link">Carte</span></a>
        <a href="list.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-align-justify"></i><span class="hideOnMobile link">Liste</span></a>
        <a href="info.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-info-sign"></i></a>
    </div>
</div>
<div class="well">
    <form class="form-horizontal">
        <div class="form-group" id="usernameGroup">
            <label class="control-label col-sm-2" for="username">Pseudo :</label>
            <div class="col-sm-10">
                <input type="text" class="form-control" id="username" placeholder="Entrer pseudo">
            </div>
        </div>
        <div class="form-group" id="passwordGroup">
            <label class="control-label col-sm-2" for="password">Mot de passe :</label>
            <div class="col-sm-10">
                <input type="password" class="form-control" id="password" placeholder="Entrer mot de passe">
            </div>
        </div>
        <div class="form-group" id="passwordCheckGroup">
            <label class="control-label col-sm-2" for="passwordCheck">(vérification) :</label>
            <div class="col-sm-10">
                <input type="password" class="form-control" id="passwordCheck" placeholder="Réentrer mot de passe">
            </div>
        </div>
        <div class="form-group" id="emailGroup">
            <label class="control-label col-sm-2" for="email">Email (facultatif) :</label>
            <div class="col-sm-10">
                <input type="email" class="form-control" id="email" placeholder="Entrer votre email pour pouvoir réinitialiser votre mot de passe">
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
                <div class="checkbox">
                    <label><input type="checkbox"> Se souvenir de moi</label>
                </div>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="button" class="btn btn-success" id="submit">Ok</button>
                <span class="alert" id="status"></span>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <a href="login.jsp" class="btn btn-link">Se connecter avec un compte existant</a>
            </div>
        </div>
    </form>
</div>

<div class="modal fade" id="successModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-body">
                <p>Le compte a été créé</p>
            </div>
            <div class="modal-footer">
                <a href="index.jsp" class="btn btn-success" id="success">Ok</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
