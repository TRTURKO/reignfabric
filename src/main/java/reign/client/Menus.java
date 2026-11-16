package reign.client;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import reign.Data;
import reign.Data.*;
import reign.client.Client.S;
public final class Menus{
 public static class Setup extends Screen{
  final int tab;int page;int rev=-1;
  static final String[] LK={"startGold","startWood","startStone","startFood","supplyCap","unitCap","minutes"};
  static final String[] LN={"Baslangic Altin","Baslangic Odun","Baslangic Tas","Baslangic Erzak","Birlik Limiti","Adet Limiti","Sure (dk)"};
  static final int[] LS={50,50,50,10,10,5,5};
  static final String[] RK={"income","nodeResp","buildRad"};
  static final String[] RN={"Gelir %","Dugum Yenilenme","Yapi Yaricapi"};
  static final int[] RS={25,5,4};
  static final String[] BK={"ff","pvp"};static final String[] BN={"Dost Atesi","Oyuncu PvP"};
  public Setup(int t,int p){super(Text.literal("Reign"));tab=t;page=p;}
  public boolean shouldPause(){return false;}
  int cx(){return width/2;}
  boolean ed(){return S.leader&&S.phase.equals("LOBBY");}
  void btn(int x,int y,int w,String s,Runnable r,boolean a){ButtonWidget b=ButtonWidget.builder(Text.literal(s),q->r.run()).dimensions(x,y,w,16).build();b.active=a;addDrawableChild(b);}
  protected void init(){
   rev=S.rev;int cx=cx();String[] t={"Harita & Irk","Yan Ayarlar","Oyuncular"};
   for(int i=0;i<3;i++){int k=i;btn(cx-168+i*112,26,110,t[i],()->get().setScreen(new Setup(k,0)),tab!=i);}
   if(tab==0){
    cyc(56,()->map(-1),()->map(1));cyc(76,()->size(-1),()->size(1));
    cyc(96,()->Client.send("teams "+Math.max(2,S.teams-1)),()->Client.send("teams "+Math.min(4,S.teams+1)));
    for(int i=0;i<4;i++){int k=i;cyc(124+i*20,()->race(k,-1),()->race(k,1),ed()&&i<S.teams);}
   }else if(tab==1){
    for(int i=0;i<LK.length;i++)step(56+i*22,-168,LK[i],LS[i]);
    for(int i=0;i<RK.length;i++)step(56+i*22,6,RK[i],RS[i]);
    for(int i=0;i<BK.length;i++){String k=BK[i];btn(cx+120,56+(RK.length+i)*22,48,"Degistir",()->Client.send("set "+k+" "+!S.bool(k)),ed());}
   }else{
    int per=7,f=page*per;
    for(int i=0;i<per&&f+i<S.players.size();i++){Client.PI p=S.players.get(f+i);
     btn(cx+40,56+i*22,128,"Takim: "+Data.label(p.asg),()->{int n=p.asg+1;if(n>=S.teams)n=-2;Client.send("assign "+p.name+" "+n);},ed());}
    btn(cx-168,56+per*22,30,"<",()->get().setScreen(new Setup(2,Math.max(0,page-1))),true);
    btn(cx-134,56+per*22,30,">",()->get().setScreen(new Setup(2,page+1)),true);
    btn(cx-90,56+per*22,120,"Oto Dengele",()->Client.send("balance"),ed());
   }
   if(S.leader)btn(cx-168,height-30,160,S.phase.equals("LOBBY")?"MACI BASLAT":"MACI BITIR",()->{Client.send(S.phase.equals("LOBBY")?"start":"stop");close();},true);
   btn(cx+8,height-30,160,"Kapat",()->close(),true);
  }
  void cyc(int y,Runnable a,Runnable b){cyc(y,a,b,ed());}
  void cyc(int y,Runnable a,Runnable b,boolean a2){btn(cx()+100,y,20,"<",a,a2);btn(cx()+124,y,20,">",b,a2);}
  void step(int y,int off,String k,int s){btn(cx()+off+114,y,20,"-",()->Client.send("set "+k+" "+(S.i(k)-s)),ed());btn(cx()+off+138,y,20,"+",()->Client.send("set "+k+" "+(S.i(k)+s)),ed());}
  void map(int d){MapDef[] v=MapDef.values();int i=(MapDef.of(S.map).ordinal()+d+v.length)%v.length;Client.send("map "+v[i].id);}
  void size(int d){int[] v={96,128,160};int i=1;for(int k=0;k<3;k++)if(v[k]==S.size)i=k;i=(i+d+3)%3;Client.send("size "+v[i]);}
  void race(int t,int d){Race[] v=Race.values();int i=(Race.of(S.race[t]).ordinal()+d+v.length)%v.length;Client.send("race "+t+" "+v[i].id);}
  public void tick(){if(S.rev!=rev)get().setScreen(new Setup(tab,page));}
  public void render(DrawContext g,int mx,int my,float dt){
   super.render(g,mx,my,dt);int cx=cx();
   g.drawCenteredTextWithShadow(textRenderer,"REIGN OF NETHER - Komuta",cx,10,0xFFFFFF);
   g.drawCenteredTextWithShadow(textRenderer,(S.leader?"Lidersin":"Sadece lider duzenler")+" | "+S.phase,cx,height-44,0xFFFFFF);
   if(tab==0){row(g,60,cx-168,"Harita",MapDef.of(S.map).name);row(g,80,cx-168,"Boyut",S.size+"x"+S.size);row(g,100,cx-168,"Takim",String.valueOf(S.teams));
    for(int i=0;i<S.teams;i++)row(g,128+i*20,cx-168,"Takim "+(i+1)+" irk",Race.of(S.race[i]).name);}
   else if(tab==1){
    for(int i=0;i<LK.length;i++)row(g,60+i*22,cx-168,LN[i],String.valueOf(S.i(LK[i])));
    for(int i=0;i<RK.length;i++)row(g,60+i*22,cx+6,RN[i],String.valueOf(S.i(RK[i])));
    for(int i=0;i<BK.length;i++)row(g,60+(RK.length+i)*22,cx+6,BN[i],S.bool(BK[i])?"Acik":"Kapali");}
   else{int per=7,f=page*per;
    for(int i=0;i<per&&f+i<S.players.size();i++){Client.PI p=S.players.get(f+i);
     g.drawTextWithShadow(textRenderer,p.name,cx-168,61+i*22,0xFFFFFF);
     g.drawTextWithShadow(textRenderer,"-> "+(p.team>=0?Data.NAME[p.team]:Data.label(p.asg)),cx-40,61+i*22,0xFFFFFF);}}
   Client.TI t=S.me();
   if(t!=null)g.drawCenteredTextWithShadow(textRenderer,"A:"+t.gold+" O:"+t.wood+" T:"+t.stone+" E:"+t.food+" Birlik:"+t.su+"/"+t.sc,cx,height-56,0xFFFFFF);
  }
  void row(DrawContext g,int y,int x,String a,String b){g.drawTextWithShadow(textRenderer,a,x,y,0xFFFFFF);g.drawTextWithShadow(textRenderer,b,x+150,y,0xFFFFFF);}
 }
 public static class BuildMenu extends Screen{
  public BuildMenu(){super(Text.literal("Yapi"));}
  public boolean shouldPause(){return false;}
  protected void init(){int cx=width/2,i=0;
   for(Build b:Build.values()){if(b==Build.CORE)continue;int c=i%3,r=i/3;
    addDrawableChild(ButtonWidget.builder(Text.literal(b.name),q->{Client.send("build "+b.id);close();}).dimensions(cx-171+c*116,50+r*40,110,32).build());i++;}
   addDrawableChild(ButtonWidget.builder(Text.literal("Kapat"),q->close()).dimensions(cx-60,height-28,120,20).build());}
  public void render(DrawContext g,int mx,int my,float dt){
   super.render(g,mx,my,dt);int cx=width/2;Client.TI t=S.me();Race r=t==null?Race.HUMANS:Race.of(t.race);
   g.drawCenteredTextWithShadow(textRenderer,"YAPI MENUSU",cx,14,0xFFFFFF);
   g.drawCenteredTextWithShadow(textRenderer,t==null?"Macta degilsin":"A:"+t.gold+" O:"+t.wood+" T:"+t.stone,cx,30,0xFFFFFF);
   int i=0;
   for(Build b:Build.values()){if(b==Build.CORE)continue;int c=i%3,rr=i/3,x=cx-171+c*116,y=50+rr*40;
    int gg=(int)Math.round(b.gold*r.cost),ww=(int)Math.round(b.wood*r.cost),ss=(int)Math.round(b.stone*r.cost);
    boolean ok=t!=null&&t.gold>=gg&&t.wood>=ww&&t.stone>=ss;
    g.drawTextWithShadow(textRenderer,gg+"A "+ww+"O "+ss+"T",x+6,y+19,ok?0x55FF55:0xFF5555);
    if(b.id.equals(S.sel))g.drawTextWithShadow(textRenderer,"*",x+98,y+4,0xFFFF55);i++;}
   g.drawCenteredTextWithShadow(textRenderer,"Sag tik: kur | Shift+sag tik: sok | Dushman kaleye sol tik: saldir",cx,height-44,0xFFFFFF);}
 }
 public static class UnitMenu extends Screen{
  public UnitMenu(){super(Text.literal("Birlik"));}
  public boolean shouldPause(){return false;}
  protected void init(){int cx=width/2;
   for(int s=1;s<=3;s++){int k=s;addDrawableChild(ButtonWidget.builder(Text.literal("Birlik "+s+" uret"),q->Client.send("train "+k)).dimensions(cx-100,52+(s-1)*44,200,26).build());}
   addDrawableChild(ButtonWidget.builder(Text.literal("Burayi toplanma noktasi yap"),q->{Client.send("rally");close();}).dimensions(cx-100,190,200,20).build());
   addDrawableChild(ButtonWidget.builder(Text.literal("Kapat"),q->close()).dimensions(cx-60,height-28,120,20).build());}
  public void render(DrawContext g,int mx,int my,float dt){
   super.render(g,mx,my,dt);int cx=width/2;Client.TI t=S.me();Race r=t==null?Race.HUMANS:Race.of(t.race);
   g.drawCenteredTextWithShadow(textRenderer,"BIRLIK MENUSU",cx,14,0xFFFFFF);
   g.drawCenteredTextWithShadow(textRenderer,t==null?"Macta degilsin":r.name+" | Birlik "+t.su+"/"+t.sc+" | Kuyruk "+t.q,cx,30,0xFFFFFF);
   for(int s=1;s<=3;s++){Unit u=Unit.of(r,s);if(u==null)continue;int y=52+(s-1)*44;
    boolean ok=t!=null&&t.gold>=u.gold&&t.food>=u.food;
    g.drawCenteredTextWithShadow(textRenderer,u.name,cx,y-10,0xFFFFFF);
    g.drawCenteredTextWithShadow(textRenderer,u.gold+"A "+u.food+"E "+u.sup+" birlik | gerek: "+u.need().name,cx,y+28,ok?0x55FF55:0xFF5555);}
   g.drawCenteredTextWithShadow(textRenderer,"Komuta Asasi sag tik = saldir-git | Shift = tum ordu",cx,height-44,0xFFFFFF);}
 }
}