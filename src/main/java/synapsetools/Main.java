package synapsetools;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import org.itxtech.synapseapi.SynapsePlayer;

import java.util.List;
import java.util.Random;

public class Main extends PluginBase implements Listener {

    Config c;

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        c = getConfig();

        if (c.getBoolean("enableFoodBarHack")) {
            getServer().getScheduler().scheduleRepeatingTask(new cn.nukkit.scheduler.Task() {
                @Override
                public void onRun(int i) {
                    try {
                        for (Player p : Server.getInstance().getOnlinePlayers().values()) {
                            p.getFoodData().setLevel(p.getFoodData().getLevel());
                        }
                    } catch (Exception ignore) {}
                }
            }, 1, true);
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof SynapsePlayer) {
            SynapsePlayer p = (SynapsePlayer) sender;
            if (command.getName().equalsIgnoreCase("transfer")) {
                if (c.getBoolean("transferCommandEnabled")) {
                    if (args.length > 0) {
                        if (p.getSynapseEntry().getServerDescription().equalsIgnoreCase(args[0])) {
                            p.sendMessage("\u00A7cYou are already on this server");
                        } else {
                            p.transferByDescription(args[0]);
                        }
                    } else {
                        p.sendMessage("Usage: /transfer <target>");
                    }
                }
            } else if (command.getName().equalsIgnoreCase("hub") || command.getName().equalsIgnoreCase("lobby")) {
                if (c.getBoolean("hubCommandEnabled")) {
                    List<String> l = c.getStringList("lobbiesForThisServer");
                    if (l.size() == 0) {
                        p.sendMessage("\u00A7cThere is no lobbies set for this server");
                        return true;
                    }
                    if (!l.contains(p.getSynapseEntry().getServerDescription())) {
                        p.transferByDescription(l.get(new Random().nextInt(l.size())));
                    } else {
                        p.sendMessage("\u00A7cYou are already on a lobby server");
                    }
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!c.getBoolean("hideSynapseMessages")) return;
        String message = e.getQuitMessage().toString();
        if (message.equals("timeout") || message.equals("generic reason") || message.equals("client disconnect") || message.equals("unknown")) {
            e.setQuitMessage("");
        }
    }
}
