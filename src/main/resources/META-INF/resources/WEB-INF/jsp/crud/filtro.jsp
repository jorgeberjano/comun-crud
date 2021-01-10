<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<div id="filtro" class="container-fluid">

    <h3><c:out value="${servicio.titulo}"/></h3>

    <div class="row m-1">
        <button id="boton_limpiar" type="button" class="btn btn-warning m-1">
            <spring:message code="boton.limpiar" />
            <div class="fa fa-eraser"/>
        </button>        
        <button id="boton_agregar" type="button" class="btn btn-primary dropdown-toggle m-1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <spring:message code="boton.agregar" />
            <div class="fa fa-plus"/>
        </button>
        <div class="dropdown-menu" aria-labelledby="boton_agregar" >
            <c:forEach var="campo" items="${servicio.camposVisibles}">            
                <a id="_${campo.idCampo}" class="agregar_campo dropdown-item" href="#"><c:out value="${campo.titulo}" /></a>
            </c:forEach>
        </div>
    </div>

    <div id="configuracion-campos" class="row m-1">
        <%@include file="filtro_campos.jsp" %>
    </div>    

    <div class="row justify-content-end m-1">
        <button id="boton_aceptar" type="button" class="btn btn-success m-1">
            <spring:message code="boton.aceptar" />
            <div class="fa fa-check"/>
        </button>            
        <button id="boton_cancelar" type="button" class="btn btn-danger m-1">
            <spring:message code="boton.cancelar" />
            <div class="fa fa-times"/>
        </button>
    </div>

    <script>
        $("#boton_limpiar").click(function () {
            var consulta = ${servicio.consulta.toJson()};
            accionFiltro(consulta.idConsulta, "limpiar");
        });

        $(".agregar_campo").click(function () {
            var consulta = ${servicio.consulta.toJson()};
            var idCampo = $(this)[0].id.substring(1);
            accionFiltro(consulta.idConsulta, "agregar", idCampo);
        });

        $("#boton_aceptar").click(function () {
            var consulta = ${servicio.consulta.toJson()};
            accionFiltro(consulta.idConsulta, "aceptar");
        });
        
        $("#boton_cancelar").click(function () {
            var consulta = ${servicio.consulta.toJson()};
            accionFiltro(consulta.idConsulta, "cancelar");
        });
        
        $('.dropdown-toggle').dropdown();
        
    </script>
</div>





