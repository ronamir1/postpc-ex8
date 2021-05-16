package huji.postpc.y2021.multicalculator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static java.util.jar.Pack200.Unpacker.PROGRESS;

public class MainActivity extends AppCompatActivity {
    public CalculationHolder calcHolder = null;
    public EditText editTextInsertCalc;
    public FloatingActionButton buttonCreateCalc;
    public RecyclerView calcRecyclerView;
    public CalculationHolder.CalculationAdapter adapter;
    public SharedPreferences sp;
    public Data.Builder dataBuilder;
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextInsertCalc = findViewById(R.id.editTextInsertCalc);
        buttonCreateCalc = findViewById(R.id.buttonCreateCalc);
        calcRecyclerView = findViewById(R.id.calcRecyclerView);
        context = MainActivity.this;
        sp = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        calcHolder = new CalculationHolder(sp);
        adapter = new CalculationHolder.CalculationAdapter(calcHolder, WorkManager.getInstance(context));
        calcRecyclerView.setAdapter(adapter);
        calcRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataBuilder  = new Data.Builder();

        buttonCreateCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rootS = editTextInsertCalc.getText().toString();
                try {
                    double root = Double.parseDouble(rootS);
                    startWork(root, -1);


                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Please Insert An Integer", Toast.LENGTH_SHORT).show();
                }
            }
        });

        for (int i = 0; i < calcHolder.calculations.size(); i++){
            Calculation calc = calcHolder.calculations.get(i);
            if (calc.inProgress){
                startWork(calc.root, i);
            }
        }


    }


    private void startWork(double root, int pos){
        Calculation calc = null;
        if (pos == -1){
            calcHolder.addCalc(root);
            calc = calcHolder.calculations.get(0);
            adapter.notifyItemInserted(0);
        }
        else {
            calc = calcHolder.calculations.get(pos);
        }

        dataBuilder.putInt("id", calc.id);
        dataBuilder.putDouble("root", calc.root);
        dataBuilder.putDouble("current", calc.curCandidate);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CalculationWorker.class).setInputData(dataBuilder.build()).build();
        WorkManager.getInstance(context).enqueue(workRequest);
        calc.workId = workRequest.getId().toString();
        LiveData<WorkInfo> workInfo = WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(workRequest.getId());

        workInfo.observeForever(new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable WorkInfo workInfo) {
                if (workInfo != null) {
                    WorkInfo.State state = workInfo.getState();
                    if (state == WorkInfo.State.SUCCEEDED){
                        Data output = workInfo.getOutputData();
                        onSuccess(output);
                    }
                    else if (state == WorkInfo.State.FAILED){
                        Data output = workInfo.getOutputData();
                        onFail(output);
                    }
                    Data progress = workInfo.getProgress();
                    int value = progress.getInt(PROGRESS, 0);
                    updateProgress(workInfo.getId().toString(), value);
                }
            }
        });
    }

    private void onSuccess(Data output){
        int id = output.getInt("id", -1);
        double first = output.getDouble("first", 0);
        double second = output.getDouble("second", 0);
        int pos = findCalc(id);
        Calculation calc = calcHolder.calculations.get(pos);
        calc.div1 = first;
        calc.div2 = second;
        calcHolder.completedCalc(calc);
        adapter.notifyDataSetChanged();
        CalculationHolder.CalculationAdapter.ViewHolder viewHolder = (CalculationHolder.CalculationAdapter.ViewHolder) calcRecyclerView.findViewHolderForLayoutPosition(pos);
        if (viewHolder != null){
            viewHolder.turnOffProgressBar();
            viewHolder.turnOffDeleteButton();
            viewHolder.setMessage(calc.toString());
        }
    }

    private void onFail(Data output){
        boolean retry = output.getBoolean("retry", true);
        int id = output.getInt("id", -1);
        double root = output.getDouble("root", 0);
        if (retry){
            double current = output.getDouble("current", 0);
            int pos = findCalc(id);
            calcHolder.updateCurCandidate(pos, current);
            startWork(root, pos);
        }
        else {
            int pos = findCalc(id);
            Calculation calc = calcHolder.calculations.get(pos);
            calc.isPrime = true;
            calcHolder.completedCalc(calc);
            adapter.notifyDataSetChanged();
            CalculationHolder.CalculationAdapter.ViewHolder viewHolder = (CalculationHolder.CalculationAdapter.ViewHolder) calcRecyclerView.findViewHolderForLayoutPosition(pos);
            if (viewHolder != null){
                viewHolder.turnOffProgressBar();
                viewHolder.turnOffDeleteButton();
                viewHolder.setMessage(calc.toString());
            }
        }
    }

    private int findCalc(int id){
        for (int i = 0 ; i < calcHolder.calculations.size(); i++){
            if (id == calcHolder.calculations.get(i).id){
                return i;
            }
        }
        return -1;
    }

    private void updateProgress(String workId, int progress){
        for (int i = 0 ; i < calcHolder.calculations.size(); i++){
            if (calcHolder.calculations.get(i).workId.equals(workId)){
                calcHolder.calculations.get(i).progress = progress;
                CalculationHolder.CalculationAdapter.ViewHolder viewHolder = (CalculationHolder.CalculationAdapter.ViewHolder) calcRecyclerView.findViewHolderForLayoutPosition(i);
                if (viewHolder != null){
                    viewHolder.setCalculationRowProgress(progress);
                }
            }
        }
    }
}