<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<div id="div_mensaje_error">
    <h2>
       <spring:message code="error.titulo" />
    </h2>
    <div class="row spacer separador"></div>

    <div id="mensaje_error" class="alert alert-danger">
        <c:out value="${mensaje_error}" />
    </div>

    <div class="row spacer separador"></div>

    <div class="pull-right">
        <button id="boton_ocultar_error" type="button" class="btn btn-default">
            <spring:message code="boton.atras" />
            <div class="fa fa-menu-left"/>
        </button>
    </div>
        
    <script>
        $("#boton_ocultar_error").unbind("click");
        $("#boton_ocultar_error").click(function() {
            ocultarError();
        });
    </script>
</div>
