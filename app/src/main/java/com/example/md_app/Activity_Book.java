package com.example.md_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.md_app.models.Model_Appointment;
import com.example.md_app.models.Model_Doctor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import noman.weekcalendar.WeekCalendar;
import noman.weekcalendar.listener.OnDateClickListener;

import static com.example.md_app.Constants.emailPattern;

public class Activity_Book extends Alert_Base {

    private Model_Doctor selected_doctor;

    private Button btn_back;
    private CircleImageView img_doctorImage;
    private TextView tv_doctorName, tv_doctorQualification, tv_doctorHospital;

    private EditText et_firstName, et_lastName, et_emailAddress, et_phoneNumber;
    private Spinner sp_countryCode;
    private WeekCalendar weekCalendar;
    private Button btn_submit;

    private Adapter_CountryCodes adapter_countryCodes;

    private String st_uid, st_doctor_uid;
    private String st_firstName, st_lastName, st_emailAddress,
            st_phoneNumber, st_appointmentDate;

    private int nCurrentdate, nAppointmentDate;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        if (getIntent() != null){
            selected_doctor = (Model_Doctor) getIntent().getSerializableExtra("selected_doctor");
        }

        init_viewItem();

        st_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        st_doctor_uid = selected_doctor.st_uid;

        event_handling();
    }

    private void init_viewItem(){
        btn_back = findViewById(R.id.activityBook_btn_back);
        img_doctorImage = findViewById(R.id.activityBook_img_doctorImage);
        tv_doctorName = findViewById(R.id.activityBook_tv_doctorName);
        tv_doctorQualification = findViewById(R.id.activityBook_tv_doctorQualification);
        tv_doctorHospital = findViewById(R.id.activityBook_tv_doctorHospital);

        Picasso.with(this)
                .load(selected_doctor.st_imgUrl)
                .into(img_doctorImage);
        tv_doctorName.setText(selected_doctor.st_name);
        tv_doctorQualification.setText(selected_doctor.st_qualification);
        tv_doctorHospital.setText(selected_doctor.st_hospital);

        et_firstName = findViewById(R.id.activityBook_et_firstName);
        et_lastName = findViewById(R.id.activityBook_et_lastName);
        et_emailAddress = findViewById(R.id.activityBook_et_email);
        sp_countryCode = findViewById(R.id.activityBook_sp_countryCode);
        et_phoneNumber = findViewById(R.id.activityBook_et_phoneNumber);
        weekCalendar = findViewById(R.id.activityBook_weekCalendar);
        weekCalendar.reset();
        btn_submit = findViewById(R.id.activityBook_btn_submit);

        init_countryCode();
    }

    private void init_countryCode(){
        adapter_countryCodes = new Adapter_CountryCodes(this);
        sp_countryCode.setAdapter(adapter_countryCodes);

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int index = Adapter_CountryCodes.getIndex(manager.getSimCountryIso());

        if (index > -1){
            sp_countryCode.setSelection(index);
        }
    }

    private void event_handling(){
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        weekCalendar.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(DateTime dateTime) {

                int day = dateTime.getDayOfMonth();
                int month = dateTime.getMonthOfYear();
                int year = dateTime.getYear();
                nAppointmentDate = year * 10000 + month * 100 + day;
                st_appointmentDate = year + "-" + month + "-" + day;

                Toast.makeText(Activity_Book.this,
                        "You Selected " + st_appointmentDate, Toast.LENGTH_LONG).show();
            }

        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate()) return;

                send_book_appointment();
            }
        });
    }

    private boolean validate() {
        st_firstName = et_firstName.getText().toString().trim();
        st_lastName = et_lastName.getText().toString().trim();
        st_emailAddress = et_emailAddress.getText().toString().trim();

        String st_countryCode = adapter_countryCodes.getCode(sp_countryCode.getSelectedItemPosition());
        String st_phone = et_phoneNumber.getText().toString().trim();

        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        nCurrentdate = Integer.parseInt(sdf.format(currentDate));

        if (st_firstName.isEmpty() || st_firstName.equals("")){
            alert_ok("Alert", "Enter your first name!");
            return false;
        }else if (st_lastName.isEmpty() || st_lastName.equals("")){
            alert_ok("Alert", "Enter your last name!");
            return false;
        } else if (st_emailAddress.isEmpty() || st_emailAddress.equals("")){
            alert_ok("Alert", "Enter your email address!");
            return false;
        } else if (!st_emailAddress.matches(emailPattern)){
            alert_ok("Alert", "InCorrect Email!");
            return false;
        } else if (st_phone.isEmpty() || st_phone.equals("")) {
            alert_ok("Alert", "Enter your phone number!");
            return false;
        } else if (st_appointmentDate.isEmpty() || st_appointmentDate.equals("")) {
            alert_ok("Alert", "Please select appointment date!");
            return false;
        } else if (nAppointmentDate < nCurrentdate){
            alert_ok("Alert", "Please select appointment date correctly!");
            return false;
        }
        st_phoneNumber = st_countryCode + st_phone;

        return true;
    }

    private void send_book_appointment(){
        btn_submit.setEnabled(false);
        progressDialog = progressDialog.show(this, "Alert", "Connecting...");
        Model_Appointment model_appointment = new Model_Appointment(selected_doctor, st_firstName,
                st_lastName, st_emailAddress, st_phoneNumber, st_appointmentDate, ServerValue.TIMESTAMP);
        FirebaseDatabase.getInstance().getReference()
                .child("book_appointments").child(st_uid).push().setValue(model_appointment)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        if (task.isSuccessful()){
                            alert_ok_withFinish("Alert", "Booked successfully!");
                        }else {
                            Toast.makeText(Activity_Book.this,
                                    "Sorry. There must be some error. Try again!",
                                    Toast.LENGTH_LONG).show();
                            btn_submit.setEnabled(true);
                        }
                    }
                });
    }

    public void alert_ok_withFinish(String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        btn_submit.setEnabled(true);
                        finish();
                    }
                });

        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle(title);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
