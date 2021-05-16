package huji.postpc.y2021.multicalculator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;

public class CalculationHolder extends Activity {
    LinkedList<Calculation> calculations;
    SharedPreferences sp;

    CalculationHolder(){this.calculations = new LinkedList<>();}

    CalculationHolder(SharedPreferences sp){
        this.sp = sp;
        if (sp == null){
            calculations = new LinkedList<>();
            return;
        }
        String stateString = this.sp.getString("calculations", "");
        if (!stateString.equals("")){
            Gson gson = new Gson();
            Calculation[] recoveredItems = gson.fromJson(stateString, Calculation[].class);
            this.calculations = new LinkedList<>(Arrays.asList(recoveredItems));
        }
        else {
            calculations = new LinkedList<>();
        }
    }

    public void updateCurCandidate(int pos, double curCandidate){
        calculations.get(pos).curCandidate = curCandidate;
        if (sp != null){
            saveItems();
        }
    }

    public void addCalc(double root){
        Calculation calc = new Calculation(root);
        calculations.addFirst(calc);
        if (sp != null){
            saveItems();
        }
    }

    public void completedCalc(Calculation calc){
        calc.inProgress = false;
        Collections.sort(this.calculations);
        if (sp != null){
            saveItems();
        }
    }

    public void deleteCalc(Calculation calc)
    {
        this.calculations.remove(calc);
        if (sp != null){
            saveItems();
        }
    }

    public void saveItems(){
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String stateString = gson.toJson(this.calculations.toArray());
        editor.putString("calculations", stateString);
        editor.apply();
    }

    public static class CalculationAdapter extends RecyclerView.Adapter<CalculationAdapter.ViewHolder>{
        private final CalculationHolder calcHolder;
        private final WorkManager workManager;

        public CalculationAdapter(CalculationHolder calcHolder, WorkManager workManager) {
            this.calcHolder = calcHolder;
            this.workManager = workManager;
        }

        @NonNull
        @Override
        public CalculationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.row_calculation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CalculationAdapter.ViewHolder holder, int position) {
            int pos = holder.getLayoutPosition();
            Calculation calc = calcHolder.calculations.get(pos);
            holder.calculationRowRoot.setText(calc.toString());
            holder.calculationRowDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getLayoutPosition();
                    Calculation calc = calcHolder.calculations.get(pos);
                    workManager.cancelWorkById(UUID.fromString(calc.workId));
                    calcHolder.deleteCalc(calc);
                    notifyItemRangeRemoved(pos, 1);
                }
            });
            if (calc.inProgress){
                holder.calculationRowProgress.setProgress(calc.progress);
            }
            else {
                holder.calculationRowProgress.setVisibility(View.INVISIBLE);
                holder.calculationRowDelete.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return calcHolder.calculations.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView calculationRowRoot;
            private final ImageView calculationRowDelete;
            private final ConstraintLayout calculationRow;
            private final ProgressBar calculationRowProgress;
            private final Context context;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.calculationRowRoot = itemView.findViewById(R.id.calculationRowRoot);
                this.calculationRowDelete = itemView.findViewById(R.id.calculationRowDelete);
                this.calculationRow = itemView.findViewById(R.id.calculationRow);
                this.calculationRowProgress = itemView.findViewById(R.id.calculationRowProgress);
                this.context = itemView.getContext();
            }

            public void setCalculationRowProgress(int progress){
                calculationRowProgress.setProgress(progress);
            }

            public void turnOffProgressBar(){
                calculationRowProgress.setVisibility(View.INVISIBLE);
            }

            public void turnOffDeleteButton(){
                calculationRowDelete.setVisibility(View.INVISIBLE);
            }

            public void setMessage(String message){
                calculationRowRoot.setText(message);
            }
        }


    }

}
