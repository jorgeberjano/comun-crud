package es.jbp.comun.crud.exportacion;

import es.jbp.comun.crud.elemento.ElementoUI;
import es.jbp.comun.crud.elemento.ValorUI;
import es.jbp.comun.crud.servicio.ServicioElemento;
import es.jbp.comun.ges.entidad.EntidadGes;
import es.jbp.comun.ges.entidad.CampoGes;
import es.jbp.comun.ges.entidad.ConsultaGes;
import es.jbp.comun.utiles.conversion.Conversion;
import es.jbp.comun.utiles.sql.TipoDato;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Genera exportacion en formato Excel a partir de los datos de sesión de una tabla.
 *
 * @author jberjano
 */
public class ExportadorExcel implements Exportador {

    /**
     * Crea un informe a partir de los datos de sesión de una tabla
     */
    public ExportadorExcel() {
    }

    /**
     * Genera la exportación
     */
    public void generar(OutputStream outputStream, ServicioElemento servicio) throws Exception {
        ConsultaGes consulta = servicio.getConsultaVisualizada();
        
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(servicio.getIdConsulta());
       
        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        int colNum = 0;
        for (CampoGes campo : consulta.getListaCampos()) {
            if (campo.isOculto()) {
                continue;
            }
            Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue(campo.getTitulo());
        }
        
        for (EntidadGes entidad : servicio.getListaEntidades()) {
            Row row = sheet.createRow(rowNum++);
            colNum = 0;
            for (CampoGes campo : consulta.getListaCampos()) {
                if (campo.isOculto()) {
                    continue;
                }
                
                Object valor = entidad.getValor(campo.getIdCampo());
                
                ElementoUI elementoUI = servicio.crearElemento(entidad, false);
                ValorUI valorUI = elementoUI.get(campo.getIdCampo());
                
                Cell cell = row.createCell(colNum++);
                if (campo.getTipoDato() == TipoDato.REAL) {
                    cell.setCellValue(Conversion.toDouble(valor));                    
                } else {
                    cell.setCellValue(valorUI.getValor());    
                }
            }
        }       
       
        workbook.write(outputStream);
        workbook.close();
    }

}
