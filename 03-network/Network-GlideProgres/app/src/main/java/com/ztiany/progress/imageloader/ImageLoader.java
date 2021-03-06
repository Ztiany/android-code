package com.ztiany.progress.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;

// @formatter:off
public interface ImageLoader {

    void display(Fragment fragment, ImageView imageView, String url, LoadListener<Drawable> loadListener);

    void display(Fragment fragment, ImageView imageView, String url, DisplayConfig displayConfig, LoadListener<Drawable> loadListener);

    void display(ImageView imageView, String url, LoadListener<Drawable> loadListener);

    void display(ImageView imageView, String url, DisplayConfig config, LoadListener<Drawable> loadListener);

    void display(Fragment fragment, ImageView imageView, String url);

    void display(Fragment fragment, ImageView imageView, String url, DisplayConfig displayConfig);

    void display(Fragment fragment, ImageView imageView, Source source);

    void display(Fragment fragment, ImageView imageView, Source source, DisplayConfig displayConfig);

    void display(ImageView imageView, String url);

    void display(ImageView imageView, String url, DisplayConfig config);

    void display(ImageView imageView, Source source);

    void display(ImageView imageView, Source source, DisplayConfig config);


    ///////////////////////////////////////////////////////////////////////////
    // pause and resume
    ///////////////////////////////////////////////////////////////////////////
    void pause(Fragment fragment);

    void resume(Fragment fragment);

    void pause(Context context);

    void resume(Context context);

    ///////////////////////////////////////////////////////////////////////////
    // preload
    ///////////////////////////////////////////////////////////////////////////
    void preload(Context context, Source source);

    void preload(Context context, Source source, int width, int height);

    ///////////////////////////////////////////////////////////////////////////
    // LoadBitmap
    ///////////////////////////////////////////////////////////////////////////
    void loadBitmap(Context context, Source source, boolean cache, LoadListener<Bitmap> bitmapLoadListener);

    void loadBitmap(Fragment fragment, Source source, boolean cache, LoadListener<Bitmap> bitmapLoadListener);

    void loadBitmap(Context context, Source source, boolean cache, int width, int height, LoadListener<Bitmap> bitmapLoadListener);

    void loadBitmap(Fragment fragment, Source source, boolean cache, int width, int height, LoadListener<Bitmap> bitmapLoadListener);

    @WorkerThread
    Bitmap loadBitmap(Context context, Source source, boolean cache, int width, int height);

    ///////////////////////////////////////////////////////////////////////////
    // clear
    ///////////////////////////////////////////////////////////////////////////
    @WorkerThread
    void clear(Context context);

    void clear(View view);

    void clear(Fragment fragment, View view);

    ///////////////////////////////////////////////////////////////////////////
    // progress
    ///////////////////////////////////////////////////////////////////////////

    /**
     * ???????????? URL ??????????????????
     *
     * @param url URL
     */
    @UiThread
    void removeAllListener(String url);

    /**
     * ?????????????????? URL ?????????????????????????????? URL ??????????????? ProgressListener????????????????????????????????????????????????
     *
     * @param url              URL
     * @param progressListener ?????????
     */
    @UiThread
    void addListener(@NonNull String url, @NonNull ProgressListener progressListener);

    /**
     * ?????????????????? URL ?????????????????????????????? URL ??????????????? ProgressListener?????????????????? ProgressListener ????????????????????????{@link #addListener(String, ProgressListener)}????????????
     *
     * @param url              URL
     * @param progressListener ?????????????????? progressListener ??? null??????????????????
     */
    void setListener(String url, @Nullable ProgressListener progressListener);
}
