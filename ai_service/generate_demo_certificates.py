import os
import argparse
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas

def generate(shelter_name="Happy Tails Demo Shelter", reg_no="DEMO/2026/001", address="Hyderabad, Telangana", suffix=""):
    os.makedirs('uploads/shelter-documents', exist_ok=True)
    file_suffix = f"_{suffix}" if suffix else ""

    # 1. Scenario A: Valid Demo Certificate (matches shelter name, reg, address)
    a_path = f'uploads/shelter-documents/demo_certificate_valid_A{file_suffix}.pdf'
    c = canvas.Canvas(a_path, pagesize=letter)
    c.setFont('Helvetica-Bold', 22)
    c.drawString(100, 720, 'PETCONNECT DEMO SHELTER')
    c.setFont('Helvetica', 14)
    c.drawString(100, 690, 'Demo Shelter Registration Certificate')
    c.drawString(100, 640, f'Shelter Name: {shelter_name}')
    c.drawString(100, 610, f'Registration Number: {reg_no}')
    c.drawString(100, 580, f'Address: {address}')
    c.drawString(100, 550, 'Issue Date: 15/01/2025')
    c.drawString(100, 520, 'Expiry Date: 15/01/2028')
    c.drawString(100, 480, 'Machine-Readable QR Code Hint: DEMO-SECURE-2026')
    c.setFont('Helvetica-Oblique', 10)
    c.drawString(100, 440, 'DEMO MODE — FOR TESTING ONLY: Simulated verification document for presentation.')
    c.save()

    # Also keep standard un-suffixed file as fallback
    if file_suffix:
        standard_a = 'uploads/shelter-documents/demo_certificate_valid_A.pdf'
        try:
            with open(a_path, 'rb') as src, open(standard_a, 'wb') as dst:
                dst.write(src.read())
        except Exception:
            pass

    # 2. Scenario B: Uncertain Demo Certificate (missing address, unclear reg)
    b_path = f'uploads/shelter-documents/demo_certificate_uncertain_B{file_suffix}.pdf'
    c = canvas.Canvas(b_path, pagesize=letter)
    c.setFont('Helvetica-Bold', 22)
    c.drawString(100, 720, 'PETCONNECT DEMO SHELTER')
    c.setFont('Helvetica', 14)
    c.drawString(100, 690, 'Demo Shelter Registration Certificate')
    c.drawString(100, 640, f'Shelter Name: {shelter_name}')
    c.drawString(100, 610, 'Registration Number: DEMO/2026/???')
    c.drawString(100, 550, 'Issue Date: 10/02/2025')
    c.setFont('Helvetica-Oblique', 10)
    c.drawString(100, 440, 'DEMO MODE — FOR TESTING ONLY (Incomplete details for manual review testing)')
    c.save()

    if file_suffix:
        standard_b = 'uploads/shelter-documents/demo_certificate_uncertain_B.pdf'
        try:
            with open(b_path, 'rb') as src, open(standard_b, 'wb') as dst:
                dst.write(src.read())
        except Exception:
            pass

    # 3. Scenario C: Invalid Demo Certificate (Registration number mismatch)
    c_path = f'uploads/shelter-documents/demo_certificate_invalid_C{file_suffix}.pdf'
    c = canvas.Canvas(c_path, pagesize=letter)
    c.setFont('Helvetica-Bold', 22)
    c.drawString(100, 720, 'PETCONNECT DEMO SHELTER')
    c.setFont('Helvetica', 14)
    c.drawString(100, 690, 'Demo Shelter Registration Certificate')
    c.drawString(100, 640, f'Shelter Name: {shelter_name}')
    c.drawString(100, 610, 'Registration Number: DEMO/2026/999-MISMATCH')
    c.drawString(100, 580, f'Address: {address}')
    c.drawString(100, 550, 'Issue Date: 15/01/2025')
    c.drawString(100, 520, 'Expiry Date: 15/01/2028')
    c.setFont('Helvetica-Oblique', 10)
    c.drawString(100, 440, 'DEMO MODE — FOR TESTING ONLY (Conflicting registration number DEMO/2026/999-MISMATCH)')
    c.save()

    if file_suffix:
        standard_c = 'uploads/shelter-documents/demo_certificate_invalid_C.pdf'
        try:
            with open(c_path, 'rb') as src, open(standard_c, 'wb') as dst:
                dst.write(src.read())
        except Exception:
            pass

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--name', default='Happy Tails Demo Shelter')
    parser.add_argument('--reg', default='DEMO/2026/001')
    parser.add_argument('--address', default='Hyderabad, Telangana')
    parser.add_argument('--suffix', default='')
    args = parser.parse_args()

    generate(shelter_name=args.name, reg_no=args.reg, address=args.address, suffix=args.suffix)
    print("Demo certificates generated successfully!")
