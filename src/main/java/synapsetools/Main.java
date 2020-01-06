package synapsetools;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.network.protocol.MobEffectPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.Config;

import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.event.player.SynapsePlayerTransferEvent;

import java.util.List;
import java.util.SplittableRandom;

public class Main extends PluginBase implements Listener {

    private Config c;

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
                            p.getFoodData().sendFoodLevel();
                        }
                    } catch (Exception ignore) {}
                }
            }, 1, true);
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof SynapsePlayer) {
            SynapsePlayer p = (SynapsePlayer) sender;
            String n = command.getName();
            if (n.equalsIgnoreCase("transfer") || n.equalsIgnoreCase("srv")) {
                if (c.getBoolean("transferCommandEnabled")) {
                    if (args.length > 0) {
                        if (p.getSynapseEntry().getServerDescription().equals(args[0])) {
                            p.sendMessage("\u00A7cYou are already on this server");
                        } else if (!p.transferByDescription(args[0])) {
                            p.sendMessage("\u00A7cUnknown server");
                        }

                        return true;
                    }
                }

                return false;
            } else if (n.equalsIgnoreCase("hub") || n.equalsIgnoreCase("lobby")) {
                if (c.getBoolean("hubCommandEnabled")) {
                    List<String> l = c.getStringList("lobbiesForThisServer");
                    if (l.size() == 0) {
                        p.sendMessage("\u00A7cThere is no lobbies set for this server");
                        return true;
                    }
                    if (!l.contains(p.getSynapseEntry().getServerDescription())) {
                        p.transferByDescription(l.get(new SplittableRandom().nextInt(l.size())));
                    } else {
                        p.sendMessage("\u00A7cYou are already on a lobby server");
                    }
                } else {
                    return false;
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

    @EventHandler
    public void onTransfer(SynapsePlayerTransferEvent e) {
        if (!e.isCancelled()) {
            if (!c.getBoolean("removeEffectsOnTransfer")) return;
            SynapsePlayer p = e.getPlayer();
            for (Effect ef : p.getEffects().values()) {
                MobEffectPacket pk = new MobEffectPacket();
                pk.eid = p.getId();
                pk.effectId = ef.getId();
                pk.eventId = MobEffectPacket.EVENT_REMOVE;
                p.dataPacket(pk);
            }
        }
    }
}
