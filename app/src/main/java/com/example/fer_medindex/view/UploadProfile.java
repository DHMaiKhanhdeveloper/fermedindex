package com.example.fer_medindex.view;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fer_medindex.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class UploadProfile extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageView imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile);

        getSupportActionBar().setTitle("Upload Profile Picture");

        Button buttonUploadPicChoose = findViewById(R.id.upload_pic_choose_button);
        Button buttonUploadPic = findViewById(R.id.upload_pic_button);
        progressBar = findViewById(R.id.progressBar);
        imageViewUploadPic = findViewById(R.id.imageView_upload_profile_user);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();
        //Set User's current DP in ImageView ( if uploaded already). We will Picasso since imageViewer setImage
        //Regular URIs.
        //Người dùng tải ảnh lên rồi
        Picasso.with(UploadProfile.this).load(uri).into(imageViewUploadPic);
//        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference()
//                .child("Registerd Users").child("imgaAvatar");
//        referenceProfile.removeValue();

        // Chọn hình ảnh để tải lên
        buttonUploadPicChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        //Cập nhật hình ảnh lên profile
        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                uploadPic();
            }
        });

    }

    private void uploadPic() {
        if(uriImage != null){
            StorageReference fileReference = storageReference.child(getFileExtension(uriImage));

            fileReference.delete().addOnSuccessListener(aVoid -> fileReference.putFile(uriImage).addOnSuccessListener(taskSnapshot -> {
                firebaseUser = authProfile.getCurrentUser();
                // download ảnh về thành công
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    //Finally set the display image of user after upload
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri).build();
                    firebaseUser.updateProfile(profileUpdates);
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                    referenceProfile.child(firebaseUser.getUid()).child("imgAvatar").setValue(uri.toString()).addOnSuccessListener(unused -> {
                        //Quá trình upload diễn ra thành công
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(UploadProfile.this,"Upload Successful!",Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(UploadProfile.this,BackgroundDoctor.class);
                        startActivity(intent);
                        finish();
                    });
                });
            })).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        }
    }

    private String getFileExtension(Uri uri) {
        //Giúp cho một ứng dụng quản lý quyền truy cập đến dữ liệu được lưu bởi ứng dụng đó
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        // /* là chọn bất cứ hình ảnh loại gì cũng được
        intent.setType("image/*");
        // lấy nội dung trên Internet ,bên trong android
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // PICK_IMAGE_REQUEST= 1 yêu cầu chọn ảnh là đúng
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // kiểm tra mã yêu cầu có bằng yêu cầu hình ảnh ko ? , dữ liệu ko rỗng
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            // đặt hình ảnh người dùng chọn vào trong nền hình ảnh android
            // người dùng chọn hình ảnh trong dữ liệu điện thoại
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);
        }
    }

}