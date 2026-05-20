package com.clinica.aauca.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.util.List;
import java.awt.Desktop;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFService {

    private static final String DESKTOP_PATH = System.getProperty("user.home") + File.separator + "Desktop" + File.separator;

    /**
     * Genera un documento PDF genérico con encabezado, tabla de datos y lo abre automáticamente.
     */
    public static String generarDocumento(String nombreArchivo, String titulo, String subtitulo, List<String[]> datos) {
        Document document = new Document(PageSize.A4);
        try {
            // Asegurar que el nombre tenga fecha para evitar conflictos
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            String fullPath = DESKTOP_PATH + nombreArchivo + "_" + timestamp + ".pdf";
            
            PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            document.open();

            // Encabezado Institucional
            Font fontEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, java.awt.Color.BLUE);
            Paragraph pEmpresa = new Paragraph("CLÍNICA AAUCA - AFRO-AMERICAN UNIVERSITY", fontEmpresa);
            pEmpresa.setAlignment(Element.ALIGN_CENTER);
            document.add(pEmpresa);
            
            document.add(new Paragraph("Sistema de Gestión Clínica | Guinea Ecuatorial", FontFactory.getFont(FontFactory.HELVETICA, 10)));
            document.add(new Paragraph("Fecha de impresión: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FontFactory.getFont(FontFactory.HELVETICA, 10)));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));

            // Título del Documento
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph pTitulo = new Paragraph(titulo.toUpperCase(), fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            pTitulo.setSpacingBefore(10);
            pTitulo.setSpacingAfter(5);
            document.add(pTitulo);

            if (subtitulo != null && !subtitulo.isEmpty()) {
                Paragraph pSub = new Paragraph(subtitulo, FontFactory.getFont(FontFactory.HELVETICA, 12));
                pSub.setAlignment(Element.ALIGN_CENTER);
                pSub.setSpacingAfter(15);
                document.add(pSub);
            }

            document.add(new Paragraph("\n"));

            // Tabla de Datos
            if (datos != null && !datos.isEmpty()) {
                PdfPTable table = new PdfPTable(datos.get(0).length);
                table.setWidthPercentage(100);

                // Estilo de Encabezado de Tabla
                for (String header : datos.get(0)) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.BOLD, java.awt.Color.WHITE)));
                    cell.setBackgroundColor(java.awt.Color.DARK_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                // Cuerpo de Tabla
                for (int i = 1; i < datos.size(); i++) {
                    for (String value : datos.get(i)) {
                        table.addCell(new Phrase(value != null ? value : "—", FontFactory.getFont(FontFactory.HELVETICA, 10)));
                    }
                }
                document.add(table);
            }

            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Firma y Sello Autorizado: ___________________________"));

            document.close();
            
            // Abrir automáticamente
            abrirArchivo(fullPath);
            
            return fullPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void abrirArchivo(String path) {
        try {
            File file = new File(path);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            System.err.println("No se pudo abrir el archivo: " + e.getMessage());
        }
    }
}
