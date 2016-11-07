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
import android.graphics.BitmapFactory;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.edmodo.cropper.CropImageView;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;
import io.github.scola.cuteqr.CuteR;
import io.github.scola.gif.AnimatedGifEncoder;;
import pl.droidsonroids.gif.GifDrawable;


public class MainActivity extends ActionBarActivity {
    private final static String TAG = "MainActivity";
    private final static int REQUEST_PICK_IMAGE = 1;
    private final static int REQUEST_SEND_QR_TEXT = 2;
    private final static int REQUEST_PICK_QR_IMAGE = 3;
    private final static int REQUEST_DETECT_QR_IMAGE = 4;
    private final static int REQUEST_SAVE_FILE = 5;

    private final static String PREF_TEXT_FOR_QR = "text";
    private final static String PREF_MODE_FOR_QR = "mode";
    public final static String PREF_GUIDE_VERSION = "version";

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
    private MenuItem galleryMenu;
    private MenuItem colorMenu;
    private MenuItem modeMenu;
    private MenuItem scanMenu;
    private MenuItem detectMenu;

    AHBottomNavigation mBottomNavigation;

    private LinearLayout editTextView;

    private EditText mEditTextView;
    private ImageView qrButton;
    private Button setTextButton;

    private String qrText;
    private Bitmap mOriginBitmap;
    private Bitmap mQRBitmap;
    private Bitmap mCropImage;

    private File shareQr;
    private File gifQr;

    private boolean doubleBackToExitPressedOnce;

    private ProgressBar mProgressBar;

    private boolean mGif;
    private GifDrawable mGifDrawable;

    private Bitmap[] gifArray;
    private Bitmap[] QRGifArray;

    private int mCurrentMode = -1;

    private static final int NORMAL_MODE = 0;
    private static final int PICTURE_MODE = 1;
    private static final int LOGO_MODE = 2;
    private static final int EMBED_MODE = 3;

    private CropImageView.CropPosSize mCropSize;
    private boolean mPickImage;
    private boolean mScan;

    private int mColor = Color.rgb(0x28, 0x32, 0x60);
    final private int[] modeGuide = {R.drawable.guide_img, R.drawable.guide_img_logo, R.drawable.guide_img_embed};

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
                String[] items = getResources().getStringArray(R.array.read_scan_qr);

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.scan_or_read);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                mScan = false;
                                new IntentIntegrator(MainActivity.this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
                                break;
                            case 1:
                                pickImage(REQUEST_PICK_QR_IMAGE);
                                break;
                            default:
                                break;
                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        mBottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.select_mode, android.R.drawable.ic_menu_slideshow, android.R.color.white);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.scan, android.R.drawable.ic_menu_camera, android.R.color.white);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.detect, android.R.drawable.ic_menu_zoom, android.R.color.white);

        mBottomNavigation.addItem(item1);
        mBottomNavigation.addItem(item2);
        mBottomNavigation.addItem(item3);

        mBottomNavigation.setDefaultBackgroundColor(ContextCompat.getColor(this, android.R.color.black));

        // Change colors
        mBottomNavigation.setAccentColor(Color.WHITE);
        mBottomNavigation.setInactiveColor(Color.LTGRAY);

        mBottomNavigation.setCurrentItem(1);

        mBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                // Do something cool here...
//                Toast.makeText(MainActivity.this, "position: " + position, Toast.LENGTH_SHORT).show();
                switch (position) {
                    case 0:
                        showListDialog();
                        break;
                    case 1:
                        mScan = true;
                        new IntentIntegrator(MainActivity.this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
                        break;
                    case 2:
                        pickImage(REQUEST_DETECT_QR_IMAGE);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences sharedPref = getSharedPreferences(PREF_GUIDE_VERSION, Context.MODE_PRIVATE);
        String version = sharedPref.getString(PREF_GUIDE_VERSION, "");
        if (version.isEmpty() || version.split("\\.")[0].equals(getMyVersion(this).split("\\.")[0]) == false) {
            Intent i = new Intent(this, IntroActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        if (editTextView.getVisibility() == View.VISIBLE) {
            editTextView.setVisibility(View.INVISIBLE);
            return;
        }

        if (mPickImage) {
            if (mGifDrawable != null) {
                mGifDrawable.recycle();
            }
            mGif = false;
            mPickImage = false;
            int mode = mCurrentMode;
            mCurrentMode = -1;
            setCurrentMode(mode);
            showNavigation();
            return;
        }

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
        galleryMenu = menu.findItem(R.id.launch_gallery);
        revertMenu = menu.findItem(R.id.revert_qr);
        colorMenu = menu.findItem(R.id.change_color);
        modeMenu = menu.findItem(R.id.select_mode);
        scanMenu = menu.findItem(R.id.scan_qr);
        detectMenu = menu.findItem(R.id.detect_qr);

        hideQrMenu();
        hideSaveMenu();

        final SharedPreferences modePref = getPreferences(Context.MODE_PRIVATE);
        int mode = modePref.getInt(PREF_MODE_FOR_QR, PICTURE_MODE);
        setCurrentMode(mode);

        showNavigation();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.launch_gallery) {
            pickImage(REQUEST_PICK_IMAGE);
        }

        if (id == R.id.add_txt) {
            if (mConverting) {
                Toast.makeText(this, _(R.string.converting), Toast.LENGTH_SHORT).show();
                return true;
            }

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

            if (editTextView.getVisibility() == View.VISIBLE) {
                editTextView.setVisibility(View.INVISIBLE);
            }

            new AlertDialog.Builder(this)
                .setTitle(_(R.string.color_or_black))
                .setMessage(_(R.string.colorful_msg))
                .setPositiveButton(R.string.colorful, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        chooseColor();
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
            File newFile = mGif ? new File(shareQr, "qrImage.gif") : new File(shareQr, "qrImage.png");
            if (!mGif) {
                Util.saveBitmap(mQRBitmap, newFile.toString());
            }

            Uri contentUri = Uri.fromFile(newFile);

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
            if (isStoragePermissionGranted(REQUEST_SAVE_FILE)) {
                saveQRImage();
            }
        }

        if (id == R.id.revert_qr) {
            revertQR(true);
        }

        if (id == R.id.about_info) {
            openAbout();
        }

        if (id == R.id.select_mode) {
            showListDialog();
        }

        if (id == R.id.change_color) {
            chooseColor();
        }

        if (id == R.id.scan_qr) {
            mScan = true;
            new IntentIntegrator(this).initiateScan(IntentIntegrator.QR_CODE_TYPES);
        }

        if (id == R.id.detect_qr) {
            pickImage(REQUEST_DETECT_QR_IMAGE);
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveQRImage() {
        shareQr = new File(Environment.getExternalStorageDirectory(), "Pictures");
        if (shareQr.exists() == false) {
            shareQr.mkdirs();
        }

        File newFile = mGif ? new File(shareQr, "Qart_"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).replaceAll("\\W+", "") + ".gif")
                : new File(shareQr, "Qart_"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).replaceAll("\\W+", "") + ".png");

        if (mGif) {
            try {
                Util.copy(new File(getExternalCacheDir(), "Pictures/qrImage.gif"), newFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            Util.saveBitmap(mQRBitmap, newFile.toString());
        }

        Toast.makeText(this, _(R.string.saved) + newFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        Uri uri = Uri.fromFile(newFile);
        Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scannerIntent);
    }

    private void revertQR(boolean showFrame) {
        pickPhoto.setShowSelectFrame(showFrame);
        if (mGif) {
            pickPhoto.setImageDrawable(mGifDrawable);
        } else {
            pickPhoto.setImageBitmap(mOriginBitmap);
        }
        mCropSize = null;
        mCropImage = null;

        hideSaveMenu();
        if (showFrame) {
            showQrMenu();
        } else {
            hideQrMenu();
        }

    }

    private void chooseColor() {
        ColorPickerDialogBuilder
                .with(MainActivity.this)
                .setTitle(R.string.choose_color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .initialColor(mColor)  //default blue
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
                        mColor = selectedColor;
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

    private void pickImage(int request) {
        if (mConverting) {
            Toast.makeText(this, _(R.string.converting), Toast.LENGTH_SHORT).show();
            return;
        }
        if (isStoragePermissionGranted(request)) {
            launchGallery(request);
        }
    }

    private void launchGallery(int request) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, request);
    }

    private void startConvert(final boolean colorful, final int color) {
        mConverting = true;
        if (mCurrentMode == NORMAL_MODE) {
            mQRBitmap = CuteR.ProductNormal(qrText, colorful, color);
            pickPhoto.setImageBitmap(mQRBitmap);
            mConverting = false;
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( Void... voids ) {
                if (mGif) {
                    QRGifArray = CuteR.ProductGIF(qrText, gifArray, colorful, color);
                    shareQr = new File(getExternalCacheDir(), "Pictures");
                    if (shareQr.exists() == false) {
                        shareQr.mkdirs();
                    }

                    gifQr = new File(shareQr, "qrImage.gif");
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(gifQr);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (fos != null) {
                        AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
                        gifEncoder.setRepeat(0);
                        gifEncoder.start(fos);

                        for (Bitmap bitmap : QRGifArray) {
                            Log.d(TAG, "gifEncoder.addFrame");
                            gifEncoder.addFrame(bitmap);
                        }
                        gifEncoder.finish();
                    }
                } else {
                    switch (mCurrentMode) {
                        case PICTURE_MODE:
                            mQRBitmap = CuteR.Product(qrText, mCropImage, colorful, color);
                            break;
                        case LOGO_MODE:
                            mQRBitmap = CuteR.ProductLogo(mCropImage, qrText, colorful, color);
                            break;
                        case EMBED_MODE:
                            mQRBitmap = CuteR.ProductEmbed(qrText, mCropImage, colorful, color, mCropSize.x, mCropSize.y, mOriginBitmap);
                            break;
                        default:
                            break;
                    }

                }
                return null;
            }
            @Override
            protected void onPostExecute(Void post) {
                super.onPostExecute(post);
                pickPhoto.setShowSelectFrame(false);
                if (mGif) {
                    try {
                        pickPhoto.setImageDrawable(new GifDrawable(gifQr));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
//                    Toast.makeText(MainActivity.this, "Saved.", Toast.LENGTH_SHORT).show();
                } else {
                    pickPhoto.setImageBitmap(mQRBitmap);
                }

                mProgressBar.setVisibility(View.INVISIBLE);
                hideQrMenu();
                showSaveMenu();
                if (mCurrentMode == NORMAL_MODE) {
                    hideRevertMenu();
                }
                mConverting = false;
            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressBar.setVisibility(View.VISIBLE);
                if (mGif) {
//                    pickPhoto.setImageBitmap(mOriginBitmap);
                    mCropSize = mCropSize == null ? pickPhoto.getCroppedSize(mOriginBitmap) : mCropSize;
                    gifArray = new Bitmap[mGifDrawable.getNumberOfFrames()];
                    for (int i = 0; i < gifArray.length; i++) {
                        gifArray[i] = pickPhoto.getCroppedImage(mGifDrawable.seekToFrameAndGet(i), mCropSize);
                    }
                } else {
                    mCropImage = mCropImage == null ? pickPhoto.getCroppedImage(mOriginBitmap) : mCropImage;
                }

                if (mCurrentMode == EMBED_MODE) {
                    mCropSize = mCropSize == null ? pickPhoto.getCroppedSize(mOriginBitmap) : mCropSize;
                }

            }
        }.execute();
    }

    private void startDecode(final Bitmap bitmap, final int requestCode) {
        Log.d(TAG, "startDecode");
        new AsyncTask<Void, Void, Result>() {
            @Override
            protected Result doInBackground(Void... voids ) {
                return CuteR.decodeQRImage(bitmap);
            }
            @Override
            protected void onPostExecute(Result post) {
                super.onPostExecute(post);
                if (post != null && post.getText().trim().isEmpty() == false) {
                    Log.d(TAG, "decode pic qr: " + post.getText());
                    switch (requestCode) {
                        case REQUEST_PICK_QR_IMAGE:
                            mEditTextView.setText(post.getText().trim());
                            break;
                        case REQUEST_DETECT_QR_IMAGE:
                            launchQRResultActivity(post.getText().trim());
                            break;
                        default:
                            break;
                    }

                } else {
                    Toast.makeText(MainActivity.this, R.string.cannot_detect_qr, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
        }.execute();
    }

    private void launchQRResultActivity(String qr) {
        Intent i = new Intent(MainActivity.this, QRCodeResultActivity.class);
        i.putExtra("TEXT", qr);
        startActivityForResult(i, REQUEST_SEND_QR_TEXT);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
//                    pickPhoto.setImageURI(data.getData());
                    Log.d(TAG, "pick image URI: " + data.getData());
                    if (mGifDrawable != null) {
                        mGifDrawable.recycle();
                    }

                    try {
//                        String path = getRealPathFromURI(this, data.getData());
                        String mimeType = getContentResolver().getType(data.getData());
                        Log.d(TAG, "mime type: " + mimeType);
                        if (mimeType != null && mimeType.equals("image/gif")) {
                            if (mCurrentMode != PICTURE_MODE) {
                                Toast.makeText(this, _(R.string.gif_picture_only), Toast.LENGTH_LONG).show();
                                return;
                            }
                            mGif = true;
                            mGifDrawable = new GifDrawable(getContentResolver(), data.getData());
                            pickPhoto.setImageDrawable(mGifDrawable);
                            mOriginBitmap = mGifDrawable.seekToFrameAndGet(0);

                        } else {
                            mGif = false;
                            mOriginBitmap = getBitmapFromUri(data.getData());
                            convertOrientation(mOriginBitmap, data.getData());
                            pickPhoto.setImageBitmap(mOriginBitmap);
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                    }


//                    String path = getRealPathFromURI(this, data.getData());
//                    convertOrientation(mOriginBitmap, data.getData());
//                    pickPhoto.setImageBitmap(mOriginBitmap);
//                    GifAnimationDrawable gif = null;
//                    try {
//                        gif = new GifAnimationDrawable(new File(getRealPathFromURI(this, data.getData())), this);
//                        gif.setOneShot(false);
//                    } catch (Resources.NotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    pickPhoto.setImageDrawable(gif);
//                    gif.setVisible(true, true);
                    mPickImage = true;
                    mCropSize = null;
                    mCropImage = null;
                    pickPhoto.setShowSelectFrame(true);

                    hideSaveMenu();
                    showQrMenu();
                    hideNavigation();
                }
                break;
            case REQUEST_SEND_QR_TEXT:
                if (resultCode == RESULT_OK) {
                    if(data.hasExtra("import")) {
                        Log.d(TAG, "REQUEST_SEND_QR_TEXT");
                        final String text = data.getExtras().getString("import");
                        mEditTextView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                editTextView.setVisibility(View.VISIBLE);
                                mEditTextView.setText(text);
                                mEditTextView.setSelection(text.length());
                            }
                        }, 200);

                    }
                }
                break;
            case REQUEST_PICK_QR_IMAGE:
            case REQUEST_DETECT_QR_IMAGE:
                if (resultCode == RESULT_OK) {
                    String mimeType = getContentResolver().getType(data.getData());
                    Log.d(TAG, "mime type: " + mimeType);
                    Bitmap qrImage = null;
                    if (mimeType != null && mimeType.equals("image/gif")) {
                        try {
                            GifDrawable gifDrawable = new GifDrawable(getContentResolver(), data.getData());
                            if (gifDrawable != null) {
                                qrImage = gifDrawable.seekToFrameAndGet(0);
                                gifDrawable.recycle();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        qrImage = getBitmapFromUri(data.getData());
                    }
                    startDecode(qrImage, requestCode);
                }
                break;

            default:
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if(result != null) {
                    if(result.getContents() == null) {
                        Toast.makeText(this, _(R.string.cancel_scan), Toast.LENGTH_LONG).show();
                    } else {
//                        Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                        if (mScan) {
                            launchQRResultActivity(result.getContents());
                        } else {
                            mEditTextView.setText(result.getContents());
                        }
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
    }

    public void hideNavigation() {
        mBottomNavigation.setVisibility(View.INVISIBLE);
        modeMenu.setVisible(true);
        scanMenu.setVisible(true);
        detectMenu.setVisible(true);
    }

    public void showNavigation() {
        mBottomNavigation.setVisibility(View.VISIBLE);
        modeMenu.setVisible(false);
        scanMenu.setVisible(false);
        detectMenu.setVisible(false);
    }

    public void hideGalleryMenu() {
        if (galleryMenu != null) {
            galleryMenu.setVisible(false);
        }
    }

    public void showGalleryMenu() {
        if (galleryMenu != null) {
            galleryMenu.setVisible(true);
        }
    }

    public void hideRevertMenu() {
        if (revertMenu != null) {
            revertMenu.setVisible(false);
        }
    }

    public void showRevertMenu() {
        if (revertMenu != null) {
            revertMenu.setVisible(true);
        }
    }

    public void hideQrMenu() {
        if (convertMenu != null) {
            convertMenu.setVisible(false);
        }
    }

    public void showQrMenu() {
        if (convertMenu != null) {
            convertMenu.setVisible(true);
        }
    }

    public void hideSaveMenu() {
        if (shareMenu != null && saveMenu != null && revertMenu != null) {
            shareMenu.setVisible(false);
            saveMenu.setVisible(false);
            revertMenu.setVisible(false);
        }

        if (colorMenu != null) {
            colorMenu.setVisible(false);
        }
    }

    public void showSaveMenu() {
        if (shareMenu != null && saveMenu != null && revertMenu != null) {
            shareMenu.setVisible(true);
            saveMenu.setVisible(true);
            revertMenu.setVisible(true);
        }

        if (colorMenu != null) {
            colorMenu.setVisible(true);
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
        if (qrText == txt) {
            return;
        }

        Bitmap checkTextBitmap = CuteR.ProductNormal(txt, false, Color.BLACK);
        if (checkTextBitmap == null) {
            Toast.makeText(this, _(R.string.text_too_long), Toast.LENGTH_LONG).show();
            return;
        }

        qrText = txt;
        if (mCurrentMode == NORMAL_MODE) {
            mQRBitmap = checkTextBitmap;
            pickPhoto.setImageBitmap(mQRBitmap);
        }

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

    public  boolean isStoragePermissionGranted(int request) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, request);
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
            if (requestCode == REQUEST_SAVE_FILE) {
                saveQRImage();
            } else {
                launchGallery(requestCode);
            }
        }
    }

    private void showListDialog() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setTitle(R.string.select_mode_title);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.select_dialog_singlechoice);
        final String[] modes = new String[]{_(R.string.normal_mode), _(R.string.picture_mode), _(R.string.logo_mode), _(R.string.embed_mode)};
        for (String mode : modes) {
            arrayAdapter.add(mode);
        }
        builderSingle.setSingleChoiceItems(modes, mCurrentMode, null);
//        builderSingle.setNegativeButton(
//                "cancel",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setCurrentMode(which);
                        dialog.dismiss();
                    }
                });

        builderSingle.show();
    }

    public int getCurrentMode() {
        return mCurrentMode;
    }

    public void setCurrentMode(int mode) {
        if (mCurrentMode == mode) {
            return;
        }
        mCurrentMode = mode;

        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(PREF_MODE_FOR_QR, mode);
        editor.commit();

        switch (mCurrentMode) {
            case NORMAL_MODE:
                hideGalleryMenu();
                hideQrMenu();
                mQRBitmap = CuteR.ProductNormal(qrText, false, Color.BLACK);
                pickPhoto.setImageBitmap(mQRBitmap);
                pickPhoto.setShowSelectFrame(false);
                showSaveMenu();
                hideRevertMenu();
                break;
            default:
                showGalleryMenu();
                if (mPickImage) {
                    if (mGif && mCurrentMode != PICTURE_MODE) {
                        mPickImage = false;
                        mGif = false;
                        if (mGifDrawable != null) {
                            mGifDrawable.recycle();
                        }
                        mOriginBitmap = BitmapFactory.decodeResource(getResources(), modeGuide[mCurrentMode - 1]);
                    }
                } else {
                    mOriginBitmap = BitmapFactory.decodeResource(getResources(), modeGuide[mCurrentMode - 1]);
                }
                revertQR(mPickImage);
                break;
        }
    }
}
