package com.example.fer_medindex.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fer_medindex.R;
import com.example.fer_medindex.view.ProfilePatient;
import com.example.fer_medindex.view.ReadWritePatientDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class AdapterUsers extends FirebaseRecyclerAdapter<ReadWritePatientDetails, AdapterUsers.myviewholder> {
    private final Fragment fragment;


    public AdapterUsers(@NonNull FirebaseRecyclerOptions<ReadWritePatientDetails> options, Fragment fragment) {
        super(options);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        return new myviewholder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull myviewholder holder, int position, @NonNull ReadWritePatientDetails model) {
        holder.textfullname.setText(model.getFullname());
        holder.textAddress.setText(model.getDiachi());
        holder.textmobile.setText(model.getSodienthoai());
        // glide tải hình ảnh và lưu hình ảnh trong bộ đệm

        Glide.with(holder.img1.getContext())
                .load(model.getImgHinh())
                .error(R.mipmap.ic_launcher1)
                .into(holder.img1);
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfilePatient.class);
            intent.putExtra(ProfilePatient.IS_FULL_PROFILE, true);
            intent.putExtra(ProfilePatient.IS_DOCTOR, true);
            intent.putExtra(ProfilePatient.PATIENT_ID, model.getPatientId());
            intent.putExtra(ProfilePatient.FULLNAME, model.getFullname());
            intent.putExtra(ProfilePatient.EMAIL,model.getEmail());
            intent.putExtra(ProfilePatient.NGAYSINH,model.getNgaysinh());
            intent.putExtra(ProfilePatient.GIOITINH,model.getGioitinh());
            intent.putExtra(ProfilePatient.SODIENTHOAI,model.getSodienthoai());
            intent.putExtra(ProfilePatient.CMND,model.getCMND());
            intent.putExtra(ProfilePatient.DIACHI,model.getDiachi());
            intent.putExtra(ProfilePatient.TRANGTHAI,model.getTrangthai());
            intent.putExtra(ProfilePatient.HINHANH,model.getImgHinh());
            //put profile patient to intent
            intent.putExtra(ProfilePatient.CREATE_TIME, model.getCreateTimeString());
            intent.putExtra(ProfilePatient.EMOTION,(Serializable) model.getEmotions());
            fragment.requireActivity().startActivity(intent);



        });
//        holder.imageViewEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final DialogPlus dialogPlus = DialogPlus.newDialog(holder.img1.getContext())
//                        .setContentHolder(new ViewHolder(R.layout.update))
//                        .setExpanded(true,800)
//                        .create();
//
//
//                View view = dialogPlus.getHolderView();
//                EditText name = view.findViewById(R.id.editText_register_patient_full_name);
//                EditText ngaysinh = view.findViewById(R.id.editText_register_patient_dob);
//                EditText sodienthoai = view.findViewById(R.id.editText_register_patient_mobile);
//                EditText gioitinh = view.findViewById(R.id.editText_register_patient_gioitinh);
//                EditText CMND = view.findViewById(R.id.editText_register_patient_passport);
//                EditText diachi = view.findViewById(R.id.editText_register_patient_address);
//                EditText email = view.findViewById(R.id.editText_register_patient_email);
//                EditText trangthai = view.findViewById(R.id.editText_register_patient_tinh_trang);
//                Button btnUpdate = view.findViewById(R.id.button_update_patient);
//
//                name.setText(model.getFullname());
//                ngaysinh.setText(model.getNgaysinh());
//                gioitinh.setText(model.getGioitinh());
//                sodienthoai.setText(model.getSodienthoai());
//                CMND.setText(model.getCMND());
//                diachi.setText(model.getDiachi());
//                email.setText(model.getEmail());
//                trangthai.setText(model.getTrangthai());
//
//                dialogPlus.show();
//                btnUpdate.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Map<String,Object> map = new HashMap<>();
//                        map.put("fullname",name.getText().toString());
//                        map.put("sodienthoai",sodienthoai.getText().toString());
//                        map.put("ngaysinh",ngaysinh.getText().toString());
//                        map.put("gioitinh",gioitinh.getText().toString());
//                        map.put("cmnd",CMND.getText().toString());
//                        map.put("diachi",diachi.getText().toString());
//                        map.put("email",email.getText().toString());
//                        map.put("trangthai",trangthai.getText().toString());
//
//                        FirebaseDatabase.getInstance().getReference().child("Patients")
//                                .child(getRef(holder.getAdapterPosition()).getKey()).updateChildren(map)
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void unused) {
//                                        Toast.makeText(holder.textfullname.getContext(), "Dữ liệu được cập nhật thành công", Toast.LENGTH_SHORT).show();
//                                        dialogPlus.dismiss();
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Toast.makeText(holder.textfullname.getContext(), "Lỗi không cập nhật được", Toast.LENGTH_SHORT).show();
//                                        dialogPlus.dismiss();
//                                    }
//                                });
//                    }
//                });
//            }
//        });
        holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Tạo hộp thoại cảnh báo
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.textfullname.getContext());
                builder.setTitle("Bạn chắc chắn muốn xoá");
                builder.setMessage("Xoá dữ liệu");

                builder.setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference().child("Patients")
                                .child(getRef(holder.getAdapterPosition()).getKey()).removeValue();
                        Toast.makeText(holder.textfullname.getContext()," Dữ liệu đã xoá thành công",Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(holder.textfullname.getContext()," Thoát thành công",Toast.LENGTH_LONG).show();
                    }
                });
                builder.show();
            }
        });
    }

    public interface OnPlaceListener {

        void onPlaceClick(int position);
    }


    public class myviewholder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView img1;
        private TextView textfullname, textAddress, textmobile;

        private ImageView imageViewEdit, imageViewDelete;

        public myviewholder(@NonNull View itemView) {
            super(itemView);

            img1 = itemView.findViewById(R.id.avatarIv);
            textfullname = itemView.findViewById(R.id.nameTv);
            textAddress = itemView.findViewById(R.id.addressTv);
            textmobile = itemView.findViewById(R.id.mobileTv);
            cardView = itemView.findViewById(R.id.cardView_patient);

           // imageViewEdit = itemView.findViewById(R.id.edit);
            imageViewDelete = itemView.findViewById(R.id.delete);
        }
    }
}