import matplotlib.pyplot as plt
import matplotlib.patches as patches

# Set up figure with crisp 300 DPI resolution
fig, ax = plt.subplots(figsize=(14, 9.5), dpi=300)
ax.set_xlim(0, 14)
ax.set_ylim(0, 9.5)
ax.axis('off')

# Colors matching the reference document diagram
HEADER_BG = '#1A3F7A'      # Deep navy blue header
HEADER_TXT = '#FFFFFF'     # Crisp white text
BODY_BG = '#F4F7FC'        # Soft professional light-blue background
BORDER_COL = '#1A3F7A'     # Navy table border
LINE_COL = '#1A3F7A'       # Connection line color
DIVIDER_COL = '#D1D9E6'    # Subtle row divider
TEXT_MAIN = '#111827'      # Sharp dark text

def draw_table_box(ax, x, y, width, height, title, rows, is_hub=False):
    h_height = 0.50
    b_height = height - h_height
    
    # Outer box border & body background
    rect = patches.FancyBboxPatch((x, y), width, height,
                                  boxstyle="round,pad=0.0,rounding_size=0.04",
                                  linewidth=2.2 if is_hub else 1.6,
                                  edgecolor=BORDER_COL,
                                  facecolor=BODY_BG,
                                  zorder=2)
    ax.add_patch(rect)
    
    # Header rectangle
    h_rect = patches.FancyBboxPatch((x, y + b_height), width, h_height,
                                    boxstyle="round,pad=0.0,rounding_size=0.04",
                                    linewidth=0,
                                    edgecolor=BORDER_COL,
                                    facecolor=HEADER_BG,
                                    zorder=3)
    ax.add_patch(h_rect)
    
    # Title text
    ax.text(x + width / 2.0, y + b_height + h_height / 2.0, title,
            color=HEADER_TXT, fontsize=11, fontweight='bold',
            ha='center', va='center', zorder=4, family='sans-serif')
    
    # Divider line under header
    ax.plot([x, x + width], [y + b_height, y + b_height], color=BORDER_COL, lw=1.6, zorder=4)
    
    # Rows text
    row_count = len(rows)
    row_h = b_height / row_count
    for i, row in enumerate(rows):
        ry = y + b_height - (i + 0.5) * row_h
        ax.text(x + 0.20, ry, row,
                color=TEXT_MAIN, fontsize=9.2,
                ha='left', va='center', zorder=4, family='sans-serif')
        # Divider between rows
        if i < row_count - 1:
            ax.plot([x, x + width], [y + b_height - (i + 1) * row_h, y + b_height - (i + 1) * row_h],
                    color=DIVIDER_COL, lw=0.8, zorder=3)

# Coordinates configuration
BOX_W = 3.5
BOX_H = 2.4

# Top row (3 tables) at y = 6.6
draw_table_box(ax, 0.7, 6.6, BOX_W, BOX_H, "PETS", [
    "id (PK)",
    "name, type, breed",
    "age, gender, status",
    "shelter_id (FK)",
    "user_id (FK)"
])

draw_table_box(ax, 5.25, 6.6, BOX_W, BOX_H, "ADOPTION_REQUESTS", [
    "id (PK)",
    "status, reason",
    "shelter_id (FK)",
    "pet_id (FK)",
    "user_id (FK)"
])

draw_table_box(ax, 9.8, 6.6, BOX_W, BOX_H, "CARE_BOOKINGS", [
    "id (PK)",
    "check_in, check_out",
    "status, boarding_fee",
    "shelter_id (FK)",
    "pet_id (FK)"
])

# Center Hub Table at y = 3.5
draw_table_box(ax, 5.25, 3.5, BOX_W, 2.5, "SHELTERS", [
    "id (PK)",
    "name, city, location",
    "capacity, available_cap",
    "verification_status",
    "owner_id (FK)"
], is_hub=True)

# Bottom row (3 tables) at y = 0.5
draw_table_box(ax, 0.7, 0.5, BOX_W, BOX_H, "SHELTER_DOCUMENTS", [
    "id (PK)",
    "document_type",
    "ai_score, risk_level",
    "admin_decision",
    "shelter_id (FK)"
])

draw_table_box(ax, 5.25, 0.5, BOX_W, BOX_H, "SHELTER_VISIT_SLOTS", [
    "id (PK)",
    "visit_date, time_slot",
    "status, remarks",
    "shelter_id (FK)",
    "pet_id (FK)"
])

draw_table_box(ax, 9.8, 0.5, BOX_W, BOX_H, "SHELTER_REVIEWS", [
    "id (PK)",
    "rating (1-5), comment",
    "created_at",
    "shelter_id (FK)",
    "pet_id (FK)"
])

# --- Connectors with Orthogonal Routing and 1 : N Cardinality Labels ---

# 1. PETS -> SHELTERS
ax.plot([2.45, 2.45, 5.7, 5.7], [6.6, 6.15, 6.15, 6.0], color=LINE_COL, lw=2.0, zorder=1)
ax.text(2.60, 6.35, "N", color=LINE_COL, fontsize=11, fontweight='bold')
ax.text(5.85, 6.18, "1", color=LINE_COL, fontsize=11, fontweight='bold')

# 2. ADOPTION_REQUESTS -> SHELTERS
ax.plot([7.0, 7.0], [6.6, 6.0], color=LINE_COL, lw=2.0, zorder=1)
ax.text(7.15, 6.35, "N", color=LINE_COL, fontsize=11, fontweight='bold')
ax.text(7.15, 6.12, "1", color=LINE_COL, fontsize=11, fontweight='bold')

# 3. CARE_BOOKINGS -> SHELTERS
ax.plot([11.55, 11.55, 8.3, 8.3], [6.6, 6.15, 6.15, 6.0], color=LINE_COL, lw=2.0, zorder=1)
ax.text(11.25, 6.35, "N", color=LINE_COL, fontsize=11, fontweight='bold')
ax.text(8.00, 6.18, "1", color=LINE_COL, fontsize=11, fontweight='bold')

# 4. SHELTER_DOCUMENTS -> SHELTERS
ax.plot([2.45, 2.45, 5.7, 5.7], [2.9, 3.35, 3.35, 3.5], color=LINE_COL, lw=2.0, zorder=1)
ax.text(2.60, 3.05, "N", color=LINE_COL, fontsize=11, fontweight='bold')
ax.text(5.85, 3.30, "1", color=LINE_COL, fontsize=11, fontweight='bold')

# 5. SHELTER_VISIT_SLOTS -> SHELTERS
ax.plot([7.0, 7.0], [2.9, 3.5], color=LINE_COL, lw=2.0, zorder=1)
ax.text(7.15, 3.05, "N", color=LINE_COL, fontsize=11, fontweight='bold')
ax.text(7.15, 3.30, "1", color=LINE_COL, fontsize=11, fontweight='bold')

# 6. SHELTER_REVIEWS -> SHELTERS
ax.plot([11.55, 11.55, 8.3, 8.3], [2.9, 3.35, 3.35, 3.5], color=LINE_COL, lw=2.0, zorder=1)
ax.text(11.25, 3.05, "N", color=LINE_COL, fontsize=11, fontweight='bold')
ax.text(8.00, 3.30, "1", color=LINE_COL, fontsize=11, fontweight='bold')

plt.tight_layout()
plt.savefig(r'e:\petconnect\Figure_4_1_Database_Tables_Overview.png', dpi=300, bbox_inches='tight')
plt.savefig(r'C:\Users\sreen\.gemini\antigravity\brain\0074ec70-e87b-4145-9099-ce99aab72b1d\Figure_4_1_Database_Tables_Overview.png', dpi=300, bbox_inches='tight')
print("High-quality diagram regenerated successfully!")
