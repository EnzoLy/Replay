package live.ghostly.replay.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class MovementUtils {

    public static boolean differentLocation(Location one, Location two) {
        return (hasMoved(one, two) && hasRotated(one, two));
    }

    public static boolean hasMoved(Vector move) {
        return (move.getX() > 0
            || move.getY() > 0
            || move.getZ() > 0);
    }

    public static boolean hasMoved(Location one, Location two) {
        return (one.getX() != two.getX()
            || one.getY() != two.getY()
            || one.getZ() != two.getZ());
    }

    public static boolean hasRotated(Location one, Location two) {
        return (one.getYaw() != two.getYaw()
            || one.getPitch() != two.getPitch());
    }

}