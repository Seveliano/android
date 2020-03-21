package com.example.md_app.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md_app.R;
import com.example.md_app.models.Model_Appointment;
import com.example.md_app.models.Model_Doctor;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter_Appointments extends RecyclerView.Adapter<Adapter_Appointments.MyViewHolder> {

    private Context mContext;
    private List<Model_Appointment> model_appointments = new ArrayList<>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Adapter_Appointments(List<Model_Appointment> model_appointments){
        int nSize = model_appointments.size();
        for (int i = 0; i < nSize; i ++){
            this.model_appointments.add(model_appointments.get(nSize - i - 1));
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.card_appointment, parent, false);
        MyViewHolder holder = new MyViewHolder(itemView);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Model_Doctor model_doctor = model_appointments.get(position).model_doctor;
        Picasso.with(mContext)
                .load(model_doctor.st_imgUrl)
                .into(holder.circle_img_doctor);
        holder.tv_doctorName.setText(model_doctor.st_name);
        holder.tv_doctorQualification.setText(model_doctor.st_qualification);
        holder.tv_doctorHospital.setText(model_doctor.st_hospital);
        holder.tv_appointmentDate.setText(model_appointments.get(position).st_appointmentDate);

        long unixTime = (long) model_appointments.get(position).obj_timestamp;
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(unixTime);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(String.valueOf(calendar.getTimeZone())));
//        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String time = simpleDateFormat.format(date);
        holder.tv_bookedDate.setText(time);
    }

    @Override
    public int getItemCount() {
        return model_appointments.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView circle_img_doctor;
        public TextView tv_doctorName, tv_doctorQualification, tv_doctorHospital, tv_appointmentDate, tv_bookedDate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            circle_img_doctor = itemView.findViewById(R.id.cardAppointment_circleImage);
            tv_doctorName = itemView.findViewById(R.id.cardAppointment_tv_name);
            tv_doctorQualification = itemView.findViewById(R.id.cardAppointment_tv_qualification);
            tv_doctorHospital = itemView.findViewById(R.id.cardAppointment_tv_hospital);
            tv_appointmentDate = itemView.findViewById(R.id.cardAppointment_tv_appointmentDate);
            tv_bookedDate = itemView.findViewById(R.id.cardAppointment_tv_bookedDate);
        }
    }
}
