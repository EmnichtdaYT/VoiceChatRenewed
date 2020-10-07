package me.emnichtdayt.voicechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.javacord.api.entity.activity.ActivityType;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import github.scarsz.discordsrv.DiscordSRV;
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
	protected static ArrayList<Player> kickList = new ArrayList<Player>();

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

		this.getConfig().options().header(
				"Welcome to the VoiceChat config. If you want to change anything besides the messages please restart the server, /voicechat reload won't do anything. If you need help configurating the plugin take a look at https://roleplay.emnichtda.de/plugins/VoiceChat-Renewed/ and if you still have issues ask me on Discord! https://discord.gg/9vK65nD");
		this.getConfig().options().copyHeader(true);

		this.getConfig().addDefault("MySQL.ip", "ip");
		this.getConfig().addDefault("MySQL.port", "3306");
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

		this.getConfig().addDefault("VoiceChat.range.x", 4);
		this.getConfig().addDefault("VoiceChat.range.y", 4);
		this.getConfig().addDefault("VoiceChat.range.z", 4);

		this.getConfig().addDefault("VoiceChat.isRequired", true);
		this.getConfig().addDefault("VoiceChat.register.internalMode", true);

		this.getConfig().addDefault("VoiceChat.register.useDiscordSRVregister", true);

		this.getConfig().addDefault("VoiceChat.message.register.internalMode", ChatColor.GREEN + "[VoiceChat] "
				+ ChatColor.GRAY
				+ "Please register in oder to use VoiceChat. Send the following code per direct message to the VoiceChat bot: ");
		this.getConfig().addDefault("VoiceChat.message.register.externalMode", ChatColor.GREEN + "[VoiceChat] "
				+ ChatColor.GRAY
				+ "Please register! I dont have your Discord ID in my database. (Ask an administrator how to do so, I dunno, sorry!");
		this.getConfig().addDefault("VoiceChat.message.notInWaitingChannel",
				ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY + "Please join the VoiceChat waiting channel!");
		this.getConfig().addDefault("VoiceChat.message.leftDCChannel",
				ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY + "You left the waiting channel!");
		this.getConfig().addDefault("VoiceChat.message.info", ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY
				+ "VoiceChat by EmnichtdaYT. Use /voicechat help for help");
		this.getConfig().addDefault("VoiceChat.message.voice.enabled",
				ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY + "Enabled the VoiceChat for: ");
		this.getConfig().addDefault("VoiceChat.message.voice.disabled",
				ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY + "Disabled the VoiceChat for: ");
		this.getConfig().addDefault("VoiceChat.message.playerNotFound", ChatColor.GREEN + "[VoiceChat] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "Player not found.");
		this.getConfig().addDefault("VoiceChat.message.toggle.usage", ChatColor.GREEN + "[VoiceChat] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "use /voicechat toggle [Name] (on/off)");
		this.getConfig().addDefault("VoiceChat.message.noPermission", ChatColor.GREEN + "[VoiceChat] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "Server says nope! You don't have permission!");
		this.getConfig().addDefault("VoiceChat.message.help", ChatColor.GREEN + "[VoiceChat] HELP:\n" + ChatColor.GREEN
				+ "/voicechat toggle [Player] (on/off) " + ChatColor.WHITE + "-" + ChatColor.GRAY
				+ " activate/deactivate a player's VoiceChat. Please note, this does only disable the VoiceChat logic not any extentions for VoiceChat like a Phone plugin.\n"
				+ ChatColor.GREEN + "/voicechat unlink [Player] " + ChatColor.WHITE + "-" + ChatColor.GRAY
				+ " Unlinks a player's VoiceChat, comes in handy when they forgot their Discord Login and VoiceChat required is on.\n"
				+ ChatColor.GREEN + "/voicechat register " + ChatColor.WHITE + "-" + ChatColor.GRAY
				+ " Generates a new code in order to register/re-register your discord account\n" + ChatColor.GRAY
				+ ChatColor.GREEN + "/voicechat reload " + ChatColor.WHITE + "-" + ChatColor.GRAY
				+ " Reloads parts of the config. If you want to change anything related to the discord bot or mysql or the plugin core please restart the server.\n"
				+ ChatColor.GREEN + "/voicechat DiscordSRV loadLinkedPlayers " + ChatColor.WHITE + "-" + ChatColor.GRAY
				+ " Loads EVERY player registered via DiscordSRV! This might take a long time.\n" + ChatColor.GRAY
				+ "\n------- " + "[] = Required, () = Optional");
		this.getConfig().addDefault("VoiceChat.message.senderNoPlayer", ChatColor.GREEN + "[VoiceChat] "
				+ ChatColor.GRAY + "Im sorry, but look at you! You are no player! You can only do this as a player!");
		this.getConfig().addDefault("VoiceChat.message.register.externalMode.command", ChatColor.GREEN + "[VoiceChat] "
				+ ChatColor.GRAY + "Ask an administrators how to register, I dunno, sorry!");
		this.getConfig().addDefault("VoiceChat.message.unlink.usage", ChatColor.GREEN + "[VoiceChat] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "use /voicechat unlink [Name]");
		this.getConfig().addDefault("VoiceChat.message.unlink.sucsess",
				ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY + "Successfully unlinked: ");
		this.getConfig().addDefault("VoiceChat.message.reload", ChatColor.GREEN + "[VoiceChat] " + ChatColor.GRAY
				+ "Successfully reloaded a part of the config. If something didn't reload please stop the server, then edit the config, save the config and start the server again.");
		this.getConfig().addDefault("VoiceChat.message.cmdNotFound", ChatColor.GREEN + "[VoiceChat] " + ChatColor.WHITE
				+ "Command not found! Type /voicechat help for help.");

		this.getConfig().addDefault("VoiceChat.message.embed.title", "VoiceChat");
		this.getConfig().addDefault("VoiceChat.message.embed.connectedMessage",
				"Got ya up and ready! Join the waiting channel and have fun playing.");
		this.getConfig().addDefault("VoiceChat.message.embed.codeInvalid", "That code is invalid.");
		this.getConfig().addDefault("VoiceChat.message.embed.noCode", "I only accept a 4 digit code for registration.");
		this.getConfig().addDefault("VoiceChat.message.embed.color", "#077d1f");

		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		this.saveDefaultConfig();
		// CONFIG END

		this.reloadConfig();

		if (this.getConfig().getString("MySQL.ip").equalsIgnoreCase("ip")
				|| this.getConfig().getString("DCbot.token").equalsIgnoreCase("token")) {
			System.out.println(ChatColor.GREEN + "[VoiceChat] First time Startup detected!");
			System.out.println("-----------------------------------------");
			System.out.println(ChatColor.GREEN + "[VoiceChat] " + ChatColor.WHITE + "Welcome to VoiceChat.");
			System.out.println(ChatColor.GREEN + "[VoiceChat] " + ChatColor.WHITE
					+ "Please take a look at how to configure the plugin: https://roleplay.emnichtda.de/plugins/VoiceChat-Renewed/");
			System.out.println(ChatColor.GREEN + "[VoiceChat] " + ChatColor.WHITE
					+ "No idea how to configure the plugin? Found a bug? Need a new feature? Ask me on Discord! https://discord.gg/9vK65nD");
			System.out.println(ChatColor.GREEN + "[VoiceChat] " + ChatColor.WHITE
					+ "Thanks for buying my plugin, please note that you are not permitted to redistribute or decompile my plugin.");
			System.out.println(ChatColor.GREEN + "[VoiceChat] " + ChatColor.WHITE
					+ "If you want to know how I programmed VoiceChat just ask me on Discord! If you need a new feature use the api or ask on Discord!");
			System.out.println("-----------------------------------------");
			System.out.println("VoiceChat will now disable...");
			this.getPluginLoader().disablePlugin(this);
			return;
		}

		rloadConfig();

		// INSTANCES
		this.timer = new VoiceChatTimer(this).runTaskTimer(this, 0, 10);

		mcEvents = new VoiceChatMCEvents();
		this.getServer().getPluginManager().registerEvents(mcEvents, this);

		sql = new VoiceChatSQL(this.getConfig().getString("MySQL.ip"), this.getConfig().getString("MySQL.port"),
				this.getConfig().getString("MySQL.database"), this.getConfig().getString("MySQL.table"),
				this.getConfig().getString("MySQL.idColumn"), this.getConfig().getString("MySQL.uuidColumn"),
				this.getConfig().getString("MySQL.user"), this.getConfig().getString("MySQL.password"));

		dcbot = new DiscordBot(this.getConfig().getString("DCbot.token"), this.getConfig().getString("DCbot.serverID"),
				this.getConfig().getString("DCbot.categoryID"), this.getConfig().getString("DCbot.waitinChannelID"),
				ActivityType.valueOf(this.getConfig().getString("DCbot.statusType")),
				this.getConfig().getString("DCbot.status"),
				this.getConfig().getString("VoiceChat.message.leftDCChannel"),
				this.getConfig().getString("VoiceChat.message.embed.title"),
				this.getConfig().getString("VoiceChat.message.embed.connectedMessage"),
				this.getConfig().getString("VoiceChat.message.embed.codeInvalid"),
				this.getConfig().getString("VoiceChat.message.embed.noCode"),
				this.getConfig().getString("VoiceChat.message.embed.color"));

		instance = this;

		if (this.getServer().getPluginManager().getPlugin("DiscordSRV") != null
				&& this.getConfig().getBoolean("VoiceChat.register.useDiscordSRVregister")) {
			DiscordSRV.api.subscribe(new DiscordSRVListener());
			System.out.println("DiscordSRV found. Listening for new Player links.");
		}

		// INSTANCES END
	}

	public void onDisable() {
		while (!getChannels().isEmpty()) {
			DCChannel targetChannel = getChannels().get(0);
			for (VoicePlayer targetVoice : targetChannel.getUsers()) {
				getDcbot().movePlayer(targetVoice, null);
			}
			getDcbot().instantDeleteChannelFromDC(targetChannel);
		}
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

		VoiceChatMCEvents.rloadConfig(this.getConfig().getString("VoiceChat.message.register.internalMode"),
				this.getConfig().getString("VoiceChat.message.register.externalMode"),
				this.getConfig().getString("VoiceChat.message.notInWaitingChannel"));

		if (dcbot != null) {
			dcbot.rloadVoiceDisconnectMessafe(this.getConfig().getString("VoiceChat.message.leftDCChannel"),
					this.getConfig().getString("VoiceChat.message.embed.title"),
					this.getConfig().getString("VoiceChat.message.embed.connectedMessage"),
					this.getConfig().getString("VoiceChat.message.embed.codeInvalid"),
					this.getConfig().getString("VoiceChat.message.embed.noCode"),
					this.getConfig().getString("VoiceChat.message.embed.color"));
		}
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
	 * 
	 * @param listener
	 */
	public static void addPlayerVoiceStateChangeListener(PlayerVoiceStateChangeEvent listener) {
		voiceStateChangeListeners.add(listener);
	}

	/**
	 * addPlayerMoveChannelListener(PlayerMoveChannelEvent listener) registeres a
	 * new PlayerMoveChannelEvent listener
	 * 
	 * @param listener
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
	 * getPlayers() returns a HashMap (Player, VoicePlayer) with all the currently
	 * registered Players
	 * 
	 * @return players
	 */
	public static HashMap<Player, VoicePlayer> getPlayers() {
		return players;
	}

	/**
	 * getChannels() returns an ArrayList(DCChannel) with all the channels VoiceChat
	 * has currently registered
	 * 
	 * @return channels
	 */
	public static ArrayList<DCChannel> getChannels() {
		return channels;
	}

	/**
	 * getDisabledWorlds returns an ArrayList(String) with the names of the worlds
	 * where VoiceChat is not going to operate
	 * 
	 * @return disabledWorlds
	 */
	public static ArrayList<String> getDisabledWorlds() {
		return disabledWorlds;
	}

	/**
	 * getInstance() returns the Minecraft plugin instance from the VoiceChat
	 * plugin. NOTE: Only use if you really have to. Nearly everything you need is
	 * static.
	 * 
	 * @return instance
	 */
	public static VoiceChatMain getInstance() {
		return instance;
	}

	/**
	 * getVoiceRangeX() returns the x distance within the players can hear each
	 * other. (Doesnt affect non automatic controlled players)
	 * 
	 * @return rangeX
	 */
	public static int getVoiceRangeX() {
		return rangeX;
	}

	private static void setVoiceRangeX(int rangeX) {
		VoiceChatMain.rangeX = rangeX;
	}

	/**
	 * getVoiceRangeY() returns the y distance within the players can hear each
	 * other. (Doesnt affect non automatic controlled players)
	 * 
	 * @return rangeY
	 */
	public static int getVoiceRangeY() {
		return rangeY;
	}

	private static void setVoiceRangeY(int rangeY) {
		VoiceChatMain.rangeY = rangeY;
	}

	/**
	 * getVoiceRangeZ() returns the z distance within the players can hear each
	 * other. (Doesnt affect non automatic controlled players)
	 * 
	 * @return rangeZ
	 */
	public static int getVoiceRangeZ() {
		return rangeZ;
	}

	private static void setVoiceRangeZ(int rangeZ) {
		VoiceChatMain.rangeZ = rangeZ;
	}

	/**
	 * getVoiceChatRequired() returns if VoiceChat is required to play
	 * 
	 * @return voiceChatRequired
	 */
	public static boolean getVoiceChatRequired() {
		return voiceChatRequired;
	}

	private void setVoiceChatRequired(boolean voiceChatRequired) {
		VoiceChatMain.voiceChatRequired = voiceChatRequired;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(org.bukkit.command.CommandSender sender, Command cmd, String cmdlabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("voicechatinfo")) {
			if (args.length == 1) {
				Player target = this.getServer().getPlayer(args[0]);
				if (target != null && target.isOnline()) {
					if (getPlayers().containsKey(target)) {
						sender.sendMessage(getPlayers().get(target).toString());
					} else {
						sender.sendMessage("[VoiceChat] That player is not in the system.");
					}
				} else {
					sender.sendMessage("[VoiceChat] That player is not online!");
				}
			} else {
				for (DCChannel channel : channels) {
					sender.sendMessage(channel.toString());
				}
			}

		} else if (cmd.getName().equalsIgnoreCase("VoiceChat")) {
			this.reloadConfig();
			if (args.length == 0) {
				sender.sendMessage(this.getConfig().getString("VoiceChat.message.info"));
			} else if (args[0].equalsIgnoreCase("toggle")) {
				if (sender.hasPermission("voicechat.toggle")) {
					if (args.length == 2) {
						Player target = this.getServer().getPlayer(args[1]);
						if (target != null) {
							VoicePlayer targetVoice = getPlayers().get(target);
							if (targetVoice != null) {
								targetVoice.setAutomaticControlled(!targetVoice.isAutomaticControlled());
								if (targetVoice.isAutomaticControlled()) {
									sender.sendMessage(this.getConfig().getString("VoiceChat.message.voice.enabled")
											+ target.getName());
								} else {
									if (targetVoice.getCurrentChannel() != null
											&& targetVoice.getCurrentChannel().getUsers().size() <= 2) {
										targetVoice.getCurrentChannel().remove();
										targetVoice.moveTo(null);
									} else {
										targetVoice.moveTo(null);
									}
									sender.sendMessage(this.getConfig().getString("VoiceChat.message.voice.disabled")
											+ target.getName());
								}
							} else {
								sender.sendMessage(this.getConfig().getString("VoiceChat.message.playerNotFound"));
							}
						} else {
							sender.sendMessage(this.getConfig().getString("VoiceChat.message.playerNotFound"));
						}
					} else if (args.length == 3) {
						Player target = this.getServer().getPlayer(args[1]);
						if (target != null) {
							VoicePlayer targetVoice = getPlayers().get(target);
							if (targetVoice != null) {
								if (args[2].equalsIgnoreCase("on")) {
									targetVoice.setAutomaticControlled(true);
									sender.sendMessage(this.getConfig().getString("VoiceChat.message.voice.enabled")
											+ target.getName());
								} else if (args[2].equalsIgnoreCase("off")) {
									targetVoice.setAutomaticControlled(false);
									if (targetVoice.getCurrentChannel() != null
											&& targetVoice.getCurrentChannel().getUsers().size() <= 2) {
										targetVoice.moveTo(null);
										targetVoice.getCurrentChannel().remove();
									} else {
										targetVoice.moveTo(null);
									}
									sender.sendMessage(this.getConfig().getString("VoiceChat.message.voice.disabled")
											+ target.getName());
								} else {
									sender.sendMessage(this.getConfig().getString("VoiceChat.message.toggle.usage"));
								}
							} else {
								sender.sendMessage(this.getConfig().getString("VoiceChat.message.playerNotFound"));
							}
						} else {
							sender.sendMessage(this.getConfig().getString("VoiceChat.message.playerNotFound"));
						}
					} else {
						sender.sendMessage(this.getConfig().getString("VoiceChat.message.toggle.usage"));
					}
				} else {
					sender.sendMessage(this.getConfig().getString("VoiceChat.message.noPermission"));
				}
			} else if (args[0].equalsIgnoreCase("help")) {
				sender.sendMessage(this.getConfig().getString("VoiceChat.message.help"));
			} else if (args[0].equalsIgnoreCase("register")) {
				if (registerInternalMode) {
					if (sender instanceof Player) {
						sender.sendMessage(this.getConfig().getString("VoiceChat.message.register.internalMode")
								+ VoiceChatMain.getNewRegisterCodeFor((Player) sender));
					} else {
						sender.sendMessage(this.getConfig().getString("VoiceChat.message.senderNoPlayer"));
					}
				} else {
					sender.sendMessage(this.getConfig().getString("VoiceChat.message.register.externalMode.command"));
				}
			} else if (args[0].equalsIgnoreCase("unlink")) {
				if (sender.hasPermission("voicechat.unlink")) {
					if (args.length == 2) {
						OfflinePlayer target = this.getServer().getOfflinePlayer(args[1]);
						if (target != null) {
							if (getSql().isSet(target)) {
								getSql().setID(target, 0);
								sender.sendMessage(this.getConfig().getString("VoiceChat.message.unlink.sucsess")
										+ target.getName());
							} else {
								sender.sendMessage(this.getConfig().getString("VoiceChat.message.playerNotFound"));
							}
						} else {
							sender.sendMessage(this.getConfig().getString("VoiceChat.message.playerNotFound"));
						}
					} else {
						sender.sendMessage(this.getConfig().getString("VoiceChat.message.unlink.usage"));
					}
				} else {
					sender.sendMessage(this.getConfig().getString("VoiceChat.message.noPermission"));
				}
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("voicechat.reload")) {
					rloadConfig();
					sender.sendMessage(this.getConfig().getString("VoiceChat.message.reload"));
				} else {
					sender.sendMessage(this.getConfig().getString("VoiceChat.message.noPermission"));
				}
			} else if (args[0].equalsIgnoreCase("discordSRV")) {
				if (args.length == 2) {
					if (args[1].equalsIgnoreCase("loadLinkedPlayers")) {
						if (sender.hasPermission("VoiceChat.discordSRV.loadLinkedPlayers")) {
							sender.sendMessage("If you have a lot of linked players this can take a while.");
							if (this.getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
								Map<String, UUID> linkedPlayers = DiscordSRV.getPlugin().getAccountLinkManager()
										.getLinkedAccounts();
								final int length = linkedPlayers.entrySet().size();

								String query = null;

								int i = 0;
								
								for (final Entry<String, UUID> target : linkedPlayers.entrySet()) {
									if (i % 10 == 0) {
										sender.sendMessage("Loading DiscordSRV Players... " + i + "/" + length);
									}
									
									if(i != 0) {
										query = query + ", (\"" + target.getValue() + "\", \"" + target.getKey() + "\")";
									}else {
										query = query + "REPLACE INTO " + getSql().getTable() + " (" + getSql().getUuidColumn()
												+ ", " + getSql().getDcIdColumn() + ") VALUES (\"" + target.getValue() + "\", \"" + target.getKey() + "\")";
									}

									i++;
								}
								
								if(query!=null) {
									
								}
								
								sender.sendMessage("Done!");
							} else {
								sender.sendMessage("DiscordSRV not found.");
							}
						} else {
							sender.sendMessage(this.getConfig().getString("VoiceChat.message.noPermission"));
						}
					} else {
						sender.sendMessage(this.getConfig().getString("VoiceChat.message.cmdNotFound"));
					}
				}
			} else {
				sender.sendMessage(this.getConfig().getString("VoiceChat.message.cmdNotFound"));
			}
		}
		return true;
	}

	/**
	 * isRegisterInternalMode() returns if the internal register mode is enabled
	 * 
	 * @return registerInternalMode
	 */
	public static boolean isRegisterInternalMode() {
		return registerInternalMode;
	}

	private static void setRegisterInternalMode(boolean registerInternalMode) {
		VoiceChatMain.registerInternalMode = registerInternalMode;
	}

	/**
	 * getNewRegisterCodeFor(Player player) registers and returns a register key for
	 * a player
	 * 
	 * @param player
	 * @return code
	 */
	public static int getNewRegisterCodeFor(Player player) {
		Random random = new Random();
		int code = random.nextInt(10000);
		while (String.valueOf(code).length() != 4) {
			code = random.nextInt(10000);
		}
		registerKeys.put(code, player);
		return code;
	}

	/**
	 * getPlayerByID(long id) - gets a VoicePlayer by his discord id returns null if
	 * not found
	 * 
	 * @param id
	 * @return target
	 */
	public static VoicePlayer getPlayerByID(long id) {
		Player target = getInstance().getServer().getPlayer(getSql().getUUIDbyDCID(id));
		if (target != null) {
			return getPlayers().get(target);
		} else {
			return null;
		}
	}
}