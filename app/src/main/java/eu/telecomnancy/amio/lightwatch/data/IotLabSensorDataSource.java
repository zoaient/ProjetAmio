package eu.telecomnancy.amio.lightwatch.data;

import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import eu.telecomnancy.amio.lightwatch.model.MoteReading;

public class IotLabSensorDataSource implements SensorDataSource {
    private static final String ENDPOINT =
            "http://iotlab.telecomnancy.eu:8080/iotlab/rest/data/1/light1/last";
    private static final String TAG = "IotLabSensorDataSource";

    public List<MoteReading> parse(InputStream in) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        for (int ch; (ch = in.read()) != -1; ) {
            sb.append((char) ch);
        }
        JSONObject rootObject = new JSONObject(sb.toString());
        List<MoteReading> readings = new ArrayList<>();

        if (rootObject != null && rootObject.has("data") && !rootObject.isNull("data")) {
            JSONArray dataArray = rootObject.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.getJSONObject(i);
                String moteId = item.getString("mote");
                double luminosity = item.getDouble("value");
                long timestampMs = item.getLong("timestamp");
                Instant timestamp = Instant.ofEpochMilli(timestampMs);
                readings.add(new MoteReading(moteId, (float)luminosity, timestamp));
            }
        }
        Log.d(TAG,readings.toString());
        return readings;
    }
    @Override
    public List<MoteReading> fetchLatest() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new
                URL(ENDPOINT).openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(5_000);
            connection.setRequestProperty("Accept", "application/json");
            int status = connection.getResponseCode();
            Log.d(TAG, "Code HTTP : " + status);
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Réponse inattendue du serveur : " + status);
                //Marche po
            }
            try (InputStream in = connection.getInputStream()) {
                return parse(in);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } finally {
            connection.disconnect(); // ne jamais oublier de libérer la connexion
        }
    }

}
