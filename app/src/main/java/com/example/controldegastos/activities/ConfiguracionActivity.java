package com.example.controldegastos.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.controldegastos.R;
import com.example.controldegastos.utils.NotificationHelper;
import com.example.controldegastos.utils.ReminderWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ConfiguracionActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ConfiguracionPrefs";
    private static final String KEY_RECORDATORIOS_ACTIVOS = "recordatorios_activos";
    private static final String KEY_HORA_RECORDATORIO = "hora_recordatorio";
    private static final String KEY_MINUTO_RECORDATORIO = "minuto_recordatorio";
    private static final String WORK_TAG = "recordatorio_diario";

    private SwitchCompat switchRecordatorios;
    private TimePicker timePicker;
    private Button btnGuardar, btnProbar;
    private SharedPreferences prefs;

    // Launcher para solicitar permisos
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "✅ Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
                    activarRecordatorios();
                } else {
                    Toast.makeText(this, "❌ Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
                    switchRecordatorios.setChecked(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("⚙️ Configuración");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar vistas
        inicializarVistas();

        // Inicializar SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Cargar configuración guardada
        cargarConfiguracion();

        // Listeners
        configurarListeners();
    }

    private void inicializarVistas() {
        switchRecordatorios = findViewById(R.id.switchRecordatorios);
        timePicker = findViewById(R.id.timePicker);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnProbar = findViewById(R.id.btnProbar);

        // Configurar TimePicker en formato 24 horas
        timePicker.setIs24HourView(true);
    }

    private void cargarConfiguracion() {
        // Cargar estado del switch
        boolean recordatoriosActivos = prefs.getBoolean(KEY_RECORDATORIOS_ACTIVOS, false);
        switchRecordatorios.setChecked(recordatoriosActivos);

        // Cargar hora guardada (por defecto 20:00)
        int hora = prefs.getInt(KEY_HORA_RECORDATORIO, 20);
        int minuto = prefs.getInt(KEY_MINUTO_RECORDATORIO, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hora);
            timePicker.setMinute(minuto);
        } else {
            timePicker.setCurrentHour(hora);
            timePicker.setCurrentMinute(minuto);
        }
    }

    private void configurarListeners() {
        // Switch de recordatorios
        switchRecordatorios.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Verificar permisos antes de activar
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Solicitar permiso
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                        return;
                    }
                }
                activarRecordatorios();
            } else {
                desactivarRecordatorios();
            }
        });

        // Botón Guardar
        btnGuardar.setOnClickListener(v -> guardarConfiguracion());

        // Botón Probar Notificación
        btnProbar.setOnClickListener(v -> probarNotificacion());
    }

    private void guardarConfiguracion() {
        // Obtener hora seleccionada
        int hora, minuto;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hora = timePicker.getHour();
            minuto = timePicker.getMinute();
        } else {
            hora = timePicker.getCurrentHour();
            minuto = timePicker.getCurrentMinute();
        }

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_HORA_RECORDATORIO, hora);
        editor.putInt(KEY_MINUTO_RECORDATORIO, minuto);
        editor.putBoolean(KEY_RECORDATORIOS_ACTIVOS, switchRecordatorios.isChecked());
        editor.apply();

        // Si los recordatorios están activos, reprogramar con nueva hora
        if (switchRecordatorios.isChecked()) {
            programarRecordatorio();
        }

        Toast.makeText(this, "✅ Configuración guardada", Toast.LENGTH_SHORT).show();
    }

    private void activarRecordatorios() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_RECORDATORIOS_ACTIVOS, true);
        editor.apply();

        programarRecordatorio();
        Toast.makeText(this, "🔔 Recordatorios activados", Toast.LENGTH_SHORT).show();
    }

    private void desactivarRecordatorios() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_RECORDATORIOS_ACTIVOS, false);
        editor.apply();

        // Cancelar trabajo programado
        WorkManager.getInstance(this).cancelAllWorkByTag(WORK_TAG);
        Toast.makeText(this, "🔕 Recordatorios desactivados", Toast.LENGTH_SHORT).show();
    }

    private void programarRecordatorio() {
        // Calcular delay hasta la próxima hora programada
        int hora = prefs.getInt(KEY_HORA_RECORDATORIO, 20);
        int minuto = prefs.getInt(KEY_MINUTO_RECORDATORIO, 0);

        Calendar ahora = Calendar.getInstance();
        Calendar siguienteRecordatorio = Calendar.getInstance();
        siguienteRecordatorio.set(Calendar.HOUR_OF_DAY, hora);
        siguienteRecordatorio.set(Calendar.MINUTE, minuto);
        siguienteRecordatorio.set(Calendar.SECOND, 0);

        // Si la hora ya pasó hoy, programar para mañana
        if (siguienteRecordatorio.before(ahora)) {
            siguienteRecordatorio.add(Calendar.DAY_OF_MONTH, 1);
        }

        long delayEnMilisegundos = siguienteRecordatorio.getTimeInMillis() - ahora.getTimeInMillis();

        // Crear trabajo periódico (cada 24 horas)
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                ReminderWorker.class,
                24, // Repetir cada 24 horas
                TimeUnit.HOURS,
                15, // Ventana de flexibilidad de 15 minutos
                TimeUnit.MINUTES
        )
                .setInitialDelay(delayEnMilisegundos, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build();

        // Programar el trabajo
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }

    private void probarNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "⚠️ Necesitas conceder permisos de notificaciones primero", Toast.LENGTH_SHORT).show();
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }

        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.mostrarNotificacionRecordatorio();
        Toast.makeText(this, "📬 Notificación de prueba enviada", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}