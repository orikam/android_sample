package com.blackstars.export;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ExportActivity extends Activity {

    private  String SKETCHFAB_ID = "sketchfab";
    private  String OBJ_ID = "obj";
    Button mSketchfabBtn;
    Button mShareBtn;
    Activity ctx;
    android.support.v7.widget.RecyclerView mPlatforms;
    ExportAdapter adapter;
    List<Item> mPlatformList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        Item item;

        mPlatforms = findViewById(R.id.export_sites);
        mPlatformList = new ArrayList<Item>();
        item = new Item(SKETCHFAB_ID, "sketchfab_logo");
        mPlatformList.add(item);
        item = new Item(OBJ_ID, "shapeways");
        mPlatformList.add(item);
        item = new Item("cgtrader", "cgtrader");
        mPlatformList.add(item);

        LinearLayoutManager MyLayoutManager = new LinearLayoutManager(this);
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        adapter = new ExportAdapter(mPlatformList);
        mPlatforms.setAdapter(adapter);
        mPlatforms.setLayoutManager(MyLayoutManager);
    }

    public void export(String target) {
        if (target.equals(SKETCHFAB_ID)) {
            Intent intent = new Intent(ctx, Sketchfab.class);
            startActivity(intent);
        }
        else if (target.equals(OBJ_ID)) {
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
    }

    public class ExportViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTextView;
        public ImageView image;
        public Button btn;
        public String id;
        public TextView title;
        public ExportViewHolder(View v) {
            super(v);

            image = v.findViewById(R.id.export_cell_image);
            btn = v.findViewById(R.id.export_cell_btn);
            title = v.findViewById(R.id.export_cell_text);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Log.d("oooOri", "button pressed " + id);
                        export(id);
                    }
                    catch (Exception e) {

                    }
                }
            });

        }
    }

    public class ExportAdapter extends RecyclerView.Adapter<ExportViewHolder> {
        List<Item> listInfo;

        public ExportAdapter(List<Item> list) {
            listInfo = list;
        }
        @Override
        public ExportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.export_cell_layout, parent, false);

            ExportViewHolder holder = new ExportViewHolder(view);
            return holder;
        }
        @Override
        public void onBindViewHolder(final ExportViewHolder holder, int position) {
            Item info = listInfo.get(position);

            String yourImageName = info.imageKey;

            InputStream imageStream = null;
            try {
                // get input stream
                int resId= getResources().getIdentifier(yourImageName, "drawable", "eyecue.qlone_basic_android");
//                imageStream  =  ctx.getAssets().open(yourImageName);

                // load image as Drawable
//                Drawable drawable= Drawable.createFromStream(imageStream, null);
//                holder.image.setImageDrawable(drawable);
                holder.image.setImageResource(resId);
//                holder.btn.setText(info.labelKey);
                holder.title.setText(info.labelKey);
                holder.id = info.labelKey;
            }
            catch(Exception ex) {

                return;
            }

        }
        @Override
        public int getItemCount() {
            return listInfo.size();
        }


    }

    public class Item {
        public String labelKey;
        public String imageKey;

        public Item(String label, String imageName) {
            labelKey = label;
            imageKey = imageName;
        }

    }

}
