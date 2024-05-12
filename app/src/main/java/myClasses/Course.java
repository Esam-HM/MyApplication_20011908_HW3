package myClasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class Course implements Serializable, Comparable<Course> {
    private String courseCode;
    private String courseName;
    private String semester;
    private String date;
    private String createdBy;
    private String status;
    private String groupNumbers;
    private ArrayList<Group> groups;

    public Course(){

    }

    public Course(String courseName, String courseCode, String groupNumbers, String createdBy) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.groupNumbers = groupNumbers;
        this.status = "Attending";
        this.createdBy = createdBy;
        this.groups = new ArrayList<>();
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getGroupNumbers() {
        return groupNumbers;
    }

    public void setGroupNumbers(String groupNumbers) {
        this.groupNumbers = groupNumbers;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    // check status of course.
    public void checkCourseStatus(){
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int m;
        if (this.date.contains("Spring")){
            if (month>=7){
                status="Complete";
            }
        }else{
            if (month>=3){
                status = "Complete";
            }
        }
    }

    @Override
    public int compareTo(Course o) {
        return this.date.compareTo(o.getDate());
    }
}

