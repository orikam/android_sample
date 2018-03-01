package eyecue.qlone_basic_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.blackstars.export.ExportActivity;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent intent = new Intent(this, BuyCreditActivity.class);
        Intent intent = new Intent(this, ExportActivity.class);
        startActivity(intent);

    }
}
