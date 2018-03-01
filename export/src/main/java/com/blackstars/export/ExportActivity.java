package com.blackstars.export;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ExportActivity extends Activity {

    Button mSketchfabBtn;
    Button mShareBtn;
    Activity ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_export);
        ctx = this;
        mSketchfabBtn = (Button)findViewById(R.id.sketchfab_export_btn);
        mSketchfabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, Sketchfab.class);
                startActivity(intent);
            }
        });

        mShareBtn = (Button)findViewById(R.id.obj_export_btn);
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String path = Environment.getExternalStorageDirectory() + File.separator + "temp_ori" + File.separator + "export.zip";
                File f = new File(ctx.getFilesDir(), "monster.zip");
                try {

                    InputStream is = getAssets().open("model.zip");
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();


                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(buffer);
                    fos.close();
                } catch (Exception e) { throw new RuntimeException(e); }

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                long size = f.length();
                Log.d("ooOri","file size" + size);
                Uri modelUri = FileProvider.getUriForFile(ctx,ctx.getApplicationContext().getPackageName() + ".my.package.name.provider", f );
                sharingIntent.setType("application/zip");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, modelUri);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(sharingIntent, "Share image using"));
            }
        });



    }


}
