package me.emnichtdayt.voicechat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

public class VoiceChatLogic {
	private static ArrayList<String> disabledWorlds = null;

	protected static void doLogic(VoiceChatMain pl) {

		while (!VoiceChatMain.kickList.isEmpty()) {
			VoiceChatMain.kickList.get(0).kickPlayer(DCServerVoiceChannelMemberLeaveListener.voiceDisconnectMessage);
			VoiceChatMain.kickList.remove(0);
		}

		disabledWorlds = VoiceChatMain.getDisabledWorlds();

		DiscordBot dc = VoiceChatMain.getDcbot();

		for (Iterator<? extends Player> iterator = pl.getServer().getOnlinePlayers().iterator(); iterator.hasNext();) {
			Player target = iterator.next();
			if (VoiceChatMain.getPlayers().containsKey(target)) {
				VoicePlayer targetVoice = VoiceChatMain.getPlayers().get(target);
				if (targetVoice.isAutomaticControlled() && targetVoice.getDiscordID() > 0) {
					if (!disabledWorlds.contains(target.getWorld().getName())) {

						RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
						RegionQuery query = container.createQuery();
						ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(target.getLocation()));
						if (!set.testState(WorldGuardPlugin.inst().wrapPlayer(target),
								VoiceChatMain.isDisabledRegion)) {
							if (set.testState(WorldGuardPlugin.inst().wrapPlayer(target),
									VoiceChatMain.isOwnVoiceRegion)) {

								String regionName = null;
								int maxPriority = -1;
								for (ProtectedRegion region : set) {
									if (region.getPriority() > maxPriority) {
										maxPriority = region.getPriority();
										regionName = region.getId();
									}
								}
								if (regionName != null) {
									DCChannel regionChannel = dc.getChannelByName("VoiceChat-" + regionName);
									if (regionChannel == null) {
										targetVoice.moveTo(dc.createCustomChannel(regionName));
									} else if (targetVoice.getCurrentChannel() != null
											&& !targetVoice.getCurrentChannel().equals(regionChannel)) {
										targetVoice.moveTo(regionChannel);
									} else if (targetVoice.getCurrentChannel() == null) {
										targetVoice.moveTo(regionChannel);
									}
									targetVoice.isInVoiceRegion = true;
								}
							} else if (targetVoice.isInVoiceRegion) {
								targetVoice.isInVoiceRegion = false;
								targetVoice.moveTo(null);
							} else {
								DCChannel oldPlayerChannel = targetVoice.getCurrentChannel();

								if (oldPlayerChannel != null) { // Player is in a channel
									VoicePlayer oldChannelHost = oldPlayerChannel.getHost();
									if (oldChannelHost != null && oldChannelHost == targetVoice) { // Player is
																									// Channelhost
										List<Entity> nearby = target.getNearbyEntities(VoiceChatMain.getVoiceRangeX(),
												VoiceChatMain.getVoiceRangeY(), VoiceChatMain.getVoiceRangeZ());
										if (nearby.stream().anyMatch(ent -> ent instanceof Player)) { // host hat leute
																										// um sich
											DCChannel newHostChannel = null;
											for (Entity targetNearby : nearby) {
												if (targetNearby instanceof Player) {
													VoicePlayer targetNearbyVoice = VoiceChatMain.getPlayers()
															.get((Player) targetNearby);
													if (targetNearbyVoice != null
															&& targetNearbyVoice.getDiscordID() > 0
															&& targetNearbyVoice.getCurrentChannel() != null
															&& targetNearbyVoice.getCurrentChannel().getHost() != null
															&& targetNearbyVoice.getCurrentChannel().getHost()
																	.equals(targetNearbyVoice)
															&& targetNearbyVoice.getCurrentChannel().getUsers()
																	.size() <= targetVoice.getCurrentChannel()
																			.getUsers().size()) {
														newHostChannel = targetNearbyVoice.getCurrentChannel();
													}
												}
												if (newHostChannel != null) { // neuer host in der nähe gefunden, wird
																				// zusammen gelegt
													for (VoicePlayer targetToMove : oldPlayerChannel.getUsers()) {
														targetToMove.moveTo(newHostChannel);
													}
													oldPlayerChannel = null;
												}
											}
										} else { // Host hat keinen um sich kanal löschen
											if (targetVoice.getCurrentChannel() != null) {
												oldPlayerChannel.remove();
											}
										}
									} else { // Player is no Channel Host but in Channel
										if (targetVoice.getCurrentChannel().getHost() != null) {

											List<Entity> nearby = target.getNearbyEntities(
													VoiceChatMain.getVoiceRangeX(), VoiceChatMain.getVoiceRangeY(),
													VoiceChatMain.getVoiceRangeZ());

											boolean foundPlayer = false;

											for (Entity targetNearby : nearby) {
												if (targetNearby instanceof Player) {
													for (VoicePlayer targetInChannelVoice : targetVoice
															.getCurrentChannel().getUsers()) {
														Player targetInChannel = targetInChannelVoice.getPlayer();
														if (targetNearby.equals(targetInChannel)) {
															foundPlayer = true;
															break;
														}
													}
												}
											}

											if (!foundPlayer) {
												targetVoice.moveTo(null);
											}

										}
									}
								} else { // Player not in channel
									List<Entity> nearby = target.getNearbyEntities(VoiceChatMain.getVoiceRangeX(),
											VoiceChatMain.getVoiceRangeY(), VoiceChatMain.getVoiceRangeZ());
									if (nearby.stream().anyMatch(ent -> ent instanceof Player)) { // Person hat leute um
										DCChannel newUserChannel = null;
										for (Entity entNearby : nearby) {
											if (entNearby instanceof Player) {
												VoicePlayer targetNearbyVoice = VoiceChatMain.getPlayers()
														.get((Player) entNearby);
												ApplicableRegionSet setEnt = query.getApplicableRegions(
														BukkitAdapter.adapt(entNearby.getLocation()));
												if (targetNearbyVoice != null && targetNearbyVoice.getDiscordID() > 0
														&& targetNearbyVoice.isAutomaticControlled()
														&& !setEnt.testState(
																WorldGuardPlugin.inst().wrapPlayer((Player) entNearby),
																VoiceChatMain.isDisabledRegion)) {
													if (targetNearbyVoice.getCurrentChannel() == null) {
														if (newUserChannel == null) {
															newUserChannel = dc.createNewUserVoiceChat();
															targetVoice.moveTo(newUserChannel);
															newUserChannel.setHost(targetVoice);
															targetNearbyVoice.moveTo(newUserChannel);
														} else {
															targetNearbyVoice.moveTo(newUserChannel);
														}
													} else if (newUserChannel == null
															&& targetNearbyVoice.getCurrentChannel() != null
															&& targetNearbyVoice.getCurrentChannel()
																	.getHost() != null) {
														targetVoice.moveTo(targetNearbyVoice.getCurrentChannel());
														break;
													}
												}
											}
										}
									}
								}
							}
						} else if (targetVoice.getCurrentChannel() != null) { // Player is in a disabled region
							targetVoice.moveTo(null);
						}
					} else if (targetVoice.getCurrentChannel() != null) { // Player is in disabled world
						targetVoice.moveTo(null);
					}
				}
			}
		}
	}
}
