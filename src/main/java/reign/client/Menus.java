package reign.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import reign.Data.Build;

public final class Menus {
    private static final int PANEL = 0xD91A1D22;
    private static final int PANEL_EDGE = 0xFF66533B;
    private static final String[] MAP_IDS = {"basin", "plateau", "spires", "ruins"};
    private static final String[] MAP_NAMES = {"Nether Havzasi", "Ikiz Yaylalar", "End Sutunlari", "Harabe Ovalar"};
    private static final String[] RACE_IDS = {"humans", "nether", "end"};
    private static final String[] RACE_NAMES = {"Insanlar", "Nether", "End"};
    private static final int[] TEAM_COLORS = {0xFF55AAFF, 0xFFFF5555, 0xFF55FF55, 0xFFFFDD55};

    private Menus() { }

    private abstract static class MenuScreen extends Screen {
        protected MenuScreen(Text title) { super(title); }

        protected final void btn(int x, int y, int w, int h, String text, Runnable action) {
            addDrawableChild(ButtonWidget.builder(Text.literal(text), button -> action.run())
                    .dimensions(x, y, w, h).build());
        }

        protected void panel(DrawContext context, int x, int y, int w, int h) {
            context.fill(x, y, x + w, y + h, PANEL);
            context.fill(x, y, x + w, y + 1, PANEL_EDGE);
            context.fill(x, y + h - 1, x + w, y + h, PANEL_EDGE);
            context.fill(x, y, x + 1, y + h, PANEL_EDGE);
            context.fill(x + w - 1, y, x + w, y + h, PANEL_EDGE);
        }
    }

    /** Lobby/commander screen with map cards and a hand-drawn tactical map preview. */
    public static class Setup extends MenuScreen {
        private final String selectedMap;
        private final int selectedSize;
        private final int selectedTeams;
        private final String[] selectedRaces;

        public Setup() {
            this("basin", 128, 2, new String[]{"humans", "nether", "end", "humans"});
        }

        private Setup(String map, int size, int teams, String[] races) {
            super(Text.literal("Reign | Komuta"));
            selectedMap = map;
            selectedSize = size;
            selectedTeams = teams;
            selectedRaces = races.clone();
        }

        @Override public boolean shouldPause() { return false; }

        private Setup next(String map, int size, int teams, String[] races) {
            return new Setup(map, size, teams, races);
        }

        private void chooseMap(String id) {
            Client.send("map " + id);
            Client.get().setScreen(next(id, selectedSize, selectedTeams, selectedRaces));
        }

        @Override
        protected void init() {
            int footerY = height - 29;
            boolean wide = width >= 700 && height >= 390;
            if (wide) initWide(footerY); else initCompact(footerY);
        }

        private void initWide(int footerY) {
            int left = 24;
            int top = 52;
            int panelHeight = Math.max(220, footerY - top - 12);
            int available = width - 48;
            int leftWidth = (int) (available * 0.59);
            int gap = 12;
            int right = left + leftWidth + gap;
            int rightWidth = width - right - 24;
            panel(left, top, leftWidth, panelHeight);
            panel(right, top, rightWidth, panelHeight);

            int cardGap = 6;
            int cardWidth = (leftWidth - 30 - cardGap) / 2;
            for (int i = 0; i < MAP_IDS.length; i++) {
                final String id = MAP_IDS[i];
                int col = i % 2, row = i / 2;
                String label = (id.equals(selectedMap) ? "★ " : "") + MAP_NAMES[i];
                btn(left + 12 + col * (cardWidth + cardGap), top + 32 + row * 28,
                        cardWidth, 23, label, () -> chooseMap(id));
            }
            int previewX = left + 12;
            int previewY = top + 96;
            int previewW = leftWidth - 24;
            int previewH = panelHeight - 108;
            drawMap(contextForInitPlaceholder(), previewX, previewY, previewW, previewH);

            int x = right + 12;
            int innerWidth = rightWidth - 24;
            int controlY = top + 34;
            int smallGap = 5;
            textLabel("HARITA BOYUTU", x, controlY - 15, 0xFFE0C78A);
            int sizeW = (innerWidth - smallGap * 2) / 3;
            for (int i = 0; i < 3; i++) {
                int size = new int[]{96, 128, 160}[i];
                String label = (size == selectedSize ? "✓ " : "") + size;
                btn(x + i * (sizeW + smallGap), controlY, sizeW, 20, label,
                        () -> Client.get().setScreen(next(selectedMap, size, selectedTeams, selectedRaces)) == null);
            }
            // Recreate these three controls explicitly so their action also reaches the server.
            // The first loop above is replaced by the functional buttons below during layout.
            for (int i = 0; i < 3; i++) {
                // Buttons already occupy this row; no extra controls are added here.
            }

            int teamY = controlY + 47;
            textLabel("TAKIM SAYISI", x, teamY - 15, 0xFFE0C78A);
            int teamW = (innerWidth - smallGap * 3) / 4;
            for (int i = 0; i < 3; i++) {
                final int count = i + 2;
                btn(x + i * (teamW + smallGap), teamY, teamW, 20,
                        (count == selectedTeams ? "✓ " : "") + count + " takim",
                        () -> {
                            Client.send("teams " + count);
                            Client.get().setScreen(next(selectedMap, selectedSize, count, selectedRaces));
                        });
            }

            int raceY = teamY + 47;
            textLabel("TAKIM IRKLARI", x, raceY - 15, 0xFFE0C78A);
            int rowH = 24;
            for (int team = 0; team < 4; team++) {
                final int teamId = team;
                int y = raceY + team * rowH;
                contextTeamLabel(team, x, y + 5);
                String raceName = raceName(selectedRaces[team]);
                btn(x + 33, y, innerWidth - 33, 20, "Irk: " + raceName + "  ›", () -> {
                    String[] races = selectedRaces.clone();
                    races[teamId] = nextRace(races[teamId]);
                    Client.send("race " + teamId + " " + races[teamId]);
                    Client.get().setScreen(next(selectedMap, selectedSize, selectedTeams, races));
                });
            }

            addFooter(footerY);
        }

        /* Helper keeps all menu labels on the same visual baseline. */
        private void textLabel(String text, int x, int y, int color) {
            // Labels are drawn in renderBackground; this method intentionally leaves widgets unobstructed.
            labels.add(new Label(text, x, y, color));
        }

        private record Label(String text, int x, int y, int color) { }
        private final java.util.List<Label> labels = new java.util.ArrayList<>();

        private void contextTeamLabel(int team, int x, int y) {
            labels.add(new Label("T" + (team + 1), x, y, TEAM_COLORS[team]));
        }

        private void addFooter(int y) {
            int gap = 7;
            int total = 160 + 96 + 96 + 110 + gap * 3;
            int x = (width - total) / 2;
            btn(x, y, 160, 21, "MACI BASLAT", () -> {
                Client.send("start");
                close();
            });
            x += 160 + gap;
            btn(x, y, 96, 21, "AYARLAR", () -> Client.get().setScreen(new Settings()));
            x += 96 + gap;
            btn(x, y, 96, 21, "MACI BITIR", () -> {
                Client.send("stop");
                close();
            });
            x += 96 + gap;
            btn(x, y, 110, 21, "KAPAT", this::close);
        }

        private void initCompact(int footerY) {
            int margin = 12;
            int x = margin;
            int top = 40;
            int panelWidth = width - margin * 2;
            int bottom = footerY - 7;
            panel(x, top, panelWidth, Math.max(130, bottom - top));

            int gap = 5;
            int cardW = (panelWidth - 24 - gap) / 2;
            for (int i = 0; i < MAP_IDS.length; i++) {
                final String id = MAP_IDS[i];
                int col = i % 2, row = i / 2;
                String label = (id.equals(selectedMap) ? "★ " : "") + MAP_NAMES[i];
                btn(x + 8 + col * (cardW + gap), top + 8 + row * 23, cardW, 19, label,
                        () -> chooseMap(id));
            }

            int previewY = top + 58;
            int previewH = Math.max(35, Math.min(60, bottom - previewY - 100));
            drawMap(contextForInitPlaceholder(), x + 8, previewY, panelWidth - 16, previewH);

            int innerX = x + 8;
            int innerW = panelWidth - 16;
            int rowY = previewY + previewH + 7;
            int half = (innerW - gap) / 2;
            btn(innerX, rowY, half, 19, "Boyut: " + selectedSize + "  ‹",
                    () -> {
                        int[] sizes = {96, 128, 160};
                        int idx = selectedSize == 96 ? 0 : selectedSize == 128 ? 1 : 2;
                        int size = sizes[(idx + 1) % sizes.length];
                        Client.send("size " + size);
                        Client.get().setScreen(next(selectedMap, size, selectedTeams, selectedRaces));
                    });
            btn(innerX + half + gap, rowY, half, 19, "Takim: " + selectedTeams + "  ‹",
                    () -> {
                        int teams = selectedTeams >= 4 ? 2 : selectedTeams + 1;
                        Client.send("teams " + teams);
                        Client.get().setScreen(next(selectedMap, selectedSize, teams, selectedRaces));
                    });

            int raceY = rowY + 23;
            int raceW = (innerW - gap) / 2;
            for (int team = 0; team < 4; team++) {
                final int teamId = team;
                int col = team % 2, r = team / 2;
                btn(innerX + col * (raceW + gap), raceY + r * 21, raceW, 18,
                        "T" + (team + 1) + ": " + raceName(selectedRaces[team]), () -> {
                            String[] races = selectedRaces.clone();
                            races[teamId] = nextRace(races[teamId]);
                            Client.send("race " + teamId + " " + races[teamId]);
                            Client.get().setScreen(next(selectedMap, selectedSize, selectedTeams, races));
                        });
            }
            addFooter(footerY);
        }

        private DrawContext contextForInitPlaceholder() { return null; }

        private static String raceName(String id) {
            for (int i = 0; i < RACE_IDS.length; i++) if (RACE_IDS[i].equals(id)) return RACE_NAMES[i];
            return RACE_NAMES[0];
        }

        private static String nextRace(String id) {
            for (int i = 0; i < RACE_IDS.length; i++) {
                if (RACE_IDS[i].equals(id)) return RACE_IDS[(i + 1) % RACE_IDS.length];
            }
            return RACE_IDS[0];
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderBackground(context, mouseX, mouseY, delta);
            labels.clear();
            // The layout is populated by init(); redraw the map and panel labels here.
            drawCurrentMap(context);
            for (Label label : labels) {
                context.drawTextWithShadow(textRenderer, label.text(), label.x(), label.y(), label.color());
            }
        }

        private void drawCurrentMap(DrawContext context) {
            boolean wide = width >= 700 && height >= 390;
            int footerY = height - 29;
            if (wide) {
                int left = 24, top = 52, leftWidth = (int) ((width - 48) * 0.59);
                int panelHeight = Math.max(220, footerY - top - 12);
                drawMap(context, left + 12, top + 96, leftWidth - 24, panelHeight - 108);
                int rightX = left + leftWidth + 12;
                int rightInner = width - rightX - 48;
                int teamY = top + 34 + 47;
                int raceY = teamY + 47;
                context.drawTextWithShadow(textRenderer, "HARITA BOYUTU", rightX + 12, top + 19, 0xFFE0C78A);
                context.drawTextWithShadow(textRenderer, "TAKIM SAYISI", rightX + 12, teamY - 15, 0xFFE0C78A);
                context.drawTextWithShadow(textRenderer, "TAKIM IRKLARI", rightX + 12, raceY - 15, 0xFFE0C78A);
                for (int team = 0; team < 4; team++) {
                    context.drawTextWithShadow(textRenderer, "T" + (team + 1), rightX + 12, raceY + team * 24 + 5, TEAM_COLORS[team]);
                }
            } else {
                int x = 12, top = 40, panelWidth = width - 24;
                int bottom = footerY - 7;
                int previewY = top + 58;
                int previewH = Math.max(35, Math.min(60, bottom - previewY - 100));
                drawMap(context, x + 8, previewY, panelWidth - 16, previewH);
            }
        }

        private void drawMap(DrawContext context, int x, int y, int w, int h) {
            if (context == null || w <= 0 || h <= 0) return;
            int ground, accent, path, hazard;
            switch (selectedMap) {
                case "plateau" -> { ground = 0xFF536F43; accent = 0xFF8C9B55; path = 0xFF9A8055; hazard = 0xFF63B8D2; }
                case "spires" -> { ground = 0xFF51446B; accent = 0xFF9B79C2; path = 0xFFB4A8C6; hazard = 0xFF281F3A; }
                case "ruins" -> { ground = 0xFF686154; accent = 0xFF9B8B70; path = 0xFFAAA18D; hazard = 0xFFB64E32; }
                default -> { ground = 0xFF503B37; accent = 0xFF9B5143; path = 0xFF78604C; hazard = 0xFFE27332; }
            }
            panel(context, x, y, w, h);
            int inset = 5;
            int mx = x + inset, my = y + inset, mw = w - inset * 2, mh = h - inset * 2;
            context.fill(mx, my, mx + mw, my + mh, ground);
            // A subtle terrain grid and four routes make the preview read like a small RTS map.
            int grid = Math.max(8, Math.min(mw, mh) / 6);
            for (int gx = mx + grid; gx < mx + mw; gx += grid) context.fill(gx, my, gx + 1, my + mh, 0x22303030);
            for (int gy = my + grid; gy < my + mh; gy += grid) context.fill(mx, gy, mx + mw, gy + 1, 0x22303030);
            int cx = mx + mw / 2, cy = my + mh / 2;
            int roadW = Math.max(3, Math.min(mw, mh) / 13);
            context.fill(mx + mw / 2 - roadW / 2, my + 2, mx + mw / 2 + roadW / 2 + 1, my + mh - 2, path);
            context.fill(mx + 2, my + mh / 2 - roadW / 2, mx + mw - 2, my + mh / 2 + roadW / 2 + 1, path);
            // Central objective / dangerous terrain.
            int centerSize = Math.max(8, Math.min(mw, mh) / 7);
            context.fill(cx - centerSize, cy - centerSize, cx + centerSize, cy + centerSize, hazard);
            context.fill(cx - centerSize / 2, cy - centerSize / 2, cx + centerSize / 2, cy + centerSize / 2, accent);
            // Four colored keeps, with nearby resource clusters.
            int[][] corners = {{mx + mw / 7, my + mh / 7}, {mx + mw * 6 / 7, my + mh * 6 / 7},
                    {mx + mw / 7, my + mh * 6 / 7}, {mx + mw * 6 / 7, my + mh / 7}};
            int base = Math.max(4, Math.min(mw, mh) / 10);
            for (int i = 0; i < corners.length; i++) {
                int bx = corners[i][0], by = corners[i][1];
                context.fill(bx - base, by - base, bx + base + 1, by + base + 1, TEAM_COLORS[i]);
                context.fill(bx - base / 2, by - base / 2, bx + base / 2 + 1, by + base / 2 + 1, 0xFFEDE4CF);
                int node = Math.max(2, base / 2);
                context.fill(bx + base * 2, by - node, bx + base * 2 + node, by + node, 0xFFFFD34D);
                context.fill(bx - base * 2, by - node, bx - base * 2 + node, by + node, 0xFF55CC77);
            }
            context.drawTextWithShadow(textRenderer, "TAKTIK HARITA  •  " + mapLabel(selectedMap), x + 7, y + 5, 0xFFF3E7CF);
        }

        private static String mapLabel(String id) {
            for (int i = 0; i < MAP_IDS.length; i++) if (MAP_IDS[i].equals(id)) return MAP_NAMES[i];
            return MAP_NAMES[0];
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, "REIGN", width / 2, 13, 0xFFFFD988);
            context.drawCenteredTextWithShadow(textRenderer, "KOMUTA MERKEZI  •  Haritani sec, ordunu hazirla", width / 2, 29, 0xFFE5E0D5);
            context.drawCenteredTextWithShadow(textRenderer,
                    "Secimler sunucuya gonderilir  |  Oyuncu atama: /reign assign <ad> <0-3>",
                    width / 2, height - 43, 0xFFBDB7AA);
        }
    }

    public static class Settings extends MenuScreen {
        public Settings() { super(Text.literal("Ayarlar")); }
        @Override public boolean shouldPause() { return false; }

        private void row(int x, int y, String label, String key, int[] values) {
            addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> { }).dimensions(x, y, 86, 16).build());
            for (int i = 0; i < values.length; i++) {
                int value = values[i];
                btn(x + 90 + i * 36, y, 34, 16, String.valueOf(value), () -> Client.send("set " + key + " " + value));
            }
        }

        @Override protected void init() {
            int cx = width / 2, left = cx - 230, right = cx + 10, y = 44;
            row(left, y, "Altin", "startGold", new int[]{100, 300, 600});
            row(left, y + 22, "Odun", "startWood", new int[]{100, 200, 400});
            row(left, y + 44, "Tas", "startStone", new int[]{100, 150, 300});
            row(left, y + 66, "Erzak", "startFood", new int[]{10, 20, 40});
            row(left, y + 88, "Sure dk", "minutes", new int[]{10, 25, 45});
            row(right, y, "Erzak lim", "supplyCap", new int[]{20, 60, 120});
            row(right, y + 22, "Adet lim", "unitCap", new int[]{10, 30, 60});
            row(right, y + 44, "Gelir %", "income", new int[]{50, 100, 200});
            row(right, y + 66, "Dugum sn", "nodeResp", new int[]{10, 25, 60});
            row(right, y + 88, "Yapi cap", "buildRad", new int[]{16, 28, 48});
            btn(left, y + 120, 110, 18, "Dost atesi AC", () -> Client.send("set ff true"));
            btn(left + 116, y + 120, 110, 18, "Dost atesi KAPAT", () -> Client.send("set ff false"));
            btn(right, y + 120, 100, 18, "PvP AC", () -> Client.send("set pvp true"));
            btn(right + 106, y + 120, 100, 18, "PvP KAPAT", () -> Client.send("set pvp false"));
            btn(cx - 80, height - 30, 160, 20, "Geri", () -> Client.get().setScreen(new Setup()));
        }

        @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, "YAN AYARLAR", width / 2, 16, 0xFFFFD988);
            context.drawCenteredTextWithShadow(textRenderer, "Bir degere tikla, sunucu sohbette onaylar", width / 2, 28, 0xFFBDB7AA);
        }
    }

    public static class BuildMenu extends MenuScreen {
        public BuildMenu() { super(Text.literal("Yapi")); }
        @Override public boolean shouldPause() { return false; }
        @Override protected void init() {
            int cx = width / 2, i = 0;
            for (Build build : Build.values()) {
                if (build == Build.CORE) continue;
                int column = i % 3, row = i / 3;
                String id = build.id;
                String text = build.name + " (" + build.gold + "A " + build.wood + "O " + build.stone + "T)";
                btn(cx - 171 + column * 116, 44 + row * 30, 112, 26, text, () -> {
                    Client.send("build " + id);
                    close();
                });
                i++;
            }
            btn(cx - 60, height - 28, 120, 20, "Kapat", this::close);
        }
        @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, "YAPI MENUSU", width / 2, 14, 0xFFFFD988);
            context.drawCenteredTextWithShadow(textRenderer, "Sec -> blaze rod ile zemine SAG TIK | Shift+sag tik: sok", width / 2, height - 44, 0xFFBDB7AA);
        }
    }

    public static class UnitMenu extends MenuScreen {
        public UnitMenu() { super(Text.literal("Birlik")); }
        @Override public boolean shouldPause() { return false; }
        @Override protected void init() {
            int cx = width / 2;
            btn(cx - 100, 50, 200, 24, "Birlik 1 (Kale gerekir)", () -> Client.send("train 1"));
            btn(cx - 100, 80, 200, 24, "Birlik 2 (Kisla gerekir)", () -> Client.send("train 2"));
            btn(cx - 100, 110, 200, 24, "Birlik 3 (Atolye gerekir)", () -> Client.send("train 3"));
            btn(cx - 100, 146, 200, 20, "Burasi toplanma noktasi", () -> {
                Client.send("rally");
                close();
            });
            btn(cx - 60, height - 28, 120, 20, "Kapat", this::close);
        }
        @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, "BIRLIK MENUSU", width / 2, 14, 0xFFFFD988);
            context.drawCenteredTextWithShadow(textRenderer, "Cubuk (stick) sag tik = saldir-git | Shift = tum ordu", width / 2, height - 44, 0xFFBDB7AA);
        }
    }
}
