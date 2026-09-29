package eu.telecomnancy.amio.lightwatch.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Un relevé de luminosité effectué par un capteur (« mote ») du réseau.
 *
 * <p>Classe immuable : une fois construit, un relevé ne change plus. Cela évite
 * toute une famille de bogues lorsque le même objet est manipulé par le thread
 * de l'interface et par une tâche de fond.</p>
 */
public final class MoteReading {

    private final String moteId;
    private final float luminosity;
    private final Instant timestamp;

    public MoteReading(String moteId, float luminosity, Instant timestamp) {
        this.moteId = Objects.requireNonNull(moteId, "moteId");
        this.luminosity = luminosity;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
    }

    public String getMoteId() {
        return moteId;
    }

    public float getLuminosity() {
        return luminosity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "MoteReading{" + moteId + ", " + luminosity + ", " + timestamp + '}';
    }
}
