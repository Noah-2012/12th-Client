import tkinter as tk
from tkinter import ttk, messagebox
import os, re, json

# ── Path resolution ───────────────────────────────────────────────────────────
SCRIPT_DIR     = os.path.dirname(os.path.abspath(__file__))
BASE_JAVA      = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))
MODULES_BASE   = os.path.join(BASE_JAVA, "modules", "impl")
TEMPLATES_FILE = os.path.join(SCRIPT_DIR, "module_templates.json")

CATEGORIES = ["combat", "misc", "movement", "player", "render"]
CATEGORY_ENUM = {c: f"Category.{c.upper()}" for c in CATEGORIES}

CAT_COLORS = {
    "combat":   "#ef4444",
    "misc":     "#f59e0b",
    "movement": "#22c55e",
    "player":   "#3b82f6",
    "render":   "#a855f7",
}

MINECRAFT_ITEMS = [
    "Items.TOTEM_OF_UNDYING", "Items.DIAMOND_SWORD", "Items.SHIELD",
    "Items.BOW", "Items.ARROW", "Items.FEATHER", "Items.COMPASS",
    "Items.CLOCK", "Items.ENDER_EYE", "Items.BLAZE_ROD",
    "Items.IRON_SWORD", "Items.GOLDEN_APPLE", "Items.APPLE",
    "Items.BREAD", "Items.COOKED_BEEF", "Items.STICK",
    "Items.BONE", "Items.SNOWBALL", "Items.EGG",
    "Items.ENDER_PEARL", "Items.NETHER_STAR", "Items.BEACON",
    "Items.EMERALD", "Items.DIAMOND", "Items.GOLD_INGOT",
    "Items.IRON_INGOT", "Items.NETHERITE_INGOT",
    "Items.LEATHER_HELMET", "Items.LEATHER_BOOTS",
    "Items.IRON_CHESTPLATE", "Items.DIAMOND_CHESTPLATE",
    "Items.TRIDENT", "Items.CROSSBOW", "Items.FISHING_ROD",
    "Items.LEAD", "Items.SPYGLASS", "Items.LANTERN",
    "Items.TORCH", "Items.REDSTONE", "Items.GLOWSTONE_DUST",
]

BUILTIN_TEMPLATES = {
    "⚔  Basic Combat": {
        "class_name": "MyCombatModule", "category": "combat",
        "name": "Combat Module", "display_name": "Combat Module",
        "tooltip": "A basic combat module", "icon_item": "Items.DIAMOND_SWORD",
        "on_enable": True, "on_disable": True, "toggle": True,
        "settings_window": False, "tick_listener": True,
    },
    "🏃  Basic Movement": {
        "class_name": "MyMovementModule", "category": "movement",
        "name": "Movement Module", "display_name": "Movement Module",
        "tooltip": "A basic movement module", "icon_item": "Items.FEATHER",
        "on_enable": True, "on_disable": True, "toggle": True,
        "settings_window": False, "tick_listener": True,
    },
    "🎨  Basic Render": {
        "class_name": "MyRenderModule", "category": "render",
        "name": "Render Module", "display_name": "Render Module",
        "tooltip": "A basic render module", "icon_item": "Items.SPYGLASS",
        "on_enable": True, "on_disable": True, "toggle": False,
        "settings_window": True, "tick_listener": False,
    },
    "🧩  Minimal Stub": {
        "class_name": "MyModule", "category": "misc",
        "name": "My Module", "display_name": "My Module",
        "tooltip": "Description", "icon_item": "Items.NETHER_STAR",
        "on_enable": False, "on_disable": False, "toggle": False,
        "settings_window": False, "tick_listener": False,
    },
}

# ── Template persistence ──────────────────────────────────────────────────────
def load_user_templates() -> dict:
    if os.path.exists(TEMPLATES_FILE):
        try:
            with open(TEMPLATES_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            pass
    return {}

def save_user_templates(templates: dict):
    with open(TEMPLATES_FILE, "w", encoding="utf-8") as f:
        json.dump(templates, f, indent=2)

# ── Project scanning ──────────────────────────────────────────────────────────
def scan_existing_modules() -> dict:
    result = {c: [] for c in CATEGORIES}
    if not os.path.isdir(MODULES_BASE):
        return result
    for cat in CATEGORIES:
        cat_dir = os.path.join(MODULES_BASE, cat)
        if not os.path.isdir(cat_dir):
            continue
        for fname in sorted(os.listdir(cat_dir)):
            if not fname.endswith(".java"):
                continue
            path = os.path.join(cat_dir, fname)
            result[cat].append(parse_module_file(path, cat))
    return result

def parse_module_file(path: str, category: str) -> dict:
    try:
        with open(path, "r", encoding="utf-8") as f:
            src = f.read()
    except Exception:
        src = ""
    class_name = os.path.splitext(os.path.basename(path))[0]
    m = re.search(
        r'super\s*\(\s*"([^"]*)"\s*,\s*"([^"]*)"\s*[^,]*,[^,]*,\s*"([^"]*)"\s*,\s*([^)]+)\)',
        src, re.DOTALL)
    return {
        "class_name":      class_name,
        "category":        category,
        "name":            m.group(1).strip() if m else class_name,
        "display_name":    m.group(2).strip() if m else class_name,
        "tooltip":         m.group(3).strip() if m else "",
        "icon_item":       m.group(4).strip() if m else "Items.TOTEM_OF_UNDYING",
        "on_enable":       bool(re.search(r'void\s+onEnable\s*\(',      src)),
        "on_disable":      bool(re.search(r'void\s+onDisable\s*\(',     src)),
        "toggle":          bool(re.search(r'void\s+toggle\s*\(',        src)),
        "settings_window": bool(re.search(r'createSettingsWindow\s*\(', src)),
        "tick_listener":   bool(re.search(r'TickListener',              src)),
        "path":            path,
    }

def class_exists(class_name: str) -> str | None:
    for cat in CATEGORIES:
        p = os.path.join(MODULES_BASE, cat, f"{class_name}.java")
        if os.path.isfile(p):
            return p
    return None

# ── Code generation ───────────────────────────────────────────────────────────
def generate_code(cfg: dict) -> str:
    cat_lower  = cfg["category"].lower()
    class_name = cfg["class_name"]
    pkg        = f"com.noadsch12.modules.impl.{cat_lower}"
    cat_enum   = CATEGORY_ENUM[cat_lower]
    impl_str   = "TickListener" if cfg["tick_listener"] else ""
    class_decl = (f"public class {class_name} extends Module"
                  + (f" implements {impl_str}" if impl_str else ""))

    imports = ["import com.noadsch12.modules.Category;",
               "import com.noadsch12.modules.Module;"]
    if cfg["tick_listener"]:
        imports += ["import com.noadsch12.event.events.TickEvent;",
                    "import com.noadsch12.event.listeners.TickListener;"]
    if cfg["settings_window"]:
        imports.append("import com.noadsch12.ui.GLWindow;")
    icon_item  = cfg["icon_item"]
    item_class = icon_item.split(".")[0] if "." in icon_item else "Items"
    imports.append(f"import net.minecraft.item.{item_class};")

    ctor = (f'    public {class_name}() {{\n'
            f'        super("{cfg["name"]}", "{cfg["display_name"]}", {cat_enum},\n'
            f'                "{cfg["tooltip"]}", {icon_item});\n'
            f'    }}')

    methods = []
    if cfg["on_enable"]:
        methods.append('    /** Called when the module is toggled on */\n'
                       '    @Override\n    protected void onEnable() {\n'
                       '        // TODO: implement onEnable\n    }')
    if cfg["on_disable"]:
        methods.append('    /** Called when the module is toggled off */\n'
                       '    @Override\n    protected void onDisable() {\n'
                       '        // TODO: implement onDisable\n    }')
    if cfg["settings_window"]:
        methods.append('    /* Override for an optional Settings Screen */\n'
                       '    @Override\n    protected GLWindow createSettingsWindow() {\n'
                       '        return null; // TODO: return your settings window\n    }')
    if cfg["toggle"]:
        methods.append('    /** Toggle the module on/off */\n'
                       '    @Override\n    public void toggle() {\n'
                       '        // TODO: implement toggle\n    }')
    if cfg["tick_listener"]:
        methods.append('    @Override\n    public void onTick(TickEvent.Pre event) {\n'
                       '        // TODO: handle pre-tick\n    }\n\n'
                       '    @Override\n    public void onTick(TickEvent.Post event) {\n'
                       '        // TODO: handle post-tick\n    }')

    body = "\n\n".join([ctor] + methods)
    return (f"package {pkg};\n\n"
            f"{chr(10).join(sorted(set(imports)))}\n\n"
            f"{class_decl} {{\n\n{body}\n}}\n")

def save_module(cfg: dict) -> str:
    out_dir = os.path.join(MODULES_BASE, cfg["category"].lower())
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, f"{cfg['class_name']}.java")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(generate_code(cfg))
    return out_path

# ═════════════════════════════════════════════════════════════════════════════
#  GUI
# ═════════════════════════════════════════════════════════════════════════════
class ModuleCreatorApp(tk.Tk):
    BG       = "#0b0e16"
    PANEL    = "#10141f"
    PANEL2   = "#141926"
    BORDER   = "#1e2638"
    ACCENT   = "#4f8ef7"
    ACCENT2  = "#8b5cf6"
    TEXT     = "#dde4f0"
    MUTED    = "#4a5568"
    MUTED2   = "#64748b"
    SUCCESS  = "#22c55e"
    WARN     = "#f59e0b"
    ERROR    = "#ef4444"
    ENTRY_BG = "#181e2e"
    HOVER    = "#1e2d4a"

    FH = ("Consolas", 13, "bold")
    FB = ("Consolas", 10)
    FM = ("Consolas", 9)
    FS = ("Consolas", 8)

    def __init__(self):
        super().__init__()
        self.title("Fabric Module Creator  ·  noadsch12")
        self.configure(bg=self.BG)
        self.resizable(True, True)
        self.minsize(1000, 660)

        self._user_templates: dict = load_user_templates()
        self._existing: dict       = {}
        self._status_after         = None

        self._setup_styles()
        self._build_ui()
        self._refresh_existing()
        self._refresh_preview()

        self.update_idletasks()
        w, h = 1200, 780
        x = (self.winfo_screenwidth()  - w) // 2
        y = (self.winfo_screenheight() - h) // 2
        self.geometry(f"{w}x{h}+{x}+{y}")

    def _setup_styles(self):
        s = ttk.Style(self)
        s.theme_use("clam")
        s.configure("TFrame",       background=self.BG,    foreground=self.TEXT, font=self.FB)
        s.configure("Panel.TFrame", background=self.PANEL, foreground=self.TEXT, font=self.FB)
        s.configure("TLabel",       background=self.BG,    foreground=self.TEXT, font=self.FB)
        s.configure("Panel.TLabel", background=self.PANEL, foreground=self.TEXT, font=self.FB)
        s.configure("TCombobox",
                    fieldbackground=self.ENTRY_BG, background=self.ENTRY_BG,
                    foreground=self.TEXT, selectbackground=self.ACCENT,
                    font=self.FB, arrowcolor=self.MUTED2)
        s.map("TCombobox",
              fieldbackground=[("readonly", self.ENTRY_BG)],
              foreground      =[("readonly", self.TEXT)],
              selectbackground=[("readonly", self.ACCENT)])
        s.configure("TCheckbutton",
                    background=self.PANEL, foreground=self.TEXT,
                    font=self.FB, indicatorcolor=self.ENTRY_BG,
                    selectcolor=self.ACCENT, focuscolor=self.PANEL)
        s.map("TCheckbutton",
              background=[("active", self.PANEL)],
              foreground=[("active", self.ACCENT)])
        s.configure("Treeview",
                    background=self.PANEL2, foreground=self.TEXT,
                    fieldbackground=self.PANEL2, font=self.FM,
                    rowheight=24, borderwidth=0)
        s.configure("Treeview.Heading",
                    background=self.BORDER, foreground=self.MUTED2,
                    font=("Consolas", 8, "bold"), relief="flat")
        s.map("Treeview",
              background=[("selected", self.HOVER)],
              foreground=[("selected", self.ACCENT)])
        s.configure("Dark.TNotebook",     background=self.BG, borderwidth=0, tabmargins=0)
        s.configure("Dark.TNotebook.Tab",
                    background=self.PANEL, foreground=self.MUTED2,
                    font=("Consolas", 9, "bold"), padding=(16, 7), borderwidth=0)
        s.map("Dark.TNotebook.Tab",
              background=[("selected", self.BG)],
              foreground=[("selected", self.ACCENT)])

    # ── top-level UI ──────────────────────────────────────────────────────────
    def _build_ui(self):
        bar = tk.Frame(self, bg=self.BG)
        bar.pack(fill="x")
        tk.Canvas(bar, height=2, bg=self.ACCENT, highlightthickness=0).pack(fill="x")
        inner = tk.Frame(bar, bg=self.BG)
        inner.pack(fill="x", padx=18, pady=10)
        tk.Label(inner, text="⬡  FABRIC MODULE CREATOR",
                 bg=self.BG, fg=self.ACCENT,
                 font=("Consolas", 15, "bold")).pack(side="left")
        tk.Label(inner, text="noadsch12  ·  1.21.10",
                 bg=self.BG, fg=self.MUTED, font=self.FS).pack(side="right", pady=3)

        nb = ttk.Notebook(self, style="Dark.TNotebook")
        nb.pack(fill="both", expand=True)
        self._nb = nb

        t1 = tk.Frame(nb, bg=self.BG)
        t2 = tk.Frame(nb, bg=self.BG)
        t3 = tk.Frame(nb, bg=self.BG)
        nb.add(t1, text="  ✦  CREATE MODULE  ")
        nb.add(t2, text="  ◈  PROJECT MODULES  ")
        nb.add(t3, text="  ▤  TEMPLATES  ")

        self._build_creator_tab(t1)
        self._build_loader_tab(t2)
        self._build_templates_tab(t3)

    # ═══════════════════════════════════
    #  TAB 1 – Creator
    # ═══════════════════════════════════
    def _build_creator_tab(self, parent):
        pane = tk.PanedWindow(parent, orient="horizontal",
                              bg=self.BORDER, sashwidth=3, handlesize=0)
        pane.pack(fill="both", expand=True)
        left  = tk.Frame(pane, bg=self.BG)
        right = tk.Frame(pane, bg=self.BG)
        pane.add(left,  minsize=380, width=460)
        pane.add(right, minsize=340)
        self._build_form(left)
        self._build_preview(right)

    def _build_form(self, parent):
        canvas = tk.Canvas(parent, bg=self.BG, highlightthickness=0)
        sb = tk.Scrollbar(parent, orient="vertical", command=canvas.yview,
                          bg=self.BG, troughcolor=self.BG,
                          activebackground=self.ACCENT)
        canvas.configure(yscrollcommand=sb.set)
        sb.pack(side="right", fill="y")
        canvas.pack(side="left", fill="both", expand=True)
        frame = tk.Frame(canvas, bg=self.BG)
        win   = canvas.create_window((0, 0), window=frame, anchor="nw")
        canvas.bind("<Configure>", lambda e: canvas.itemconfig(win, width=e.width))
        frame.bind("<Configure>",  lambda e: canvas.configure(scrollregion=canvas.bbox("all")))
        canvas.bind_all("<MouseWheel>",
                        lambda e: canvas.yview_scroll(-1*(e.delta//120), "units"))

        p = 16

        # ── Class Definition ──────────────────────────────────────────────────
        self._section(frame, "CLASS DEFINITION", p)

        self._field_label(frame, "Class Name", p)
        row = tk.Frame(frame, bg=self.BG)
        row.pack(fill="x", padx=p, pady=(0, 6))
        self.var_class = tk.StringVar(value="MyModule")
        e = tk.Entry(row, textvariable=self.var_class,
                     bg=self.ENTRY_BG, fg=self.TEXT,
                     insertbackground=self.ACCENT, relief="flat",
                     font=self.FB, highlightthickness=1,
                     highlightcolor=self.ACCENT, highlightbackground=self.BORDER)
        e.pack(side="left", fill="x", expand=True, ipady=5)
        e.bind("<KeyRelease>", lambda _: self._on_form_change())

        self.lbl_dup = tk.Label(row, text="", bg=self.BG,
                                font=("Consolas", 8, "bold"), width=16, anchor="w")
        self.lbl_dup.pack(side="left", padx=(8, 0))

        # Category pill buttons
        self._field_label(frame, "Category", p)
        cat_row = tk.Frame(frame, bg=self.BG)
        cat_row.pack(fill="x", padx=p, pady=(0, 10))
        self.var_category = tk.StringVar(value=CATEGORIES[0])
        self.cat_btns: dict[str, tk.Button] = {}
        for cat in CATEGORIES:
            btn = tk.Button(cat_row, text=cat.upper(), relief="flat",
                            font=("Consolas", 8, "bold"), cursor="hand2",
                            padx=10, pady=5,
                            command=lambda c=cat: self._select_category(c))
            btn.pack(side="left", padx=(0, 5))
            self.cat_btns[cat] = btn
        self._select_category(CATEGORIES[0], init=True)

        # ── super() Arguments ─────────────────────────────────────────────────
        self._section(frame, "SUPER() ARGUMENTS", p)

        self.var_name    = tk.StringVar(value="My Module")
        self.var_display = tk.StringVar(value="My Module")
        self.var_tooltip = tk.StringVar(value="A brief description")
        for label, var in [("Module Name",  self.var_name),
                            ("Display Name", self.var_display),
                            ("Tooltip",      self.var_tooltip)]:
            self._field_label(frame, label, p)
            e2 = tk.Entry(frame, textvariable=var,
                          bg=self.ENTRY_BG, fg=self.TEXT,
                          insertbackground=self.ACCENT, relief="flat",
                          font=self.FB, highlightthickness=1,
                          highlightcolor=self.ACCENT, highlightbackground=self.BORDER)
            e2.pack(fill="x", padx=p, pady=(0, 4), ipady=5)
            e2.bind("<KeyRelease>", lambda _: self._on_form_change())

        self._field_label(frame, "Icon Item", p)
        self.var_icon = tk.StringVar(value=MINECRAFT_ITEMS[0])
        icon_cb = ttk.Combobox(frame, textvariable=self.var_icon,
                               values=MINECRAFT_ITEMS, font=self.FB)
        icon_cb.pack(fill="x", padx=p, pady=(0, 10))
        icon_cb.bind("<<ComboboxSelected>>", lambda _: self._on_form_change())
        icon_cb.bind("<KeyRelease>",         lambda _: self._on_form_change())

        # ── Overrides ─────────────────────────────────────────────────────────
        self._section(frame, "OVERRIDES", p)
        self.bool_on_enable     = tk.BooleanVar(value=True)
        self.bool_on_disable    = tk.BooleanVar(value=True)
        self.bool_toggle        = tk.BooleanVar(value=True)
        self.bool_settings      = tk.BooleanVar(value=False)
        self.bool_tick_listener = tk.BooleanVar(value=False)
        for var, label in [(self.bool_on_enable,     "onEnable()"),
                           (self.bool_on_disable,    "onDisable()"),
                           (self.bool_toggle,        "toggle()"),
                           (self.bool_settings,      "createSettingsWindow()"),
                           (self.bool_tick_listener, "TickListener  (onTick Pre + Post)")]:
            ttk.Checkbutton(frame, text=f"  {label}", variable=var,
                            command=self._on_form_change).pack(
                                anchor="w", padx=p+2, pady=3)

        tk.Frame(frame, bg=self.BG, height=10).pack()

        # ── Buttons ───────────────────────────────────────────────────────────
        btn_row = tk.Frame(frame, bg=self.BG)
        btn_row.pack(fill="x", padx=p, pady=(4, 6))
        self._btn(btn_row, "⬡  GENERATE FILE",  self._generate,
                  self.ACCENT,  "#0e1c33").pack(side="left", fill="x", expand=True, padx=(0,5))
        self._btn(btn_row, "⎘  COPY CODE",      self._copy_code,
                  self.ACCENT2, "#160f33").pack(side="left", fill="x", expand=True, padx=(0,5))
        self._btn(btn_row, "▤  SAVE TEMPLATE",  self._save_template_dialog,
                  self.WARN,    "#201a08").pack(side="left", fill="x", expand=True)

        self.lbl_status = tk.Label(frame, text="", bg=self.BG,
                                   fg=self.SUCCESS, font=self.FB,
                                   wraplength=420, justify="left")
        self.lbl_status.pack(padx=p, pady=(6, 16))

    def _build_preview(self, parent):
        hdr = tk.Frame(parent, bg=self.PANEL)
        hdr.pack(fill="x")
        tk.Label(hdr, text="  ◈  LIVE PREVIEW", bg=self.PANEL, fg=self.ACCENT,
                 font=("Consolas", 9, "bold")).pack(side="left", pady=8)
        self.lbl_preview_path = tk.Label(hdr, text="", bg=self.PANEL,
                                         fg=self.MUTED2, font=self.FM)
        self.lbl_preview_path.pack(side="right", padx=12)

        self.preview = tk.Text(parent, bg="#080b12", fg="#adbbd4",
                               insertbackground=self.ACCENT, font=self.FM,
                               relief="flat", borderwidth=0, padx=14, pady=12,
                               wrap="none", state="disabled")
        vsb = tk.Scrollbar(parent, orient="vertical",   command=self.preview.yview,
                           bg=self.BG, troughcolor=self.BG, activebackground=self.ACCENT)
        hsb = tk.Scrollbar(parent, orient="horizontal", command=self.preview.xview,
                           bg=self.BG, troughcolor=self.BG, activebackground=self.ACCENT)
        self.preview.configure(yscrollcommand=vsb.set, xscrollcommand=hsb.set)
        vsb.pack(side="right",  fill="y")
        hsb.pack(side="bottom", fill="x")
        self.preview.pack(fill="both", expand=True)
        self.preview.tag_configure("kw",      foreground="#569cd6")
        self.preview.tag_configure("pkg",     foreground="#4ec9b0")
        self.preview.tag_configure("import",  foreground="#c586c0")
        self.preview.tag_configure("string",  foreground="#ce9178")
        self.preview.tag_configure("comment", foreground="#6a9955")
        self.preview.tag_configure("annot",   foreground="#dcdcaa")

    # ═══════════════════════════════════
    #  TAB 2 – Module Loader
    # ═══════════════════════════════════
    def _build_loader_tab(self, parent):
        # header
        hdr = tk.Frame(parent, bg=self.PANEL)
        hdr.pack(fill="x")
        tk.Label(hdr, text="  ◈  PROJECT MODULES", bg=self.PANEL, fg=self.ACCENT,
                 font=("Consolas", 9, "bold")).pack(side="left", pady=8)
        self._btn(hdr, "↺  REFRESH", self._refresh_existing,
                  self.MUTED2, self.PANEL).pack(side="right", padx=10, pady=5)

        # stats bar
        self.lbl_stats = tk.Label(parent, text="", bg=self.BG, fg=self.MUTED2, font=self.FS)
        self.lbl_stats.pack(anchor="w", padx=12, pady=(6, 2))

        # filter pills
        filter_row = tk.Frame(parent, bg=self.BG)
        filter_row.pack(fill="x", padx=10, pady=(2, 8))
        tk.Label(filter_row, text="Filter:", bg=self.BG, fg=self.MUTED2, font=self.FS).pack(
            side="left", padx=(0, 8))
        self.loader_filter = tk.StringVar(value="all")
        self.filter_btns: dict[str, tk.Button] = {}
        for label, val in [("ALL", "all")] + [(c.upper(), c) for c in CATEGORIES]:
            btn = tk.Button(filter_row, text=label, relief="flat",
                            font=("Consolas", 8, "bold"), cursor="hand2",
                            padx=9, pady=4,
                            command=lambda v=val: self._apply_loader_filter(v))
            btn.pack(side="left", padx=(0, 4))
            self.filter_btns[val] = btn
        self._apply_loader_filter("all", init=True)

        # treeview
        tree_wrap = tk.Frame(parent, bg=self.BG)
        tree_wrap.pack(fill="both", expand=True, padx=10, pady=(0, 0))
        cols = ("category", "class", "name", "overrides", "path")
        self.loader_tree = ttk.Treeview(tree_wrap, columns=cols,
                                        show="headings", selectmode="browse")
        for col, w, txt in [("category", 90, "Category"), ("class", 180, "Class Name"),
                             ("name", 150, "Module Name"), ("overrides", 200, "Overrides"),
                             ("path", 300, "File Path")]:
            self.loader_tree.heading(col, text=txt)
            self.loader_tree.column(col, width=w, minwidth=50)
        vsb2 = ttk.Scrollbar(tree_wrap, orient="vertical", command=self.loader_tree.yview)
        self.loader_tree.configure(yscrollcommand=vsb2.set)
        vsb2.pack(side="right", fill="y")
        self.loader_tree.pack(fill="both", expand=True)
        for cat, col in CAT_COLORS.items():
            self.loader_tree.tag_configure(cat, foreground=col)

        # bottom bar
        bot = tk.Frame(parent, bg=self.PANEL)
        bot.pack(fill="x")
        self.lbl_loader_sel = tk.Label(bot, text="  Double-click or select and press Load.",
                                       bg=self.PANEL, fg=self.MUTED2, font=self.FS)
        self.lbl_loader_sel.pack(side="left", pady=9)
        self._btn(bot, "⬡  LOAD INTO CREATOR", self._load_selected_module,
                  self.ACCENT, "#0e1c33").pack(side="right", padx=10, pady=6)

        self.loader_tree.bind("<<TreeviewSelect>>", self._on_tree_select)
        self.loader_tree.bind("<Double-1>",         lambda _: self._load_selected_module())

    def _apply_loader_filter(self, val: str, init=False):
        self.loader_filter.set(val)
        for v, btn in self.filter_btns.items():
            col = CAT_COLORS.get(v, self.ACCENT)
            if v == val:
                btn.configure(bg=col, fg="#fff",
                              activebackground=col, activeforeground="#fff")
            else:
                btn.configure(bg=self.BORDER, fg=self.MUTED2,
                              activebackground=self.HOVER, activeforeground=self.TEXT)
        if not init:
            self._populate_loader_tree()

    def _populate_loader_tree(self):
        for r in self.loader_tree.get_children():
            self.loader_tree.delete(r)
        filt  = self.loader_filter.get()
        count = 0
        for cat, modules in self._existing.items():
            if filt not in ("all", cat):
                continue
            for m in modules:
                overrides = []
                if m["on_enable"]:      overrides.append("onEnable")
                if m["on_disable"]:     overrides.append("onDisable")
                if m["toggle"]:         overrides.append("toggle")
                if m["settings_window"]:overrides.append("settings")
                if m["tick_listener"]:  overrides.append("TickListener")
                ov_str = ", ".join(overrides) if overrides else "—"
                short  = (os.path.relpath(m["path"], BASE_JAVA)
                          if os.path.exists(m.get("path", "")) else "")
                self.loader_tree.insert("", "end", tags=(cat,),
                    values=(cat.upper(), m["class_name"], m["name"], ov_str, short))
                count += 1

        total_all = sum(len(v) for v in self._existing.values())
        self.lbl_stats.configure(
            text=f"  {total_all} module(s) total across all categories  ·  showing {count}")

    def _on_tree_select(self, _=None):
        sel = self.loader_tree.selection()
        if sel:
            v = self.loader_tree.item(sel[0], "values")
            self.lbl_loader_sel.configure(
                text=f"  Selected: {v[1]}  ({v[0]})  —  {v[3]}")

    def _load_selected_module(self):
        sel = self.loader_tree.selection()
        if not sel:
            self._status("⚠  Select a module first.", self.WARN); return
        v   = self.loader_tree.item(sel[0], "values")
        cat, cname = v[0].lower(), v[1]
        cfg = next((m for m in self._existing.get(cat, [])
                    if m["class_name"] == cname), None)
        if cfg is None:
            self._status("⚠  Could not locate module data.", self.WARN); return
        self._apply_cfg(cfg)
        self._nb.select(0)
        self._status(f"✓  Loaded '{cname}' into the creator.", self.SUCCESS)

    # ═══════════════════════════════════
    #  TAB 3 – Templates
    # ═══════════════════════════════════
    def _build_templates_tab(self, parent):
        pane = tk.PanedWindow(parent, orient="horizontal",
                              bg=self.BORDER, sashwidth=3, handlesize=0)
        pane.pack(fill="both", expand=True)
        left  = tk.Frame(pane, bg=self.BG)
        right = tk.Frame(pane, bg=self.BG)
        pane.add(left,  minsize=260, width=320)
        pane.add(right, minsize=340)

        tk.Label(left, text="  ▤  TEMPLATES", bg=self.PANEL, fg=self.ACCENT,
                 font=("Consolas", 9, "bold")).pack(fill="x", pady=8)
        self.tpl_listbox = tk.Listbox(left,
                                      bg=self.PANEL2, fg=self.TEXT,
                                      selectbackground=self.HOVER,
                                      selectforeground=self.ACCENT,
                                      font=self.FB, relief="flat",
                                      borderwidth=0, activestyle="none",
                                      highlightthickness=0)
        self.tpl_listbox.pack(fill="both", expand=True, padx=8, pady=(0, 8))
        self.tpl_listbox.bind("<<ListboxSelect>>", self._on_tpl_select)
        self.tpl_listbox.bind("<Double-1>",        lambda _: self._load_template())

        btn_row = tk.Frame(left, bg=self.BG)
        btn_row.pack(fill="x", padx=8, pady=(0, 10))
        self._btn(btn_row, "⬡  LOAD",   self._load_template,
                  self.ACCENT,  "#0e1c33").pack(side="left", fill="x", expand=True, padx=(0,5))
        self._btn(btn_row, "✕  DELETE", self._delete_template,
                  self.ERROR,   "#200c0c").pack(side="left", fill="x", expand=True)

        tk.Label(right, text="  ◈  DETAILS", bg=self.PANEL, fg=self.ACCENT,
                 font=("Consolas", 9, "bold")).pack(fill="x", pady=8)
        self.tpl_info = tk.Text(right, bg=self.PANEL2, fg=self.TEXT,
                                font=self.FM, relief="flat", borderwidth=0,
                                padx=14, pady=12, state="disabled",
                                wrap="word", highlightthickness=0)
        self.tpl_info.pack(fill="both", expand=True, padx=8, pady=(0, 10))
        self.tpl_info.tag_configure("heading", foreground=self.ACCENT,
                                    font=("Consolas", 9, "bold"))
        self.tpl_info.tag_configure("check",   foreground=self.SUCCESS)
        self.tpl_info.tag_configure("cross",   foreground=self.MUTED2)
        self.tpl_info.tag_configure("val",     foreground=self.WARN)

        self._populate_template_list()

    def _populate_template_list(self):
        self.tpl_listbox.delete(0, "end")
        self._tpl_keys: list[tuple] = []
        for name in BUILTIN_TEMPLATES:
            self.tpl_listbox.insert("end", f"  {name}")
            self._tpl_keys.append(("builtin", name))
        if self._user_templates:
            self.tpl_listbox.insert("end", "  ─── My Templates ───")
            self._tpl_keys.append(("sep", ""))
            for name in self._user_templates:
                self.tpl_listbox.insert("end", f"  💾  {name}")
                self._tpl_keys.append(("user", name))

    def _on_tpl_select(self, _=None):
        idx = self.tpl_listbox.curselection()
        if not idx:
            return
        kind, name = self._tpl_keys[idx[0]]
        if kind == "sep":
            return
        cfg = (BUILTIN_TEMPLATES if kind == "builtin" else self._user_templates)[name]
        t = self.tpl_info
        t.configure(state="normal")
        t.delete("1.0", "end")
        t.insert("end", f"  {name}\n\n", "heading")
        for k, label in [("class_name","Class"), ("category","Category"),
                          ("name","Name"), ("display_name","Display"),
                          ("tooltip","Tooltip"), ("icon_item","Icon")]:
            t.insert("end", f"  {label:<12}")
            t.insert("end", f"{cfg.get(k,'')}\n", "val")
        t.insert("end", "\n  Overrides\n", "heading")
        for k, label in [("on_enable","onEnable"), ("on_disable","onDisable"),
                          ("toggle","toggle"),
                          ("settings_window","createSettingsWindow"),
                          ("tick_listener","TickListener")]:
            tick = "  ✔  " if cfg.get(k) else "  ✘  "
            tag  = "check" if cfg.get(k) else "cross"
            t.insert("end", tick, tag)
            t.insert("end", f"{label}\n")
        t.configure(state="disabled")

    def _load_template(self):
        idx = self.tpl_listbox.curselection()
        if not idx:
            return
        kind, name = self._tpl_keys[idx[0]]
        if kind == "sep":
            return
        cfg = (BUILTIN_TEMPLATES if kind == "builtin" else self._user_templates)[name]
        self._apply_cfg(cfg)
        self._nb.select(0)
        self._status(f"✓  Template '{name}' loaded.", self.SUCCESS)

    def _delete_template(self):
        idx = self.tpl_listbox.curselection()
        if not idx:
            return
        kind, name = self._tpl_keys[idx[0]]
        if kind == "builtin":
            self._status("⚠  Built-in templates cannot be deleted.", self.WARN); return
        if kind == "sep":
            return
        if messagebox.askyesno("Delete Template", f"Delete template '{name}'?"):
            del self._user_templates[name]
            save_user_templates(self._user_templates)
            self._populate_template_list()
            self._status(f"✓  Template '{name}' deleted.", self.SUCCESS)

    def _save_template_dialog(self):
        dlg = tk.Toplevel(self)
        dlg.title("Save as Template")
        dlg.configure(bg=self.BG)
        dlg.resizable(False, False)
        dlg.transient(self)
        dlg.grab_set()

        tk.Label(dlg, text="Template name:", bg=self.BG, fg=self.TEXT, font=self.FB).pack(
            padx=22, pady=(20, 4))
        var = tk.StringVar()
        e = tk.Entry(dlg, textvariable=var, bg=self.ENTRY_BG, fg=self.TEXT,
                     insertbackground=self.ACCENT, relief="flat", font=self.FB,
                     highlightthickness=1, highlightcolor=self.ACCENT,
                     highlightbackground=self.BORDER, width=32)
        e.pack(padx=22, ipady=6)
        e.focus_set()
        err = tk.Label(dlg, text="", bg=self.BG, fg=self.ERROR, font=self.FS)
        err.pack(pady=(3, 0))

        def _save():
            name = var.get().strip()
            if not name:
                err.configure(text="Name cannot be empty."); return
            if name in BUILTIN_TEMPLATES:
                err.configure(text="Conflicts with a built-in template."); return
            cfg = self._get_cfg()
            cfg.pop("path", None)
            self._user_templates[name] = cfg
            save_user_templates(self._user_templates)
            self._populate_template_list()
            dlg.destroy()
            self._status(f"✓  Template '{name}' saved.", self.SUCCESS)

        row = tk.Frame(dlg, bg=self.BG)
        row.pack(pady=16, padx=22)
        self._btn(row, "SAVE",   _save,       self.ACCENT,  "#0e1c33").pack(side="left", ipadx=14, padx=(0,8))
        self._btn(row, "CANCEL", dlg.destroy, self.MUTED2,  self.BORDER).pack(side="left", ipadx=14)
        e.bind("<Return>", lambda _: _save())

        dlg.update_idletasks()
        dlg.geometry(f"+{self.winfo_x()+(self.winfo_width()-dlg.winfo_width())//2}"
                     f"+{self.winfo_y()+(self.winfo_height()-dlg.winfo_height())//2}")

    # ═══════════════════════════════════
    #  Shared helpers
    # ═══════════════════════════════════
    def _section(self, parent, title, p):
        f = tk.Frame(parent, bg=self.BG)
        f.pack(fill="x", padx=p, pady=(14, 5))
        tk.Label(f, text=title, bg=self.BG, fg=self.ACCENT2,
                 font=("Consolas", 8, "bold")).pack(side="left")
        tk.Frame(f, bg=self.BORDER, height=1).pack(
            side="left", fill="x", expand=True, padx=(8, 0), pady=(1, 0))

    def _field_label(self, parent, text, p):
        tk.Label(parent, text=text, bg=self.BG, fg=self.MUTED2,
                 font=self.FS).pack(anchor="w", padx=p, pady=(4, 1))

    def _btn(self, parent, text, cmd, fg, bg) -> tk.Button:
        return tk.Button(parent, text=text, command=cmd,
                         bg=bg, fg=fg, activebackground=self.HOVER,
                         activeforeground=fg, relief="flat",
                         font=("Consolas", 9, "bold"), cursor="hand2",
                         pady=7, highlightthickness=1, highlightbackground=fg)

    def _select_category(self, cat: str, init=False):
        if init:
            self.var_category = tk.StringVar(value=cat)
        else:
            self.var_category.set(cat)
        for c, btn in self.cat_btns.items():
            col = CAT_COLORS[c]
            if c == cat:
                btn.configure(bg=col, fg="#fff",
                              activebackground=col, activeforeground="#fff")
            else:
                btn.configure(bg=self.BORDER, fg=self.MUTED2,
                              activebackground=self.HOVER, activeforeground=self.TEXT)
        if not init:
            self._on_form_change()

    def _get_cfg(self) -> dict:
        return {
            "class_name":      self.var_class.get().strip()   or "MyModule",
            "category":        self.var_category.get(),
            "name":            self.var_name.get().strip()    or "My Module",
            "display_name":    self.var_display.get().strip() or "My Module",
            "tooltip":         self.var_tooltip.get().strip() or "",
            "icon_item":       self.var_icon.get().strip()    or "Items.TOTEM_OF_UNDYING",
            "on_enable":       self.bool_on_enable.get(),
            "on_disable":      self.bool_on_disable.get(),
            "toggle":          self.bool_toggle.get(),
            "settings_window": self.bool_settings.get(),
            "tick_listener":   self.bool_tick_listener.get(),
        }

    def _apply_cfg(self, cfg: dict):
        self.var_class.set(cfg.get("class_name", "MyModule"))
        self._select_category(cfg.get("category", CATEGORIES[0]))
        self.var_name.set(cfg.get("name", ""))
        self.var_display.set(cfg.get("display_name", ""))
        self.var_tooltip.set(cfg.get("tooltip", ""))
        self.var_icon.set(cfg.get("icon_item", MINECRAFT_ITEMS[0]))
        self.bool_on_enable.set(cfg.get("on_enable", True))
        self.bool_on_disable.set(cfg.get("on_disable", True))
        self.bool_toggle.set(cfg.get("toggle", True))
        self.bool_settings.set(cfg.get("settings_window", False))
        self.bool_tick_listener.set(cfg.get("tick_listener", False))
        self._on_form_change()

    def _on_form_change(self):
        self._check_duplicate()
        self._refresh_preview()

    def _check_duplicate(self):
        name = self.var_class.get().strip()
        if not name:
            self.lbl_dup.configure(text="", bg=self.BG); return
        existing = class_exists(name)
        if existing:
            # figure out which category
            for cat in CATEGORIES:
                if f"{os.sep}{cat}{os.sep}" in existing or f"/{cat}/" in existing:
                    cat_hit = cat; break
            else:
                cat_hit = "?"
            col = CAT_COLORS.get(cat_hit, self.WARN)
            self.lbl_dup.configure(text=f"⚠ exists · {cat_hit}", fg=col, bg=self.BG)
        else:
            self.lbl_dup.configure(text="✔ available", fg=self.SUCCESS, bg=self.BG)

    def _refresh_existing(self):
        self._existing = scan_existing_modules()
        if hasattr(self, "loader_tree"):
            self._populate_loader_tree()

    def _refresh_preview(self):
        cfg  = self._get_cfg()
        code = generate_code(cfg)
        t = self.preview
        t.configure(state="normal")
        t.delete("1.0", "end")
        t.insert("1.0", code)

        KEYWORDS = ("public","class","extends","implements","protected",
                    "private","void","return","null","new","super",
                    "this","true","false")

        for i, line in enumerate(code.splitlines(), 1):
            s = line.strip()
            if s.startswith("//") or s.startswith("/*") or s.startswith("*"):
                t.tag_add("comment", f"{i}.0", f"{i}.end"); continue
            if s.startswith("package"):
                t.tag_add("pkg",    f"{i}.0", f"{i}.end"); continue
            if s.startswith("import"):
                t.tag_add("import", f"{i}.0", f"{i}.end"); continue
            if s.startswith("@"):
                t.tag_add("annot",  f"{i}.0", f"{i}.end"); continue
            for kw in KEYWORDS:
                pos = f"{i}.0"
                while True:
                    pos = t.search(rf'\b{kw}\b', pos, f"{i}.end", regexp=True)
                    if not pos: break
                    end = f"{pos}+{len(kw)}c"
                    t.tag_add("kw", pos, end)
                    pos = end
            for m in re.finditer(r'"[^"]*"', line):
                t.tag_add("string", f"{i}.{m.start()}", f"{i}.{m.end()}")

        t.configure(state="disabled")
        cat = cfg["category"]
        self.lbl_preview_path.configure(
            text=f"modules/impl/{cat}/{cfg['class_name']}.java")

    def _status(self, msg: str, color: str):
        self.lbl_status.configure(text=msg, fg=color)
        if self._status_after:
            self.after_cancel(self._status_after)
        self._status_after = self.after(
            7000, lambda: self.lbl_status.configure(text=""))

    def _validate_class_name(self, name: str) -> bool:
        return bool(re.match(r'^[A-Z][A-Za-z0-9_]*$', name))

    def _generate(self):
        cfg = self._get_cfg()
        if not self._validate_class_name(cfg["class_name"]):
            self._status("✗  Class name must start with uppercase + letters/digits/underscores.",
                         self.ERROR); return
        existing = class_exists(cfg["class_name"])
        if existing:
            rel = os.path.relpath(existing, BASE_JAVA)
            if not messagebox.askyesno("Overwrite?",
                    f"'{cfg['class_name']}' already exists:\n{rel}\n\nOverwrite it?"):
                return
        try:
            path = save_module(cfg)
            self._refresh_existing()
            self._status(f"✓  Written → {os.path.relpath(path, BASE_JAVA)}", self.SUCCESS)
        except Exception as ex:
            self._status(f"✗  {ex}", self.ERROR)

    def _copy_code(self):
        self.clipboard_clear()
        self.clipboard_append(generate_code(self._get_cfg()))
        self._status("✓  Code copied to clipboard.", self.SUCCESS)


if __name__ == "__main__":
    ModuleCreatorApp().mainloop()