package student.attendance.asessment;

import android.app.Application;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import student.attendance.asessment.api.AttendanceApi;
import student.attendance.asessment.pojo.Attendance;
import student.attendance.asessment.pojo.Student;

public class AttendanceViewModel extends AndroidViewModel {

    private static final String TAG = "AttendanceViewModel";

    private MutableLiveData<List<Student>> students;
    private MutableLiveData<List<Attendance>> attendance;
    private MutableLiveData<double[]> mutableWorkHours;
    private MutableLiveData<Double> mutableHoursLogged;
    private MutableLiveData<Integer> mutableDaysPresent;

    private AttendanceApi attendanceApi;

    private Application application;
    private DecimalFormat decimalFormat;

    public AttendanceViewModel(@NonNull Application application) {
        super(application);
        this.application = application;

        decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);

        //LiveData Init
        students = new MutableLiveData<>();
        attendance = new MutableLiveData<>();
        mutableWorkHours = new MutableLiveData<>();
        mutableHoursLogged = new MutableLiveData<>();
        mutableDaysPresent = new MutableLiveData<>();

        //Create GSON
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").create();
        //OkHttpClient & interceptor
        HttpLoggingInterceptor bodyInterceptor = new HttpLoggingInterceptor();
        bodyInterceptor.level(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(bodyInterceptor).build();
        //Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(application.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        attendanceApi = retrofit.create(AttendanceApi.class);
    }

    public LiveData<List<Student>> getStudents() {
        attendanceApi.getStudents().enqueue(new Callback<List<Student>>() {
            @Override
            public void onResponse(@NotNull Call<List<Student>> call, @NotNull Response<List<Student>> response) {
                students.setValue(response.body());
            }

            @Override
            public void onFailure(@NotNull Call<List<Student>> call, @NotNull Throwable t) {
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return students;
    }

    public void getAttendanceReport(String emp_id, String from_dt, String to_dt, int month, int year) {

        RequestBody body_emp = RequestBody.create(emp_id, MediaType.parse("text/plain"));
        RequestBody from = RequestBody.create(from_dt, MediaType.parse("text/plain"));
        RequestBody to = RequestBody.create(to_dt, MediaType.parse("text/plain"));
        attendanceApi.getAttendanceOfStudent(body_emp, from, to).enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(@NotNull Call<List<Attendance>> call, @NotNull Response<List<Attendance>> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    attendance.setValue(response.body());
                    AttendanceCalculator attendanceCalculator = new AttendanceCalculator(response.body(), month, year);
                    attendanceCalculator.generateAttendanceReport();
                    mutableWorkHours.setValue(attendanceCalculator.getDailyWorkHours());
                    mutableHoursLogged.setValue(attendanceCalculator.getHoursLogged());
                    mutableDaysPresent.setValue(attendanceCalculator.getDaysPresent());
                } else
                    Toast.makeText(application, "Request failed with response code" + response.code() + response.message(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NotNull Call<List<Attendance>> call, @NotNull Throwable t) {
                Toast.makeText(application, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public LiveData<double[]> getWorkHours() {
        return mutableWorkHours;
    }

    public LiveData<Double> getHoursLogged() {
        return mutableHoursLogged;
    }

    public LiveData<Integer> getDaysPresent() {
        return mutableDaysPresent;
    }
}
