package com.messenger.nik.fragment;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.messenger.nik.BuildConfig;
import com.messenger.nik.R;
import com.messenger.nik.activity.FragmentLoadActivity;
import com.messenger.nik.helper.Constants;
import com.messenger.nik.models.UserStatus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {

    //CONSTANTS
    private static final String TAG = CameraFragment.class.getSimpleName();
    private static String ADDED_STATUS_CONTACTS = null;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //ANDROID RESOURCE CLASS
    private final Executor executor = Executors.newSingleThreadExecutor();
    private ProcessCameraProvider cameraProvider;
    private File file;
    private Bitmap bitmap;
    private ActivityResultLauncher<String[]> activityResultLauncher;

    //UI COMPONENTS
    private PreviewView previewView;
    private FloatingActionButton captureImage, sendImage;
    private ImageView displayImage;

    //FIREBASE CLASS
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private UploadTask uploadTask;

    public CameraFragment() {}

    @Override
    public void onPause() {
        super.onPause();
        if ( cameraProvider != null ) {
            cameraProvider.unbindAll();
        }

        //cancel the quick status upload task
        if ( uploadTask != null ) {
            uploadTask.cancel();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ADDED_STATUS_CONTACTS = "added_contacts" + "/" + Constants.current_user_virtual_number;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        previewView = view.findViewById(R.id.previewView);
        captureImage = view.findViewById(R.id.capture_image);
        sendImage = view.findViewById(R.id.send_image);
        displayImage = view.findViewById(R.id.display_image);

        view.setOnKeyListener((v, keyCode, event) -> {
            if ( keyCode == KeyEvent.KEYCODE_BACK ) {
                clearViews();

                FragmentLoadActivity.get()
                        .loadFragment( new ProfileFragment(), "ProfileFragment" );
            }
            return false;
        });

        updateSurfaceView(false);
        updateButtonState(false);

        permissions();

        return view;
    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance( requireContext() );

        cameraProviderFuture.addListener(() -> {
            try {

                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor( requireContext() ) );
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        final ImageCapture imageCapture = builder
                .setTargetRotation(requireActivity().getWindowManager().getDefaultDisplay().getRotation())
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);

        captureImage.setOnClickListener(v -> {

            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss", Locale.US);
            file = new File(getBatchDirectoryName(), mDateFormat.format(new Date())+ ".jpg");

            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        //show a success full toast of image capture
                        FragmentLoadActivity.get().toast(
                                getString(R.string.image_captured), Toast.LENGTH_SHORT);

                        //update the fab button when image is captured
                        updateButtonState(true);
                        //hide the surface view and show captured image in imageView
                        updateSurfaceView(true);

                        //get the bitmap from file path
                        bitmap = BitmapFactory.decodeFile( file.getAbsolutePath() );
                        //display the captured image in image view
                        displayImage.setImageBitmap( bitmap );

                        //initialize the on click listener of send image button
                        sendImage.setOnClickListener(view -> {
                            //initialize firebase storage reference with url
                            StorageReference storageReference = firebaseStorage
                                    .getReferenceFromUrl( Constants.URL_STORAGE_REFERENCE )
                                    .child( Constants.QUICK_STATUS );
                            StorageReference reference = storageReference
                                    .child( file.getName() + "_camera" );
                            //proceed to upload file to firebase storage
                            uploadToFirebase( reference, file );
                        });
                    });
                }
                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    error.printStackTrace();
                }
            });
        });
    }

    private String getBatchDirectoryName() {

        String app_folder_path;
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/nikApp";
        File dir = new File(app_folder_path);
        //noinspection StatementWithEmptyBody
        if (!dir.exists() && !dir.mkdirs()) { }

        return app_folder_path;
    }

    private void updateButtonState(boolean state) {
        if (state) {
            captureImage.setVisibility(View.GONE);
            sendImage.setVisibility(View.VISIBLE);
        } else {
            sendImage.setVisibility(View.GONE);
            captureImage.setVisibility(View.VISIBLE);
        }
    }

    private void updateSurfaceView(boolean state) {
        if (state) {
            previewView.setVisibility(View.GONE);
            displayImage.setVisibility(View.VISIBLE);
        } else {
            displayImage.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
        }
    }

    private void uploadToFirebase(StorageReference storageReference, File file) {
        if ( storageReference != null ) {
            Uri photoURI = FileProvider.getUriForFile( requireContext(), BuildConfig.APPLICATION_ID + ".provider", file );

            uploadTask = storageReference.putFile( photoURI );

            uploadTask.addOnFailureListener(e ->

                    FragmentLoadActivity.get().toast( getString(
                            R.string.something_went_wrong_profile ), Toast.LENGTH_SHORT )

            ).addOnCanceledListener(() ->

                    FragmentLoadActivity.get().toast(
                            getString(R.string.quick_failed_to_upload), Toast.LENGTH_SHORT )

            ).addOnSuccessListener(taskSnapshot -> {

                Task<Uri> fileURL = taskSnapshot.getStorage().getDownloadUrl();
                //noinspection StatementWithEmptyBody
                while( !fileURL.isComplete() );
                Uri url = fileURL.getResult(); //here we get download url

                String URL = String.valueOf( url );

                UserStatus userStatus = new UserStatus(
                        Constants.current_user_name,
                        Constants.current_user_virtual_number,
                        URL
                );

                getAllContacts( userStatus );
            });
        }
    }

    private void getAllContacts(UserStatus userStatus) {
        DatabaseReference child = FirebaseDatabase.getInstance()
                .getReference().child( ADDED_STATUS_CONTACTS );
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String storeVN = ds.getKey();

                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add( storeVN );

                    for (int i = 0; i < arrayList.size(); i++)
                    {
                        String virtualNumber = arrayList.get(i);
                        databaseReference.child( Constants.DB_US )
                                .child( virtualNumber )
                                .child( Constants.current_user_virtual_number )
                                .setValue( userStatus ).addOnSuccessListener(unused -> {
                                    clearViews();
                                    removeFragment();
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage()); //Don't ignore potential errors!
            }
        };
        child.addListenerForSingleValueEvent(listener);
    }

    private void clearViews() {
        previewView = null;
        captureImage = null;
        sendImage = null;
        displayImage = null;

        if ( file != null ) {

            boolean delete = file.delete();
            //noinspection StatementWithEmptyBody
            if ( delete ) {} else {}

            file = null;
        }

        if ( bitmap != null ) {
            bitmap.recycle();
        }
    }

    private void removeFragment() {
        /*
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove( this );
        fragmentTransaction.commit();
        fragmentManager.popBackStack();
        */

        FragmentLoadActivity.get().loadFragment( new ProfileFragment(), "ProfileFragment" );
        FragmentLoadActivity.get().toast( getString( R.string.status_sent ), Toast.LENGTH_SHORT );
    }

    private void permissions() {
        //PERMISSION
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (result.containsValue(false)) {
                alertDialog();
            } else if (shouldShowRequestPermissionRationale( Manifest.permission.CAMERA ) || shouldShowRequestPermissionRationale( Manifest.permission.WRITE_EXTERNAL_STORAGE ) || shouldShowRequestPermissionRationale( Manifest.permission.READ_EXTERNAL_STORAGE ) ){
                alertDialog();
            } else {
                startCamera();
            }
        });
        activityResultLauncher.launch( REQUIRED_PERMISSIONS );
    }

    private void alertDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder( requireContext() );
        dialogBuilder.setTitle(R.string.permissions_required);
        dialogBuilder.setMessage(R.string.permissions_required_summary);
        dialogBuilder.setPositiveButton(R.string.allow_permissions, (dialog, which) -> {
            dialog.dismiss();

            if ( activityResultLauncher != null ) {
                activityResultLauncher.launch( REQUIRED_PERMISSIONS );
            } else {
                FragmentLoadActivity.get().toast(
                        getString(R.string.something_went_wrong_permissions), Toast.LENGTH_SHORT);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
    }
}
