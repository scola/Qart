package io.github.scola.qart;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import free6om.research.qart4j.QArt;
import io.github.scola.cuteqr.CuteR;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d(TAG, "Qart begin");
//            QArt.main(new String[]{
//                    "-i", android.os.Environment.getExternalStorageDirectory().toString() + "/Pictures/panda.jpg",
//                    "-o", android.os.Environment.getExternalStorageDirectory().toString() + "/Pictures/sample-output.png",
//                    "-u", "http://www.imdb.com/title/tt2267968/",
//                    "-w", "660",
//                    "-h", "978",
//                    "--mr", "147",
//                    "--mb", "334",
//                    "-z", "342",
//                    "-v", "16",
//                    "-q", "1",
//                    "--cw", "EFFFFFFF"});

            CuteR.Product("http://www.chinuno.com",
                    android.os.Environment.getExternalStorageDirectory().toString() + "/Pictures/wushaozheng_no_align-output.png",
                    android.os.Environment.getExternalStorageDirectory().toString() + "/Pictures/wushaozheng.jpeg");
            Log.d(TAG, "Qart end");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
