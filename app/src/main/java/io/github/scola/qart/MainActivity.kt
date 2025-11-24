package io.github.scola.qart

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.edmodo.cropper.CropImageView
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.zxing.integration.android.IntentIntegrator
import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable
import io.github.scola.cuteqr.CuteR
import io.github.scola.gif.AnimatedGifEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.droidsonroids.gif.GifDrawable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val REQUEST_PICK_IMAGE = 1
    private val REQUEST_SEND_QR_TEXT = 2
    private val REQUEST_PICK_QR_IMAGE = 3
    private val REQUEST_DETECT_QR_IMAGE = 4
    private val REQUEST_SAVE_FILE = 5

    private val PREF_TEXT_FOR_QR = "text"
    private val PREF_MODE_FOR_QR = "mode"

    private val MAX_INPUT_BITMAP_WIDTH = 720
    private val MAX_INPUT_BITMAP_HEIGHT = 1280

    private val COLOR_BRIGHTNESS_THRESHOLD = 0x7f

    private var mConverting = false

    private lateinit var pickPhoto: CropImageView

    private var convertMenu: MenuItem? = null
    private var addTextMenu: MenuItem? = null

    private var shareMenu: MenuItem? = null
    private var saveMenu: MenuItem? = null
    private var revertMenu: MenuItem? = null
    private var galleryMenu: MenuItem? = null
    private var colorMenu: MenuItem? = null
    private var modeMenu: MenuItem? = null
    private var scanMenu: MenuItem? = null
    private var detectMenu: MenuItem? = null

    private lateinit var mBottomNavigation: AHBottomNavigation

    private lateinit var editTextView: LinearLayout

    private lateinit var mEditTextView: EditText
    private lateinit var qrButton: ImageView
    private lateinit var setTextButton: Button

    private var qrText: String? = null
    private var mOriginBitmap: Bitmap? = null
    private var mQRBitmap: Bitmap? = null
    private var mCropImage: Bitmap? = null

    private var shareQr: File? = null
    private var gifQr: File? = null

    private var doubleBackToExitPressedOnce = false

    private lateinit var mProgressBar: ProgressBar

    private var mGif = false
    private var mGifDrawable: GifDrawable? = null

    private lateinit var gifArray: Array<Bitmap>
    private lateinit var QRGifArray: Array<Bitmap>

    private var mCurrentMode = -1

    private val NORMAL_MODE = 0
    private val PICTURE_MODE = 1
    private val LOGO_MODE = 2
    private val EMBED_MODE = 3

    private var mCropSize: CropImageView.CropPosSize? = null
    private var mPickImage = false
    private var mScan = false

    private var mColor = Color.rgb(0x28, 0x32, 0x60)
    private val modeGuide = intArrayOf(
        R.drawable.guide_img,
        R.drawable.guide_img_logo,
        R.drawable.guide_img_embed
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pickPhoto = findViewById<View>(R.id.pick_img) as CropImageView
        editTextView = findViewById<View>(R.id.text_group) as LinearLayout
        mEditTextView = findViewById<View>(R.id.edit_text) as EditText
        qrButton = findViewById<View>(R.id.emotion_button) as ImageView
        setTextButton = findViewById<View>(R.id.btn_send) as Button

        mProgressBar = findViewById<View>(R.id.progressbar) as ProgressBar
        mProgressBar.indeterminateDrawable = SmoothProgressDrawable.Builder(this).interpolator(
            AccelerateInterpolator()
        ).build()
        mProgressBar.visibility = View.INVISIBLE

        mEditTextView.addTextChangedListener(TextWatcherNewInstance())

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        qrText = sharedPref.getString(PREF_TEXT_FOR_QR, str(R.string.default_qr_text))

        pickPhoto.setFixedAspectRatio(true)

        setTextButton.setOnClickListener {
            val txt = mEditTextView.text.toString().trim { it <= ' ' }
            if (!txt.isEmpty()) {
                saveQrText(txt)
                val view = this@MainActivity.currentFocus
                if (view != null) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                editTextView.visibility = View.INVISIBLE
            }
        }

        qrButton.setOnClickListener {
            val items = resources.getStringArray(R.array.read_scan_qr)

            val builder = androidx.appcompat.app.AlertDialog.Builder(
                this@MainActivity
            )
            builder.setTitle(R.string.scan_or_read)
            builder.setItems(items) { dialogInterface, i ->
                switchScanOrRead(i)
                dialogInterface.dismiss()
            }
            builder.show()
        }

        mBottomNavigation = findViewById<View>(R.id.bottom_navigation) as AHBottomNavigation
        val item1 = AHBottomNavigationItem(
            R.string.select_mode,
            android.R.drawable.ic_menu_slideshow, android.R.color.white
        )
        val item2 = AHBottomNavigationItem(
            R.string.scan, android.R.drawable.ic_menu_camera,
            android.R.color.white
        )
        val item3 = AHBottomNavigationItem(
            R.string.detect, android.R.drawable.ic_menu_zoom,
            android.R.color.white
        )

        mBottomNavigation.addItem(item1)
        mBottomNavigation.addItem(item2)
        mBottomNavigation.addItem(item3)

        mBottomNavigation.defaultBackgroundColor = ContextCompat.getColor(this, android.R.color.black)

        // Change colors
        mBottomNavigation.accentColor = Color.WHITE
        mBottomNavigation.inactiveColor = Color.LTGRAY

        mBottomNavigation.currentItem = 1

        mBottomNavigation.setOnTabSelectedListener { position, wasSelected ->
            // Do something cool here...
            // Toast.makeText(MainActivity.this, "position: " + position,
            // Toast.LENGTH_SHORT).show();
            when (position) {
                0 -> showListDialog()
                1 -> {
                    mScan = true
                    IntentIntegrator(this@MainActivity).initiateScan(IntentIntegrator.QR_CODE_TYPES)
                }

                2 -> pickImage(REQUEST_DETECT_QR_IMAGE)
                else -> {
                }
            }
            true
        }
    }

    private fun switchScanOrRead(i: Int) {
        when (i) {
            0 -> {
                mScan = false
                IntentIntegrator(this@MainActivity).initiateScan(IntentIntegrator.QR_CODE_TYPES)
            }

            1 -> pickImage(REQUEST_PICK_QR_IMAGE)
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences(PREF_GUIDE_VERSION, Context.MODE_PRIVATE)
        val version = sharedPref.getString(PREF_GUIDE_VERSION, "")
        if (version!!.isEmpty() || version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0] != getMyVersion(this).split("\\.".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()[0]) {
            val i = Intent(this, IntroActivity::class.java)
            startActivity(i)
        }
    }

    override fun onBackPressed() {
        if (editTextView.visibility == View.VISIBLE) {
            editTextView.visibility = View.INVISIBLE
            return
        }

        if (mPickImage) {
            if (mGifDrawable != null) {
                mGifDrawable!!.recycle()
            }
            mGif = false
            mPickImage = false
            val mode = mCurrentMode
            mCurrentMode = -1
            setCurrentMode(mode)
            showNavigation()
            return
        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, str(R.string.back_to_exit), Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        convertMenu = menu.findItem(R.id.convert_qr)
        addTextMenu = menu.findItem(R.id.add_txt)

        shareMenu = menu.findItem(R.id.share_qr)
        saveMenu = menu.findItem(R.id.save_qr)
        galleryMenu = menu.findItem(R.id.launch_gallery)
        revertMenu = menu.findItem(R.id.revert_qr)
        colorMenu = menu.findItem(R.id.change_color)
        modeMenu = menu.findItem(R.id.select_mode)
        scanMenu = menu.findItem(R.id.scan_qr)
        detectMenu = menu.findItem(R.id.detect_qr)

        hideQrMenu()
        hideSaveMenu()

        val modePref = getPreferences(Context.MODE_PRIVATE)
        val mode = modePref.getInt(PREF_MODE_FOR_QR, PICTURE_MODE)
        setCurrentMode(mode)

        showNavigation()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.launch_gallery) {
            pickImage(REQUEST_PICK_IMAGE)
        }

        if (id == R.id.add_txt) {
            if (mConverting) {
                Toast.makeText(this, str(R.string.converting), Toast.LENGTH_SHORT).show()
                return true
            }

            if (editTextView.visibility == View.INVISIBLE) {
                editTextView.visibility = View.VISIBLE
                if (qrText != null && !qrText!!.isEmpty()) {
                    mEditTextView.setText(qrText)
                    mEditTextView.setSelection(qrText!!.length)
                }
            } else {
                editTextView.visibility = View.INVISIBLE
            }
        }

        if (id == R.id.convert_qr) {
            if (mConverting) {
                Toast.makeText(this, str(R.string.converting), Toast.LENGTH_SHORT).show()
                return true
            }

            if (editTextView.visibility == View.VISIBLE) {
                editTextView.visibility = View.INVISIBLE
            }

            AlertDialog.Builder(this)
                .setTitle(str(R.string.color_or_black))
                .setMessage(str(R.string.colorful_msg))
                .setPositiveButton(R.string.colorful) { dialogInterface, i ->
                    dialogInterface.cancel()
                    chooseColor()
                }
                .setNegativeButton(R.string.black_white) { dialogInterface, i ->
                    dialogInterface.cancel()
                    startConvert(false, Color.BLACK)
                }
                .create()
                .show()

        }

        if (id == R.id.share_qr) {
            shareQr = File(appCacheDir, "Pictures")
            if (!shareQr!!.exists()) {
                shareQr!!.mkdirs()
            }
            val newFile =
            if (mGif) File(shareQr!!, "qrImage.gif") else File(shareQr!!, "qrImage.png")
            if (!mGif) {
                Util.saveBitmap(mQRBitmap!!, newFile.toString())
            }

            val contentUri = FileProvider.getUriForFile(
                this, packageName + ".provider",
                newFile
            )

            if (contentUri != null) {
                Log.d(TAG, "Uri: $contentUri")
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                shareIntent.type = "image/png"
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                startActivity(Intent.createChooser(shareIntent, str(R.string.share_via)))
            }
        }

        if (id == R.id.save_qr) {
            if (isStoragePermissionGranted(REQUEST_SAVE_FILE)) {
                saveQRImage()
            }
        }

        if (id == R.id.revert_qr) {
            revertQR(true)
        }

        if (id == R.id.about_info) {
            openAbout()
        }

        if (id == R.id.select_mode) {
            showListDialog()
        }

        if (id == R.id.change_color) {
            chooseColor()
        }

        if (id == R.id.scan_qr) {
            mScan = true
            IntentIntegrator(this).initiateScan(IntentIntegrator.QR_CODE_TYPES)
        }

        if (id == R.id.detect_qr) {
            pickImage(REQUEST_DETECT_QR_IMAGE)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun saveQRImage() {
        shareQr = File(Environment.getExternalStorageDirectory(), "Pictures")
        if (!shareQr!!.exists()) {
            shareQr!!.mkdirs()
        }

        val newFile = if (mGif)
            File(
                shareQr,
                "Qart_" + SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    .replace("\\W+".toRegex(), "")
                        + ".gif"
            )
        else
            File(
                shareQr,
                "Qart_" + SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    .replace("\\W+".toRegex(), "")
                        + ".png"
            )

        if (mGif) {
            try {
                Util.copy(File(appCacheDir, "Pictures/qrImage.gif"), newFile)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

        } else {
            Util.saveBitmap(mQRBitmap!!, newFile.toString())
        }

        Toast.makeText(this, str(R.string.saved) + newFile.absolutePath, Toast.LENGTH_LONG).show()
        val uri = Uri.fromFile(newFile)
        val scannerIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
        sendBroadcast(scannerIntent)
    }

    private fun revertQR(showFrame: Boolean) {
        pickPhoto.setShowSelectFrame(showFrame)
        if (mGif) {
            pickPhoto.setImageDrawable(mGifDrawable)
        } else {
            pickPhoto.setImageBitmap(mOriginBitmap)
        }
        mCropSize = null
        mCropImage = null

        hideSaveMenu()
        if (showFrame) {
            showQrMenu()
        } else {
            hideQrMenu()
        }

    }

    private fun chooseColor() {
        ColorPickerDialogBuilder
            .with(this@MainActivity)
            .setTitle(R.string.choose_color)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .initialColor(mColor) // default blue
            .density(12)
            .lightnessSliderOnly()
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, selectedColor, allColors ->
                if (selectedColor == Color.WHITE) {
                    Toast.makeText(this@MainActivity, R.string.select_white, Toast.LENGTH_LONG)
                        .show()
                } else if (Util.calculateColorGrayValue(selectedColor) > COLOR_BRIGHTNESS_THRESHOLD) {
                    Toast.makeText(this@MainActivity, R.string.select_light, Toast.LENGTH_LONG)
                        .show()
                }
                mColor = selectedColor
                startConvert(true, selectedColor)
            }
            .setNegativeButton(
                android.R.string.cancel
            ) { dialog, which -> startConvert(true, Color.BLACK) }
            .showColorEdit(false)
            .build()
            .show()
    }

    private fun pickImage(request: Int) {
        if (mConverting) {
            Toast.makeText(this, str(R.string.converting), Toast.LENGTH_SHORT).show()
            return
        }
        if (isStoragePermissionGranted(request)) {
            launchGallery(request)
        }
    }

    private fun launchGallery(request: Int) {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, request)
    }

    private val appCacheDir: File
        get() {
            var cacheDir = externalCacheDir
            if (cacheDir == null) {
                cacheDir = cacheDir
            }
            return cacheDir!!
        }

    private fun startConvert(colorful: Boolean, color: Int) {
        mConverting = true
        if (mCurrentMode == NORMAL_MODE) {
            mQRBitmap = CuteR.ProductNormal(qrText, colorful, color)
            pickPhoto.setImageBitmap(mQRBitmap)
            mConverting = false
            return
        }

        lifecycleScope.launch {
            onPreExecuteConvert()
            withContext(Dispatchers.IO) {
                doInBackgroundConvert(colorful, color)
            }
            onPostExecuteConvert()
        }
    }

    private fun onPreExecuteConvert() {
        mProgressBar.visibility = View.VISIBLE
        if (mGif) {
            // pickPhoto.setImageBitmap(mOriginBitmap);
            mCropSize = if (mCropSize == null) pickPhoto.getCroppedSize(mOriginBitmap) else mCropSize
            gifArray = Array(mGifDrawable!!.numberOfFrames) { i ->
                pickPhoto.getCroppedImage(mGifDrawable!!.seekToFrameAndGet(i), mCropSize)
            }
        } else {
            mCropImage = if (mCropImage == null) pickPhoto.getCroppedImage(mOriginBitmap) else mCropImage
        }

        if (mCurrentMode == EMBED_MODE) {
            mCropSize = if (mCropSize == null) pickPhoto.getCroppedSize(mOriginBitmap) else mCropSize
        }
    }

    private fun doInBackgroundConvert(colorful: Boolean, color: Int) {
        if (mGif) {
            QRGifArray = CuteR.ProductGIF(qrText, gifArray, colorful, color)
            shareQr = File(appCacheDir, "Pictures")
            if (!shareQr!!.exists()) {
                shareQr!!.mkdirs()
            }

            gifQr = File(shareQr, "qrImage.gif")
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(gifQr)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (fos != null) {
                val gifEncoder = AnimatedGifEncoder()
                gifEncoder.setRepeat(0)
                gifEncoder.start(fos)

                for (bitmap in QRGifArray) {
                    Log.d(TAG, "gifEncoder.addFrame")
                    gifEncoder.addFrame(bitmap)
                }
                gifEncoder.finish()
            }
        } else {
            when (mCurrentMode) {
                PICTURE_MODE -> mQRBitmap = CuteR.Product(qrText, mCropImage, colorful, color)
                LOGO_MODE -> mQRBitmap = CuteR.ProductLogo(mCropImage, qrText, colorful, color)
                EMBED_MODE -> mQRBitmap = CuteR.ProductEmbed(
                    qrText, mCropImage, colorful, color, mCropSize!!.x,
                    mCropSize!!.y, mOriginBitmap
                )

                else -> {
                }
            }

        }
    }

    private fun onPostExecuteConvert() {
        pickPhoto.setShowSelectFrame(false)
        if (mGif) {
            try {
                pickPhoto.setImageDrawable(GifDrawable(gifQr!!))
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            // Toast.makeText(MainActivity.this, "Saved.", Toast.LENGTH_SHORT).show();
        } else {
            pickPhoto.setImageBitmap(mQRBitmap)
        }

        mProgressBar.visibility = View.INVISIBLE
        hideQrMenu()
        showSaveMenu()
        if (mCurrentMode == NORMAL_MODE) {
            hideRevertMenu()
        }
        mConverting = false
    }


    private fun startDecode(bitmap: Bitmap?, requestCode: Int) {
        Log.d(TAG, "startDecode")
        lifecycleScope.launch(Dispatchers.IO) {
            val result = CuteR.decodeQRImage(bitmap)
            withContext(Dispatchers.Main) {
                if (result != null && !result.text.trim { it <= ' ' }.isEmpty()) {
                    Log.d(TAG, "decode pic qr: " + result.text)
                    switchRequestCode(requestCode, result.text.trim { it <= ' ' })

                } else {
                    Toast.makeText(this@MainActivity, R.string.cannot_detect_qr, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun switchRequestCode(requestCode: Int, text: String) {
        when (requestCode) {
            REQUEST_PICK_QR_IMAGE -> mEditTextView.setText(text)
            REQUEST_DETECT_QR_IMAGE -> launchQRResultActivity(text)
            else -> {
            }
        }
    }

    private fun launchQRResultActivity(qr: String) {
        val i = Intent(this@MainActivity, QRCodeResultActivity::class.java)
        i.putExtra("TEXT", qr)
        startActivityForResult(i, REQUEST_SEND_QR_TEXT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_IMAGE -> if (resultCode == RESULT_OK) {
                // pickPhoto.setImageURI(data.getData());
                Log.d(TAG, "pick image URI: " + data!!.data)
                if (mGifDrawable != null) {
                    mGifDrawable!!.recycle()
                }

                try {
                    // String path = getRealPathFromURI(this, data.getData());
                    val mimeType = contentResolver.getType(data.data!!)
                    Log.d(TAG, "mime type: " + mimeType!!)
                    if (mimeType != null && mimeType == "image/gif") {
                        if (mCurrentMode != PICTURE_MODE) {
                            Toast.makeText(this, str(R.string.gif_picture_only), Toast.LENGTH_LONG)
                                .show()
                            return
                        }
                        mGif = true
                        mGifDrawable = GifDrawable(contentResolver, data.data!!)
                        pickPhoto.setImageDrawable(mGifDrawable)
                        mOriginBitmap = mGifDrawable!!.seekToFrameAndGet(0)

                    } else {
                        mGif = false
                        mOriginBitmap = getBitmapFromUri(data.data)
                        convertOrientation(mOriginBitmap, data.data)
                        pickPhoto.setImageBitmap(mOriginBitmap)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                // String path = getRealPathFromURI(this, data.getData());
                // convertOrientation(mOriginBitmap, data.getData());
                // pickPhoto.setImageBitmap(mOriginBitmap);
                // GifAnimationDrawable gif = null;
                // try {
                // gif = new GifAnimationDrawable(new File(getRealPathFromURI(this,
                // data.getData())), this);
                // gif.setOneShot(false);
                // } catch (Resources.NotFoundException e) {
                // e.printStackTrace();
                // } catch (IOException e) {
                // e.printStackTrace();
                // }
                //
                // pickPhoto.setImageDrawable(gif);
                // gif.setVisible(true, true);
                mPickImage = true
                mCropSize = null
                mCropImage = null
                pickPhoto.setShowSelectFrame(true)

                hideSaveMenu()
                showQrMenu()
                hideNavigation()
            }

            REQUEST_SEND_QR_TEXT -> if (resultCode == RESULT_OK) {
                if (data!!.hasExtra("import")) {
                    Log.d(TAG, "REQUEST_SEND_QR_TEXT")
                    val text = data.extras!!.getString("import")
                    mEditTextView.postDelayed({
                        editTextView.visibility = View.VISIBLE
                        mEditTextView.setText(text)
                        mEditTextView.setSelection(text!!.length)
                    }, 200)

                }
            }

            REQUEST_PICK_QR_IMAGE, REQUEST_DETECT_QR_IMAGE -> if (resultCode == RESULT_OK) {
                val mimeType = contentResolver.getType(data!!.data!!)
                Log.d(TAG, "mime type: " + mimeType!!)
                var qrImage: Bitmap? = null
                if (mimeType != null && mimeType == "image/gif") {
                    try {
                        val gifDrawable = GifDrawable(contentResolver, data.data!!)
                        if (gifDrawable != null) {
                            qrImage = gifDrawable.seekToFrameAndGet(0)
                            gifDrawable.recycle()
                        }
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }

                } else {
                    qrImage = getBitmapFromUri(data.data)
                }
                startDecode(qrImage, requestCode)
            }

            else -> {
                val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result != null) {
                    if (result.contents == null) {
                        Toast.makeText(this, str(R.string.cancel_scan), Toast.LENGTH_LONG).show()
                    } else {
                        // Toast.makeText(this, "Scanned: " + result.getContents(),
                        // Toast.LENGTH_LONG).show();
                        if (mScan) {
                            launchQRResultActivity(result.contents)
                        } else {
                            mEditTextView.setText(result.contents)
                        }
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    fun hideNavigation() {
        mBottomNavigation.visibility = View.INVISIBLE
        modeMenu!!.isVisible = true
        scanMenu!!.isVisible = true
        detectMenu!!.isVisible = true
    }

    fun showNavigation() {
        mBottomNavigation.visibility = View.VISIBLE
        modeMenu!!.isVisible = false
        scanMenu!!.isVisible = false
        detectMenu!!.isVisible = false
    }

    fun hideGalleryMenu() {
        if (galleryMenu != null) {
            galleryMenu!!.isVisible = false
        }
    }

    fun showGalleryMenu() {
        if (galleryMenu != null) {
            galleryMenu!!.isVisible = true
        }
    }

    fun hideRevertMenu() {
        if (revertMenu != null) {
            revertMenu!!.isVisible = false
        }
    }

    fun showRevertMenu() {
        if (revertMenu != null) {
            revertMenu!!.isVisible = true
        }
    }

    fun hideQrMenu() {
        if (convertMenu != null) {
            convertMenu!!.isVisible = false
        }
    }

    fun showQrMenu() {
        if (convertMenu != null) {
            convertMenu!!.isVisible = true
        }
    }

    fun hideSaveMenu() {
        if (shareMenu != null && saveMenu != null && revertMenu != null) {
            shareMenu!!.isVisible = false
            saveMenu!!.isVisible = false
            revertMenu!!.isVisible = false
        }

        if (colorMenu != null) {
            colorMenu!!.isVisible = false
        }
    }

    fun showSaveMenu() {
        if (shareMenu != null && saveMenu != null && revertMenu != null) {
            shareMenu!!.isVisible = true
            saveMenu!!.isVisible = true
            revertMenu!!.isVisible = true
        }

        if (colorMenu != null) {
            colorMenu!!.isVisible = true
        }
    }

    private fun TextWatcherNewInstance(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val text = mEditTextView.text.toString().trim { it <= ' ' }
                if (text != null && !text.isEmpty()) {
                    setTextButton.isClickable = true
                    setTextButton.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                } else {
                    setTextButton.isClickable = false
                    setTextButton
                        .setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.reply_button_text_disable
                            )
                        )
                }

            }
        }
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }

    fun convertOrientation(bitmap: Bitmap?, imageUri: Uri?) {
        // int orientation = ExifInterface.ORIENTATION_NORMAL;
        // try {
        // ExifInterface ei = new ExifInterface(photoPath);
        // orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
        // ExifInterface.ORIENTATION_UNDEFINED);
        // } catch (IOException ex) {
        // Log.e(TAG, "can not retrieve exif");
        // }

        val orientationColumn = arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cur = contentResolver.query(imageUri!!, orientationColumn, null, null, null)
        var orientation = 0
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]))
            mOriginBitmap = CuteR.rotateImage(bitmap, orientation.toFloat())
        }
        //
        // switch(orientation) {
        // case ExifInterface.ORIENTATION_ROTATE_90:
        // mOriginBitmap = CuteR.rotateImage(bitmap, 90);
        // break;
        // case ExifInterface.ORIENTATION_ROTATE_180:
        // mOriginBitmap = CuteR.rotateImage(bitmap, 180);
        // break;
        // case ExifInterface.ORIENTATION_ROTATE_270:
        // mOriginBitmap = CuteR.rotateImage(bitmap, 270);
        // break;
        // case ExifInterface.ORIENTATION_NORMAL:
        // default:
        // break;
        // }
    }

    fun getBitmapFromUri(imageUri: Uri?): Bitmap? {
        contentResolver.notifyChange(imageUri!!, null)
        val cr = contentResolver
        var bitmap: Bitmap
        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri)
            val scale = kotlin.math.min(
                1.0f * MAX_INPUT_BITMAP_WIDTH / bitmap.width,
                1.0f * MAX_INPUT_BITMAP_HEIGHT / bitmap.height
            )
            if (scale < 1) {
                bitmap = CuteR.getResizedBitmap(bitmap, scale, scale)
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    private fun saveQrText(txt: String) {
        if (qrText === txt) {
            return
        }

        val checkTextBitmap = CuteR.ProductNormal(txt, false, Color.BLACK)
        if (checkTextBitmap == null) {
            Toast.makeText(this, str(R.string.text_too_long), Toast.LENGTH_LONG).show()
            return
        }

        qrText = txt
        if (mCurrentMode == NORMAL_MODE) {
            mQRBitmap = checkTextBitmap
            pickPhoto.setImageBitmap(mQRBitmap)
        }

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(PREF_TEXT_FOR_QR, qrText)
        editor.commit()
    }

    private fun str(id: Int): String {
        return resources.getString(id)
    }

    private fun openAbout() {
        val web = WebView(this)
        web.loadUrl(str(R.string.about_page))
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return true
            }
        }
        AlertDialog.Builder(this)
            .setTitle(String.format(str(R.string.about_info_title), getMyVersion(this)))
            .setPositiveButton(R.string.about_info_share) { dialogInterface, i ->
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, str(R.string.share_subject))
                intent.putExtra(Intent.EXTRA_TEXT, str(R.string.share_content))
                startActivity(Intent.createChooser(intent, str(R.string.share_channel)))
            }
            .setNegativeButton(R.string.about_info_close) { dialogInterface, i ->
                dialogInterface.cancel()
            }
            .setView(web)
            .create()
            .show()
    }

    companion object {
        val PREF_GUIDE_VERSION = "version"

        fun getMyVersion(context: Context): String {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                return if (null == packageInfo.versionName) {
                    "Unknown"
                } else {
                    packageInfo.versionName
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "failed to get package info" + e)
                return "Unknown"
            }

        }
    }

    fun isStoragePermissionGranted(request: Int): Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED ||
                (Build.VERSION.SDK_INT >= 34 && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                Log.v(TAG, "Permission is granted")
                return true
            } else {
                Log.v(TAG, "Permission is revoked")
                if (Build.VERSION.SDK_INT >= 34) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                        ), request
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        request
                    )
                }
                return false
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, "Permission is granted")
                return true
            } else {
                Log.v(TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    request
                )
                return false
            }
        } else { // permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0])
            if (requestCode == REQUEST_SAVE_FILE) {
                saveQRImage()
            } else {
                launchGallery(requestCode)
            }
        }
    }

    private fun showListDialog() {
        val builderSingle = AlertDialog.Builder(this@MainActivity)
        builderSingle.setTitle(R.string.select_mode_title)

        val arrayAdapter = ArrayAdapter<String>(
            this@MainActivity,
            android.R.layout.select_dialog_singlechoice
        )
        val modes = arrayOf(
            str(R.string.normal_mode), str(R.string.picture_mode),
            str(R.string.logo_mode),
            str(R.string.embed_mode)
        )
        for (mode in modes) {
            arrayAdapter.add(mode)
        }
        builderSingle.setSingleChoiceItems(modes, mCurrentMode, null)
        // builderSingle.setNegativeButton(
        // "cancel",
        // new DialogInterface.OnClickListener() {
        // @Override
        // public void onClick(DialogInterface dialog, int which) {
        // dialog.dismiss();
        // }
        // });

        builderSingle.setAdapter(
            arrayAdapter
        ) { dialog, which ->
            setCurrentMode(which)
            dialog.dismiss()
        }

        builderSingle.show()
    }

    val currentMode: Int
        get() = mCurrentMode

    fun setCurrentMode(mode: Int) {
        if (mCurrentMode == mode) {
            return
        }
        mCurrentMode = mode

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(PREF_MODE_FOR_QR, mode)
        editor.commit()

        when (mCurrentMode) {
            NORMAL_MODE -> {
                hideGalleryMenu()
                hideQrMenu()
                mQRBitmap = CuteR.ProductNormal(qrText, false, Color.BLACK)
                pickPhoto.setImageBitmap(mQRBitmap)
                pickPhoto.setShowSelectFrame(false)
                showSaveMenu()
                hideRevertMenu()
            }

            else -> {
                showGalleryMenu()
                if (mPickImage) {
                    if (mGif && mCurrentMode != PICTURE_MODE) {
                        mPickImage = false
                        mGif = false
                        if (mGifDrawable != null) {
                            mGifDrawable!!.recycle()
                        }
                        mOriginBitmap = BitmapFactory.decodeResource(resources, modeGuide[mCurrentMode - 1])
                    }
                } else {
                    mOriginBitmap = BitmapFactory.decodeResource(resources, modeGuide[mCurrentMode - 1])
                }
                revertQR(mPickImage)
            }
        }
    }
}
