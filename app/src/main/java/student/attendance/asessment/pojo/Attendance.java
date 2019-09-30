package student.attendance.asessment.pojo;

public class Attendance {
    private String emp_id;
    private String entry_at;
    private String exit_at;

    public Attendance(String emp_id, String entry_at, String exit_at) {
        this.emp_id = emp_id;
        this.entry_at = entry_at;
        this.exit_at = exit_at;
    }

    public Attendance() {
    }

    public String getEmp_id() {
        return emp_id;
    }

    public void setEmp_id(String emp_id) {
        this.emp_id = emp_id;
    }

    public String getEntry_at() {
        return entry_at;
    }

    public void setEntry_at(String entry_at) {
        this.entry_at = entry_at;
    }

    public String getExit_at() {
        return exit_at;
    }

    public void setExit_at(String exit_at) {
        this.exit_at = exit_at;
    }
}
