package me.emnichtdayt.voicechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.javacord.api.entity.activity.ActivityType;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import net.md_5.bungee.api.ChatColor;

public class VoiceChatMain extends JavaPlugin {
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
	private static ArrayList<DCChannel> channels = new ArrayList<DCChannel>();

	private static ArrayList<String> disabledWorlds = new ArrayList<String>();

	private static int rangeX = 4;
	private static int rangeY = 4;
	private static int rangeZ = 4;
	
	protected static HashMap<Integer, Player> registerKeys = new HashMap<Integer, Player>();

	private static boolean voiceChatRequired = true;
	private static boolean registerInternalMode = true;

	public void onLoad() {
		// WORLDGUARD
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
		// WORLDGUARD END
	}

	public void onEnable() {
		// CONFIG
		this.reloadConfig();

		this.getConfig().options().header("Willkommen in der Config");
		this.getConfig().options().copyHeader(true);

		this.getConfig().addDefault("MySQL.ip", "ip");
		this.getConfig().addDefault("MySQL.database", "data");
		this.getConfig().addDefault("MySQL.table", "table");
		this.getConfig().addDefault("MySQL.idColumn", "discordID");
		this.getConfig().addDefault("MySQL.uuidColumn", "uuid");
		this.getConfig().addDefault("MySQL.user", "user");
		this.getConfig().addDefault("MySQL.password", "pass");

		this.getConfig().addDefault("DCbot.token", "token");
		this.getConfig().addDefault("DCbot.status", "roleplay.emnichtda.de");
		this.getConfig().addDefault("DCbot.statusType", "PLAYING");
		this.getConfig().addDefault("DCbot.serverID", "server");
		this.getConfig().addDefault("DCbot.categoryID", "category");
		this.getConfig().addDefault("DCbot.waitinChannelID", "1234567890");

		this.getConfig().addDefault("VoiceChat.disabledWorlds", new ArrayList<String>());
		this.getConfig().addDefault("VoiceChat.disabledRegions", new ArrayList<String>());

		this.getConfig().addDefault("VoiceChat.range.x", 4);
		this.getConfig().addDefault("VoiceChat.range.y", 4);
		this.getConfig().addDefault("VoiceChat.range.z", 4);

		this.getConfig().addDefault("VoiceChat.isRequired", true);
		
		this.getConfig().addDefault("VoiceChat.register.internalMode", true);
		this.getConfig().addDefault("VoiceChat.message.register.internalMode", ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY + "Please register in oder to use VoiceChat. Send the following code per direct message to the VoiceChat bot: ");
		this.getConfig().addDefault("VoiceChat.message.register.externalMode", ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY + "Please register! I dont have your Discord ID in my database.");
		this.getConfig().addDefault("VoiceChat.message.notInWaitingChannel", ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY + "Please join the VoiceChat waiting channel!");
		this.getConfig().addDefault("VoiceChat.message.leftDCChannel", ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY + "You left the waiting channel!");
		
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		this.saveDefaultConfig();
		// CONFIG END

		this.reloadConfig();
		rloadConfig();

		// INSTANCES
		this.timer = new VoiceChatTimer(this).runTaskTimer(this, 0, 10);

		mcEvents = new VoiceChatMCEvents();
		this.getServer().getPluginManager().registerEvents(mcEvents, this);

		sql = new VoiceChatSQL(this.getConfig().getString("MySQL.ip"), this.getConfig().getString("MySQL.database"),
				this.getConfig().getString("MySQL.table"), this.getConfig().getString("MySQL.idColumn"),
				this.getConfig().getString("MySQL.uuidColumn"), this.getConfig().getString("MySQL.user"),
				this.getConfig().getString("MySQL.password"));

		dcbot = new DiscordBot(this.getConfig().getString("DCbot.token"), this.getConfig().getString("DCbot.serverID"),
				this.getConfig().getString("DCbot.categoryID"), this.getConfig().getString("DCbot.waitinChannelID"),
				ActivityType.valueOf(this.getConfig().getString("DCbot.statusType")),
				this.getConfig().getString("DCbot.status"), this.getConfig().getString("VoiceChat.message.leftDCChannel"));

		instance = this;
		// INSTANCES END
	}

	@SuppressWarnings("unchecked")
	/**
	 * rloadConfig() reloads the VoiceChat config
	 */
	public void rloadConfig() {
		this.reloadConfig();

		disabledWorlds = (ArrayList<String>) this.getConfig().getList("VoiceChat.disabledWorlds");

		setVoiceRangeX(this.getConfig().getInt("VoiceChat.range.x"));
		setVoiceRangeY(this.getConfig().getInt("VoiceChat.range.y"));
		setVoiceRangeZ(this.getConfig().getInt("VoiceChat.range.z"));

		setVoiceChatRequired(this.getConfig().getBoolean("VoiceChat.isRequired"));
		
		setRegisterInternalMode(this.getConfig().getBoolean("VoiceChat.register.internalMode"));
		
		VoiceChatMCEvents.rloadConfig(this.getConfig().getString("VoiceChat.message.register.internalMode"), this.getConfig().getString("VoiceChat.message.register.externalMode"), this.getConfig().getString("VoiceChat.message.notInWaitingChannel"));
		
		if(dcbot!=null) {
			dcbot.rloadVoiceDisconnectMessafe(this.getConfig().getString("VoiceChat.message.leftDCChannel"));
		}

		// TODO sql reload
	}

	protected static void fireVoiceStateChange(VoicePlayer player, VoiceState oldVoiceState, VoiceState newVoiceState,
			boolean getsKicked) {
		for (PlayerVoiceStateChangeEvent listener : voiceStateChangeListeners) {
			listener.onPlayerVoiceStateChange(player, oldVoiceState, newVoiceState, getsKicked);
		}
	}

	protected static void firePlayerMoveChannel(VoicePlayer player, DCChannel oldChannel, DCChannel newChannel) {
		for (PlayerMoveChannelEvent listener : moveChannelListeners) {
			listener.onPlayerMoveChannel(player, oldChannel, newChannel);
		}
	}

	/**
	 * addPlayerVoieStateChangeListener(PlayerVoiceStateChangeEvent listener)
	 * registeres a new PlayerVoiceStateChangeEvent listener
	 */
	public static void addPlayerVoiceStateChangeListener(PlayerVoiceStateChangeEvent listener) {
		voiceStateChangeListeners.add(listener);
	}

	/**
	 * addPlayerMoveChannelListener(PlayerMoveChannelEvent listener) registeres a
	 * new PlayerMoveChannelEvent listener
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
	 * getPlayers() returns a HashMap<Player, VoicePlayer> with all the currently
	 * registered Players
	 */
	public static HashMap<Player, VoicePlayer> getPlayers() {
		return players;
	}

	/**
	 * getChannels() returns an ArrayList<DCChannel> with all the channels VoiceChat
	 * has currently registered
	 */
	public static ArrayList<DCChannel> getChannels() {
		return channels;
	}

	/**
	 * getDisabledWorlds returns an ArrayList<String> with the names of the worlds
	 * where VoiceChat is not going to operate
	 */
	public static ArrayList<String> getDisabledWorlds() {
		return disabledWorlds;
	}

	/**
	 * getInstance() returns the Minecraft plugin instance from the VoiceChat
	 * plugin. NOTE: Only use if you really have to. Nearly everything you need is
	 * static.
	 */
	public static VoiceChatMain getInstance() {
		return instance;
	}

	/**
	 * getVoiceRangeX() returns the x distance arround the host within the players
	 * can hear each other. (Doesnt affect non automatic controlled players)
	 */
	public static int getVoiceRangeX() {
		return rangeX;
	}

	private static void setVoiceRangeX(int rangeX) {
		VoiceChatMain.rangeX = rangeX;
	}

	/**
	 * getVoiceRangeY() returns the y distance arround the host within the players
	 * can hear each other. (Doesnt affect non automatic controlled players)
	 */
	public static int getVoiceRangeY() {
		return rangeY;
	}

	private static void setVoiceRangeY(int rangeY) {
		VoiceChatMain.rangeY = rangeY;
	}

	/**
	 * getVoiceRangeZ() returns the z distance arround the host within the players
	 * can hear each other. (Doesnt affect non automatic controlled players)
	 */
	public static int getVoiceRangeZ() {
		return rangeZ;
	}

	private static void setVoiceRangeZ(int rangeZ) {
		VoiceChatMain.rangeZ = rangeZ;
	}

	/**
	 * getVoiceChatRequired() returns if VoiceChat is required to play
	 */
	public static boolean getVoiceChatRequired() {
		return voiceChatRequired;
	}

	private void setVoiceChatRequired(boolean voiceChatRequired) {
		VoiceChatMain.voiceChatRequired = voiceChatRequired;
	}

	public boolean onCommand(org.bukkit.command.CommandSender sender, Command cmd, String cmdlabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("voicechatinfo")) {

			for (DCChannel channel : channels) {
				sender.sendMessage(channel.toString());
			}

		}
		return true;
	}

	public static boolean isRegisterInternalMode() {
		return registerInternalMode;
	}

	private static void setRegisterInternalMode(boolean registerInternalMode) {
		VoiceChatMain.registerInternalMode = registerInternalMode;
	}

	public static int getNewRegisterCodeFor(Player player) {
		Random random = new Random();
		int code = random.nextInt(10000);
		while(String.valueOf(code).length()!=4) {
			code = random.nextInt(10000);
		}
		registerKeys.put(code, player);
		return code;
	}
}