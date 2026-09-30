package eu.telecomnancy.amio.lightwatch.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import eu.telecomnancy.amio.lightwatch.R;
import eu.telecomnancy.amio.lightwatch.ui.MainActivity;

import java.util.Collection;

/**
 * Création des canaux et fabrication des notifications.
 *
 * <p>Depuis Android 8 (API 26), <strong>toute notification doit appartenir à un
 * canal</strong>. Une notification émise sur un canal inexistant n'apparaît pas, et
 * ce sans message d'erreur explicite : c'est une cause de perte de temps classique.
 * Le canal porte le niveau d'importance, que l'utilisateur peut ensuite ajuster
 * indépendamment pour chaque canal depuis les réglages du système.</p>
 */
public final class Notifications {

    /** Notification persistante du service de premier plan : discrète. */
    public static final String CHANNEL_MONITORING = "channel_monitoring";

    /** Alertes de détection : doivent se faire remarquer. */
    public static final String CHANNEL_ALERTS = "channel_alerts";

    /** Identifiant de la notification permanente du service. */
    public static final int ID_ONGOING = 1;

    /**
     * Identifiant des alertes. Volontairement <strong>stable</strong> : la nouvelle
     * alerte remplace la précédente au lieu de s'empiler.
     */
    public static final int ID_ALERT = 2;

    private static final int REQ_OPEN_APP = 0;

    private Notifications() {
        // Classe utilitaire : pas d'instance.
    }

    /**
     * Crée les canaux. Idempotent : recréer un canal existant est sans effet, il est
     * donc inoffensif d'appeler cette méthode à chaque démarrage du service.
     *
     * <p>Attention : le nom et l'importance d'un canal <strong>ne peuvent plus être
     * modifiés après sa création</strong>. En cas d'erreur pendant la mise au point,
     * désinstaller l'application pour repartir de zéro.</p>
     */
    public static void createChannels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        NotificationChannel monitoring = new NotificationChannel(
                CHANNEL_MONITORING,
                context.getString(R.string.channel_monitoring_name),
                // IMPORTANCE_LOW : ni son ni vibration. La notification permanente
                // du service est purement informative, elle ne doit pas déranger.
                NotificationManager.IMPORTANCE_LOW);
        monitoring.setDescription(context.getString(R.string.channel_monitoring_desc));

        NotificationChannel alerts = new NotificationChannel(
                CHANNEL_ALERTS,
                context.getString(R.string.channel_alerts_name),
                NotificationManager.IMPORTANCE_HIGH);
        alerts.setDescription(context.getString(R.string.channel_alerts_desc));

        manager.createNotificationChannel(monitoring);
        manager.createNotificationChannel(alerts);
    }

    /** Notification permanente exigée par un service de premier plan. */
    public static Notification buildOngoing(Context context) {
        return new NotificationCompat.Builder(context, CHANNEL_MONITORING)
                // setSmallIcon est obligatoire : sans icône, la notification est rejetée.
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notif_ongoing_title))
                .setContentText(context.getString(R.string.notif_ongoing_text))
                .setContentIntent(openAppIntent(context))
                .setOngoing(true)          // non balayable par l'utilisateur
                .setShowWhen(false)
                .build();
    }

    /**
     * Alerte d'allumage.
     *
     * <p>Une <strong>seule</strong> notification récapitulative même si plusieurs
     * capteurs s'allument dans le même relevé : un utilisateur noyé sous les
     * notifications désactive le canal.</p>
     */
    public static Notification buildAlert(Context context, Collection<String> moteIds) {
        int count = moteIds.size();
        String joined = String.join(", ", moteIds);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ALERTS)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(context.getString(R.string.notif_alert_title))
                        .setContentText(context.getResources().getQuantityString(
                                R.plurals.notif_alert_text, count, count, joined))
                        .setContentIntent(openAppIntent(context))
                        .setAutoCancel(true)   // disparaît quand on appuie dessus
                        .setCategory(NotificationCompat.CATEGORY_ALARM);

        if (count > 1) {
            // InboxStyle : liste dépliable, un capteur par ligne.
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                    .setBigContentTitle(context.getString(R.string.notif_alert_title));
            for (String moteId : moteIds) {
                style.addLine(moteId);
            }
            builder.setStyle(style);
        }

        // TODO T.P. 4 : ajouter une action « Envoyer un courriel » portée par un
        // PendingIntent, seule façon d'ouvrir le client de messagerie depuis
        // l'arrière-plan sur Android 10.
        return builder.build();
    }

    /** {@link PendingIntent} ramenant à l'écran principal. */
    private static PendingIntent openAppIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        // Si l'activité est déjà lancée, on la ramène au premier plan au lieu d'en
        // empiler une seconde instance.
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // FLAG_IMMUTABLE déclare que le destinataire ne peut pas modifier le contenu
        // de l'intention. Pas encore obligatoire sur l'API 29, mais c'est le
        // comportement souhaitable, et ce l'est devenu depuis.
        return PendingIntent.getActivity(context, REQ_OPEN_APP, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
