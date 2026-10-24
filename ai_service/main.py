from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
import os
import shutil
import tempfile
import uvicorn
from verifier import verify_shelter_document

app = FastAPI(
    title="PetConnect AI Shelter Document Verification Service",
    description="Microservice for OCR extraction, tampering detection, and shelter document authenticity assessment",
    version="1.0.0"
)

class VerificationRequest(BaseModel):
    filePath: str = Field(..., description="Path to uploaded document on local disk")
    shelterName: str = Field(..., description="Registered shelter name in MySQL")
    address: Optional[str] = Field(None, description="Registered shelter address")
    city: Optional[str] = Field(None, description="Registered shelter city")
    state: Optional[str] = Field(None, description="Registered shelter state")
    registrationNumber: Optional[str] = Field(None, description="Pre-existing registration number in DB")
    autoRejectThreshold: Optional[int] = Field(40, description="Score below which document is auto rejected")
    lowRiskThreshold: Optional[int] = Field(85, description="Score above which document is low risk")
    weights: Optional[Dict[str, int]] = Field(None, description="Custom criteria weight configuration")
    verificationMode: Optional[str] = Field("PRODUCTION", description="Verification mode: PRODUCTION or DEMO")

@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "PetConnect-AI-Document-Verifier",
        "version": "1.0.0"
    }

@app.post("/verify-document")
def verify_document(req: VerificationRequest):
    if not os.path.exists(req.filePath):
        raise HTTPException(status_code=404, detail=f"File not found: {req.filePath}")
        
    result = verify_shelter_document(
        file_path=req.filePath,
        shelter_name=req.shelterName,
        address=req.address,
        city=req.city,
        state=req.state,
        db_registration_number=req.registrationNumber,
        weights=req.weights,
        auto_reject_threshold=req.autoRejectThreshold or 40,
        low_risk_threshold=req.lowRiskThreshold or 85,
        verification_mode=req.verificationMode or "PRODUCTION"
    )
    return result

@app.post("/verify-upload")
async def verify_upload(
    file: UploadFile = File(...),
    shelterName: str = Form(...),
    address: Optional[str] = Form(None),
    city: Optional[str] = Form(None),
    state: Optional[str] = Form(None),
    registrationNumber: Optional[str] = Form(None),
    autoRejectThreshold: Optional[int] = Form(40),
    lowRiskThreshold: Optional[int] = Form(85),
    verificationMode: Optional[str] = Form("PRODUCTION")
):
    ext = os.path.splitext(file.filename)[1].lower() if file.filename else ".tmp"
    with tempfile.NamedTemporaryFile(delete=False, suffix=ext) as tmp:
        shutil.copyfileobj(file.file, tmp)
        tmp_path = tmp.name

    try:
        result = verify_shelter_document(
            file_path=tmp_path,
            shelter_name=shelterName,
            address=address,
            city=city,
            state=state,
            db_registration_number=registrationNumber,
            auto_reject_threshold=autoRejectThreshold or 40,
            low_risk_threshold=lowRiskThreshold or 85,
            verification_mode=verificationMode or "PRODUCTION"
        )
        return result
    finally:
        if os.path.exists(tmp_path):
            os.remove(tmp_path)

if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=False)
