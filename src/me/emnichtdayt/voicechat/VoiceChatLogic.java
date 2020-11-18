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

import me.emnichtdayt.voicechat.entity.DCChannel;
import me.emnichtdayt.voicechat.entity.DiscordBot;
import me.emnichtdayt.voicechat.entity.VoicePlayer;

public class VoiceChatLogic {
	private VoiceChatMain pl;
	DiscordBot dc;

	protected VoiceChatLogic(VoiceChatMain pl) {
		this.pl = pl;
		this.dc = pl.getDcbot();
	}

	protected void doLogic(Iterator<? extends Player> iterator) {

		ArrayList<String> disabledWorlds = pl.getDisabledWorlds();

		Player target = iterator.next();
		if (!pl.getPlayers().containsKey(target)) {
			return;
		}

		VoicePlayer targetVoice = pl.getPlayers().get(target);

		if (!targetVoice.isAutomaticControlled() || targetVoice.getDiscordID() <= 0) { // Target is not meant to be
																						// managed
			return;
		}

		if (disabledWorlds.contains(target.getWorld().getName())) { // Player is in disabled world
			if (targetVoice.getCurrentChannel() != null) {
				targetVoice.moveTo(null);
			}
			return;
		}

		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(target.getLocation()));

		if (set.testState(WorldGuardPlugin.inst().wrapPlayer(target), pl.isDisabledRegion)) { // Player is in a disabled
																								// region
			if (targetVoice.getCurrentChannel() != null) {
				targetVoice.moveTo(null);
			}
			return;
		}

		if (set.testState(WorldGuardPlugin.inst().wrapPlayer(target), pl.isOwnVoiceRegion)) {

			String regionName = null;
			int maxPriority = -1;
			for (ProtectedRegion region : set) {
				if (region.getPriority() > maxPriority) {
					maxPriority = region.getPriority();
					regionName = region.getId();
				}
			}

			if (regionName == null) {
				return;
			}

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

		} else if (targetVoice.isInVoiceRegion) {
			targetVoice.isInVoiceRegion = false;
			targetVoice.moveTo(null);
		} else {
			DCChannel oldPlayerChannel = targetVoice.getCurrentChannel();

			if (oldPlayerChannel != null) { // Player is in a channel
				VoicePlayer oldChannelHost = oldPlayerChannel.getHost();
				if (oldChannelHost != null && oldChannelHost == targetVoice) { // Player is
																				// Channelhost
					List<Entity> nearby = target.getNearbyEntities(pl.getVoiceRangeX(), pl.getVoiceRangeY(),
							pl.getVoiceRangeZ());
					if (nearby.stream().anyMatch(ent -> ent instanceof Player)) { // host hat leute
																					// um sich
						DCChannel newHostChannel = null;
						for (Entity targetNearby : nearby) {
							if (targetNearby instanceof Player) {
								VoicePlayer targetNearbyVoice = pl.getPlayers().get((Player) targetNearby);
								if (targetNearbyVoice != null && targetNearbyVoice.getDiscordID() > 0
										&& targetNearbyVoice.getCurrentChannel() != null
										&& targetNearbyVoice.getCurrentChannel().getHost() != null
										&& targetNearbyVoice.getCurrentChannel().getHost().equals(targetNearbyVoice)
										&& targetNearbyVoice.getCurrentChannel().getUsers().size() <= targetVoice
												.getCurrentChannel().getUsers().size()) {
									newHostChannel = targetNearbyVoice.getCurrentChannel();
								}
							}
							if (newHostChannel != null) { // neuer host in der nähe gefunden, wird
															// zusammen gelegt
								while (oldPlayerChannel != null && !oldPlayerChannel.getUsers().isEmpty()) {
									oldPlayerChannel.getUsers().get(0).moveTo(newHostChannel);
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
					if (targetVoice.getCurrentChannel().getHost() == null) {
						return;
					}

					List<Entity> nearby = target.getNearbyEntities(pl.getVoiceRangeX(), pl.getVoiceRangeY(),
							pl.getVoiceRangeZ());

					if (!nearby.contains(targetVoice.getCurrentChannel().getHost().getPlayer())) {
						targetVoice.moveTo(null);
					}
				}
			} else { // Player not in channel
				List<Entity> nearby = target.getNearbyEntities(pl.getVoiceRangeX(), pl.getVoiceRangeY(),
						pl.getVoiceRangeZ());
				if (!nearby.stream().anyMatch(ent -> ent instanceof Player)) { // Player has no one around him
					return;
				}

				DCChannel newUserChannel = null;
				for (Entity entNearby : nearby) {
					if (entNearby instanceof Player) {
						VoicePlayer targetNearbyVoice = pl.getPlayers().get((Player) entNearby);
						ApplicableRegionSet setEnt = query
								.getApplicableRegions(BukkitAdapter.adapt(entNearby.getLocation()));
						if (targetNearbyVoice != null && targetNearbyVoice.getDiscordID() > 0
								&& targetNearbyVoice.isAutomaticControlled()
								&& !setEnt.testState(WorldGuardPlugin.inst().wrapPlayer((Player) entNearby),
										pl.isDisabledRegion)) {
							if (targetNearbyVoice.getCurrentChannel() == null) {
								if (newUserChannel == null) {
									newUserChannel = dc.createNewUserVoiceChat();
									targetVoice.moveTo(newUserChannel);
									newUserChannel.setHost(targetVoice);
									targetNearbyVoice.moveTo(newUserChannel);
								} else {
									targetNearbyVoice.moveTo(newUserChannel);
								}
							} else if (newUserChannel == null && targetNearbyVoice.getCurrentChannel() != null
									&& targetNearbyVoice.getCurrentChannel().getHost() != null
									&& targetNearbyVoice.getCurrentChannel().getHost().equals(targetNearbyVoice)) {
								targetVoice.moveTo(targetNearbyVoice.getCurrentChannel());
								break;
							}
						}
					}
				}

			}
		}

	}

}
