package com.example.fer_medindex.view;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.example.fer_medindex.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class SelectPatient extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_patient);
        ImageView imageViewDoctor = findViewById(R.id.Register_Background_Patient);
        imageViewDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(SelectPatient.this, Register_Patient.class);
            startActivity(intent);
        });

        ImageView imageViewPatient = findViewById(R.id.xem_lai_form);
        imageViewPatient.setOnClickListener(v -> {
            v.setEnabled(false);
            ReadWritePatientDetails patientDetails = PatientFormInput.getForm();
            if (patientDetails == null) {
                new AlertDialog.Builder(SelectPatient.this)
                        .setMessage("Bạn chưa điền form nào, bạn có muốn điền một form không?")
                        .setOnCancelListener((dialog)->{
                            v.setEnabled(true);
                        })
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            Intent intent = new Intent(SelectPatient.this, Register_Patient.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("Thoát", (dialog, which) -> {

                        })
                        .create()
                        .show();
            } else {
                ReadWritePatientDetails patientNewDetails = PatientFormInput.getForm();
//                String patientId = intent.getStringExtra(PATIENT_ID);
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Patients");
                referenceProfile.child(patientNewDetails.patientId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            ReadWritePatientDetails readWritePatientDetails = task.getResult().getValue(ReadWritePatientDetails.class);
                            if(patientDetails.patientId!=null){
                                Intent intent = new Intent(SelectPatient.this, ProfilePatient.class);
                                intent.putExtra(ProfilePatient.IS_FULL_PROFILE , true);
                                intent.putExtra(ProfilePatient.PATIENT_ID, patientDetails.patientId);
                                intent.putExtra(ProfilePatient.FULLNAME, patientDetails.fullname);
                                intent.putExtra(ProfilePatient.EMAIL,patientDetails.email);
                                intent.putExtra(ProfilePatient.NGAYSINH,patientDetails.ngaysinh);
                                intent.putExtra(ProfilePatient.GIOITINH,patientDetails.gioitinh);
                                intent.putExtra(ProfilePatient.SODIENTHOAI,patientDetails.sodienthoai);
                                intent.putExtra(ProfilePatient.CMND,patientDetails.cmnd);
                                intent.putExtra(ProfilePatient.DIACHI,patientDetails.diachi);
                                intent.putExtra(ProfilePatient.TRANGTHAI,patientDetails.trangthai);
                                intent.putExtra(ProfilePatient.CREATE_TIME,patientDetails.getCreateTimeString());
                                intent.putExtra(ProfilePatient.HINHANH,patientDetails.imgHinh);
                                intent.putExtra(ProfilePatient.EMOTION,(Serializable)patientDetails.getEmotions());
                                intent.putExtra(ProfilePatient.IS_SHOW_TINH_TRANG_BENH, true);
//                                intent.putExtra(ProfilePatient.IS_FULL_PROFILE, true);
                                intent.putExtra(ProfilePatient.TINH_TRANG_BENH,readWritePatientDetails.getTinhtrangbenh());
                                //put profile patient to intent
                                startActivity(intent);

                            }
                        }
                    }
                    v.setEnabled(true);
                });
                       // ReadWritePatientDetails readWritePatientDetails = snapshot.getValue(ReadWritePatientDetails.class);




            }
        });

//        ImageView imageViewDanh_Sach = findViewById(R.id.danh_sach_benh_nhan);
//        imageViewDanh_Sach.setOnClickListener(v -> {
//            Intent intent = new Intent(SelectPatient.this,ListPatientActivity.class);
//            startActivity(intent);
//        });

    }
    //public static final String TINH_TRANG_BENH = "TRINH_TRANG_BENH";
    public static String PATIENT_ID = "PATIENT_ID";
}