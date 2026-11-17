package reign;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Formatting;
public final class Data{
 public static final Formatting[] COL={Formatting.BLUE,Formatting.RED,Formatting.GREEN,Formatting.YELLOW};
 public static final String[] NAME={"Mavi","Kirmizi","Yesil","Sari"};
 public static final BlockState[] TEAM={Blocks.BLUE_CONCRETE.getDefaultState(),Blocks.RED_CONCRETE.getDefaultState(),Blocks.LIME_CONCRETE.getDefaultState(),Blocks.YELLOW_CONCRETE.getDefaultState()};
 public static final BlockState[] GL={Blocks.BLUE_STAINED_GLASS.getDefaultState(),Blocks.RED_STAINED_GLASS.getDefaultState(),Blocks.LIME_STAINED_GLASS.getDefaultState(),Blocks.YELLOW_STAINED_GLASS.getDefaultState()};
 public static int cl(int v,int a,int b){return Math.max(a,Math.min(b,v));}
 public static String label(int a){return a==-2?"Oto":a==-1?"Izleyici":NAME[cl(a,0,3)];}
 public enum Race{
  HUMANS("humans","Insanlar",.9,1.25,0,Blocks.STONE_BRICKS,Blocks.OAK_PLANKS),
  NETHER("nether","Nether Soyu",1,1,0,Blocks.POLISHED_BLACKSTONE_BRICKS,Blocks.NETHER_BRICKS),
  END("end","End Dogumlu",1.1,1,2,Blocks.END_STONE_BRICKS,Blocks.PURPUR_BLOCK);
  public final String id,name;public final double cost,gold;public final int sup;public final BlockState wall,floor;
  Race(String i,String n,double c,double g,int s,Block w,Block f){id=i;name=n;cost=c;gold=g;sup=s;wall=w.getDefaultState();floor=f.getDefaultState();}
  public static Race of(String s){for(Race r:values())if(r.id.equalsIgnoreCase(s))return r;return HUMANS;}
 }
 public enum MapDef{
  BASIN("basin","Nether Havzasi",Blocks.NETHERRACK,Blocks.BLACKSTONE,Blocks.POLISHED_BLACKSTONE_BRICKS,Blocks.SHROOMLIGHT,Blocks.CRIMSON_STEM,Blocks.NETHER_GOLD_ORE,Blocks.LAVA),
  PLATEAU("plateau","Ikiz Yaylalar",Blocks.GRASS_BLOCK,Blocks.DIRT,Blocks.STONE_BRICKS,Blocks.SEA_LANTERN,Blocks.OAK_LOG,Blocks.GOLD_ORE,Blocks.BLUE_ICE),
  SPIRES("spires","End Sutunlari",Blocks.END_STONE_BRICKS,Blocks.END_STONE,Blocks.PURPUR_PILLAR,Blocks.PEARLESCENT_FROGLIGHT,Blocks.PURPUR_PILLAR,Blocks.GOLD_ORE,Blocks.BLUE_ICE),
  RUINS("ruins","Harabe Ovalar",Blocks.STONE_BRICKS,Blocks.COBBLESTONE,Blocks.CHISELED_STONE_BRICKS,Blocks.GLOWSTONE,Blocks.SPRUCE_LOG,Blocks.DEEPSLATE_GOLD_ORE,Blocks.MAGMA_BLOCK);
  public final String id,name;public final BlockState floor,sub,wall,light,log,ore,hazard;
  MapDef(String i,String n,Block f,Block s,Block w,Block l,Block g,Block o,Block h){id=i;name=n;floor=f.getDefaultState();sub=s.getDefaultState();wall=w.getDefaultState();light=l.getDefaultState();log=g.getDefaultState();ore=o.getDefaultState();hazard=h.getDefaultState();}
  public static MapDef of(String s){for(MapDef m:values())if(m.id.equalsIgnoreCase(s))return m;return BASIN;}
 }
 public enum Build{
  CORE("core","Kale",0,0,0,9,9,5,600,0,Blocks.BEACON),
  HOUSE("house","Ev",40,30,0,5,5,4,90,120,Blocks.BOOKSHELF),
  BARRACKS("barracks","Kisla",120,80,40,7,7,5,180,240,Blocks.SMITHING_TABLE),
  FARM("farm","Ciftlik",60,40,0,7,7,2,80,160,Blocks.HAY_BLOCK),
  LUMBER("lumber","Odun Kampi",50,20,0,5,5,4,80,160,Blocks.CRAFTING_TABLE),
  QUARRY("quarry","Tas Ocagi",70,40,0,5,5,4,90,160,Blocks.BLAST_FURNACE),
  MARKET("market","Pazar",100,60,40,7,7,5,140,240,Blocks.GOLD_BLOCK),
  TOWER("tower","Ok Kulesi",90,30,80,3,3,8,160,200,Blocks.TARGET),
  SIEGE("siege","Kusatma Atolyesi",180,120,100,7,7,6,220,320,Blocks.IRON_BLOCK),
  WALL("wall","Sur",10,0,20,5,1,3,70,60,Blocks.CHISELED_STONE_BRICKS);
  public final String id,name;public final int gold,wood,stone,w,d,h,hp,ticks;public final Block heart;
  Build(String i,String n,int g,int wd,int st,int ww,int dd,int hh,int hp,int t,Block hr){id=i;name=n;gold=g;wood=wd;stone=st;w=ww;d=dd;h=hh;this.hp=hp;ticks=t;heart=hr;}
  public int gi(){return this==MARKET?3:this==CORE?2:0;}
  public int wi(){return this==LUMBER?2:0;}
  public int si(){return this==QUARRY?2:0;}
  public int fi(){return this==FARM?3:0;}
  public int sup(){return this==CORE?10:this==HOUSE?8:0;}
  public static Build of(String s){for(Build b:values())if(b.id.equalsIgnoreCase(s))return b;return null;}
 }
 public enum UnitDef{
  H1(Race.HUMANS,1,"Muhafiz",EntityType.VINDICATOR,60,1,1,100,7,1.05,false),
  H2(Race.HUMANS,2,"Okcu",EntityType.PILLAGER,90,2,2,160,4,1,false),
  H3(Race.HUMANS,3,"Demir Sovalye",EntityType.IRON_GOLEM,200,4,4,280,18,.85,false),
  N1(Race.NETHER,1,"Kara Iskelet",EntityType.WITHER_SKELETON,55,1,1,100,8,1.1,false),
  N2(Race.NETHER,2,"Alev Topu",EntityType.BLAZE,100,2,2,170,6,1,false),
  N3(Race.NETHER,3,"Kusatma Canavari",EntityType.HOGLIN,190,4,4,270,22,1,false),
  E1(Race.END,1,"Golge Muhafiz",EntityType.ENDERMAN,65,1,1,100,8,1.25,false),
  E2(Race.END,2,"Kabuk Nisanci",EntityType.SHULKER,110,2,2,180,9,0,true),
  E3(Race.END,3,"Sizici",EntityType.ENDERMITE,30,1,1,60,3,1.4,false);
  public final Race race;public final int slot;public final String name;public final EntityType<?> type;
  public final int gold,food,sup,ticks,siege;public final double speed;public final boolean still;
  UnitDef(Race r,int s,String n,EntityType<?> t,int g,int f,int su,int ti,int si,double sp,boolean st){race=r;slot=s;name=n;type=t;gold=g;food=f;sup=su;ticks=ti;siege=si;speed=sp;still=st;}
  public Build need(){return slot==1?Build.CORE:slot==2?Build.BARRACKS:Build.SIEGE;}
  public static UnitDef of(Race r,int s){for(UnitDef u:values())if(u.race==r&&u.slot==s)return u;return null;}
 }
 public static final class Cfg{
  public MapDef map=MapDef.BASIN;public int size=128,teams=2;public Race[] race={Race.HUMANS,Race.NETHER,Race.END,Race.HUMANS};
  public int gold=300,wood=200,stone=150,food=20,supCap=60,unitCap=30,min=25,income=100,nodeResp=25,rad=28;
  public boolean ff=false,pvp=true;
 }
}
