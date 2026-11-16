package reign.client;
import com.google.gson.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import reign.Reign;
import java.util.*;
@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer{
 public static MinecraftClient get(){return MinecraftClient.getInstance();}
 public static void send(String c){
  if(ClientPlayNetworking.canSend(Reign.Cmd.ID))ClientPlayNetworking.send(new Reign.Cmd(c));
  else if(get().player!=null)get().player.networkHandler.sendChatCommand("reign "+c);}
 @Override public void onInitializeClient(){
  KeyBinding r=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.reignfabric.setup",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_R,"key.categories.reignfabric"));
  KeyBinding b=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.reignfabric.build",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_B,"key.categories.reignfabric"));
  KeyBinding n=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.reignfabric.units",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_N,"key.categories.reignfabric"));
  ClientPlayNetworking.registerGlobalReceiver(Reign.Syn.ID,(p,c)->c.client().execute(()->S.load(p.j())));
  ClientTickEvents.END_CLIENT_TICK.register(cl->{
   while(r.wasPressed())cl.setScreen(new Menus.Setup(0,0));
   while(b.wasPressed())cl.setScreen(new Menus.BuildMenu());
   while(n.wasPressed())cl.setScreen(new Menus.UnitMenu());});
 }
 public static class TI{public int i,gold,wood,stone,food,su,sc,un,bd,q;public boolean alive;public String race="humans";}
 public static class PI{public String name="";public int asg=-2,team=-1;}
 public static final class S{
  public static String phase="LOBBY",map="basin",sel="house";
  public static boolean leader;public static int rev,size=128,teams=2,myTeam=-1,left;
  public static String[] race={"humans","nether","end","humans"};
  public static int startGold=300,startWood=200,startStone=150,startFood=20,supplyCap=60,unitCap=30,minutes=25,income=100,nodeResp=25,buildRad=28;
  public static boolean ff,pvp=true;
  public static final List<TI> ts=new ArrayList<>();public static final List<PI> players=new ArrayList<>();
  public static TI me(){for(TI t:ts)if(t.i==myTeam)return t;return null;}
  public static int i(String k){return switch(k){
   case "startGold"->startGold;case "startWood"->startWood;case "startStone"->startStone;case "startFood"->startFood;
   case "supplyCap"->supplyCap;case "unitCap"->unitCap;case "minutes"->minutes;case "income"->income;
   case "nodeResp"->nodeResp;case "buildRad"->buildRad;default->0;};}
  public static boolean bool(String k){return k.equals("ff")?ff:pvp;}
  public static void load(String j){try{
   JsonObject o=JsonParser.parseString(j).getAsJsonObject();
   phase=o.get("phase").getAsString();leader=o.get("leader").getAsBoolean();rev=o.get("rev").getAsInt();
   left=o.get("left").getAsInt();map=o.get("map").getAsString();size=o.get("size").getAsInt();teams=o.get("teams").getAsInt();
   JsonArray a=o.getAsJsonArray("race");for(int i=0;i<a.size()&&i<4;i++)race[i]=a.get(i).getAsString();
   JsonObject c=o.getAsJsonObject("cfg");
   startGold=c.get("startGold").getAsInt();startWood=c.get("startWood").getAsInt();startStone=c.get("startStone").getAsInt();startFood=c.get("startFood").getAsInt();
   supplyCap=c.get("supplyCap").getAsInt();unitCap=c.get("unitCap").getAsInt();minutes=c.get("minutes").getAsInt();
   income=c.get("income").getAsInt();nodeResp=c.get("nodeResp").getAsInt();buildRad=c.get("buildRad").getAsInt();
   ff=c.get("ff").getAsBoolean();pvp=c.get("pvp").getAsBoolean();
   JsonObject m=o.getAsJsonObject("me");myTeam=m.get("team").getAsInt();sel=m.get("sel").getAsString();
   ts.clear();for(JsonElement e:o.getAsJsonArray("ts")){JsonObject x=e.getAsJsonObject();TI t=new TI();
    t.i=x.get("i").getAsInt();t.race=x.get("race").getAsString();t.alive=x.get("alive").getAsBoolean();
    t.gold=x.get("gold").getAsInt();t.wood=x.get("wood").getAsInt();t.stone=x.get("stone").getAsInt();t.food=x.get("food").getAsInt();
    t.su=x.get("su").getAsInt();t.sc=x.get("sc").getAsInt();t.un=x.get("un").getAsInt();t.bd=x.get("bd").getAsInt();t.q=x.get("q").getAsInt();ts.add(t);}
   players.clear();for(JsonElement e:o.getAsJsonArray("pl")){JsonObject x=e.getAsJsonObject();PI p=new PI();
    p.name=x.get("name").getAsString();p.asg=x.get("asg").getAsInt();p.team=x.get("team").getAsInt();players.add(p);}
  }catch(Exception ignored){}}
 }
}