package myClasses;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String name, surname, id, account_type;
    private String phoneNumber, email, password;
    private String profile, educ_info, twit, instagram;
    private Map<String, String> hiddens = new HashMap<>();
    private HashMap<String,String> courses;
    public User(){
    }

    public User(String name,String surname,String id,String email,String password){
        this.name = name;
        this.surname = surname;
        this.id = id;
        this.email = email;
        this.password = password;
        this.phoneNumber= "";
        this.educ_info = "";
        this.instagram = "";
        this.twit = "";
        this.profile = "";
        this.courses = new HashMap<String,String>();
        this.set_account_type();
        this.initializePrivate();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEduc_info() {
        return educ_info;
    }
    public void setEduc_info(String educ_info) {
        this.educ_info = educ_info;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getInstagram() {
        return instagram;
    }
    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }
    public String getTwit() {
        return twit;
    }
    public void setTwit(String twit) {
        this.twit = twit;
    }
    public String getAccount_type() {
        return account_type;
    }
    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }
    public String getProfile() {
        return profile;
    }
    public void setProfile(String profile) {
        this.profile = profile;
    }

    public HashMap<String, String> getCourses() {
        return courses;
    }

    public void setCourses(HashMap<String, String> courses) {
        this.courses = courses;
    }

    public void set_account_type(){
        String std = "@std.yildiz.edu.tr";
        if (this.email.contains(std)){
            this.setAccount_type("Student");
        }else {
            this.setAccount_type("Instructor");
        }
    }

    public Map<String, String> getHiddens() {
        return hiddens;
    }

    public String getTargetIndex(String key){
        return this.hiddens.get(key);
    }

    public void setTargetIndex(String key,String value){
        this.hiddens.replace(key,value);
    }

    public void initializePrivate(){
        hiddens.put("email","0");
        hiddens.put("phone_num","0");
        hiddens.put("insta","0");
        hiddens.put("twit","0");
    }

    public String contactInfo(){
        String message = "I'm " + this.name +" " + this.surname +".";
        if(!this.instagram.isEmpty()) {
            if (this.hiddens.get("insta").equals("0")) {
                message = message + "\nPlease follow my instagram account " + this.instagram + "!!";
            }else {
                message = message + "\nmy instagram account is hidden!!";
            }
        } else{
            message = message + " I do not have instagram account.";
        }
        return message;
    }
}

