import pygame
import re
from pygments import highlight
from pygments.lexers import JavaLexer
from pygments.formatters import HtmlFormatter
from pygments.token import Token
from pygments import lex

# ==========================
# CONFIG
# ==========================
SCREEN_WIDTH = 1280
SCREEN_HEIGHT = 720

BG_COLOR = (14, 14, 14)
BTN_COLOR = (90, 90, 90)
BTN_HIGHLIGHT = (200, 200, 100)
BTN_DRAGGING = (100, 200, 255)
BTN_BORDER = (230, 230, 230)
TEXT_COLOR = (255, 255, 255)
GRID_COLOR = (50, 50, 50)
AXIS_COLOR = (200, 70, 70)
GUIDE_COLOR = (255, 0, 0)  # Red snap guide lines
POPUP_BG = (30, 30, 30, 230)
POPUP_BORDER = (255, 255, 0)
GRID_SPACING = 50

zoom_scale = 1.0
ZOOM_STEP = 0.1
MIN_ZOOM = 0.2
MAX_ZOOM = 5.0

# Snap-to-grid settings
SNAP_ENABLED = True
SNAP_THRESHOLD = 10  # Distance in pixels to trigger snap lines

JAVA_FILE = "ClientSettingsScreen.java"

# Button dragging state
dragging_button = None
drag_offset_x = 0
drag_offset_y = 0
snap_guides = []  # List of (orientation, position) tuples for snap lines

pygame.init()
screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
pygame.display.set_caption("Minecraft UI Layout Debugger Interactive")
font = pygame.font.SysFont("Arial", 12)
clock = pygame.time.Clock()

CENTER_X = SCREEN_WIDTH // 2
CENTER_Y = SCREEN_HEIGHT // 2
view_offset_x = 0
view_offset_y = 0
dragging = False
last_mouse_pos = (0, 0)

ctx = {
    "width": SCREEN_WIDTH,
    "height": SCREEN_HEIGHT,
    "centerX": CENTER_X,
    "centerY": CENTER_Y,
}

# ==========================
# Java Parsing
# ==========================
def java_eval(expr):
    try:
        return int(eval(expr, {"__builtins__": {}}, ctx))
    except Exception:
        return 0

def extract_init_block(code):
    lines = code.splitlines()
    start = -1
    brace_depth = 0
    block = []

    for i, line in enumerate(lines):
        if "protected void init()" in line:
            start = i
            break
    if start == -1:
        return ""

    for line in lines[start:]:
        brace_depth += line.count("{")
        brace_depth -= line.count("}")
        block.append(line)
        if brace_depth == 0 and len(block) > 1:
            break
    return "\n".join(block)

def extract_render_block(code):
    """Extract the render() method block"""
    lines = code.splitlines()
    start = -1
    brace_depth = 0
    block = []

    for i, line in enumerate(lines):
        if "public void render(DrawContext context" in line:
            start = i
            break
    if start == -1:
        return ""

    for line in lines[start:]:
        brace_depth += line.count("{")
        brace_depth -= line.count("}")
        block.append(line)
        if brace_depth == 0 and len(block) > 1:
            break
    return "\n".join(block)

def extract_text_elements(render_code):
    """Extract drawTextWithShadow and drawCenteredTextWithShadow calls"""
    texts = []

    # Pattern for drawTextWithShadow: context.drawTextWithShadow(renderer, Text.literal("..."), x, y, color)
    pattern1 = r'context\.drawTextWithShadow\([^,]+,\s*Text\.literal\("([^"]+)"\)[^,]*,\s*([^,]+),\s*([^,]+),\s*[^\)]+\)'
    for match in re.finditer(pattern1, render_code):
        label = match.group(1).replace('§', '')
        x_expr = match.group(2).strip()
        y_expr = match.group(3).strip()

        texts.append({
            "type": "text",
            "x": java_eval(x_expr) - CENTER_X,
            "y": java_eval(y_expr) - CENTER_Y,
            "w": len(label) * 6,  # Approximate width
            "h": 9,  # Standard text height in Minecraft
            "label": label,
            "code": match.group(0),
            "original_code": match.group(0),
            "x_expr": x_expr,
            "y_expr": y_expr,
            "w_expr": str(len(label) * 6),
            "h_expr": "9",
            "align": "left",
            "baseline_y": True  # Y position is at baseline (bottom of text)
        })

    # Find all drawCenteredTextWithShadow calls
    pattern2 = r'context\.drawCenteredTextWithShadow\([^,]+,\s*(?:this\.)?(\w+),\s*([^,]+),\s*([^,]+),\s*[^\)]+\)'
    for match in re.finditer(pattern2, render_code):
        label_var = match.group(1)
        x_expr = match.group(2).strip()
        y_expr = match.group(3).strip()

        # Try to get the actual text if it's a variable like "this.title"
        label = label_var if label_var != "title" else "Title"

        texts.append({
            "type": "text",
            "x": java_eval(x_expr) - CENTER_X,
            "y": java_eval(y_expr) - CENTER_Y,
            "w": len(label) * 6,  # Approximate width
            "h": 9,  # Standard text height
            "label": label,
            "code": match.group(0),
            "original_code": match.group(0),
            "x_expr": x_expr,
            "y_expr": y_expr,
            "w_expr": str(len(label) * 6),
            "h_expr": "9",
            "align": "center",
            "baseline_y": True  # Y position is at baseline (bottom of text)
        })

    return texts

def extract_buttons_with_full_code(init_code):
    buttons = []
    i = 0
    while i < len(init_code):
        idx = init_code.find("new ModernButton(", i)
        if idx == -1:
            break
        start = idx
        depth = 0
        in_string = False
        escape = False
        for j in range(idx, len(init_code)):
            c = init_code[j]
            if c == '"' and not escape:
                in_string = not in_string
            if c == "\\" and in_string:
                escape = not escape
            else:
                escape = False
            if not in_string:
                if c == "(":
                    depth += 1
                elif c == ")":
                    depth -= 1
                    if depth == 0:
                        end = j + 1
                        full_code = init_code[start:end]
                        label_match = re.search(r'Text\.literal\("([^"]*?)"\s*(?:\+)?', full_code, re.DOTALL)
                        label = label_match.group(1).replace('§','') if label_match else "Button"
                        numbers = re.findall(r'\(\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^,]+)', full_code)
                        if numbers:
                            x_expr, y_expr, w_expr, h_expr = numbers[0]
                            buttons.append({
                                "x": java_eval(x_expr)-CENTER_X,
                                "y": java_eval(y_expr)-CENTER_Y,
                                "w": java_eval(w_expr),
                                "h": java_eval(h_expr),
                                "label": label,
                                "code": full_code,
                                "original_code": full_code,
                                "x_expr": x_expr.strip(),
                                "y_expr": y_expr.strip(),
                                "w_expr": w_expr.strip(),
                                "h_expr": h_expr.strip(),
                                "type": "button"
                            })
                        i = j
                        break
        i += 1
    return buttons

def extract_text_labels(render_code):
    """Extract text drawing calls from render method"""
    labels = []

    # Pattern for drawTextWithShadow and drawCenteredTextWithShadow
    patterns = [
        (r'context\.drawTextWithShadow\([^,]+,\s*Text\.literal\("([^"]+)"\)[^,]*,\s*([^,]+),\s*([^,]+),', 'left'),
        (r'context\.drawCenteredTextWithShadow\([^,]+,\s*Text\.literal\("([^"]+)"\)[^,]*,\s*([^,]+),\s*([^,]+),', 'center')
    ]

    for pattern, align in patterns:
        for match in re.finditer(pattern, render_code):
            text = match.group(1).replace('§', '')
            x_expr = match.group(2).strip()
            y_expr = match.group(3).strip()

            # Get the full line for code replacement
            line_start = render_code.rfind('\n', 0, match.start()) + 1
            line_end = render_code.find(';', match.end())
            if line_end != -1:
                full_code = render_code[line_start:line_end + 1].strip()

                labels.append({
                    "x": java_eval(x_expr) - CENTER_X,
                    "y": java_eval(y_expr) - CENTER_Y,
                    "w": 50,  # Approximate width for rendering
                    "h": 12,  # Approximate height for text
                    "label": text,
                    "code": full_code,
                    "original_code": full_code,
                    "x_expr": x_expr,
                    "y_expr": y_expr,
                    "align": align,
                    "type": "text"
                })

    return labels

def parse_java(java_path):
    with open(java_path, "r", encoding="utf-8") as f:
        code = f.read()

    init_code = extract_init_block(code)
    render_code = extract_render_block(code)

    # Extract all variable definitions (int varname = expression;)
    for name, expr in re.findall(r'int\s+(\w+)\s*=\s*([^;]+);', init_code):
        ctx[name] = java_eval(expr)

    # Also check render method for centerX/centerY redefinitions
    for name, expr in re.findall(r'int\s+(\w+)\s*=\s*([^;]+);', render_code):
        if name not in ctx or name in ['centerX', 'centerY']:
            ctx[name] = java_eval(expr)

    buttons = extract_buttons_with_full_code(init_code)
    text_labels = extract_text_labels(render_code)

    # Combine buttons and text labels
    all_elements = buttons + text_labels

    # Print extracted variables for debugging
    print(f"Extracted variables: {dict((k, v) for k, v in ctx.items() if k not in ['width', 'height'])}")
    print(f"Found {len(buttons)} buttons and {len(text_labels)} text labels")

    return all_elements

# ==========================
# Saving Changes
# ==========================
def save_buttons_to_java(elements, java_path):
    """Write modified button positions and text positions back to the Java file"""
    with open(java_path, "r", encoding="utf-8") as f:
        content = f.read()

    changes_made = 0
    # For each element, replace the old code with new code
    for elem in elements:
        if elem["original_code"] != elem["code"]:
            if elem["original_code"] in content:
                content = content.replace(elem["original_code"], elem["code"])
                changes_made += 1
                elem_type = "Text" if elem["type"] == "text" else "Button"
                print(f"  Updated {elem_type}: {elem['label']}")
            else:
                elem_type = "text" if elem["type"] == "text" else "button"
                print(f"  WARNING: Could not find original code for {elem_type} '{elem['label']}'")

    if changes_made > 0:
        with open(java_path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"✓ Saved {changes_made} element(s) to {java_path}")
    else:
        print("No changes to save (elements unchanged)")

def update_button_code(element):
    """Update the element's code string with new x,y coordinates"""
    # Convert back from center-relative to absolute
    abs_x = element["x"] + CENTER_X
    abs_y = element["y"] + CENTER_Y

    if element["type"] == "button":
        # Parse the original expressions to understand the variable being used
        x_expr = element["x_expr"]
        y_expr = element["y_expr"]

        # Rebuild expressions with new offsets
        new_x_expr = rebuild_expression(x_expr, abs_x, "X")
        new_y_expr = rebuild_expression(y_expr, abs_y, "Y")

        # Find and replace the first four numbers in the code
        pattern = r'\(\s*([^,\)]+)\s*,\s*([^,\)]+)\s*,\s*([^,\)]+)\s*,\s*([^,\)]+)\s*'
        match = re.search(pattern, element["code"])
        if match:
            # Get the full matched string
            old_full = match.group(0)
            # Create new coordinate string preserving expression format
            new_full = f"({new_x_expr}, {new_y_expr}, {element['w_expr']}, {element['h_expr']}"
            element["code"] = element["code"].replace(old_full, new_full, 1)

            # Update stored expressions for next time
            element["x_expr"] = new_x_expr
            element["y_expr"] = new_y_expr

    elif element["type"] == "text":
        # Parse the original expressions
        x_expr = element["x_expr"]
        y_expr = element["y_expr"]

        # Rebuild expressions with new offsets
        new_x_expr = rebuild_expression(x_expr, abs_x, "X")
        new_y_expr = rebuild_expression(y_expr, abs_y, "Y")

        # Replace the x, y coordinates in the text drawing call
        # Pattern matches the x, y part after Text.literal(...)
        old_x_str = x_expr
        old_y_str = y_expr

        # Replace in the code
        element["code"] = element["code"].replace(f", {old_x_str}, {old_y_str},", f", {new_x_expr}, {new_y_expr},")

        # Update stored expressions
        element["x_expr"] = new_x_expr
        element["y_expr"] = new_y_expr

def rebuild_expression(original_expr, new_absolute_value, axis):
    """
    Rebuild a Java expression preserving the variable used.
    E.g., "centerX - 424" with new value 500 and centerX=640 -> "centerX - 140"
    """
    original_expr = original_expr.strip()

    # Check if it's a simple number (no expression)
    try:
        int(original_expr)
        # It's just a number, return the new value
        return str(new_absolute_value)
    except ValueError:
        pass

    # Find the variable name (centerX, centerY, playerX, MiscX, etc.)
    var_pattern = r'(center[XY]|player[XY]|Misc[XY]|combat[XY]|movement[XY]|render[XY]|b(?:Width|Height)|btc(?:Width|Height))'
    match = re.search(var_pattern, original_expr)

    if match:
        var_name = match.group(1)

        # Get the value of this variable from context
        if var_name in ctx:
            var_value = ctx[var_name]
            # Calculate new offset
            new_offset = new_absolute_value - var_value

            # Build expression
            if new_offset == 0:
                return var_name
            elif new_offset > 0:
                return f"{var_name} + {new_offset}"
            else:
                return f"{var_name} - {abs(new_offset)}"

    # If we can't parse it, just return the absolute value
    return str(new_absolute_value)

# ==========================
# Snap-to-Grid Detection
# ==========================
def find_snap_guides(buttons, dragged_button):
    """Find snap guide lines for alignment with other buttons"""
    guides = []

    if not SNAP_ENABLED or not dragged_button:
        return guides

    # Get edges of dragged button
    d_left = dragged_button["x"]
    d_right = dragged_button["x"] + dragged_button["w"]
    d_center_x = dragged_button["x"] + dragged_button["w"] // 2
    d_top = dragged_button["y"]
    d_bottom = dragged_button["y"] + dragged_button["h"]
    d_center_y = dragged_button["y"] + dragged_button["h"] // 2

    for btn in buttons:
        if btn is dragged_button:
            continue

        # Get edges of comparison button
        b_left = btn["x"]
        b_right = btn["x"] + btn["w"]
        b_center_x = btn["x"] + btn["w"] // 2
        b_top = btn["y"]
        b_bottom = btn["y"] + btn["h"]
        b_center_y = btn["y"] + btn["h"] // 2

        # Vertical guides (align horizontally)
        if abs(d_left - b_left) < SNAP_THRESHOLD:
            guides.append(("vertical", b_left))
            dragged_button["x"] = b_left
        elif abs(d_right - b_right) < SNAP_THRESHOLD:
            guides.append(("vertical", b_right))
            dragged_button["x"] = b_right - dragged_button["w"]
        elif abs(d_center_x - b_center_x) < SNAP_THRESHOLD:
            guides.append(("vertical", b_center_x))
            dragged_button["x"] = b_center_x - dragged_button["w"] // 2
        elif abs(d_left - b_right) < SNAP_THRESHOLD:
            guides.append(("vertical", b_right))
            dragged_button["x"] = b_right
        elif abs(d_right - b_left) < SNAP_THRESHOLD:
            guides.append(("vertical", b_left))
            dragged_button["x"] = b_left - dragged_button["w"]

        # Horizontal guides (align vertically)
        if abs(d_top - b_top) < SNAP_THRESHOLD:
            guides.append(("horizontal", b_top))
            dragged_button["y"] = b_top
        elif abs(d_bottom - b_bottom) < SNAP_THRESHOLD:
            guides.append(("horizontal", b_bottom))
            dragged_button["y"] = b_bottom - dragged_button["h"]
        elif abs(d_center_y - b_center_y) < SNAP_THRESHOLD:
            guides.append(("horizontal", b_center_y))
            dragged_button["y"] = b_center_y - dragged_button["h"] // 2
        elif abs(d_top - b_bottom) < SNAP_THRESHOLD:
            guides.append(("horizontal", b_bottom))
            dragged_button["y"] = b_bottom
        elif abs(d_bottom - b_top) < SNAP_THRESHOLD:
            guides.append(("horizontal", b_top))
            dragged_button["y"] = b_top - dragged_button["h"]

    return guides

# ==========================
# Drawing
# ==========================
def draw_grid():
    for x in range(-5000, 5000, GRID_SPACING):
        sx = CENTER_X + (x + view_offset_x) * zoom_scale
        pygame.draw.line(screen, GRID_COLOR, (sx, 0), (sx, SCREEN_HEIGHT))
    for y in range(-5000, 5000, GRID_SPACING):
        sy = CENTER_Y + (y + view_offset_y) * zoom_scale
        pygame.draw.line(screen, GRID_COLOR, (0, sy), (SCREEN_WIDTH, sy))

def draw_axes():
    pygame.draw.line(
        screen, AXIS_COLOR,
        (CENTER_X + view_offset_x * zoom_scale, 0),
        (CENTER_X + view_offset_x * zoom_scale, SCREEN_HEIGHT),
        2
    )
    pygame.draw.line(
        screen, AXIS_COLOR,
        (0, CENTER_Y + view_offset_y * zoom_scale),
        (SCREEN_WIDTH, CENTER_Y + view_offset_y * zoom_scale),
        2
    )

def draw_snap_guides(guides):
    """Draw red PowerPoint-style snap guide lines"""
    for orientation, pos in guides:
        if orientation == "vertical":
            sx = CENTER_X + (pos + view_offset_x) * zoom_scale
            pygame.draw.line(screen, GUIDE_COLOR, (sx, 0), (sx, SCREEN_HEIGHT), 2)
        elif orientation == "horizontal":
            sy = CENTER_Y + (pos + view_offset_y) * zoom_scale
            pygame.draw.line(screen, GUIDE_COLOR, (0, sy), (SCREEN_WIDTH, sy), 2)

def draw_buttons(elements, mouse_pos):
    global dragging_button

    hovered_element = None
    for elem in elements:
        sx = CENTER_X + (elem["x"] + view_offset_x) * zoom_scale
        sy = CENTER_Y + (elem["y"] + view_offset_y) * zoom_scale

        # For text elements, Y is at baseline (bottom), so adjust to top-left for pygame
        if elem["type"] == "text":
            sy = sy - elem["h"] * zoom_scale  # Move up by text height

        sw = elem["w"] * zoom_scale
        sh = elem["h"] * zoom_scale
        rect = pygame.Rect(sx, sy, sw, sh)

        # Determine color based on type and state
        if elem["type"] == "text":
            # Text labels - draw as outlined text area
            if elem is dragging_button:
                color = (255, 200, 100)
                border_color = (255, 150, 0)
            elif rect.collidepoint(mouse_pos):
                hovered_element = elem
                color = (100, 100, 150)
                border_color = (150, 150, 200)
            else:
                color = (60, 60, 80, 100)
                border_color = (120, 120, 140)

            # Draw semi-transparent background for text
            s = pygame.Surface((sw, sh), pygame.SRCALPHA)
            s.fill(color)
            screen.blit(s, (sx, sy))
            pygame.draw.rect(screen, border_color, rect, 1)

            # Draw text label with alignment indicator
            text = font.render(elem["label"], True, TEXT_COLOR)
            if elem.get("align") == "center":
                screen.blit(text, text.get_rect(center=rect.center))
            else:
                screen.blit(text, (sx + 2, sy + 2))

        else:
            # Regular buttons
            if elem is dragging_button:
                color = BTN_DRAGGING
            elif rect.collidepoint(mouse_pos):
                hovered_element = elem
                color = BTN_HIGHLIGHT
            else:
                color = BTN_COLOR

            pygame.draw.rect(screen, color, rect)
            pygame.draw.rect(screen, BTN_BORDER, rect, 1)
            text = font.render(elem["label"], True, TEXT_COLOR)
            screen.blit(text, text.get_rect(center=rect.center))

    return hovered_element

def draw_popup(button, mouse_pos):
    if not button or button is dragging_button:
        return

    title_lines = [
        f"Label: {button['label']}",
        f"Center-Relative Pos: ({button['x']},{button['y']})",
        "Full Constructor:"
    ]

    code = button["code"]
    lexer = JavaLexer()
    tokens = list(lex(code, lexer))

    font_height = font.get_height()

    lines = []
    current_line = []
    for ttype, text in tokens:
        split_lines = text.split("\n")
        for i, part in enumerate(split_lines):
            if i > 0:
                lines.append(current_line)
                current_line = []
            if part != "":
                current_line.append((ttype, part))
    if current_line:
        lines.append(current_line)

    max_width = 0
    for line in lines:
        line_text = "".join([t[1] for t in line])
        w, _ = font.size(line_text)
        if w > max_width:
            max_width = w
    max_width += 10

    total_height = (len(title_lines) + len(lines)) * (font_height + 2) + 6
    px, py = mouse_pos
    popup_rect = pygame.Rect(px + 15, py - total_height - 15, max_width, total_height)

    s = pygame.Surface((popup_rect.w, popup_rect.h), pygame.SRCALPHA)
    s.fill(POPUP_BG)
    screen.blit(s, (popup_rect.x, popup_rect.y))
    pygame.draw.rect(screen, POPUP_BORDER, popup_rect, 1)

    for i, line in enumerate(title_lines):
        text_surface = font.render(line, True, TEXT_COLOR)
        screen.blit(text_surface, (popup_rect.x + 5, popup_rect.y + 3 + i * (font_height + 2)))

    for i, line in enumerate(lines):
        x_offset = 5
        y = popup_rect.y + 3 + (len(title_lines) + i) * (font_height + 2)
        for ttype, text in line:
            color = TEXT_COLOR
            if ttype in Token.Keyword:
                color = (220, 100, 50)
            elif ttype in Token.Name:
                color = (100, 200, 100)
            elif ttype in Token.Literal.String:
                color = (180, 180, 100)
            elif ttype in Token.Comment:
                color = (150, 150, 150)
            elif ttype in Token.Operator:
                color = (255, 150, 0)
            text_surface = font.render(text, True, color)
            screen.blit(text_surface, (popup_rect.x + x_offset, y))
            x_offset += text_surface.get_width()

def draw_status_bar(modified):
    """Draw status bar with save indicator"""
    status_text = "Modified - Press Ctrl+S to save" if modified else "No changes"
    status_color = (255, 200, 0) if modified else (100, 200, 100)
    text_surface = font.render(status_text, True, status_color)
    screen.blit(text_surface, (10, SCREEN_HEIGHT - 25))

    help_text = "R: Reload | Ctrl+S: Save | Drag: Move button | Scroll: Zoom | Middle-drag: Pan"
    help_surface = font.render(help_text, True, (150, 150, 150))
    screen.blit(help_surface, (10, 10))

def draw_snap_toggle_button(mouse_pos):
    """Draw toggle button for snap guides"""
    global SNAP_ENABLED

    btn_width = 120
    btn_height = 30
    btn_x = SCREEN_WIDTH - btn_width - 10
    btn_y = 10

    btn_rect = pygame.Rect(btn_x, btn_y, btn_width, btn_height)

    # Determine color based on state and hover
    if SNAP_ENABLED:
        base_color = (50, 150, 50)  # Green when enabled
        hover_color = (70, 200, 70)
    else:
        base_color = (150, 50, 50)  # Red when disabled
        hover_color = (200, 70, 70)

    color = hover_color if btn_rect.collidepoint(mouse_pos) else base_color

    # Draw button
    pygame.draw.rect(screen, color, btn_rect)
    pygame.draw.rect(screen, BTN_BORDER, btn_rect, 2)

    # Draw text
    text = f"Snap: {'ON' if SNAP_ENABLED else 'OFF'}"
    text_surface = font.render(text, True, TEXT_COLOR)
    text_rect = text_surface.get_rect(center=btn_rect.center)
    screen.blit(text_surface, text_rect)

    return btn_rect

# ==========================
# Main Loop
# ==========================
def main():
    global dragging, last_mouse_pos, view_offset_x, view_offset_y, zoom_scale
    global dragging_button, drag_offset_x, drag_offset_y, snap_guides, SNAP_ENABLED

    elements = parse_java(JAVA_FILE)
    modified = False
    snap_toggle_rect = None

    running = True
    while running:
        mouse_pos = pygame.mouse.get_pos()
        keys = pygame.key.get_pressed()

        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False

            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:  # Left click
                    # Check if clicking on snap toggle button
                    if snap_toggle_rect and snap_toggle_rect.collidepoint(event.pos):
                        SNAP_ENABLED = not SNAP_ENABLED
                        print(f"Snap guides: {'ON' if SNAP_ENABLED else 'OFF'}")
                        continue

                    # Check if clicking on an element
                    clicked_element = None
                    for elem in elements:
                        sx = CENTER_X + (elem["x"] + view_offset_x) * zoom_scale
                        sy = CENTER_Y + (elem["y"] + view_offset_y) * zoom_scale

                        # For text elements, Y is at baseline, so adjust to top-left
                        if elem["type"] == "text":
                            sy = sy - elem["h"] * zoom_scale

                        sw = elem["w"] * zoom_scale
                        sh = elem["h"] * zoom_scale
                        rect = pygame.Rect(sx, sy, sw, sh)
                        if rect.collidepoint(event.pos):
                            clicked_element = elem
                            break

                    if clicked_element:
                        # Start dragging element
                        dragging_button = clicked_element
                        # Calculate offset from element's top-left to mouse
                        sx = CENTER_X + (clicked_element["x"] + view_offset_x) * zoom_scale
                        sy = CENTER_Y + (clicked_element["y"] + view_offset_y) * zoom_scale

                        # For text, adjust Y to top-left
                        if clicked_element["type"] == "text":
                            sy = sy - clicked_element["h"] * zoom_scale

                        drag_offset_x = event.pos[0] - sx
                        drag_offset_y = event.pos[1] - sy

                elif event.button == 2:  # Middle click - pan view
                    dragging = True
                    last_mouse_pos = event.pos

            elif event.type == pygame.MOUSEBUTTONUP:
                if event.button == 1 and dragging_button:
                    # Finish dragging element
                    update_button_code(dragging_button)
                    dragging_button = None
                    snap_guides = []
                    modified = True
                elif event.button == 2:
                    dragging = False

            elif event.type == pygame.MOUSEMOTION:
                if dragging_button:
                    # Move element with mouse
                    mx, my = event.pos
                    # Convert screen position to world position
                    world_x = ((mx - drag_offset_x - CENTER_X) / zoom_scale) - view_offset_x
                    world_y = ((my - drag_offset_y - CENTER_Y) / zoom_scale) - view_offset_y

                    # For text elements, we need to add back the height since Y is at baseline
                    if dragging_button["type"] == "text":
                        world_y = world_y + dragging_button["h"]

                    dragging_button["x"] = int(world_x)
                    dragging_button["y"] = int(world_y)

                    # Check for snap guides
                    snap_guides = find_snap_guides(elements, dragging_button)

                elif dragging:
                    # Pan view
                    mx, my = event.pos
                    dx = mx - last_mouse_pos[0]
                    dy = my - last_mouse_pos[1]
                    view_offset_x += dx / zoom_scale
                    view_offset_y += dy / zoom_scale
                    last_mouse_pos = event.pos

            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_r:
                    # Reload from file
                    elements = parse_java(JAVA_FILE)
                    modified = False
                    print("Reloaded from file")

                elif event.key == pygame.K_s and (keys[pygame.K_LCTRL] or keys[pygame.K_RCTRL]):
                    # Ctrl+S - Save to file
                    if modified:
                        save_buttons_to_java(elements, JAVA_FILE)
                        # Update original_code for all elements
                        for elem in elements:
                            elem["original_code"] = elem["code"]
                        modified = False
                    else:
                        print("No changes to save")

            elif event.type == pygame.MOUSEWHEEL:
                # Zoom in/out
                if event.y > 0:
                    zoom_scale = min(MAX_ZOOM, zoom_scale * (1 + ZOOM_STEP))
                elif event.y < 0:
                    zoom_scale = max(MIN_ZOOM, zoom_scale * (1 - ZOOM_STEP))

        screen.fill(BG_COLOR)
        draw_grid()
        draw_axes()
        draw_snap_guides(snap_guides)
        hovered_element = draw_buttons(elements, mouse_pos)
        draw_popup(hovered_element, mouse_pos)
        draw_status_bar(modified)
        snap_toggle_rect = draw_snap_toggle_button(mouse_pos)

        pygame.display.flip()
        clock.tick(120)

    pygame.quit()

if __name__ == "__main__":
    main()