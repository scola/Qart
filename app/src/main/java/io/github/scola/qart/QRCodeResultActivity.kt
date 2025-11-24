package io.github.scola.qart;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

import io.github.scola.cuteqr.CuteR;

public class QRCodeResultActivity extends AppCompatActivity {
    private final static String TAG = "QRCodeResultActivity";

    private final static int REQUEST_SAVE_FILE = 5;

    private String qrText;
    private TextView mTextView;
    private ImageView mQRImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_result_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.qr_result);

        mTextView = (TextView) findViewById(R.id.qr_text);
        mQRImage = (ImageView) findViewById(R.id.qr_result_img);

        qrText = getIntent().getExtras().getString("TEXT", "");
        if (qrText.isEmpty()) {
            mTextView.setVisibility(View.INVISIBLE);
        } else {
            mQRImage.setImageBitmap(CuteR.ProductForResult(qrText));
            mTextView.setOnClickListener(getClickListener(qrText, getResources().getStringArray(R.array.qr_list_uri)));
            mTextView.setText(qrText);
        }
    }

    private View.OnClickListener getClickListener (final String text, final String[] items) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v){
                AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeResultActivity.this);
                builder.setTitle(text);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handTextAction(i, text);
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        };
    }

    private void handTextAction(int pos, String text) {
        switch (pos) {
            case 0:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.qr_copied, Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Intent data = new Intent();
                data.putExtra("import", text);
                setResult(RESULT_OK, data);
                finish();
                break;
            case 2:
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(i, getResources().getString(R.string.share_channel)));
                break;
            case 3:
                Uri uri = Uri.parse(qrText);;
                String url = qrText.toLowerCase();
                if (url.contains("http://") || url.contains("https://")) {
                    Matcher m = Patterns.WEB_URL.matcher(url);
                    if (m.find()) {
                        url = m.group();
                        Log.d(TAG, "find url: " + url);
                        uri = Uri.parse(url);
                    } else {
                        Log.w(TAG, "No url found");
                    }
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.e(TAG, "Can not find activity to open the text");
                    Toast.makeText(this, R.string.no_activity, Toast.LENGTH_LONG).show();
                }

                break;

            default:
                break;
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

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
