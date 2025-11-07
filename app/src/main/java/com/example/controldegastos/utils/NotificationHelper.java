package com.example.controldegastos.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.controldegastos.R;
import com.example.controldegastos.activities.AgregarTransaccionActivity;
import com.example.controldegastos.activities.MainActivity;
import com.example.controldegastos.database.DatabaseHelper;

import java.text.NumberFormat;
import java.util.Locale;

public class NotificationHelper {

    private static final String CHANNEL_ID = "recordatorio_gastos";
    private static final String CHANNEL_NAME = "Recordatorios de Gastos";
    private static final String CHANNEL_DESC = "Recordatorios y resúmenes de tus gastos diarios";
    private static final int NOTIFICATION_ID = 1001;

    private Context context;
    private NotificationManagerCompat notificationManager;
    private DatabaseHelper dbHelper;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.dbHelper = new DatabaseHelper(context);
        crearCanalNotificacion();
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // Mostrar notificación de recordatorio (con resumen dinámico)
    public void mostrarNotificacionRecordatorio() {
        // Obtener datos del día
        int cantidadTransacciones = dbHelper.obtenerCantidadTransaccionesHoy();
        double totalGastado = dbHelper.obtenerTotalGastadoHoy();
        double totalIngresos = dbHelper.obtenerTotalIngresosHoy();
        double promedioGasto = dbHelper.obtenerPromedioGastoDiarioUltimos30Dias();

        // Formatear moneda
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        // Determinar tipo de notificación según contexto
        String titulo, mensaje, mensajeLargo;
        String emoji;

        if (cantidadTransacciones == 0) {
            // 📊 SIN TRANSACCIONES HOY
            emoji = "💰";
            titulo = emoji + " ¡Registra tus gastos!";
            mensaje = "No has registrado nada hoy. ¿Tuviste gastos?";
            mensajeLargo = "No has registrado transacciones hoy. Si tuviste gastos o ingresos, " +
                    "regístralos ahora para mantener el control de tus finanzas. ¡Solo toma unos segundos!";

        } else if (totalGastado == 0 && totalIngresos > 0) {
            // 🎉 SOLO INGRESOS (¡DÍA PERFECTO!)
            emoji = "🎉";
            titulo = emoji + " ¡Excelente día!";
            mensaje = "Hoy: " + cantidadTransacciones + " ingresos, ¡0 gastos!";
            mensajeLargo = "¡Felicidades! Hoy solo registraste ingresos por " +
                    formatoMoneda.format(totalIngresos) + " y ningún gasto. " +
                    "¡Sigue así! ¿Falta algo por registrar?";

        } else if (totalGastado > 0 && totalIngresos == 0) {
            // 📊 SOLO GASTOS
            if (totalGastado > promedioGasto * 1.5) {
                // ⚠️ GASTOS ALTOS
                emoji = "⚠️";
                titulo = emoji + " Gastos por encima del promedio";
                mensaje = "Hoy: " + formatoMoneda.format(totalGastado) + " en " + cantidadTransacciones + " transacciones";
                mensajeLargo = "Hoy gastaste " + formatoMoneda.format(totalGastado) +
                        " en " + cantidadTransacciones + " transacciones. " +
                        "Tu promedio diario es " + formatoMoneda.format(promedioGasto) +
                        ". ¿Falta algo por registrar?";
            } else {
                // 📊 GASTOS NORMALES
                emoji = "📊";
                titulo = emoji + " Resumen del día";
                mensaje = "Hoy: " + cantidadTransacciones + " transacciones, " + formatoMoneda.format(totalGastado);
                mensajeLargo = "Hoy registraste " + cantidadTransacciones + " transacciones " +
                        "por un total de " + formatoMoneda.format(totalGastado) + ". " +
                        "¿Falta algo por registrar?";
            }

        } else {
            // 💵 INGRESOS Y GASTOS
            double balance = totalIngresos - totalGastado;
            emoji = balance >= 0 ? "💚" : "📊";
            titulo = emoji + " Resumen del día";
            mensaje = "Ingresos: " + formatoMoneda.format(totalIngresos) +
                    " | Gastos: " + formatoMoneda.format(totalGastado);
            mensajeLargo = "Hoy tuviste:\n" +
                    "↗️ Ingresos: " + formatoMoneda.format(totalIngresos) + "\n" +
                    "↘️ Gastos: " + formatoMoneda.format(totalGastado) + "\n" +
                    "💵 Balance: " + formatoMoneda.format(balance) + "\n" +
                    "¿Falta algo por registrar?";
        }

        // Intents
        Intent intentMain = new Intent(context, MainActivity.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(
                context, 0, intentMain, PendingIntent.FLAG_IMMUTABLE
        );

        Intent intentAgregar = new Intent(context, AgregarTransaccionActivity.class);
        intentAgregar.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntentAgregar = PendingIntent.getActivity(
                context, 1, intentAgregar, PendingIntent.FLAG_IMMUTABLE
        );

        // Construir notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensajeLargo))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntentMain)
                .setAutoCancel(true)
                .addAction(
                        android.R.drawable.ic_input_add,
                        "Agregar",
                        pendingIntentAgregar
                );

        // Mostrar notificación
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static boolean tienePermisosNotificacion(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
        return true;
    }
}