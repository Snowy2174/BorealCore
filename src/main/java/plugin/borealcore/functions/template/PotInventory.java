package plugin.borealcore.functions.template;

import net.kyori.adventure.text.Component;
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.core.block.CustomCropsBlock;
import net.momirealms.customcrops.api.core.block.PotBlock;
import net.momirealms.customcrops.api.core.mechanic.crop.CropConfig;
import net.momirealms.customcrops.api.core.mechanic.pot.PotConfig;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import net.momirealms.customcrops.api.core.world.Pos3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import plugin.borealcore.BorealCore;

import java.util.List;
import java.util.Optional;

import static plugin.borealcore.utility.InventoryUtil.buildia;

public class PotInventory implements InventoryHolder {

    private final CustomCropsBlockState customCropsBlockState;
    private final CropConfig cropConfig;
    private PotBlock potBlock;
    private final Location location;
    private final Inventory inventory;

    public PotInventory(@NotNull CustomCropsBlockState customCropsBlockState, @NotNull Location location, @NotNull CropConfig cropConfig) {
        this.customCropsBlockState = customCropsBlockState;
        this.inventory = BorealCore.getInstance().getServer().createInventory(this, 27);
        this.location = location;
        this.cropConfig = cropConfig;

        updateInventory();
    }

    private void updateInventory() {
        Optional<CustomCropsBlockState> blockState = BukkitCustomCropsPlugin.getInstance().getWorldManager().getWorld(Bukkit.getWorld("world")).get().getBlockState(Pos3.from(this.location.subtract(0, 1, 0)));
        BorealCore.getInstance().getLogger().info("blockState present: " + blockState.isPresent());
        if (blockState.isPresent()) {
            CustomCropsBlock block = blockState.get().type();
            BorealCore.getInstance().getLogger().info("block type: " + (block != null ? block.getClass().getName() : "null"));
            if (block instanceof PotBlock potBlock) {
                this.potBlock = potBlock;
                BorealCore.getInstance().getLogger().info("block is PotBlock");
                PotConfig potConfig = potBlock.config(blockState.get());
                inventory.setItem(9, generateItem());
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void handleClick(InventoryClickEvent event) {

    }

    public ItemStack generateItem() {
        ItemStack item = buildia("pot");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        int maxFertilizers = potBlock != null ? potBlock.config(customCropsBlockState).maxFertilizers() : 0;
        int waterStorage = potBlock != null ? potBlock.config(customCropsBlockState).storage() : 0;
        int currentWater = potBlock != null ? potBlock.water(customCropsBlockState) : 0;
        Object fertilizers = potBlock != null ? potBlock.fertilizers(customCropsBlockState) : List.of();

        meta.displayName(Component.text("§6Epic Pot"));

        meta.lore();

        item.setItemMeta(meta);
        return item;
    }
}
