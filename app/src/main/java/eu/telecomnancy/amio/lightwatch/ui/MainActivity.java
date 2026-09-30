package eu.telecomnancy.amio.lightwatch.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import eu.telecomnancy.amio.lightwatch.R;
import eu.telecomnancy.amio.lightwatch.data.FakeSensorDataSource;
import eu.telecomnancy.amio.lightwatch.data.IotLabSensorDataSource;
import eu.telecomnancy.amio.lightwatch.data.SensorDataSource;
import eu.telecomnancy.amio.lightwatch.detection.MoteStateStore;
import eu.telecomnancy.amio.lightwatch.model.MoteReading;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Écran principal : carte d'état, synthèse et liste des capteurs.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Les vues sont récupérées une seule fois dans onCreate() et conservées ici.
    // Appeler findViewById() à chaque usage parcourrait l'arbre de vues à chaque fois.
    private View coordinator;
    private TextView statusValue;
    private TextView lastUpdateValue;
    private TextView lastAlertValue;
    private TextView emptyView;
    private SwitchMaterial monitoringSwitch;
    private Chip chipSensors;
    private Chip chipLightsOn;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;

    private MoteAdapter adapter;
    private SensorDataSource dataSource;

    /** Exécuteur des appels bloquants (ici la source simulée, le réseau au T.P. 2). */
    private ExecutorService io;

    private MoteStateStore store;

    /** Permet de revenir sur le thread de l'interface depuis une tâche de fond. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Création de l'activité");

        // Gonfle le gabarit et l'installe dans la fenêtre de l'activité.
        // Tout findViewById() doit venir APRÈS cet appel.
        setContentView(R.layout.activity_main);

        coordinator = findViewById(R.id.coordinator);
        statusValue = findViewById(R.id.statusValue);
        lastUpdateValue = findViewById(R.id.lastUpdateValue);
        lastAlertValue = findViewById(R.id.lastAlertValue);
        emptyView = findViewById(R.id.emptyView);
        monitoringSwitch = findViewById(R.id.monitoringSwitch);
        chipSensors = findViewById(R.id.chipSensors);
        chipLightsOn = findViewById(R.id.chipLightsOn);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        store =  new MoteStateStore();

        // Le thème hérite de NoActionBar : c'est notre MaterialToolbar qui tient
        // ce rôle. Sans cet appel, le menu d'options n'aurait nulle part où s'afficher.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        io = Executors.newSingleThreadExecutor();

        // TODO T.P. 2 : remplacer par IotLabSensorDataSource.
        dataSource = new IotLabSensorDataSource();

        adapter = new MoteAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::refresh);
        monitoringSwitch.setOnCheckedChangeListener(
                (button, isChecked) -> onMonitoringToggled(isChecked));

        statusValue.setText(R.string.status_stopped);
        lastUpdateValue.setText(R.string.value_none);
        lastAlertValue.setText(R.string.value_none);
        updateSummary(Collections.emptyList());

        refresh();
    }

    @Override
    protected void onDestroy() {
        // Symétrie du cycle de vie : ce qui est créé dans onCreate est libéré ici.
        mainHandler.removeCallbacksAndMessages(null);
        io.shutdownNow();
        super.onDestroy();
    }

    // ------------------------------------------------------------------ menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // TODO T.P. 4 : startActivity(new Intent(this, SettingsActivity.class));
            Snackbar.make(coordinator, R.string.settings_not_available,
                    Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ------------------------------------------------------------ surveillance

    /**
     * Réaction à l'interrupteur de surveillance.
     *
     * <p>TODO T.P. 3 : démarrer et arrêter le service de premier plan
     * {@code MonitoringService} au lieu de se contenter de l'affichage.</p>
     */
    private void onMonitoringToggled(boolean enabled) {
        Log.d(TAG, "Surveillance " + (enabled ? "activée" : "désactivée"));
        statusValue.setText(enabled ? R.string.status_running : R.string.status_stopped);
    }

    // --------------------------------------------------------------- relevés

    private void refresh() {
        swipeRefresh.setRefreshing(true);
        io.execute(() -> {
            try {
                List<MoteReading> readings = dataSource.fetchLatest();
                mainHandler.post(() -> onReadings(readings));
            } catch (Exception e) {
                Log.w(TAG, "Échec du relevé", e);
                mainHandler.post(this::onReadingsFailed);
            }
        });
    }

    private void onReadings(List<MoteReading> readings) {
        List<MoteReading> sorted = new ArrayList<>(readings);
        // Les capteurs les plus lumineux — donc les plus suspects — en premier.
        // Le témoin de type <MoteReading> est nécessaire : en position de receveur,
        // le compilateur ne peut pas l'inférer depuis la seule référence de méthode.
        sorted.sort(Comparator.<MoteReading>comparingDouble(
                MoteReading::getLuminosity).reversed());

        adapter.submitList(sorted);
        updateSummary(sorted);

        lastUpdateValue.setText(timeFormatter.format(LocalTime.now()));
        emptyView.setVisibility(sorted.isEmpty() ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(false);
        store.updateStates(readings);
    }

    private void onReadingsFailed() {
        swipeRefresh.setRefreshing(false);
        // Les données déjà affichées sont conservées : un échec ne doit pas vider l'écran.
        Snackbar.make(coordinator, R.string.error_fetch_failed, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_retry, v -> refresh())
                .show();
    }

    private void updateSummary(List<MoteReading> readings) {
        int lightsOn = 0;
        for (MoteReading reading : readings) {
            if (store.getState(reading) == MoteStateStore.LightState.ON) {
                lightsOn++;
            }
        }
        chipSensors.setText(getResources()
                .getQuantityString(R.plurals.summary_sensors, readings.size(), readings.size()));
        chipLightsOn.setText(getResources()
                .getQuantityString(R.plurals.summary_lights_on, lightsOn, lightsOn));
    }
}
