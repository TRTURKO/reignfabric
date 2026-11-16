package reign;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.BlockPos;
public final class Compat{
 private Compat(){}
 public static boolean op(ServerCommandSource s){return s.hasPermissionLevel(2);}
 public static ServerPlayerEntity pl(ServerCommandSource s)throws Exception{return s.getPlayerOrThrow();}
 public static void run(MinecraftServer s,String c){s.getCommandManager().executeWithPrefix(s.getCommandSource().withSilent(),c);}
 public static void in(MinecraftServer s,String d,String c){run(s,"execute in "+d+" run "+c);}
 public static void tp(MinecraftServer s,ServerPlayerEntity p,String d,double x,double y,double z){in(s,d,"tp "+p.getUuidAsString()+" "+x+" "+y+" "+z);}
 public static MobEntity mob(ServerWorld w,EntityType<?> t,BlockPos p){Entity e=t.spawn(w,p,SpawnReason.MOB_SUMMONED);return e instanceof MobEntity m?m:null;}
 public static void set(ServerWorld w,BlockPos p,BlockState s){w.setBlockState(p,s,2);}
 public static boolean hurt(Entity e,ServerWorld w,DamageSource s,float f){return e.damage(w,s,f);}
 public static BlockPos look(ServerPlayerEntity p,double d){HitResult h=p.raycast(d,0f,false);return h.getType()==HitResult.Type.BLOCK?((BlockHitResult)h).getBlockPos():null;}
 public static ScoreboardObjective obj(MinecraftServer s,String id,Text t){Scoreboard b=s.getScoreboard();ScoreboardObjective o=b.getObjective(id);if(o==null)o=b.addObjective(id,ObjectiveCriteria.DUMMY,t,ObjectiveCriteria.RenderType.INTEGER);return o;}
 public static void line(MinecraftServer s,ScoreboardObjective o,String n,int v){s.getScoreboard().getOrCreateScore(ScoreHolder.fromName(n),o).setScore(v);}
 public static void side(MinecraftServer s,String id){run(s,"scoreboard objectives setdisplay sidebar "+id);}
 public static void noside(MinecraftServer s){run(s,"scoreboard objectives setdisplay sidebar");}
 public static void del(MinecraftServer s,String id){ScoreboardObjective o=s.getScoreboard().getObjective(id);if(o!=null)s.getScoreboard().removeObjective(o);}
}