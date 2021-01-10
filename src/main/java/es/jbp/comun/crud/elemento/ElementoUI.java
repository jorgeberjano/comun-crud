package es.jbp.comun.crud.elemento;

import es.jbp.comun.ges.dao.EntidadGes;
import es.jbp.comun.ges.entidad.CampoGes;
import es.jbp.comun.ges.entidad.ConsultaGes;
import es.jbp.comun.ges.utilidades.ConversionValores;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Representa una entidad en la capa de vista.
 *
 * @author jberjano
 */
public final class ElementoUI implements Serializable {

    private MapaValoresUI mapa = new MapaValoresUI();

    public ElementoUI() {
    }

    public MapaValoresUI getMapa() {
        return mapa;
    }

    public void setMapa(MapaValoresUI mapa) {
        this.mapa = mapa;
    }

    public void set(String nombreCampo, String valor) {

        mapa.put(nombreCampo, new ValorUI(valor));
    }

    public ValorUI get(String nombreCampo) {
        return mapa.get(nombreCampo);
    }

    public EntidadGes obtenerValores(EntidadGes entidad, ConsultaGes consulta) {

        for (CampoGes campo : consulta.getListaCampos()) {
            String idCampo = campo.getIdCampo();
            ValorUI valorUI = get(idCampo);
            if (valorUI == null) {
                continue;
            }
            Object valor = ConversionValores.aValorBD(valorUI.getValor(), campo);
            if (campo.isClave()) {
                Object valorClavePrimaria = entidad.getValorClavePrimaria(idCampo);
                if (valorClavePrimaria == null) {
                    entidad.setValorClavePrimaria(idCampo, valor);
                }
            }
            entidad.setValor(idCampo, valor);
        }

        return entidad;
    }

    public void asignarValores(EntidadGes entidad, ConsultaGes consulta, boolean paraEdicion) {
        mapa = new MapaValoresUI();
        if (entidad == null) {
            return;
        }
        for (CampoGes campo : consulta.getListaCampos()) {
            String idCampo = campo.getIdCampo();
            ValorUI valorUI = new ValorUI();
            Object valor = entidad.get(idCampo);
            valorUI.setValor(campo.formatearValor(valor, paraEdicion));
            if (campo.tieneOpciones()) {
                valorUI.setOpciones(campo.getOpcionesEnumerado().getListaTextos());
            } else {
                valorUI.setOpciones(new ArrayList<>());
            }
            mapa.put(idCampo, valorUI);
        }
    }

    public void asignarValores(ElementoUI elemento) {
        mapa.putAll(elemento.mapa);
    }
}
