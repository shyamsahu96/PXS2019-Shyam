package student.attendance.asessment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.MyViewHolder> {

    private WeakReference<Context> context;
    private double[] workHours;

    public GridAdapter(WeakReference<Context> context, double[] workHours) {
        this.workHours = workHours;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context.get()).inflate(R.layout.two_side, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        myViewHolder.dayOfMonth.setText(String.valueOf(i + 1));
        if (workHours[i] == -1.0D)//Sunday
            myViewHolder.noOfHours.setText("Sunday");
        else if (workHours[i] == -2.0D)//Saturday
            myViewHolder.noOfHours.setText("Saturday");
        else
            myViewHolder.noOfHours.setText(String.valueOf((workHours[i])));

    }

    public void setWorkHours(double[] workHours) {
        this.workHours = workHours;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return workHours.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvSide1)
        TextView dayOfMonth;
        @BindView(R.id.tvSide2)
        TextView noOfHours;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}