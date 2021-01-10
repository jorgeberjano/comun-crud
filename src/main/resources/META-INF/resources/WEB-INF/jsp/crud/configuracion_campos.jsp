<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<div>
    <div class="pull-right p-1">
        <button id="boton_restaurar" type="button" class="btn btn-warning">
            <spring:message code="boton.restaurar" />
            <div class="fa fa-undo"/>
        </button>
        <div class="dropdown btn">
            <button id="boton_agregar" type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                <spring:message code="boton.agregar" />
                <div class="fa fa-plus"/>
            </button>
            <div class="dropdown-menu" aria-labelledby="boton_agregar" >
                <c:forEach var="campo" items="${servicio.camposDisponibles}">        
                    <c:if test="${not campo.oculto}">
                        <a id="_${campo.idCampo}" class="agregar_campo dropdown-item" href="#">
                            <c:out value="${campo.titulo}" />
                        </a>
                    </c:if>
                </c:forEach>
            </div>
        </div>
    </div>

    <div class="row container-fluid">
        <c:forEach var="campo" items="${servicio.camposConfigurados}">
            <c:if test="${not campo.oculto}">
                <div id="${campo.idCampo}" class="card bg-dark p-1 m-1">
                    <div class="card-title">
                        <button type="button" class="boton_adelantar btn btn-circle float-left">
                            <div class="fa fa-caret-left"/>
                        </button>
                        <button type="button" class="boton_atrasar btn btn-circle float-left">
                            <div class="fa fa-caret-right"/>
                        </button>                
                        <button type="button" class="boton_eliminar btn btn-circle float-right">
                            <div class="fa fa-remove"/>
                        </button>
                    </div>
                    <div class="card-body bg-light">
                        <c:out value="${campo.titulo}" />
                    </div>
                </div>
            </c:if>
        </c:forEach>
        <script>
            $("#boton_restaurar").click(function () {
                modificarConfiguracionCampo("restaurar", "---");
            });
            $(".agregar_campo").click(function () {
                modificarConfiguracionCampo("agregar", $(this).get(0).id.substring(1));
            });
            $(".boton_adelantar").click(function () {
                modificarConfiguracionCampo("adelantar", $(this).parents(".card").get(0).id);
            });
            $(".boton_atrasar").click(function () {
                modificarConfiguracionCampo("atrasar", $(this).parents(".card").get(0).id);
            });
            $(".boton_eliminar").click(function () {
                modificarConfiguracionCampo("eliminar", $(this).parents(".card").get(0).id);
            });

            $('.dropdown-toggle').dropdown();

        </script>
    </div>
</div>