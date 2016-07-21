package me.jrl1004.java.chestgambler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ChestGambler extends JavaPlugin {

	private static ChestGambler instance;
	public Set<GambleChest> chests;
	private static Economy economy;
	private static Random random;

	@Override
	public void onEnable() {
		if (!setupEconomy()) {
			getLogger().warning("Unable to register economy provider. Please verify your Vault installation.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		instance = this;
		chests = new HashSet<GambleChest>();
		random = new Random();
		Bukkit.getPluginManager().registerEvents(new GambleListener(), this);
		getCommand("chestgambler").setExecutor(new GambleCommand());

		// Load in the old stones if we need to
		if (getConfig() == null)
			saveDefaultConfig();
		if (getConfig().getKeys(false).size() == 0) return;
		Set<String> keys = getConfig().getKeys(false);
		for (String s : keys) {
			World world = Bukkit.getWorld(s);
			List<String> list = getConfig().getStringList(s);
			for (String data : list) {
				String[] serialized = data.split(":");
				GambleChest chest = new GambleChest(world, serialized[0], serialized[1]);
				getLogger().info("Gambling Chest #" + (chests.size() + 1) + " loading");
				if (chest.verifyData() && chests.add(chest)) getLogger().fine("Gambling loaded sucessfully");
				else getLogger().warning("Gambling Chest failed to load");
			}
		}
		if (chests.size() > 0)
			getLogger().info("Sucessfully loaded in " + chests.size() + " Gambling Chests!");
		super.onEnable();
	}

	@Override
	public void onDisable() {
		instance = null;
		HandlerList.unregisterAll(this);
		// Save the chests we currently have to file
		if (chests == null || chests.isEmpty()) return;
		Map<World, List<String>> sd = new HashMap<World, List<String>>();
		for (GambleChest gChest : chests)
			if (sd.containsKey(gChest.getWorld())) sd.get(gChest.getWorld()).add(gChest.getSaveString());
			else sd.put(gChest.getWorld(), new ArrayList<String>(Arrays.asList(gChest.getSaveString())));
		for (World w : sd.keySet())
			getConfig().set(w.getName(), sd.get(w));
		saveConfig();
		getLogger().info("Saved " + chests.size() + " Gambling Chests to file");
		chests.clear();
		chests = null;
		super.onDisable();
		super.onDisable();
	}

	public static ChestGambler getInstance() {
		return instance;
	}

	private boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public static Economy getEconomy() {
		return economy;
	}

	public static Random getRandom() {
		return random;
	}

	public GambleChest findChest(Location location) {
		if (chests.isEmpty()) return null;
		for (GambleChest gambleChest : chests) {
			if (gambleChest.getWorld().hashCode() != location.getWorld().hashCode()) continue;
			Vector vector = location.toVector();
			if (gambleChest.getVec().getBlockX() != vector.getBlockX()) continue;
			if (gambleChest.getVec().getBlockY() != vector.getBlockY()) continue;
			if (gambleChest.getVec().getBlockZ() != vector.getBlockZ()) continue;
			return gambleChest;
		}
		return null;
	}

	public void updateInvs() {
		if(chests.isEmpty()) return;
		 for (GambleChest c : chests)
			 c.updateValue();
	}

}
