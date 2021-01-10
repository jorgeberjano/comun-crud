<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div id="div_totalizacion" class="container-fluid">

    <h3><c:out value="${servicio.titulo}"/></h3>
    
    ${servicio.totalizacion}

    <div class="pull-right p-1">
        <button id="boton_atras" type="button" class="btn btn-dark">
            <spring:message code="boton.atras" />
            <div class="fa fa-sign-out"/>
        </button>
    </div>    
    <script>
        $("#boton_atras").unbind("click");
        $("#boton_atras").click(function () {
            mostrarTablaElementos("${servicio.consultaOriginal.idConsulta}");
        });
    </script>
</div>
