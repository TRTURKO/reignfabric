package reign;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
public class Reign implements ModInitializer{
 public record Cmd(String l) implements CustomPayload{
  public static final Id<Cmd> ID=new Id<>(Identifier.of("reignfabric","cmd"));
  public static final PacketCodec<RegistryByteBuf,Cmd> CODEC=PacketCodec.tuple(PacketCodecs.STRING,Cmd::l,Cmd::new);
  public Id<? extends CustomPayload> getId(){return ID;}}
 public record Syn(String j) implements CustomPayload{
  public static final Id<Syn> ID=new Id<>(Identifier.of("reignfabric","syn"));
  public static final PacketCodec<RegistryByteBuf,Syn> CODEC=PacketCodec.tuple(PacketCodecs.STRING,Syn::j,Syn::new);
  public Id<? extends CustomPayload> getId(){return ID;}}
 public static void send(ServerPlayerEntity p,String json){if(ServerPlayNetworking.canSend(p,Syn.ID))ServerPlayNetworking.send(p,new Syn(json));}
 @Override public void onInitialize(){
  PayloadTypeRegistry.playC2S().register(Cmd.ID,Cmd.CODEC);
  PayloadTypeRegistry.playS2C().register(Syn.ID,Syn.CODEC);
  ServerPlayNetworking.registerGlobalReceiver(Cmd.ID,(pl,ctx)->{ServerPlayerEntity p=ctx.player();ctx.server().execute(()->Game.of(ctx.server()).act(p,pl.l()));});
  CommandRegistrationCallback.EVENT.register((d,acc,env)->{
   d.register(CommandManager.literal("setleader").requires(Compat::op)
    .then(CommandManager.argument("player",EntityArgumentType.player()).executes(c->{
     Game.of(c.getSource().getServer()).leader(EntityArgumentType.getPlayer(c,"player"));return 1;})));
   d.register(CommandManager.literal("reign").executes(c->act(c.getSource(),"info"))
    .then(CommandManager.argument("args",StringArgumentType.greedyString()).executes(c->act(c.getSource(),StringArgumentType.getString(c,"args")))));
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
   if(!(e.getWorld() instanceof ServerWorld sw))return true;
   return Game.of(sw.getServer()).dmg(e,s,a);});
  ServerLivingEntityEvents.ALLOW_DEATH.register((e,s,a)->{
   if(!(e.getWorld() instanceof ServerWorld sw))return true;
   return Game.of(sw.getServer()).death(e,s,a);});
  ServerTickEvents.END_SERVER_TICK.register(s->Game.of(s).tick());
  ServerPlayConnectionEvents.JOIN.register((h,s,srv)->Game.of(srv).sync(h.player));
  ServerLifecycleEvents.SERVER_STOPPING.register(s->{Game.of(s).shutdown();Game.drop(s);});
 }
 static int act(ServerCommandSource s,String l){
  try{Game.of(s.getServer()).act(Compat.pl(s),l);return 1;}
  catch(Exception e){s.sendFeedback(()->Text.literal("Bu komut oyuncu gerektirir."),false);return 0;}}
}