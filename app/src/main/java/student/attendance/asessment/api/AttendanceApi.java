package student.attendance.asessment.api;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import student.attendance.asessment.pojo.Attendance;
import student.attendance.asessment.pojo.Student;

public interface AttendanceApi {
    @GET("att_rprt_api.php?e76c37b493ea168cea60b8902072387caf297979")
    Call<List<Student>> getStudents();

    @Multipart
    @POST("att_rprt_api.php?e76c37b493ea168cea60b8902072387caf297979")
    Call<List<Attendance>> getAttendanceOfStudent(@Part("emp_id") RequestBody emp, @Part("from_dt") RequestBody from, @Part("to_dt") RequestBody to);
}
