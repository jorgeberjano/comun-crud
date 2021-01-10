<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>


<div class="row justify-content-end m-1">
    <c:if test='${not servicioElemento.estaEnModoEdicion()}'>        
        <c:if test='${not servicioElemento.consulta.sinModificacion}'>
            <button type="button" class="boton_modificar btn btn-primary m-1">
                <spring:message code="boton.modificar" />
                <div class="fa fa-edit"/>
            </button>
        </c:if>
        <c:if test='${not servicioElemento.consulta.sinBaja}'>
            <button type="button" class="boton_borrar btn btn-danger m-1">
                <spring:message code="boton.borrar" />
                <div class="fa fa-trash"/>
            </button>
        </c:if>
        <button type="button" class="boton_atras btn btn-dark m-1">
            <spring:message code="boton.atras" />
            <div class="fa fa-sign-out"/>
        </button>
        <script>
            $(".boton_atras").unbind("click");
            $(".boton_atras").click(function () {
                mostrarTablaElementos(consultaElemento.idConsulta);
            });
            
            $(".boton_modificar").unbind("click");
            $(".boton_modificar").click(function () {
                modificarElemento('${pk.toString()}');
            });
            
            $(".boton_borrar").unbind("click");
            $(".boton_borrar").click(function () {                
                borrarElemento('${pk.toString()}');
            });
        </script>
    </c:if>
    <c:if test='${servicioElemento.estaEnModoEdicion()}'>
        <button type="button" class="boton_guardar btn btn-success m-1">
            <spring:message code="boton.guardar" />
            <div class="fa fa-save"/>
        </button>
        <button type="button" class="boton_cancelar btn btn-danger m-1">
            <spring:message code="boton.cancelar" />
            <div class="fa fa-remove"/>
        </button>
        <script>
            $(".boton_guardar").unbind("click");
            $(".boton_guardar").click(function () {
                $("#formulario").submit();
            });
            
            $(".boton_cancelar").unbind("click");
            $(".boton_cancelar").click(function () {
                var pk = '${pk.toString()}';
                if (pk === undefined || pk === "") {
                    mostrarTablaElementos(consultaElemento.idConsulta);
                } else {
                    mostrarElemento(pk);
                }
            });
        </script>
    </c:if>   
</div>
