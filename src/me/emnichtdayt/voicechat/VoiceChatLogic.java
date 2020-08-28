package me.emnichtdayt.voicechat;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

public class VoiceChatLogic {
	private static ArrayList<String> disabledWorlds = null;
	
	protected static void doLogic(VoiceChatMain pl) {
		

		disabledWorlds = VoiceChatMain.getDisabledWorlds();
		
		for (Iterator<? extends Player> iterator = pl.getServer().getOnlinePlayers().iterator(); iterator.hasNext();) {
	        Player target = iterator.next();
	        
	        if(!disabledWorlds.contains(target.getWorld().getName())) {
	        	RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
	        	RegionQuery query = container.createQuery();
	        	ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(target.getLocation()));
	        	if(!set.testState(WorldGuardPlugin.inst().wrapPlayer(target), VoiceChatMain.isDisabledRegion)) {
	        		
	        	}	        	
	        }
	    }
	}
}
