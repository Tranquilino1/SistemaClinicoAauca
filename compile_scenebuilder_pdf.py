import os
import re
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch

def build_pdf():
    # Nombre del archivo de salida del PDF
    pdf_filename = "manual_scenebuilder.pdf"
    
    # Configurar el documento PDF con tamaño carta y márgenes
    doc = SimpleDocTemplate(
        pdf_filename,
        pagesize=letter,
        rightMargin=54, leftMargin=54, topMargin=54, bottomMargin=54
    )
    
    styles = getSampleStyleSheet()
    
    # Estilos de párrafo personalizados
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=20,
        leading=24,
        textColor=colors.HexColor('#003366'),
        spaceAfter=15
    )
    
    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=14,
        leading=18,
        textColor=colors.HexColor('#003366'),
        spaceBefore=14,
        spaceAfter=8,
        keepWithNext=True
    )
    
    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=11,
        leading=14,
        textColor=colors.HexColor('#1E3E62'),
        spaceBefore=10,
        spaceAfter=6,
        keepWithNext=True
    )
    
    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=13.5,
        textColor=colors.HexColor('#333333'),
        spaceAfter=6
    )
    
    bullet_style = ParagraphStyle(
        'Bullet_Custom',
        parent=body_style,
        leftIndent=15,
        spaceAfter=4
    )
    
    table_header_style = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8.5,
        leading=10,
        textColor=colors.white
    )
    
    table_cell_style = ParagraphStyle(
        'TableCell',
        parent=body_style,
        fontSize=8.5,
        leading=10,
        spaceAfter=0
    )
    
    warning_style = ParagraphStyle(
        'WarningBox',
        parent=body_style,
        fontName='Helvetica-Oblique',
        textColor=colors.HexColor('#003366'),
        backColor=colors.HexColor('#E3F2FD'),
        borderColor=colors.HexColor('#1565C0'),
        borderWidth=0.5,
        borderPadding=8,
        spaceBefore=8,
        spaceAfter=8
    )
    
    story = []
    
    # Leer el manual escrito en markdown
    with open("MANUAL_SCENEBUILDER.md", "r", encoding="utf-8") as f:
        lines = f.readlines()
        
    in_table = False
    table_data = []
    
    for line in lines:
        line_str = line.strip()
        
        # Procesamiento de tablas markdown
        if line_str.startswith('|'):
            # Saltar fila de separadores (| :--- | :--- |)
            if '---' in line_str:
                continue
            cells = [c.strip() for c in line_str.split('|')[1:-1]]
            if not in_table:
                in_table = True
                table_data = [cells]
            else:
                table_data.append(cells)
            continue
        else:
            if in_table:
                # Generar tabla en ReportLab
                in_table = False
                formatted_data = []
                for idx, row in enumerate(table_data):
                    formatted_row = []
                    for cell in row:
                        # Limpiar etiquetas markdown de negritas y código
                        cell_clean = re.sub(r'\*\*(.*?)\*\*|\*(.*?)\*', r'\1\2', cell)
                        cell_clean = re.sub(r'`(.*?)`', r'\1', cell_clean)
                        style = table_header_style if idx == 0 else table_cell_style
                        formatted_row.append(Paragraph(cell_clean, style))
                    formatted_data.append(formatted_row)
                
                # Configuración de anchos de columnas de la tabla (Suma total = 7.0 pulgadas)
                col_widths = [0.8*inch, 1.8*inch, 2.0*inch, 2.4*inch]
                t = Table(formatted_data, colWidths=col_widths)
                t.setStyle(TableStyle([
                    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#003366')),
                    ('ALIGN', (0,0), (-1,-1), 'LEFT'),
                    ('VALIGN', (0,0), (-1,-1), 'TOP'),
                    ('BOTTOMPADDING', (0,0), (-1,0), 6),
                    ('TOPPADDING', (0,0), (-1,0), 6),
                    ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#CCCCCC')),
                    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F8FAFC')]),
                    ('BOTTOMPADDING', (0,1), (-1,-1), 5),
                    ('TOPPADDING', (0,1), (-1,-1), 5),
                ]))
                story.append(t)
                story.append(Spacer(1, 8))
                table_data = []
                
        if not line_str:
            story.append(Spacer(1, 4))
            continue
            
        # Parsear Encabezados y Markdown
        if line_str.startswith('# '):
            text = line_str[2:]
            story.append(Paragraph(text, title_style))
            story.append(Spacer(1, 8))
        elif line_str.startswith('## '):
            text = line_str[3:]
            story.append(Paragraph(text, h1_style))
        elif line_str.startswith('### '):
            text = line_str[4:]
            story.append(Paragraph(text, h2_style))
        elif line_str.startswith('---'):
            story.append(Spacer(1, 10))
        elif line_str.startswith('* ') or line_str.startswith('- '):
            text = line_str[2:]
            text = re.sub(r'\*\*(.*?)\*\*', r'<b>\1</b>', text)
            text = re.sub(r'\[(.*?)\]\((.*?)\)', r'<font color="#003366"><u>\1</u></font>', text)
            text = re.sub(r'`(.*?)`', r'<font face="Courier"><b>\1</b></font>', text)
            story.append(Paragraph(f"&bull; {text}", bullet_style))
        elif line_str.startswith('1. ') or re.match(r'^\d+\.', line_str):
            text = re.sub(r'^\d+\.\s*', '', line_str)
            text = re.sub(r'\*\*(.*?)\*\*', r'<b>\1</b>', text)
            text = re.sub(r'\[(.*?)\]\((.*?)\)', r'<font color="#003366"><u>\1</u></font>', text)
            text = re.sub(r'`(.*?)`', r'<font face="Courier"><b>\1</b></font>', text)
            prefix = line_str.split('.')[0]
            story.append(Paragraph(f"{prefix}. {text}", bullet_style))
        elif line_str.startswith('>'):
            text = line_str.replace('>', '').strip()
            if '[!IMPORTANT]' in text:
                text = text.replace('[!IMPORTANT]', '<b>IMPORTANTE:</b>')
            text = re.sub(r'\*\*(.*?)\*\*', r'<b>\1</b>', text)
            story.append(Paragraph(text, warning_style))
        else:
            text = line_str
            text = re.sub(r'\*\*(.*?)\*\*', r'<b>\1</b>', text)
            text = re.sub(r'\[(.*?)\]\((.*?)\)', r'<font color="#003366"><u>\1</u></font>', text)
            text = re.sub(r'`(.*?)`', r'<font face="Courier"><b>\1</b></font>', text)
            story.append(Paragraph(text, body_style))
            
    doc.build(story)
    print("PDF creado con éxito.")

if __name__ == '__main__':
    build_pdf()
