package plugin.borealcore.functions.cooking.competition.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.competition.Competition;
import plugin.borealcore.listener.SimpleListener;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.util.HashMap;

public class BossBarManager extends Function {

    public static HashMap<Player, BossBarSender> cache = new HashMap<>();
    private final SimpleListener simpleListener;

    public BossBarManager() {
        this.simpleListener = new SimpleListener(this);
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.simpleListener, BorealCore.plugin);
    }

    @Override
    public void unload() {
        if (this.simpleListener != null) HandlerList.unregisterAll(this.simpleListener);
        for (BossBarSender bossBarSender : cache.values()) {
            bossBarSender.hide();
        }
        cache.clear();
    }

    @Override
    public void onQuit(Player player) {
        BossBarSender sender = cache.get(player);
        if (sender != null) {
            if (sender.getStatus()) {
                sender.hide();
            }
            cache.remove(player);
        }
    }

    @Override
    public void onJoin(Player player) {
        if (Competition.currentCompetition != null) {
            if (Competition.currentCompetition.isJoined(player) && cache.get(player) == null) {
                BossBarSender sender = new BossBarSender(player, Competition.currentCompetition.getCompetitionConfig().getBossBarConfig());
                if (!sender.getStatus()) {
                    sender.show();
                }
                cache.put(player, sender);
            } else {
                AdventureUtil.playerMessage(player, MessageManager.competitionOn);
            }
        }
    }

    public void tryJoin(Player player) {
        if (cache.get(player) == null) {
            BossBarSender sender = new BossBarSender(player, Competition.currentCompetition.getCompetitionConfig().getBossBarConfig());
            if (!sender.getStatus()) {
                sender.show();
            }
            cache.put(player, sender);
            for (String joinCmd : Competition.currentCompetition.getCompetitionConfig().getJoinCommand()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), joinCmd.replace("{player}", player.getName()));
            }
        }
    }
}
