<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<div id="div_editor" class="container-fluid">
    <h2>
        <c:out value="${servicioElemento.titulo}" />
    </h2>

    <c:if test="${not empty mensaje_error}">
        <div class="alert alert-danger alert-dismissable">
            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
            <c:out value="${mensaje_error}" />
        </div>
    </c:if>

    <div id="editor" class="container">

        <div class="row separador"></div>

        <%@include file="editor_botones.jsp" %>

        <div class="row separador"></div>

        <div class="container">
            <div id="div_formulario">
                <%@include file="editor_formulario.jsp" %>
            </div>
        </div>

        <div class="row spacer separador"></div>

        <%@include file="editor_botones.jsp" %>

        <br/>
        <br/>
        <br/>        
    </div>
    <div id="panel_seleccion" class="d-none">
        <div id="div_seleccion">            
        </div>
    </div>

    <div class="row spacer separador"></div>

    <c:if test='${not servicioElemento.estaEnModoEdicion()}'>
        <script>
            soloLectura("#div_formulario");
        </script>
    </c:if>
    <script>
        var consultaElemento = ${servicioElemento.consultaOriginal.toJson()};
    </script>

</div>
