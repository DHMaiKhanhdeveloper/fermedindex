package com.example.fer_medindex.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fer_medindex.R;
import com.example.fer_medindex.utils.Extensions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {
    private EditText editTextRegisterFullName, editTextRegisterEmail,
            editTextRegisterDoB, editTextRegisterMobile, editTextRegiterPass, editRegisterConfirmPass;
    private ProgressBar progressBar;
    private RadioButton radioButtonRegisterGenderSelected;
    private RadioGroup radioGroupRegisterGender;
    private DatePickerDialog picker;
    private static final String TAG = "RegisterActivity";
    Uri uriImage;
    public static final int REQUEST_CODE_CAMERA = 456;
    public static final int SELECT_PICTURE = 123;

    private static final int REQUEST_PHOTO_GALLERY = 100;
    private ImageView imageProfile;
    private static final int REQUEST_CAPTURE_IMAGE = 110;
    private String textFullName;
    private String textEmail;
    private String textDoB;
    private String textMobile;
    private String textPassword;
    private String textConfirmPass;
    private String textGender;
    private String mobileRegex;
    private Matcher mobileMatcher;
    private  Pattern mobilePattern;
    private Button buttonRegister;
    private StorageReference reference = FirebaseStorage.getInstance().getReference();
    private ImageView imageViewUploadPic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        changeStatusBarColor();
//        getSupportActionBar().setTitle("Register");
//        Toast.makeText(RegisterActivity.this, "You can register now",Toast.LENGTH_LONG).show();

        imageProfile = findViewById(R.id.imagePatient);
        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterDoB = findViewById(R.id.editText_register_dob);
        editTextRegisterMobile = findViewById(R.id.editText_register_mobile);
        editTextRegiterPass = findViewById(R.id.editText_register_password);
        editRegisterConfirmPass = findViewById(R.id.editText_register_confirm_password);
        progressBar = findViewById(R.id.progressBar);
        imageViewUploadPic = findViewById(R.id.imageView_profile_user);

        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogChooseImage();
            }
        });

        //setting up DatePicker on EditText
        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                int style = android.app.AlertDialog.THEME_HOLO_LIGHT;

                //Date Picker Dialog
                picker = new DatePickerDialog(RegisterActivity.this,style, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day); // 3 tham số xác định
                picker.show();
            }
        });

       buttonRegister = findViewById(R.id.button_register);
        buttonRegister.setOnClickListener(v -> {
            v.setEnabled(false);
            int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
            radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

            textFullName = editTextRegisterFullName.getText().toString();
            textEmail = editTextRegisterEmail.getText().toString();
            textDoB = editTextRegisterDoB.getText().toString();
            textMobile = editTextRegisterMobile.getText().toString();
            textPassword = editTextRegiterPass.getText().toString();
            textConfirmPass = editRegisterConfirmPass.getText().toString();
            //Xác thực điện thoại di động sử dụng Matcher và Pattern
            mobileRegex = "[0][0-9]{9}";
            mobilePattern = Pattern.compile(mobileRegex); // xác định mẫu di động
            mobileMatcher = mobilePattern.matcher(textMobile);

            if (TextUtils.isEmpty(textFullName)) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ họ và tên của bạn", Toast.LENGTH_LONG).show();
                editTextRegisterFullName.setError("Bắt buộc nhập họ và tên");
                editTextRegisterFullName.requestFocus();// yeu cau nhap lai
                v.setEnabled(true);
            } else if (TextUtils.isEmpty((textEmail))) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập email của bạn", Toast.LENGTH_LONG).show();
                editTextRegisterEmail.setError("Email is required");
                editTextRegisterEmail.requestFocus();
                v.setEnabled(true);
            } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) { // khác true
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập lại email của bạn", Toast.LENGTH_LONG).show();
                editTextRegisterEmail.setError("email không hợp lệ");
                editTextRegisterEmail.requestFocus();
                v.setEnabled(true);
            } else if (TextUtils.isEmpty(textDoB)) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập ngày sinh của bạn", Toast.LENGTH_LONG).show();
                editTextRegisterDoB.setError("Bắt buộc nhập ngày sinh");
                editTextRegisterDoB.requestFocus();
                v.setEnabled(true);
            } else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                Toast.makeText(RegisterActivity.this, "Vui lòng chọn giới tính của bạn", Toast.LENGTH_LONG).show();
                radioButtonRegisterGenderSelected.setError("Bắt buộc phải chọn giới tính");
                radioButtonRegisterGenderSelected.requestFocus();
                v.setEnabled(true);
            } else if (TextUtils.isEmpty(textMobile)) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập số điện thoại của bạn", Toast.LENGTH_LONG).show();
                editTextRegisterMobile.setError("Bắt buộc nhập số điện thoại");
                editTextRegisterMobile.requestFocus();
                v.setEnabled(true);
            } else if (textMobile.length() != 10) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập số điện thoại của bạn ", Toast.LENGTH_LONG).show();
                editTextRegisterMobile.setError("Điện thoại di động phải có 10 chữ số");
                editTextRegisterMobile.requestFocus();
                v.setEnabled(true);
            } else if (!mobileMatcher.find()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập lại số điện thoại của bạn ", Toast.LENGTH_LONG).show();
                editTextRegisterMobile.setError("Điện thoại di động không hợp lệ");
                editTextRegisterMobile.requestFocus();
                v.setEnabled(true);
            } else if (TextUtils.isEmpty(textPassword)) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập lại mật khẩu của bạn ", Toast.LENGTH_LONG).show();
                editTextRegiterPass.setError("Bắt buộc phải nhập mật khẩu");
                editTextRegiterPass.requestFocus();
                v.setEnabled(true);
            } else if (textPassword.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu phải có ít nhất 6 kí tự ", Toast.LENGTH_LONG).show();
                editTextRegiterPass.setError("Mật khẩu quá yếu");
                editTextRegiterPass.requestFocus();
                v.setEnabled(true);
            } else if (TextUtils.isEmpty(textConfirmPass)) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập lại mật khẩu ", Toast.LENGTH_LONG).show();
                editRegisterConfirmPass.setError("Bắt buộc phải nhập mật khẩu xác nhận");
                editRegisterConfirmPass.requestFocus();
                v.setEnabled(true);
            } else if (!textPassword.equals(textConfirmPass)) {
                Toast.makeText(RegisterActivity.this, "Vui lòng nhập cùng một mật khẩu ", Toast.LENGTH_LONG).show();
                editRegisterConfirmPass.setError("Bắt buộc phải nhập mật khẩu xác nhận");
                editTextRegiterPass.clearComposingText(); // xoa nhap lai
                editRegisterConfirmPass.clearComposingText();
                v.setEnabled(true);
            } else {
                uploadToFirebase(uriImage);
            }
        });

    }

    private void registerUser(String textFullName, String textEmail, String textDoB, String textMobile, String textGender, String textPassword,String imgHinh) {
        FirebaseAuth auth = FirebaseAuth.getInstance(); //xac thuc firebase
        // tạo user profile
        auth.createUserWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(RegisterActivity.this,
                task -> {
                    buttonRegister.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();

                        //Nhap du lieu vao Firebase Realtime Database java object
                        ReadWriteUserDetails writerUserDetails = new ReadWriteUserDetails(textFullName, textEmail, textDoB, textGender, textMobile,imgHinh);

                        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                        referenceProfile.child(firebaseUser.getUid()).setValue(writerUserDetails).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                // Gui xac nhan Email
                                firebaseUser.sendEmailVerification();

                                Toast.makeText(RegisterActivity.this, "Người dùng đã đăng ký thành công, Vui lòng xác minh email của bạn", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                // Ngan nguoi dung dang ki thanh cong khong quay lai dang ki lai lan nua , nguoi dung dang ki thanh cong se chuyen den trang ho so
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                auth.signOut();
                                firebaseUser.reload();
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Người dùng đăng ký không thành công, Vui lòng thử lại", Toast.LENGTH_LONG).show();
                            }

                            progressBar.setVisibility(View.GONE);
                        });


                    } else {
                        try {
                            throw task.getException(); // java exception
                        } catch (FirebaseAuthWeakPasswordException e) {
                            editTextRegiterPass.setError("Mật khẩu của bạn quá yếu. Vui lòng sử dụng kết hợp các bảng chữ cái hoặc số");
                            editRegisterConfirmPass.requestFocus();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            editTextRegiterPass.setError("Email của bạn không hợp lệ hoặc đã được sử dụng. Vui lòng nhập lại");
                            editTextRegiterPass.requestFocus();
                        } catch (FirebaseAuthUserCollisionException e) { // Ngoại lệ va chạm người dùng
                            editTextRegiterPass.setError("Người dùng đã được đăng ký với email này. Sử dụng email khác.");
                            editTextRegiterPass.requestFocus();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    @SuppressLint("ObsoleteSdkInt")
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);

    }


    private void uploadToFirebase(Uri uri) {
        final StorageReference fileRef = reference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                textGender = radioButtonRegisterGenderSelected.getText().toString();
                                progressBar.setVisibility(View.VISIBLE);
                                registerUser(textFullName, textEmail, textDoB, textMobile, textGender, textPassword,uri.toString());
                            }
                        }, 1000);

                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
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
                        ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_CAMERA);
                        break;
                    case 1:
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SELECT_PICTURE);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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

    public void callCamera(){
        String filename = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
        Bundle bundleOptions = new Bundle();
        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE, bundleOptions);
    }

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            uriImage = uri;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
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
            img = Bitmap.createBitmap(tmpImg, 0, 0, tmpImg.getWidth(), tmpImg.getHeight(), mat, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (img.getConfig() != null) {
            switch (img.getConfig()) {
                case RGBA_F16:
                    img = img.copy(Bitmap.Config.ARGB_8888, true);
                    break;
            }
        }
        try {
            imageViewUploadPic.setImageBitmap(img);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cache != null) ((File) cache).delete();
    }




}
