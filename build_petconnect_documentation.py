# build_petconnect_documentation.py
# Generates PetConnect_Project_Documentation.docx and PetConnect_Project_Documentation.pdf

import os
import sys
import shutil
import zipfile
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

def set_p(p, text, font_size=12, bold=False, italic=False, align=WD_ALIGN_PARAGRAPH.JUSTIFY, color=None):
    p.text = ""
    p.alignment = align
    run = p.add_run(text)
    set_run_font(run, font_name="Times New Roman", size_pt=font_size, bold=bold, italic=italic, color=color)
    return run

def set_heading(p, text, font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT):
    p.text = ""
    p.alignment = align
    run = p.add_run(text)
    set_run_font(run, font_name="Times New Roman", size_pt=font_size, bold=bold, italic=False, color=RGBColor(0, 0, 0))
    return run

def set_cell(cell, text, font_size=10, bold=False, align=WD_ALIGN_PARAGRAPH.LEFT):
    cell.text = ""
    p = cell.paragraphs[0]
    p.alignment = align
    run = p.add_run(text)
    set_run_font(run, "Times New Roman", size_pt=font_size, bold=bold, italic=False)

def build_document():
    print("Loading source document:", SOURCE_DOCX)
    doc = docx.Document(SOURCE_DOCX)
    
    # -------------------------------------------------------------
    # 1. FRONT MATTER (Cover, Certificate, Declaration, Acknowledgement)
    # -------------------------------------------------------------
    set_p(doc.paragraphs[0], "Pet Connect Management System", font_size=20, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[1], "A Centralized Pet Adoption and Temporary Pet Care Platform\n\nAPI & Microservices", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[2], "A Cornerstone Project Report submitted in partial fulfillment of the requirements for the award of degree of", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[3], "BACHELOR OF TECHNOLOGY", font_size=14, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[4], "in", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[5], "ARTIFICIAL INTELLIGENCE AND MACHINE LEARNING", font_size=14, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[7], "Submitted by", font_size=12, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    set_p(doc.paragraphs[8], "Manindra Babu Chappidi   (24B11AI060)", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[9], "Kowsalya Nadisetti            (24B11AI321)", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[10], "Nagendra Pilla                    (24B11AI344)", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[11], "Sreenija Teeparthi               (24B11AI417)", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    set_p(doc.paragraphs[13], "Under the Guidance of", font_size=12, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[14], "Mrs. Khasimbee Shaik, M. Tech, (Ph.D)", font_size=12, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[15], "Assistant Professor, AIML", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    # paragraph 16 is logo image run - preserve untouched!
    set_p(doc.paragraphs[17], "DEPARTMENT OF ARTIFICIAL INTELLIGENCE AND MACHINE LEARNING", font_size=12, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[18], "ADITYA UNIVERSITY", font_size=12, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[19], "2024-2028", font_size=12, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    # Certificate page
    set_p(doc.paragraphs[21], "School of Computing", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[22], "Department of Artificial Intelligence and Machine Learning", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[24], "CERTIFICATE", font_size=14, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER, color=RGBColor(192, 0, 0))
    set_p(doc.paragraphs[25], 'This is to certify that the project work entitled "Pet Connect Management System" is submitted by', font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_p(doc.paragraphs[26], "Manindra Babu Chappidi   (24B11AI060)", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[27], "Kowsalya Nadisetti            (24B11AI321)", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[28], "Nagendra Pilla                    (24B11AI344)", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[29], "Sreenija Teeparthi               (24B11AI417)", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[30], "in partial fulfilment of the requirements for the award of the B.Tech degree in Artificial Intelligence and Machine Learning for the academic year 2026-2027.", font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[37], "External Examiner", font_size=12, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    # Declaration
    set_heading(doc.paragraphs[39], "DECLARATION", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[40], 'We hereby declare that the project entitled "Pet Connect Management System" is original and is submitted to ADITYA UNIVERSITY, Surampalem in partial fulfilment of the requirements for the award of the B.Tech degree in Artificial Intelligence and Machine Learning. We further declare that this project work has not been submitted, either in full or in part, for the award of any degree in this or any other institution. We also confirm that this work complies with the Institutional Academic Integrity and AI Usage Policy.', font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[41], "Place: Surampalem", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[42], "Date: 14/10/2026", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[43], "By", font_size=12, align=WD_ALIGN_PARAGRAPH.RIGHT)
    set_p(doc.paragraphs[44], "Manindra Babu Chappidi (24B11AI060)", font_size=12, align=WD_ALIGN_PARAGRAPH.RIGHT)
    set_p(doc.paragraphs[45], "Kowsalya Nadisetti     (24B11AI321)", font_size=12, align=WD_ALIGN_PARAGRAPH.RIGHT)
    set_p(doc.paragraphs[46], "Nagendra Pilla         (24B11AI344)", font_size=12, align=WD_ALIGN_PARAGRAPH.RIGHT)
    set_p(doc.paragraphs[47], "Sreenija Teeparthi     (24B11AI417)", font_size=12, align=WD_ALIGN_PARAGRAPH.RIGHT)
    
    # Acknowledgement
    set_heading(doc.paragraphs[48], "ACKNOWLEDGEMENT", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[49], "It is with immense pleasure that we would like to express our indebted gratitude to our project guide Mrs. Khasimbee Shaik, Department of Artificial Intelligence and Machine Learning, who has guided us a lot and encouraged us in every step of the project work. Her valuable moral support and guidance throughout the project helped us to a greater extent.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[50], "We are grateful to Dr. B. Manikyala Rao (Dy.HOD), for inspiring us all the way and for arranging all the facilities and resources needed for our project.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[51], "We are grateful to Dr. K. Naga Bhargavi (HOD), for inspiring us all the way and for arranging all the facilities and resources needed for our project.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[52], "We wish to thank our Dr. M.V. Rajesh, Associate Dean (School of Computing), Dr. A. Ramesh, Pro Vice-Chancellor (Engineering & Sciences), Dr. S. Rama Sree, Pro Vice-Chancellor (Academics) and Dr. G. Suresh, Registrar, for their encouragement and support during the course of our project.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[53], "We would like to extend our sincere thanks to Dr. M. B. Srinivas, Vice-Chancellor, Dr. M. Sreenivasa Reddy, Deputy Pro-Chancellor and Management, Aditya University for their unconditional support in providing us with the best infrastructural facilities and state-of-the-art laboratories during our project.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[54], "Not to forget, Non-Teaching Staff and our Friends who have directly or indirectly supported us in completing this project on time.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    # Abstract
    set_heading(doc.paragraphs[55], "ABSTRACT", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_p(doc.paragraphs[56], "PetConnect is a centralized pet management platform that connects pet adopters, shelter owners, and administrators through a single web application. Instead of relying on separate platforms for pet adoption, shelter management, and temporary pet care, PetConnect brings these services together. Users can browse available pets, save favorites, submit adoption applications, track application status, and book temporary pet care services. Shelter owners can register shelters, upload verification documents, manage pet listings, review adoption requests, and handle care bookings. Administrators manage users, verify shelters and pet listings, approve registrations, and monitor platform activities.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[57], "The application is engineered using Java 17 and Spring Boot 3.3.3 as the robust enterprise backend framework, paired with Thymeleaf and responsive Bootstrap 5 for dynamic, role-based user interfaces. MySQL is used as the relational database to store user credentials, verified shelter details, pet records, adoption requests, visit slots, and temporary care reservations. The platform integrates an AI-assisted shelter document verification microservice running on Python FastAPI with Optical Character Recognition (OCR), evaluating government registration certificates, comparing text against submitted information, and generating risk-adjusted confidence scores. PetMatch AI provides personalized pet recommendations based on user living conditions and lifestyle compatibility, while video consultations enable pre-adoption screenings.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[58], "To guarantee operational trust and transactional reliability, the system incorporates role-based access control (RBAC), database concurrency control with optimistic locking (@Version), Spring AOP logging, and unified global exception handling. Automated AI document evaluation assists administrators in identifying forged or inconsistent registrations while queuing ambiguous filings for manual audit. PetConnect transforms the fragmented animal welfare landscape into an organized, accountable, and transparent ecosystem. Future enhancements include dedicated mobile clients, automated payment gateway integration, and smart collar IoT tracking.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[59], "Keywords: Pet Management System, Pet Adoption, Shelter Management, Temporary Pet Care, Artificial Intelligence, OCR, Document Verification, PetMatch AI, Spring Boot 3, Thymeleaf, MySQL, REST APIs.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    # Table of Contents & List of Figures headings
    set_heading(doc.paragraphs[61], "TABLE OF CONTENTS", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[62], "LIST OF FIGURES", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    # -------------------------------------------------------------
    # CHAPTER 1: INTRODUCTION
    # -------------------------------------------------------------
    set_heading(doc.paragraphs[64], "CHAPTER 1", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[65], "INTRODUCTION", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[66], "1.1 Introduction", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[67], "PetConnect is a web-based Pet Management System designed to simplify pet adoption, shelter management, and temporary pet care. In many communities, pet adoption and rescue operations rely on fragmented channels such as unverified social media groups, paper flyers, and isolated shelter websites. PetConnect addresses this issue by providing a centralized platform that connects pet adopters, shelter owners, and administrators in a single, verified digital environment.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[68], "The platform provides three distinct role-based experiences: the User/Adopter Panel, Shelter Owner Panel, and Administrator Panel. Prospective pet adopters can discover rescued animals, inspect medical histories, utilize AI matching, schedule visits, and submit adoption or temporary care applications. Shelter owners can manage pet profiles, review applications, and coordinate visit schedules. Administrators oversee community trust, audit shelter licenses, and monitor platform activities.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[69], "A standout innovation in PetConnect is the integration of an AI-assisted shelter verification microservice. By utilizing Optical Character Recognition (OCR), the system inspects uploaded shelter registration certificates, extracts licensing details, and checks for data consistency. This reduces manual workload for administrators and prevents fraudulent rescue organizations from exploiting adopters.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[70], "The architecture combines a robust Spring Boot 3.3.3 enterprise backend with Thymeleaf and responsive Bootstrap 5 on the presentation layer, backed by a MySQL relational database. The system follows clean layered software engineering principles, incorporating Spring Security, Spring AOP execution logging, Jakarta Bean Validation, and declarative transaction management.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[72], "1.2 Problem Statement", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[73], "Pet adopters face substantial hurdles when navigating fragmented and uncoordinated rescue operations. Prospective adopters must search across multiple disconnected websites, social media pages, and local bulletin boards, making it difficult to find verified shelters or available pets that match their household circumstances.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[74], "Fraudulent and unregistered shelters present serious animal welfare and legal risks. Shelter registration documents and pet listings often lack standardized vetting, leading to illegal breeding operations, inaccurate medical histories, and compromised animal health.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[75], "Traditional paper-based or informal adoption workflows are cumbersome and opaque. Adopters receive little to no feedback on application status, while shelters struggle to track multiple inquiries, coordinate in-person visit appointments, and avoid double bookings.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[76], "Temporary pet care and boarding options are often unaffordable or unavailable. Pet owners traveling for business, facing medical emergencies, or undergoing home renovations frequently lack reliable, verified boarding facilities, resulting in pet abandonment.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[77], "Shelter operators lack unified digital management tools. Coordinating medical records, adoption inquiries, boarding capacity, and visit schedules using spreadsheets and phone calls creates operational bottlenecks and increases human error.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[78], "Platform administrators lack centralized oversight mechanisms to audit incoming shelter registrations, inspect uploaded government certificates, monitor adoption histories, and ensure regulatory compliance.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[79], "Without intelligent recommendation systems, pet compatibility mismatches often lead to adoption failures, animal distress, and pet returns. A centralized, intelligent platform is essential to address these multifaceted challenges.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[81], "1.3 Objectives of the Project", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    objectives = [
        "To develop a centralized Pet Management System connecting adopters, shelter owners, and administrators on a secure platform.",
        "To enable adopters to browse, search, and filter rescued pets, save favorites, submit adoption applications, and track progress.",
        "To allow shelter owners to register shelters, upload legal documents, manage pet listings, and process care bookings.",
        "To implement AI-assisted shelter document verification using OCR, entity extraction, and confidence scoring.",
        "To provide personalized pet recommendations through PetMatch AI based on adopter lifestyle and home environment.",
        "To facilitate temporary pet care by supporting short-term boarding reservations, capacity limits, and emergency medical notes.",
        "To provide administrators with centralized oversight tools for user auditing, shelter licensing verification, and reporting.",
        "To support video consultation workflows enabling adopters and shelter owners to communicate effectively before physical visits.",
        "To enforce database concurrency control, optimistic locking, and transactional data integrity across all adoption and care bookings."
    ]
    for idx, obj in enumerate(objectives):
        p = doc.paragraphs[82 + idx]
        p.style = doc.styles['List Paragraph']
        set_p(p, obj, font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
        
    set_heading(doc.paragraphs[91], "1.4 Scope of the Project", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[92], "The scope of PetConnect encompasses user authentication, shelter registration and document vetting, pet inventory management, adoption application workflows, visit appointment scheduling, temporary pet boarding, and post-adoption shelter reviews.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[93], "The technical scope integrates a Spring Boot 3.3.3 web application with a Python FastAPI AI microservice for OCR document processing, relational data persistence in MySQL 8.x, role-based access control via Spring Security, and Spring AOP logging.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[94], "Future expansions outside the current scope include native mobile applications for iOS and Android, online payment gateway integrations for boarding fees, and IoT smart collar tracking devices.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    # -------------------------------------------------------------
    # CHAPTER 2: SYSTEM OVERVIEW
    # -------------------------------------------------------------
    set_heading(doc.paragraphs[96], "CHAPTER 2", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[97], "SYSTEM OVERVIEW", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[98], "2.1 System Architecture", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[99], "The PetConnect system follows a robust multi-tiered enterprise architecture consisting of presentation, application, business logic, persistence, and external AI microservice layers. This decoupled structure ensures modularity, maintainability, and scalability.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[100], "The backend is implemented using Java 17 and Spring Boot 3.3.3. Incoming HTTP web and REST requests are processed by Spring MVC controllers, validated using Jakarta Bean Validation, and passed to service components. Declarative transactions (@Transactional) and Spring AOP logging surround the business logic.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[101], "MySQL 8.x serves as the relational database, communicating with Spring Data JPA and Hibernate ORM. Dedicated JPA repositories handle data persistence and maintain referential integrity across all entities. A secondary Python FastAPI microservice handles OCR document verification via asynchronous HTTP calls.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[103], "Figure 2.1: System Architecture", font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    set_heading(doc.paragraphs[104], "2.2 System Flow", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    flow_steps = [
        "A user registers or logs in, accessing the Adopter Dashboard to discover rescued pets and explore verified shelter organizations.",
        "The adopter searches pets by species, breed, age, and location, views medical details, and leverages PetMatch AI for compatibility scoring.",
        "The user submits a formal adoption application or reserves a temporary care boarding slot for an existing pet.",
        "A shelter owner registers their organization, providing address details, operating hours, capacity, and government registration documents.",
        "The administrator reviews shelter applications in the Admin Console, triggering AI OCR document inspection before granting verified status.",
        "The shelter owner lists rescued animals, updates medical records, reviews adoption applications, and coordinates visit appointments.",
        "The administrator monitors platform activity, reviews user feedback, and enforces compliance across registered shelters.",
        "The adopter tracks adoption status in real-time, receiving notifications upon status transitions (Submitted, Under Review, Approved, Rejected).",
        "Adopters and shelter owners participate in scheduled video consultations to review home readiness before final adoption.",
        "Following successful adoption, adopters post ratings and reviews to foster transparency and recognize outstanding shelter care."
    ]
    for idx, step in enumerate(flow_steps):
        p = doc.paragraphs[105 + idx]
        set_p(p, step, font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
        
    set_heading(doc.paragraphs[115], "2.3 User Roles", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[116], "Table 2.1 outlines the three primary user roles in PetConnect: User/Pet Adopter, Shelter Owner, and Administrator. Each role possesses specialized privileges enforced by Spring Security role-based access control.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[117], "Table 2.1: User Roles", font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    t_roles = doc.tables[3]
    for r in t_roles.rows:
        for c in r.cells:
            for p in c.paragraphs:
                for run in p.runs:
                    set_run_font(run, "Times New Roman", size_pt=10, bold=run.bold)
                    
    set_heading(doc.paragraphs[119], "2.4 System Modules", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    modules = [
        "Shelter Registration & Verification - Onboarding workflow, license document uploads, and administrative verification.",
        "User & Adopter Profile Management - Account profiles, pet preferences, and application history tracking.",
        "Pet Catalog & Filtering - Searchable inventory of rescued pets filtered by species, breed, age, size, gender, and location.",
        "Adoption Application Workflow - Multi-step screening forms, status updates (Submitted, Under Review, Approved, Rejected).",
        "Temporary Pet Care & Boarding - Booking short-term shelter boarding with check-in/out dates and medical notes.",
        "AI-Assisted Document Verification - Python FastAPI microservice with OCR to extract and validate shelter licensing.",
        "PetMatch AI Recommendation Engine - Intelligent matching evaluating lifestyle traits and pet compatibility.",
        "Shelter Visit Scheduling - Calendar slot reservation for in-person meetings with conflict prevention.",
        "Shelter Reviews & Feedback - Post-adoption feedback and star ratings promoting shelter accountability."
    ]
    for idx, mod in enumerate(modules):
        p = doc.paragraphs[120 + idx]
        set_p(p, mod, font_size=12, bold=False, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
        
    # -------------------------------------------------------------
    # CHAPTER 3: TECHNOLOGIES USED
    # -------------------------------------------------------------
    set_heading(doc.paragraphs[129], "CHAPTER 3", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[130], "TECHNOLOGIES USED", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[131], "3.1 Java", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[132], "Java 17 Long-Term Support (LTS) serves as the primary programming language for the PetConnect backend. Java provides strong static typing, robust memory management, modern record classes, pattern matching, and high performance. Its mature exception handling and extensive ecosystem make it an ideal foundation for enterprise web services handling mission-critical adoption and shelter operations.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[133], "3.2 Spring Boot", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[134], "Spring Boot 3.3.3 is the overarching backend framework. It streamlines application bootstrap through opinionated auto-configuration, an embedded Apache Tomcat web server, comprehensive dependency injection, and declarative transaction management. Spring Boot simplifies multi-layered architecture by decoupling controllers, business services, and persistence repositories.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[135], "Table 3.1: Backend Dependencies (pom.xml)", font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    t_deps = doc.tables[4]
    deps_data = [
        ("Starter / Library", "Purpose"),
        ("spring-boot-starter-web", "Spring MVC REST controllers, embedded Tomcat container, JSON message conversion via Jackson"),
        ("spring-boot-starter-security", "Authentication, authorization, role-based access control, BCrypt password hashing"),
        ("spring-boot-starter-thymeleaf", "Server-side HTML5 template engine rendering dynamic user, shelter, and admin interfaces"),
        ("thymeleaf-extras-springsecurity6", "Thymeleaf dialect enabling role-based conditional UI rendering (sec:authorize)"),
        ("spring-boot-starter-data-jpa", "Spring Data JPA and Hibernate ORM for entity-relational mapping and database persistence"),
        ("mysql-connector-j", "High-performance JDBC driver for relational communication with MySQL 8.x database server"),
        ("spring-boot-starter-validation", "Jakarta Bean Validation (@NotNull, @NotBlank, @Size, @Email) on DTOs and models"),
        ("spring-boot-starter-mail", "JavaMailSender service for automated email notifications (adoption status, booking confirmations)")
    ]
    for row_idx, (col0, col1) in enumerate(deps_data):
        if row_idx < len(t_deps.rows):
            set_cell(t_deps.rows[row_idx].cells[0], col0, font_size=10, bold=(row_idx==0))
            set_cell(t_deps.rows[row_idx].cells[1], col1, font_size=10, bold=(row_idx==0))
            
    set_heading(doc.paragraphs[137], "3.3 Frontend Technologies", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[138], "The PetConnect presentation layer is built using Thymeleaf 3, HTML5, CSS3, JavaScript, and Bootstrap 5. Thymeleaf seamlessly integrates with Spring Boot, rendering dynamic server-side templates with natural templating capabilities. Role-based view logic is managed cleanly via the Thymeleaf Spring Security 6 dialect, dynamically toggling navigation items and administrative controls.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[139], "Bootstrap 5 and custom CSS styles deliver a responsive, accessible interface optimized across desktops, tablets, and smartphones. Client-side JavaScript enhances user experience through asynchronous AJAX requests, real-time input validation, interactive image previews, and modal confirmation dialogs.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[140], "3.4 RESTful Web Services", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[141], "RESTful web services connect frontend asynchronous components and external microservices with the Spring Boot backend. Standard HTTP methods (GET, POST, PUT, DELETE) are mapped cleanly to domain entities. Endpoints produce and consume JSON payloads with Jackson serialization, adhering to REST architectural constraints including statelessness and uniform resource identifiers.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[142], "3.5 Spring Data JPA", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[143], "Spring Data JPA, powered by Hibernate ORM, handles relational persistence. Domain models are annotated with standard Jakarta Persistence annotations (@Entity, @Table, @Id, @ManyToOne, @OneToMany, @Version). Data access repositories extend JpaRepository, automatically inheriting CRUD methods, pagination, sorting, and dynamic derived query execution without boilerplate SQL.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[144], "3.6 MySQL Database", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[145], "MySQL 8.x serves as the enterprise relational database management system. MySQL provides ACID transactional compliance, foreign key constraints, robust indexing on frequently queried columns (such as pet breed, city, and status), and native support for UTF-8 character encoding. Schema evolution is managed safely to ensure referential integrity across all pet rescue and booking data.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[146], "3.7 Validation and Exception Handling", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[147], "Data integrity is enforced at the controller boundary using Jakarta Bean Validation annotations (@NotBlank, @NotNull, @Size, @Email, @Min). Invalid inputs trigger MethodArgumentNotValidException. A centralized GlobalExceptionHandler annotated with @ControllerAdvice intercepts all exceptions, transforming them into uniform JSON responses or intuitive user-facing error views.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[148], "3.8 Spring AOP", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[149], "Spring Aspect-Oriented Programming (AOP) encapsulates cross-cutting concerns, keeping business logic clean and decoupled. A dedicated LoggingAspect utilizes @Around advice to intercept service and controller methods, capturing execution duration, method signatures, parameter values, and error traces. This non-invasive mechanism provides end-to-end auditability and performance telemetry.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[150], "3.9 Python FastAPI AI Microservice", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[151], "An intelligent Python FastAPI microservice is deployed alongside the Spring Boot backend to handle AI-assisted document verification and PetMatch recommendations. Running on Uvicorn, the service accepts uploaded shelter certificates, performs Optical Character Recognition (OCR) using Tesseract/EasyOCR, extracts registration numbers, and calculates verification confidence scores and risk indicators.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[152], "3.10 Security Headers and Spring Security", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[153], "Spring Security 6 enforces robust authentication and authorization. Passwords are encrypted using BCrypt adaptive hashing. Role-based access rules restrict administrative endpoints (/admin/**) and shelter portals (/shelter/**). HTTP response headers (X-Frame-Options: DENY, X-Content-Type-Options: nosniff, and strict CORS configuration) protect the platform against clickjacking and cross-site attacks.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[154], "3.11 Maven and Testing Tools", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[155], "Apache Maven coordinates dependency management, compilation, resource filtering, and packaging. The Maven wrapper (mvnw and mvnw.cmd) guarantees reproducible builds across development and deployment environments. Automated testing is powered by JUnit 5, Mockito for service-level mocking, and Spring Security Test utilities for verifying role-based endpoints.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    # -------------------------------------------------------------
    # CHAPTER 4: DATABASE DESIGN
    # -------------------------------------------------------------
    set_heading(doc.paragraphs[156], "CHAPTER 4", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[157], "DATABASE DESIGN", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[158], "4.1 Database Tables Overview", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[159], "The PetConnect database is organized around eight core relational tables: SHELTERS, USERS, PETS, ADOPTION_REQUESTS, CARE_BOOKINGS, SHELTER_DOCUMENTS, SHELTER_VISIT_SLOTS, and SHELTER_REVIEWS. Each table is normalized to eliminate data redundancy while enforcing referential integrity through foreign keys and unique constraints across all pet rescue and boarding workflows.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[161], "Figure 4.1: Database Design", font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    set_heading(doc.paragraphs[162], "4.2 Shelters Table", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[163], "The SHELTERS table serves as the primary registry of animal rescue shelters and care centers that all other operational tables refer to.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[164], "id - Primary Key (BIGINT, Auto-Increment)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[165], "name (required, VARCHAR(150)) - Official registered name of the shelter", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[166], "registration_number (required, unique) - Government or municipal license registration ID", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[167], "city, state, address, area, location (required) - Physical geographic coordinates and address details", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[168], "verification_status (default PENDING; transitions to VERIFIED or REJECTED), capacity, contact_email, phone", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[169], "4.3 Users Table", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[170], "The USERS table stores credentials, personal profiles, and authority roles for pet adopters, shelter managers, and platform administrators.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[171], "id - Primary Key (BIGINT, Auto-Increment)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[172], "full_name (required) and email (required, unique across the platform)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[173], "password (required, BCrypt encrypted string), phone, city, address, role (USER, SHELTER_OWNER, ADMIN), created_at", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[174], "4.4 Pets Table", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[175], "The PETS table maintains profiles of rescued animals listed for adoption or temporary care.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[176], "id - Primary Key (BIGINT, Auto-Increment)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[177], "shelter_id - Foreign Key (SHELTERS), identifying the shelter responsible for the pet", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[178], "name (required), type (required, e.g., DOG, CAT), breed (required), age (0 or more), gender, size, color", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[179], "description, health_info, vaccination_status, photo_url, status (default AVAILABLE; can be PENDING_ADOPTION or ADOPTED)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[180], "4.5 Adoption Requests Table", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[181], "The ADOPTION_REQUESTS table records formal adoption applications submitted by prospective adopters.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[182], "id - Primary Key (BIGINT, Auto-Increment)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[183], "user_id, pet_id, and shelter_id - Foreign Keys (all required)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[184], "readiness_q1 to q4 (required) - Responses assessing home environment, prior pet experience, and daily commitment", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[185], "status (default SUBMITTED; updated to UNDER_REVIEW, APPROVED, REJECTED, or CANCELLED), applied_date, decision_date", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[186], "4.6 Care Bookings Table", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[187], "The CARE_BOOKINGS table manages temporary boarding reservations for pet owners needing short-term accommodation.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[188], "id - Primary Key (BIGINT, Auto-Increment)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[189], "user_id, pet_id, and shelter_id - Foreign Keys (required)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[190], "start_date and end_date (required, valid date range in the future)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[191], "special_instructions, emergency_contact, vaccination_info, medical_information", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[192], "status (REQUESTED, CONFIRMED, COMPLETED, CANCELLED), version - optimistic lock (@Version) preventing overbooking", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[193], "4.7 Shelter Documents Table", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[194], "The SHELTER_DOCUMENTS table stores uploaded certificates, legal filings, and AI OCR verification metadata.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[195], "id - Primary Key (BIGINT, Auto-Increment)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[196], "shelter_id - Foreign Key (SHELTERS)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[197], "document_type (REGISTRATION_CERTIFICATE, ID_PROOF, ADDRESS_PROOF, OTHER), document_url, original_filename", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[198], "verification_confidence_score (AI confidence score from 0 to 100), risk_level (LOW, MEDIUM, HIGH)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[199], "name_match, address_match, extracted_text, verification_status (PENDING, APPROVED, REJECTED)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[200], "4.8 Shelter Visit Slots Table", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[201], "The SHELTER_VISIT_SLOTS table controls calendar appointments for in-person adopter visits.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[202], "id - Primary Key (BIGINT, Auto-Increment)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[203], "shelter_id - Foreign Key (SHELTERS)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[204], "pet_id and application_id - Foreign Keys (optional, linked when meeting a specific pet)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[205], "booked_by_user_id - Foreign Key (USERS)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[206], "visit_date (required) and time_slot (required, e.g., 10:00-11:00 AM), status (AVAILABLE, BOOKED, CONFIRMED, COMPLETED)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[207], "unique constraint on (shelter_id, visit_date, time_slot), preventing second bookings of the same slot", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[208], "4.9 Shelter Reviews Table", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[209], "The SHELTER_REVIEWS table maintains ratings and feedback from verified adopters.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[210], "id - Primary Key (BIGINT, Auto-Increment)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[211], "user_id and shelter_id - Foreign Keys (required)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[212], "pet_id and adoption_application_id - Foreign Keys (optional)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[213], "rating (required, integer 1 to 5 stars) and comment (required text feedback)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[214], "created_at (defaults to current timestamp)", font_size=12, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[215], "4.10 Database Relationships and Concurrency Control", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[216], "A shelter operates as a parent entity owning multiple pets, uploaded verification documents, visit slots, care bookings, and reviews. A user maintains multiple adoption requests, care reservations, visit bookings, and reviews. A pet belongs to one shelter and links to multiple adoption applications and care bookings. An adoption application establishes a three-way relationship between an adopter (user), a pet, and a shelter. Foreign key constraints enforce referential integrity across all associations.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[217], "Concurrency is handled robustly within the database layer. The unique constraint on (shelter_id, visit_date, time_slot) eliminates double-booking of visit appointments. For temporary pet care, optimistic locking with the @Version column on the CARE_BOOKINGS table guarantees that simultaneous reservation attempts on the last available capacity unit conclude with one successful booking and one controlled conflict (409 Conflict), preventing shelter overbooking.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    # -------------------------------------------------------------
    # CHAPTER 5: SYSTEM MODULES
    # -------------------------------------------------------------
    set_heading(doc.paragraphs[218], "CHAPTER 5", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[219], "SYSTEM MODULES", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    set_heading(doc.paragraphs[220], "5.1 Shelter Registration and Onboarding", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[221], "The Shelter Registration module manages the initial onboarding of animal rescue centers. Shelter owners provide institutional credentials, address coordinates, operating capacity, and upload legal documentation. The shelter record is created with a PENDING verification status until audited by administrators.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[222], "5.2 User and Adopter Profile Management", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[223], "The User Management module handles account registration, authentication, and profile maintenance. Adopters can record housing characteristics, yard availability, family composition, and pet preferences, which PetMatch AI uses for personalized pet recommendations.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[224], "5.3 Pet Catalog and Search Filtering", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[225], "The Pet Catalog module allows adopters to explore rescued pets through multi-faceted search filters including species (dog, cat), breed, age range, gender, size, and geographic proximity. Each listing presents high-resolution photos, personality traits, and vaccination history.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[226], "5.4 Adoption Application Workflow", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[227], "The Adoption Workflow module guides prospective adopters through a structured questionnaire assessing pet readiness. Submitted applications enter an administrative queue for shelter review. Shelter managers can accept, request clarification, or reject applications with formal feedback.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[228], "5.5 Temporary Pet Care and Boarding", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[229], "The Temporary Pet Care module enables pet owners to book short-term shelter boarding during travel or home emergencies. Owners specify check-in/out dates, medical regimens, dietary restrictions, and emergency contacts. Optimistic locking prevents exceeding shelter capacity limits.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[230], "5.6 AI-Assisted Document Verification", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[231], "The Document Verification module integrates with the Python FastAPI microservice. Using Optical Character Recognition (OCR), the system extracts legal entity names and registration numbers from uploaded licenses, compares them against applicant records, and outputs a confidence score and risk level for admin review.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[232], "5.7 PetMatch AI Recommendation Engine", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[233], "PetMatch AI calculates compatibility scores between adopter lifestyles and pet behavioral profiles. Factors such as living space, activity levels, presence of young children, and daily time away from home are processed to recommend animals most likely to thrive in the adopter's home.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[234], "5.8 Shelter Visit Scheduling", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[235], "The Visit Scheduling module coordinates in-person appointments for adopters to meet animals prior to adoption. Shelters publish available time slots on an interactive calendar. Unique constraints at the database level ensure no slot can be double-booked.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[236], "5.9 Shelter Ratings and Reviews", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[237], "The Review module allows verified adopters to submit feedback and numerical star ratings (1 to 5) regarding their adoption experience and shelter facility cleanliness. Published reviews assist the community and promote accountable rescue practices.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[238], "5.10 Audit Logging, Error Handling and Email Alerts", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[239], "The Audit and Alert module leverages Spring AOP to record execution metrics and user actions across services. GlobalExceptionHandler delivers sanitized, uniform error feedback, while the JavaMailSender service delivers instant email notifications upon adoption approvals and booking confirmations.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    # -------------------------------------------------------------
    # CHAPTER 6: API AND IMPLEMENTATION
    # -------------------------------------------------------------
    set_heading(doc.paragraphs[240], "CHAPTER 6", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[241], "API AND IMPLEMENTATION", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[242], "6.1 Frontend Implementation", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[243], "The frontend presentation layer is structured into three distinct user spaces: the Public/Adopter Portal, the Shelter Management Dashboard, and the Central Administrator Console. Pages are delivered via Thymeleaf templates enriched with Bootstrap 5 components and modern vanilla JavaScript.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[244], "Authentication state is managed through secure HTTP-only cookies and Spring Security session contexts. Asynchronous operations, including document upload progress, pet catalog filtering, and visit slot reservations, communicate with REST endpoints using the standard browser fetch API.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[245], "6.2 Backend Implementation", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[246], "The backend codebase is organized under the root package com.petconnect, following a strict layered architecture that decouples web routing, business services, and data persistence.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[247], "Table 6.1: Backend Package Structure", font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    t_pkgs = doc.tables[5]
    pkgs_data = [
        ("Package", "Contents"),
        ("controller", "PublicController, UserController, ShelterOwnerController, AdminController, AdoptionController, PetMatchController, VideoCallController, ShelterReviewController"),
        ("service", "Core business interfaces and transactional implementations (UserService, ShelterService, PetService, AdoptionService, CareBookingService, PetMatchService, EmailNotificationService)"),
        ("repository", "Spring Data JPA repositories extending JpaRepository, one per entity with custom derived and JPQL query methods"),
        ("entity", "JPA entity models mapping relational tables (User, Shelter, Pet, AdoptionApplication, CareBooking, ShelterDocument, ShelterVisitSlot, ShelterReview)"),
        ("dto", "Data transfer objects for registration, adoption applications, care reservations, and verification payloads"),
        ("exception", "GlobalExceptionHandler, ResourceNotFoundException, DoubleBookingException, ShelterCapacityExceededException, UnauthorizedException"),
        ("aop", "LoggingAspect for execution tracking and performance auditing across controller and service layers"),
        ("security", "SecurityConfig, CustomUserDetailsService, and authentication handlers enforcing role-based access control"),
        ("config", "WebMvcConfig, MailConfig, FileStorageProperties, and initial database bootstrap loaders")
    ]
    for row_idx, (col0, col1) in enumerate(pkgs_data):
        if row_idx < len(t_pkgs.rows):
            set_cell(t_pkgs.rows[row_idx].cells[0], col0, font_size=10, bold=(row_idx==0))
            set_cell(t_pkgs.rows[row_idx].cells[1], col1, font_size=10, bold=(row_idx==0))
            
    set_heading(doc.paragraphs[249], "6.3 REST API Operations", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[250], "Table 6.2 lists the core REST and web controller operations of PetConnect. The base URL is http://localhost:8080/api.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[251], "Table 6.2: REST API Operations", font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    t_ops = doc.tables[6]
    ops_data = [
        ("Method", "Path", "Purpose"),
        ("GET", "/pets?type=&breed=&city=", "Search and filter available rescued pets"),
        ("GET", "/pets/{id}", "View detailed pet profile, health history, and photos"),
        ("POST", "/adoptions/apply", "Submit an adoption application for a selected pet"),
        ("GET", "/user/adoptions", "List all adoption applications submitted by the current user"),
        ("PUT", "/shelter/adoptions/{id}/status", "Update adoption application status (APPROVED / REJECTED)"),
        ("POST", "/care/book", "Submit a temporary pet care / boarding reservation"),
        ("GET", "/user/care-bookings", "List temporary care bookings for the current user"),
        ("PUT", "/shelter/care-bookings/{id}/status", "Confirm or decline a care booking (409 on capacity full)"),
        ("GET", "/shelters", "List verified animal shelters with search and pagination"),
        ("GET", "/shelters/{id}", "Retrieve shelter details, capacity, and listed pets"),
        ("POST", "/shelter/register", "Register a shelter and upload legal verification documents"),
        ("GET", "/shelter/dashboard", "Retrieve shelter metrics, active listings, and bookings"),
        ("POST", "/shelter/pets", "Add a rescued pet to the shelter inventory"),
        ("PUT / DELETE", "/shelter/pets/{id}", "Update or remove a pet profile from the catalog"),
        ("POST", "/shelter/slots", "Create available in-person visit slots on the shelter calendar"),
        ("POST", "/shelter/slots/book", "Book an in-person visit appointment (409 on double booking)"),
        ("GET", "/admin/dashboard", "Platform-wide administrative metrics and pending verifications"),
        ("POST", "/admin/shelters/{id}/verify", "Approve or reject a shelter registration with formal reason"),
        ("POST", "/api/ai/verify-document", "FastAPI microservice endpoint for OCR document verification"),
        ("POST", "/api/petmatch/recommend", "Generate compatibility-scored pet recommendations"),
        ("POST", "/reviews", "Submit a review and 1-5 star rating for an adopted pet's shelter"),
        ("GET", "/notifications", "Retrieve user or shelter notifications and alerts")
    ]
    for row_idx, (col0, col1, col2) in enumerate(ops_data):
        if row_idx < len(t_ops.rows):
            set_cell(t_ops.rows[row_idx].cells[0], col0, font_size=10, bold=(row_idx==0))
            set_cell(t_ops.rows[row_idx].cells[1], col1, font_size=10, bold=(row_idx==0))
            set_cell(t_ops.rows[row_idx].cells[2], col2, font_size=10, bold=(row_idx==0))
            
    set_p(doc.paragraphs[253], "A sample curl request to submit an adoption application is shown below:", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[254], 'curl -X POST http://localhost:8080/api/adoptions/apply \\', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[255], '  -H "Content-Type: application/json" \\', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[256], '  -d \'{"petId": 12, "livingSituation": "OWNED", "hasYard": true, \\', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[257], '       "dailyHoursAlone": 3, "petExperience": "EXPERIENCED"}\'', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[259], "6.4 Transaction Management", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[260], "Declarative transaction management is enforced with Spring's @Transactional annotation at the service boundary. For instance, when an adoption application is approved in AdoptionService, the method atomically updates the application record, transitions the pet's status to ADOPTED, invalidates competing pending applications for the same animal, and dispatches email notifications. If any step fails, the entire transaction rolls back cleanly, maintaining absolute database consistency.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[261], "6.5 Spring AOP Logging", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[262], "The LoggingAspect class utilizes @Aspect and @Around advice targeting execution(* com.petconnect.service.*.*(..)) and execution(* com.petconnect.controller.*.*(..)). The aspect records entry timestamps, method signatures, parameter arguments, execution latency, and return values or exceptions. This telemetry enables real-time performance bottleneck diagnosis and security auditing without polluting core business code.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[263], "6.6 Validation and Exception Handling", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[264], "Every incoming request DTO is validated with Jakarta Bean Validation annotations. Table 6.3 details the unified exception mapping enforced by GlobalExceptionHandler (@ControllerAdvice).", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[265], "Table 6.3: Exception to HTTP Status Mapping", font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    t_exc = doc.tables[7]
    exc_data = [
        ("Failure", "Status", "Notes"),
        ("Bean Validation or bad request", "400", "field: message pairs; generic message for malformed JSON"),
        ("ResourceNotFoundException", "404", "Unknown pet, shelter, user, adoption request, or care booking"),
        ("DoubleBookingException", "409", "Visit slot already booked for that shelter and time slot"),
        ("ShelterCapacityExceededException", "409", "Shelter temporary boarding capacity is completely occupied"),
        ("DuplicateUserEmailException", "409", "Email address already registered across PetConnect"),
        ("Optimistic-lock race (@Version)", "409", "Concurrent care booking or capacity modification"),
        ("Any unexpected error", "500", "Sanitized correlation error reference; no stack traces exposed")
    ]
    for row_idx, (col0, col1, col2) in enumerate(exc_data):
        if row_idx < len(t_exc.rows):
            set_cell(t_exc.rows[row_idx].cells[0], col0, font_size=10, bold=(row_idx==0))
            set_cell(t_exc.rows[row_idx].cells[1], col1, font_size=10, bold=(row_idx==0))
            set_cell(t_exc.rows[row_idx].cells[2], col2, font_size=10, bold=(row_idx==0))
            
    set_p(doc.paragraphs[267], "A sample error response returned on a visit slot double-booking conflict is formatted as follows:", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[268], "{", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[269], '  "status": 409,', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[270], '  "error": "Conflict",', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[271], '  "message": "The selected visit slot is already booked by another adopter.",', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[272], '  "path": "/api/shelter/slots/book"', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[273], "}", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    set_heading(doc.paragraphs[275], "6.7 AI Microservice Integration", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[276], "The Spring Boot application interfaces with the Python FastAPI microservice via RestTemplate/WebClient over HTTP. Table 6.4 summarizes the AI microservice endpoints for OCR document extraction and PetMatch recommendations.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[277], "Table 6.4: AI Microservice Operations", font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    t_ai = doc.tables[8]
    ai_data = [
        ("Operation", "Request", "Returns"),
        ("verifyDocument", "MultipartForm {shelterId, documentFile}", "Extracted registration ID, name match confidence (0-100), risk level (LOW/MED/HIGH)"),
        ("extractDocumentOcr", "MultipartForm {documentFile}", "Full sanitized OCR text, detected document type, and bounding coordinates"),
        ("petMatchRecommend", "UserPreferenceDto {living, activity, aloneHours}", "Ranked list of compatible pet IDs with compatibility percentages")
    ]
    for row_idx, (col0, col1, col2) in enumerate(ai_data):
        if row_idx < len(t_ai.rows):
            set_cell(t_ai.rows[row_idx].cells[0], col0, font_size=10, bold=(row_idx==0))
            set_cell(t_ai.rows[row_idx].cells[1], col1, font_size=10, bold=(row_idx==0))
            set_cell(t_ai.rows[row_idx].cells[2], col2, font_size=10, bold=(row_idx==0))
            
    set_heading(doc.paragraphs[279], "6.8 Security Headers, CORS and Access Control", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[280], "Spring Security enforces strict Role-Based Access Control (RBAC). Routes prefixed with /admin/** require ROLE_ADMIN authority, /shelter/** requires ROLE_SHELTER_OWNER, and adoption/care bookings require authenticated ROLE_USER accounts. SecurityHeadersFilter injects Content-Security-Policy, X-Frame-Options: DENY, and X-Content-Type-Options: nosniff headers. CORS configuration restricts cross-origin access to authorized origins.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[281], "6.9 Testing", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[282], "Automated testing covers unit and integration test suites using JUnit 5, Mockito, and Spring Security Test. Table 6.5 details the automated tests verifying adoption flows, capacity locking, and security boundaries.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[283], "Table 6.5: Automated Backend Tests", font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    t_tests = doc.tables[9]
    tests_data = [
        ("Test Class", "Tests", "Area Verified"),
        ("AdoptionServiceTest", "3", "Valid application creation, approval status transition, competing application rejection"),
        ("CareBookingServiceTest", "4", "Valid boarding booking; full capacity throws ShelterCapacityExceededException; optimistic locking"),
        ("ShelterVerificationTest", "3", "AI confidence score evaluation; admin manual approval override; invalid license rejection"),
        ("PetMatchServiceTest", "2", "Adopter questionnaire matching with dog/cat energy levels; edge case lifestyle handling"),
        ("VisitSlotServiceTest", "2", "Slot reservation creates confirmed booking; double-booking same slot throws DoubleBookingException"),
        ("GlobalExceptionHandlerTest", "6", "Accurate mapping of custom exceptions to HTTP status codes (400, 403, 404, 409, 500)"),
        ("SecurityAccessTest", "3", "Role-based authorization restricting /admin/** and /shelter/** endpoints"),
        ("PetConnectApplicationTests", "1", "Spring Boot application context initialization and bean wiring"),
        ("Total", "24", "Failures: 0, Errors: 0, Skipped: 0")
    ]
    for row_idx, (col0, col1, col2) in enumerate(tests_data):
        if row_idx < len(t_tests.rows):
            set_cell(t_tests.rows[row_idx].cells[0], col0, font_size=10, bold=(row_idx==0))
            set_cell(t_tests.rows[row_idx].cells[1], col1, font_size=10, bold=(row_idx==0))
            set_cell(t_tests.rows[row_idx].cells[2], col2, font_size=10, bold=(row_idx==0))
            
    set_heading(doc.paragraphs[285], "6.10 Running the Project", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[286], "Running PetConnect requires Java 17+, MySQL 8.x, and Python 3.10+ (for the AI microservice). Steps to build and start the system:", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[287], "# Terminal 1 - Start Spring Boot application on :8080", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[288], ".\\mvnw.cmd spring-boot:run", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[290], "# Terminal 2 - Start Python FastAPI AI microservice on :8000", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[291], "cd ai-service", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[292], "pip install -r requirements.txt", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[293], "uvicorn main:app --host 0.0.0.0 --port 8000 --reload", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[296], "# Open web browser at http://localhost:8080/", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[298], "# Execute automated backend test suite", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[299], ".\\mvnw.cmd test", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    
    # -------------------------------------------------------------
    # CHAPTER 7: OUTPUT SCREENS
    # -------------------------------------------------------------
    set_heading(doc.paragraphs[303], "CHAPTER 7", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[304], "OUTPUT SCREENS", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    
    screens = [
        (305, "7.1 Citizen Landing Page", 306, "The PetConnect public homepage introduces users to pet adoption opportunities, featured rescued animals, and search filters.", 308, "Figure 7.1: Citizen Landing Page"),
        (309, "7.2 Pet and Shelter Finder", 310, "The Shelter Finder interface allows users to locate verified rescue organizations by city, district, and operational capacity.", 312, "Figure 7.2: Pet and Shelter Finder"),
        (313, "7.3 Shelter Detail View", 314, "The Shelter Detail page showcases organization background, operating hours, facility photos, and currently housed pets.", 316, "Figure 7.3: Shelter Detail View"),
        (317, "7.4 Rescued Pet Catalog", 318, "The Pet Directory presents rescued dogs and cats with age, breed, gender, vaccination status, and high-resolution photos.", 320, "Figure 7.4: Rescued Pet Catalog"),
        (321, "7.5 Adoption Application Workflow", 322, "The three-step adoption application allows prospective adopters to submit background information and home environment readiness.", 324, "Figure 7.5: Adoption Application Workflow"),
        (325, "7.6 User and Shelter Sign-In", 326, "The secure login interface supports authentication with email and BCrypt-hashed password credentials across all user roles.", 328, "Figure 7.6: User and Shelter Sign-In"),
        (329, "7.7 Shelter and Adopter Registration", 330, "The registration portal provides dedicated sign-up forms for adopters and shelter owners, including license upload fields.", 332, "Figure 7.7: Shelter and Adopter Registration"),
        (333, "7.8 Shelter Management Dashboard", 334, "The Shelter Owner Dashboard summarizes total pets, pending adoption inquiries, scheduled visits, and temporary care capacity.", 336, "Figure 7.8: Shelter Management Dashboard"),
        (337, "7.9 Pet Inventory Management", 338, "The Pet Registry displays complete records of animals under shelter care, enabling pet addition, status changes, and photo updates.", 340, "Figure 7.9: Pet Inventory Management"),
        (341, "7.10 Adoption Application Timeline", 342, "The Adoption Timeline provides adopters with transparent real-time tracking of application review milestones and approvals.", 344, "Figure 7.10: Adoption Application Timeline"),
        (345, "7.11 Temporary Care Booking Queue", 346, "The Care Booking Queue allows shelter staff to inspect upcoming pet boarding reservations, check-in dates, and medical notes.", 348, "Figure 7.11: Temporary Care Booking Queue"),
        (349, "7.12 Shelter Boarding Capacity Board", 350, "The Boarding Occupancy Board visualizes current kennel capacity and real-time occupancy to avoid overbooking.", 352, "Figure 7.12: Shelter Boarding Capacity Board"),
        (353, "7.13 Pet Health and Vaccination Records", 354, "The Medical Records page logs clinical history, rabies vaccinations, neutering records, and special dietary needs for each pet.", 356, "Figure 7.13: Pet Health and Vaccination Records"),
        (357, "7.14 Platform Analytics and Reports", 358, "The Analytics view provides administrators and shelter managers with trends on monthly adoptions, care bookings, and demographics.", 360, "Figure 7.14: Platform Analytics and Reports"),
        (361, "7.15 Emergency Rescue and Lost Pet Alerts", 362, "The Emergency Board broadcasts urgent community alerts for lost pets and injured animals requiring immediate rescue.", 364, "Figure 7.15: Emergency Rescue and Lost Pet Alerts"),
        (365, "7.16 Adoption Guidelines and Animal Welfare Policy", 366, "The Policy page provides legal guidelines, adoption responsibilities, and animal welfare standards for prospective pet parents.", 368, "Figure 7.16: Adoption Guidelines and Animal Welfare Policy"),
        (369, "7.17 Profile and Notification Preferences", 370, "The Profile interface enables users to update personal details, home address, contact numbers, and email notification settings.", 372, "Figure 7.17: Profile Page"),
        (375, "7.18 Help Center and Animal Helplines", 376, "The Help Center provides frequently asked questions, rescue guidelines, veterinary emergency contacts, and support helplines.", 378, "Figure 7.19: Help Center and Animal Helplines"),
        (379, "7.19 Dark Theme Support", 380, "PetConnect provides full dark theme support, enhancing accessibility and visual comfort in low-light environments.", 382, "Figure 7.20: Landing Page in Dark Theme")
    ]
    for h_idx, h_text, b_idx, b_text, c_idx, c_text in screens:
        if h_idx < len(doc.paragraphs):
            set_heading(doc.paragraphs[h_idx], h_text, font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
        if b_idx < len(doc.paragraphs):
            set_p(doc.paragraphs[b_idx], b_text, font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
        if c_idx < len(doc.paragraphs):
            set_p(doc.paragraphs[c_idx], c_text, font_size=11, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
            
    set_heading(doc.paragraphs[385], "7.20 Mobile Responsive Layout", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[386], "PetConnect adapts dynamically to mobile viewports, providing fluid navigation, touch-friendly touch targets, and responsive cards.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[387], "7.21 AI Document Verification API Response", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[388], "Upon processing a shelter registration certificate, the AI microservice returns a structured JSON payload with OCR text and verification confidence:", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[389], "{", font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[390], '  "shelterId": 5, "registrationNumber": "AP-SHELTER-2024-889",', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[391], '  "nameMatch": true, "confidenceScore": 96, "riskLevel": "LOW",', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[392], '  "verificationStatus": "APPROVED"', font_size=11, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_heading(doc.paragraphs[393], "7.22 Automated Test and Build Result", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[394], "Executing .\\mvnw.cmd test compiles all source files and executes the 24 automated unit and integration tests with zero failures or errors.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    # -------------------------------------------------------------
    # CHAPTER 8: CONCLUSION
    # -------------------------------------------------------------
    set_heading(doc.paragraphs[401], "CHAPTER 8", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[402], "CONCLUSION", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    set_heading(doc.paragraphs[403], "8.1 Summary", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[404], "The PetConnect Management System successfully demonstrates how modern enterprise engineering and artificial intelligence can transform animal welfare operations. By consolidating pet adoption, shelter licensing verification, and temporary boarding into a unified platform, PetConnect resolves the fragmentation that has historically challenged rescue organizations and prospective adopters.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[405], "The application is built on Java 17, Spring Boot 3.3.3, Spring Security, Thymeleaf, and MySQL 8.x. The incorporation of a Python FastAPI microservice for Optical Character Recognition (OCR) enables automated document inspection, enhancing trust and administrative efficiency. Concurrency controls, including optimistic locking and unique slot constraints, protect transactional integrity during adoption approvals and temporary boarding reservations.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    set_p(doc.paragraphs[406], "Extensive automated unit and integration testing confirms that the architecture is reliable, secure, and resilient under concurrent user load.", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    
    set_heading(doc.paragraphs[407], "8.2 Future Enhancements", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.LEFT)
    set_p(doc.paragraphs[408], "Several key enhancements are planned for future iterations of PetConnect:", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
    enhancements = [
        "Dedicated Mobile Application: Developing native iOS and Android applications using Flutter or React Native to offer push notifications and camera-based pet health tracking.",
        "Integrated Payment Gateway: Implementing Razorpay or Stripe to facilitate adoption fee processing, temporary boarding deposits, and charitable donations directly within the platform.",
        "IoT Smart Collar Integration: Connecting GPS and biometric smart collars to monitor real-time location, activity levels, and vital signs of pets during temporary shelter boarding.",
        "Veterinary Telehealth Integration: Expanding video consultations to include certified veterinarians for virtual health check-ups and digital prescription issuance.",
        "Automated Vaccination Reminders: Implementing SMS and WhatsApp messaging APIs to notify pet parents of approaching rabies and distemper booster schedules.",
        "Geolocation and Route Optimization: Assisting adopters in finding nearby shelters, veterinary clinics, and pet-friendly supply stores using interactive mapping.",
        "Machine Learning Fraud Detection: Analyzing adoption behavioral patterns and duplicate pet listings across geographic zones to prevent unauthorized pet flipping."
    ]
    for idx, enh in enumerate(enhancements):
        p_idx = 409 + idx
        if p_idx < len(doc.paragraphs):
            p = doc.paragraphs[p_idx]
            set_p(p, enh, font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
            
    # References
    ref_heading_idx = 416
    for i in range(410, len(doc.paragraphs)):
        if "REFERENCES" in doc.paragraphs[i].text.upper():
            ref_heading_idx = i
            break
            
    set_heading(doc.paragraphs[ref_heading_idx], "REFERENCES", font_size=13, bold=True, align=WD_ALIGN_PARAGRAPH.CENTER)
    refs = [
        "Spring Boot Reference Documentation. Version 3.3.3. VMware Tanzu, 2024. https://docs.spring.io/spring-boot/docs/current/reference/html/",
        "Spring Data JPA Reference Guide. VMware Tanzu, 2024. https://docs.spring.io/spring-data/jpa/reference/",
        "Spring Security Architecture and Reference Manual. VMware Tanzu, 2024. https://docs.spring.io/spring-security/reference/",
        "Thymeleaf 3: Modern Server-Side Java Template Engine Documentation. Thymeleaf Project, 2024. https://www.thymeleaf.org/",
        "MySQL 8.0 Reference Manual. Oracle Corporation, 2024. https://dev.mysql.com/doc/refman/8.0/en/",
        "FastAPI Modern High-Performance Web Framework Documentation. Sebastián Ramírez, 2024. https://fastapi.tiangolo.com/",
        "Tesseract OCR: Open Source Optical Character Recognition Engine. Ray Smith, Google LLC, 2024. https://github.com/tesseract-ocr/tesseract",
        "Hibernate ORM 6.5 User Guide. Red Hat, Inc., 2024. https://hibernate.org/orm/documentation/",
        "Jakarta Bean Validation Specification. Eclipse Foundation, 2024. https://beanvalidation.org/",
        "Apache Maven Project Documentation. Apache Software Foundation, 2024. https://maven.apache.org/",
        "JUnit 5 User Guide. JUnit Team, 2024. https://junit.org/junit5/docs/current/user-guide/",
        "Pet Welfare and Animal Shelter Standards. Animal Welfare Board of India, 2024. http://www.awbi.in/",
        "PetConnect Management System Architecture Repository. Aditya University AIML Project, 2026.",
        "Docker Community Edition Documentation. Docker Inc., 2024. https://docs.docker.com/",
        "OAuth 2.0 and OpenID Connect Security Patterns. Internet Engineering Task Force, 2024. https://oauth.net/2/"
    ]
    for idx, ref in enumerate(refs):
        p_idx = ref_heading_idx + 1 + idx
        if p_idx < len(doc.paragraphs):
            set_p(doc.paragraphs[p_idx], f"[{idx+1}]\t{ref}", font_size=12, align=WD_ALIGN_PARAGRAPH.JUSTIFY)
            
    # -------------------------------------------------------------
    # UPDATE TABLE 1 (Table of Contents) & TABLE 2 (List of Figures)
    # -------------------------------------------------------------
    t1 = doc.tables[1]
    toc_updates = {
        14: ("3.3", "Frontend Technologies", "6"),
        17: ("3.6", "MySQL Database", "7"),
        20: ("3.9", "Python FastAPI AI Microservice", "8"),
        21: ("3.10", "Security Headers and Spring Security", "8"),
        25: ("4.2", "Shelters Table", "9"),
        26: ("4.3", "Users Table", "9"),
        27: ("4.4", "Pets Table", "10"),
        28: ("4.5", "Adoption Requests Table", "10"),
        29: ("4.6", "Care Bookings Table", "10"),
        30: ("4.7", "Shelter Documents Table", "11"),
        31: ("4.8", "Shelter Visit Slots Table", "11"),
        32: ("4.9", "Shelter Reviews Table", "11"),
        33: ("4.10", "Database Relationships & Concurrency Control", "12"),
        35: ("5.1", "Shelter Registration & Onboarding Module", "13"),
        36: ("5.2", "User & Pet Adopter Profile Management", "13"),
        37: ("5.3", "Pet Catalog & Search Filter Module", "13"),
        38: ("5.4", "Adoption Application & Screening Workflow", "14"),
        39: ("5.5", "Temporary Pet Care & Boarding Module", "14"),
        40: ("5.6", "AI-Assisted Document Verification Microservice", "14"),
        41: ("5.7", "PetMatch AI Recommendation Engine", "15"),
        42: ("5.8", "Shelter Visit Scheduling & Slot Booking", "15"),
        43: ("5.9", "Shelter Ratings, Reviews & Feedback Module", "15"),
        44: ("5.10", "Audit Logging, Error Handling & Email Alerts", "16"),
        52: ("6.7", "AI Microservice Integration", "18"),
        53: ("6.8", "Security Headers, CORS and Access Control", "17"),
        57: ("7.1", "Citizen & Adopter Landing Page", "20"),
        58: ("7.2", "Pet & Shelter Finder", "20"),
        59: ("7.3", "Shelter Detail View", "21"),
        60: ("7.4", "Rescued Pet Catalog", "21"),
        61: ("7.5", "Adoption Application Workflow", "22"),
        62: ("7.6", "User and Shelter Sign-In", "22"),
        63: ("7.7", "Shelter and Adopter Registration", "23"),
        64: ("7.8", "Shelter Management Dashboard", "23"),
        65: ("7.9", "Pet Inventory Management", "24"),
        66: ("7.10", "Adoption Application Timeline", "24"),
        67: ("7.11", "Temporary Care Booking Queue", "25"),
        68: ("7.12", "Shelter Boarding Capacity Board", "25"),
        69: ("7.13", "Pet Health and Vaccination Records", "26"),
        70: ("7.14", "Platform Analytics and Reports", "26"),
        71: ("7.15", "Emergency Rescue and Lost Pet Alerts", "27"),
        72: ("7.16", "Adoption Guidelines and Animal Welfare Policy", "27"),
        73: ("7.17", "Profile and Notification Preferences", "28"),
        74: ("7.18", "Help Center and Animal Helplines", "28"),
        75: ("7.19", "Dark Theme Support", "29"),
        76: ("7.20", "Mobile Responsive Layout", "29"),
        77: ("7.21", "AI Document Verification API Response", "30"),
        78: ("7.22", "Automated Test and Build Result", "30")
    }
    for r_idx, (col0, col1, col2) in toc_updates.items():
        if r_idx < len(t1.rows):
            set_cell(t1.rows[r_idx].cells[0], col0, font_size=10, bold=False)
            set_cell(t1.rows[r_idx].cells[1], col1, font_size=10, bold=False)
            set_cell(t1.rows[r_idx].cells[2], col2, font_size=10, bold=False, align=WD_ALIGN_PARAGRAPH.RIGHT)
            
    for r in t1.rows:
        for c in r.cells:
            for p in c.paragraphs:
                for run in p.runs:
                    set_run_font(run, "Times New Roman", size_pt=10, bold=run.bold)
                    
    # Update Table 2 (List of Figures)
    t2 = doc.tables[2]
    fig_updates = {
        2: ("Figure 4.1", "Database Design", "9"),
        3: ("Figure 7.1", "Citizen & Adopter Landing Page", "20"),
        4: ("Figure 7.2", "Pet & Shelter Finder", "20"),
        5: ("Figure 7.3", "Shelter Detail View", "21"),
        6: ("Figure 7.4", "Rescued Pet Catalog", "21"),
        7: ("Figure 7.5", "Adoption Application Workflow", "22"),
        8: ("Figure 7.6", "User and Shelter Sign-In", "22"),
        9: ("Figure 7.7", "Shelter and Adopter Registration", "23"),
        10: ("Figure 7.8", "Shelter Management Dashboard", "23"),
        11: ("Figure 7.9", "Pet Inventory Management", "24"),
        12: ("Figure 7.10", "Adoption Application Timeline", "24"),
        13: ("Figure 7.11", "Temporary Care Booking Queue", "25"),
        14: ("Figure 7.12", "Shelter Boarding Capacity Board", "25"),
        15: ("Figure 7.13", "Pet Health and Vaccination Records", "26"),
        16: ("Figure 7.14", "Platform Analytics and Reports", "26"),
        17: ("Figure 7.15", "Emergency Rescue and Lost Pet Alerts", "27"),
        18: ("Figure 7.16", "Adoption Guidelines and Animal Welfare Policy", "27"),
        19: ("Figure 7.17", "Profile and Settings", "28"),
        20: ("Figure 7.18", "System Configuration and Rules", "28"),
        21: ("Figure 7.19", "Help Center and Animal Helplines", "28"),
        22: ("Figure 7.20", "Landing Page in Dark Theme", "29"),
        23: ("Figure 7.21", "Dashboard in Dark Theme", "29"),
        24: ("Figure 7.22", "Landing Page on a Mobile Screen", "29"),
        25: ("Figure 7.23", "Dashboard on a Mobile Screen", "30")
    }
    for r_idx, (col0, col1, col2) in fig_updates.items():
        if r_idx < len(t2.rows):
            set_cell(t2.rows[r_idx].cells[0], col0, font_size=10, bold=False)
            set_cell(t2.rows[r_idx].cells[1], col1, font_size=10, bold=False)
            set_cell(t2.rows[r_idx].cells[2], col2, font_size=10, bold=False, align=WD_ALIGN_PARAGRAPH.RIGHT)
            
    for r in t2.rows:
        for c in r.cells:
            for p in c.paragraphs:
                for run in p.runs:
                    set_run_font(run, "Times New Roman", size_pt=10, bold=run.bold)

    # -------------------------------------------------------------
    # GLOBAL TYPOGRAPHY VERIFICATION (Times New Roman, Headings 13pt bold, Body 12pt justified)
    # -------------------------------------------------------------
    print("Enforcing global typography across all paragraphs...")
    for idx, p in enumerate(doc.paragraphs):
        # Skip paragraph 16 and 20 if they are logo runs
        if idx in [16, 20]:
            continue
        text = p.text.strip()
        if not text:
            continue
        is_heading = (p.style.name.startswith("Heading") or 
                      text.startswith("CHAPTER") or 
                      (len(text) > 3 and text[:2].isdigit() and text[2] == '.') or
                      text in ["DECLARATION", "ACKNOWLEDGEMENT", "ABSTRACT", "TABLE OF CONTENTS", "LIST OF FIGURES", "REFERENCES", "CERTIFICATE"])
        
        # Don't alter alignment of front matter pages
        if idx >= 64:
            if is_heading:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER if ("CHAPTER" in text or text in ["INTRODUCTION", "SYSTEM OVERVIEW", "TECHNOLOGIES USED", "DATABASE DESIGN", "SYSTEM MODULES", "API AND IMPLEMENTATION", "OUTPUT SCREENS", "CONCLUSION", "REFERENCES"]) else WD_ALIGN_PARAGRAPH.LEFT
                for run in p.runs:
                    set_run_font(run, "Times New Roman", size_pt=13, bold=True)
            elif text.startswith("Figure") or text.startswith("Table"):
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                for run in p.runs:
                    set_run_font(run, "Times New Roman", size_pt=11, bold=True)
            elif text.startswith("#") or text.startswith("curl") or text.startswith("{\n") or text.startswith("}") or text.startswith(".\\mvnw"):
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
                for run in p.runs:
                    set_run_font(run, "Times New Roman", size_pt=10, bold=False)
            else:
                p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
                for run in p.runs:
                    set_run_font(run, "Times New Roman", size_pt=12, bold=run.bold)

    print("Saving modified DOCX to:", TARGET_DOCX)
    doc.save(TARGET_DOCX)
    print("DOCX successfully saved.")

def replace_database_diagram():
    print("Replacing Figure 4.1 image in DOCX zip with Figure_4_1_Database_Design.png...")
    temp_zip = TARGET_DOCX + ".tmp.zip"
    
    with zipfile.ZipFile(TARGET_DOCX, 'r') as zin:
        with zipfile.ZipFile(temp_zip, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                if item.filename == "word/media/image4.png":
                    print(f"Replacing {item.filename} with {DB_IMAGE}")
                    with open(DB_IMAGE, "rb") as f:
                        zout.writestr(item.filename, f.read())
                else:
                    zout.writestr(item, zin.read(item.filename))
                    
    shutil.move(temp_zip, TARGET_DOCX)
    print("Figure 4.1 successfully replaced.")

def copy_to_downloads():
    print("Copying DOCX to Downloads:", DOWNLOADS_DOCX)
    shutil.copyfile(TARGET_DOCX, DOWNLOADS_DOCX)

if __name__ == "__main__":
    build_document()
    replace_database_diagram()
    copy_to_downloads()
    print("All DOCX operations completed successfully.")
