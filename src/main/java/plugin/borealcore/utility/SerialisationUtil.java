package plugin.borealcore.utility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import plugin.borealcore.BorealCore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;

public class SerialisationUtil {

    public static String serializeLocation(Location location) {
        return "world" + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ();
    }

    public static String serializeItems(List<ItemStack> items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(items.size());
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }


    public static Location deserializeLocation(String locationString) {
        String[] location = locationString.split(":");
        return new Location(BorealCore.getInstance().getServer().getWorld(location[0]), parseDouble(location[1]), parseDouble(location[2]), parseDouble(location[3]));
    }

    public static List<ItemStack> deserializeItems(String itemsString) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(itemsString));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int size = dataInput.readInt();
            List<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                itemStacks.add((ItemStack) dataInput.readObject());
            }
            dataInput.close();

            if (itemStacks.size() == 0) {
                itemStacks.add(new ItemStack(Material.AIR));
            }

            return itemStacks;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load item stacks.", e);
        }
    }
}
