package me.jrl1004.java.chestgambler;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GambleListener implements Listener {

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		if (!(event.getBlockPlaced().getState() instanceof Chest)) {
			System.out.println("Not a chest");
			return;
		}
		Player player = event.getPlayer();
		ItemStack stack = event.getItemInHand();
		if (stack.getItemMeta().getLore() == null || !stack.getItemMeta().getLore().contains("Gambling Chest")) {
			System.out.println("Missing Lore");
			return;
		}
		if (!(player.hasPermission("chestgambler.admin") || player.isOp())) {
			messageColored(player, "You do not have permission to place Gambling Chests");
			event.setCancelled(true);
			return;
		}
		GambleChest chest = new GambleChest((Chest) event.getBlockPlaced().getState(), Integer.parseInt(ChatColor.stripColor(stack.getItemMeta().getDisplayName())));
		ChestGambler.getInstance().chests.add(chest);
		messageColored(event.getPlayer(), "Gambling Chest created with a price of: " + chest.getCost());
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		GambleChest chest = ChestGambler.getInstance().findChest(event.getBlock().getLocation());
		if (chest == null) return;
		if (!(player.hasPermission("chestgambler.admin") || player.isOp())) {
			messageColored(player, "You do not have permission to remove Gambling Chests");
			event.setCancelled(true);
			return;
		}
		ChestGambler.getInstance().chests.remove(chest);
		messageColored(event.getPlayer(), "Gambling Chest removed");
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block block = event.getClickedBlock();
		if (block.getType() != Material.CHEST) return;
		GambleChest chest = ChestGambler.getInstance().findChest(block.getLocation());
		if (chest == null)
			return;
		if (event.getPlayer().isSneaking() && event.getPlayer().hasPermission("chestgambler.admin")) return;
		chest.gamble(event.getPlayer());
		event.setCancelled(true);
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		if (!event.getPlayer().hasPermission("chestgambler.admin")) return;
		ChestGambler.getInstance().updateInvs();
	}

	private String prefix = ChatColor.GREEN + "ChestGambler > " + ChatColor.AQUA;

	private void messageColored(CommandSender sender, String... messages) {
		if (sender == null) return;
		if (messages.length == 0) return;
		for (String msg : messages)
			sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', msg));
	}
}
