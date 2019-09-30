package student.attendance.asessment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import student.attendance.asessment.api.AttendanceApi;
import student.attendance.asessment.pojo.Student;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.spUserName)
    Spinner spUser;
    @BindView(R.id.spMonth)
    Spinner spMonth;
    @BindView(R.id.spYear)
    Spinner spYear;
    private int selectedMonth = 0;
    private int selectedYear = 0;
    private int selectedStudent = 0;
    private Calendar calendar;
    private SimpleDateFormat df;

    private String[] months = new String[0];
    private String[] years = new String[0];
    private AttendanceApi attendanceApi;
    private AttendanceViewModel model;
    private List<Student> students = new ArrayList<>();
    private List<String> names = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        calendar = Calendar.getInstance();
        df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        initSpinners();
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setNotifyOnChange(true);
        spUser.setAdapter(adapter);

        model = ViewModelProviders.of(this).get(AttendanceViewModel.class);

        //Get the list of Students.
        model.getStudents().observe(this, students -> {
            Log.i(TAG, "Received Students Size of: " + students.size());
            MainActivity.this.students.clear();
            MainActivity.this.students.addAll(students);
            names.clear();
            adapter.clear();
            for (Student student : students) {
                names.add(student.getName());
                Log.i(TAG, "Student Name: " + student.getName());
            }
            //spUser.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });
    }

    private void initSpinners() {
        //Months
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Set up adapter
        spMonth.setAdapter(monthAdapter);
        spMonth.setOnItemSelectedListener(this);
        months = getResources().getStringArray(R.array.months);

        //Years
        ArrayAdapter<CharSequence> yearsAdapter = ArrayAdapter.createFromResource(this,
                R.array.years, android.R.layout.simple_spinner_item);
        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Set up adapter
        spYear.setAdapter(yearsAdapter);
        spYear.setOnItemSelectedListener(this);
        years = getResources().getStringArray(R.array.years);

        spUser.setOnItemSelectedListener(this);
    }

    @OnClick(R.id.btnView)
    void getAttendanceReport() {
        String emp_id = students.get(selectedStudent).getEmp_id();
        if (emp_id == null || emp_id.isEmpty()) {
            Toast.makeText(this, "Employee Id is blank or Null", Toast.LENGTH_SHORT).show();
            return;
        }

        //Calculate the start date of a month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String from_dt = df.format(calendar.getTime());
        //calculate the end date of a month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String to_dt = df.format(calendar.getTime());

        Log.i(TAG, "From Date: " + from_dt + " To Date: " + to_dt);

        //Launch the activity
        if (students != null && students.size() > 0) {
            Intent intent = new Intent(this, AttendanceActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.emp_id), students.get(selectedStudent).getEmp_id());
            bundle.putString(getString(R.string.emp_name), students.get(selectedStudent).getName());
            bundle.putInt(getString(R.string.selected_month), selectedMonth);
            bundle.putInt(getString(R.string.selected_year), Integer.valueOf(years[selectedYear]));
            bundle.putString(getString(R.string.from_dt), from_dt);
            bundle.putString(getString(R.string.to_dt), to_dt);
            bundle.putInt(getString(R.string.numDays), calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spMonth:
                Log.i(TAG, "Selected Month: " + position);
                selectedMonth = position;
                calendar.set(Calendar.MONTH, selectedMonth);
                break;
            case R.id.spYear:
                Log.i(TAG, "Selected Year: " + position);
                selectedYear = position;
                calendar.set(Calendar.YEAR, Integer.valueOf(years[selectedYear]));
                break;
            case R.id.spUserName:
                Log.i(TAG, "Selected Student: " + names.get(position));
                selectedStudent = position;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
