package com.example.md_app.models;

public class Model_Appointment {
    public Model_Doctor model_doctor;
    public String st_firstName;
    public String st_lastName;
    public String st_email;
    public String st_phoneNumber;
    public String st_appointmentDate;
    public Object obj_timestamp;


    public Model_Appointment(){}

    public Model_Appointment(Model_Doctor model_doctor,
                             String st_firstName, String st_lastName,
                             String st_email, String st_phoneNumber,
                             String st_appointmentDate, Object obj_timestamp) {
        this.model_doctor = model_doctor;
        this.st_firstName = st_firstName;
        this.st_lastName = st_lastName;
        this.st_email = st_email;
        this.st_phoneNumber = st_phoneNumber;
        this.st_appointmentDate = st_appointmentDate;
        this.obj_timestamp = obj_timestamp;
    }
}

/*
* SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm")
* long unixTime = (long) model.get(position).timestamp;
* Date date = new Date(unixTime);
* simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
* String time = simpleDateFormat.formate(date);*
* */
