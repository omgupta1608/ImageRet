package com.example.imageret;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Folder path for Firebase Storage.
    String Storage_Path = "All_Image_Uploads/";

    // Root Database Name for Firebase Database.
    String Database_Path = "All_Image_Uploads_Database";

    // Creating button.
    Button UploadButton;

    // Creating EditText.
    EditText ImageTitle;
    EditText ImageDescription;

    StorageReference ref;



    // Creating ImageView.


    // Creating URI.
    Uri selectedImage;

    private static int RESULT_LOAD_IMAGE = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;
    ImageView image;

    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;

    // Image request code for onActivityResult() .
    int Image_Request_Code = 7;

    ProgressDialog progressDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);

// Setting up message in Progress dialog.
        progressDialog.setMessage("Loading Images From Firebase.");

// Showing progress dialog.
        progressDialog.show();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {

            } else {
                requestPermission();
            }
        }
        UploadButton = findViewById(R.id.choose);
        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkPermission()) {

                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(i, RESULT_LOAD_IMAGE);

                } else {
                    Toast.makeText(MainActivity.this, "The app doesn't has the permissions granted!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            String[] filePathColumn = new String[]{MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            image = findViewById(R.id.preview);

            image.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            findViewById(R.id.upload).setVisibility(View.VISIBLE);

        }

        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });


    }

    private boolean checkPermission() {
        //PackageManager packageManager = getPackageManager();
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //PackageManager packageManager = getPackageManager();
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this, "Permission accepted", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(MainActivity.this,
                            "Permission denied", Toast.LENGTH_LONG).show();
                    //Button sendSMS = (Button) findViewById(R.id.sendSMS);
                    //sendSMS.setEnabled(false);

                }
                break;
        }
    }

    private void uploadImage() {
        if (selectedImage != null) {

            // Code for showing progressDialog while uploading
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            ref = storageReference.child("images/" + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(selectedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast
                                    .makeText(MainActivity.this,
                                            "Image Uploaded!!",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            String TempImageTitle = ImageTitle.getText().toString().trim();
                            String TempImageDescription = ImageDescription.getText().toString().trim();



                            // Showing toast message after done uploading.
                            //Toast.makeText(getApplicationContext(), "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();

                            @SuppressWarnings("VisibleForTests")
                            ImageDetails imageUploadInfo = new ImageDetails(TempImageTitle,TempImageDescription, taskSnapshot.getDownloadUrl().toString());

                            // Getting image upload ID.
                            String ImageUploadId = databaseReference.push().getKey();

                            // Adding image upload id s child element into databaseReference.
                            databaseReference.child(ImageUploadId).setValue(imageUploadInfo);

                            startActivity(new Intent(getApplicationContext(), Ret.class));
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast
                                    .makeText(MainActivity.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress
                                    = (100.0
                                    * taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(
                                    "Uploaded "
                                            + (int) progress + "%");

                        }
                    });
        }
    }



}
