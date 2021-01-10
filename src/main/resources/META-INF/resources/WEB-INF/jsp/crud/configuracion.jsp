<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<div id="configuracion" class="container-fluid">    

    <h3><c:out value="${servicio.titulo}"/></h3>
    
    <div id="configuracion-campos">
        <%@include file="configuracion_campos.jsp" %>
    </div>    

    <div class="pull-right p-1">
        <button id="boton_aceptar" type="button" class="btn btn-success">
            <spring:message code="boton.aceptar" />
            <div class="fa fa-check"/>
        </button>            
        <button id="boton_cancelar" type="button" class="btn btn-danger">
            <spring:message code="boton.cancelar" />
            <div class="fa fa-times"/>
        </button>       
    </div>

    <script>       
        $("#boton_aceptar").click(function () {
            var consulta = ${servicio.consulta.toJson()};
            aceptarConfiguracionTabla(consulta.idConsulta);
        });
        $("#boton_cancelar").click(function () {
            var consulta = ${servicio.consulta.toJson()};
            mostrarTablaElementos(consulta.idConsulta);
        });
    </script>
</div>