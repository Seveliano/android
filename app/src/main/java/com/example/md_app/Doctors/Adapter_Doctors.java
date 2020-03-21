package com.example.md_app.Doctors;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md_app.Activity_Book;
import com.example.md_app.R;
import com.example.md_app.models.Model_Doctor;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter_Doctors extends RecyclerView.Adapter<Adapter_Doctors.MyViewHolder> {

    private List<Model_Doctor> model_doctors = new ArrayList<>();
    private Context mContext;

    public Adapter_Doctors(List<Model_Doctor> model_doctors){
        this.model_doctors = model_doctors;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.card_doctor, parent, false);
        MyViewHolder holder = new MyViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.with(mContext)
                .load(model_doctors.get(position).st_imgUrl)
                .into(holder.imageView);
        holder.tv_name.setText(model_doctors.get(position).st_name);
        holder.tv_qualification.setText(model_doctors.get(position).st_qualification);
        holder.tv_hospital.setText(model_doctors.get(position).st_hospital);
    }

    @Override
    public int getItemCount() {
        return model_doctors.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout lin_holder;
        public CircleImageView imageView;
        public TextView tv_name, tv_qualification, tv_hospital;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);

            lin_holder = itemView.findViewById(R.id.cardDoctor_lin_holder);
            imageView = itemView.findViewById(R.id.cardDoctor_circleImage);
            tv_name = itemView.findViewById(R.id.cardDoctor_tv_name);
            tv_qualification = itemView.findViewById(R.id.cardDoctor_tv_qualification);
            tv_hospital = itemView.findViewById(R.id.cardDoctor_tv_hospital);

            lin_holder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int nPos = getAdapterPosition();
                    Model_Doctor selected_doctor = model_doctors.get(nPos);
                    Intent intent = new Intent(mContext, Activity_Book.class);
                    intent.putExtra("selected_doctor", model_doctors.get(nPos));
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
