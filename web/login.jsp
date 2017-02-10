<!doctype html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@taglib prefix="gcum" uri="http://www.gcum.lol/gcum" %>
<html>
<head>
    <title>GCUM Login</title>
    <link rel="stylesheet" type="text/css" href="stylesheets/shared.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script type="text/javascript" src="lib/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="lib/bootstrap.min.js"></script>
    <script type="text/javascript" src="scripts/login.js"></script>
</head>
<body>
<div id="controls">
    <span id="logo"><img src="images/logo.png"></span>
    <div class="links">
        <a href="index.jsp" class="btn btn-outline-primary"><i class="glyphicon glyphicon-eye-open"></i> Carte</a>
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
                <a href="register.jsp" class="btn btn-link">Créer un compte</a>
            </div>
        </div>
    </form>
</div>
</body>
</html>
