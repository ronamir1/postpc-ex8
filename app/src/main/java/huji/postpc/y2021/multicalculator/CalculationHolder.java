package huji.postpc.y2021.multicalculator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
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

    public int addCalc(double root){
        Calculation calc = new Calculation(root);
        calculations.add(calc);
        Collections.sort(this.calculations);
        if (sp != null){
            saveItems();
        }

        return calculations.indexOf(calc);
    }

    public int completedCalc(Calculation calc){
        calc.inProgress = false;
        Collections.sort(this.calculations);
        int pos =this.calculations.indexOf(calc);
        if (sp != null){
            saveItems();
        }
        return pos;
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
        Context context;

        public CalculationAdapter(CalculationHolder calcHolder, WorkManager workManager) {
            this.calcHolder = calcHolder;
            this.workManager = workManager;

        }

        @NonNull
        @Override
        public CalculationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            this.context = parent.getContext();
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
                    if (calc.inProgress){
                        workManager.cancelWorkById(UUID.fromString(calc.workId));
                    }
                    calcHolder.deleteCalc(calc);
                    notifyItemRangeRemoved(pos, 1);
                }
            });
            if (calc.inProgress){
                holder.calculationRowProgress.setProgress(calc.progress);
            }
            else {
                holder.calculationRowProgress.setVisibility(View.INVISIBLE);
                holder.calculationRowDelete.setBackground(AppCompatResources.getDrawable(context, R.drawable.ic_baseline_delete_24));
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
            private final View view;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                this.view = itemView;
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
                calculationRowProgress.setVisibility(View.GONE);
            }

            public void turnOffDeleteButton(){
                calculationRowDelete.setBackground(AppCompatResources.getDrawable(context, R.drawable.ic_baseline_delete_24));
            }

            public void setMessage(String message){
                calculationRowRoot.setText(message);
            }
        }


    }

}
