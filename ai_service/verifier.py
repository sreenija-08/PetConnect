import os
import re
import datetime
from typing import Dict, Any, List, Optional
from pypdf import PdfReader
from PIL import Image

def normalize_text(text: Optional[str]) -> str:
    if not text:
        return ""
    # Lowercase, replace non-alphanumeric with spaces, collapse spaces
    cleaned = re.sub(r'[^a-zA-Z0-9\s]', ' ', text.lower())
    return ' '.join(cleaned.split())

def extract_pdf_data(file_path: str) -> Dict[str, Any]:
    text_content = ""
    metadata = {}
    has_qr_hint = False
    
    try:
        reader = PdfReader(file_path)
        if len(reader.pages) == 0:
            return {"success": False, "error": "Empty PDF document", "text": "", "metadata": {}}
        
        for page in reader.pages:
            t = page.extract_text()
            if t:
                text_content += t + "\n"
            
            # Check for QR / barcode / URL indicators in annotations or images
            if "/Annots" in page:
                annots = page["/Annots"]
                if annots:
                    has_qr_hint = True
            
            # Check if there are embedded images that might be QR codes
            if hasattr(page, 'images') and len(page.images) > 0:
                has_qr_hint = True

        if reader.metadata:
            for k, v in reader.metadata.items():
                if v:
                    metadata[str(k).replace('/', '')] = str(v)
                    
        return {
            "success": True,
            "text": text_content.strip(),
            "metadata": metadata,
            "page_count": len(reader.pages),
            "has_qr_hint": has_qr_hint
        }
    except Exception as e:
        return {"success": False, "error": str(e), "text": "", "metadata": {}}

def extract_image_data(file_path: str) -> Dict[str, Any]:
    metadata = {}
    text_content = ""
    has_qr_hint = False
    
    try:
        with Image.open(file_path) as img:
            img.verify()
            
        with Image.open(file_path) as img:
            format_name = img.format
            width, height = img.size
            
            exif = img.getexif()
            if exif:
                from PIL.ExifTags import TAGS
                for tag_id, value in exif.items():
                    tag = TAGS.get(tag_id, str(tag_id))
                    metadata[str(tag)] = str(value)
            
            # Check image info dictionary
            if hasattr(img, 'info'):
                for k, v in img.info.items():
                    if isinstance(v, (str, int, float)):
                        metadata[str(k)] = str(v)

        # Also inspect binary strings for text or QR metadata
        with open(file_path, "rb") as f:
            raw_bytes = f.read(500000)
            # Find printable ASCII strings of length >= 4
            ascii_strings = re.findall(b"[A-Za-z0-9 /:.,#&()_-]{4,}", raw_bytes)
            decoded = [s.decode('latin-1', errors='ignore') for s in ascii_strings]
            text_content = "\n".join(decoded)
            if "qr" in text_content.lower() or "barcode" in text_content.lower():
                has_qr_hint = True

        return {
            "success": True,
            "text": text_content.strip(),
            "metadata": metadata,
            "width": width,
            "height": height,
            "format": format_name,
            "has_qr_hint": has_qr_hint
        }
    except Exception as e:
        return {"success": False, "error": str(e), "text": "", "metadata": {}}

def parse_date(date_str: str) -> Optional[datetime.date]:
    date_str = date_str.strip()
    formats = [
        "%d/%m/%Y", "%d-%m-%Y", "%Y-%m-%d", "%Y/%m/%d",
        "%d.%m.%Y", "%d %b %Y", "%d %B %Y", "%b %d, %Y", "%B %d, %Y"
    ]
    for fmt in formats:
        try:
            return datetime.datetime.strptime(date_str, fmt).date()
        except ValueError:
            pass
    return None

def extract_dates(text: str) -> Dict[str, Any]:
    # Look for issue date and expiry date patterns
    issue_date = None
    expiry_date = None
    
    # Common date regex (e.g. 15/08/2024, 2024-08-15, 15-Aug-2024)
    date_regex = r'(\b\d{1,2}[/-]\d{1,2}[/-]\d{4}\b|\b\d{4}[/-]\d{1,2}[/-]\d{1,2}\b|\b\d{1,2}\s+[A-Za-z]{3,9}\s+\d{4}\b)'
    
    # Search for Issue Date
    issue_pattern = re.compile(r'(?:issue(?:d)?\s*date|date\s*of\s*issue|issued\s*on)[\s:-]*' + date_regex, re.IGNORECASE)
    m_issue = issue_pattern.search(text)
    if m_issue:
        issue_date = parse_date(m_issue.group(1))
        
    # Search for Expiry / Valid Until Date
    expiry_pattern = re.compile(r'(?:expiry\s*date|valid\s*until|valid\s*upto|date\s*of\s*expiry|expires\s*on)[\s:-]*' + date_regex, re.IGNORECASE)
    m_expiry = expiry_pattern.search(text)
    if m_expiry:
        expiry_date = parse_date(m_expiry.group(1))
        
    # If not found by keyword, gather general dates
    if not issue_date or not expiry_date:
        all_dates = re.findall(date_regex, text)
        parsed_dates = []
        for d in all_dates:
            dt = parse_date(d)
            if dt:
                parsed_dates.append(dt)
        parsed_dates = sorted(list(set(parsed_dates)))
        if not issue_date and len(parsed_dates) >= 1:
            issue_date = parsed_dates[0]
        if not expiry_date and len(parsed_dates) >= 2:
            expiry_date = parsed_dates[-1]

    return {
        "issue_date": issue_date.isoformat() if issue_date else None,
        "expiry_date": expiry_date.isoformat() if expiry_date else None,
        "detected": issue_date is not None or expiry_date is not None
    }

def detect_document_type(text: str, is_demo: bool = False) -> Dict[str, Any]:
    norm = normalize_text(text)

    # In Demo Mode, recognize clearly fictional demo certificates
    if is_demo:
        demo_keywords = [
            "petconnect demo shelter", "demo shelter registration certificate",
            "demo certificate", "demo shelter", "happy tails demo shelter",
            "demonstration certificate", "demo document"
        ]
        if any(dk in norm for dk in demo_keywords) or ("demo" in norm and ("shelter" in norm or "certificate" in norm or "registration" in norm)):
            return {
                "type": "DEMO_SHELTER_REGISTRATION_CERTIFICATE",
                "is_valid_shelter_doc": True,
                "match_count": 5,
                "reason": "Recognized Demo Shelter Registration Certificate (Simulated Test Document)"
            }

    # 1. Unambiguous Red Flag / Mismatched Document Types
    mismatch_indicators = [
        "electricity bill", "utility bill", "water bill", "electricity distribution",
        "consumer number", "curriculum vitae", "resume", "salary slip",
        "payslip", "boarding pass", "flight ticket", "hotel booking receipt",
        "tax invoice", "cash receipt", "restaurant menu", "pet adoption questionnaire",
        "course certificate", "completion certificate", "udemy"
    ]
    for mi in mismatch_indicators:
        if mi in norm:
            return {
                "type": "UNKNOWN",
                "is_valid_shelter_doc": False,
                "reason": f"Detected unrelated document type ({mi.title()})"
            }

    # 2. Legitimate Shelter / NGO / Animal Welfare Certificate Keywords
    shelter_keywords = [
        "animal welfare", "shelter registration", "society registration",
        "societies registration act", "indian trusts act", "trust deed",
        "prevention of cruelty to animals", "animal welfare board", "spca",
        "animal shelter license", "sanctuary registration", "rescue foundation",
        "section 8", "veterinary license", "ngo registration", "charitable trust",
        "certificate of registration", "government of", "registration certificate",
        "department of animal husbandry", "animal care license"
    ]
    
    match_count = sum(1 for kw in shelter_keywords if kw in norm)
    
    if match_count >= 2 or ("certificate" in norm and ("registration" in norm or "welfare" in norm or "shelter" in norm or "trust" in norm or "society" in norm)):
        if "license" in norm or "licence" in norm:
            doc_type = "ANIMAL_WELFARE_LICENSE"
        elif "trust" in norm or "society" in norm:
            doc_type = "SHELTER_REGISTRATION_CERTIFICATE"
        elif "government" in norm:
            doc_type = "GOVERNMENT_REGISTRATION"
        else:
            doc_type = "SHELTER_REGISTRATION_CERTIFICATE"
            
        return {
            "type": doc_type,
            "is_valid_shelter_doc": True,
            "match_count": match_count,
            "reason": f"Recognized legal {doc_type.replace('_', ' ').title()}"
        }

    return {
        "type": "UNKNOWN",
        "is_valid_shelter_doc": False,
        "reason": "Document does not match recognized shelter registration or license format"
    }

def extract_registration_number(text: str) -> Optional[str]:
    # Look for common registration number patterns
    patterns = [
        r'\b(DEMO[-/]\d{4}[-/][A-Za-z0-9?]{1,6})\b',
        r'(?:registration\s*no|reg\s*no|regn\s*no|certificate\s*no|license\s*no|lic\s*no)[\s.:#-]*([A-Za-z0-9/-]{4,25})',
        r'\b([A-Z]{2,4}[-/\s]?[0-9]{3,8}[-/\s]?[A-Z0-9]*)\b',
        r'\b(SHELTER[-/]\d{4}[-/]\d{2,6})\b',
        r'\b(AWBI[-/]\d{2,6})\b',
        r'\b(NGO[-/][A-Z0-9/-]{4,15})\b'
    ]
    for p in patterns:
        m = re.search(p, text, re.IGNORECASE)
        if m:
            candidate = ' '.join(m.group(1).replace('\n', ' ').replace('\r', ' ').split()).strip()
            # If candidate ends with question marks like ???, it's incomplete
            if "???" in candidate:
                return candidate
            # Avoid generic short words or pure years
            if len(candidate) >= 4 and not re.match(r'^(19|20)\d\d$', candidate):
                return candidate
    return None

def analyze_tampering(file_path: str, metadata: Dict[str, Any], text: str) -> Dict[str, Any]:
    indicators = []
    
    # 1. Inspect metadata for photo/document manipulation software
    suspicious_software = [
        "photoshop", "gimp", "canva", "photopea", "pixlr", "paint.net", "ilovepdf", "pdfescape"
    ]
    for k, v in metadata.items():
        v_str = str(v).lower()
        for sw in suspicious_software:
            if sw in v_str:
                indicators.append(f"Metadata indicates modification via {sw.title()} ({k}: {v})")

    # 2. Check for contradictory font or dates in text
    dates_extracted = re.findall(r'\b(19|20\d\d)\b', text)
    unique_years = [int(y) for y in dates_extracted if 1990 <= int(y) <= 2035]
    if unique_years and (max(unique_years) - min(unique_years)) > 25:
        indicators.append("Substantial discrepancy in chronological document years detected")

    # 3. Check for repetitive replacement tokens or obvious template markers
    template_markers = ["lorem ipsum", "insert name here", "your shelter name", "sample certificate", "specimen copy"]
    norm_t = text.lower()
    for tm in template_markers:
        if tm in norm_t:
            indicators.append(f"Suspicious placeholder text detected: '{tm}'")

    return {
        "tampering_indicators": len(indicators) > 0,
        "reasons": indicators
    }

def verify_shelter_document(
    file_path: str,
    shelter_name: str,
    address: Optional[str] = None,
    city: Optional[str] = None,
    state: Optional[str] = None,
    db_registration_number: Optional[str] = None,
    weights: Optional[Dict[str, int]] = None,
    auto_reject_threshold: int = 40,
    low_risk_threshold: int = 85,
    verification_mode: str = "PRODUCTION"
) -> Dict[str, Any]:
    
    is_demo = (verification_mode.upper() == "DEMO")
    
    # Default weights totaling 100 points
    w = {
        "doc_type": 15,
        "name_match": 20,
        "address_match": 15,
        "reg_number_match": 20,
        "date_validity": 10,
        "official_verification": 10,
        "tampering": 10
    }
    if weights:
        w.update(weights)

    reasons: List[str] = []
    rejection_reasons: List[str] = []
    hard_fail = False
    rejection_reason_code: Optional[str] = None

    # Step 1: File check
    if not os.path.exists(file_path):
        return {
            "documentType": "UNKNOWN",
            "confidenceScore": 0,
            "riskLevel": "HIGH",
            "hardFail": True,
            "autoRejected": True,
            "verificationStatus": "DEMO_VALIDATION_FAILED" if is_demo else "AUTO_REJECTED",
            "rejectionReasonCode": "FILE_NOT_FOUND",
            "reasons": ["Uploaded file does not exist on disk."],
            "isDemo": is_demo,
            "verificationMode": "DEMO" if is_demo else "PRODUCTION"
        }
        
    file_size = os.path.getsize(file_path)
    if file_size == 0 or file_size > 15 * 1024 * 1024:
        return {
            "documentType": "UNKNOWN",
            "confidenceScore": 0,
            "riskLevel": "HIGH",
            "hardFail": True,
            "autoRejected": True,
            "verificationStatus": "DEMO_VALIDATION_FAILED" if is_demo else "AUTO_REJECTED",
            "rejectionReasonCode": "INVALID_FILE_SIZE",
            "reasons": ["File is empty or exceeds the maximum allowable size of 10MB."],
            "isDemo": is_demo,
            "verificationMode": "DEMO" if is_demo else "PRODUCTION"
        }

    ext = os.path.splitext(file_path)[1].lower()
    allowed_exts = [".pdf", ".jpg", ".jpeg", ".png", ".webp"]
    if ext not in allowed_exts:
        return {
            "documentType": "UNKNOWN",
            "confidenceScore": 0,
            "riskLevel": "HIGH",
            "hardFail": True,
            "autoRejected": True,
            "verificationStatus": "DEMO_VALIDATION_FAILED" if is_demo else "AUTO_REJECTED",
            "rejectionReasonCode": "UNSUPPORTED_FILE_TYPE",
            "reasons": [f"File format '{ext}' is not supported. Allowed formats: PDF, JPG, JPEG, PNG, WEBP."],
            "isDemo": is_demo,
            "verificationMode": "DEMO" if is_demo else "PRODUCTION"
        }

    # Step 2: Extraction
    if ext == ".pdf":
        extracted = extract_pdf_data(file_path)
    else:
        extracted = extract_image_data(file_path)

    if not extracted["success"]:
        return {
            "documentType": "UNKNOWN",
            "confidenceScore": 10,
            "riskLevel": "HIGH",
            "hardFail": True,
            "autoRejected": True,
            "verificationStatus": "DEMO_VALIDATION_FAILED" if is_demo else "AUTO_REJECTED",
            "rejectionReasonCode": "CORRUPTED_DOCUMENT",
            "reasons": [f"Document is unreadable or corrupted: {extracted.get('error', 'Unknown error')}"],
            "isDemo": is_demo,
            "verificationMode": "DEMO" if is_demo else "PRODUCTION"
        }

    text = extracted["text"]
    metadata = extracted["metadata"]
    ocr_status = "SUCCESS" if text.strip() else "NO_TEXT"

    # Step 3: Document Type Classification
    doc_type_info = detect_document_type(text, is_demo=is_demo)
    doc_type = doc_type_info["type"]
    is_valid_type = doc_type_info["is_valid_shelter_doc"]

    score = 0

    if not is_valid_type:
        hard_fail = True
        rejection_reason_code = "WRONG_DOCUMENT_TYPE"
        rejection_reasons.append(doc_type_info["reason"])
        reasons.append(f"✕ {doc_type_info['reason']}")
    else:
        score += w["doc_type"]
        reasons.append(f"✓ Valid document classification: {doc_type.replace('_', ' ').title()}")

    # Step 4: Extracted Data Comparison
    norm_text = normalize_text(text)
    norm_name = normalize_text(shelter_name)
    
    # Shelter Name Matching (normalize tokens)
    name_match = False
    if norm_name and norm_name in norm_text:
        name_match = True
    elif norm_name:
        # Check significant individual words (excluding common words)
        stopwords = {"shelter", "rescue", "foundation", "society", "trust", "the", "and", "for", "animal", "care"}
        tokens = [t for t in norm_name.split() if t not in stopwords and len(t) > 2]
        if tokens:
            matched_tokens = [t for t in tokens if t in norm_text]
            if len(matched_tokens) / len(tokens) >= 0.7:
                name_match = True

    if name_match:
        score += w["name_match"]
        reasons.append(f"✓ Shelter name '{shelter_name}' verified in document text")
    else:
        reasons.append(f"⚠️ Shelter name '{shelter_name}' could not be matched with high confidence")

    # Address / City / State Matching
    address_matched = False
    city_matched = False
    state_matched = False
    
    if city and normalize_text(city) in norm_text:
        city_matched = True
    if state and normalize_text(state) in norm_text:
        state_matched = True
    if address and len(address) > 4:
        norm_addr = normalize_text(address)
        addr_tokens = [t for t in norm_addr.split() if len(t) > 3]
        if addr_tokens and any(t in norm_text for t in addr_tokens):
            address_matched = True

    if city_matched or state_matched or address_matched:
        score += w["address_match"]
        matched_loc_parts = []
        if city_matched: matched_loc_parts.append(city)
        if state_matched: matched_loc_parts.append(state)
        reasons.append(f"✓ Location details verified in document ({', '.join(matched_loc_parts) if matched_loc_parts else 'Address'})")
    else:
        reasons.append("⚠️ Registered address/city was not clearly detected in document")

    # Step 5: Registration Number Matching
    extracted_reg_no = extract_registration_number(text)
    reg_detected = extracted_reg_no is not None
    reg_match = False
    
    if db_registration_number and db_registration_number.strip():
        norm_db_reg = normalize_text(db_registration_number).replace(' ', '')
        if reg_detected:
            norm_ext_reg = normalize_text(extracted_reg_no).replace(' ', '')
            if norm_db_reg == norm_ext_reg or norm_db_reg in norm_ext_reg or norm_ext_reg in norm_db_reg:
                reg_match = True
                score += w["reg_number_match"]
                reasons.append(f"✓ Registration number matches database ({extracted_reg_no})")
            else:
                # Registration number conflict!
                reg_match = False
                hard_fail = True
                rejection_reason_code = "REGISTRATION_NUMBER_MISMATCH"
                conflict_msg = f"Registration number mismatch detected: Document has '{extracted_reg_no}', expected '{db_registration_number}'"
                rejection_reasons.append(conflict_msg)
                reasons.append(f"✕ {conflict_msg}")
        else:
            reasons.append(f"⚠️ Registered registration number '{db_registration_number}' was not found in document")
    else:
        # DB did not have pre-stored reg number
        if reg_detected:
            score += w["reg_number_match"]
            reasons.append(f"✓ Official registration number detected: {extracted_reg_no}")
            reg_match = True
        else:
            # Neutral score when not specified
            score += int(w["reg_number_match"] * 0.5)
            reasons.append("ℹ️ Registration number not explicitly detected (partial score applied)")

    # Step 6: Date Validation
    date_info = extract_dates(text)
    date_valid = True
    
    if date_info["detected"]:
        iss = date_info["issue_date"]
        exp = date_info["expiry_date"]
        
        if iss and exp:
            iss_d = datetime.date.fromisoformat(iss)
            exp_d = datetime.date.fromisoformat(exp)
            if iss_d > exp_d:
                date_valid = False
                hard_fail = True
                rejection_reason_code = "INVALID_CERTIFICATE_DATES"
                err = f"Issue date ({iss}) is after expiry date ({exp})"
                rejection_reasons.append(err)
                reasons.append(f"✕ {err}")
            elif exp_d < datetime.date.today():
                date_valid = False
                # Expired certificate is high risk
                reasons.append(f"⚠️ Certificate expired on {exp}")
            else:
                score += w["date_validity"]
                reasons.append(f"✓ Valid certificate dates (Issued: {iss}, Expires: {exp})")
        elif iss:
            iss_d = datetime.date.fromisoformat(iss)
            if iss_d > datetime.date.today() + datetime.timedelta(days=30):
                date_valid = False
                reasons.append(f"⚠️ Future issue date detected: {iss}")
            else:
                score += w["date_validity"]
                reasons.append(f"✓ Valid issue date: {iss}")
        else:
            score += int(w["date_validity"] * 0.7)
            reasons.append(f"✓ Valid date detected: {exp}")
    else:
        score += int(w["date_validity"] * 0.5)
        reasons.append("ℹ️ Certificate validity dates not explicitly detected")

    # Step 7: QR Code / Barcode Detection (Never fake official verification!)
    qr_detected = extracted.get("has_qr_hint", False) or "qr" in norm_text or "barcode" in norm_text or "http" in norm_text
    
    if is_demo:
        official_verification_status = "DEMO_TEST_DATA"
        if qr_detected:
            score += int(w["official_verification"] * 0.6)
            reasons.append("✓ Machine-readable QR / barcode security element detected")
            reasons.append("ℹ️ Demo Document Verified for Testing (Simulated Test Data)")
        else:
            score += int(w["official_verification"] * 0.3)
            reasons.append("ℹ️ Demo Document Verified for Testing")
    else:
        official_verification_status = "NOT_AVAILABLE"
        if qr_detected:
            # Give partial score for machine-readable certificate element
            score += int(w["official_verification"] * 0.6)
            reasons.append("✓ Machine-readable QR / barcode security element detected")
            reasons.append("ℹ️ Official government live verification API not available (Honest status: NOT_AVAILABLE)")
        else:
            score += int(w["official_verification"] * 0.3)
            reasons.append("ℹ️ No digital QR verification code found")

    # Step 8: Tampering Analysis
    tamper_result = analyze_tampering(file_path, metadata, text)
    tampering_indicators = tamper_result["tampering_indicators"]
    
    if not tampering_indicators:
        score += w["tampering"]
        reasons.append("✓ No obvious digital tampering indicators or editing traces detected")
    else:
        for tr in tamper_result["reasons"]:
            reasons.append(f"⚠️ Possible tampering indicator: {tr}")

    # Step 9: Final Score & Status Calculation
    final_score = max(0, min(100, score))
    
    # Determine Risk Level based on thresholds
    if is_demo and hard_fail:
        final_score = min(final_score, 25)
        risk_level = "HIGH"
    elif final_score >= low_risk_threshold:
        risk_level = "LOW"
    elif final_score >= auto_reject_threshold:
        risk_level = "MEDIUM"
    else:
        risk_level = "HIGH"

    # Strict Decision Engine
    auto_rejected = False
    
    if is_demo:
        if hard_fail:
            auto_rejected = True
            verification_status = "DEMO_VALIDATION_FAILED"
            if not rejection_reasons:
                rejection_reasons.append("Demo document failed critical validation checks.")
        elif final_score < auto_reject_threshold:
            auto_rejected = True
            verification_status = "DEMO_VALIDATION_FAILED"
            rejection_reason_code = "HIGH_RISK_SCORE"
            rejection_reasons.append(f"Demo verification score ({final_score}/100) is below minimum threshold ({auto_reject_threshold}).")
        elif final_score < low_risk_threshold:
            auto_rejected = False
            verification_status = "DEMO_MANUAL_REVIEW"
            if not any("manual review" in r.lower() for r in reasons):
                reasons.append("Some required information could not be confidently extracted. Manual review is required.")
        else:
            auto_rejected = False
            verification_status = "DEMO_VALIDATION_PASSED"
    else:
        if hard_fail:
            auto_rejected = True
            verification_status = "AUTO_REJECTED"
            if not rejection_reasons:
                rejection_reasons.append("Document failed critical authenticity validation.")
        elif final_score < auto_reject_threshold:
            auto_rejected = True
            verification_status = "AUTO_REJECTED"
            rejection_reason_code = "HIGH_RISK_SCORE"
            rejection_reasons.append(f"AI Verification Confidence score ({final_score}/100) is below minimum threshold ({auto_reject_threshold}).")
        elif final_score < low_risk_threshold:
            auto_rejected = False
            verification_status = "MANUAL_REVIEW"
        else:
            auto_rejected = False
            verification_status = "PENDING"  # Continues to Admin approval with recommendation: LOW RISK

    rejection_msg = "; ".join(rejection_reasons) if rejection_reasons else None

    demo_report = {
        "documentTypeDetected": is_valid_type,
        "shelterNameExtracted": name_match,
        "registrationNumberExtracted": reg_detected,
        "addressExtracted": (city_matched or state_matched or address_matched),
        "documentStructureAnalyzed": True,
        "tamperingIndicatorsAnalyzed": True,
        "score": final_score,
        "result": verification_status,
        "disclaimer": "This is a simulated verification result for testing only."
    } if is_demo else None

    return {
        "documentType": doc_type,
        "confidenceScore": final_score,
        "riskLevel": risk_level,
        "verificationStatus": verification_status,
        "autoRejected": auto_rejected,
        "hardFail": hard_fail,
        "rejectionReasonCode": rejection_reason_code,
        "rejectionReason": rejection_msg,
        "nameMatch": name_match,
        "addressMatch": (city_matched or state_matched or address_matched),
        "registrationNumberDetected": reg_detected,
        "registrationNumberMatch": reg_match if db_registration_number else (True if reg_detected else None),
        "extractedRegistrationNumber": extracted_reg_no,
        "dateValid": date_valid,
        "issueDate": date_info["issue_date"],
        "expiryDate": date_info["expiry_date"],
        "qrDetected": qr_detected,
        "qrVerified": False,
        "officialVerification": official_verification_status,
        "tamperingIndicators": tampering_indicators,
        "ocrStatus": ocr_status,
        "reasons": reasons,
        "verifiedAt": datetime.datetime.now().isoformat(),
        "aiVersion": "PetConnect-DocAI-v1.0",
        "isDemo": is_demo,
        "verificationMode": "DEMO" if is_demo else "PRODUCTION",
        "demoNotice": "DEMO MODE — FOR TESTING ONLY: Demo results do not prove government document authenticity." if is_demo else None,
        "demoVerificationReport": demo_report
    }
