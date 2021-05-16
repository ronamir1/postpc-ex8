package huji.postpc.y2021.multicalculator;

import android.content.Context;
import android.content.Intent;
import android.telecom.Call;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;


import java.io.IOException;

import javax.security.auth.callback.Callback;

import static java.util.jar.Pack200.Unpacker.PROGRESS;

public class CalculationWorker extends Worker {
    public static final int MAX_SEARCH_TIME = 600000;
    private int cur_progress = 0;
    public CalculationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        setProgressAsync(new Data.Builder().putInt(PROGRESS, 0).build());
    }



    @NonNull
    @Override
    public Result doWork() {
        Data.Builder dataBuilder = new Data.Builder();
        long timeStartMs = System.currentTimeMillis();
        int id = getInputData().getInt("id", -1);
        double root = getInputData().getDouble("root", 0);
        double current = getInputData().getDouble("current", 2);

        while (current < root){
            if(System.currentTimeMillis() - timeStartMs >= MAX_SEARCH_TIME){
                dataBuilder.putBoolean("retry", true);
                dataBuilder.putDouble("root", root);
                dataBuilder.putInt("id", id);
                dataBuilder.putDouble("current", current);
                Data outputData = dataBuilder.build();
                return Result.failure(outputData);
            }
            int progress = (int) ((current / root) * 100);
            if (cur_progress != progress){
                cur_progress = progress;
                setProgressAsync(new Data.Builder().putInt(PROGRESS, progress).build());
            }

            if (root % current == 0){
                double dividend = root / current;
                dataBuilder.putInt("id", id);
                dataBuilder.putDouble("first", current);
                dataBuilder.putDouble("second", dividend);
                Data outputData = dataBuilder.build();
                return Result.success(outputData);
            }
            current += 1;
        }
        dataBuilder.putInt("id", id);
        dataBuilder.putDouble("root", root);
        dataBuilder.putBoolean("retry", false);
        return Result.failure(dataBuilder.build());
    }
}
