<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div id="div_tabla_rejilla" class="">   
    <div class="row justify-content-between">
        <div class="m-1">
            <c:if test='${empty infoSeleccion}'>
                <c:if test='${not servicio.consulta.sinAlta}'>
                    <button id="boton_crear" type="button" class="btn btn-success m-1">
                        <spring:message code="boton.crear" />
                        <div class="fa fa-plus"/>
                    </button>
                </c:if>
                <button id="boton_actualizar" type="button" class="btn btn-dark">
                    <spring:message code="boton.actualizar" />
                    <div class="fa fa-refresh"/>
                </button>                
                <button id="boton_filtrar" type="button" class="btn btn-primary m-1">
                    <spring:message code="boton.filtrar" />
                    <div class="fa fa-filter"/>
                </button>                
                <button id="boton_configurar" type="button" class="btn btn-primary m-1">
                    <spring:message code="boton.configurar" />
                    <div class="fa fa-wrench"/>
                </button>
                    <button id="boton_exportar" type="button" class="btn btn-primary dropdown-toggle m-1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    <spring:message code="boton.exportar" />
                    <div class="fa fa-download"/>
                </button>
                <div class="dropdown-menu" aria-labelledby="boton_exportar" >                          
                    <a id="boton_pdf" class="dropdown-item" href="#">
                        <div class="fa fa-file-pdf-o"/>
                        <spring:message code="boton.pdf" />
                    </a>
                    <a id="boton_csv" class="dropdown-item" href="#">
                        <div class="fa fa-file-o"/>
                        <spring:message code="boton.csv" />
                    </a>
                    <a id="boton_excell" class="dropdown-item" href="#">
                        <div class="fa fa-file-excel-o"/>
                        <spring:message code="boton.excell" />
                    </a>
                </div>
            </c:if>
            <c:if test='${not servicio.consulta.sinTotalizacion}'>
                <button id="boton_totalizar" type="button" class="btn btn-primary m-1">
                    <spring:message code="boton.totalizar" />
                    <div class="fa fa-calculator"/>
                </button>
            </c:if>
            <c:if test='${not empty infoSeleccion}'>
                <button id="boton_sin_seleccion" type="button" class="btn btn-warning m-1">
                    <spring:message code="boton.sin_seleccion" />
                    <div class="fa fa-erase"/>
                </button>
                <button id="boton_cancelar" type="button" class="btn btn-danger m-1">
                    <spring:message code="boton.cancelar" />
                    <div class="fa fa-remove"/>
                </button>
            </c:if>
        </div>
                    
        <%@include file="paginacion.jsp" %>
        
    </div>

    <div class="row spacer"/>
    <div class="table-responsive">
        <table id="tabla" class="table table-striped table-bordered table-hover table-sm">
            <thead>
                <tr>
                    <c:forEach var="campo" items="${servicio.camposVisibles}">
                        <th class="th-sm text-center ${campo.oculto ? "d-none" : ""}" 
                            data-field="${campo.idCampo}">
                            <div>
                                <c:out value="${campo.titulo}" />
                                <c:if test="${servicio.campoOrden eq campo.idCampo}">
                                    <span class="fa fa-triangle-${servicio.ordenDescendente ? 'bottom' : 'top'}"></span>
                                </c:if>
                            </div>
                        </th>
                    </c:forEach>
                    <c:if test="${servicio.permitirSeleccion()}">
                        <th>
                            <span class="d-none"><spring:message code="seleccion" /></span>
                        </th>
                    </c:if>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="elemento" items="${servicio.listaElementosPagina}">
                    <tr class="fila">
                        <c:forEach var="campo" items="${servicio.camposVisibles}">
                            <td title="${campo.idCampo}"
                                class="${campo.oculto ? "d-none" : ""} ${campo.clave ? "pk" : ""} text-${campo.align}">                                
                                <c:out value="${servicio.getValor(elemento, campo.idCampo)}" />
                            </td>
                        </c:forEach>   

                        <c:if test='${servicio.permitirSeleccion()}'>
                            <td>
                                <a class="seleccion" href="#" title="<spring:message code="seleccion" />">
                                    <span class="fa fa-hand-o-left"/>
                                </a>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
            </tbody>
        </table>      
    </div>
    <c:choose>
        <c:when test="${not empty mensaje_error}">
            <div class="row show-grid">
                <div class="col-sm-12 text-center">
                    <c:out value="${mensaje_error}" />
                </div>
            </div>
        </c:when>
        <c:when test="${empty servicio.listaElementosPagina || servicio.listaElementosPagina.size() == 0}" >
            <div class="row show-grid">
                <div class="col-sm-12 text-center">
                    <spring:message code="tabla.vacia" />
                </div>
            </div>                                
        </c:when>
    </c:choose>

    <script>
        function seleccionarFila(fila) {
            var pk = serializarCamposFila(fila.find("td.pk"));
            if (!pk) {
                return;
            }
            var valores = obtenerCamposFila(fila.find("td"));
            filaSeleccionada(pk, valores, '${infoSeleccion}');
        }
        $("tr.fila").dblclick(function () {
            seleccionarFila($(this));
        });
        $("a.seleccion").click(function () {
            seleccionarFila($(this).closest("tr.fila"));
        });
        
        $("#boton_crear").click(function () {
            crearElemento();
        });

        $("#boton_actualizar").click(function () {
            mostrarTablaElementos(consulta.idConsulta);
        });

        $("#boton_filtrar").click(function () {
            editarFiltroTabla();
        });

        $('.dropdown-toggle').dropdown();

        $("#boton_pdf").click(function () {
            exportarTabla("pdf");
        });
        
        $("#boton_csv").click(function () {
            exportarTabla("csv");
        });
        
        $("#boton_excell").click(function () {
            exportarTabla("xlsx");
        });

        $("#boton_configurar").click(function () {
            editarConfiguracionTabla();
        });
        
        $("#boton_totalizar").click(function () {
            totalizarTabla();
        });

        $("#boton_cancelar").click(function () {
            ocultarSeleccion('${infoSeleccion}');
        });

        $("#boton_sin_seleccion").click(function () {
            filaSeleccionada(null, null, '${infoSeleccion}');
        });

        $("th").click(function () {
            var campoOrden = this.getAttribute("data-field");
            var idConsulta = "${servicio.consulta.idConsulta}";
            var infoSeleccion = '${infoSeleccion}';
            ordenar(campoOrden, idConsulta, infoSeleccion);
        });
    </script>
</div>
