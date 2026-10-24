import os
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas

def generate():
    os.makedirs('uploads/shelter-documents', exist_ok=True)

    # 1. Scenario A: Valid Demo Certificate
    c = canvas.Canvas('uploads/shelter-documents/demo_certificate_valid_A.pdf', pagesize=letter)
    c.setFont('Helvetica-Bold', 22)
    c.drawString(100, 720, 'PETCONNECT DEMO SHELTER')
    c.setFont('Helvetica', 14)
    c.drawString(100, 690, 'Demo Shelter Registration Certificate')
    c.drawString(100, 640, 'Shelter Name: Happy Tails Demo Shelter')
    c.drawString(100, 610, 'Registration Number: DEMO/2026/001')
    c.drawString(100, 580, 'Address: Hyderabad, Telangana')
    c.drawString(100, 550, 'Issue Date: 15/01/2025')
    c.drawString(100, 520, 'Expiry Date: 15/01/2028')
    c.drawString(100, 480, 'Machine-Readable QR Code Hint: DEMO-SECURE-2026')
    c.setFont('Helvetica-Oblique', 10)
    c.drawString(100, 440, 'DEMO MODE — FOR TESTING ONLY: Simulated verification document for presentation.')
    c.save()

    # 2. Scenario B: Uncertain Demo Certificate
    c = canvas.Canvas('uploads/shelter-documents/demo_certificate_uncertain_B.pdf', pagesize=letter)
    c.setFont('Helvetica-Bold', 22)
    c.drawString(100, 720, 'PETCONNECT DEMO SHELTER')
    c.setFont('Helvetica', 14)
    c.drawString(100, 690, 'Demo Shelter Registration Certificate')
    c.drawString(100, 640, 'Shelter Name: Happy Tails Demo Shelter')
    c.drawString(100, 610, 'Registration Number: DEMO/2026/???')
    c.drawString(100, 550, 'Issue Date: 10/02/2025')
    c.setFont('Helvetica-Oblique', 10)
    c.drawString(100, 440, 'DEMO MODE — FOR TESTING ONLY (Incomplete details for manual review testing)')
    c.save()

    # 3. Scenario C: Invalid Demo Certificate (Mismatch)
    c = canvas.Canvas('uploads/shelter-documents/demo_certificate_invalid_C.pdf', pagesize=letter)
    c.setFont('Helvetica-Bold', 22)
    c.drawString(100, 720, 'PETCONNECT DEMO SHELTER')
    c.setFont('Helvetica', 14)
    c.drawString(100, 690, 'Demo Shelter Registration Certificate')
    c.drawString(100, 640, 'Shelter Name: Happy Tails Demo Shelter')
    c.drawString(100, 610, 'Registration Number: DEMO/2026/999')
    c.drawString(100, 580, 'Address: Hyderabad, Telangana')
    c.drawString(100, 550, 'Issue Date: 15/01/2025')
    c.drawString(100, 520, 'Expiry Date: 15/01/2028')
    c.setFont('Helvetica-Oblique', 10)
    c.drawString(100, 440, 'DEMO MODE — FOR TESTING ONLY (Conflicting registration number DEMO/2026/999)')
    c.save()

if __name__ == '__main__':
    generate()
    print("Demo certificates generated successfully!")
