<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<div id="confirmacion" class="container">
    <h2>
        <spring:message code="confirmacion.titulo" />
    </h2>
    <div class="row spacer separador"></div>

    <div class="alert alert-info">
        <c:out value="${mensaje}" />
    </div>

    <div class="row spacer separador"></div>

    <div class="pull-right">
        <button id="boton_aceptar" type="button" class="btn btn-success">
            <spring:message code="boton.aceptar" />
            <div class="fa fa-ok"/>
        </button>
        <button id="boton_cancelar" type="button" class="btn btn-danger">
            <spring:message code="boton.cancelar" />
            <div class="fa fa-remove"/>
        </button>
        <script>
            $("#boton_aceptar").click(function() {
                volver('${url_aceptar}');
            });
            $("#boton_cancelar").click(function() {
                volver('${url_cancelar}');
            });
        </script>
    </div>
</div>