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
	private static List<PlayerMoveChannelEvent> moveChannelListeners = new ArrayList<>();
	
	private static DiscordBot dcbot = null;	
	private static VoiceChatSQL sql = null;
	
	public static StateFlag isOwnVoiceRegion;
	public static StateFlag isDisabledRegion;
	
	private static VoiceChatMain instance = null;
	
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
		this.getConfig().addDefault("DCbot.serverID", "server");
		this.getConfig().addDefault("DCbot.categoryID", "category");
		
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
		
		dcbot = new DiscordBot(this.getConfig().getString("DCbot.token"), this.getConfig().getString("DCbot.serverID"), this.getConfig().getString("DCbot.categoryID"), ActivityType.valueOf(this.getConfig().getString("DCbot.statusType")), this.getConfig().getString("DCbot.status"));
		
		instance = this;
		//INSTANCES END
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * rloadConfig() reloads the VoiceChat config
	 */
	public void rloadConfig() {
		this.reloadConfig();
		
		disabledWorlds = (ArrayList<String>) this.getConfig().getList("VoiceChat.disabledWorlds");
		
		//TODO sql reload
	}

	protected static void fireVoiceStateChange(VoicePlayer player, VoiceState oldVoiceState, VoiceState newVoiceState, boolean getsKicked) {
		for(PlayerVoiceStateChangeEvent listener : voiceStateChangeListeners) {
			listener.onPlayerVoiceStateChange(player, oldVoiceState, newVoiceState, getsKicked);
		}
	}
	
	protected static void firePlayerMoveChannel(VoicePlayer player, DCChannel oldChannel, DCChannel newChannel) {
		for(PlayerMoveChannelEvent listener : moveChannelListeners) {
			listener.onPlayerMoveChannel(player, oldChannel, newChannel);
		}
	}
	
	/**
	 * addPlayerVoieStateChangeListener(PlayerVoiceStateChangeEvent listener) registeres a new PlayerVoiceStateChangeEvent listener
	 */
	public static void addPlayerVoiceStateChangeListener(PlayerVoiceStateChangeEvent listener) {
		voiceStateChangeListeners.add(listener);
	}
	
	/**
	 * addPlayerMoveChannelListener(PlayerMoveChannelEvent listener) registeres a new PlayerMoveChannelEvent listener
	 */
	public static void addPlayerMoveChannelListener(PlayerMoveChannelEvent listener) {
		moveChannelListeners.add(listener);
	}

	protected static DiscordBot getDcbot() {
		return dcbot;
	}

	protected static VoiceChatSQL getSql() {
		return sql;
	}
	
	/**
	 * getPlayers() returns a HashMap<Player, VoicePlayer> with all the currently registered Players
	 */
	public static HashMap<Player, VoicePlayer> getPlayers() {
		return players;
	}
	
	/**
	 * getChannels() returns an ArrayList<DCChannel> with all the channels VoiceChat has currently registered
	 */
	public static ArrayList<DCChannel> getChannels() {
		return channels;
	}
	
	/**
	 * getDisabledWorlds returns an ArrayList<String> with the names of the worlds where VoiceChat is not going to operate
	 */
	public static ArrayList<String> getDisabledWorlds() {
		return disabledWorlds;
	}

	/**
	 * getInstance() returns the Minecraft plugin instance from the VoiceChat plugin. NOTE: Only use if you really have to. Nearly everything you need is static.
	 */
	public static VoiceChatMain getInstance() {
		return instance;
	}
}
