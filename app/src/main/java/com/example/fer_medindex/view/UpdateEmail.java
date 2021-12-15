package com.example.fer_medindex.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmail extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private  String userOldEmail, userNewEmail, userPwd;
    private Button buttonUpdateEmail;
    private EditText editTextNewEmail,editTextPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        getSupportActionBar().setTitle("Cập nhật email");

        progressBar = findViewById(R.id.progressBar);
        editTextPwd = findViewById(R.id.editText_update_email_verify_password);
        editTextNewEmail = findViewById(R.id.editText_update_email_new);
        textViewAuthenticated = findViewById(R.id.textView_update_email_authenticated);
        buttonUpdateEmail = findViewById(R.id.button_update_email);

        buttonUpdateEmail.setEnabled(false);
        editTextNewEmail.setEnabled(false);
        // xác thực firebase
        authProfile = FirebaseAuth.getInstance();

        firebaseUser = authProfile.getCurrentUser();
        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.textView_update_email_old);
        textViewOldEmail.setText(userOldEmail);

        if(firebaseUser.equals("")){
            Toast.makeText(UpdateEmail.this,"Đã xảy ra lỗi! Thông tin chi tiết của người dùng không có sẵn",Toast.LENGTH_LONG).show();
        } else{
            reAuthenticate(firebaseUser);
        }
    }
    //Xác thực khi người  dùng thay đổi và cập nhật id email
    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button buttonVerifyUser = findViewById(R.id.button_authenticate_user);
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwd = editTextPwd.getText().toString();
                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(UpdateEmail.this,"Mật khẩu là cần thiết để tiếp tục",Toast.LENGTH_LONG).show();
                    editTextPwd.setError("Vui lòng nhập mật khẩu của bạn để xác thực");
                    editTextPwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail,userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(UpdateEmail.this,"Mật khẩu đã được xác minh."+
                                    "Bạn có thể cập nhật email ngay bây giờ",Toast.LENGTH_LONG).show();
                            textViewAuthenticated.setText("Bạn đã được xác thực. Bạn có thể cập nhật email của mình ngay bây giờ.");

                            editTextNewEmail.setEnabled(true);
                            buttonUpdateEmail.setEnabled(true);
                            buttonVerifyUser.setEnabled(false);
                            editTextPwd.setEnabled(false);

                            // Change color of update email button
                            buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmail.this, R.color.yellow));
                            buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    userNewEmail = editTextNewEmail.getText().toString();
                                    if(TextUtils.isEmpty(userNewEmail)){
                                        Toast.makeText(UpdateEmail.this,"Bắt buộc nhập email mới",Toast.LENGTH_LONG).show();
                                        editTextPwd.setError("Nhập Email mới");
                                        editTextPwd.requestFocus();
                                    } else  if(!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                        Toast.makeText(UpdateEmail.this,"Vui lòng nhập email hợp lệ",Toast.LENGTH_LONG).show();
                                        editTextPwd.setError("Vui lòng cung cấp Email hợp lệ");
                                        editTextPwd.requestFocus();
                                    }else  if( userOldEmail.matches(userNewEmail)){
                                        Toast.makeText(UpdateEmail.this,"Email mới không được giống với Email cũ",Toast.LENGTH_LONG).show();
                                        editTextPwd.setError("Nhập Email mới");
                                        editTextPwd.requestFocus();
                                    } else {
                                        progressBar.setVisibility(View.VISIBLE);
                                        updateEmail(firebaseUser);
                                    }
                                }
                            });
                            } else {
                                try{
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(UpdateEmail.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    });
                }
            }
        });

    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){
                    // gửi liên kết xác thực email người dùng
                    firebaseUser.sendEmailVerification();
                    Toast.makeText(UpdateEmail.this,"Email đã được cập nhật. Vui lòng xác minh email mới của bạn",Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UpdateEmail.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    try{
                        throw task.getException();
                    }catch (Exception e){
                        Toast.makeText(UpdateEmail.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }


}