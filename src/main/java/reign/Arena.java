package reign;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import reign.Data.*;
import java.util.*;
public class Arena{
 record Job(BlockPos p,BlockState s){}
 final ServerWorld w;final ArrayDeque<Job> q=new ArrayDeque<>();int total,done;
 public Arena(ServerWorld w){this.w=w;}
 void put(BlockPos p,BlockState s){q.add(new Job(p,s));total++;}
 public boolean empty(){return q.isEmpty();}
 public float pct(){return total==0?1:(float)done/total;}
 public void run(){int n=0;while(!q.isEmpty()&&n<3500){Job j=q.peekFirst();if(!w.isChunkLoaded(j.p()))break;q.pollFirst();Compat.set(w,j.p(),j.s());n++;done++;}}
 void box(int x1,int y1,int z1,int x2,int y2,int z2,BlockState s,List<BlockPos> o){for(int x=x1;x<=x2;x++)for(int y=y1;y<=y2;y++)for(int z=z1;z<=z2;z++){BlockPos p=new BlockPos(x,y,z);put(p,s);if(o!=null)o.add(p);}}
 public static BlockPos pad(int size,int teams,int i){int h=size/2-14;int[][]c={{-h,-h},{h,h},{-h,h},{h,-h}};int[]k=c[i%4];return Game.O.add(k[0],0,k[1]);}
 void cluster(BlockPos c,MapDef m,Map<BlockPos,BlockState> nodes,int g,int wd,int st,int f,Random r){
  int i=0;BlockState stone=Blocks.STONE.getDefaultState(),hay=Blocks.HAY_BLOCK.getDefaultState();
  for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
   if(i>=g+wd+st+f)return;if(r.nextInt(3)==0)continue;
   BlockPos p=c.add(dx,0,dz);
   BlockState s=i<g?m.ore:i<g+wd?m.log:i<g+wd+st?stone:hay;
   put(p,s);nodes.put(p.toImmutable(),s);
   if(s==m.log){put(p.up(),s);nodes.put(p.up().toImmutable(),s);}
   i++;}
 }
 public void gen(Cfg c,Map<BlockPos,BlockState> nodes){
  MapDef m=c.map;int h=c.size/2,y=Game.O.getY(),x1=Game.O.getX()-h,x2=Game.O.getX()+h,z1=Game.O.getZ()-h,z2=Game.O.getZ()+h;
  Random r=new Random(7+m.ordinal()*31+c.size);BlockState bar=Blocks.BARRIER.getDefaultState();
  for(int x=x1;x<=x2;x++)for(int z=z1;z<=z2;z++){
   put(new BlockPos(x,y,z),r.nextInt(8)==0?m.sub:m.floor);
   put(new BlockPos(x,y-1,z),m.sub);put(new BlockPos(x,y-2,z),m.sub);
   if(x==x1||x==x2||z==z1||z==z2){box(x,y+1,z,x,y+3,z,m.wall,null);box(x,y+4,z,x,y+12,z,bar,null);}
   else if((x-x1)%14==7&&(z-z1)%14==7)put(new BlockPos(x,y,z),m.light);}
  for(int i=0;i<c.teams;i++){
   BlockPos p=pad(c.size,c.teams,i);BlockState acc=Data.TEAM[i];
   box(p.getX()-6,y,p.getZ()-6,p.getX()+6,y,p.getZ()+6,m.wall,null);
   for(int k=-6;k<=6;k++){put(p.add(k,0,-6),acc);put(p.add(k,0,6),acc);put(p.add(-6,0,k),acc);put(p.add(6,0,k),acc);}
   for(int[]d:new int[][]{{-6,-6},{6,-6},{-6,6},{6,6}}){box(p.getX()+d[0],y+1,p.getZ()+d[1],p.getX()+d[0],y+3,p.getZ()+d[1],m.wall,null);put(p.add(d[0],4,d[1]),m.light);}
   cluster(p.add(Integer.signum(Game.O.getX()-p.getX())*15,1,Integer.signum(Game.O.getZ()-p.getZ())*15),m,nodes,5,6,6,3,r);}
  cluster(Game.O.up(),m,nodes,9,7,7,5,r);
  int rr=h-8;
  for(int k=-rr;k<=rr;k++){put(Game.O.add(k,0,-rr),m.hazard);put(Game.O.add(k,0,rr),m.hazard);put(Game.O.add(-rr,0,k),m.hazard);put(Game.O.add(rr,0,k),m.hazard);}
  for(int[]d:new int[][]{{0,-1},{0,1},{-1,0},{1,0}})for(int t=rr-1;t<=rr+1;t++)
   box(Game.O.getX()+d[0]*t-1,y,Game.O.getZ()+d[1]*t-1,Game.O.getX()+d[0]*t+1,y,Game.O.getZ()+d[1]*t+1,m.wall,null);
 }
 public void scaffold(Build b,BlockPos a,BlockState acc,List<BlockPos> o){
  int hw=b.w/2,hd=b.d/2;BlockState sc=Blocks.SCAFFOLDING.getDefaultState();
  for(int x=-hw;x<=hw;x++)for(int z=-hd;z<=hd;z++){BlockPos p=a.add(x,0,z);put(p,Math.abs(x)==hw||Math.abs(z)==hd?acc:sc);o.add(p);}
 }
 public BlockPos build(Build b,BlockPos a,Race r,int team,List<BlockPos> o){
  int hw=b.w/2,hd=b.d/2,y=a.getY();BlockState acc=Data.TEAM[team],wall=r.wall,fl=r.floor;
  box(a.getX()-hw,y,a.getZ()-hd,a.getX()+hw,y,a.getZ()+hd,fl,o);
  BlockPos heart=a.up();
  if(b==Build.WALL){box(a.getX()-hw,y+1,a.getZ(),a.getX()+hw,y+b.h,a.getZ(),wall,o);put(heart,b.heart.getDefaultState());o.add(heart);return heart;}
  if(b==Build.FARM){
   for(int x=-hw+1;x<=hw-1;x++)for(int z=-hd+1;z<=hd-1;z++){BlockPos p=a.add(x,0,z);put(p,Blocks.FARMLAND.getDefaultState());o.add(p);put(p.up(),Blocks.WHEAT.getDefaultState().with(CropBlock.AGE,7));o.add(p.up());}
   put(heart,b.heart.getDefaultState());o.add(heart);put(heart.up(2),acc);o.add(heart.up(2));return heart;}
  int top=y+b.h-1;
  boolean dx=Math.abs(Game.O.getX()-a.getX())>Math.abs(Game.O.getZ()-a.getZ());
  for(int x=-hw;x<=hw;x++)for(int z=-hd;z<=hd;z++){
   if(Math.abs(x)!=hw&&Math.abs(z)!=hd)continue;
   boolean corner=Math.abs(x)==hw&&Math.abs(z)==hd;
   boolean door=dx?(x==Integer.signum(Game.O.getX()-a.getX())*hw&&z==0):(z==Integer.signum(Game.O.getZ()-a.getZ())*hd&&x==0);
   for(int yy=y+1;yy<=top;yy++){if(door&&yy<=y+2)continue;BlockPos p=a.add(x,yy-y,z);put(p,corner?acc:yy==top?fl:wall);o.add(p);}}
  if(b==Build.TOWER){
   for(int x=-hw;x<=hw;x++)for(int z=-hd;z<=hd;z++)if(Math.abs(x)==hw||Math.abs(z)==hd){put(a.add(x,b.h,z),acc);o.add(a.add(x,b.h,z));}
   put(a.up(b.h),Blocks.GLOWSTONE.getDefaultState());o.add(a.up(b.h));
  }else{
   box(a.getX()-hw,top+1,a.getZ()-hd,a.getX()+hw,top+1,a.getZ()+hd,fl,o);
   put(a.up(b.h+1),acc);o.add(a.up(b.h+1));
   if(b==Build.CORE){put(a.up(b.h+3),Data.GL[team]);o.add(a.up(b.h+3));}}
  put(heart,b.heart.getDefaultState());o.add(heart);
  return heart;
 }
 public void remove(List<BlockPos> l){BlockState air=Blocks.AIR.getDefaultState();for(BlockPos p:l)if(!w.getBlockState(p).isAir())put(p,air);}
 public void clear(int size){int h=size/2+2;BlockState air=Blocks.AIR.getDefaultState();
  for(int x=Game.O.getX()-h;x<=Game.O.getX()+h;x++)for(int z=Game.O.getZ()-h;z<=Game.O.getZ()+h;z++)for(int y=Game.O.getY()-4;y<=Game.O.getY()+16;y++){BlockPos p=new BlockPos(x,y,z);if(!w.getBlockState(p).isAir())put(p,air);}}
}