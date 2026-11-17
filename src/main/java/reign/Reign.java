package reign;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class Reign implements ModInitializer{
 @Override public void onInitialize(){
  CommandRegistrationCallback.EVENT.register((d,acc,env)->{
   d.register(CommandManager.literal("setleader").requires(Compat::op)
    .then(CommandManager.argument("player",EntityArgumentType.player()).executes(c->{
     Game.of(c.getSource().getServer()).leader(EntityArgumentType.getPlayer(c,"player"));return 1;})));
   d.register(CommandManager.literal("reign").executes(c->act(c.getSource(),"info"))
    .then(CommandManager.argument("args",StringArgumentType.greedyString())
     .executes(c->act(c.getSource(),StringArgumentType.getString(c,"args")))));
  });
  UseBlockCallback.EVENT.register((p,w,h,hit)->{
   if(!(w instanceof ServerWorld sw)||!(p instanceof ServerPlayerEntity sp))return ActionResult.PASS;
   return Game.of(sw.getServer()).use(sp,h,hit.getBlockPos());});
  AttackBlockCallback.EVENT.register((p,w,h,pos,dir)->{
   if(!(w instanceof ServerWorld sw)||!(p instanceof ServerPlayerEntity sp))return ActionResult.PASS;
   return Game.of(sw.getServer()).attack(sp,pos);});
  PlayerBlockBreakEvents.BEFORE.register((w,p,pos,st,be)->{
   if(!(w instanceof ServerWorld sw)||!(p instanceof ServerPlayerEntity sp))return true;
   return Game.of(sw.getServer()).canBreak(sp,pos);});
  PlayerBlockBreakEvents.AFTER.register((w,p,pos,st,be)->{
   if(!(w instanceof ServerWorld sw)||!(p instanceof ServerPlayerEntity sp))return;
   Game.of(sw.getServer()).afterBreak(sp,pos,st);});
  ServerLivingEntityEvents.ALLOW_DAMAGE.register((e,s,a)->{
   if(!(e.getEntityWorld() instanceof ServerWorld sw))return true;
   return Game.of(sw.getServer()).dmg(e,s,a);});
  ServerLivingEntityEvents.ALLOW_DEATH.register((e,s,a)->{
   if(!(e.getEntityWorld() instanceof ServerWorld sw))return true;
   return Game.of(sw.getServer()).death(e,s,a);});
  ServerTickEvents.END_SERVER_TICK.register(s->Game.of(s).tick());
  ServerLifecycleEvents.SERVER_STOPPING.register(s->{Game.of(s).shutdown();Game.drop(s);});
 }
 static int act(ServerCommandSource s,String l){
  ServerPlayerEntity p=s.getPlayer();
  if(p==null){s.sendMessage(Text.literal("Bu komut oyuncu gerektirir."));return 0;}
  Game.of(s.getServer()).act(p,l);return 1;}
}
