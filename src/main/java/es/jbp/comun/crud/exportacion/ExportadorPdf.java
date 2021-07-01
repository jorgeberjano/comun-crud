package es.jbp.comun.crud.exportacion;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import es.jbp.comun.crud.elemento.ElementoUI;
import es.jbp.comun.crud.elemento.ValorUI;
import es.jbp.comun.crud.servicio.ServicioElemento;
import es.jbp.comun.ges.entidad.EntidadGes;
import es.jbp.comun.ges.entidad.CampoGes;
import es.jbp.comun.ges.entidad.ConsultaGes;
import static es.jbp.comun.ges.entidad.ConsultaGes.CONSIMPR_ORIENTACION_HORIZONTAL;
import es.jbp.comun.ges.entidad.TipoRolGes;
import es.jbp.comun.utiles.conversion.Conversion;
import java.io.OutputStream;

/**
 * Genera informes PDF a partir de los datos de sesión de una tabla.
 *
 * @author jberjano
 */
public class ExportadorPdf implements Exportador {

    private static final Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA, 26, Font.BOLDITALIC);
    private static final Font fuenteCabecera = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC);
    private static final Font fuenteNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);

    private ServicioElemento servicio;
    private ConsultaGes consulta;
    private String titulo;
    private String subtitulo;

    class ListenerEvento extends PdfPageEventHelper {

        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte contentByte = writer.getDirectContent();
            Phrase cabecera = new Phrase(titulo, fuenteCabecera);
            Phrase pie = new Phrase(String.format("Página %d", writer.getPageNumber()), fuenteCabecera);
            ColumnText.showTextAligned(contentByte, Element.ALIGN_RIGHT,
                    cabecera,
                    document.right(),
                    document.top() + 10, 0);
            ColumnText.showTextAligned(contentByte, Element.ALIGN_RIGHT,
                    pie,
                    document.right(),
                    document.bottom() - 10, 0);
        }
    }

    /**
     * Crea un informe a partir de los datos de sesión de una tabla
     */
    public ExportadorPdf() {
    }

    /**
     * Genera el informe en pdf
     */
    public void generar(OutputStream outputStream, ServicioElemento servicio) throws Exception {
        this.servicio = servicio;
        this.consulta = servicio.getConsultaVisualizada();

        Rectangle rect = PageSize.A4;
        if (consulta.tieneEstiloImpresion(CONSIMPR_ORIENTACION_HORIZONTAL)) {
            rect = rect.rotate();
        }
        Document document = new Document(rect);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        writer.setPageEvent(new ListenerEvento());
        document.open();

        titulo = consulta.getTituloImpresion();
        if (Conversion.isBlank(titulo)) {
            titulo = servicio.getNombreEnPlural();
        }

        subtitulo = consulta.getSubtituloImpresion();
        if (Conversion.isBlank(subtitulo)) {
            subtitulo = servicio.getSubtitulo();
        }

        // Metadatos
        document.addTitle(titulo);
//            document.addSubject("...");
//            document.addKeywords("...");
//            document.addAuthor("...");
//            document.addCreator("...");

        Chunk chunkTitulo = new Chunk(titulo + "\n", fuenteTitulo);
        Paragraph parrafoTitulo = new Paragraph(chunkTitulo);
        parrafoTitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(parrafoTitulo);

        Chunk chunkSubtitulo = new Chunk(subtitulo + "\n", fuenteNormal);
        Paragraph parrafoSubtitulo = new Paragraph(chunkSubtitulo);
        parrafoSubtitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(parrafoSubtitulo);

        PdfPTable tabla = generarTabla();
        if (tabla != null) {
            document.add(tabla);
        }
        document.close();

    }

    private PdfPTable generarTabla() throws DocumentException {

        int numeroColumnas = consulta.getListaCampos().size();
        PdfPTable table = new PdfPTable(numeroColumnas);
        table.setHeaderRows(1);
        table.setSpacingBefore(10f);
        table.setWidthPercentage(100);

        int anchosRelativos[] = new int[numeroColumnas];
        int i = 0;
        PdfPCell columnHeader;
        for (CampoGes campo : consulta.getListaCampos()) {
            if (campo.isOculto()) {
                continue;
            }
            columnHeader = new PdfPCell(new Phrase(campo.getTitulo(), fuenteCabecera));
            columnHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            columnHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(columnHeader);
            anchosRelativos[i++] = campo.getLongitud();
        }
        table.setWidths(anchosRelativos);

        Acumulador acumulador = new Acumulador();
        for (EntidadGes entidad : servicio.getListaEntidades()) {
            for (CampoGes campo : consulta.getListaCampos()) {
                if (campo.isOculto()) {
                    continue;
                }

                if (campo.esTotalizable()) {
                    acumulador.acumularValor(campo.getIdCampo(), entidad.get(campo.getIdCampo()));
                }
                ElementoUI elementoUI = servicio.crearElemento(entidad, false);
                ValorUI valor = elementoUI.get(campo.getIdCampo());
                PdfPCell celda = new PdfPCell(new Phrase(valor.getValor(), fuenteNormal));
                celda.setHorizontalAlignment(getAlineacionPdf(campo.getAlineacion()));
                table.addCell(celda);
            }
        }

        if (acumulador.hayAcumulados()) {
            for (CampoGes campo : consulta.getListaCampos()) {
                if (campo.isOculto()) {
                    continue;
                }
                PdfPCell celda;
                if (campo.esTotalizable()) {
                    Double total = acumulador.getAcumulado(campo.getIdCampo());
                    //Object valor = campo.convertirValor(total);
                    Object valor = Conversion.convertirValor(total, campo.getTipoDato());
                    String texto = campo.formatearValor(valor, false);
                    celda = new PdfPCell(new Phrase(texto, fuenteNormal));
                } else {
                    celda = new PdfPCell(new Phrase("", fuenteNormal));
                }
                celda.setHorizontalAlignment(getAlineacionPdf(campo.getAlineacion()));
                table.addCell(celda);
            }
        }

        return table;
    }

    private int getAlineacionPdf(int alineacionCampo) {
        int alineacion = Element.ALIGN_LEFT;
        switch (alineacionCampo) {
            case CampoGes.ALINEACION_CENTRO:
                alineacion = Element.ALIGN_CENTER;
                break;
            case CampoGes.ALINEACION_DERECHA:
                alineacion = Element.ALIGN_RIGHT;
                break;
        }
        return alineacion;
    }
}
