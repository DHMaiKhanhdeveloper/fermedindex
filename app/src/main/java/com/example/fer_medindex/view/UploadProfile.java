package com.example.fer_medindex.view;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.fer_medindex.R;
import com.example.fer_medindex.utils.Extensions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadProfile extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageView imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference reference = FirebaseStorage.getInstance().getReference();
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;
    private ImageView imageProfile;
    public static final int REQUEST_CODE_CAMERA = 456;
    public static final int SELECT_PICTURE = 123;

    private static final int REQUEST_PHOTO_GALLERY = 100;
    private static final int REQUEST_CAPTURE_IMAGE = 110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile);

        //getSupportActionBar().setTitle("Cập nhật ảnh hồ sơ");


        Button buttonUploadPic = findViewById(R.id.upload_pic_button);
        progressBar = findViewById(R.id.progressBar);
        imageViewUploadPic = findViewById(R.id.imageView_upload_profile_user);
        imageProfile = findViewById(R.id.imagePatient);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();


        Uri uri = firebaseUser.getPhotoUrl();

        Picasso.with(UploadProfile.this).load(uri).into(imageViewUploadPic);


        // Chọn hình ảnh để tải lên
        imageProfile.setOnClickListener(v -> dialogChooseImage());
        //Cập nhật hình ảnh lên profile
        buttonUploadPic.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            FirebaseAuth auth = FirebaseAuth.getInstance();
            referenceProfile.child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ReadWriteUserDetails readWritePatientDetails = snapshot.getValue(ReadWriteUserDetails.class);
                    if (readWritePatientDetails != null) {
                        uploadPic(readWritePatientDetails.getImgAvatar(), uriImage);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(UploadProfile.this, "Đã xảy ra lỗi! Thông tin chi tiết của người dùng hiện không có sẵn", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        });

    }

    private void uploadPic(String oldUrl, Uri uri) {
        if (uri != null) {

            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldUrl);
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    storageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                                    Map<String, Object> dataUpdate = new HashMap<>();
                                    //ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails();
                                    dataUpdate.put("imgAvatar", uri.toString());
                                    referenceProfile.child(firebaseUser.getUid()).updateChildren(dataUpdate).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setPhotoUri(uri).build();
                                            firebaseUser.updateProfile(profileUpdates);
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(UploadProfile.this, "Tải lên thành công!", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(UploadProfile.this, BackgroundDoctor.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(exception -> {
                progressBar.setVisibility(View.GONE);
            });


        }
    }

    public void dialogChooseImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        ArrayList<String> list = new ArrayList<String>();

        list.add("Máy ảnh");
        list.add("Album");

        CharSequence[] items = list.toArray(new CharSequence[list.size()]);
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(UploadProfile.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_CAMERA);
                        break;
                    case 1:
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(UploadProfile.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SELECT_PICTURE);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getFileExtension(Uri uri) {
        //Giúp cho một ứng dụng quản lý quyền truy cập đến dữ liệu được lưu bởi ứng dụng đó
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    protected Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth,
                                                int reqHeight, ContentResolver contentResolver) throws IOException {
        InputStream in = contentResolver.openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);
        in.close();
        int width = options.outWidth / reqWidth + 1;
        int height = options.outHeight / reqHeight + 1;
        options.inSampleSize = Math.max(width, height);
        options.inJustDecodeBounds = false;
        in = contentResolver.openInputStream(uri);
        Bitmap tmpImg = BitmapFactory.decodeStream(in, null, options);
        in.close();
        return tmpImg;
    }

    public void callGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_PHOTO_GALLERY);
    }

    protected String getPath(ContentResolver contentResolver, Uri uri) {
        String[] columns = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(uri, columns, null, null, null);
        cursor.moveToFirst();
        String path = cursor.getString(0);
        cursor.close();
        return path;
    }

    public void callCamera() {
        String filename = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
        Bundle bundleOptions = new Bundle();
        // trả về ảnh gọi  startActivityForResult hàm để trở về truyền Permision
        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE, bundleOptions);
    }

    // chọn camera hay chọn ảnh từ thư viện
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                callCamera();
            } else if (requestCode == SELECT_PICTURE) {
                callGallery();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private Uri mPhotoUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String path = null;
        Bitmap tmpImg = null;
        File cache = null;
        if (requestCode == REQUEST_PHOTO_GALLERY && resultCode == RESULT_OK) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                cache = Extensions.cache(this, in);
                path = cache.getAbsolutePath();

            } catch (FileNotFoundException e) {
                throw new AssertionError();
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());

                tmpImg = this.decodeSampledBitmapFromUri(data.getData(), bitmap.getWidth(), bitmap.getHeight(), getContentResolver());
                uriImage = data.getData();
                imageViewUploadPic.setImageURI(uriImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            final boolean existsData = data != null && data.getData() != null;
            Uri uri = existsData ? data.getData() : mPhotoUri;
            // truyền vào hàm đẩy lên ảnh
            uriImage = uri;
            try {// covert uri sang bitmap sau đó đổ bitmap lên ảnh
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                //decode dung lượng ảnh hoặc kích thước ảnh bé theo kích thước của mình
                tmpImg = this.decodeSampledBitmapFromUri(uri, bitmap.getWidth(), bitmap.getHeight(), getContentResolver());
            } catch (IOException e) {
                e.printStackTrace();
            }
            path = this.getPath(getContentResolver(), uri);
        }
        if (path == null) {
            return;
        }

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exif == null && tmpImg == null) {
            return;
        }

        Matrix mat = new Matrix();
        String exifDate = null;
        if (exif != null) {
            exifDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
            mat = Extensions.asMatrixByOrientation(exif);
        }

        Bitmap img = null;
        try {
            // làm rõ ảnh cho ảnh hiện lên ko bị mờ
            img = Bitmap.createBitmap(tmpImg, 0, 0, tmpImg.getWidth(), tmpImg.getHeight(), mat, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (img.getConfig() != null) {
            switch (img.getConfig()) {
                case RGBA_F16: // dạng chuẩn bitmap là ARGB 8888
                    img = img.copy(Bitmap.Config.ARGB_8888, true);
                    break;
            }
        }
        try {
            // imageProfile.setImageBitmap(img);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cache != null) ((File) cache).delete();

    }

}