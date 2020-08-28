package me.emnichtdayt.voicechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.javacord.api.entity.activity.ActivityType;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class VoiceChatMain extends JavaPlugin{
	private static List<PlayerVoiceStateChangeEvent> voiceStateChangeListeners = new ArrayList<>();
	
	private static DiscordBot dcbot = null;	
	private static VoiceChatSQL sql = null;
	
	public static StateFlag isOwnVoiceRegion;
	public static StateFlag isDisabledRegion;
	
	@SuppressWarnings("unused")
	private BukkitTask timer = null;
	
	private static VoiceChatMCEvents mcEvents = null;
	
	private static HashMap<Player, VoicePlayer> players = new HashMap<Player, VoicePlayer>();
	private static ArrayList<DCChannel> channels =new ArrayList<DCChannel>();
	
	private static ArrayList<String> disabledWorlds = new ArrayList<String>();
	
	public void onLoad() {
		//WORLDGUARD
	    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
	    try {
	        StateFlag flag = new StateFlag("isOwnVoiceChatRegion", false);
	        registry.register(flag);
	        VoiceChatMain.isOwnVoiceRegion = flag;
	    } catch (FlagConflictException e) {
	    	
	    }
	    try {
	        StateFlag flag = new StateFlag("isDisabledVoiceChatRegion", false);
	        registry.register(flag);
	        VoiceChatMain.isDisabledRegion = flag;
	    } catch (FlagConflictException e) {
	    	
	    }
	    //WORLDGUARD END
	}
	
	public void onEnable() {	    
		//CONFIG
		this.reloadConfig();
		
		this.getConfig().options().header("Willkommen in der Config");
		this.getConfig().options().copyHeader(true);
		
		this.getConfig().addDefault("MySQL.ip", "ip");
		this.getConfig().addDefault("MySQL.database", "data");
		this.getConfig().addDefault("MySQL.user", "user");
		this.getConfig().addDefault("MySQL.password", "pass");
		
		this.getConfig().addDefault("DCbot.token", "token");
		this.getConfig().addDefault("DCbot.status", "roleplay.emnichtda.de");
		this.getConfig().addDefault("DCbot.statusType", "PLAYING");
		
		this.getConfig().addDefault("VoiceChat.disabledWorlds", new ArrayList<String>());
		this.getConfig().addDefault("VoiceChat.disabledRegions", new ArrayList<String>());
		
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		this.saveDefaultConfig();
		//CONFIG END
		
		this.reloadConfig();
		rloadConfig();
		
		//INSTANCES
		this.timer = new VoiceChatTimer(this).runTaskTimer(this, 0, 10);
		
		mcEvents = new VoiceChatMCEvents();
		this.getServer().getPluginManager().registerEvents(mcEvents, this);
		
		dcbot = new DiscordBot(this.getConfig().getString("DCbot.token"), ActivityType.valueOf(this.getConfig().getString("DCbot.statusType")), this.getConfig().getString("DCbot.status"));		
		//INSTANCES END
		
	}
	
	@SuppressWarnings("unchecked")
	public void rloadConfig() {
		this.reloadConfig();
		
		disabledWorlds = (ArrayList<String>) this.getConfig().getList("VoiceChat.disabledWorlds");
		
		//TODO sql reload
	}

	protected void fireVoiceStateChange(VoicePlayer player, VoiceState newVoiceState, boolean getsKicked) {
		for(PlayerVoiceStateChangeEvent listener : voiceStateChangeListeners) {
			listener.onPlayerVoiceStateChange(player, newVoiceState, getsKicked);
		}
	}
	
	public static void addVoiceChatListener(PlayerVoiceStateChangeEvent listener) {
		voiceStateChangeListeners.add(listener);
	}

	protected static DiscordBot getDcbot() {
		return dcbot;
	}

	protected static VoiceChatSQL getSql() {
		return sql;
	}

	public static HashMap<Player, VoicePlayer> getPlayers() {
		return players;
	}

	public static ArrayList<DCChannel> getChannels() {
		return channels;
	}

	public static ArrayList<String> getDisabledWorlds() {
		return disabledWorlds;
	}
}