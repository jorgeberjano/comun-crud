
function keypressFiltro(e, idConsulta, infoSeleccion) {
    if (e.keyCode === 13) {
        aplicarFiltro(idConsulta, infoSeleccion);
    } else {
        filtrar();
    }
}

function filtrar(idConsulta, infoSeleccion) {
    if (typeof (timerFiltro) !== "undefined") {
        clearTimeout(timerFiltro);
    }
    timerFiltro = setTimeout(function() { 
        aplicarFiltro(idConsulta, infoSeleccion) }, 3000);
}

function aplicarFiltro(idConsulta, infoSeleccion) {
    var filtro = $('#filtro').serialize();    
    $.ajax({
        type: "POST",
        url: contextPath + "/filtrar/" + idConsulta,
        cache: false,
        data: {
            filtro: filtro,
            infoSeleccion: infoSeleccion
         },
        success: function (result) {
            mostrarTabla(result, infoSeleccion);
        },
        error: manejarError
    });
}

function ordenar(campoOrden, idConsulta, infoSeleccion) {
    $.ajax({
        type: "POST",
        url: contextPath + "/ordenar/" + idConsulta,
        cache: false,
        data: {
            campoOrden: campoOrden,
            infoSeleccion: infoSeleccion
        },
        success: function (result) {
            mostrarTabla(result, infoSeleccion);
        },
        error: manejarError
    });
}

function paginar(desplazamiento, idConsulta, infoSeleccion, estilo) {
    if (estilo === 'disabled') {
        return;
    }
    $.ajax({
        type: "GET",
        url: contextPath + "/paginar/" + idConsulta,
        cache: false,
        data: {
            accion: desplazamiento,
            infoSeleccion: infoSeleccion
        },        
        success: function (result) {                        
            mostrarTabla(result, infoSeleccion);
        },
        error: manejarError
    });
}

function volver(url) {
    $.ajax({
        type: "GET",
        url: contextPath + url,
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        }
    });
}

function mostrarTablaTrasSeleccion(form) {
    var idConsulta = consultaFiltroPrevio.idConsulta;
    var datos = new FormData(form);
    $.ajax({
        type: "POST",
        url: contextPath + "/tablaConFiltroPrevio/" + idConsulta + "/",
        cache: false,
        contentType: false,
        processData: false,
        data: datos,
        success: function (result) {
            mostrarEnPanelPrincipal(result);     
        },
        error: manejarError
    });
}

function mostrarTablaElementos(idConsulta, inicial) {
    $.ajax({
        type: "GET",
        url: contextPath + (inicial ? "/iniciarTabla/" : "/tabla/") + idConsulta,
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function exportarTabla(formato) {
    var idConsulta = consulta.idConsulta;
    window.open(contextPath + "/exportarTabla/" + formato + "/" + idConsulta);
}

function editarConfiguracionTabla() {
    $.ajax({
        type: "GET",
        url: contextPath + "/editarConfiguracionTabla/" + consulta.idConsulta,
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function editarFiltroTabla() {
    $.ajax({
        type: "GET",
        url: contextPath + "/editarFiltroTabla/" + consulta.idConsulta,
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function totalizarTabla() {
    $.ajax({
        type: "GET",
        url: contextPath + "/totalizarTabla/" + consulta.idConsulta,
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function mostrarElemento(pk) {
    var idConsulta;
    if (!consultaElemento) {
        idConsulta = consulta.idConsulta;
    } else {
        idConsulta = consultaElemento.idConsulta;
    }
    
    $.ajax({
        type: "GET",
        url: contextPath + "/mostrar/" + idConsulta + "/" + pk + "/",
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function crearElemento() {
    var idConsulta = consulta.idConsulta;
    $.ajax({
        type: "GET",
        url: contextPath + "/crear/" + idConsulta,
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function modificarElemento(pk) {
    var idConsulta = consultaElemento.idConsulta;    
    $.ajax({
        type: "GET",
        url: contextPath + "/modificar/" + idConsulta + "/" + pk + "/",
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function borrarElemento(pk) {
    var idConsulta = consultaElemento.idConsulta;
    $.ajax({
        type: "GET",
        url: contextPath + "/confirmarBorrado/" + idConsulta + "/" + pk + "/",
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function guardarElemento(form, pk) {
    var idConsulta = consultaElemento.idConsulta;    
    var datos = new FormData(form);
    var url;
    if (pk) {
        url = "/guardar/" + idConsulta + "/" + pk + "/";
    } else {
        url = "/guardar/" + idConsulta + "/";
    }
    $.ajax({
        type: "POST",
        url: contextPath + url,
        cache: false,
        contentType: false,
        processData: false,
        data: datos,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function manejarError(xhr, errorType, exception) {
    if (xhr) {
        mostrarEnPanelPrincipal(xhr.responseText);
        alert(errorType + ": " + xhr.statusText +" " + xhr.staus + "\n" + exception);
    }
}

function soloLectura(s) {
    $(s + " :input").prop("disabled", true);
}

function pedirPagina(pagina) {
    $.ajax({
        type: "GET",
        url: contextPath + pagina,
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}

function esError(contenido) {
    var html = $.parseHTML(contenido);
    var hayMensajeError = $('#mensaje_error', html).length > 0;
    if (hayMensajeError) {
        mostrarError(contenido);    
    } else {
        ocultarError();
    }
    return hayMensajeError;
}

function mostrarTabla(contenido, infoSeleccion) {
           
    if (infoSeleccion) {
        mostrarEnPanelSeleccion(contenido);
    } else {
        mostrarEnPanelPrincipal(contenido);
    }
}

function mostrarEnPanelPrincipal(contenido) {    
    if (!contenido) {
        window.location.href = contextPath + "/";
    } else {
        mostrarEnPanel("#div_contenido", contenido);
    }  
}

function mostrarEnPanel(idPanel, contenido) {
    
    if (esError(contenido)) {
        return;
    }    
    if (contenido) {
        $(idPanel).removeClass("d-none");
        $(idPanel).html(contenido);
    } else {
        $(idPanel).addClass("d-none");
        $(idPanel).html("");
    }   
}

function mostrarEnPanelSeleccion(contenido) {
    
    if (esError(contenido)) {
        return;
    }
    
    $('#editor').addClass("d-none");
    $('#panel_seleccion').removeClass("d-none");
    $('#div_seleccion').removeClass("d-none");
    $('#div_seleccion').html(contenido);    
}

function mostrarError(contenido) {
    $("#div_contenido").addClass("d-none");
    $("#div_error").removeClass("d-none");
    $("#div_error").html(contenido);
}

function ocultarError() {
    $('#div_contenido').removeClass("d-none");
    $("#div_error").addClass("d-none");        
    $("#div_error").html("");
}

function clearDateInput(name) {
    $("#" + name).val("");   
}

function mostrarSeleccion(formulario, idConsultaSeleccion, idCampo, idCampoRelacion, idCampoSeleccion) {
    var seleccion = { 
        idConsulta: consultaElemento.idConsulta,
        idCampo: idCampo,        
        idCampoRelacion: idCampoRelacion,
        idCampoSeleccion: idCampoSeleccion
    };
    var elemento = {};
    formulario.find(':input').each(function() {
        elemento[this.id] = this.value;
    });
    
    $.ajax({
        type: "GET",        
        url: contextPath + "/seleccionar/" + idConsultaSeleccion,
        cache: false,
        data: {
            infoSeleccion: JSON.stringify(seleccion),
            elemento: JSON.stringify(elemento)
        },    
        success: function (result) {
            mostrarEnPanelSeleccion(result);
        },
        error: manejarError
    });
}
// Oculta la tabla de seleccion y vuelve a mostrar el formulario
function ocultarSeleccion(infoSeleccion) {
    $("#editor").removeClass("d-none");    
    $("#panel_seleccion").addClass("d-none");
    $("#div_seleccion").html("");
    
    var seleccion = JSON.parse(infoSeleccion);
    $("#" + seleccion.idCampo).focus();
}

function filaSeleccionada(pk, valores, infoSeleccion) {      
    if (infoSeleccion) {        
        seleccionado(infoSeleccion, pk, valores);
    } else {
        mostrarElemento(pk);
    }
}

// Se realiza la seleccion de un campo, actualizando tanto el valor del
// campo seleccionado como los campos que tengan relacion
function seleccionado(infoSeleccion, pk, valores) {
    var seleccion = JSON.parse(infoSeleccion);
    var idCampoEnConsultaSeleccion = seleccion.idCampoSeleccion;
    var valor = obtenerValorCampo(idCampoEnConsultaSeleccion, pk, valores);
    
    ocultarSeleccion(infoSeleccion);
    $("#" + seleccion.idCampo).val(valor);
    
    var camposConsulta = consultaElemento.listaCampos;
    var camposConsultaSeleccion = consulta.listaCampos;
    for (var i = 0; i < camposConsulta.length; i++) {
        var campo = camposConsulta[i];
        if (campo.idCampoRelacion !== seleccion.idCampo) {
            continue;
        }
        for (var j = 0; j < camposConsultaSeleccion.length; j++) {            
            var campoSeleccion = camposConsultaSeleccion[j];
            if (campo.idCampo === campoSeleccion.idCampo) {// && campo.tabla === campoSeleccion.tabla) {
                var valor = "";
                if (valores !== null) {
                    valor = valores[campoSeleccion.idCampo];
                }
                $("#" + campo.idCampo).val(valor);
            }
        }
    }
}

// Obtiene el valor de un campo de una clave primaria
// Si la clave primaria es unica o el campoSeleccion está vacio devuelve el primer valor.
// Em otro caso devuelve el valor que tiene el campoSeleccion
function obtenerValorCampo(idCampo, pk, valores) {
    if (!pk) {
        return "";
    }
    var partes = pk.split("%");
    for (var i = 0; i < partes.length; i++) {
        var parte = partes[i];
        var campoValor = parte.split("=");
        if (campoValor.length === 2) {
            if (campoValor[0] === idCampo) {
                return campoValor[1];
            }
        }
    }
    var valor = valores[idCampo];
    if (valor) {
        return valor;
    }
    return "";
}

function obtenerCamposFila(elementos) {
    var fila = { };
    for (var i = 0; i < elementos.length; i++) {
        var e = elementos[i];
        var nombre = e.title;
        var valor = e.textContent.trim();        
        fila[nombre] = valor;
    }
    return fila;
}

function serializarCamposFila(elementos) {
    var pk = "";
    for (var i = 0; i < elementos.length; i++) {
        var e = elementos[i];
        var nombre = e.title;
        var valor = e.textContent.trim();
        if (pk !== "") {
            pk += "%";
        }
        pk += nombre + "=" + valor;
    }
    return pk;
}

function botonEditorPulsado(idCampo, pk) {
    var form = document.getElementById("formulario");
    var datos = new FormData(form);
    var idConsulta = consultaElemento.idConsulta;        
    var url = "/botonEdicionPulsado/" + idConsulta;
    if (pk) {
        url += "?modo=MODIFICAR&campo=" + idCampo + "&pk=" + pk;
    } else {
        url += "?modo=CREAR&campo=" + idCampo;
    }        
    $.ajax({
        type: "POST",
        url: contextPath + url,
        cache: false,
        contentType: false,
        processData: false,
        data: datos,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    }); 
}

function fromJson(texto) {
   return JSON.parse(texto); 
}

function cambioEnCampoSeleccionable(idCampo) {    
    var valor = $("#" + idCampo).get(0).value;
    var idConsulta = consultaElemento.idConsulta;
    var url = "/obtenerDatosRelacionados/" + idConsulta;
    url += "?campo=" + idCampo + "&valor=" + valor;
    $.ajax({
        type: "GET",
        url: contextPath + url,
        cache: false,
        success: function (result) {
            aplicarValores(result);
        },
        error: manejarError
    }); 
}

function aplicarValores(valores) {
    for (var campo in valores) {
       $("#" + campo).get(0).value = valores[campo];
    }
}

function modificarConfiguracionCampo(operacion, idCampo) {
    var idConsulta = consulta.idConsulta;
    var url = "/modificarConfiguracionCampo/" + idConsulta + "/" + operacion + "/" + idCampo;
     
    $.ajax({
        type: "GET",
        url: contextPath + url,
        cache: false,
        success: function (result) {
            mostrarEnPanel("#configuracion-campos", result);
        },
        error: manejarError
    }); 
}

function aceptarConfiguracionTabla(idConsulta) {
    var url = "/aceptarConfiguracionTabla/" + idConsulta ;
     
    $.ajax({
        type: "GET",
        url: contextPath + url,
        cache: false,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    }); 
}

function accionFiltro(idConsulta, accion, parametro) {
    var form = $("#formulario")[0];
    var url = "/accionFiltro/" + idConsulta + "?accion=" + accion;
    if (parametro) {
        url += "&parametro=" + parametro;
    }
    var datos = new FormData(form);
    $.ajax({
        type: "POST",
        url: contextPath + url,
        cache: false,
        contentType: false,
        processData: false,
        data: datos,
        success: function (result) {
            mostrarEnPanelPrincipal(result);
        },
        error: manejarError
    });
}
  



