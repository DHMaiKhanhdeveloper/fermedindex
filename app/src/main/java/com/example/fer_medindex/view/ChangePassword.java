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

        getSupportActionBar().setTitle("Đổi mật khẩu");

        editTextPwdNew = findViewById(R.id.editText_change_pwd_new);
        editTextPwdCurr = findViewById(R.id.editText_change_pwd_current);
        editTextConfirmPwdNew = findViewById(R.id.editText_change_pwd_new_confirm);
        textViewAuthenticated = findViewById(R.id.textView_change_pwd_authenticated);
        progressBar = findViewById(R.id.progressBar);
        buttonReAuthenticate = findViewById(R.id.button_change_pwd_authenticate);
        buttonChangePwd = findViewById(R.id.button_change_password);

        // tắt nhập mật khẩu mới để nhập mật khẩu cũ , hiện tại
        editTextPwdNew.setEnabled(false);
        editTextConfirmPwdNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);
        //xác thực firebase
        authProfile = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){ // kiểm tra xem người dùng có null equals có nghĩa là bằng
            Toast.makeText(ChangePassword.this,"Đã xảy ra lỗi! Thông tin chi tiết của người dùng không có sẵn",Toast.LENGTH_SHORT).show();
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
                userPwdCurr = editTextPwdCurr.getText().toString();
                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePassword.this,"Mật khẩu là cần thiết",Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("Vui lòng nhập mật khẩu hiện tại của bạn để xác thực");
                    editTextPwdCurr.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    //ReAuthenticate User now lấy mật khẩu trong firebase và dùng biến người dùng firebase nhận email cũ trong firebase
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),userPwdCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                //Tắt editText cho Mật khẩu Hiện tại. Bật EditText cho mật khẩu mới và xác nhận mật khẩu mới
                                editTextPwdCurr.setEnabled(false);
                                editTextPwdNew.setEnabled(true);
                                editTextConfirmPwdNew.setEnabled(true);

                                buttonReAuthenticate.setEnabled(false);
                                buttonChangePwd.setEnabled(true);

                                textViewAuthenticated.setText("Bạn đã xác thực"+"Bạn có thể thay đổi mật khẩu ngay bây giờ!");
                                Toast.makeText(ChangePassword.this,"Password has been verified"+
                                        "Thay đổi mật khẩu ngay bây giờ",Toast.LENGTH_SHORT).show();
                                //Update color of Change Password Button
                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(ChangePassword.this, R.color.yellow));

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
            Toast.makeText(ChangePassword.this,"Mật khẩu mới là cần thiết",Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Vui lòng nhập mật khẩu mới của bạn");
            editTextPwdNew.requestFocus();
        }else if(TextUtils.isEmpty(userPwdConfirmNew)){
            Toast.makeText(ChangePassword.this,"Vui lòng xác nhận mật khẩu mới của bạn",Toast.LENGTH_SHORT).show();
            editTextConfirmPwdNew.setError("Vui lòng nhập lại mật khẩu mới của bạn");
            editTextConfirmPwdNew.requestFocus();
        } else if(!userPwdNew.matches(userPwdConfirmNew)){
            Toast.makeText(ChangePassword.this,"Mật khẩu không khớp",Toast.LENGTH_SHORT).show();
            editTextConfirmPwdNew.setError("Vui lòng nhập lại cùng một mật khẩu");
            editTextConfirmPwdNew.requestFocus();
        } else if(userPwdNew.matches(userPwdCurr)){
            Toast.makeText(ChangePassword.this,"Mật khẩu mới không được giống mật khẩu cũ",Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Vui lòng nhập mật khẩu mới");
            editTextPwdNew.requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            // sử dụng biến người dùng firebase , gọi phương thức cập nhật mật khẩu
            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){ // thay đổi mật khẩu thành công
                        Toast.makeText(ChangePassword.this,"Mật khẩu đã được thay đổi",Toast.LENGTH_SHORT).show();
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