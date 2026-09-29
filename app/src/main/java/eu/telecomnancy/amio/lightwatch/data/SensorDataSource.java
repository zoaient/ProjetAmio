package eu.telecomnancy.amio.lightwatch.data;

import eu.telecomnancy.amio.lightwatch.model.MoteReading;

import java.io.IOException;
import java.util.List;

/**
 * Contrat d'accès aux relevés des capteurs.
 *
 * <p>L'interface graphique ne connaît que cette interface : au T.P. 2, il suffira
 * de remplacer {@link FakeSensorDataSource} par une implémentation interrogeant
 * le web service IoT Lab, sans toucher à l'activité.</p>
 */
public interface SensorDataSource {

    /**
     * Renvoie le dernier relevé connu de chaque capteur.
     *
     * <p><strong>Appelé hors du thread principal :</strong> cette méthode peut être
     * bloquante (attente réseau).</p>
     *
     * @throws IOException si les relevés n'ont pas pu être obtenus
     */
    List<MoteReading> fetchLatest() throws IOException;
}
