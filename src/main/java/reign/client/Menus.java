package reign.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import reign.Data.Build;

public final class Menus {
    private Menus() {
    }

    private abstract static class MenuScreen extends Screen {
        protected MenuScreen(Text title) {
            super(title);
        }

        protected final void btn(int x, int y, int w, int h, String text, Runnable action) {
            addDrawableChild(ButtonWidget.builder(Text.literal(text), button -> action.run())
                    .dimensions(x, y, w, h)
                    .build());
        }
    }

    public static class Setup extends MenuScreen {
        public Setup() {
            super(Text.literal("Reign"));
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        @Override
        protected void init() {
            int cx = width / 2;
            int y = 40;
            String[] mapIds = {"basin", "plateau", "spires", "ruins"};
            String[] mapNames = {"Nether", "Yayla", "End", "Harabe"};
            for (int i = 0; i < mapIds.length; i++) {
                String id = mapIds[i];
                btn(cx - 160 + i * 82, y, 78, 18, mapNames[i], () -> Client.send("map " + id));
            }

            y += 26;
            int[] sizes = {96, 128, 160};
            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];
                btn(cx - 160 + i * 82, y, 78, 18, "Boyut " + size, () -> Client.send("size " + size));
            }
            for (int i = 2; i <= 4; i++) {
                int teams = i;
                btn(cx + 90 + (i - 2) * 40, y, 36, 18, teams + "T", () -> Client.send("teams " + teams));
            }

            y += 28;
            String[] raceIds = {"humans", "nether", "end"};
            String[] raceNames = {"Insan", "Nether", "End"};
            for (int team = 0; team < 4; team++) {
                int teamId = team;
                int rowY = y + team * 24;
                addDrawableChild(ButtonWidget.builder(Text.literal("T" + (team + 1)), button -> { })
                        .dimensions(cx - 160, rowY, 26, 18)
                        .build());
                for (int i = 0; i < raceIds.length; i++) {
                    String raceId = raceIds[i];
                    btn(cx - 130 + i * 70, rowY, 66, 18, raceNames[i],
                            () -> Client.send("race " + teamId + " " + raceId));
                }
            }

            y += 104;
            btn(cx - 160, y, 100, 20, "Ayarlar", () -> Client.get().setScreen(new Settings()));
            btn(cx - 54, y, 100, 20, "Oto Dengele", () -> Client.send("balance"));
            btn(cx + 52, y, 108, 20, "Bilgi (sohbet)", () -> Client.send("info"));
            btn(cx - 160, height - 30, 150, 20, "MACI BASLAT", () -> {
                Client.send("start");
                close();
            });
            btn(cx - 4, height - 30, 80, 20, "Bitir", () -> {
                Client.send("stop");
                close();
            });
            btn(cx + 82, height - 30, 78, 20, "Kapat", this::close);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, "REIGN - Komuta (lider)", width / 2, 14, 0xFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, "Harita / Boyut / Takim sayisi / Irklar", width / 2, 28, 0xAAAAAA);
            context.drawCenteredTextWithShadow(textRenderer,
                    "Secimler sohbette onaylanir. Oyuncu atama: /reign assign <ad> <0-3>",
                    width / 2, height - 46, 0xAAAAAA);
        }
    }

    public static class Settings extends MenuScreen {
        public Settings() {
            super(Text.literal("Ayarlar"));
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        private void row(int x, int y, String label, String key, int[] values) {
            addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> { })
                    .dimensions(x, y, 86, 16)
                    .build());
            for (int i = 0; i < values.length; i++) {
                int value = values[i];
                btn(x + 90 + i * 36, y, 34, 16, String.valueOf(value),
                        () -> Client.send("set " + key + " " + value));
            }
        }

        @Override
        protected void init() {
            int cx = width / 2;
            int left = cx - 230;
            int right = cx + 10;
            int y = 44;
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

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, "YAN AYARLAR", width / 2, 16, 0xFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer,
                    "Bir degere tikla, sunucu sohbette onaylar", width / 2, 28, 0xAAAAAA);
        }
    }

    public static class BuildMenu extends MenuScreen {
        public BuildMenu() {
            super(Text.literal("Yapi"));
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        @Override
        protected void init() {
            int cx = width / 2;
            int i = 0;
            for (Build build : Build.values()) {
                if (build == Build.CORE) {
                    continue;
                }
                int column = i % 3;
                int row = i / 3;
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

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, "YAPI MENUSU", width / 2, 14, 0xFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer,
                    "Sec -> blaze rod ile zemine SAG TIK | Shift+sag tik: sok", width / 2, height - 44, 0xAAAAAA);
        }
    }

    public static class UnitMenu extends MenuScreen {
        public UnitMenu() {
            super(Text.literal("Birlik"));
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        @Override
        protected void init() {
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

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, "BIRLIK MENUSU", width / 2, 14, 0xFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer,
                    "Cubuk (stick) sag tik = saldir-git | Shift = tum ordu", width / 2, height - 44, 0xAAAAAA);
        }
    }
}
