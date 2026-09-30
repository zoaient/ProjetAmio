package eu.telecomnancy.amio.lightwatch.detection;

import eu.telecomnancy.amio.lightwatch.model.MoteReading;
import eu.telecomnancy.amio.lightwatch.detection.LightDetector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MoteStateStore {
    public enum LightState { UNKNOWN, ON, OFF }
    // Considérant qu'on n'ajoute pas de capteurs pendant l'execution du tout
    public class MoteState {
        private MoteReading reading;
        private LightState lightState;

        public MoteState(MoteReading reading, LightState lightState) {
            this.reading = Objects.requireNonNull(reading, "reading");
            this.lightState = Objects.requireNonNull(lightState, "lightState");
        }

        public MoteReading getReading() {
            return reading;
        }

        public LightState getLightState() {
            return lightState;
        }
    }

    private final List<MoteState> states = new ArrayList<>();


    public LightState getState(MoteReading reading) {
        for (MoteState state : states) {
            if (state.getReading().getMoteId().equals(reading.getMoteId())) {
                return state.getLightState();
            }
        }
        return LightState.UNKNOWN;
    }
    public void updateStates(List<MoteReading> Readings) {
        if(states.isEmpty()) {
            for(MoteReading reading : Readings) {
                states.add(new MoteState(reading, LightState.UNKNOWN));
            }
        }else{
        for(int i = 0; i < Readings.size(); i++) {
            MoteReading reading = Readings.get(i);
            MoteState state = states.get(i);
            MoteReading previousReading = state.getReading();
            if(LightDetector.isComparable(previousReading,reading)){
                if(LightDetector.isRise(previousReading,reading)){
                    state.lightState = LightState.ON;
                }else if(LightDetector.isDrop(previousReading,reading)){
                    state.lightState = LightState.OFF;
                }
            }
        }
        }
    }
}
