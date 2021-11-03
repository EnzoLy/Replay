package live.ghostly.replay.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LocationUtils {

    public byte toAngle(float old) {
        return (byte) (old * 256F / 360F);
    }

    public int toFixedPointNumber(double old) {
        return (int) Math.round(old * 32D);
    }

}