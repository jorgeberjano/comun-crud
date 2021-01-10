<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<form:form id="formulario"
           modelAttribute="elemento"
           method="POST"
           enctype="multipart/form-data"           
           cssClass="form-signin"
           role="form" 
           acceptCharset="UTF-8">

    <form:input class="d-none" path="mapa['uuid'].valor" id="uuid" />

    <c:forEach var="campo" items="${servicioElemento.consultaOriginal.listaCampos}">
        <div class="form-group form-row ${campo.oculto ? 'd-none' : ''}">
            <form:label class="col-sm-2 col-form-label" path="mapa['${campo.getIdCampo()}'].valor" for="${campo.getIdCampo()}">
                <c:out value='${campo.getTitulo()}' />
            </form:label>
            <c:set var="campoEditable" value="${servicioElemento.esEditable(campo)}" />
            <c:set var="tieneBotones" value="${servicioElemento.tieneBotones(campo)}" />
            <div class="col-sm-10 ${tieneBotones ? 'input-group' : ''}">
                <c:if test="${not campoEditable}">
                    <form:input path="mapa['${campo.getIdCampo()}'].valor" tabindex="${campo.getIndice()}" class="form-control" id="${campo.getIdCampo()}" disabled="true" />
                </c:if> 
                <c:if test="${campoEditable}">
                    <c:choose>
                        <c:when test="${campo.tipoDato eq 'FECHA' or campo.tipoDato eq 'FECHA_HORA'}">
                            <div class="input-group date ${campo.tipoDato}" id="${campo.getIdCampo()}" data-target-input="nearest">
                                <form:input id="${campo.getIdCampo()}"
                                            class="form-control datetimepicker-input"
                                            path="mapa['${campo.getIdCampo()}'].valor"
                                            data-target="#${campo.getIdCampo()}" />
                                <div class="input-group-append" data-target="#${campo.getIdCampo()}" data-toggle="datetimepicker">
                                    <div class="input-group-text">
                                        <i class="fa fa-calendar"></i>
                                    </div>
                                </div>
                            </div>
                        </c:when>
                        <c:when test="${not empty campo.opciones}">
                            <form:select id="${campo.getIdCampo()}"
                                         class="form-control"
                                         path="mapa['${campo.getIdCampo()}'].valor"
                                         items="${campo.opciones}"
                                         multiple="false"
                                         readonly="${campo.isSoloLectura()}"
                                         />
                        </c:when>
                        <c:otherwise>
                            <form:input id="${campo.getIdCampo()}"
                                        class="form-control ${campo.tipoDato} ${campo.isSiempreMayusculas() ? 'text-uppercase' : ''} ${not empty campo.consultaSeleccion ? 'seleccion' : ''}"
                                        path="mapa['${campo.getIdCampo()}'].valor"
                                        maxlength="${campo.getTamano() > 0 ? campo.getTamano() : 524288}"
                                        readonly="${campo.isSoloLectura()}"
                                        />
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${not empty campo.consultaSeleccion}">
                        <span class="input-group-btn">
                            <button type="button" class="btn btn-secondary" onclick="mostrarSeleccion($('#formulario'), '${campo.consultaSeleccion}', '${campo.idCampo}', '${campo.idCampoRelacion}', '${campo.idCampoSeleccion}')">
                                <span class="d-none">Buscar</span>
                                <span class="fa fa-search"></span>
                            </button>
                        </span>
                    </c:if>
                </c:if>
                <c:if test="${not empty servicioElemento.getTextoBoton(campo.getIdCampo())}">                    
                    <span class="input-group-btn">
                        <button type="button"
                                class="btn btn-secondary"
                                onclick="botonEditorPulsado('${campo.idCampo}', '${pk.toString()}')">
                            <c:out value="${servicioElemento.getTextoBoton(campo.getIdCampo())}" />
                        </button>
                    </span>
                </c:if>
            </div>
        </div>
    </c:forEach>

    <script>
        $("#${servicioElemento.getNombreCampoFocoInicial()}").focus();

        $("#formulario input.seleccion").change(function (evento) {
            cambioEnCampoSeleccionable(evento.target.id);
        });

        $("#formulario").unbind("submit");
        $("#formulario").submit(function (event) {
            event.preventDefault();
            guardarElemento(this, '${pk.toString()}');
        });


        $(".FECHA_HORA").datetimepicker({
            format: "DD/MM/YYYY HH:mm:ss"
        });

        $(".FECHA").datetimepicker({
            format: "DD/MM/YYYY"
        });


    </script>

</form:form> 


