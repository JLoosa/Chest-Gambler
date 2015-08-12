package me.jrl1004.java.chestgambler;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GambleChest {

	private Chest chest;
	private World world;
	private Vector vec;
	private int cost;
	private int totalItems;

	public GambleChest(Chest chest, int price) {
		this.chest = chest;
		this.world = chest.getWorld();
		this.vec = chest.getLocation().toVector();
		this.cost = price;
		updateValue();
	}

	public GambleChest(World world, String price, String vector) {
		Block block = fromString(vector).toLocation(world).getBlock();
		if (!(block.getState() instanceof Chest)) return;
		this.chest = (Chest) block.getState();
		this.world = world;
		this.vec = chest.getLocation().toVector();
		this.cost = Integer.parseInt(price);
		updateValue();
	}

	private Vector fromString(String s) {
		if (!s.contains(",")) return null;
		String[] split = s.split(",");
		return new Vector(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
	}

	public void updateValue() {
		totalItems = 0;
		ItemStack[] items = chest.getBlockInventory().getContents();
		if (items.length > 0)
			for (ItemStack i : items) {
				if (i == null || i.getType() == Material.AIR) continue;
				totalItems += i.getAmount();
			}
	}

	public ItemStack getRandomItem() {
		int itemNumber = ChestGambler.getRandom().nextInt(totalItems) + 1;
		ItemStack[] items = chest.getBlockInventory().getContents();
		for (ItemStack i : items) {
			if (i == null || i.getType() == Material.AIR) continue;
			itemNumber -= i.getAmount();
			if (itemNumber <= 0) {
				ItemStack reward = i.clone();
				reward.setAmount(1);
				return reward;
			}
		}
		return null;
	}

	public World getWorld() {
		return world;
	}

	public String getStorableVector() {
		return vec.getBlockX() + "," + vec.getBlockY() + "," + vec.getBlockZ();
	}

	public String getSaveString() {
		return cost + ":" + getStorableVector();
	}

	public Vector getVec() {
		return vec.clone();
	}

	public int getCost() {
		return cost;
	}

	public void gamble(Player player) {
		if (totalItems == 0) {
			messageColored(player, "This chest has no items in it.");
			return;
		}
		Economy eco = ChestGambler.getEconomy();
		if (!eco.has(player, world.getName(), cost)) {
			messageColored(player, "You need at least " + cost + " to gamble at this location");
			return;
		}
		int ff = player.getInventory().firstEmpty();
		if (ff == -1) {
			messageColored(player, "You must have a t least one free inventory slot to gamble");
			return;
		}
		eco.withdrawPlayer(player, world.getName(), cost);
		ItemStack stack = getRandomItem();
		messageColored(player, "You won a(n) " + (stack.getItemMeta().getDisplayName() == null ? stack.getType().toString() : stack.getItemMeta().getDisplayName()));
		player.getInventory().setItem(ff, stack);
		player.updateInventory();
	}

	private String prefix = ChatColor.GREEN + "ChestGambler > " + ChatColor.AQUA;

	private void messageColored(CommandSender sender, String... messages) {
		if (sender == null) return;
		if (messages.length == 0) return;
		for (String msg : messages)
			sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
	}

	public boolean verifyData() {
		try {
			Block b = vec.toLocation(world).getBlock();
			return b != null && b.getType() != Material.AIR && b.getState() instanceof Chest;
		} catch (Exception exc) {
			return false;
		}
	}
}
