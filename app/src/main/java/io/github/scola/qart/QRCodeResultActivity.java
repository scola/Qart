package io.github.scola.qart;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.github.scola.cuteqr.CuteR;

public class QRCodeResultActivity extends AppCompatActivity {
    private final static String TAG = "QRCodeResultActivity";

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

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String[] items = getResources().getStringArray(R.array.qr_list_uri);

                AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeResultActivity.this);
                builder.setTitle(qrText);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handTextAction(i);
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        qrText = getIntent().getExtras().getString("TEXT", "");
        if (qrText.isEmpty()) {
            mTextView.setVisibility(View.INVISIBLE);
        } else {
            mQRImage.setImageBitmap(CuteR.ProductNormal(qrText, false, Color.BLACK));
            mTextView.setText(qrText);
        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void handTextAction(int pos) {
        switch (pos) {
            case 0:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", qrText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.qr_copied, Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Intent data = new Intent();
                data.putExtra("import", qrText);
                setResult(RESULT_OK, data);
                finish();
                break;
            case 2:
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, qrText);
                startActivity(Intent.createChooser(i, getResources().getString(R.string.share_channel)));
                break;
            case 3:
                Uri uri = Uri.parse(qrText);
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
