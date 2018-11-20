package me.downthepark.backpack;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Backpack extends JavaPlugin implements Listener {

    private static File storage;
    private static Player player;

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        player = (Player) sender;
        storage = new File(getDataFolder(), player.getUniqueId().toString() + ".yml");
        Inventory backpack = getServer().createInventory(player, 54, player.getName() + "'s Backpack");

        if ((command.getName().equals("backpack"))) {
            if (player.hasPermission("backpack.use")) {
                if (!storage.exists()) {
                    saveDefaultConfig();
                    try {
                        if(storage.createNewFile()) {
                            player.openInventory(backpack);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (storage.exists()) {
                    try {
                        player.openInventory(fromBase64(FileUtils.readFileToString(storage, "UTF-8")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4[&c*&4]&c You are lacking the following permission node: backpack.use"));
            }
        }
        return false;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getName().equals(event.getPlayer().getName() + "'s Backpack")) {
            String data = toBase64(inventory);
            try {
                FileUtils.writeStringToFile(storage, data, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String toBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(inventory.getSize());
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    private static Inventory fromBase64(String data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt(), player.getName() + "'s Backpack");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, (ItemStack) dataInput.readObject());
        }
        dataInput.close();
        return inventory;
    }

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
}
