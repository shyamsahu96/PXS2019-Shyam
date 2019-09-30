package student.attendance.asessment;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import student.attendance.asessment.pojo.Attendance;

public class AttendanceCalculator {

    private static final String TAG = "AttendanceCalculator";

    private int daysPresent;
    private List<Attendance> attendances;
    private int month, year;
    private double[] dailyWorkHours;
    private double hoursLogged;

    private int numDays;

    private Calendar calendar;
    private DecimalFormat decimalFormat;

    public AttendanceCalculator(List<Attendance> attendances, int month, int year) {
        this.attendances = attendances;
        this.month = month;
        this.year = year;

        //Creating calendar object for counting number of days
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        numDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        //Initialize the array to start storing the hours logged per day
        dailyWorkHours = new double[numDays];

        //Decimal Format to be set as 2 Fraction digits
        decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);
    }

    void generateAttendanceReport() {
        initWorkHours();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        long totalWorkInMillis = 0;
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
                if (dailyWorkHours[day] == -1.0D || dailyWorkHours[day] == -2.0D)//Remember we already initialized the sat/sun to -1 and -2 resp.
                    Log.i(TAG, "Day is Saturday/Sunday");
                else {
                    totalWorkInMillis += diff;//Add total work hours
                    loginHours = Double.valueOf(decimalFormat.format(loginHours));
                    dailyWorkHours[day] += loginHours;
                    Log.i(TAG, "Login Hours: " + loginHours);
                }
            } catch (ParseException e) {
                Log.i(TAG, e.getMessage());
            }
        }
        //adapter.setWorkHours(workHours);
        //adapter.notifyDataSetChanged();
        //hours logged
        hoursLogged = TimeUnit.MILLISECONDS.toHours(totalWorkInMillis) + (TimeUnit.MILLISECONDS.toMinutes(totalWorkInMillis) % 60L) / 60D;

        //Number of days absent && no of days present
        for (int i = 0; i < numDays; i++) {
            if (dailyWorkHours[i] > 0)
                daysPresent++;
        }
        Log.i(TAG, "Days Absent: " + (numDays - daysPresent));
        Log.i(TAG, "Days Present: " + daysPresent);
    }

    private void initWorkHours() {
        for (int i = 0; i < numDays; i++) {
            //Day
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            if (calendar.get(Calendar.DAY_OF_WEEK) == 1)
                dailyWorkHours[i] = -1.0D;//-1 for SUNDAY
            if (calendar.get(Calendar.DAY_OF_WEEK) == 7)
                dailyWorkHours[i] = -2.0D;//-2 for SATURDAY
        }
    }

    public int getDaysPresent() {
        return daysPresent;
    }

    public int getMonth() {
        return month;
    }

    public double[] getDailyWorkHours() {
        return dailyWorkHours;
    }

    public double getHoursLogged() {
        return hoursLogged;
    }
}