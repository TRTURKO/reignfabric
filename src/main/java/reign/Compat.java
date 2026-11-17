package reign;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class Compat{
 private Compat(){}
 public static boolean op(ServerCommandSource s){
  return CommandManager.<ServerCommandSource>requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK).test(s);}
 public static void run(MinecraftServer s,String c){s.getCommandManager().parseAndExecute(s.getCommandSource(),c);}
 public static void in(MinecraftServer s,String dim,String c){run(s,"execute in "+dim+" run "+c);}
 public static void tp(MinecraftServer s,ServerPlayerEntity p,String dim,double x,double y,double z){
  in(s,dim,"tp "+p.getUuid().toString()+" "+x+" "+y+" "+z);}
 public static MobEntity mob(ServerWorld w,EntityType<?> t,BlockPos p){
  Entity e=t.spawn(w,p,SpawnReason.MOB_SUMMONED);return e instanceof MobEntity m?m:null;}
 public static void set(ServerWorld w,BlockPos p,BlockState s){w.setBlockState(p,s,2);}
 public static boolean hurt(Entity e,ServerWorld w,DamageSource s,float f){return e.damage(w,s,f);}
}
