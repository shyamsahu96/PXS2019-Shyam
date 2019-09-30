package student.attendance.asessment.pojo;

public class Student {

    private String emp_id;
    private String name;

    public Student(String emp_id, String name) {
        this.emp_id = emp_id;
        this.name = name;
    }

    public Student() {
    }

    public String getEmp_id() {
        return emp_id;
    }

    public void setEmp_id(String emp_id) {
        this.emp_id = emp_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
