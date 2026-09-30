package eu.telecomnancy.amio.lightwatch.detection;

import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import eu.telecomnancy.amio.lightwatch.model.MoteReading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoteStateStoreTest {

    @Test
    public void firstReadingOnlyInitializesSensor() {
        MoteStateStore store = new MoteStateStore();
        MoteReading first = reading("mote-1", 10, 0);

        MoteStateStore.Transitions transitions = store.processReadings(
                Collections.singletonList(first));

        assertTrue(transitions.getTurnedOn().isEmpty());
        assertTrue(transitions.getTurnedOff().isEmpty());
        assertEquals(MoteStateStore.LightState.UNKNOWN, store.getLightState("mote-1"));
        assertEquals(first, store.getLastReading("mote-1"));
    }

    @Test
    public void significantRiseAndDropUpdateStateAndTransitions() {
        MoteStateStore store = new MoteStateStore();
        store.processReadings(Collections.singletonList(reading("mote-1", 10, 0)));

        MoteStateStore.Transitions rise = store.processReadings(
                Collections.singletonList(reading("mote-1", 100, 100)));
        MoteStateStore.Transitions drop = store.processReadings(
                Collections.singletonList(reading("mote-1", 10, 200)));

        assertEquals(Collections.singleton("mote-1"), rise.getTurnedOn());
        assertTrue(rise.getTurnedOff().isEmpty());
        assertEquals(Collections.singleton("mote-1"), drop.getTurnedOff());
        assertTrue(drop.getTurnedOn().isEmpty());
        assertEquals(MoteStateStore.LightState.OFF, store.getLightState("mote-1"));
    }

    @Test
    public void missingSensorRetainsItsReadingAndState() {
        MoteStateStore store = new MoteStateStore();
        store.processReadings(Arrays.asList(
                reading("mote-1", 10, 0), reading("mote-2", 10, 0)));
        store.processReadings(Collections.singletonList(reading("mote-1", 100, 100)));

        store.processReadings(Collections.singletonList(reading("mote-2", 10, 200)));

        assertEquals(100, store.getLastReading("mote-1").getLuminosity(), 0);
        assertEquals(MoteStateStore.LightState.ON, store.getLightState("mote-1"));
    }

    @Test
    public void returnAfterLongGapRefreshesBaselineWithoutTransition() {
        MoteStateStore store = new MoteStateStore();
        store.processReadings(Arrays.asList(
                reading("mote-1", 10, 0), reading("mote-1", 100, 100)));

        MoteReading afterGap = reading("mote-1", 10, 1000);
        MoteStateStore.Transitions transitions = store.processReadings(
                Collections.singletonList(afterGap));

        assertTrue(transitions.getTurnedOn().isEmpty());
        assertTrue(transitions.getTurnedOff().isEmpty());
        assertEquals(MoteStateStore.LightState.ON, store.getLightState("mote-1"));
        assertEquals(afterGap, store.getLastReading("mote-1"));
    }


    @Test
    public void riseAboveAmplitudeTurnsLightOn() {
        MoteStateStore store = new MoteStateStore();

        store.processReadings(Collections.singletonList(reading("mote-1", 10, 0)));

        MoteStateStore.Transitions transitions = store.processReadings(
                Collections.singletonList(reading("mote-1", 200, 100)));

        assertEquals(Collections.singleton("mote-1"), transitions.getTurnedOn());
        assertTrue(transitions.getTurnedOff().isEmpty());
        assertEquals(MoteStateStore.LightState.ON, store.getLightState("mote-1"));
    }

    @Test
    public void smallRiseDoesNotTriggerAnyEvent() {
        MoteStateStore store = new MoteStateStore();

        store.processReadings(Collections.singletonList(reading("mote-1", 100, 0)));

        MoteStateStore.Transitions transitions = store.processReadings(
                Collections.singletonList(reading("mote-1", 101, 100)));

        assertTrue(transitions.getTurnedOn().isEmpty());
        assertTrue(transitions.getTurnedOff().isEmpty());
        assertEquals(MoteStateStore.LightState.UNKNOWN, store.getLightState("mote-1"));
    }

    @Test
    public void dropAboveAmplitudeTurnsLightOff() {
        MoteStateStore store = new MoteStateStore();

        store.processReadings(Collections.singletonList(reading("mote-1", 100, 0)));

        MoteStateStore.Transitions transitions = store.processReadings(
                Collections.singletonList(reading("mote-1", 0, 100)));

        assertTrue(transitions.getTurnedOn().isEmpty());
        assertEquals(Collections.singleton("mote-1"), transitions.getTurnedOff());
        assertEquals(MoteStateStore.LightState.OFF, store.getLightState("mote-1"));
    }

    @Test
    public void firstReadingIsolatedDoesNotTriggerEventAndLeavesStateUnknown() {
        MoteStateStore store = new MoteStateStore();

        MoteStateStore.Transitions transitions = store.processReadings(
                Collections.singletonList(reading("mote-1", 42, 0)));

        assertTrue(transitions.getTurnedOn().isEmpty());
        assertTrue(transitions.getTurnedOff().isEmpty());
        assertEquals(MoteStateStore.LightState.UNKNOWN, store.getLightState("mote-1"));
    }

    @Test
    public void largeRiseAfterSeveralHoursDoesNotTriggerEvent() {
        MoteStateStore store = new MoteStateStore();

        store.processReadings(Collections.singletonList(reading("mote-1", 10, 0)));

        long severalHoursInMillis = 5L * 60L * 60L * 1000L;
        MoteStateStore.Transitions transitions = store.processReadings(
                Collections.singletonList(reading("mote-1", 200, severalHoursInMillis)));

        assertTrue(transitions.getTurnedOn().isEmpty());
        assertTrue(transitions.getTurnedOff().isEmpty());
        assertEquals(MoteStateStore.LightState.UNKNOWN, store.getLightState("mote-1"));
    }

    @Test
    public void outOfOrderReadingsDoNotTriggerTransition() {
        MoteStateStore store = new MoteStateStore();

        store.processReadings(Collections.singletonList(reading("mote-1", 20, 0)));

        MoteStateStore.Transitions transitions = store.processReadings(Arrays.asList(
                reading("mote-1", 150, 2000),
                reading("mote-1", 40, 1000)
        ));

        assertTrue(transitions.getTurnedOn().isEmpty());
        assertTrue(transitions.getTurnedOff().isEmpty());
        assertEquals(MoteStateStore.LightState.UNKNOWN, store.getLightState("mote-1"));
    }
    private static MoteReading reading(String moteId, float luminosity, long millis) {
        return new MoteReading(moteId, luminosity, Instant.ofEpochMilli(millis));
    }
}