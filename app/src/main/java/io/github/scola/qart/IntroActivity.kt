package io.github.scola.qart

import android.content.Context
import android.os.Bundle
import com.chyrta.onboarder.OnboarderActivity
import com.chyrta.onboarder.OnboarderPage
import java.util.ArrayList

class IntroActivity : OnboarderActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onboarderPage1 = OnboarderPage(resources.getString(R.string.black_white), "", R.drawable.guide_img)
        val onboarderPage2 = OnboarderPage(resources.getString(R.string.colorful), "", R.drawable.guide_img_colorful)
        val onboarderPage3 = OnboarderPage(resources.getString(R.string.gif), "", R.drawable.guide_img_gif)
        val onboarderPage4 = OnboarderPage(resources.getString(R.string.logo_title), "", R.drawable.guide_img_logo)
        val onboarderPage5 = OnboarderPage(resources.getString(R.string.embed_title), "", R.drawable.guide_img_embed)

        val pages: MutableList<OnboarderPage> = ArrayList()
        pages.add(onboarderPage1)
        pages.add(onboarderPage2)
        pages.add(onboarderPage3)
        pages.add(onboarderPage4)
        pages.add(onboarderPage5)

        for (page in pages) {
            page.setBackgroundColor(R.color.guide_bg)
            page.setTitleTextSize(18f)
        }
        //        setSkipButtonTitle("Skip");
        setSkipButtonHidden()
        setFinishButtonTitle(resources.getString(R.string.guide_start))
        setOnboardPagesReady(pages)
    }

    override fun onSkipButtonPressed() {
        super.onSkipButtonPressed()
        //        Toast.makeText(this, "Skip button was pressed!", Toast.LENGTH_SHORT).show();
        //        finish();
    }

    override fun onFinishButtonPressed() {
        //        Toast.makeText(this, "Finish button was pressed", Toast.LENGTH_SHORT).show();
        val sharedPref = getSharedPreferences(MainActivity.PREF_GUIDE_VERSION, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(MainActivity.PREF_GUIDE_VERSION, MainActivity.getMyVersion(this))
        editor.commit()

        finish()
    }

    override fun onBackPressed() {
        return
    }
}
