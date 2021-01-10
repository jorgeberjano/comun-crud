<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<form:form id="formulario"
           modelAttribute="filtro"
           method="POST"
           enctype="multipart/form-data"           
           cssClass="form-signin"
           role="form" 
           acceptCharset="UTF-8">     
    <div class="row">
        <c:forEach var="condicion" items="${filtro.listaCondiciones}">          
            <c:set var="campo" value="${servicio.getCampo(condicion.idCampo)}" />
            <c:set var="opciones" value="${servicio.getMapaOpciones(campo.idCampo)}" />
            <div class="form-group m-1">
                <div class="row no-gutters">
                    <div class="d-none">
                        <form:input class="form-control"
                                    path="mapaCondiciones['${condicion.indice}'].indice" />
                        <form:input class="form-control"
                                    path="mapaCondiciones['${condicion.indice}'].idCampo"
                                    readonly="true" />
                    </div>
                    <div class="col mb-1  ">
                        <form:input class="form-control"
                                    path="mapaCondiciones['${condicion.indice}'].tituloCampo"
                                    readonly="true" />
                    </div>
                    <div class="col mb-1">
                        <form:select class="form-control"
                                     path="mapaCondiciones['${condicion.indice}'].operador"
                                     items="${servicio.getMapaOperadores(campo.idCampo)}"
                                     multiple="false" />
                    </div>
                    <div class="col mb-1">
                        <c:choose>
                            <c:when test="${campo.tipoDato eq 'FECHA' or campo.tipoDato eq 'FECHA_HORA'}">
                                <div class="input-group date FECHA" id="fecha_${condicion.indice}" data-target-input="nearest">
                                    <form:input id="fecha_${condicion.indice}"
                                                class="form-control datetimepicker-input"
                                                path="mapaCondiciones['${condicion.indice}'].valor"
                                                data-target="#fecha_${condicion.indice}"/>   
                                    <div class="input-group-append" data-target="#fecha_${condicion.indice}" data-toggle="datetimepicker">
                                        <div class="input-group-text">
                                            <i class="fa fa-calendar"></i>
                                        </div>
                                    </div>
                                </div>
                            </c:when>
                            <c:when test="${not empty opciones}">
                                <form:input class="form-control ${campo.tipoDato} ${campo.isSiempreMayusculas() ? 'text-uppercase' : ''} ${not empty campo.consultaSeleccion ? 'seleccion' : ''}"
                                            path="mapaCondiciones['${condicion.indice}'].valor" 
                                            list="opciones_${campo.idCampo}" />
                                <datalist id="opciones_${campo.idCampo}">
                                    <c:forEach var="opcion" items="${opciones}">
                                        <option value="${opcion.key}">${opcion.value}</option>
                                    </c:forEach>
                                </datalist>
                            </c:when>
                            <c:otherwise>
                                <form:input class="form-control ${campo.tipoDato} ${campo.isSiempreMayusculas() ? 'text-uppercase' : ''} ${not empty campo.consultaSeleccion ? 'seleccion' : ''}"
                                            path="mapaCondiciones['${condicion.indice}'].valor" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="col-1 mb-1">
                        <button id="_${condicion.indice}" type="button" class="boton_borrar btn btn-outline-danger">
                            <div class="fa fa-window-close"/>
                        </button>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
    <script>
        $(".FECHA").datetimepicker({
            format: "DD/MM/YYYY"
        });
        $(".boton_borrar").click(function () {
            var consulta = ${servicio.consulta.toJson()};
            accionFiltro(consulta.idConsulta, "borrar", $(this)[0].id.substring(1));
        });
    </script>
</form:form>
