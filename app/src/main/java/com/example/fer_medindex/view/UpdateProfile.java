package com.example.fer_medindex.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fer_medindex.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfile extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateDoB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName, textDoB, textGender, textMobile, textGmail;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Upload Profile Details");
        progressBar = findViewById(R.id.progressBar);
        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_gender);
        //xác thực firebase
        authProfile = FirebaseAuth.getInstance();
        // lấy người dùng hiện đang đăng nhập bằng sử dụng hồ sơ auth
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        //Show profile data
        showProfile(firebaseUser);

        //Upload Profile Pic
        Button buttonUploadProfilePic = findViewById(R.id.button_upload_profile_pic);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfile.this, UploadProfile.class);
                startActivity(intent);
                finish();
            }
        });


        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hiển thị ngày đã chọn trước hiển thị trên data picker dialog
                //Văn bản sẽ được tách ra theo dấu gạch chéo sau đó chúng ta chỉ có thể lấy những con số đó và lưu chúng vào các biến khác nhau
                String textSADoB[] = textDoB.split("/");
                //Integer.parseInt chuyển String thành Integer
                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1]) - 1; // tháng trong mảng index bắt đầu từ 0
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;

                //Date Picker Dialog
                picker = new DatePickerDialog(UpdateProfile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day); // 3 tham số xác định
                picker.show();
            }
        });
        // Update Profile
        Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  chuyển người dùng firebase
                updateProfile(firebaseUser);
            }
        });
    }

    // Update Profile
    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        // lưu nó dưới dạng cập nhật giới tính nút radio
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        //Xác thực điện thoại di động sử dụng Matcher và Pattern
        String mobileRegex = "[0][0-9]{9}";
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex); // xác định mẫu di động
        mobileMatcher = mobilePattern.matcher(textMobile);

        if (TextUtils.isEmpty(textFullName)) {
            Toast.makeText(UpdateProfile.this, "Please enter your full name", Toast.LENGTH_LONG).show();
            editTextUpdateName.setError("Full Name is required");
            editTextUpdateName.requestFocus();// yeu cau nhap lai
        } else if (TextUtils.isEmpty(textDoB)) {
            Toast.makeText(UpdateProfile.this, "Please enter date of birth", Toast.LENGTH_LONG).show();
            editTextUpdateDoB.setError("Date of birth is required");
            editTextUpdateDoB.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())) {
            Toast.makeText(UpdateProfile.this, "Please select your gender", Toast.LENGTH_LONG).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        } else if (TextUtils.isEmpty(textMobile)) {
            Toast.makeText(UpdateProfile.this, "Please enter your mobile ", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile is required");
            editTextUpdateMobile.requestFocus();
        } else if (textMobile.length() != 10) {
            Toast.makeText(UpdateProfile.this, "Please re-enter your mobile ", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile shoule be 10 digits");
            editTextUpdateMobile.requestFocus();
        } else if (!mobileMatcher.find()) {
            Toast.makeText(UpdateProfile.this, "Please re-enter your mobile ", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile is not valid");
            editTextUpdateMobile.requestFocus();
        } else {
            // Obtain the data entered by user
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();


            //Enter User Data into the Firebase Realtime Database .Set up dependencies
            // Ghi những thông tin người dùng nhập vào cơ sở dữ liệu
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textFullName, textDoB, textGender, textMobile);

            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);
            Map<String, Object> dataUpdate = new HashMap<>();
            dataUpdate.put("fullName", writeUserDetails.getFullName());
            dataUpdate.put("doB", writeUserDetails.getDoB());
            dataUpdate.put("gender", writeUserDetails.getGender());
            dataUpdate.put("mobile", writeUserDetails.getMobile());
            referenceProfile.child(userID).updateChildren(dataUpdate).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    //Setting new display name Cập nhật tên hiển thị vào đối tượng người dùng firebase
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                            setDisplayName(textFullName).build();
                    firebaseUser.updateProfile(profileUpdates);

                    Toast.makeText(UpdateProfile.this, "Update Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UpdateProfile.this, BackgroundDoctor.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(UpdateProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            });
        }

    }

    // nạp dữ liệu từ cơ sở dữ liệu
    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();

        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);
        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readUserDetails != null) {
                    textFullName = readUserDetails.getFullName();
                    textDoB = readUserDetails.getDoB();
                    textGender = readUserDetails.getGender();
                    textMobile = readUserDetails.getMobile();


                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdateMobile.setText(textMobile);
//                  firebaseUser.getEmail();


                    if (textGender.equals("Male")) {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_update_male);
                    } else {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_update_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(UpdateProfile.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfile.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}