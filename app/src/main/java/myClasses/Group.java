package myClasses;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
    private String groupNo;
    private String instructorId;
    private ArrayList<String> students;

    public Group(){

    }

    public Group(String groupNo) {
        this.groupNo = groupNo;
        this.instructorId = "";
        this.students = new ArrayList<>();
    }

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public ArrayList<String> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<String> students) {
        this.students = students;
    }
}

