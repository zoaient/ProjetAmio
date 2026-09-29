package eu.telecomnancy.amio.lightwatch.data;

import eu.telecomnancy.amio.lightwatch.model.MoteReading;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Source de données simulée, utilisée au T.P. 1 et conservée ensuite pour
 * développer sans réseau et pour les tests unitaires.
 *
 * <p>Les identifiants de capteurs sont stables d'un appel à l'autre, alors que les
 * valeurs de luminosité varient : la moitié environ correspond à une pièce dans le
 * noir, l'autre à une lumière allumée.</p>
 */
public class FakeSensorDataSource implements SensorDataSource {

    /** Latence simulée, pour vérifier que l'interface ne se figera pas. */
    private static final long SIMULATED_LATENCY_MS = 500L;

    private static final List<String> MOTE_IDS = Collections.unmodifiableList(
            java.util.Arrays.asList(
                    "mote-01", "mote-02", "mote-03", "mote-04",
                    "mote-05", "mote-06", "mote-07", "mote-08"));

    private final Random random = new Random();

    @Override
    public List<MoteReading> fetchLatest() throws IOException {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Relevé interrompu", e);
        }

        Instant now = Instant.now();
        List<MoteReading> readings = new ArrayList<>(MOTE_IDS.size());
        for (String moteId : MOTE_IDS) {
            readings.add(new MoteReading(moteId, randomLuminosity(), now));
        }
        return readings;
    }

    private float randomLuminosity() {
        return random.nextBoolean()
                ? 5f + random.nextInt(60)      // pièce dans le noir
                : 250f + random.nextInt(600);  // lumière allumée
    }
}
