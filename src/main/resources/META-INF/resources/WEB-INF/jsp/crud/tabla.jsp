<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<div id="div_tabla" class="container-fluid">
   
    <div id="campoSeleccion" class="d-none"><c:out value="${campoSeleccion}"/></div>

    <h3><c:out value="${servicio.titulo}"/></h3>
    <c:if test="${not empty servicio.subtitulo}"> 
        <pre><c:out value="${servicio.subtitulo}"/></pre>
    </c:if>
    <div class="row separador"></div>

    <div class="row spacer"></div>

    <div id="div_contenedor_tabla" class="container-fluid">
        <%@include file="tabla_rejilla.jsp" %>
    </div>
    
    <script>            
        var consulta = ${servicio.consulta.toJson()};
        if (${empty infoSeleccion}) {
            consultaElemento = null;
        }
    </script>
    
</div>