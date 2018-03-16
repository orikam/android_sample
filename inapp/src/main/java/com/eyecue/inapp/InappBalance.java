package com.eyecue.inapp;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by orikam on 27/01/2018.
 */


public class InappBalance {
    private static InappBalance instance =  null;
    private  GoogleSignInClient mGoogleSignInClient;
    private Task<GoogleSignInAccount> mSignInTask;
    private  Task<DriveFolder> mAppFolderTask;
    private Activity ctx;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private GoogleSignInAccount mSignInAccount;
    private DriveFolder mFolder;
    private DriveFile mFile;
    private int mValue = 0;
    private boolean mLogedIn = false;

    protected InappBalance(Activity context) {
        if(context != null)
            ctx = context;
        mGoogleSignInClient = buildGoogleSignInClient();
    }

    public static InappBalance getInstance(Activity context) {
        if(instance == null) {
            instance = new InappBalance(context);

        }
        if(context != null)
            instance.ctx = context;
        return instance;
    }


    public void logIn(@Nullable Intent data) {
//        Activity actv = new singletonActivity();
//        Intent intent = new Intent(ctx, actv.getClass());
//        ctx.startActivityForResult(intent, 1002);
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        instance.handleSignInResult( task);
    }

    public boolean isLoggedIn(){
        return mLogedIn;
    }
    public int getValue() {
        //getDriveFolder();
        Log.e("oooOri","mValue = " + mValue);
        return mValue;
    }

    public void setValue(int value) {
        mValue += value;
        writeFileContent();
    }

    public Intent getSignInIntent() {
        return instance.mGoogleSignInClient.getSignInIntent();
    }
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE , Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(ctx, signInOptions);
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
//            updateUI(account);

            Log.w("oooOri", "signInResult:success " );
            mSignInAccount = completedTask.getResult();
            mDriveResourceClient = Drive.getDriveResourceClient(ctx, mSignInAccount);
            mDriveClient = Drive.getDriveClient(ctx, mSignInAccount);
            mLogedIn = true;
            getDriveFolder();



        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("oooOri", "signInResult:failed code=" + e.getStatusCode() + " " + e.getMessage());
//            updateUI(null);
        }
//        activity.finish();
    }


    private void writeFileContent () {

        Task<DriveContents> fileTask = mDriveResourceClient.openFile(mFile, DriveFile.MODE_WRITE_ONLY);

        fileTask.continueWithTask(new Continuation<DriveContents, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                DriveContents driveContents = task.getResult();
                try (OutputStream out = driveContents.getOutputStream()) {
                    out.write(Integer.toString(mValue).getBytes());
                }
                // [START commit_content]
                Task<Void> commitTask =
                        mDriveResourceClient.commitContents(driveContents, null);
                // [END commit_content]
                return commitTask;
            }
        }).addOnSuccessListener(ctx,
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("oooOri", " new value saved");

                        mDriveClient.requestSync();
                    }
                })
                .addOnFailureListener(ctx, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("oooOri", "Unable to update contents", e);


                    }
                });

    }

    private  void readFileContent() {
        if (mFile != null) {
            Task<DriveContents> fileTask = mDriveResourceClient.openFile(mFile, DriveFile.MODE_READ_ONLY);

            fileTask
                    .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                        @Override
                        public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                            DriveContents contents = task.getResult();

                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(contents.getInputStream()))) {
                                String line;

                                if((line = reader.readLine()) != null) {
                                    mValue = Integer.parseInt(line);
                                    Log.d("oooOri", "content of file " + line);
                                }
                            }
                            // [END read_as_string]
                            // [END_EXCLUDE]
                            // [START discard_contents]
                            Task<Void> discardTask = mDriveResourceClient.discardContents(contents);
                            // [END discard_contents]
                            return discardTask;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure
                            // [START_EXCLUDE]
                            Log.e("oooOri", "Unable to read contents", e);
                            // [END_EXCLUDE]
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });

        }
    }

    private void checkIfFileFound() {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "xx.yy"))
                .build();

        Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(mFolder, query);
        // END query_children]
        queryTask
                .addOnSuccessListener(ctx,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                Log.e("oooOri", "Success retrieving files:" + metadataBuffer.getCount());
                                if(metadataBuffer.getCount()>=1) {
                                    mFile = metadataBuffer.get(0).getDriveId().asDriveFile();
                                    readFileContent();

                                }
                                else {
                                    createFileInAppFolder();
                                }

//                                mResultsAdapter.append(metadataBuffer);
                            }
                        })
                .addOnFailureListener(ctx, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("oooOri", "Error retrieving files", e);
                    }
                });
    }

    private void getDriveFolder() {

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        Log.e("oooOri","in get drive folder "  + appFolderTask.toString() + "," + ctx.toString());

        appFolderTask.addOnSuccessListener(ctx, new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {
                mFolder = driveFolder;
                Log.d("oooOri", "found drive folder");
                checkIfFileFound();

            }
        }).addOnFailureListener(ctx, new OnFailureListener (){
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("oooOri", "Unable get drive folder", e);

            }
        });
    }

    private void createFileInAppFolder() {
        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write("0");
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("xx.yy")
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();

                        return mDriveResourceClient.createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(ctx,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                Log.e("oooOri", "file created");
                                mFile = driveFile;
                            }
                        })
                .addOnFailureListener(ctx, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("oooOri", "Unable to create file", e);

                    }
                });
    }


//    public static class singletonActivity extends Activity{
//
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            Intent signInIntent = instance.mGoogleSignInClient.getSignInIntent();
//            startActivityForResult(signInIntent, 1002);
//        }
//
//        protected void onActivityResult(int requestCode, int resultCode, Intent data){
//            if (requestCode == 1002) {
//                // The Task returned from this call is always completed, no need to attach
//                // a listener.
//                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                instance.ctx = this;
//                instance.handleSignInResult( task);
//            }
//        }
//    }

}
