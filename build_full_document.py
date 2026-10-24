# build_full_document.py
# Comprehensive script to build PetConnect Project Documentation DOCX and PDF

import os
import zipfile
import shutil
import docx
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

SOURCE_DOCX = r"C:\Users\sreen\Downloads\CureHub_Project_Documentation.docx"
TARGET_DOCX = r"E:\petconnect\PetConnect_Project_Documentation.docx"
TARGET_PDF = r"E:\petconnect\PetConnect_Project_Documentation.pdf"
DB_IMAGE = r"E:\petconnect\Figure_4_1_Database_Design.png"

DOWNLOADS_DOCX = r"C:\Users\sreen\Downloads\PetConnect_Project_Documentation.docx"
DOWNLOADS_PDF = r"C:\Users\sreen\Downloads\PetConnect_Project_Documentation.pdf"

def set_run_font(run, font_name="Times New Roman", size_pt=12, bold=False, italic=False, color=None):
    run.font.name = font_name
    run.font.size = Pt(size_pt)
    run.bold = bold
    run.italic = italic
    if color:
        run.font.color.rgb = color
    rPr = run._r.get_or_add_rPr()
    rFonts = rPr.find(qn('w:rFonts'))
    if rFonts is None:
        rFonts = OxmlElement('w:rFonts')
        rPr.insert(0, rFonts)
    rFonts.set(qn('w:ascii'), font_name)
    rFonts.set(qn('w:hAnsi'), font_name)
    rFonts.set(qn('w:cs'), font_name)

def format_paragraph(p, text, font_size=12, bold=False, italic=False, align=WD_ALIGN_PARAGRAPH.JUSTIFY, color=None):
    p.text = ""
    p.alignment = align
    run = p.add_run(text)
    set_run_font(run, font_name="Times New Roman", size_pt=font_size, bold=bold, italic=italic, color=color)
    return run

def format_heading(p, text, font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT):
    p.text = ""
    p.alignment = align
    run = p.add_run(text)
    set_run_font(run, font_name="Times New Roman", size_pt=font_size, bold=bold, italic=False, color=RGBColor(0, 0, 0))
    return run

print("Script template ready.")
