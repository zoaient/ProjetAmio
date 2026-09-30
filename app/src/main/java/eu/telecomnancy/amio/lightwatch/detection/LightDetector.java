package eu.telecomnancy.amio.lightwatch.detection;

import java.time.Duration;

import eu.telecomnancy.amio.lightwatch.model.MoteReading;

public class LightDetector {
    private static final float riseThreshold = 70; // amplitude minimale, en lux
    private static final Duration maxGap = Duration.ofMillis(500); // écart maximal entre deux relevés
    /** true si les deux relevés sont assez proches dans le temps pour être
     comparés. */
    public static boolean isComparable(MoteReading previous, MoteReading current){
        return Duration.between(previous.getTimestamp(), current.getTimestamp()).compareTo(maxGap)==-1;
    }
    /** true si la hausse entre les deux relevés traduit un allumage. */
    public static boolean isRise(MoteReading previous, MoteReading current) {
        float previousLuminosity= previous.getLuminosity();
        float currentLuminosity= current.getLuminosity();
        return currentLuminosity - previousLuminosity >= riseThreshold;

    }
    /** true si la baisse entre les deux relevés traduit une extinction. */
    public static boolean isDrop(MoteReading previous, MoteReading current) {
        float previousLuminosity= previous.getLuminosity();
        float currentLuminosity= current.getLuminosity();
        return currentLuminosity - previousLuminosity <= -riseThreshold;
    }
}
