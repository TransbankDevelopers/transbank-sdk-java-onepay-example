<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<!--[if lt IE 7 ]><html lang="en" class="no-js ie6"><![endif]-->
<!--[if IE 7 ]><html lang="en" class="no-js ie7"><![endif]-->
<!--[if IE 8 ]><html lang="en" class="no-js ie8"><![endif]-->
<!--[if IE 9 ]><html lang="en" class="no-js ie9"><![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--><html lang="en" class="no-js"><!--<![endif]-->
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta charset="utf-8">
</head>
<body>
<h3>Tu pago se completo en forma exitosa</h3>
<table>
    <tr>
        <td>OCC:</td>
        <td>${model.transaction.occ}</td>
    </tr>
    <tr>
        <td>Número de carro:</td>
        <td>${model.externalUniqueNumber}</td>
    </tr>
    <tr>
        <td>Código de autorización:</td>
        <td>${model.transaction.authorizationCode}</td>
    </tr>
    <tr>
        <td>Orden de compra:</td>
        <td>${model.transaction.buyOrder}</td>
    </tr>
    <tr>
        <td>Descripción:</td>
        <td>${model.transaction.transactionDesc}</td>
    </tr>
    <tr>
        <td>Monto compra:</td>
        <td>${model.transaction.amount}</td>
    </tr>
    <tr>
        <td>Numero de cuotas:</td>
        <td>${model.transaction.installmentsNumber}</td>
    </tr>
    <tr>
        <td>Monto cuota:</td>
        <td>${model.transaction.installmentsAmount}</td>
    </tr>
    <tr>
        <td>Fecha:</td>
        <td>${model.transaction.issuedAt}</td>
    </tr>
    <tr>
        <td>Anulación</td>
        <td>
            <c:url value="./refund.html" var="refundUrl">
                <c:param name="amount" value="${model.transaction.amount}"/>
                <c:param name="occ" value="${model.transaction.occ}"/>
                <c:param name="externalUniqueNumber" value="${model.externalUniqueNumber}"/>
                <c:param name="authorizationCode" value="${model.transaction.authorizationCode}"/>
            </c:url>
            <a href="${refundUrl}">Anular esta compra</a>
        </td>
    </tr>
</table>
</body>
</html>