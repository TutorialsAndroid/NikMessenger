package com.messenger.nik.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.messenger.nik.R;
import com.messenger.nik.activity.FragmentLoadActivity;
import com.messenger.nik.adapter.ChatAdapter;
import com.messenger.nik.adapter.GifAdapter;
import com.messenger.nik.helper.Constants;
import com.messenger.nik.helper.FileOpen;
import com.messenger.nik.customInterface.ChatUserData;
import com.messenger.nik.models.ChatModel;
import com.messenger.nik.models.FileModel;
import com.messenger.nik.models.GifModel;
import com.messenger.nik.models.RCModel;
import com.tenor.android.core.constant.MediaCollectionFormats;
import com.tenor.android.core.model.impl.Result;
import com.tenor.android.core.network.ApiClient;
import com.tenor.android.core.response.BaseError;
import com.tenor.android.core.response.WeakRefCallback;
import com.tenor.android.core.response.impl.GifsResponse;
import com.tenor.android.core.response.impl.TrendingGifResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class ChatFragment extends Fragment implements View.OnClickListener, ChatUserData {

    //CONSTANTS
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int MAX_GIF_STORE = 10;
    private static final int TRENDING_MAX_GIF_STORE =10;
    private static String other_user_name = null;
    private static String other_user_avatar = null;
    private static String other_user_virtual_number = null;
    private static String selected_gif_url = null;
    private static String selected_tagged_message = null;
    private static String selected_file_url = null;
    private static String selected_file_type = null;
    private static String chat_room_ID = null;
    private static String group_vn = null;
    private String notificationKey = null;
    private static boolean isActiveDownload = false;

    //UI COMPONENTS
    private LinearLayout tag_message_view, user_chat_gif_view;
    private TextView user_chat_text_view, tag_message_text_view;
    private RecyclerView user_chat_recycler_view, user_chat_gif_recycler_view;
    private EditText write_message_edit_text, search_gif_edit_text;
    private ProgressBar download_file_progress_bar, loading_gif_progress_bar;
    private ImageView send_message_button,
            custom_toolbar_back_button,
            add_person_to_contact_button,
            send_file_button,
            close_tag_message_view,
            close_gif_view_button,
            search_gif_button,
            send_gif_button;

    //ANDROID RESOURCE CLASS
    private File localFile;
    private ArrayList<GifModel> imageUrlList;
    private Context context;
    private RecyclerView.AdapterDataObserver chatDataObserver;

    //FIREBASE DATABASE CLASS
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private ChatAdapter chatAdapter;
    private GifAdapter gifAdapter;
    private StorageReference downloadReference;
    private ValueEventListener fileAlertListener;
    private ValueEventListener fileDownloadListener;
    private ValueEventListener denyDownloadListener;
    private Query fileAlertQuery;
    private Query fileDownloadQuery;
    private Query denyDownloadQuery;

    //WeakReference
    public static WeakReference<ChatFragment> weakActivity;

    //FIREBASE STORAGE CLASS
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage
            .getReferenceFromUrl( Constants.URL_STORAGE_REFERENCE )
            .child( Constants.FOLDER_STORAGE_IMG );

    //ACTIVITY LAUNCHER METHOD
    private final ActivityResultLauncher<String> fileResultLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            selectedImageUri -> {
                // Handle the returned Uri
                if (selectedImageUri != null){
                    sendFileFirebase(storageRef,selectedImageUri);
                }
            });

    //PERMISSION LAUNCHER METHOD
    private final ActivityResultLauncher<String[]> permissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        if (result.containsValue(false)) {
            showPermissionRationale();
        } else if ( shouldShowRequestPermissionRationale( Manifest.permission.WRITE_EXTERNAL_STORAGE ) || shouldShowRequestPermissionRationale( Manifest.permission.READ_EXTERNAL_STORAGE ) ){
            showPermissionRationale();
        } else {
            fileResultLauncher.launch("*/*");
        }
    });
    private final ActivityResultLauncher<String[]> permissionFileViewResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        if (result.containsValue(false)) {
            showPermissionRationale2();
        } else if ( shouldShowRequestPermissionRationale( Manifest.permission.WRITE_EXTERNAL_STORAGE ) || shouldShowRequestPermissionRationale( Manifest.permission.READ_EXTERNAL_STORAGE ) ){
            showPermissionRationale2();
        } else {
            downloadFile();
        }
    });

    /**
     * Method to send file firebase storage.
     * @param storageReference StorageReference.
     * @param file uri of selected image from gallery.
     */
    private void sendFileFirebase(StorageReference storageReference, final Uri file) {
        if (storageReference != null){

            //get the current date and time.
            final String name = DateFormat.format("yyyy-MM-dd(hh:mm:ss)", new Date()).toString();

            //now we will name the file
            StorageReference storageRef = storageReference.child(name+"_file");

            //here we start the upload task.
            UploadTask uploadTask = storageRef.putFile(file);

            uploadTask.addOnFailureListener(requireActivity(), e -> {
                FragmentLoadActivity.get().toast(getString(R.string.file_upload_failed)
                        , Toast.LENGTH_SHORT);
                Log.e(TAG,"Error :" + e.toString());
            }).addOnSuccessListener(requireActivity(), taskSnapshot -> {
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                //noinspection StatementWithEmptyBody
                while( !uri.isComplete() );
                Uri url = uri.getResult(); //here we get download url

                //now we have get the uploaded file url. after getting the url
                //we will now push the url and other data to model class.
                assert url != null;
                FileModel fileModel = new FileModel(
                        fileType( file ), //file extension
                        url.toString(), //file url
                        name, //file name
                        fileSize( file ) //file size
                );
                sendFileToDatabase( sendFileModel(fileModel) );
            });
        }
    }

    private String fileSize(Uri file) {

        AssetFileDescriptor fileDescriptor = null;
        try { fileDescriptor = context.getContentResolver().openAssetFileDescriptor(file,"r"); }
        catch (FileNotFoundException e) { e.printStackTrace(); }

        assert fileDescriptor != null;
        long fileSize = fileDescriptor.getLength();
        try { fileDescriptor.close(); } catch (IOException e) { e.printStackTrace(); }

        return String.valueOf(fileSize);
    }

    private String fileType(Uri file) {
        return MimeTypeMap.getFileExtensionFromUrl( file.toString() );
    }

    @Override
    public void onResume() {
        super.onResume();

        if ( context == null || weakActivity == null ) {
            context = requireContext();
            weakActivity = new WeakReference<>(ChatFragment.this);
        }

        //start listening to firebase adapter
        chatAdapter.startListening();

        //create file alert listener
        fileAlertQuery.addValueEventListener(fileAlertListener);
        //create file download listener
        fileDownloadQuery.addValueEventListener(fileDownloadListener);
        //create deny file listener
        denyDownloadQuery.addValueEventListener(denyDownloadListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        //set the tempGifMessage to null
        selected_gif_url = null;
        //stop listening to firebase adapter
        chatAdapter.stopListening();
        //remove the query of file alert listener
        fileAlertQuery.removeEventListener(fileAlertListener);
        //remove the query of file download listener
        fileDownloadQuery.removeEventListener(fileDownloadListener);
        //remove the query of deny download listener
        denyDownloadQuery.removeEventListener(denyDownloadListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //To prevent from memory leaks remove the views
        //unRegister chatAdapter dataObserver
        chatAdapter.unregisterAdapterDataObserver(chatDataObserver);
        //set null to chatDataObserver
        chatDataObserver = null;
        //set null adapter to user chat recycler view
        user_chat_recycler_view.setAdapter(null);
        //set the user chat recycler view to null
        user_chat_recycler_view = null;
        //set null adapter to gif recycler view
        user_chat_gif_recycler_view.setAdapter(null);
        //set the gif recycler view to null
        user_chat_gif_recycler_view = null;
        //set null to gif image url list
        imageUrlList = null;
        //Linear Layout to null
        user_chat_gif_view = null;
        tag_message_view = null;
        //user name text-view
        user_chat_text_view = null;
        //text view to null
        tag_message_text_view = null;
        //set the button to null
        close_tag_message_view = null;
        custom_toolbar_back_button = null;
        add_person_to_contact_button = null;
        send_file_button = null;
        send_message_button = null;
        close_gif_view_button = null;
        search_gif_button = null;
        send_gif_button = null;
        //set edit-text to null
        search_gif_edit_text = null;
        write_message_edit_text = null;
        //set progress bar to null
        download_file_progress_bar = null;
        loading_gif_progress_bar = null;
        //set is reply to false
        selected_tagged_message = null;

        //Set the context to null
        context = null;
        //set the weak activity to null
        weakActivity = null;

        //delete the download file if it exits
        if (localFile != null) {
            boolean isDeleted = localFile.delete();
            if (isDeleted)
                FragmentLoadActivity.get().toast(
                        getString(R.string.download_filed_deleted), Toast.LENGTH_LONG);
            else
                FragmentLoadActivity.get().toast(
                        getString(R.string.error_code_282), Toast.LENGTH_SHORT);
        }

        //if downloading task is open then cancel the downloading task
        //as we don't want background downloads
        if ( downloadReference != null ) {
            downloadReference.getStream().cancel();
            downloadReference = null;
            isActiveDownload = false;
        }
    }

    public ChatFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize context
        context = requireContext();
        //weakActivity initializing
        weakActivity = new WeakReference<>(ChatFragment.this);
    }

    public static ChatFragment get() {
        return weakActivity.get();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_chat_screen, container, false);

        if (notificationKey != null) {
            Log.d(TAG, notificationKey);
        } else {
            Log.d(TAG, "notification key was null");
        }
        FirebaseMessaging.getInstance().subscribeToTopic(notificationKey);

        //Initialize custom toolbar back button with click listener
        custom_toolbar_back_button = view.findViewById(R.id.custom_toolbar_back_button);
        custom_toolbar_back_button.setOnClickListener(this);

        //Initialize add person to contact button with click listener
        add_person_to_contact_button = view.findViewById(R.id.add_person_to_contact_button);
        add_person_to_contact_button.setOnClickListener(this);

        //Initialize send file button with click listener
        send_file_button = view.findViewById(R.id.send_file_button);
        send_file_button.setOnClickListener(this);

        download_file_progress_bar = view.findViewById(R.id.file_download_progress_bar);

        //Initialize loading gif progress bar
        loading_gif_progress_bar = view.findViewById(R.id.loading_gif_progress_bar);

        //Initialize the user name text-view
        user_chat_text_view = view.findViewById(R.id.user_chat_screen_user_name);
        user_chat_text_view.setOnClickListener(this);

        //Load the user-name in toolbar
        user_chat_text_view.setText(other_user_name);

        //Initialize the user profile-photo image-view
        ImageView user_chat_profile_photo = view.findViewById(R.id.user_chat_screen_user_profile_photo);

        //Load the profile photo in image-view
        //TODO app is crashing on backPress sometimes replace context with requireContext
        Glide.with( requireContext() ).load( other_user_avatar )
                .apply(RequestOptions.circleCropTransform()).into(user_chat_profile_photo);

        //Initialize write message edit text
        write_message_edit_text = view.findViewById(R.id.write_message_edit_text);

        //Initialize send message button
        send_message_button = view.findViewById(R.id.send_message_button);
        //Initialize click listener for send message button
        send_message_button.setOnClickListener(this);

        //Initialize reply message view
        tag_message_view = view.findViewById(R.id.tag_message_view);
        //Initialize reply message text view
        tag_message_text_view = view.findViewById(R.id.tag_message_text_view);
        //Initialize close tag message view
        close_tag_message_view = view.findViewById(R.id.close_tag_message_view);
        close_tag_message_view.setOnClickListener(this);
        tag_message_text_view.setMovementMethod(ScrollingMovementMethod.getInstance());

        //Initialize user chat gif view
        user_chat_gif_view = view.findViewById(R.id.user_chat_gif_view);

        //Initialize user chat search gif edit-text
        search_gif_edit_text = view.findViewById(R.id.search_gif_edit_text);

        //Initialize the close gif view button
        close_gif_view_button = view.findViewById(R.id.close_gif_view_button);
        close_gif_view_button.setOnClickListener(this);

        //Initialize the search gif button with click listener
        search_gif_button = view.findViewById(R.id.search_gif_button);
        search_gif_button.setOnClickListener(this);

        //Initialize send gif button with click listener
        send_gif_button = view.findViewById(R.id.send_gif_button);
        send_gif_button.setOnClickListener(this);

        //Initialize the gif recycler-view with gridLayout manager
        user_chat_gif_recycler_view = view.findViewById(R.id.user_chat_gif_recycler_view);
        user_chat_gif_recycler_view.setLayoutManager(new GridLayoutManager(context, 2));

        //Initialize the chats recycler view
        user_chat_recycler_view = view.findViewById(R.id.user_chat_screen_recycler_view);

        if ( chat_room_ID != null ) {
            //Initialize linear layout manager
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setStackFromEnd(true);

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child( Constants.DB_CR + "/" + chat_room_ID );

            FirebaseRecyclerOptions<ChatModel> options = new FirebaseRecyclerOptions.Builder<ChatModel>()
                    .setQuery(query, ChatModel.class).setLifecycleOwner(this).build();

            chatAdapter = new ChatAdapter(options,this);

            chatDataObserver = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    int friendlyMessageCount = chatAdapter.getItemCount();
                    int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                        user_chat_recycler_view.scrollToPosition(positionStart);
                    }
                }
            };
            chatAdapter.registerAdapterDataObserver(chatDataObserver);

            linearLayoutManager.setStackFromEnd(true);
            user_chat_recycler_view.setLayoutManager(linearLayoutManager);
            user_chat_recycler_view.setAdapter(chatAdapter);
            chatAdapter.startListening();
        } else { closeApp(); }

        checkUserExitInContacts();
        createFileAlertListener();
        fileDownloadListener();
        denyDownloadListener();

        return view;
    }

    @Override
    public void onClick(View v) {

        if (v == user_chat_text_view) {
            //create a object of profile view fragment
            ProfileViewFragment profileViewFragment = new ProfileViewFragment();
            //pass the user profile photo to profile view fragment
            if ( group_vn != null ) {
                profileViewFragment.receivedData( other_user_avatar, group_vn, other_user_name );
            } else {
                profileViewFragment.receivedData( other_user_avatar, other_user_virtual_number, other_user_name );
            }
            //Load the profile info fragment
            FragmentLoadActivity.get().loadFragment( profileViewFragment, "ProfileViewFragment" );
        }

        if (v == close_gif_view_button) {
            gifViewVisibility(false);
        }

        if (v == send_gif_button) {
            gifViewVisibility(true);
        }

        if (v == send_message_button) {
            messageType();
        }

        if (v == send_file_button) {
            permissionResultLauncher.launch(REQUIRED_PERMISSIONS);
        }

        if (v == custom_toolbar_back_button) {
            FragmentLoadActivity.get().loadFragment(new RCSFragment(), "RCSFragment");
        }

        if (v == add_person_to_contact_button) {
            addUserToContacts();
        }

        if (v == close_tag_message_view) {
            tagMessageView(false, "");
        }
    }

    private void showPermissionRationale() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder( requireContext() );
        dialogBuilder.setTitle( getString( R.string.give_permission_title ) );
        dialogBuilder.setMessage( getString( R.string.give_permission_summary ) );
        dialogBuilder.setPositiveButton(R.string.allow_permissions, (dialog, which) -> {
            dialog.dismiss();
            permissionResultLauncher.launch( REQUIRED_PERMISSIONS );
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
    }

    private void showPermissionRationale2() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder( requireContext() );
        dialogBuilder.setTitle( getString( R.string.give_permission_title ) );
        dialogBuilder.setMessage( "To download file please give permissions. Click ALLOW to give permissions" );
        dialogBuilder.setPositiveButton(R.string.allow_permissions, (dialog, which) -> {
            dialog.dismiss();
            permissionFileViewResultLauncher.launch( REQUIRED_PERMISSIONS );
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        dialogBuilder.show();
    }

    private void addUserToContacts() {
        RCModel rcModel;

        if ( group_vn != null ) {
            //group exists
            rcModel = new RCModel(
                    other_user_name,
                    other_user_avatar,
                    other_user_virtual_number,
                    group_vn,
                    Calendar.getInstance().getTime().getTime()+"",
                    chat_room_ID,
                    notificationKey,
                    null
            );
        } else {
            //group doesn't exists its an normal contact
            rcModel = new RCModel(
                    other_user_name,
                    other_user_avatar,
                    other_user_virtual_number,
                    null,
                    Calendar.getInstance().getTime().getTime()+"",
                    chat_room_ID,
                    notificationKey,
                    null
            );
        }
        databaseReference.child( Constants.CONTACTS_DB + "/" + Constants.current_user_virtual_number)
                .child(other_user_virtual_number).setValue(rcModel)
                .addOnSuccessListener(unused ->
                        add_person_to_contact_button.setVisibility(View.GONE));
    }

    private String msg = null;
    private void messageType() {
        //check if write message edit text contains text or not
        if ( !write_message_edit_text.getText().toString().isEmpty() ) {
            msg = write_message_edit_text.getText().toString();
            if (selected_tagged_message != null) {
                //send the tagged message
                sendMessage( sendTagMessageModel( write_message_edit_text.getText().toString() ) );
            } else {
                //send the normal message
                sendMessage( sendMessageModel( write_message_edit_text.getText().toString() ) );
            }
            updateView();
        } else {
            Log.e(TAG,"onError: write message edit text was null");
        }
    }

    public void sendGif(ChatModel chatModel) {
        if ( chat_room_ID != null ) {
            databaseReference.child( Constants.DB_CR ).child( chat_room_ID )
                    .push().setValue( chatModel );

            if ( group_vn != null ) {
                groupSenderPush( groupSenderModel(), 1 );
                getGroupMembers();
            } else {
                senderPush( senderModel() );
                receiverPush( receiverModel(), 1 );
            }

            updateView();
        } else {
            closeApp();
        }
    }

    private ChatModel sendMessageModel(String message) {
        return new ChatModel(
                Constants.current_user_virtual_number,
                message,
                null,
                null,
                Calendar.getInstance().getTime().getTime()+"",
                null
        );
    }

    public ChatModel sendGifModel() {
        return new ChatModel(
                Constants.current_user_virtual_number,
                null,
                null,
                selected_gif_url,
                Calendar.getInstance().getTime().getTime()+"",
                null
        );
    }

    private ChatModel sendTagMessageModel(String message) {
        return new ChatModel(
                Constants.current_user_virtual_number,
                message,
                selected_tagged_message,
                null,
                Calendar.getInstance().getTime().getTime()+"",
                null
        );
    }

    private RCModel senderModel() {
        return new RCModel(
                other_user_name,
                other_user_avatar,
                other_user_virtual_number,
                null,
                Calendar.getInstance().getTime().getTime()+"",
                chat_room_ID,
                notificationKey,
                null
        );
    }

    private RCModel groupSenderModel() {
        return new RCModel(
                other_user_name,
                other_user_avatar,
                other_user_virtual_number,
                group_vn,
                Calendar.getInstance().getTime().getTime()+"",
                chat_room_ID,
                notificationKey,
                null
        );
    }

    private void getGroupMembers() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference().child( Constants.GRP_INFO + "/" + group_vn );

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for ( DataSnapshot dataSnapshot : snapshot.getChildren() ) {
                    String members = dataSnapshot.getKey();

                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add( members );

                    for (int i = 0; i<arrayList.size(); i++)
                    {
                        String m = arrayList.get(i);
                        databaseReference.child( Constants.DB_MAIN )
                                .child( m )
                                .child( group_vn )
                                .setValue( groupSenderModel() );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
        reference.addListenerForSingleValueEvent( listener );
    }

    private RCModel receiverModel() {
        return new RCModel(
                Constants.current_user_name,
                Constants.current_user_avatar,
                Constants.current_user_virtual_number,
                null,
                Calendar.getInstance().getTime().getTime()+"",
                chat_room_ID,
                notificationKey,
                null
        );
    }

    private void senderPush(RCModel rcModel) {
        if ( Constants.current_user_virtual_number != null && other_user_virtual_number !=null ) {
            databaseReference.child(Constants.DB_MAIN)
                    .child(Constants.current_user_virtual_number)
                    .child(other_user_virtual_number)
                    .setValue(rcModel);
        } else {
            closeApp();
        }
    }

    private void receiverPush(RCModel rcModel, int type) {
        if ( Constants.current_user_virtual_number != null && other_user_virtual_number !=null ) {
            databaseReference.child(Constants.DB_MAIN)
                    .child(other_user_virtual_number)
                    .child(Constants.current_user_virtual_number)
                    .setValue(rcModel);

            if (type == 1) { //It is gif handle notification logic
                Log.d(TAG, "GIF");
                new Thread(() -> sendNotification(
                        Constants.current_user_name, "Sent you GIF", notificationKey)
                ).start();
            } else if (type == 2) { //It is normal message handle notification logic
                Log.d(TAG, "MSG");
                new Thread(() -> sendNotification(
                        Constants.current_user_name, msg, notificationKey)
                ).start();
            } else if (type == 3) { //It is a file handle notification logic
                Log.d(TAG, "File");
                new Thread(() -> sendNotification(
                        Constants.current_user_name, "Sent you file", notificationKey)
                ).start();
            }
        } else {
            closeApp();
        }
    }

    private void groupSenderPush(RCModel rcModel, int type) {
        if ( group_vn != null ) {
            databaseReference.child(Constants.DB_MAIN)
                    .child(Constants.current_user_virtual_number)
                    .child( group_vn )
                    .setValue(rcModel);

            if (type == 1) { //It is gif handle notification logic
                Log.d(TAG, "GIF");
                new Thread(() -> sendNotification(
                        other_user_name, "Sent you GIF", notificationKey)
                ).start();
            } else if (type == 2) { //It is normal message handle notification logic
                Log.d(TAG, "MSG");
                new Thread(() -> sendNotification(
                        other_user_name, msg, notificationKey)
                ).start();
            } else if (type == 3) { //It is a file handle notification logic
                Log.d(TAG, "File");
                new Thread(() -> sendNotification(
                        other_user_name, "Sent you file", notificationKey)
                ).start();
            }
        } else {
            closeApp();
        }
    }

    private ChatModel sendFileModel(FileModel fileModel) {
        return new ChatModel(
                Constants.current_user_virtual_number,
                null,
                null,
                null,
                Calendar.getInstance().getTime().getTime()+"",
                fileModel
        );
    }

    public void downloadFile() {
        if ( selected_file_url != null ) {
            //show the progress bar when download starts
            if (download_file_progress_bar != null) {
                download_file_progress_bar.setVisibility(View.VISIBLE);
                download_file_progress_bar.setMax(100);
            }
            //initialize storage reference
            downloadReference = storage.getReferenceFromUrl(selected_file_url);
            //downloadReference = storageReference;

            //creates the unique name of the file
            final String name = DateFormat.format("yyyy-MM-dd(hh:mm:ss)", new Date()).toString();

            File rootPath = new File(Environment.getExternalStorageDirectory(), "nikApp");
            //if rootPath doesn't exits then create the folder named as
            //nikApp
            if(!rootPath.exists()) {
                boolean mkdirs = rootPath.mkdirs();
                if (mkdirs) { Log.i(TAG,"success to create directory"); }
                else { Log.e(TAG,"failed to create directory"); }
            }
            //Initialize localFile with the file name and its extension
            localFile = new File(rootPath,name + "." + selected_file_type);

            downloadReference.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Log.i(TAG,"file downloaded" +localFile.toString());

                //remove the download request so that it can handle new download
                //request to
//                if (isActiveDownload) {
                    //hide the progress bar when downloads get completes
                    download_file_progress_bar.setVisibility(View.GONE);

                    openFile(); //open the file on download success
                    //removeDownload();
//                }
            }).addOnFailureListener(exception ->
                    Log.e(TAG,"file not downloaded" +exception.toString())
            ).addOnProgressListener(taskSnapshot -> {
                if (download_file_progress_bar != null) {

                    download_file_progress_bar.setProgress(0);
                    //calculating progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                            taskSnapshot.getTotalByteCount();
                    //displaying percentage in progress dialog
                    Log.e(TAG,"Uploaded " + ((int) progress) + "%...");
                    //set the progress value in progress bar
                    download_file_progress_bar.post(() ->
                            download_file_progress_bar.setProgress((int) progress));

                }
            });
        } else { closeApp(); }
    }

    /*
    Method to open downloaded File
     */
    private void openFile() {
        try { FileOpen.openFile(context,localFile); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void permissions() {
        permissionFileViewResultLauncher.launch(REQUIRED_PERMISSIONS);
    }

    /*
    Method to send message to database
     */
    public void sendMessage(ChatModel chatModel) {
        if ( chat_room_ID != null ) {
            databaseReference.child( Constants.DB_CR ).child( chat_room_ID )
                    .push().setValue( chatModel );

            if ( group_vn != null ) {
                groupSenderPush( groupSenderModel(), 2 );
                getGroupMembers();
            } else {
                senderPush( senderModel() );
                receiverPush( receiverModel(), 2 );
            }
        } else { closeApp(); }
    }

    /*
    Method to send file to database
     */
    private void sendFileToDatabase(ChatModel chatModel) {
        if ( chat_room_ID != null ) {
            databaseReference.child( Constants.DB_CR ).child( chat_room_ID )
                    .push().setValue(chatModel);

            if ( group_vn != null ) {
                groupSenderPush( groupSenderModel(), 3 );
                getGroupMembers();
            } else {
                senderPush( senderModel() );
                receiverPush( receiverModel(),3 );
            }
        } else { closeApp(); }
    }

    public void tagMessageView(boolean visibility, String tMessage) {
        if (visibility) {
            tag_message_view.setVisibility(View.VISIBLE);
            tag_message_text_view.setText(tMessage);
        } else {
            //set the tempSaveTagMessage to null
            selected_tagged_message = null;
            //change visibility
            tag_message_view.setVisibility(View.GONE);
            //set text to null
            tag_message_text_view.setText(null);
        }
    }

    public void gifViewVisibility(boolean visibility) {
        if (visibility) {
            user_chat_gif_view.setVisibility(View.VISIBLE);

            loadTrendingGifInView();
            initializeSearchGifButton();
        } else {
            user_chat_gif_view.setVisibility(View.GONE);
            user_chat_gif_recycler_view.setVisibility(View.GONE);
            //set the tempGifMessage to null
            selected_gif_url = null;
        }
    }

    private void initializeSearchGifButton() {
        search_gif_button.setOnClickListener(v -> {
            //show the progress bar
            loading_gif_progress_bar.setVisibility(View.GONE);

            String searchValue = search_gif_edit_text.getText().toString();
            loadSearchedGifsInView(searchValue);
        });
    }

    private void loadTrendingGifInView() {
        //show the recycler view
        user_chat_gif_recycler_view.setVisibility(View.VISIBLE);
        //hide the progress bar
        loading_gif_progress_bar.setVisibility(View.GONE);

        Call<TrendingGifResponse> call = ApiClient.getInstance(context).getTrending(ApiClient.getServiceIds(context),10,"");

        call.enqueue(new Callback<TrendingGifResponse>() {
            @Override
            public void onResponse(@NonNull Call<TrendingGifResponse> call, @NonNull Response<TrendingGifResponse> response) {
                String[] name = new String[TRENDING_MAX_GIF_STORE];
                String res = response.toString();

                imageUrlList = new ArrayList<>();
                for (int i = 0; i < res.length(); i++) {
                    if (i == 10) {
                        //If 10 gifs url are fetched then exit the for loop with break statement;
                        break;
                    } else {
                        //get the gifs result and store it in result variable
                        assert response.body() != null;
                        Result result = response.body().getResults().get(i);
                        //store the fetched gif url in string array form
                        name[i] = result.getMedias().get(0).get(MediaCollectionFormats.GIF_TINY).getUrl();

                        //Initialize the gifModel
                        GifModel imageUrl = new GifModel();
                        //pass the image url to gifModel
                        imageUrl.setImageUrl(name[i]);
                        //Add the imageUrl i.e, gifModel to arrayList
                        imageUrlList.add(imageUrl);
                    }
                }
                //Initialize the GifAdapter and pass the context and ArrayList to it
                gifAdapter = new GifAdapter(context, imageUrlList,
                        (tagMessage, gifURL, fileURL, fileType) -> selected_gif_url = gifURL
                );
                //set the gifAdapter to the recycler view to display data
                user_chat_gif_recycler_view.setAdapter(gifAdapter);
            }

            @Override
            public void onFailure(@NonNull Call<TrendingGifResponse> call, @NonNull Throwable t) { }
        });
    }

    private void loadSearchedGifsInView(String searchValue) {
        //show the recycler view
        user_chat_gif_recycler_view.setVisibility(View.VISIBLE);
        //hide the progress bar
        loading_gif_progress_bar.setVisibility(View.GONE);

        Call<GifsResponse> call = ApiClient.getInstance(requireActivity()).search(
                ApiClient.getServiceIds(requireActivity()),searchValue,10, "");

        call.enqueue(new WeakRefCallback<Activity, GifsResponse>(requireActivity()) {
            @Override
            public void success(@NonNull Activity activity, @Nullable GifsResponse gifsResponse) {
                assert gifsResponse != null;

                String[] name = new String[MAX_GIF_STORE];
                String res = gifsResponse.toString();

                imageUrlList = new ArrayList<>();
                for (int i = 0; i < res.length(); i++) {
                    if (i == 10) {
                        //If 10 gifs url are fetched then exit the for loop with break statement;
                        break;
                    } else {
                        //get the gifs result and store it in result variable
                        Result result = gifsResponse.getResults().get(i);
                        //store the fetched gif url in string array form
                        name[i] = result.getMedias().get(0).get(MediaCollectionFormats.GIF_TINY).getUrl();

                        //Initialize the gifModel
                        GifModel imageUrl = new GifModel();
                        //pass the image url to gifModel
                        imageUrl.setImageUrl(name[i]);
                        //Add the imageUrl i.e, gifModel to arrayList
                        imageUrlList.add(imageUrl);
                    }
                }
                //Initialize the GifAdapter and pass the context and ArrayList to it
                GifAdapter gifAdapter = new GifAdapter(context, imageUrlList,
                        (tagMessage, gifURL, fileURL, fileType) -> selected_gif_url = gifURL
                );
                //set the gifAdapter to the recycler view to display data
                user_chat_gif_recycler_view.setAdapter(gifAdapter);
            }

            @Override
            public void failure(@NonNull Activity activity, BaseError baseError) {
                //TODO update the view on failed to fetch the gif
                Log.e(TAG,"onError: failed to fetch gifs");
            }
        });
    }

    @Override
    public void data(String tagMessage, String gifURL, String fileURL, String fileType) {
        selected_tagged_message = tagMessage;
        //if fileURL is null then return and don't do anything
        if (fileURL == null && fileType == null) return;
        //fileURL is not null then store fileURL in selected_file_url variable
        selected_file_url = fileURL;
        selected_file_type = fileType;
    }

    /**
     * Data received on-click of recent chat
     * @param name of the user
     * @param number of the user
     */
    protected void receivedData(String name, String avatar, String number, String groupVn, String crId, String Notification_key) {
            other_user_name = name;
            other_user_avatar = avatar;
            other_user_virtual_number = number;
            group_vn = groupVn;
            chat_room_ID = crId;
            notificationKey = Notification_key;
            if (Notification_key != null) {
                Log.d(TAG, Notification_key);
            } else {
                Log.d(TAG, "notification key was null");
            }
    }

    private void checkUserExitInContacts() {
        //user doesn't exit in contact list so show the add contact button in chat view
        ValueEventListener contactExitListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot user = snapshot.child(
                        Constants.CONTACTS_DB + "/" +
                                Constants.current_user_virtual_number + "/" + other_user_virtual_number);

                if (!user.exists()) {
                    //user doesn't exit in contact list so show the add contact button in chat view
                    if (add_person_to_contact_button != null && group_vn == null) {
                        add_person_to_contact_button.setVisibility(View.VISIBLE);
                    } else {
                        add_person_to_contact_button.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        databaseReference.addListenerForSingleValueEvent(contactExitListener);
    }

    private void updateView() {
        //hide the gif view
        gifViewVisibility(false);
        //set the message edit-text to null
        write_message_edit_text.setText(null);
        //set the tagged-message view visibility to false
        tagMessageView(false,"");
        //set gif_url to null
        selected_gif_url = null;
        //set tagged_message to null
        selected_tagged_message = null;
    }

    public void createAlertToFileSender() {

        if ( group_vn != null ) {
            isActiveDownload = true;
            downloadFile();
        } else {
            databaseReference.child( Constants.REQ_FILE )
                    .child( other_user_virtual_number )
                    .child( Constants.current_user_virtual_number )
                    .setValue( "true" );
        }
    }

    private void createFileAlertListener() {
        fileAlertQuery = databaseReference.child( Constants.REQ_FILE )
                .child( Constants.current_user_virtual_number )
                .child( other_user_virtual_number );

        fileAlertListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ( snapshot.exists() ) {
                    showDialogFileAlert();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
    }

    private void fileDownloadListener() {
        fileDownloadQuery = databaseReference.child( Constants.ALL_FILE )
                .child( Constants.current_user_virtual_number )
                .child( other_user_virtual_number );

        fileDownloadListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ( snapshot.exists() ) { showDialogDownloadFile(); }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
    }

    private void denyDownloadListener() {
        denyDownloadQuery = databaseReference.child( Constants.DENY_FILE )
                .child( Constants.current_user_virtual_number )
                .child( other_user_virtual_number );

        denyDownloadListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ( snapshot.exists() ) { showDenyFileDialog(); }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
    }

    private void showDialogFileAlert() {
        if ( context == null ) return;
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder( context );
        dialogBuilder.setTitle( other_user_name.toUpperCase() );
        dialogBuilder.setMessage(
                getString(R.string.file_alert_summary3) + " " +
                        other_user_name.toUpperCase() + " " +
                        getString(R.string.file_alert_summary4)
        );
        dialogBuilder.setPositiveButton(getString(R.string.allow), (dialog, which) -> {
            dialog.dismiss();

            allowRequest();
        });
        dialogBuilder.setNegativeButton("Deny", (dialog, which) -> {
            dialog.dismiss();

            denyRequest();
        });
        dialogBuilder.show();
    }

    private void showDialogDownloadFile() {
        if ( context == null ) return;
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder( context );
        dialogBuilder.setTitle( other_user_name.toUpperCase() );
        dialogBuilder.setMessage( getString( R.string.file_download_summary ) );
        dialogBuilder.setPositiveButton( getString(R.string.download_file), (dialog, which) -> {
            dialog.dismiss();

            isActiveDownload = true;
            downloadFile();
        });
        dialogBuilder.setNegativeButton("Deny", (dialog, which) -> {
            dialog.dismiss();

            denyRequest();
        });
        dialogBuilder.show();
    }

    private void showDenyFileDialog() {
        if ( context == null ) return;

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder( context );
        dialogBuilder.setTitle( other_user_name.toUpperCase() );
        dialogBuilder.setMessage( getString( R.string.file_download_summary2 ) );
        dialogBuilder.setPositiveButton( getString(R.string.request_again), (dialog, which) -> {
            dialog.dismiss();
            cancelRequest();
            createAlertToFileSender();
        });
        dialogBuilder.setNegativeButton("Deny", (dialog, which) -> {
            dialog.dismiss();

            cancelRequest();
        });
        dialogBuilder.show();
    }

    private void denyRequest() {
        //remove the requesting file child so that user can again listen
        //to file request.
        databaseReference.child( Constants.REQ_FILE )
                .child( Constants.current_user_virtual_number )
                .child( other_user_virtual_number )
                .removeValue();

        //add a deny child in database so that file requesting user
        //get a message that user user has denied its file downloading
        //request
        databaseReference.child( Constants.DENY_FILE )
                .child( other_user_virtual_number )
                .child( Constants.current_user_virtual_number )
                .setValue( "true" );
    }

    private void allowRequest() {
        //if user allow the file request then create a child of
        // allowingFile so that file requester user can download the file
        databaseReference.child( Constants.ALL_FILE )
                .child( other_user_virtual_number )
                .child( Constants.current_user_virtual_number )
                .setValue( "true" );

        //user has allowed the file then remove the requesting
        //file child
        databaseReference.child( Constants.REQ_FILE )
                .child( Constants.current_user_virtual_number )
                .child( other_user_virtual_number )
                .removeValue();
    }

    private void removeDownload() {
        //after download file has been success then remove the
        //download child so that user can download other files to
        databaseReference.child( Constants.ALL_FILE )
                .child( Constants.current_user_virtual_number )
                .child( other_user_virtual_number )
                .removeValue();
    }

    private void cancelRequest() {
        //if user denys the request of file requester and then file
        //requester user wants to cancel the file request then we will
        //remove the deny child
        databaseReference.child( Constants.DENY_FILE )
                .child( Constants.current_user_virtual_number )
                .child( other_user_virtual_number )
                .removeValue();
    }

    private void closeApp() {
        Log.e(TAG,"unexpected error was occurred so app was closed with system exit code 0");
        System.exit(0);
    }

    private static final String SERVER_KEY = "AAAAfQTKWBw:APA91bEJ9lHfcrqrYLwC5inXBM47YG4TV6zgJ0YP1S_ZIszl-C0iAuT12YmeOWlSIpIkbKYiYXFFSkJqYS0M-bi408_ICm4r5DjrsanogmET93D53lLDc06hws3F_Xu5RPi-ad6qnMNt";
    //see https://stackoverflow.com/a/62447173/9893403
    private void sendNotification(String title, String message, String crID) {
        JSONObject jPayload = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            Log.d(TAG, "sending notification...");

            data.put("title", title);
            data.put("body", message);

            //to subscribed topics
            jPayload.put("to", "/topics/" + crID);

            jPayload.put("priority", "high");
            jPayload.put("time_to_live",60);
            jPayload.put("data", data);
            //jPayload.put("notification", data);

            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jPayload.toString().getBytes());

            // Read FCM response.
            InputStream inputStream = conn.getInputStream();
            //noinspection unused
            final String resp = convertStreamToString(inputStream);

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }
}