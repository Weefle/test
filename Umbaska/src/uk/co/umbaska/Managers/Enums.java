package uk.co.umbaska.Managers;

import ca.thederpygolems.armorequip.ArmourEquipEvent.EquipMethod;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.party.PartyManager;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Locale;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.PluginManager;
import uk.co.umbaska.Enums.BukkitEffectEnum;
import uk.co.umbaska.Enums.InventoryTypes;
import uk.co.umbaska.Enums.Operation;
import uk.co.umbaska.Enums.ParticleEnum;
import uk.co.umbaska.JSON.JSONMessage;
import uk.co.umbaska.JSON.JsonBuilder;
import uk.co.umbaska.Main;
import uk.co.umbaska.Misc.Date.DayOfWeek;
import uk.co.umbaska.Utils.Disguise.EntityDisguise;
import uk.co.umbaska.Utils.EnumClassInfo;

public class Enums
{
  public static Boolean debugInfo = Boolean.valueOf(Main.getInstance().getConfig().getBoolean("debug_info"));
  private static String version = Register.getVersion();
  
  private static void registerEnum(String cls, String name, Boolean multiversion)
  {
    if (Skript.isAcceptRegistrations())
    {
      if (multiversion.booleanValue())
      {
        Class newCls = Register.getClass(cls);
        if (newCls == null) {
          Bukkit.getLogger().info("Umbaska »»» Can't Register Enum for " + name + " due to \nWrong Spigot/Bukkit Version!");
        }
        if (debugInfo.booleanValue()) {
          Bukkit.getLogger().info("Umbaska »»» Registered Enum for " + name + " for Version " + version);
        }
        registerEnum(newCls, name);
      }
      else
      {
        try
        {
          registerEnum(Class.forName(cls), name);
        }
        catch (ClassNotFoundException e)
        {
          Bukkit.getLogger().info("Umbaska »»» Can't Register Enum for " + name + " due to \nWrong Spigot/Bukkit Version!");
        }
      }
    }
    else {
      Bukkit.getLogger().info("Umbaska »»» Can't Register Enum for " + name + " due to \nSkript Not Accepting Registrations");
    }
  }
  
  private static void registerEnum(Class cls, String name)
  {
    if (Skript.isAcceptRegistrations()) {
      EnumClassInfo.create(cls, name).register();
    } else {
      Bukkit.getLogger().info("Umbaska »»» Can't Register Enum for " + name + " due to \nSkript Not Accepting Registrations");
    }
  }
  
  private static void registerEnum(Class cls, String name, EventValueExpression defaultExpression)
  {
    if (Skript.isAcceptRegistrations()) {
      EnumClassInfo.create(cls, name, defaultExpression).register();
    } else {
      Bukkit.getLogger().info("Umbaska »»» Can't Register Enum for " + name + " due to \nSkript Not Accepting Registrations");
    }
  }
  
  public static void runRegister()
  {
    registerEnum(InventoryTypes.class, "umbaskainv");
    registerEnum(ParticleEnum.class, "particleenum");
    registerEnum(BukkitEffectEnum.class, "bukkiteffect");
    registerEnum("Enums.Attributes", "entityattribute", Boolean.valueOf(true));
    registerEnum(Locale.class, "locale");
    registerEnum(DayOfWeek.class, "dayofweek");
    registerEnum(PatternType.class, "bannerpattern");
    registerEnum(EntityDisguise.class, "entitydisguise");
    registerEnum(Operation.class, "nbtoperation");
    registerEnum(ArmourEquipEvent.EquipMethod.class, "equipmethod");
    registerEnum(ClickType.class, "clicktype");
    registerEnum(EntityEffect.class, "entityeffect");
    EnumClassInfo.create(Material.class, "material").after(new String[] { "block" }).register();
    
    Classes.registerClass(new ClassInfo(JSONMessage.class, "umbjsonmessage").parser(new Parser()
    {
      public String toString(Object o, int i)
      {
        return ((JSONMessage)o).toString();
      }
      
      public String toVariableNameString(Object o)
      {
        return ((JSONMessage)o).toString();
      }
      
      public JsonBuilder parse(String s, ParseContext parseContext)
      {
        return null;
      }
      
      public boolean canParse(ParseContext context)
      {
        return false;
      }
      
      public String toString(JSONMessage jsonMessage, int i)
      {
        return jsonMessage.toOldMessageFormat();
      }
      
      public String toVariableNameString(JSONMessage jsonMessage)
      {
        return jsonMessage.toString();
      }
      
      public String getVariableNamePattern()
      {
        return ".+";
      }
    }));
    if (!Main.disableSkRambled.booleanValue())
    {
      if ((Bukkit.getPluginManager().getPlugin("Factions") != null) && (Bukkit.getPluginManager().getPlugin("MassiveCore") != null))
      {
        Classes.registerClass(new ClassInfo(Faction.class, "faction").user(new String[] { "faction" }).name("Faction").defaultExpression(new EventValueExpression(Faction.class)).parser(new Parser()
        {
          @Nullable
          public Faction parse(String s, ParseContext context)
          {
            return FactionColl.get().getByName(s);
          }
          
          public String toString(Faction faction, int flags)
          {
            return faction.getName().toLowerCase();
          }
          
          public String toVariableNameString(Faction faction)
          {
            return faction.getName().toLowerCase();
          }
          
          public String getVariableNamePattern()
          {
            return ".+";
          }
        }));
        Classes.registerClass(new ClassInfo(Rel.class, "rel").name("Rel").parser(new Parser()
        {
          @Nullable
          public Rel parse(String s, ParseContext context)
          {
            return Rel.parse(s);
          }
          
          public String toString(Rel rel, int flags)
          {
            return rel.toString().toLowerCase();
          }
          
          public String toVariableNameString(Rel rel)
          {
            return rel.toString().toLowerCase();
          }
          
          public String getVariableNamePattern()
          {
            return ".+";
          }
        }));
      }
      if (Bukkit.getPluginManager().getPlugin("mcMMO") != null)
      {
        Classes.registerClass(new ClassInfo(Party.class, "party").name("Party").parser(new Parser()
        {
          @Nullable
          public Party parse(String s, ParseContext context)
          {
            return PartyManager.getParty(s);
          }
          
          public String toString(Party party, int flags)
          {
            return party.getName().toLowerCase();
          }
          
          public String toVariableNameString(Party party)
          {
            return party.getName().toLowerCase();
          }
          
          public String getVariableNamePattern()
          {
            return ".+";
          }
        }));
        Classes.registerClass(new ClassInfo(SkillType.class, "skill").name("Skill").parser(new Parser()
        {
          @Nullable
          public SkillType parse(String s, ParseContext context)
          {
            try
            {
              return SkillType.valueOf(s.toUpperCase());
            }
            catch (Exception e) {}
            return null;
          }
          
          public String toString(SkillType skill, int flags)
          {
            return skill.getName().toLowerCase();
          }
          
          public String toVariableNameString(SkillType skill)
          {
            return skill.getName().toLowerCase();
          }
          
          public String getVariableNamePattern()
          {
            return ".+";
          }
        }));
      }
      if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
      {
        Classes.registerClass(new ClassInfo(ProtectedRegion.class, "protectedregion").name("Protected Region").user(new String[] { "protectedregions?" }).defaultExpression(new EventValueExpression(ProtectedRegion.class)).parser(new Parser()
        {
          @Nullable
          public ProtectedRegion parse(String s, ParseContext context)
          {
            for (World w : Bukkit.getServer().getWorlds()) {
              if (WGBukkit.getRegionManager(w).hasRegion(s)) {
                return WGBukkit.getRegionManager(w).getRegion(s);
              }
            }
            return null;
          }
          
          public String toString(ProtectedRegion region, int flags)
          {
            return region.getId().toLowerCase();
          }
          
          public String toVariableNameString(ProtectedRegion region)
          {
            return region.getId().toLowerCase();
          }
          
          public String getVariableNamePattern()
          {
            return ".+";
          }
        }));
        Classes.registerClass(new ClassInfo(Flag.class, "flag").name("Flag").user(new String[] { "flags?" }).defaultExpression(new EventValueExpression(Flag.class)).parser(new Parser()
        {
          public Flag<?> parse(String s, ParseContext context)
          {
            return DefaultFlag.fuzzyMatchFlag(s);
          }
          
          public String toString(Flag<?> flag, int flags)
          {
            return flag.getName().toLowerCase();
          }
          
          public String toVariableNameString(Flag<?> flag)
          {
            return flag.getName().toLowerCase();
          }
          
          public String getVariableNamePattern()
          {
            return ".+";
          }
        }));
      }
    }
    Main.getInstance().getLogger().info("[Umbaska > SkQuery] Registered Custom Particle Enum. Have some BACON!!!!");
  }
}
