package eyecue.qlone_basic_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.eyecue.inapp.BuyCreditActivity;
import com.eyecue.inapp.InappBalance;
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


public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;

//        Intent intent = new Intent(this, BuyCreditActivity.class);
//        Intent intent = new Intent(this, ExportActivity.class);
//        startActivity(intent);

        //google sign in
        mGoogleSignInClient = buildGoogleSignInClient();


        Button tmpBtn = findViewById(R.id.create_file_btn);
        tmpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                createFileInAppFolder();
                Intent signInIntent = InappBalance.getInstance(ctx).getSignInIntent();
                startActivityForResult(signInIntent, 1002);

            }
        });
        tmpBtn = findViewById(R.id.check_for_file_btn);
        tmpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, BuyCreditActivity.class);
//                Intent intent = new Intent(this, ExportActivity.class);
                startActivity(intent);

            }
        });
        tmpBtn = findViewById(R.id.read_file_btn);
        tmpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                InappBalance.getInstance(ctx).logIn();
                InappBalance.getInstance(ctx).getValue();
            }
        });
        tmpBtn = findViewById(R.id.write_file_btn);
        tmpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                writeFileContent();
                InappBalance.getInstance(ctx).setValue(5);
            }

        });

//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, 1002);

//        InappBalance.getInstance(ctx).logIn();

    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE , Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
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
            getDriveFolder();


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("oooOri", "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1002) {

//            // The Task returned from this call is always completed, no need to attach
//            // a listener.
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            handleSignInResult(task);
            InappBalance.getInstance(this).logIn(data);
        }
    }

    private void writeFileContent () {
        mValue += 1;
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
        }).addOnSuccessListener(this,
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("oooOri", " new value saved");

                                mDriveClient.requestSync();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
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
                .addOnSuccessListener(this,
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
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("oooOri", "Error retrieving files", e);
                    }
                });
    }

    private void getDriveFolder() {

        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        appFolderTask.addOnSuccessListener(this, new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {
                mFolder = driveFolder;
                Log.d("oooOri", "found drive folder");
            }
        }).addOnFailureListener(this, new OnFailureListener (){
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("oooOri", "Unable get drive folder", e);
                finish();
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
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                Log.e("oooOri", "file created");

                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("oooOri", "Unable to create file", e);
                        finish();
                    }
                });
    }



}
