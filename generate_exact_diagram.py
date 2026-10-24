import matplotlib.pyplot as plt
import matplotlib.patches as patches

# Set up figure
fig, ax = plt.subplots(figsize=(13.5, 9.5), dpi=300)
ax.set_xlim(0, 13.5)
ax.set_ylim(0, 9.5)
ax.axis('off')

# Color palette matching user's exact screenshot
HEADER_BG = '#1A3F7A'      # Deep navy blue header
HEADER_TXT = '#FFFFFF'     # Crisp white header text
BODY_BG = '#F0F4FA'        # Light soft blue-gray body background
BORDER_COL = '#1A3F7A'     # Navy border for tables and lines
LINE_COL = '#1A3F7A'       # Connection line color
DIVIDER_COL = '#D5DFED'    # Row divider lines
TEXT_MAIN = '#111827'      # Dark readable text

def draw_table_card(ax, x, y, width, height, title, rows, is_hub=False):
    h_height = 0.44
    b_height = height - h_height
    
    # Outer box
    box = patches.FancyBboxPatch((x, y), width, height,
                                 boxstyle="round,pad=0.0,rounding_size=0.03",
                                 linewidth=2.0 if is_hub else 1.5,
                                 edgecolor=BORDER_COL,
                                 facecolor=BODY_BG,
                                 zorder=2)
    ax.add_patch(box)
    
    # Header box
    h_box = patches.FancyBboxPatch((x, y + b_height), width, h_height,
                                   boxstyle="round,pad=0.0,rounding_size=0.03",
                                   linewidth=0,
                                   edgecolor=BORDER_COL,
                                   facecolor=HEADER_BG,
                                   zorder=3)
    ax.add_patch(h_box)
    
    # Title
    ax.text(x + width / 2.0, y + b_height + h_height / 2.0, title,
            color=HEADER_TXT, fontsize=10.5, fontweight='bold',
            ha='center', va='center', zorder=4, family='sans-serif')
    
    # Border under header
    ax.plot([x, x + width], [y + b_height, y + b_height], color=BORDER_COL, lw=1.5, zorder=4)
    
    # Rows
    n_rows = len(rows)
    row_h = b_height / n_rows
    for i, r in enumerate(rows):
        ry = y + b_height - (i + 0.5) * row_h
        ax.text(x + 0.15, ry, r,
                color=TEXT_MAIN, fontsize=8.8,
                ha='left', va='center', zorder=4, family='sans-serif')
        if i < n_rows - 1:
            ax.plot([x, x + width], [y + b_height - (i + 1) * row_h, y + b_height - (i + 1) * row_h],
                    color=DIVIDER_COL, lw=0.7, zorder=3)

# ----------------- 1. TOP ROW (3 TABLES) -----------------
# Top-Left: PETS (like DEPARTMENTS)
draw_table_card(ax, 0.7, 7.0, 3.2, 1.9, "PETS", [
    "id (PK)",
    "name, type, breed",
    "age, gender, status",
    "shelter_id (FK)"
])

# Top-Center: CARE_BOOKINGS (like DOCTORS)
draw_table_card(ax, 5.0, 6.8, 3.3, 2.1, "CARE_BOOKINGS", [
    "id (PK)",
    "pet_id, user_id (FK)",
    "shelter_id (FK)",
    "check_in, check_out",
    "boarding_fee, status"
])

# Top-Right: SHELTER_DOCUMENTS (like BED_INVENTORY)
draw_table_card(ax, 9.4, 6.8, 3.3, 2.1, "SHELTER_DOCUMENTS", [
    "id (PK)",
    "shelter_id (FK)",
    "document_type",
    "ai_score, risk_level",
    "admin_decision"
])

# ----------------- 2. MIDDLE ROW (3 TABLES) -----------------
# Middle-Left: USERS (like PATIENTS)
draw_table_card(ax, 0.7, 3.7, 3.2, 2.1, "USERS", [
    "id (PK)",
    "full_name, email",
    "phone, city",
    "status, roles",
    "created_at"
])

# Middle-Center: SHELTERS (Central Hub - like HOSPITALS)
draw_table_card(ax, 5.0, 3.7, 3.3, 2.1, "SHELTERS", [
    "id (PK)",
    "name, city",
    "location, capacity",
    "verification_status",
    "owner_id (FK)"
], is_hub=True)

# Middle-Right: ADOPTION_REQUESTS (like REFERRALS)
draw_table_card(ax, 9.4, 3.7, 3.3, 2.1, "ADOPTION_REQUESTS", [
    "id (PK)",
    "pet_id, user_id (FK)",
    "shelter_id (FK)",
    "status, reason",
    "created_at"
])

# ----------------- 3. BOTTOM ROW (2 TABLES + 1 LEGEND) -----------------
# Bottom-Center: SHELTER_VISIT_SLOTS (like APPOINTMENTS)
draw_table_card(ax, 4.5, 0.6, 3.3, 2.1, "SHELTER_VISIT_SLOTS", [
    "id (PK)",
    "pet_id, user_id (FK)",
    "shelter_id (FK)",
    "visit_date, time_slot",
    "status, remarks"
])

# Bottom-Right: SHELTER_REVIEWS (like MEDICAL_RECORDS)
draw_table_card(ax, 8.8, 0.6, 3.3, 2.1, "SHELTER_REVIEWS", [
    "id (PK)",
    "pet_id, user_id (FK)",
    "shelter_id (FK)",
    "rating (1-5), comment",
    "created_at"
])

# Bottom-Left: LEGEND (Dashed Box exactly like user's screenshot)
legend_box = patches.FancyBboxPatch((0.7, 0.6), 3.4, 1.4,
                                   boxstyle="round,pad=0.0,rounding_size=0.04",
                                   linewidth=1.2,
                                   linestyle='--',
                                   edgecolor='#6B8DBE',
                                   facecolor='#F8FAFC',
                                   zorder=2)
ax.add_patch(legend_box)

ax.text(0.9, 1.5, "PK - Primary Key", color=TEXT_MAIN, fontsize=9.2, fontweight='medium', family='sans-serif')
ax.text(2.6, 1.5, "FK - Foreign Key", color=TEXT_MAIN, fontsize=9.2, fontweight='medium', family='sans-serif')
ax.text(0.9, 0.95, "1 - one record", color=TEXT_MAIN, fontsize=9.2, fontweight='medium', family='sans-serif')
ax.text(2.6, 0.95, "N - many records", color=TEXT_MAIN, fontsize=9.2, fontweight='medium', family='sans-serif')


# ----------------- 4. RELATIONSHIP LINES & LABELS -----------------
LW = 1.8

# A. Middle-Left (USERS) to SHELTERS (Direct Horizontal Line)
ax.plot([3.9, 5.0], [4.75, 4.75], color=LINE_COL, lw=LW, zorder=1)
ax.text(4.05, 4.90, "N", color=LINE_COL, fontsize=10.5, fontweight='bold')
ax.text(4.75, 4.90, "1", color=LINE_COL, fontsize=10.5, fontweight='bold')

# B. SHELTERS to Middle-Right (ADOPTION_REQUESTS) (Direct Horizontal Line)
ax.plot([8.3, 9.4], [4.75, 4.75], color=LINE_COL, lw=LW, zorder=1)
ax.text(8.50, 4.90, "1", color=LINE_COL, fontsize=10.5, fontweight='bold')
ax.text(9.15, 4.90, "N", color=LINE_COL, fontsize=10.5, fontweight='bold')

# C. Top-Left (PETS) to SHELTERS
ax.plot([2.3, 2.3, 5.5, 5.5], [7.0, 6.3, 6.3, 5.8], color=LINE_COL, lw=LW, zorder=1)
ax.text(2.45, 6.70, "N", color=LINE_COL, fontsize=10.5, fontweight='bold')
ax.text(5.65, 5.95, "1", color=LINE_COL, fontsize=10.5, fontweight='bold')

# D. Top-Center (CARE_BOOKINGS) to SHELTERS (Direct Vertical Line)
ax.plot([6.65, 6.65], [6.8, 5.8], color=LINE_COL, lw=LW, zorder=1)
ax.text(6.80, 6.55, "N", color=LINE_COL, fontsize=10.5, fontweight='bold')
ax.text(6.80, 5.95, "1", color=LINE_COL, fontsize=10.5, fontweight='bold')

# E. Top-Right (SHELTER_DOCUMENTS) to SHELTERS
ax.plot([11.05, 11.05, 7.8, 7.8], [6.8, 6.3, 6.3, 5.8], color=LINE_COL, lw=LW, zorder=1)
ax.text(10.75, 6.55, "N", color=LINE_COL, fontsize=10.5, fontweight='bold')
ax.text(7.55, 5.95, "1", color=LINE_COL, fontsize=10.5, fontweight='bold')

# F. SHELTERS to Bottom-Center (SHELTER_VISIT_SLOTS)
ax.plot([6.15, 6.15], [3.7, 2.7], color=LINE_COL, lw=LW, zorder=1)
ax.text(6.30, 3.45, "1", color=LINE_COL, fontsize=10.5, fontweight='bold')
ax.text(6.30, 2.85, "N", color=LINE_COL, fontsize=10.5, fontweight='bold')

# G. SHELTERS to Bottom-Right (SHELTER_REVIEWS)
ax.plot([7.6, 7.6, 10.45, 10.45], [3.7, 3.2, 3.2, 2.7], color=LINE_COL, lw=LW, zorder=1)
ax.text(7.75, 3.45, "1", color=LINE_COL, fontsize=10.5, fontweight='bold')
ax.text(10.60, 2.85, "N", color=LINE_COL, fontsize=10.5, fontweight='bold')

plt.tight_layout()
plt.savefig(r'e:\petconnect\Figure_4_1_Database_Design.png', dpi=300, bbox_inches='tight')
plt.savefig(r'C:\Users\sreen\.gemini\antigravity\brain\0074ec70-e87b-4145-9099-ce99aab72b1d\Figure_4_1_Database_Design.png', dpi=300, bbox_inches='tight')
print("Accurate Figure 4.1 Database Design diagram created!")
