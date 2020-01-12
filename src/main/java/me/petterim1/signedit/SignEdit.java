package me.petterim1.signedit;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSignPost;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;

import java.util.HashMap;
import java.util.Map;

public class SignEdit extends PluginBase implements Listener {

    private Map<Player, Location> editMode = new HashMap<>();

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (b instanceof BlockSignPost) {
            if (editMode.containsKey(p)) {
                editMode.put(p, b.getLocation());
                showEditForm(p, b.getLocation());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        editMode.remove(e.getPlayer());
    }

    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent e) {
        Player p = e.getPlayer();
        if (e.getResponse() == null) return;
        if (e.getWindow().wasClosed()) return;

        if (e.getWindow() instanceof FormWindowCustom) {
            if (((FormWindowCustom) e.getWindow()).getTitle().equals("SignEdit")) {
                String[] text = new String[4];
                text[0] = ((FormWindowCustom) e.getWindow()).getResponse().getInputResponse(1);
                text[1] = ((FormWindowCustom) e.getWindow()).getResponse().getInputResponse(2);
                text[2] = ((FormWindowCustom) e.getWindow()).getResponse().getInputResponse(3);
                text[3] = ((FormWindowCustom) e.getWindow()).getResponse().getInputResponse(4);

                Location loc = editMode.get(p);

                if (loc == null) {
                    p.sendMessage("§cError: Location is null");
                    return;
                }

                BlockEntitySign be = (BlockEntitySign) loc.getLevel().getBlockEntity(loc);

                if (be == null) {
                    p.sendMessage("§cError: Unable to find block entity for the sign at " + loc2string(loc));
                } else {
                    be.setText(text);
                    p.sendMessage("§aDone!");
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("signedit")) {
            if (!sender.hasPermission("signedit")) {
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can be only used in game");
                return true;
            }

            Player p = (Player) sender;

            if (editMode.containsKey(p)) {
                editMode.remove(p);
                p.sendMessage("§eSign edit mode §cdisabled");
            } else {
                editMode.put(p, null);
                p.sendMessage("§eSign edit mode §aenabled");
            }

            return true;
        }

        return false;
    }

    private void showEditForm(Player p, Location loc) {
        FormWindowCustom form = new FormWindowCustom("SignEdit");

        BlockEntitySign be = (BlockEntitySign) loc.getLevel().getBlockEntity(loc);

        if (be == null) {
            form.addElement(new ElementLabel("§cUnable to find block entity for the sign at " + loc2string(loc)));
        } else {
            form.addElement(new ElementLabel("§7Editing sign at " + loc2string(loc)));
            String[] text = be.getText();
            form.addElement(new ElementInput("", "", text[0]));
            form.addElement(new ElementInput("", "", text[1]));
            form.addElement(new ElementInput("", "", text[2]));
            form.addElement(new ElementInput("", "", text[3]));
        }

        p.showFormWindow(form);
    }

    private static String loc2string(Location loc) {
        return '[' + loc.getLevel().getName() + "] " + loc.x + ' ' + loc.y + ' ' + loc.z;
    }
}
