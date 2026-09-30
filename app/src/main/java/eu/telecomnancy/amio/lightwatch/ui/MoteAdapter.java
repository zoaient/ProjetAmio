package eu.telecomnancy.amio.lightwatch.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import eu.telecomnancy.amio.lightwatch.R;
import eu.telecomnancy.amio.lightwatch.detection.MoteStateStore;
import eu.telecomnancy.amio.lightwatch.model.MoteReading;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptateur de la liste des capteurs.
 *
 * <p>Deux responsabilités, comme tout adaptateur de {@link RecyclerView} :
 * créer les vues d'une ligne ({@code onCreateViewHolder}) et remplir une ligne
 * existante à partir d'une donnée ({@code onBindViewHolder}).</p>
 */
public class MoteAdapter extends RecyclerView.Adapter<MoteAdapter.MoteViewHolder> {

    /**
     * Seuil provisoire de détection, choisi arbitrairement au T.P. 1.
     *
     * <p>TODO T.P. 2 : le calibrer sur de vraies mesures et le déplacer dans
     * {@code LightDetector}. TODO T.P. 4 : le rendre configurable.</p>
     */
    public static final float PROVISIONAL_THRESHOLD = 200f;

    private final List<MoteReading> readings = new ArrayList<>();
    private final MoteStateStore store = new MoteStateStore();
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

    /** Remplace le contenu de la liste. */
    public void submitList(List<MoteReading> newReadings) {
        readings.clear();
        readings.addAll(newReadings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // false : c'est le RecyclerView qui rattachera la vue, pas nous.
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mote, parent, false);
        return new MoteViewHolder(itemView);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull MoteViewHolder holder, int position) {
        MoteReading reading = readings.get(position);
        Context context = holder.itemView.getContext();
        MoteStateStore.LightState state = store.getState(reading);
        int stateColor;
        int stateText = 0;


        if(state== MoteStateStore.LightState.ON) {
            stateColor = R.color.light_on;
        } else if (state == MoteStateStore.LightState.OFF) {
            stateColor = R.color.light_off;
        } else {
            stateColor = R.color.light_unknown;
        }

        holder.moteId.setText(reading.getMoteId());
        holder.moteLuminosity.setText(
                context.getString(R.string.luminosity_format, reading.getLuminosity()));
        holder.moteTime.setText(
                timeFormatter.format(reading.getTimestamp().atZone(ZoneId.systemDefault())));


        holder.moteState.setText(stateText);
        holder.indicator.setBackgroundTintList(ColorStateList.valueOf(stateColor));
        holder.indicator.setContentDescription(context.getString(stateText));
    }

    @Override
    public int getItemCount() {
        return readings.size();
    }

    /**
     * Mémorise les vues d'une ligne pour éviter de les rechercher à chaque défilement.
     *
     * <p>Les recherches partent d'{@code itemView} et non de l'activité : chaque ligne
     * possède ses propres vues, qui portent les mêmes identifiants.</p>
     */
    public static class MoteViewHolder extends RecyclerView.ViewHolder {

        final View indicator;
        final TextView moteId;
        final TextView moteState;
        final TextView moteLuminosity;
        final TextView moteTime;

        MoteViewHolder(View itemView) {
            super(itemView);
            indicator = itemView.findViewById(R.id.indicator);
            moteId = itemView.findViewById(R.id.moteId);
            moteState = itemView.findViewById(R.id.moteState);
            moteLuminosity = itemView.findViewById(R.id.moteLuminosity);
            moteTime = itemView.findViewById(R.id.moteTime);
        }
    }
}
