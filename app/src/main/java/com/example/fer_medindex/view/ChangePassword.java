package com.example.fer_medindex.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fer_medindex.R;
import com.example.fer_medindex.fragment.DoctorFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText editTextPwdCurr , editTextPwdNew , editTextConfirmPwdNew;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd , buttonReAuthenticate;
    private ProgressBar progressBar;
    private String userPwdCurr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Change Password");

        editTextPwdNew = findViewById(R.id.editText_change_pwd_new);
        editTextPwdCurr = findViewById(R.id.editText_change_pwd_current);
        editTextConfirmPwdNew = findViewById(R.id.editText_change_pwd_new_confirm);
        textViewAuthenticated = findViewById(R.id.textView_change_pwd_authenticated);
        progressBar = findViewById(R.id.progressBar);
        buttonReAuthenticate = findViewById(R.id.button_change_pwd_authenticate);
        buttonChangePwd = findViewById(R.id.button_change_password);
        // Disable editText for New Password , Confirm New Password and Make Change Pwd Button unclickable till user ishn
        // tắt nhập mật khẩu mới để nhập mật khẩu cũ , hiện tại
        editTextPwdNew.setEnabled(false);
        editTextConfirmPwdNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);
        //xác thực firebase
        authProfile = FirebaseAuth.getInstance();
        // sử dụng biến auth lưu nó trong biến người dùng
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){ // kiểm tra xem người dùng có null equals có nghĩa là bằng
            Toast.makeText(ChangePassword.this,"Something went wrong! User's details not available",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePassword.this, BackgroundDoctor.class);
            startActivity(intent);
            finish();
        }else {
            reAuthenticateUser(firebaseUser);
        }
    }
 // ReAuthenticate User before changing password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // lấy được mật khẩu hiện tại mà người dùng đang sử dụng
                userPwdCurr = editTextPwdCurr.getText().toString();
                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePassword.this,"Password is needed",Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("Please enter your current password to authenticate");
                    editTextPwdCurr.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    // tạo thông tin xác thực mà được đã thấy trong cập nhật các hoạt động email
                    //ReAuthenticate User now lấy mật khẩu trong firebase và dùng biến người dùng firebase nhận email cũ trong firebase
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),userPwdCurr);
                    // chuyển thông tin đăng nhập chỉ cần trình nghe hoàn chỉnh
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                //Disable editText for Current Password. Enable EditText for New Password and Confirm New Password
                                //Tắt editText cho Mật khẩu Hiện tại. Bật EditText cho mật khẩu mới và xác nhận mật khẩu mới
                                editTextPwdCurr.setEnabled(false);
                                editTextPwdNew.setEnabled(true);
                                editTextConfirmPwdNew.setEnabled(true);
                                //Enable Change Pwd Button . Disable Authenticate Button
                                buttonReAuthenticate.setEnabled(false);
                                buttonChangePwd.setEnabled(true);
                                // thay đổi mật khẩu Set TextView to show User is authenticated/verified
                                textViewAuthenticated.setText("You are authenticated/Verified"+"You can change password now!");
                                Toast.makeText(ChangePassword.this,"Password has been verified"+
                                        "Change password now",Toast.LENGTH_SHORT).show();
                                //Update color of Change Password Button
                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(ChangePassword.this, R.color.yellow));
                                // Trình nghe nhấn chuột để thay đổi mật khẩu
                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            }else {
                                try{
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(ChangePassword.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userPwdNew = editTextPwdNew.getText().toString();
        String userPwdConfirmNew = editTextConfirmPwdNew.getText().toString();
       String userPwdCurr = editTextPwdCurr.getText().toString();

        if(TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(ChangePassword.this,"New Password is needed",Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter your new password");
            editTextPwdNew.requestFocus();
        }else if(TextUtils.isEmpty(userPwdConfirmNew)){
            Toast.makeText(ChangePassword.this,"Please confirm your new password",Toast.LENGTH_SHORT).show();
            editTextConfirmPwdNew.setError("Please re-enter your new password");
            editTextConfirmPwdNew.requestFocus();
        } else if(!userPwdNew.matches(userPwdConfirmNew)){
            Toast.makeText(ChangePassword.this,"Password did not match",Toast.LENGTH_SHORT).show();
            editTextConfirmPwdNew.setError("Please re-enter same password");
            editTextConfirmPwdNew.requestFocus();
        } else if(userPwdNew.matches(userPwdCurr)){
            Toast.makeText(ChangePassword.this,"New Password cannot be same as old password",Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter a new password");
            editTextPwdNew.requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            // sử dụng biến người dùng firebase , gọi phương thức cập nhật mật khẩu
            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){ // thay đổi mật khẩu thành công
                        Toast.makeText(ChangePassword.this,"Password has been changed",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePassword.this,BackgroundDoctor.class);
                        startActivity(intent);
                        finish();
                    }else {
                        try {
                            throw task.getException();
                        }catch (Exception e){
                            Toast.makeText(ChangePassword.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

}