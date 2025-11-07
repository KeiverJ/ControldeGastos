package com.example.controldegastos.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Mostrar notificación
        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
        notificationHelper.mostrarNotificacionRecordatorio();

        return Result.success();
    }
}