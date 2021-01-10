<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<nav aria-label="Barra de navegación">
    <ul class="pagination justify-content-end">
        <li class="page-item disabled">
            <a class="page-link" href="#">
                <spring:message code="tabla.pagina" />
                <c:out value="${servicio.paginaActual}" />
                <spring:message code="tabla.de" />
                <c:out value="${servicio.numeroPaginas}" />
            </a>
        </li>
        <c:set var='estiloRetroceso' value='${servicio.paginaActual <= 1 ? "disabled" : ""}'/>
        <li class="page-item ${estiloRetroceso}">
            <a class="page-link" href="javascript:paginacion('primero', '${estiloRetroceso}')" aria-label="Primera página">
                <span class="fa fa-fast-backward" title="Primera página"></span>
            </a>
        </li>
        <li class="page-item ${estiloRetroceso}">
            <a class="page-link" href="javascript:paginacion('anterior', '${estiloRetroceso}')" aria-label="Página anterior">
                <span class="fa fa-step-backward"></span>
            </a>
        </li> 

        <c:set var='estiloAvance' value='${servicio.paginaActual == servicio.numeroPaginas ? "disabled" : ""}'/>
        <li class="page-item ${estiloAvance}">
            <a class="page-link" class="page-link" href="javascript:paginacion('siguiente', '${estiloAvance}')" aria-label="Página siguiente">
                <span class="fa fa-step-forward"></span>  
            </a>
        </li>
        <li class="page-item  ${estiloAvance}">
            <a class="page-link" href="javascript:paginacion('ultimo', '${estiloAvance}')" aria-label="Última página">
                <span class="fa fa-fast-forward"></span>
            </a>
        </li>                         
    </ul>
    <script>
        function paginacion(desplazamiento, estilo) {
            var idConsulta = '${servicio.consulta.idConsulta}';
            var infoSeleccion = '${infoSeleccion}';
            paginar(desplazamiento, idConsulta, infoSeleccion, estilo);
        }
    </script>
</nav>
