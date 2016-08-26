package io.github.scola.qart;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import free6om.research.qart4j.QArt;
import io.github.scola.cuteqr.CuteR;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = "MainActivity";
    private final static int REQUEST_PICK_IMAGE = 1;

    private boolean pickImage;

    private ImageView pickPhoto;

    private MenuItem convertMenu;
    private MenuItem addTextMenu;

    private MenuItem shareMenu;
    private MenuItem saveMenu;
    private LinearLayout editTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickPhoto = (ImageView)findViewById(R.id.pick_img);
        editTextView = (LinearLayout)findViewById(R.id.text_group);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        convertMenu = menu.findItem(R.id.convert_qr);
        addTextMenu = menu.findItem(R.id.add_txt);

        shareMenu = menu.findItem(R.id.share_qr);
        saveMenu = menu.findItem(R.id.save_qr);

        hideQrMenu();
        hideSaveMenu();
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

//            CuteR.Product("http://www.chinuno.com#adffffffffffddddddddddddddddddddfdfdfdfasdfasfasfafasfafasfaasdfadfadfafadfadffffffffffddddddddddddddddddddfdfdfdfasdfasfasfafasfafasfaasdfadfadfafadfadffffffffffddddddddddddddddddddfdfdfdfasdfasfasfafasfafasfaasdfadfadfafadfadf",
//                    android.os.Environment.getExternalStorageDirectory().toString() + "/Pictures/wushaozheng_no_align-output.png",
//                    android.os.Environment.getExternalStorageDirectory().toString() + "/Pictures/wushaozheng.jpeg");
            Log.d(TAG, "Qart end");
            return true;
        }

        if (id == R.id.launch_gallery) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
        }

        if (id == R.id.add_txt) {
            if (editTextView.getVisibility() == View.INVISIBLE) {
                editTextView.setVisibility(View.VISIBLE);
            } else {
                editTextView.setVisibility(View.INVISIBLE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    pickPhoto.setImageURI(data.getData());
                    pickImage = true;
                    showQrMenu();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void hideQrMenu() {
        if (convertMenu != null && addTextMenu != null) {
            convertMenu.setVisible(false);
            addTextMenu.setVisible(false);
        }
    }

    public void showQrMenu() {
        if (convertMenu != null && addTextMenu != null) {
            convertMenu.setVisible(true);
            addTextMenu.setVisible(true);
        }
    }

    public void hideSaveMenu() {
        if (shareMenu != null && saveMenu != null) {
            shareMenu.setVisible(false);
            saveMenu.setVisible(false);
        }
    }

    public void showSaveMenu() {
        if (shareMenu != null && saveMenu != null) {
            shareMenu.setVisible(true);
            saveMenu.setVisible(true);
        }
    }
}
