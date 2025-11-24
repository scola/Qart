package io.github.scola.qart;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;
import java.util.ArrayList;
import java.util.List;
public class IntroActivity extends OnboarderActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnboarderPage onboarderPage1 = new OnboarderPage(getResources().getString(R.string.black_white), "", R.drawable.guide_img);
        OnboarderPage onboarderPage2 = new OnboarderPage(getResources().getString(R.string.colorful), "", R.drawable.guide_img_colorful);
        OnboarderPage onboarderPage3 = new OnboarderPage(getResources().getString(R.string.gif), "", R.drawable.guide_img_gif);
        OnboarderPage onboarderPage4 = new OnboarderPage(getResources().getString(R.string.logo_title), "", R.drawable.guide_img_logo);
        OnboarderPage onboarderPage5 = new OnboarderPage(getResources().getString(R.string.embed_title), "", R.drawable.guide_img_embed);

        List<OnboarderPage> pages = new ArrayList<>();
        pages.add(onboarderPage1);
        pages.add(onboarderPage2);
        pages.add(onboarderPage3);
        pages.add(onboarderPage4);
        pages.add(onboarderPage5);

        for (OnboarderPage page : pages) {
            page.setBackgroundColor(R.color.guide_bg);
            page.setTitleTextSize(18);
        }
//        setSkipButtonTitle("Skip");
        setSkipButtonHidden();
        setFinishButtonTitle(getResources().getString(R.string.guide_start));
        setOnboardPagesReady(pages);
    }
    @Override
    public void onSkipButtonPressed() {
        super.onSkipButtonPressed();
//        Toast.makeText(this, "Skip button was pressed!", Toast.LENGTH_SHORT).show();
//        finish();
    }
    @Override
    public void onFinishButtonPressed() {
//        Toast.makeText(this, "Finish button was pressed", Toast.LENGTH_SHORT).show();
        final SharedPreferences sharedPref = getSharedPreferences(MainActivity.PREF_GUIDE_VERSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(MainActivity.PREF_GUIDE_VERSION, MainActivity.getMyVersion(this));
        editor.commit();

        finish();
    }

    @Override
    public void onBackPressed(){
        return;
    }
}