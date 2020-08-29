package me.emnichtdayt.voicechat;

import java.util.ArrayList;
import java.util.Iterator;

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
						if (!set.testState(WorldGuardPlugin.inst().wrapPlayer(target),VoiceChatMain.isDisabledRegion)) {
							if (set.testState(WorldGuardPlugin.inst().wrapPlayer(target),VoiceChatMain.isOwnVoiceRegion)) {
								String regionName = null;
								int maxPriority = -1;
								for (ProtectedRegion region : set) {
									if (region.getPriority() > maxPriority) {
										regionName = region.getId();
									}
								}
								if(regionName != null) {
									DCChannel regionChannel = dc.getChannelByName("VoiceChat-" + regionName);
									if(regionChannel==null){
										dc.createCustomChannel(regionName);
									}
								}
							} else {

							}
						}
					}
				}
			}
		}
	}
}
