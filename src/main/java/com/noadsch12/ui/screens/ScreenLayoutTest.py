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
BTN_BORDER = (230, 230, 230)
TEXT_COLOR = (255, 255, 255)
GRID_COLOR = (50, 50, 50)
AXIS_COLOR = (200, 70, 70)
POPUP_BG = (30, 30, 30, 230)
POPUP_BORDER = (255, 255, 0)
GRID_SPACING = 50

zoom_scale = 1.0
ZOOM_STEP = 0.1  # How much to zoom per scroll tick
MIN_ZOOM = 0.2
MAX_ZOOM = 5.0

JAVA_FILE = "ClientSettingsScreen.java"

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

# Simplified button parser
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
                        # Extract label text BEFORE first '+'
                        label_match = re.search(r'Text\.literal\("([^"]*?)"\s*(?:\+)?', full_code, re.DOTALL)
                        label = label_match.group(1).replace('§','') if label_match else "Button"
                        # Extract first four numbers x,y,w,h
                        numbers = re.findall(r'\(\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^,]+)\s*,\s*([^,]+)', full_code)
                        if numbers:
                            x, y, w, h = numbers[0]
                            buttons.append({
                                "x": java_eval(x)-CENTER_X,
                                "y": java_eval(y)-CENTER_Y,
                                "w": java_eval(w),
                                "h": java_eval(h),
                                "label": label,
                                "code": full_code
                            })
                        i = j
                        break
        i += 1
    return buttons

def parse_java(java_path):
    with open(java_path, "r", encoding="utf-8") as f:
        code = f.read()
    init_code = extract_init_block(code)
    # Resolve variables in order
    for name, expr in re.findall(r'int\s+(\w+)\s*=\s*([^;]+);', init_code):
        ctx[name] = java_eval(expr)
    buttons = extract_buttons_with_full_code(init_code)
    return buttons

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

def draw_buttons(buttons, mouse_pos):
    hovered_button = None
    for b in buttons:
        sx = CENTER_X + (b["x"] + view_offset_x) * zoom_scale
        sy = CENTER_Y + (b["y"] + view_offset_y) * zoom_scale
        sw = b["w"] * zoom_scale
        sh = b["h"] * zoom_scale
        rect = pygame.Rect(sx, sy, sw, sh)
        if rect.collidepoint(mouse_pos):
            hovered_button = b
            color = BTN_HIGHLIGHT
        else:
            color = BTN_COLOR
        pygame.draw.rect(screen, color, rect)
        pygame.draw.rect(screen, BTN_BORDER, rect, 1)
        text = font.render(b["label"], True, TEXT_COLOR)
        screen.blit(text, text.get_rect(center=rect.center))
    return hovered_button

def draw_popup(button, mouse_pos):
    if not button:
        return

    # Lines for title and position
    title_lines = [
        f"Label: {button['label']}",
        f"Center-Relative Pos: ({button['x']},{button['y']})",
        "Full Constructor:"
    ]

    code = button["code"]
    lexer = JavaLexer()
    tokens = list(lex(code, lexer))

    font_height = font.get_height()

    # Build lines: each element is a list of (ttype, text) for that line
    lines = []
    current_line = []
    for ttype, text in tokens:
        split_lines = text.split("\n")
        for i, part in enumerate(split_lines):
            if i > 0:
                # New line starts
                lines.append(current_line)
                current_line = []
            if part != "":
                current_line.append((ttype, part))
    if current_line:
        lines.append(current_line)

    # Compute popup width
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

    # Draw background
    s = pygame.Surface((popup_rect.w, popup_rect.h), pygame.SRCALPHA)
    s.fill(POPUP_BG)
    screen.blit(s, (popup_rect.x, popup_rect.y))
    pygame.draw.rect(screen, POPUP_BORDER, popup_rect, 1)

    # Draw title lines
    for i, line in enumerate(title_lines):
        text_surface = font.render(line, True, TEXT_COLOR)
        screen.blit(text_surface, (popup_rect.x + 5, popup_rect.y + 3 + i * (font_height + 2)))

    # Draw code lines with syntax highlighting
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

# ==========================
# Main Loop
# ==========================
def main():
    global dragging, last_mouse_pos, view_offset_x, view_offset_y, zoom_scale

    buttons = parse_java(JAVA_FILE)

    running = True
    while running:
        mouse_pos = pygame.mouse.get_pos()
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:
                    dragging = True
                    last_mouse_pos = event.pos
            elif event.type == pygame.MOUSEBUTTONUP:
                if event.button == 1:
                    dragging = False
            elif event.type == pygame.MOUSEMOTION and dragging:
                mx, my = event.pos
                dx = mx - last_mouse_pos[0]
                dy = my - last_mouse_pos[1]
                view_offset_x += dx
                view_offset_y += dy
                last_mouse_pos = event.pos
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_r:
                    buttons = parse_java(JAVA_FILE)
            elif event.type == pygame.MOUSEWHEEL:
                # Zoom in/out
                if event.y > 0:
                    zoom_scale = min(MAX_ZOOM, zoom_scale * (1 + ZOOM_STEP))
                elif event.y < 0:
                    zoom_scale = max(MIN_ZOOM, zoom_scale * (1 - ZOOM_STEP))

        screen.fill(BG_COLOR)
        draw_grid()
        draw_axes()
        hovered_button = draw_buttons(buttons, mouse_pos)
        draw_popup(hovered_button, mouse_pos)

        pygame.display.flip()
        clock.tick(120)

    pygame.quit()

if __name__ == "__main__":
    main()
