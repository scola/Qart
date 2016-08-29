package io.github.scola.qart;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dss886.emotioninputdetector.library.EmotionInputDetector;

import java.io.IOException;

import free6om.research.qart4j.QArt;
import io.github.scola.cuteqr.CuteR;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = "MainActivity";
    private final static int REQUEST_PICK_IMAGE = 1;
    private final static String PREF_TEXT_FOR_QR = "text";

    private final static int MAX_INPUT_BITMAP_WIDTH = 720;
    private final static int MAX_INPUT_BITMAP_HEIGHT= 1280;

    private boolean pickImage;

    private ImageView pickPhoto;

    private MenuItem convertMenu;
    private MenuItem addTextMenu;

    private MenuItem shareMenu;
    private MenuItem saveMenu;
    private LinearLayout editTextView;

    private EditText mEditTextView;
    private ImageView qrButton;
    private Button setTextButton;

    private String qrText;
    private Bitmap mOriginBitmap;
    private Bitmap mQRBitmap;

    private EmotionInputDetector mDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickPhoto = (ImageView)findViewById(R.id.pick_img);
        editTextView = (LinearLayout)findViewById(R.id.text_group);
        mEditTextView = (EditText) findViewById(R.id.edit_text);
        qrButton = (ImageView)findViewById(R.id.emotion_button);
        setTextButton = (Button)findViewById(R.id.btn_send);

        mEditTextView.addTextChangedListener(TextWatcherNewInstance());

        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        qrText = sharedPref.getString(PREF_TEXT_FOR_QR, "hello world");

//        mDetector = EmotionInputDetector.with(this)
//                .bindToContent(findViewById(R.id.list))
//                .bindToEditText(mEditTextView)
//                .bindToEmotionButton(qrButton)
//                .build();

        setTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String txt = mEditTextView.getText().toString().trim();
                if (txt.isEmpty() == false) {
                    qrText = txt;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(PREF_TEXT_FOR_QR, qrText);
                    editor.commit();
//                    mEditTextView.setInputType(InputType.TYPE_NULL);
                    View view = MainActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    editTextView.setVisibility(View.INVISIBLE);
                }
            }
        });
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
                if (qrText != null && qrText.isEmpty() == false) {
                    mEditTextView.setText(qrText);
                    mEditTextView.setSelection(qrText.length());
                }
            } else {
                editTextView.setVisibility(View.INVISIBLE);
            }
        }

        if (id == R.id.convert_qr) {
            mQRBitmap = CuteR.Product(qrText, mOriginBitmap);
            pickPhoto.setImageBitmap(mQRBitmap);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
//                    pickPhoto.setImageURI(data.getData());
                    mOriginBitmap = getBitmapFromUri(data.getData());
                    String path = getRealPathFromURI(this, data.getData());
                    convertOrientation(path, mOriginBitmap);
                    pickPhoto.setImageBitmap(mOriginBitmap);
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

    private TextWatcher TextWatcherNewInstance() {
        return new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String text = mEditTextView.getText().toString().trim();
                if (text != null && text.isEmpty() == false) {
                    setTextButton.setClickable(true);
                    setTextButton.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
                } else {
                    setTextButton.setClickable(false);
                    setTextButton.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.reply_button_text_disable));
                }

            }
        };
    }



    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void convertOrientation(String photoPath, Bitmap bitmap) {
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            ExifInterface ei = new ExifInterface(photoPath);
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        } catch (IOException ex) {
            Log.e(TAG, "can not retrieve exif");
        }

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                mOriginBitmap = CuteR.rotateImage(bitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                mOriginBitmap = CuteR.rotateImage(bitmap, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                mOriginBitmap = CuteR.rotateImage(bitmap, 270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                break;
        }
    }

    public Bitmap getBitmapFromUri(Uri imageUri) {
        getContentResolver().notifyChange(imageUri, null);
        ContentResolver cr = getContentResolver();
        Bitmap bitmap;
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, imageUri);
            float scale = Math.min((float)1.0 * MAX_INPUT_BITMAP_WIDTH/bitmap.getWidth(), (float)1.0 * MAX_INPUT_BITMAP_HEIGHT/bitmap.getHeight());
            if (scale < 1) {
                bitmap = CuteR.getResizedBitmap(bitmap, scale, scale);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
