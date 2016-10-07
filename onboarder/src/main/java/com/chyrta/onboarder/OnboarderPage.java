package com.chyrta.onboarder;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public class OnboarderPage {

    public String title;
    public String description;
    public Drawable imageResource;
    @StringRes public int titleResourceId;
    @StringRes public int descriptionResourceId;
    @DrawableRes public int imageResourceId;
    @ColorRes public int titleColor;
    @ColorRes public int descriptionColor;
    @ColorRes public int backgroundColor;
    public float titleTextSize;
    public float descriptionTextSize;

    public OnboarderPage(String title, String description) {
        this.title = title;
        this.description = description;
        this.backgroundColor = R.color.black_transparent;
    }

    public OnboarderPage(String title, String description, int imageResourceId) {
        this.title = title;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.backgroundColor = R.color.black_transparent;
    }

    public OnboarderPage(String title, String description, Drawable imageResource) {
        this.title = title;
        this.description = description;
        this.imageResource = imageResource;
        this.backgroundColor = R.color.black_transparent;
    }

    public OnboarderPage(int title, int description) {
        this.titleResourceId = title;
        this.descriptionResourceId = description;
        this.backgroundColor = R.color.black_transparent;
    }

    public OnboarderPage(int title, int description, int imageResourceId) {
        this.titleResourceId = title;
        this.descriptionResourceId = description;
        this.imageResourceId = imageResourceId;
        this.backgroundColor = R.color.black_transparent;
    }

    public OnboarderPage(int title, int description, Drawable imageResource) {
        this.titleResourceId = title;
        this.descriptionResourceId = description;
        this.imageResource = imageResource;
        this.backgroundColor = R.color.black_transparent;
    }

    public String getTitle() {
        return title;
    }

    public int getTitleResourceId() {
        return titleResourceId;
    }

    public String getDescription() {
        return description;
    }

    public int getDescriptionResourceId() {
        return descriptionResourceId;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public int getDescriptionColor() {
        return descriptionColor;
    }

    public void setTitleColor(int color) {
        this.titleColor = color;
    }

    public void setDescriptionColor(int color) {
        this.descriptionColor = color;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public float getTitleTextSize() {
        return titleTextSize;
    }

    public void setTitleTextSize(float titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    public float getDescriptionTextSize() {
        return descriptionTextSize;
    }

    public void setDescriptionTextSize(float descriptionTextSize) {
        this.descriptionTextSize = descriptionTextSize;
    }

}
