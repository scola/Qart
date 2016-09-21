package io.github.scola.qart;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;
import io.github.scola.cuteqr.CuteR;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = "MainActivity";
    private final static int REQUEST_PICK_IMAGE = 1;
    private final static String PREF_TEXT_FOR_QR = "text";

    private final static int MAX_INPUT_BITMAP_WIDTH = 720;
    private final static int MAX_INPUT_BITMAP_HEIGHT= 1280;

    private final static int COLOR_BRIGHTNESS_THRESHOLD = 0x7f;

    private boolean mConverting;

    private CropImageView pickPhoto;

    private MenuItem convertMenu;
    private MenuItem addTextMenu;

    private MenuItem shareMenu;
    private MenuItem saveMenu;
    private MenuItem revertMenu;

    private LinearLayout editTextView;

    private EditText mEditTextView;
    private ImageView qrButton;
    private Button setTextButton;

    private String qrText;
    private Bitmap mOriginBitmap;
    private Bitmap mQRBitmap;
    private Bitmap mCropImage;

    private File shareQr;

    private boolean doubleBackToExitPressedOnce;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickPhoto = (CropImageView)findViewById(R.id.pick_img);
        editTextView = (LinearLayout)findViewById(R.id.text_group);
        mEditTextView = (EditText) findViewById(R.id.edit_text);
        qrButton = (ImageView)findViewById(R.id.emotion_button);
        setTextButton = (Button)findViewById(R.id.btn_send);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mProgressBar.setIndeterminateDrawable(new SmoothProgressDrawable.Builder(this).interpolator(new AccelerateInterpolator()).build());
        mProgressBar.setVisibility(View.INVISIBLE);

        mEditTextView.addTextChangedListener(TextWatcherNewInstance());

        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        qrText = sharedPref.getString(PREF_TEXT_FOR_QR, _(R.string.default_qr_text));

        pickPhoto.setFixedAspectRatio(true);

        setTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String txt = mEditTextView.getText().toString().trim();
                if (txt.isEmpty() == false) {
                    saveQrText(txt);
                    View view = MainActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    editTextView.setVisibility(View.INVISIBLE);
                }
            }
        });

        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                new IntentIntegrator(MainActivity.this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, _(R.string.back_to_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        convertMenu = menu.findItem(R.id.convert_qr);
        addTextMenu = menu.findItem(R.id.add_txt);

        shareMenu = menu.findItem(R.id.share_qr);
        saveMenu = menu.findItem(R.id.save_qr);

        revertMenu = menu.findItem(R.id.revert_qr);

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

        if (id == R.id.launch_gallery) {
            if (mConverting) {
                Toast.makeText(this, _(R.string.converting), Toast.LENGTH_SHORT).show();
                return true;
            }
            if (isStoragePermissionGranted()) {
                launchGallery();
            }
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
            if (mConverting) {
                Toast.makeText(this, _(R.string.converting), Toast.LENGTH_SHORT).show();
                return true;
            }
            new AlertDialog.Builder(this)
                    .setTitle(_(R.string.color_or_black))
                    .setMessage(_(R.string.colorful_msg))
                    .setPositiveButton(R.string.colorful, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            ColorPickerDialogBuilder
                                    .with(MainActivity.this)
                                    .setTitle(R.string.choose_color)
                                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                                    .initialColor(Color.rgb(0x28, 0x32, 0x60))  //default blue
                                    .density(12)
                                    .lightnessSliderOnly()
                                    .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                            if (selectedColor == Color.WHITE) {
                                                Toast.makeText(MainActivity.this, R.string.select_white, Toast.LENGTH_LONG).show();
                                            } else if (Util.calculateColorGrayValue(selectedColor) > COLOR_BRIGHTNESS_THRESHOLD){
                                                Toast.makeText(MainActivity.this, R.string.select_light, Toast.LENGTH_LONG).show();
                                            }
                                            startConvert(true, selectedColor);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startConvert(true, Color.BLACK);
                                        }
                                    })
                                    .showColorEdit(false)
                                    .build()
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.black_white, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            startConvert(false, Color.BLACK);
                        }
                    })
                    .create()
                    .show();
        }

        if (id == R.id.share_qr) {
            shareQr = new File(getExternalCacheDir(), "Pictures");
            if (shareQr.exists() == false) {
                shareQr.mkdirs();
            }
            File newFile = new File(shareQr, "qrImage.png");
            Util.saveBitmap(mQRBitmap, newFile.toString());
            Uri contentUri = Uri.parse("file://" + newFile.getAbsolutePath());

            if (contentUri != null) {
                Log.d(TAG, "Uri: " + contentUri);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, _(R.string.share_via)));

            }
        }

        if (id == R.id.save_qr) {
            shareQr = new File(Environment.getExternalStorageDirectory(), "Pictures");
            if (shareQr.exists() == false) {
                shareQr.mkdirs();
            }
            File newFile = new File(shareQr, "Qart_"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).replaceAll("\\W+", "") + ".png");
            Util.saveBitmap(mQRBitmap, newFile.toString());
            Toast.makeText(this, _(R.string.saved) + newFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Uri uri = Uri.fromFile(newFile);
            Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
            sendBroadcast(scannerIntent);
        }

        if (id == R.id.revert_qr) {
            pickPhoto.setImageBitmap(mOriginBitmap);
            hideSaveMenu();
            showQrMenu();
        }

        if (id == R.id.about_info) {
            openAbout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
    }

    private void startConvert(final boolean colorful, final int color) {
        mConverting = true;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( Void... voids ) {
                mQRBitmap = CuteR.Product(qrText, mCropImage, colorful, color);
                return null;
            }
            @Override
            protected void onPostExecute(Void post) {
                super.onPostExecute(post);
                pickPhoto.setImageBitmap(mQRBitmap);
                mProgressBar.setVisibility(View.INVISIBLE);
                hideQrMenu();
                showSaveMenu();
                mConverting = false;
            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressBar.setVisibility(View.VISIBLE);
                mCropImage = pickPhoto.getCroppedImage();
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
//                    pickPhoto.setImageURI(data.getData());
                    Log.d(TAG, "pick image URI: " + data.getData());
                    mOriginBitmap = getBitmapFromUri(data.getData());
//                    String path = getRealPathFromURI(this, data.getData());
                    convertOrientation(mOriginBitmap, data.getData());
                    pickPhoto.setImageBitmap(mOriginBitmap);
                    hideSaveMenu();
                    showQrMenu();
                }
                break;

            default:
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if(result != null) {
                    if(result.getContents() == null) {
                        Toast.makeText(this, _(R.string.cancel_scan), Toast.LENGTH_LONG).show();
                    } else {
//                        Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                        mEditTextView.setText(result.getContents());
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
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
        if (shareMenu != null && saveMenu != null && revertMenu != null) {
            shareMenu.setVisible(false);
            saveMenu.setVisible(false);
            revertMenu.setVisible(false);
        }
    }

    public void showSaveMenu() {
        if (shareMenu != null && saveMenu != null && revertMenu != null) {
            shareMenu.setVisible(true);
            saveMenu.setVisible(true);
            revertMenu.setVisible(true);
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
                    setTextButton.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));
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

    public void convertOrientation(Bitmap bitmap, Uri imageUri) {
//        int orientation = ExifInterface.ORIENTATION_NORMAL;
//        try {
//            ExifInterface ei = new ExifInterface(photoPath);
//            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
//        } catch (IOException ex) {
//            Log.e(TAG, "can not retrieve exif");
//        }

        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(imageUri,  orientationColumn, null, null, null);
        int orientation = 0;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
            mOriginBitmap = CuteR.rotateImage(bitmap, orientation);
        }
//
//        switch(orientation) {
//            case ExifInterface.ORIENTATION_ROTATE_90:
//                mOriginBitmap = CuteR.rotateImage(bitmap, 90);
//                break;
//            case ExifInterface.ORIENTATION_ROTATE_180:
//                mOriginBitmap = CuteR.rotateImage(bitmap, 180);
//                break;
//            case ExifInterface.ORIENTATION_ROTATE_270:
//                mOriginBitmap = CuteR.rotateImage(bitmap, 270);
//                break;
//            case ExifInterface.ORIENTATION_NORMAL:
//            default:
//                break;
//        }
    }

    public Bitmap getBitmapFromUri(Uri imageUri) {
        getContentResolver().notifyChange(imageUri, null);
        ContentResolver cr = getContentResolver();
        Bitmap bitmap;
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, imageUri);
            float scale = Math.min((float) 1.0 * MAX_INPUT_BITMAP_WIDTH / bitmap.getWidth(), (float) 1.0 * MAX_INPUT_BITMAP_HEIGHT / bitmap.getHeight());
            if (scale < 1) {
                bitmap = CuteR.getResizedBitmap(bitmap, scale, scale);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveQrText(String txt) {
        qrText = txt;
        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PREF_TEXT_FOR_QR, qrText);
        editor.commit();
    }

    private String _(int id) {
        return getResources().getString(id);
    }

    private void openAbout() {
        WebView web = new WebView(this);
        web.loadUrl(_(R.string.about_page));
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        });
        new AlertDialog.Builder(this)
                .setTitle(String.format(_(R.string.about_info_title), getMyVersion(this)))
                .setPositiveButton(R.string.about_info_share, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, _(R.string.share_subject));
                        intent.putExtra(Intent.EXTRA_TEXT, _(R.string.share_content));
                        startActivity(Intent.createChooser(intent, _(R.string.share_channel)));
                    }
                })
                .setNegativeButton(R.string.about_info_close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setView(web)
                .create()
                .show();
    }

    public static String getMyVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (null == packageInfo.versionName) {
                return "Unknown";
            } else {
                return packageInfo.versionName;
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to get package info" + e);
            return "Unknown";
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+ permissions[0] + "was "+ grantResults[0]);
            launchGallery();
        }
    }
}
