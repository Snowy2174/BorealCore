package plugin.borealcore.functions.cooking.competition.bossbar;


import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.competition.Competition;
import plugin.borealcore.utility.AdventureUtil;

public class BossBarSender {

    private final Player player;
    private final int size;
    private final TextCache[] texts;
    private final BossBarConfig config;
    private final BossBar bossBar;

    private int timer_1;
    private int timer_2;
    private int counter;
    private TextCache text;
    private BukkitTask bukkitTask;
    private boolean force;
    private boolean isShown;

    public BossBarSender(Player player, BossBarConfig config) {
        this.player = player;
        this.config = config;

        String[] str = config.getText();
        this.size = str.length;
        this.texts = new TextCache[size];

        for (int i = 0; i < size; i++) {
            texts[i] = new TextCache(player, str[i]);
        }

        this.text = texts[0];
        this.bossBar = BossBar.bossBar(
                AdventureUtil.getComponentFromMiniMessage(text.getLatestValue()),
                Competition.currentCompetition.getProgress(),
                config.getColor(),
                config.getOverlay()
        );

        this.isShown = false;
    }

    public void setText(int position) {
        this.text = texts[position];
        this.force = true;
    }

    public void show() {
        if (isShown) return;

        isShown = true;
        player.showBossBar(bossBar);

        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (size != 1) {
                    timer_2++;
                    if (timer_2 > config.getInterval()) {
                        timer_2 = 0;
                        counter = (counter + 1) % size;
                        setText(counter);
                    }
                }

                if (timer_1 < config.getRate()) {
                    timer_1++;
                } else {
                    timer_1 = 0;

                    if (text.update() || force) {
                        force = false;
                        bossBar.name(AdventureUtil.getComponentFromMiniMessage(text.getLatestValue()));
                        bossBar.progress(Competition.currentCompetition.getProgress());
                    }
                }
            }
        }.runTaskTimerAsynchronously(BorealCore.plugin, 0, 1);
    }

    public void hide() {
        if (!isShown) return;

        player.hideBossBar(bossBar);
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
        isShown = false;
    }

    public boolean getStatus() {
        return isShown;
    }

    public BossBarConfig getConfig() {
        return config;
    }
}
