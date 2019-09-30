package student.attendance.asessment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import student.attendance.asessment.pojo.Attendance;

public class AttendanceActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceActivity";

    @BindView(R.id.tvHeader)
    TextView header;
    @BindView(R.id.rvAttendance)
    RecyclerView recyclerView;
    @BindView(R.id.hoursLoggedValue)
    TextView tvHoursLogged;
    @BindView(R.id.daysAbsentValue)
    TextView tvDaysAbsent;
    @BindView(R.id.daysPresentValue)
    TextView tvDaysPresent;

    private int numDays = 0;
    private long totalWorkInMillis = 0;
    private double hoursLogged = 0;
    private double daysPresent = 0, daysAbsent = 0;
    private double[] workHours;
    private DecimalFormat decimalFormat;

    private String from_dt, to_dt, emp_id, emp_name;
    private int selectedMonth, selectedYear;
    private AttendanceViewModel model;
    private String[] months;
    private List<Attendance> attendances;
    private List<String> hours;
    private GridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        ButterKnife.bind(this);
        extractIntentData();
        //initWorkHours();

        hours = new ArrayList<>();
        adapter = new GridAdapter(new WeakReference<>(this), workHours);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        //Adapter

        model = ViewModelProviders.of(this).get(AttendanceViewModel.class);
        if (emp_id == null || emp_id.isEmpty()) {
            Log.i(TAG, "Employee ID is null");
            return;
        }
        model.getAttendanceReport(emp_id, from_dt, to_dt, selectedMonth, selectedYear);
        model.getWorkHours().observe(this, workHours -> {
            adapter.setWorkHours(workHours);
        });
        model.getHoursLogged().observe(this, hoursLogged -> {
            tvHoursLogged.setText(String.valueOf(hoursLogged));
        });
        model.getDaysPresent().observe(this, daysPresent -> {
            tvDaysPresent.setText(String.valueOf(daysPresent));
            tvDaysAbsent.setText(String.valueOf(numDays - daysPresent));
        });
    }

    private void initWorkHours() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, selectedMonth);
        c.set(Calendar.YEAR, selectedYear);
        c.set(Calendar.DAY_OF_MONTH, 1);
        numDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        Log.i(TAG, "Number of Days: " + numDays);
        workHours = new double[numDays];
        for (int i = 0; i < numDays; i++) {
            //Day
            c.set(Calendar.DAY_OF_MONTH, i + 1);
            if (c.get(Calendar.DAY_OF_WEEK) == 1)
                workHours[i] = -1.0D;//-1 for SUNDAY
            if (c.get(Calendar.DAY_OF_WEEK) == 7)
                workHours[i] = -2.0D;//-2 for SATURDAY
        }

        decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);
    }

    //Extract data from the intent that started this activity
    void extractIntentData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            selectedMonth = bundle.getInt(getString(R.string.selected_month), -1);
            selectedYear = bundle.getInt(getString(R.string.selected_year), -1);
            Log.i(TAG, "Selected Month: " + selectedMonth + "Selected Year: " + selectedYear);
            from_dt = bundle.getString(getString(R.string.from_dt));
            to_dt = bundle.getString(getString(R.string.to_dt));
            emp_id = bundle.getString(getString(R.string.emp_id));
            emp_name = bundle.getString(getString(R.string.emp_name));
            numDays = bundle.getInt(getString(R.string.numDays), 28);
        }
        workHours = new double[numDays];
        months = getResources().getStringArray(R.array.months);
        String headerText = emp_name + "'s Attendance Report for " + months[selectedMonth] + " " + selectedYear;
        Log.i(TAG, headerText);
        header.setText(headerText);
    }

    void processAttendances(List<Attendance> attendances) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        for (int i = 0; i < attendances.size(); i++) {
            Attendance attendance = attendances.get(i);
            //Find the Date and Hour and their difference
            String entry_at = attendance.getEntry_at();
            String exit_at = attendance.getExit_at();

            if (entry_at == null || exit_at == null) {
                Log.i(TAG, "One of the Dates is Null");
                continue;
            } else {
                Log.i(TAG, "Entry At: " + entry_at);
                Log.i(TAG, "Exit At: " + exit_at);
            }
            try {
                //Difference
                long diff = df.parse(exit_at).getTime() - df.parse(entry_at).getTime();
                if (diff <= 0) {
                    Log.i(TAG, "Diiference is less than or equal to 0");
                    continue;
                }
                //Set the date to login day
                calendar.setTimeInMillis(df.parse(entry_at).getTime());
                //find login hours
                double loginHours = TimeUnit.MILLISECONDS.toHours(diff) + (TimeUnit.MILLISECONDS.toMinutes(diff) % 60L) / 60D;
                //Add login hours to the particular day
                int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;
                if (workHours[day] == -1.0D || workHours[day] == -2.0D)//Remember we already initialized the sat/sun to -1 and -2 resp.
                    Log.i(TAG, "Day is Saturday/Sunday");
                else {
                    totalWorkInMillis += diff;//Add total work hours
                    loginHours = Double.valueOf(decimalFormat.format(loginHours));
                    workHours[day] += loginHours;
                    Log.i(TAG, "Login Hours: " + loginHours);
                }
            } catch (ParseException e) {
                Log.i(TAG, e.getMessage());
            }
        }
        adapter.setWorkHours(workHours);
        adapter.notifyDataSetChanged();
        //hours logged
        hoursLogged = TimeUnit.MILLISECONDS.toHours(totalWorkInMillis) + (TimeUnit.MILLISECONDS.toMinutes(totalWorkInMillis) % 60L) / 60D;

        //Number of days absent && no of days present
        for (int i = 0; i < numDays; i++) {
            if (workHours[i] <= 0)
                daysAbsent++;
            else if (workHours[i] > 0)
                daysPresent++;
        }
        Log.i(TAG, "Days Absent: " + daysAbsent);
        Log.i(TAG, "Days Present: " + daysPresent);
    }
}