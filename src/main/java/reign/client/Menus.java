package reign.client;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import reign.Data.Build;

public final class Menus{
 static void btn(Screen s,int x,int y,int w,int h,String t,Runnable r){
  s.addDrawableChild(ButtonWidget.builder(Text.literal(t),b->r.run()).dimensions(x,y,w,h).build());}

 public static class Setup extends Screen{
  public Setup(){super(Text.literal("Reign"));}
  public boolean shouldPause(){return false;}
  protected void init(){
   int cx=width/2,y=40;
   String[] mid={"basin","plateau","spires","ruins"},mn={"Nether","Yayla","End","Harabe"};
   for(int i=0;i<4;i++){String id=mid[i];btn(this,cx-160+i*82,y,78,18,mn[i],()->Client.send("map "+id));}
   y+=26;
   int[] sz={96,128,160};
   for(int i=0;i<3;i++){int v=sz[i];btn(this,cx-160+i*82,y,78,18,"Boyut "+v,()->Client.send("size "+v));}
   for(int i=2;i<=4;i++){int v=i;btn(this,cx+90+(i-2)*40,y,36,18,v+"T",()->Client.send("teams "+v));}
   y+=28;
   String[] rid={"humans","nether","end"},rn={"Insan","Nether","End"};
   for(int t=0;t<4;t++){
    int team=t;
    addDrawableChild(ButtonWidget.builder(Text.literal("T"+(t+1)),b->{}).dimensions(cx-160,y+t*24,26,18).build());
    for(int i=0;i<3;i++){String id=rid[i];btn(this,cx-130+i*70,y+t*24,66,18,rn[i],()->Client.send("race "+team+" "+id));}}
   y+=104;
   btn(this,cx-160,y,100,20,"Ayarlar",()->Client.get().setScreen(new Settings()));
   btn(this,cx-54,y,100,20,"Oto Dengele",()->Client.send("balance"));
   btn(this,cx+52,y,108,20,"Bilgi (sohbet)",()->Client.send("info"));
   btn(this,cx-160,height-30,150,20,"MACI BASLAT",()->{Client.send("start");close();});
   btn(this,cx-4,height-30,80,20,"Bitir",()->{Client.send("stop");close();});
   btn(this,cx+82,height-30,78,20,"Kapat",()->close());
  }
  public void render(DrawContext g,int mx,int my,float d){
   super.render(g,mx,my,d);
   g.drawCenteredTextWithShadow(textRenderer,"REIGN - Komuta (lider)",width/2,14,0xFFFFFF);
   g.drawCenteredTextWithShadow(textRenderer,"Harita / Boyut / Takim sayisi / Irklar",width/2,28,0xAAAAAA);
   g.drawCenteredTextWithShadow(textRenderer,"Secimler sohbette onaylanir. Oyuncu atama: /reign assign <ad> <0-3>",width/2,height-46,0xAAAAAA);
  }
 }

 public static class Settings extends Screen{
  public Settings(){super(Text.literal("Ayarlar"));}
  public boolean shouldPause(){return false;}
  void row(int x,int y,String label,String key,int[] vals){
   addDrawableChild(ButtonWidget.builder(Text.literal(label),b->{}).dimensions(x,y,86,16).build());
   for(int i=0;i<vals.length;i++){int v=vals[i];btn(this,x+90+i*36,y,34,16,String.valueOf(v),()->Client.send("set "+key+" "+v));}}
  protected void init(){
   int cx=width/2,L=cx-230,R=cx+10,y=44;
   row(L,y,"Altin","startGold",new int[]{100,300,600});
   row(L,y+22,"Odun","startWood",new int[]{100,200,400});
   row(L,y+44,"Tas","startStone",new int[]{100,150,300});
   row(L,y+66,"Erzak","startFood",new int[]{10,20,40});
   row(L,y+88,"Sure dk","minutes",new int[]{10,25,45});
   row(R,y,"Erzak lim","supplyCap",new int[]{20,60,120});
   row(R,y+22,"Adet lim","unitCap",new int[]{10,30,60});
   row(R,y+44,"Gelir %","income",new int[]{50,100,200});
   row(R,y+66,"Dugum sn","nodeResp",new int[]{10,25,60});
   row(R,y+88,"Yapi cap","buildRad",new int[]{16,28,48});
   btn(this,L,y+120,110,18,"Dost atesi AC",()->Client.send("set ff true"));
   btn(this,L+116,y+120,110,18,"Dost atesi KAPAT",()->Client.send("set ff false"));
   btn(this,R,y+120,100,18,"PvP AC",()->Client.send("set pvp true"));
   btn(this,R+106,y+120,100,18,"PvP KAPAT",()->Client.send("set pvp false"));
   btn(this,cx-80,height-30,160,20,"Geri",()->Client.get().setScreen(new Setup()));
  }
  public void render(DrawContext g,int mx,int my,float d){
   super.render(g,mx,my,d);
   g.drawCenteredTextWithShadow(textRenderer,"YAN AYARLAR",width/2,16,0xFFFFFF);
   g.drawCenteredTextWithShadow(textRenderer,"Bir degere tikla, sunucu sohbette onaylar",width/2,28,0xAAAAAA);
  }
 }

 public static class BuildMenu extends Screen{
  public BuildMenu(){super(Text.literal("Yapi"));}
  public boolean shouldPause(){return false;}
  protected void init(){
   int cx=width/2,i=0;
   for(Build b:Build.values()){
    if(b==Build.CORE)continue;
    int c=i%3,r=i/3;String id=b.id;
    String t=b.name+" ("+b.gold+"A "+b.wood+"O "+b.stone+"T)";
    btn(this,cx-171+c*116,44+r*30,112,26,t,()->{Client.send("build "+id);close();});i++;}
   btn(this,cx-60,height-28,120,20,"Kapat",()->close());
  }
  public void render(DrawContext g,int mx,int my,float d){
   super.render(g,mx,my,d);
   g.drawCenteredTextWithShadow(textRenderer,"YAPI MENUSU",width/2,14,0xFFFFFF);
   g.drawCenteredTextWithShadow(textRenderer,"Sec -> blaze rod ile zemine SAG TIK | Shift+sag tik: sok",width/2,height-44,0xAAAAAA);
  }
 }

 public static class UnitMenu extends Screen{
  public UnitMenu(){super(Text.literal("Birlik"));}
  public boolean shouldPause(){return false;}
  protected void init(){
   int cx=width/2;
   btn(this,cx-100,50,200,24,"Birlik 1 (Kale gerekir)",()->Client.send("train 1"));
   btn(this,cx-100,80,200,24,"Birlik 2 (Kisla gerekir)",()->Client.send("train 2"));
   btn(this,cx-100,110,200,24,"Birlik 3 (Atolye gerekir)",()->Client.send("train 3"));
   btn(this,cx-100,146,200,20,"Burasi toplanma noktasi",()->{Client.send("rally");close();});
   btn(this,cx-60,height-28,120,20,"Kapat",()->close());
  }
  public void render(DrawContext g,int mx,int my,float d){
   super.render(g,mx,my,d);
   g.drawCenteredTextWithShadow(textRenderer,"BIRLIK MENUSU",width/2,14,0xFFFFFF);
   g.drawCenteredTextWithShadow(textRenderer,"Cubuk (stick) sag tik = saldir-git | Shift = tum ordu",width/2,height-44,0xAAAAAA);
  }
 }
}
