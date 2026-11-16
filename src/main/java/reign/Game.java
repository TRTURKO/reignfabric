package reign;
import com.google.gson.*;
import net.minecraft.block.BlockState;import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.*;import net.minecraft.entity.boss.*;import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.*;import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;import net.minecraft.server.network.ServerPlayerEntity;import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.*;import net.minecraft.text.Text;import net.minecraft.util.*;import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;import net.minecraft.world.World;
import reign.Data.*;
import java.util.*;
public class Game{
 public enum Phase{LOBBY,PREP,RUN,CLEAN}
 public static final BlockPos O=new BlockPos(12000,180,12000);
 public static final String DIM="minecraft:the_nether",SIDE="reignfabric";
 static final Map<MinecraftServer,Game> INST=new IdentityHashMap<>();
 public static Game of(MinecraftServer s){return INST.computeIfAbsent(s,Game::new);}
 public static void drop(MinecraftServer s){INST.remove(s);}
 public static class Team{public final int i;public final Race race;public int gold,wood,stone,food,su,sc,un,bd,dth;public boolean alive=true;public BlockPos spawn=O,rally=O;public final List<UUID> mem=new ArrayList<>();public final List<Ord> q=new ArrayList<>();Team(int i,Race r){this.i=i;race=r;}public int score(){return bd*10+un*5+gold/10-dth*3;}}
 public static class Ord{Unit u;int t;BlockPos at;}
 public static class Bld{Build type;int team;BlockPos a,heart;int t;boolean done;float hp;final List<BlockPos> bl=new ArrayList<>();}
 public static class Un{UUID id;int team;Unit type;BlockPos goal;boolean am;}
 public static class Node{BlockPos p;BlockState s;int cd;}
 public static class PD{int team=-1;boolean spec,saved;Build sel=Build.HOUSE;ItemStack[] inv;GameMode mode;BlockPos back;String dim;int rc;long hit;}
 public final MinecraftServer sv;public Phase phase=Phase.LOBBY;public UUID leader;public Cfg cfg=new Cfg();
 public final List<Team> teams=new ArrayList<>();public final Map<UUID,PD> ps=new HashMap<>();public final Map<UUID,Integer> asg=new HashMap<>();
 public final Map<BlockPos,Bld> blds=new LinkedHashMap<>();public final Map<BlockPos,Node> nodes=new LinkedHashMap<>();public final Map<BlockPos,BlockState> pend=new LinkedHashMap<>();
 public final List<Un> units=new ArrayList<>();public final Map<UUID,Integer> ut=new HashMap<>();
 Arena ar;ServerBossBar bar;long tick;int left;
 Game(MinecraftServer s){sv=s;}
 public ServerWorld aw(){return sv.getWorld(World.NETHER);}
 public boolean isL(UUID u){return leader!=null&&leader.equals(u);}
 public PD pd(UUID u){return ps.get(u);}
 void msg(ServerPlayerEntity p,String s){p.sendMessage(Text.literal("[Reign] "+s),false);}
 void all(String s){sv.getPlayerManager().broadcast(Text.literal("[Reign] "+s),false);}
 void tmsg(int t,String s){for(var e:ps.entrySet())if(e.getValue().team==t){ServerPlayerEntity p=sv.getPlayerManager().getPlayer(e.getKey());if(p!=null)msg(p,s);}}
 public void leader(ServerPlayerEntity p){leader=p.getUuid();all(p.getName().getString()+" lider oldu. R = komuta menusu.");syncAll();}
 static int pi(String s,int d){try{return Integer.parseInt(s);}catch(Exception e){return d;}}
 static int ni(String[] a,int i,int d){return a.length>i?pi(a[i],d):d;}
 ServerPlayerEntity find(String n){for(ServerPlayerEntity p:sv.getPlayerManager().getPlayerList())if(p.getName().getString().equalsIgnoreCase(n))return p;return null;}
 boolean gate(ServerPlayerEntity p){if(!isL(p.getUuid())){msg(p,"Sadece lider degistirir.");return false;}if(phase!=Phase.LOBBY){msg(p,"Mac surerken ayar degismez.");return false;}return true;}
 void rev(ServerPlayerEntity p,String s){cfg.rev++;msg(p,s);syncAll();}
 boolean part(PD d){return phase==Phase.RUN&&d!=null&&!d.spec&&d.team>=0;}
 public void act(ServerPlayerEntity p,String line){
  String[] a=line.trim().split("\\s+");
  try{switch(a[0].toLowerCase(Locale.ROOT)){
   case "start"->start(p);
   case "stop"->{if(isL(p.getUuid())||Compat.op(p.getCommandSource()))stop();else msg(p,"Yetkin yok.");}
   case "leave"->leave(p);
   case "balance"->{if(!gate(p))return;for(ServerPlayerEntity o:sv.getPlayerManager().getPlayerList())asg.put(o.getUuid(),-2);rev(p,"Otomatik dengeleme acildi.");}
   case "map"->{if(!gate(p))return;cfg.map=MapDef.of(a[1]);rev(p,"Harita: "+cfg.map.name);}
   case "size"->{if(!gate(p))return;int s=ni(a,1,128);cfg.size=s>=160?160:s<=96?96:128;rev(p,"Boyut: "+cfg.size);}
   case "teams"->{if(!gate(p))return;cfg.teams=Data.cl(ni(a,1,2),2,4);rev(p,"Takim sayisi: "+cfg.teams);}
   case "race"->{if(!gate(p))return;int t=Data.cl(ni(a,1,0),0,3);cfg.race[t]=Race.of(a.length>2?a[2]:"humans");rev(p,"Takim "+(t+1)+" irk: "+cfg.race[t].name);}
   case "assign"->{if(!gate(p))return;ServerPlayerEntity t=find(a[1]);if(t==null){msg(p,"Oyuncu bulunamadi.");return;}
    int v=a[2].equalsIgnoreCase("auto")?-2:a[2].equalsIgnoreCase("spec")?-1:Data.cl(ni(a,2,-2),-2,3);asg.put(t.getUuid(),v);rev(p,t.getName().getString()+" -> "+Data.label(v));}
   case "set"->set(p,a[1],a[2]);
   case "build"->{PD d=pd(p.getUuid());if(!part(d)){msg(p,"Macta degilsin.");return;}Build b=Build.of(a[1]);
    if(b==null||b==Build.CORE){msg(p,"Gecersiz yapi.");return;}d.sel=b;msg(p,"Secili: "+b.name+" | Yapi Asasi ile zemine sag tik.");sync(p);}
   case "train"->train(p,ni(a,1,1));
   case "rally"->rally(p,p.getBlockPos(),true);
   case "demolish"->{BlockPos l=Compat.look(p,6);if(l!=null)demo(p,l);}
   default->info(p);
  }}catch(Exception e){info(p);}
 }
 void set(ServerPlayerEntity p,String k,String v){
  if(!gate(p))return;int i=pi(v,0);boolean b=Boolean.parseBoolean(v);
  switch(k){
   case "startGold"->cfg.gold=Data.cl(i,0,5000);case "startWood"->cfg.wood=Data.cl(i,0,5000);
   case "startStone"->cfg.stone=Data.cl(i,0,5000);case "startFood"->cfg.food=Data.cl(i,0,1000);
   case "supplyCap"->cfg.supCap=Data.cl(i,10,200);case "unitCap"->cfg.unitCap=Data.cl(i,5,120);
   case "minutes"->cfg.min=Data.cl(i,5,120);case "income"->cfg.income=Data.cl(i,25,400);
   case "nodeResp"->cfg.nodeResp=Data.cl(i,5,180);case "buildRad"->cfg.rad=Data.cl(i,8,64);
   case "ff"->cfg.ff=b;case "pvp"->cfg.pvp=b;
   default->{msg(p,"Bilinmeyen ayar: "+k);return;}}
  rev(p,k+" = "+v);
 }
 void info(ServerPlayerEntity p){
  msg(p,"Durum: "+phase+" | Lider: "+(leader==null?"yok":leader.toString().substring(0,8)));
  msg(p,"Harita: "+cfg.map.name+" "+cfg.size+"x"+cfg.size+" | Takim: "+cfg.teams);
  msg(p,"Tuslar: R=komuta B=yapi N=birlik. Komut: /reign map|size|teams|race|assign|set|start|stop|build|train|rally|leave");
 }
 void start(ServerPlayerEntity by){
  if(!isL(by.getUuid())){msg(by,"Sadece lider baslatir.");return;}
  if(phase!=Phase.LOBBY){msg(by,"Mac zaten aktif.");return;}
  if(aw()==null){msg(by,"Nether boyutu kapali. Arena oraya kuruluyor, nether'i ac.");return;}
  List<ServerPlayerEntity> on=sv.getPlayerManager().getPlayerList();
  if(on.size()<cfg.teams){msg(by,"En az "+cfg.teams+" oyuncu gerekli.");return;}
  teams.clear();ps.clear();units.clear();blds.clear();nodes.clear();ut.clear();pend.clear();
  for(int i=0;i<cfg.teams;i++){Team t=new Team(i,cfg.race[i]);t.gold=cfg.gold;t.wood=cfg.wood;t.stone=cfg.stone;t.food=cfg.food;teams.add(t);}
  for(ServerPlayerEntity p:on){PD d=new PD();ps.put(p.getUuid(),d);int v=asg.getOrDefault(p.getUuid(),-2);if(v==-1)d.spec=true;else if(v>=0&&v<cfg.teams)d.team=v;}
  for(var e:ps.entrySet()){PD d=e.getValue();if(d.spec||d.team>=0)continue;int best=0,bc=999;
   for(Team t:teams){int n=0;for(PD o:ps.values())if(o.team==t.i)n++;if(n<bc){bc=n;best=t.i;}}d.team=best;}
  for(Team t:teams){t.mem.clear();for(var e:ps.entrySet())if(e.getValue().team==t.i)t.mem.add(e.getKey());
   if(t.mem.isEmpty()){msg(by,"Takim "+(t.i+1)+" bos. Oyuncu ata veya takim sayisini dusur.");teams.clear();ps.clear();return;}}
  for(var e:ps.entrySet()){ServerPlayerEntity p=sv.getPlayerManager().getPlayer(e.getKey());if(p!=null)snap(p,e.getValue());}
  ar=new Arena(aw());ar.gen(cfg,pend);force(true);
  phase=Phase.PREP;bar=new ServerBossBar(Text.literal("Arena insa ediliyor..."),BossBar.Color.YELLOW,BossBar.Style.PROGRESS);
  for(UUID u:ps.keySet()){ServerPlayerEntity p=sv.getPlayerManager().getPlayer(u);if(p!=null)bar.addPlayer(p);}
  all("Arena insa ediliyor: "+cfg.map.name+" ("+cfg.size+"x"+cfg.size+")");syncAll();
 }
 void begin(){
  ServerWorld w=aw();
  for(var e:pend.entrySet()){Node n=new Node();n.p=e.getKey();n.s=e.getValue();nodes.put(n.p,n);}pend.clear();
  for(Team t:teams){BlockPos p=Arena.pad(cfg.size,cfg.teams,t.i);t.spawn=p.add(6,1,0);t.rally=p.add(9,1,0);
   Bld b=new Bld();b.type=Build.CORE;b.team=t.i;b.a=p.up();b.done=true;b.hp=Build.CORE.hp;
   b.heart=ar.build(Build.CORE,b.a,t.race,t.i,b.bl);blds.put(b.heart,b);}
  Compat.in(sv,DIM,"gamerule doMobSpawning false");Compat.in(sv,DIM,"gamerule mobGriefing false");Compat.side(sv,SIDE);
  for(var e:ps.entrySet()){ServerPlayerEntity p=sv.getPlayerManager().getPlayer(e.getKey());if(p==null)continue;PD d=e.getValue();
   p.clearStatusEffects();
   if(d.spec){p.changeGameMode(GameMode.SPECTATOR);Compat.tp(sv,p,DIM,O.getX()+.5,O.getY()+14,O.getZ()+.5);continue;}
   Team t=teams.get(d.team);p.changeGameMode(GameMode.SURVIVAL);
   Compat.tp(sv,p,DIM,t.spawn.getX()+.5,t.spawn.getY(),t.spawn.getZ()+.5);kit(p,t);
   msg(p,"Takim: "+Data.NAME[d.team]+" | Irk: "+t.race.name);
   msg(p,"B=yapi N=birlik R=menu | Yapi Asasi: sag tik | Komuta Asasi: toplanma/saldiri");}
  left=cfg.min*60;if(bar!=null)bar.clearPlayers();
  bar=new ServerBossBar(Text.literal("Reign"),BossBar.Color.GREEN,BossBar.Style.PROGRESS);
  for(UUID u:ps.keySet()){ServerPlayerEntity p=sv.getPlayerManager().getPlayer(u);if(p!=null)bar.addPlayer(p);}
  phase=Phase.RUN;all("Mac basladi! Dusman Kalelerini (beacon) yok et.");syncAll();
 }
 void snap(ServerPlayerEntity p,PD d){
  if(d.saved)return;
  d.inv=new ItemStack[p.getInventory().size()];
  for(int i=0;i<d.inv.length;i++)d.inv[i]=p.getInventory().getStack(i).copy();
  d.mode=p.interactionManager.getGameMode();d.back=p.getBlockPos();d.dim=p.getWorld().getRegistryKey().getValue().toString();d.saved=true;
 }
 void restore(ServerPlayerEntity p,PD d){
  if(!d.saved)return;
  p.getInventory().clear();
  for(int i=0;i<d.inv.length;i++)p.getInventory().setStack(i,d.inv[i]);
  p.changeGameMode(d.mode==null?GameMode.SURVIVAL:d.mode);p.clearStatusEffects();
  p.setHealth(p.getMaxHealth());p.getHungerManager().setFoodLevel(20);
  Compat.tp(sv,p,d.dim,d.back.getX()+.5,d.back.getY(),d.back.getZ()+.5);d.saved=false;
 }
 void kit(ServerPlayerEntity p,Team t){
  p.getInventory().clear();
  ItemStack w=new ItemStack(Items.BLAZE_ROD);w.set(DataComponentTypes.CUSTOM_NAME,Text.literal("Yapi Asasi"));
  ItemStack c=new ItemStack(Items.STICK);c.set(DataComponentTypes.CUSTOM_NAME,Text.literal("Komuta Asasi"));
  p.getInventory().insertStack(w);p.getInventory().insertStack(c);
  p.getInventory().insertStack(new ItemStack(Items.IRON_SWORD));
  p.getInventory().insertStack(new ItemStack(Items.COOKED_BEEF,16));
  p.equipStack(EquipmentSlot.CHEST,new ItemStack(Items.IRON_CHESTPLATE));
  p.equipStack(EquipmentSlot.FEET,new ItemStack(Items.IRON_BOOTS));
  if(t.race==Race.HUMANS){p.getInventory().insertStack(new ItemStack(Items.BOW));p.getInventory().insertStack(new ItemStack(Items.ARROW,32));}
  if(t.race==Race.NETHER)p.getInventory().insertStack(new ItemStack(Items.GOLDEN_APPLE,2));
  if(t.race==Race.END)p.getInventory().insertStack(new ItemStack(Items.ENDER_PEARL,8));
  p.setHealth(p.getMaxHealth());p.getHungerManager().setFoodLevel(20);p.getHungerManager().setSaturationLevel(5f);
  for(StatusEffectInstance f:t.race.player())p.addStatusEffect(f);
 }
 public void tick(){
  tick++;if(ar!=null)ar.run();
  switch(phase){
   case PREP->{if(bar!=null)bar.setPercent(ar.pct());if(ar.empty())begin();}
   case RUN->{if(tick%5==0)units();if(tick%10==0)cons();if(tick%20==0){second();syncAll();}}
   case CLEAN->{if(ar.empty())finish();}
   default->{if(tick%40==0)syncAll();}}
 }
 void cons(){
  for(Bld b:new ArrayList<>(blds.values())){
   if(b.done)continue;b.t-=10;
   aw().spawnParticles(ParticleTypes.HAPPY_VILLAGER,b.a.getX()+.5,b.a.getY()+1.5,b.a.getZ()+.5,3,.6,.5,.6,.02);
   if(b.t<=0){Team t=teams.get(b.team);ar.remove(b.bl);b.bl.clear();b.heart=ar.build(b.type,b.a,t.race,b.team,b.bl);b.done=true;blds.put(b.heart,b);
    aw().playSound(null,b.a,SoundEvents.BLOCK_ANVIL_USE,SoundCategory.BLOCKS,.8f,1.1f);tmsg(b.team,b.type.name+" tamamlandi.");}}
 }
 void units(){
  ServerWorld w=aw();Iterator<Un> it=units.iterator();
  while(it.hasNext()){
   Un u=it.next();Entity e=w.getEntity(u.id);
   if(!(e instanceof MobEntity m)||!m.isAlive()){ut.remove(u.id);it.remove();continue;}
   Team t=teams.get(u.team);
   if(!t.alive){m.discard();ut.remove(u.id);it.remove();continue;}
   LivingEntity tg=m.getTarget();
   if(tg!=null){Integer tt=teamOf(tg);if(tt!=null&&tt==u.team){m.setTarget(null);tg=null;}}
   if(tg==null){LivingEntity f=enemy(m.getBlockPos(),u.team,18);if(f!=null){m.setTarget(f);tg=f;}}
   if(!u.type.still&&tick%20==0){
    boolean fight=tg!=null&&m.squaredDistanceTo(tg)<100;
    if(!fight){BlockPos g=u.goal!=null?u.goal:t.rally;
     if(u.am){Bld nb=nearB(m.getBlockPos(),u.team,32);if(nb!=null)g=nb.heart;}
     if(m.getBlockPos().getSquaredDistance(g)>9)m.getNavigation().startMovingTo(g.getX()+.5,g.getY(),g.getZ()+.5,u.type.speed);}}
   if(tg==null&&tick%20==0){Bld nb=nearB(m.getBlockPos(),u.team,4);
    if(nb!=null){dmgB(nb,u.type.siege);w.spawnParticles(ParticleTypes.SMOKE,nb.heart.getX()+.5,nb.heart.getY()+1,nb.heart.getZ()+.5,4,.2,.2,.2,.02);}}
  }
 }
 void second(){
  ServerWorld w=aw();
  for(Team t:teams){t.su=0;t.un=0;t.bd=0;t.sc=0;}
  for(Un u:units){Team t=teams.get(u.team);t.su+=u.type.sup;t.un++;}
  int[]g=new int[teams.size()],wd=new int[teams.size()],st=new int[teams.size()],f=new int[teams.size()];
  for(Bld b:blds.values()){if(!b.done)continue;Team t=teams.get(b.team);t.bd++;
   t.sc+=b.type.sup()+(b.type==Build.HOUSE?t.race.sup:0);
   g[b.team]+=b.type.gi();wd[b.team]+=b.type.wi();st[b.team]+=b.type.si();f[b.team]+=b.type.fi();}
  double inc=cfg.income/100.0;
  for(Team t:teams){
   t.sc=Math.min(cfg.supCap,t.sc);
   t.gold=Data.cl(t.gold+(int)Math.round((g[t.i]+1)*inc*t.race.gold),0,99999);
   t.wood=Data.cl(t.wood+(int)Math.round((wd[t.i]+1)*inc),0,99999);
   t.stone=Data.cl(t.stone+(int)Math.round((st[t.i]+1)*inc),0,99999);
   t.food=Data.cl(t.food+(int)Math.round(f[t.i]*inc),0,99999);
   if(!t.q.isEmpty()){Ord o=t.q.get(0);o.t-=20;
    if(o.t<=0){if(t.su+o.u.sup<=t.sc&&t.un<cfg.unitCap){spawn(t,o);t.q.remove(0);}else o.t=0;}}}
  for(Bld b:new ArrayList<>(blds.values()))if(b.done&&b.type==Build.TOWER){
   LivingEntity tg=enemy(b.heart,b.team,18);
   if(tg!=null){Compat.hurt(tg,w,w.getDamageSources().magic(),6f);
    w.spawnParticles(ParticleTypes.CRIT,b.heart.getX()+.5,b.heart.getY()+5,b.heart.getZ()+.5,6,.3,.3,.3,.1);}}
  for(Node n:nodes.values())if(n.cd>0&&--n.cd<=0)Compat.set(w,n.p,n.s);
  left--;
  for(var e:ps.entrySet()){
   PD d=e.getValue();if(d.rc>0)d.rc--;
   ServerPlayerEntity p=sv.getPlayerManager().getPlayer(e.getKey());
   if(p==null||d.spec||d.team<0)continue;
   if(p.getWorld()!=w||!inside(p.getBlockPos(),-2)){Team t=teams.get(d.team);Compat.tp(sv,p,DIM,t.spawn.getX()+.5,t.spawn.getY(),t.spawn.getZ()+.5);}
   Team t=teams.get(d.team);
   p.sendMessage(Text.literal("A:"+t.gold+" O:"+t.wood+" T:"+t.stone+" E:"+t.food+" Birlik:"+t.su+"/"+t.sc+" ["+d.sel.name+"]"),true);}
  ScoreboardObjective ob=Compat.obj(sv,SIDE,Text.literal("REIGN - "+cfg.map.name));
  Compat.line(sv,ob,"Sure",Math.max(0,left));
  for(Team t:teams){int core=0;for(Bld b:blds.values())if(b.team==t.i&&b.type==Build.CORE)core++;
   Compat.line(sv,ob,Data.NAME[t.i]+" Yapi",t.bd);Compat.line(sv,ob,Data.NAME[t.i]+" Ordu",t.un);Compat.line(sv,ob,Data.NAME[t.i]+" Kale",core);}
  if(bar!=null){bar.setPercent(Math.max(0f,Math.min(1f,left/(float)(cfg.min*60))));
   bar.setName(Text.literal("Reign | "+Math.max(0,left)/60+"dk "+Math.max(0,left)%60+"sn | "+cfg.map.name));}
  victory();
 }
 void victory(){
  for(Team t:teams){if(!t.alive)continue;boolean core=false;
   for(Bld b:blds.values())if(b.team==t.i&&b.type==Build.CORE){core=true;break;}
   if(!core){t.alive=false;all(Data.NAME[t.i]+" takimi elendi!");}}
  int alive=0;for(Team t:teams)if(t.alive)alive++;
  if(alive<=1||left<=0)end();
 }
 void end(){
  Team win=null;for(Team t:teams)if(t.alive&&(win==null||t.score()>win.score()))win=t;
  all(win==null?"Berabere!":"Kazanan: "+Data.NAME[win.i]+" ("+win.score()+" puan)");
  for(UUID u:ps.keySet()){ServerPlayerEntity p=sv.getPlayerManager().getPlayer(u);if(p!=null)restore(p,ps.get(u));}
  ServerWorld w=aw();
  for(Un u:units){Entity e=w.getEntity(u.id);if(e!=null)e.discard();}
  units.clear();ut.clear();blds.clear();nodes.clear();
  ar.clear(cfg.size);
  if(bar!=null){bar.clearPlayers();bar=null;}
  Compat.noside(sv);Compat.del(sv,SIDE);
  Compat.in(sv,DIM,"gamerule doMobSpawning true");Compat.in(sv,DIM,"gamerule mobGriefing true");
  phase=Phase.CLEAN;all("Arena temizleniyor...");syncAll();
 }
 void finish(){force(false);ar=null;teams.clear();ps.clear();phase=Phase.LOBBY;all("Mac bitti. Lider R ile yeni mac kurabilir.");syncAll();}
 public void stop(){if(phase==Phase.LOBBY||phase==Phase.CLEAN)return;left=0;for(Team t:teams)t.alive=false;end();}
 void leave(ServerPlayerEntity p){PD d=pd(p.getUuid());if(d==null){msg(p,"Macta degilsin.");return;}restore(p,d);ps.remove(p.getUuid());if(bar!=null)bar.removePlayer(p);msg(p,"Ayrildin, esyalarin geri verildi.");}
 public void shutdown(){
  for(var e:ps.entrySet()){ServerPlayerEntity p=sv.getPlayerManager().getPlayer(e.getKey());if(p!=null)restore(p,e.getValue());}
  ServerWorld w=aw();if(w!=null)for(Un u:units){Entity e=w.getEntity(u.id);if(e!=null)e.discard();}
  if(bar!=null)bar.clearPlayers();Compat.noside(sv);Compat.del(sv,SIDE);phase=Phase.LOBBY;
 }
 void force(boolean on){int h=cfg.size/2+8;
  Compat.in(sv,DIM,(on?"forceload add ":"forceload remove ")+(O.getX()-h)+" "+(O.getZ()-h)+" "+(O.getX()+h)+" "+(O.getZ()+h));}
 boolean inside(BlockPos p,int m){int h=cfg.size/2-m;
  return Math.abs(p.getX()-O.getX())<=h&&Math.abs(p.getZ()-O.getZ())<=h&&p.getY()>O.getY()-6&&p.getY()<O.getY()+24;}
 Integer teamOf(Entity e){if(e instanceof ServerPlayerEntity p){PD d=pd(p.getUuid());return d==null||d.spec?null:d.team;}return ut.get(e.getUuid());}
 LivingEntity enemy(BlockPos pos,int team,double r){
  ServerWorld w=aw();double bd=r*r;LivingEntity best=null;
  for(Un u:units){if(u.team==team||!teams.get(u.team).alive)continue;Entity e=w.getEntity(u.id);
   if(e instanceof LivingEntity l&&l.isAlive()){double d=pos.getSquaredDistance(l.getBlockPos());if(d<bd){bd=d;best=l;}}}
  for(var e:ps.entrySet()){PD d=e.getValue();
   if(d.spec||d.team<0||d.team==team||d.rc>0||!teams.get(d.team).alive)continue;
   ServerPlayerEntity p=sv.getPlayerManager().getPlayer(e.getKey());if(p==null||!p.isAlive())continue;
   double dd=pos.getSquaredDistance(p.getBlockPos());if(dd<bd){bd=dd;best=p;}}
  return best;
 }
 Bld nearB(BlockPos from,int team,double r){Bld best=null;double bd=r*r;
  for(Bld b:blds.values()){if(b.team==team||!b.done||!teams.get(b.team).alive)continue;
   double d=from.getSquaredDistance(b.heart);if(d<bd){bd=d;best=b;}}return best;}
 void dmgB(Bld b,float dmg){
  b.hp-=dmg;if(b.hp>0)return;
  ServerWorld w=aw();ar.remove(b.bl);blds.remove(b.heart);
  w.spawnParticles(ParticleTypes.EXPLOSION,b.heart.getX()+.5,b.heart.getY()+1,b.heart.getZ()+.5,6,.8,.8,.8,.02);
  w.playSound(null,b.heart,SoundEvents.ENTITY_GENERIC_EXPLODE,SoundCategory.BLOCKS,.9f,1f);
  all(Data.NAME[b.team]+" takiminin "+b.type.name+" yapisi yikildi!");syncAll();
 }
 void train(ServerPlayerEntity p,int slot){
  PD d=pd(p.getUuid());if(!part(d)){msg(p,"Macta degilsin.");return;}
  Team t=teams.get(d.team);Unit u=Unit.of(t.race,Data.cl(slot,1,3));if(u==null)return;
  Bld src=null;for(Bld b:blds.values())if(b.team==t.i&&b.done&&b.type==u.need()){src=b;break;}
  if(src==null){msg(p,"Gerekli yapi: "+u.need().name);return;}
  if(t.gold<u.gold||t.food<u.food){msg(p,"Kaynak yetersiz: "+u.gold+"A "+u.food+"E");return;}
  if(t.q.size()>=5){msg(p,"Uretim kuyrugu dolu.");return;}
  if(t.un+t.q.size()>=cfg.unitCap){msg(p,"Birlik limiti dolu.");return;}
  t.gold-=u.gold;t.food-=u.food;
  Ord o=new Ord();o.u=u;o.t=u.ticks;o.at=src.a;t.q.add(o);
  msg(p,u.name+" uretime alindi ("+(u.ticks/20)+"sn, kuyruk "+t.q.size()+")");syncAll();
 }
 void spawn(Team t,Ord o){
  ServerWorld w=aw();BlockPos at=o.at.add(2,1,2);
  for(int i=0;i<8&&!w.getBlockState(at).isAir();i++)at=at.add(1,0,1);
  MobEntity m=Compat.mob(w,o.u.type,at);
  if(m==null){t.gold+=o.u.gold;t.food+=o.u.food;return;}
  m.setPersistent();m.setCanPickUpLoot(false);
  m.setCustomName(Text.literal(Data.NAME[t.i]+" "+o.u.name));m.setCustomNameVisible(true);
  if(o.u.weapon!=null)m.equipStack(EquipmentSlot.MAINHAND,new ItemStack(o.u.weapon));
  if(o.u.type==EntityType.PILLAGER)m.equipStack(EquipmentSlot.OFFHAND,new ItemStack(Items.ARROW,64));
  for(StatusEffectInstance f:t.race.unit())m.addStatusEffect(f);
  Un u=new Un();u.id=m.getUuid();u.team=t.i;u.type=o.u;u.goal=t.rally;units.add(u);ut.put(u.id,t.i);
  w.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,at.getX()+.5,at.getY()+1,at.getZ()+.5,12,.3,.4,.3,.02);
  tmsg(t.i,o.u.name+" hazir.");
 }
 void rally(ServerPlayerEntity p,BlockPos pos,boolean all){
  PD d=pd(p.getUuid());if(!part(d))return;Team t=teams.get(d.team);t.rally=pos.toImmutable();
  int n=0;ServerWorld w=aw();
  for(Un u:units){if(u.team!=t.i)continue;Entity e=w.getEntity(u.id);if(e==null)continue;
   if(!all&&e.getBlockPos().getSquaredDistance(p.getBlockPos())>1024)continue;
   u.goal=pos.toImmutable();u.am=true;n++;}
  w.spawnParticles(ParticleTypes.END_ROD,pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,10,.2,.5,.2,.01);
  msg(p,"Toplanma/saldiri noktasi ("+n+" birlik)");
 }
 void demo(ServerPlayerEntity p,BlockPos pos){
  PD d=pd(p.getUuid());if(!part(d))return;Bld b=blds.get(pos);
  if(b==null){msg(p,"Burada yapi yok.");return;}
  if(b.team!=d.team){msg(p,"Dushman yapisini sokeemezsin, saldir.");return;}
  if(b.type==Build.CORE){msg(p,"Kale sokulemez.");return;}
  Team t=teams.get(d.team);t.gold+=b.type.gold/2;t.wood+=b.type.wood/2;t.stone+=b.type.stone/2;
  ar.remove(b.bl);blds.remove(b.heart);tmsg(d.team,b.type.name+" sokuldu (%50 iade).");syncAll();
 }
 public ActionResult use(ServerPlayerEntity p,Hand h,BlockPos clicked){
  PD d=pd(p.getUuid());if(phase!=Phase.RUN||d==null||d.spec)return ActionResult.PASS;
  ItemStack s=p.getStackInHand(h);
  if(s.isOf(Items.BLAZE_ROD)){if(p.isSneaking())demo(p,clicked);else place(p,d,clicked.up());return ActionResult.SUCCESS;}
  if(s.isOf(Items.STICK)){rally(p,clicked.up(),p.isSneaking());return ActionResult.SUCCESS;}
  if(s.getItem() instanceof BlockItem){msg(p,"Blok yerlestirme kapali. B menusu + Yapi Asasi kullan.");return ActionResult.FAIL;}
  return ActionResult.PASS;
 }
 void place(ServerPlayerEntity p,PD d,BlockPos a){
  Team t=teams.get(d.team);Build ty=d.sel;
  int g=(int)Math.round(ty.gold*t.race.cost),w=(int)Math.round(ty.wood*t.race.cost),s=(int)Math.round(ty.stone*t.race.cost);
  if(t.gold<g||t.wood<w||t.stone<s){msg(p,"Kaynak yetersiz: "+g+"A "+w+"O "+s+"T");return;}
  int cnt=0;for(Bld b:blds.values())if(b.team==t.i)cnt++;
  if(cnt>=40){msg(p,"Yapi limiti (40) dolu.");return;}
  if(!inside(a,ty.w/2+2)){msg(p,"Arena disina yapi kurulamaz.");return;}
  boolean near=false;
  for(Bld b:blds.values())if(b.team==t.i&&b.done&&b.a.getSquaredDistance(a)<=(double)cfg.rad*cfg.rad){near=true;break;}
  if(!near){msg(p,"Kendi yapina "+cfg.rad+" blok yakininda olmali.");return;}
  ServerWorld ww=aw();int hw=ty.w/2,hd=ty.d/2;
  for(int x=-hw;x<=hw;x++)for(int z=-hd;z<=hd;z++)for(int y=0;y<=ty.h;y++)
   if(!ww.getBlockState(a.add(x,y,z)).isAir()){msg(p,"Alan dolu.");return;}
  for(Bld b:blds.values())
   if(Math.abs(b.a.getX()-a.getX())<=(b.type.w+ty.w)/2&&Math.abs(b.a.getZ()-a.getZ())<=(b.type.d+ty.d)/2){msg(p,"Baska yapiyla cakisiyor.");return;}
  t.gold-=g;t.wood-=w;t.stone-=s;
  Bld b=new Bld();b.type=ty;b.team=t.i;b.a=a.toImmutable();b.t=ty.ticks;b.hp=ty.hp;b.heart=a.up();
  ar.scaffold(ty,a,Data.TEAM[t.i],b.bl);blds.put(b.heart,b);
  ww.playSound(null,a,SoundEvents.BLOCK_WOOD_PLACE,SoundCategory.BLOCKS,.8f,.9f);
  tmsg(t.i,ty.name+" insa ediliyor ("+(ty.ticks/20)+"sn)");syncAll();
 }
 public ActionResult attack(ServerPlayerEntity p,BlockPos pos){
  PD d=pd(p.getUuid());if(!part(d))return ActionResult.PASS;
  Bld b=blds.get(pos);if(b==null)return ActionResult.PASS;
  if(b.team==d.team){msg(p,b.type.name+" HP: "+(int)b.hp+"/"+b.type.hp);return ActionResult.FAIL;}
  long now=p.getWorld().getTime();
  if(now-d.hit>=6){d.hit=now;dmgB(b,14);msg(p,b.type.name+" HP: "+(int)Math.max(0,b.hp));}
  return ActionResult.FAIL;
 }
 public boolean canBreak(ServerPlayerEntity p,BlockPos pos){
  PD d=pd(p.getUuid());if(phase!=Phase.RUN||d==null||d.spec)return true;
  if(nodes.containsKey(pos))return true;
  if(blds.containsKey(pos))return false;
  msg(p,"Zemin kirilamaz. Kaynak dugumlerini (cevher/kutuk/tas/saman) kir.");return false;
 }
 public void afterBreak(ServerPlayerEntity p,BlockPos pos,BlockState old){
  PD d=pd(p.getUuid());if(!part(d))return;Node n=nodes.get(pos);if(n==null)return;
  Team t=teams.get(d.team);String s;
  if(old.isOf(cfg.map.ore.getBlock())){t.gold+=15;s="+15 Altin";}
  else if(old.isOf(cfg.map.log.getBlock())){t.wood+=12;s="+12 Odun";}
  else if(old.isOf(Blocks.HAY_BLOCK)){t.food+=10;s="+10 Erzak";}
  else{t.stone+=12;s="+12 Tas";}
  n.cd=cfg.nodeResp;msg(p,s+" ("+cfg.nodeResp+"sn sonra yenilenir)");
 }
 public boolean dmg(LivingEntity v,DamageSource src,float amt){
  if(phase!=Phase.RUN)return true;
  Entity at=src.getAttacker();Integer vt=teamOf(v),aa=at==null?null:teamOf(at);
  if(aa!=null&&vt==null)return false;
  if(vt!=null&&aa!=null&&vt.equals(aa)&&!cfg.ff)return false;
  if(v instanceof ServerPlayerEntity p){PD d=pd(p.getUuid());
   if(d!=null&&(d.spec||d.rc>0))return false;
   if(!cfg.pvp&&at instanceof ServerPlayerEntity)return false;}
  return true;
 }
 public boolean death(LivingEntity v,DamageSource src,float amt){
  if(phase!=Phase.RUN||!(v instanceof ServerPlayerEntity p))return true;
  PD d=pd(p.getUuid());if(d==null||d.spec||d.team<0)return true;
  Team t=teams.get(d.team);t.dth++;t.gold=Math.max(0,t.gold-25);
  p.setHealth(p.getMaxHealth());p.getHungerManager().setFoodLevel(20);p.clearStatusEffects();
  for(StatusEffectInstance f:t.race.player())p.addStatusEffect(f);
  p.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS,120,0,false,false));
  d.rc=6;Compat.tp(sv,p,DIM,t.spawn.getX()+.5,t.spawn.getY(),t.spawn.getZ()+.5);
  tmsg(d.team,p.getName().getString()+" dustu (-25 altin, 6sn bekleme)");
  return false;
 }
 public void syncAll(){for(ServerPlayerEntity p:sv.getPlayerManager().getPlayerList())sync(p);}
 public void sync(ServerPlayerEntity p){
  JsonObject r=new JsonObject();
  r.addProperty("phase",phase.name());r.addProperty("leader",isL(p.getUuid()));r.addProperty("rev",cfg.rev);
  r.addProperty("left",left);r.addProperty("map",cfg.map.id);r.addProperty("size",cfg.size);r.addProperty("teams",cfg.teams);
  JsonArray rc=new JsonArray();for(Race x:cfg.race)rc.add(x.id);r.add("race",rc);
  JsonObject c=new JsonObject();
  c.addProperty("startGold",cfg.gold);c.addProperty("startWood",cfg.wood);c.addProperty("startStone",cfg.stone);c.addProperty("startFood",cfg.food);
  c.addProperty("supplyCap",cfg.supCap);c.addProperty("unitCap",cfg.unitCap);c.addProperty("minutes",cfg.min);
  c.addProperty("income",cfg.income);c.addProperty("nodeResp",cfg.nodeResp);c.addProperty("buildRad",cfg.rad);
  c.addProperty("ff",cfg.ff);c.addProperty("pvp",cfg.pvp);r.add("cfg",c);
  PD me=pd(p.getUuid());JsonObject mj=new JsonObject();
  mj.addProperty("team",me==null?-1:me.team);mj.addProperty("sel",me==null?"house":me.sel.id);r.add("me",mj);
  JsonArray ts=new JsonArray();
  for(Team t:teams){JsonObject o=new JsonObject();
   o.addProperty("i",t.i);o.addProperty("race",t.race.id);o.addProperty("alive",t.alive);
   o.addProperty("gold",t.gold);o.addProperty("wood",t.wood);o.addProperty("stone",t.stone);o.addProperty("food",t.food);
   o.addProperty("su",t.su);o.addProperty("sc",t.sc);o.addProperty("un",t.un);o.addProperty("bd",t.bd);o.addProperty("q",t.q.size());
   ts.add(o);}
  r.add("ts",ts);
  JsonArray pl=new JsonArray();
  for(ServerPlayerEntity o:sv.getPlayerManager().getPlayerList()){JsonObject j=new JsonObject();
   j.addProperty("name",o.getName().getString());j.addProperty("asg",asg.getOrDefault(o.getUuid(),-2));
   PD d=pd(o.getUuid());j.addProperty("team",d==null?-1:d.team);pl.add(j);}
  r.add("pl",pl);
  Reign.send(p,r.toString());
 }
}