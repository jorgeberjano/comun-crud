package es.jbp.comun.crud.exportacion;

/**
 * Factoria para crear el exportador adecuado segun el formato
 * @author jorge
 */
public class FactoriaExportadores {
    public static Exportador crearExportador(String formato) {
        formato = formato.toLowerCase();
        switch (formato) {
            case "csv":
                return new ExportadorCsv();
            case "pdf":
                return new ExportadorPdf();
            case "xlsx":
                return new ExportadorExcel();
            default:
                return null;
        }
    }
}
