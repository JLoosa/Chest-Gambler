package me.jrl1004.java.chestgambler;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GambleCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(command.getName().equalsIgnoreCase("ChestGambler"))) return false;
		if (!(sender instanceof Player)) {
			messageColored(sender, "Only players may use ChestGambler commands");
			return true;
		}
		Player player = (Player) sender;
		if (args.length == 0) {
			messageColored(player, "Usage: /ChestGambler <CreateChest | Reset>");
			return true;
		}
		if (!(player.hasPermission("chestgambler.admin") || player.isOp())) {
			messageColored(player, "You do not have permission to do this");
			return true;
		}
		ItemStack stack = player.getInventory().getItemInMainHand();
		if (stack == null || stack.getType() != Material.CHEST) {
			messageColored(player, "Please hold the chest you would like to use");
			return true;
		}
		if (args[0].equalsIgnoreCase("CreateChest")) {
			if (args.length < 2) {
				messageColored(sender, "Usage: /ChestGambler CreateChest <Price>");
				return true;
			}
			String price = args[1].replaceAll("[^0-9]", "");
			int cost = Integer.parseInt(price);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + "" + cost);
			List<String> lore = meta.getLore() == null ? new ArrayList<String>() : meta.getLore();
			if (!lore.contains("Gambling Chest"))
				lore.add("Gambling Chest");
			meta.setLore(lore);
			stack.setItemMeta(meta);
			messageColored(player, "Held block is now a gambling chest");
		}
		if (args[0].equalsIgnoreCase("Reset")) {
			ItemMeta m = stack.getItemMeta();
			List<String> lore = m.getLore() == null ? new ArrayList<String>() : m.getLore();
			while (lore.contains("Gambling Chest"))
				lore.remove("Gambling Chest");
			m.setLore(lore);
			stack.setItemMeta(m);
			player.getInventory().setItemInMainHand(stack);
			messageColored(player, "Held block reset");
		}
		return true;
	}

	private String prefix = ChatColor.GREEN + "ChestGambler > " + ChatColor.AQUA;

	private void messageColored(CommandSender sender, String... messages) {
		if (sender == null) return;
		if (messages.length == 0) return;
		for (String msg : messages)
			sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
	}
}
