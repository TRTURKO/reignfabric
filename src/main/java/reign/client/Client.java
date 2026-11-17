package reign.client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer{
 public static MinecraftClient get(){return MinecraftClient.getInstance();}
 public static void send(String c){if(get().player!=null)get().player.networkHandler.sendChatCommand("reign "+c);}
 @Override public void onInitializeClient(){
  KeyBinding.Category cat=KeyBinding.Category.create(Identifier.of("reignfabric","main"));
  KeyBinding r=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.reignfabric.setup",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_R,cat));
  KeyBinding b=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.reignfabric.build",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_B,cat));
  KeyBinding n=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.reignfabric.units",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_N,cat));
  ClientTickEvents.END_CLIENT_TICK.register(cl->{
   while(r.wasPressed())cl.setScreen(new Menus.Setup());
   while(b.wasPressed())cl.setScreen(new Menus.BuildMenu());
   while(n.wasPressed())cl.setScreen(new Menus.UnitMenu());});
 }
}
