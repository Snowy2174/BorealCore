package plugin.borealcore.functions.herbalism;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.borealcore.functions.cooking.Difficulty;
import plugin.borealcore.functions.cooking.object.Layout;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.utility.AdventureUtil;

public class InfusingPlayer extends BukkitRunnable {
    private final long deadline;
    private final Player player;
    private final Difficulty difficulty;
    private final HerbalismManager herbalismManager;
    private int progress;
    private int internalTimer;
    private final int size;
    private boolean face;
    private final String start;
    private final String bar;
    private final String pointer;
    private final String offset;
    private final String end;
    private final String pointerOffset;
    private final String title;
    private final double[] successRate;
    private final int range;


    public InfusingPlayer(long deadline, Player player, Layout layout, Difficulty difficulty, HerbalismManager herbalismManager) {
        this.deadline = deadline;
        this.player = player;
        this.difficulty = difficulty;
        this.herbalismManager = herbalismManager;
        this.size = layout.getSize();
        this.start = layout.getStart();
        this.bar = layout.getBar();
        this.pointer = layout.getPointer();
        this.offset = layout.getOffset();
        this.end = layout.getEnd();
        this.pointerOffset = layout.getPointerOffset();
        this.title = layout.getTitle();
        this.range = layout.getRange();
        this.successRate = layout.getSuccessRate();
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() > deadline) {
            AdventureUtil.playerMessage(player, MessageManager.tooSlow);
            //@TODO ingredient fail, not infuse cancel
            herbalismManager.removeInfusingPlayer(player);
            cancel();
            return;
        }
        if (internalTimer < difficulty.timer() - 1) {
            internalTimer++;
            return;
        } else {
            if (face) {
                progress += difficulty.speed();
            } else {
                progress -= difficulty.speed();
            }
            if (progress > size) {
                face = !face;
                progress = 2 * size - progress;
            } else if (progress < 0) {
                face = !face;
                progress = -progress;
            }
        }
        StringBuilder stringBuilder = new StringBuilder(start + bar + pointerOffset);
        for (int index = 0; index <= size; index++) {
            if (index == progress) {
                stringBuilder.append(pointer);
            } else {
                stringBuilder.append(offset);
            }
        }
        stringBuilder.append(end);
        AdventureUtil.playerTitle(player, title, stringBuilder.toString(), 0, 500, 0);
    }

    public boolean isSuccess() {
        int last = progress / range;
        return (Math.random() < successRate[last]);
    }

    public boolean isPerfect() {
        int last = progress / range;
        return (successRate[last] == 2);
    }
}
