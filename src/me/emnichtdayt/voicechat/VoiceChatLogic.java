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

		disabledWorlds = VoiceChatMain.getDisabledWorlds();

		DiscordBot dc = VoiceChatMain.getDcbot();

		for (Iterator<? extends Player> iterator = pl.getServer().getOnlinePlayers().iterator(); iterator.hasNext();) {
			Player target = iterator.next();
			if (VoiceChatMain.getPlayers().containsKey(target)) {
				VoicePlayer targetVoice = VoiceChatMain.getPlayers().get(target);
				if (targetVoice.isAutomaticControlled()) {
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
										regionName = region.getId();
									}
								}
								if (regionName != null) {
									DCChannel regionChannel = dc.getChannelByName("VoiceChat-" + regionName);
									if (regionChannel == null) {
										targetVoice.moveTo(dc.createCustomChannel(regionName));
									} else if (!targetVoice.getCurrentChannel().equals(regionChannel)) {
										targetVoice.moveTo(regionChannel);
									}
								}
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
													if (targetNearbyVoice.getCurrentChannel() == null) {
														targetNearbyVoice.moveTo(oldPlayerChannel);
													} else if (targetNearbyVoice.getCurrentChannel().getHost()
															.equals(targetNearbyVoice)) {
														newHostChannel = targetNearbyVoice.getCurrentChannel();
													}
												}
												if (newHostChannel != null) { // neuer host in der nähe gefunden, wird
																				// zusammen gelegt
													for (VoicePlayer targetToMove : oldPlayerChannel.getUsers()) {
														targetToMove.moveTo(newHostChannel);
													}
													oldPlayerChannel.remove();
													oldPlayerChannel = null;
												}
											}
										} else { // Host hat keinen um sich kanal löschen
											oldPlayerChannel.remove();
										}
									} else { // Player is no Channel Host but in Channel
										List<Entity> nearby = target.getNearbyEntities(VoiceChatMain.getVoiceRangeX(),
												VoiceChatMain.getVoiceRangeY(), VoiceChatMain.getVoiceRangeZ());
										if (!nearby.stream().anyMatch(ent -> ent instanceof Player)) { 
											targetVoice.moveTo(null);
										}
									}
								}else {
									List<Entity> nearby = target.getNearbyEntities(VoiceChatMain.getVoiceRangeX(),
											VoiceChatMain.getVoiceRangeY(), VoiceChatMain.getVoiceRangeZ());
									if (nearby.stream().anyMatch(ent -> ent instanceof Player)) { //Person hat leute um sich
										for (Entity targetNearby : nearby) {
											if(targetNearby instanceof Player) {
												VoicePlayer targetNearbyVoice = VoiceChatMain.getPlayers().get(targetNearby);
												if(targetNearbyVoice.getCurrentChannel().getHost() != null && targetNearbyVoice.getCurrentChannel().getHost().equals(targetNearbyVoice)) {
													targetVoice.moveTo(targetNearbyVoice.getCurrentChannel());
												}
											}
										}
									}
								}
							}
						} else { // Player is in a disabled region
							targetVoice.moveTo(null);
						}
					} else { // Player is in disabled world
						targetVoice.moveTo(null);
					}
				}
			}
		}
	}
}
