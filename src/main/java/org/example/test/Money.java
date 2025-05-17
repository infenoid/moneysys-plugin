package org.example.test;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Money extends JavaPlugin implements CommandExecutor, Listener, TabCompleter {
    private final Map<UUID, Long> bankAccounts = new HashMap<>(); // Use long for balances
    private final Map<Material, Integer> exchangeRates = new HashMap<>();
    private static final long MAX_BALANCE = 1_000_000_000L; // Maximum balance limit

    @Override
    public void onEnable() {
        PluginCommand bankCommand = this.getCommand("bank");
        if (bankCommand != null) {
            bankCommand.setExecutor(this);
            bankCommand.setTabCompleter(this);
        }
        this.getServer().getPluginManager().registerEvents(this, this);
        loadBalances();
        fixNegativeBalances(); // Fix negative balances on startup
        setupExchangeRates();
    }

    @Override
    public void onDisable() {
        saveBalances();
    }

    private void saveBalances() {
        for (UUID uuid : bankAccounts.keySet()) {
            getConfig().set("balances." + uuid, bankAccounts.get(uuid));
        }
        saveConfig();
    }

    private void loadBalances() {
        if (getConfig().contains("balances")) {
            getConfig().getConfigurationSection("balances")
                    .getKeys(false)
                    .forEach(uuid -> bankAccounts.put(UUID.fromString(uuid), getConfig().getLong("balances." + uuid)));
        }
    }

    private void fixNegativeBalances() {
        for (Map.Entry<UUID, Long> entry : bankAccounts.entrySet()) {
            if (entry.getValue() < 0) {
                bankAccounts.put(entry.getKey(), 0L); // Reset negative balances to 0
            }
        }
        saveBalances();
    }

    private void setupExchangeRates() {
        exchangeRates.put(Material.GOLD_NUGGET, 20);
        exchangeRates.put(Material.GOLD_INGOT, 60);
        exchangeRates.put(Material.GOLD_BLOCK, 60 * 9);
        exchangeRates.put(Material.DIAMOND, 300);
        exchangeRates.put(Material.DIAMOND_BLOCK, 300 * 9);
        exchangeRates.put(Material.NETHERITE_INGOT, 1200);
        exchangeRates.put(Material.NETHERITE_BLOCK, 1200 * 9);
        exchangeRates.put(Material.NETHERITE_SCRAP, 225);
        exchangeRates.put(Material.ANCIENT_DEBRIS, 900);
        exchangeRates.put(Material.EMERALD, 180);
        exchangeRates.put(Material.EMERALD_BLOCK, 180 * 9);
        exchangeRates.put(Material.IRON_NUGGET, 5);
        exchangeRates.put(Material.IRON_INGOT, 30);
        exchangeRates.put(Material.IRON_BLOCK, 30 * 9);
        exchangeRates.put(Material.COAL, 10);
        exchangeRates.put(Material.COAL_BLOCK, 10 * 9);
        exchangeRates.put(Material.LAPIS_LAZULI, 15);
        exchangeRates.put(Material.LAPIS_BLOCK, 15 * 9);
        exchangeRates.put(Material.REDSTONE, 8);
        exchangeRates.put(Material.REDSTONE_BLOCK, 8 * 9);
        exchangeRates.put(Material.QUARTZ, 12);
        exchangeRates.put(Material.QUARTZ_BLOCK, 12 * 4);
        exchangeRates.put(Material.COPPER_INGOT, 25);
        exchangeRates.put(Material.COPPER_BLOCK, 25 * 9);
        exchangeRates.put(Material.AMETHYST_SHARD, 50);
        exchangeRates.put(Material.AMETHYST_BLOCK, 50 * 4);
        exchangeRates.put(Material.RAW_IRON, 28);
        exchangeRates.put(Material.RAW_IRON_BLOCK, 28 * 9);
        exchangeRates.put(Material.RAW_GOLD, 55);
        exchangeRates.put(Material.RAW_GOLD_BLOCK, 55 * 9);
        exchangeRates.put(Material.RAW_COPPER, 23);
        exchangeRates.put(Material.RAW_COPPER_BLOCK, 23 * 9);
        exchangeRates.put(Material.DEEPSLATE_COAL_ORE, 12);
        exchangeRates.put(Material.DEEPSLATE_IRON_ORE, 35);
        exchangeRates.put(Material.DEEPSLATE_GOLD_ORE, 70);
        exchangeRates.put(Material.DEEPSLATE_DIAMOND_ORE, 350);
        exchangeRates.put(Material.DEEPSLATE_EMERALD_ORE, 200);
        exchangeRates.put(Material.DEEPSLATE_LAPIS_ORE, 20);
        exchangeRates.put(Material.DEEPSLATE_REDSTONE_ORE, 10);
        exchangeRates.put(Material.DEEPSLATE_COPPER_ORE, 30);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /bank <subcommand>");
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "balance":
                player.sendMessage(ChatColor.GREEN + "Your balance: " + bankAccounts.getOrDefault(uuid, 0L) + " Zee.");
                return true;
            case "pay":
                return handlePay(player, args, uuid);
            case "rich":
                player.sendMessage(ChatColor.GOLD + "Top 5 Richest Players:");
                bankAccounts.entrySet().stream()
                        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                        .limit(5)
                        .forEach(entry -> {
                            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                            if (playerName == null) playerName = "Unknown";
                            player.sendMessage(ChatColor.YELLOW + playerName + ": " + ChatColor.GREEN + entry.getValue() + " Zee");
                        });
                return true;
            case "exchange":
                return handleExchange(player, args, uuid);
            case "reexchange":
                return handleReexchange(player, args, uuid);
            case "value":
                return handleValue(player, args);
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return false;
        }
    }

    private boolean handlePay(Player player, String[] args, UUID uuid) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /bank pay <player> <amount>");
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found or offline.");
            return false;
        }
        try {
            long amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive.");
                return false;
            }
            long senderBalance = bankAccounts.getOrDefault(uuid, 0L);
            long targetBalance = bankAccounts.getOrDefault(target.getUniqueId(), 0L);
            if (senderBalance < amount) {
                player.sendMessage(ChatColor.RED + "You do not have enough Zee.");
                return false;
            }
            if (targetBalance + amount > MAX_BALANCE) {
                player.sendMessage(ChatColor.RED + "The recipient's balance would exceed the maximum limit.");
                return false;
            }
            bankAccounts.put(uuid, senderBalance - amount);
            bankAccounts.put(target.getUniqueId(), targetBalance + amount);
            player.sendMessage(ChatColor.GREEN + "You paid " + target.getName() + " " + amount + " Zee.");
            saveBalances();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount.");
        }
        return true;
    }

    private boolean handleExchange(Player player, String[] args, UUID uuid) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /bank exchange <item> <number of items>");
            return false;
        }
        return processExchange(player, args, uuid, false);
    }

    private boolean handleReexchange(Player player, String[] args, UUID uuid) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /bank reexchange <item> <number of items>");
            return false;
        }
        return processExchange(player, args, uuid, true);
    }

    private boolean processExchange(Player player, String[] args, UUID uuid, boolean isReexchange) {
        Material material = Material.matchMaterial(args[1].toUpperCase());
        if (material == null || !exchangeRates.containsKey(material)) {
            player.sendMessage(ChatColor.RED + "Invalid item.");
            return false;
        }
        int itemCount;
        try {
            itemCount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Number of items must be an integer.");
            return false;
        }
        long exchangeValue = (long) exchangeRates.get(material) * itemCount;
        long currentBalance = bankAccounts.getOrDefault(uuid, 0L);
        player.sendMessage(ChatColor.YELLOW + "Current balance: " + currentBalance + " Zee.");
        player.sendMessage(ChatColor.YELLOW + "Exchange value: " + exchangeValue + " Zee.");

        if (isReexchange) {
            if (currentBalance < exchangeValue) {
                player.sendMessage(ChatColor.RED + "You do not have enough Zee.");
                return false;
            }
            bankAccounts.put(uuid, currentBalance - exchangeValue);
            player.getInventory().addItem(new ItemStack(material, itemCount));
            player.sendMessage(ChatColor.GREEN + "Reexchanged " + exchangeValue + " Zee for " + itemCount + " " + material.name() + " successfully.");
        } else {
            if (!player.getInventory().containsAtLeast(new ItemStack(material, itemCount), itemCount)) {
                player.sendMessage(ChatColor.RED + "You do not have enough items.");
                return false;
            }
            player.getInventory().removeItem(new ItemStack(material, itemCount));
            bankAccounts.put(uuid, currentBalance + exchangeValue);
            player.sendMessage(ChatColor.GREEN + "Exchanged " + itemCount + " " + material.name() + " for " + exchangeValue + " Zee successfully.");
        }
        player.sendMessage(ChatColor.YELLOW + "New balance: " + bankAccounts.get(uuid) + " Zee.");
        saveBalances();
        return true;
    }

    private boolean handleValue(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bank value <item>");
            return false;
        }
        Material material = Material.matchMaterial(args[1].toUpperCase());
        if (material == null || !exchangeRates.containsKey(material)) {
            player.sendMessage(ChatColor.RED + "Invalid item.");
            return false;
        }
        int value = exchangeRates.get(material);
        player.sendMessage(ChatColor.GREEN + "The value of " + material.name() + " is " + value + " Zee.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        List<String> subcommands = Arrays.asList("balance", "pay", "rich", "exchange", "reexchange", "value");
        if (args.length == 1) {
            return subcommands;
        }

        if (args[0].equalsIgnoreCase("pay") && args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }

        if ((args[0].equalsIgnoreCase("exchange") || args[0].equalsIgnoreCase("reexchange") || args[0].equalsIgnoreCase("value")) && args.length == 2) {
            return exchangeRates.keySet().stream()
                    .map(material -> material.name().toLowerCase())
                    .toList();
        }

        return Collections.emptyList();
    }
}