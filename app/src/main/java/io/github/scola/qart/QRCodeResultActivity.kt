package io.github.scola.qart

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import io.github.scola.cuteqr.CuteR
import java.util.regex.Matcher

class QRCodeResultActivity : AppCompatActivity() {
    private val TAG = "QRCodeResultActivity"

    private val REQUEST_SAVE_FILE = 5

    private var qrText: String? = null
    private var mTextView: TextView? = null
    private var mQRImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_result_view)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.qr_result)

        mTextView = findViewById<View>(R.id.qr_text) as TextView
        mQRImage = findViewById<View>(R.id.qr_result_img) as ImageView

        qrText = intent.extras!!.getString("TEXT", "")
        if (qrText!!.isEmpty()) {
            mTextView!!.visibility = View.INVISIBLE
        } else {
            mQRImage!!.setImageBitmap(CuteR.ProductForResult(qrText))
            mTextView!!.setOnClickListener(
                getClickListener(
                    qrText!!,
                    resources.getStringArray(R.array.qr_list_uri)
                )
            )
            mTextView!!.text = qrText
        }
    }

    private fun getClickListener(text: String, items: Array<String>): View.OnClickListener {
        return View.OnClickListener {
            val builder = AlertDialog.Builder(this@QRCodeResultActivity)
            builder.setTitle(text)
            builder.setItems(items) { dialogInterface, i ->
                handTextAction(i, text)
                dialogInterface.dismiss()
            }
            builder.show()
        }
    }

    private fun handTextAction(pos: Int, text: String) {
        when (pos) {
            0 -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, R.string.qr_copied, Toast.LENGTH_SHORT).show()
            }

            1 -> {
                val data = Intent()
                data.putExtra("import", text)
                setResult(RESULT_OK, data)
                finish()
            }

            2 -> {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_TEXT, text)
                startActivity(Intent.createChooser(i, resources.getString(R.string.share_channel)))
            }

            3 -> {
                var uri = Uri.parse(qrText)
                var url = qrText!!.lowercase()
                if (url.contains("http://") || url.contains("https://")) {
                    val m = Patterns.WEB_URL.matcher(url)
                    if (m.find()) {
                        url = m.group()
                        Log.d(TAG, "find url: $url")
                        uri = Uri.parse(url)
                    } else {
                        Log.w(TAG, "No url found")
                    }
                }

                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    startActivity(intent)
                } catch (ex: ActivityNotFoundException) {
                    Log.e(TAG, "Can not find activity to open the text")
                    Toast.makeText(this, R.string.no_activity, Toast.LENGTH_LONG).show()
                }

            }

            else -> {
            }
        }
    }

    fun isStoragePermissionGranted(request: Int): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, "Permission is granted")
                return true
            } else {
                Log.v(TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    request
                )
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0])

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

}
