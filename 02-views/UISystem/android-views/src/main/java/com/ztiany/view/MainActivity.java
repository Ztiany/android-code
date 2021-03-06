package com.ztiany.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ztiany.recyclerview.RvMainActivity;
import com.ztiany.view.animation.circular_reveal.CircularRevealActivity;
import com.ztiany.view.animation.reversal.ReversalActivity;
import com.ztiany.view.animation.spring.SpringScrollViewFragment;
import com.ztiany.view.animation.square.SquareAnimationFragment;
import com.ztiany.view.bitmap.BitmapActivity;
import com.ztiany.view.constraint.ConstraintLayoutActivity;
import com.ztiany.view.courses.hencoderplus.HenCoderPlusFragment;
import com.ztiany.view.courses.hencoderplus.viewroot.ViewRootActivity;
import com.ztiany.view.custom.CustomViewFragment;
import com.ztiany.view.custom.flow_layout.FlowLayoutFragment;
import com.ztiany.view.custom.message_drag.MessageDragFragment;
import com.ztiany.view.custom.pull_refresh.PullToRefreshFragment;
import com.ztiany.view.custom.view_drag_helper.ViewDragHelperFragment;
import com.ztiany.view.custom.view_pager.ViewPagerFragment;
import com.ztiany.view.dialog.DialogsActivity;
import com.ztiany.view.draw.CanvasFragment;
import com.ztiany.view.draw.MatrixFragment;
import com.ztiany.view.draw.PathFragment;
import com.ztiany.view.draw.camera.Camera3DFragment;
import com.ztiany.view.draw.camera.Camera3DTheoryFragment;
import com.ztiany.view.draw.camera.CameraDemoViewFragment;
import com.ztiany.view.draw.color.ColorMatrixFilterFragment;
import com.ztiany.view.draw.color.MaskFilterFragment;
import com.ztiany.view.draw.overdraw.OverDrawFragment;
import com.ztiany.view.draw.text.SimpleTextGradualFragment;
import com.ztiany.view.draw.text.TextGradualViewPagerFragment;
import com.ztiany.view.draw.text.TextMeasureFragment;
import com.ztiany.view.drawable.DrawableBitmapFragment;
import com.ztiany.view.drawable.DrawableLayerFragment;
import com.ztiany.view.drawable.DrawableRotateFragment;
import com.ztiany.view.drawable.DrawableSelectorFragment;
import com.ztiany.view.drawable.DrawableVectorFragment;
import com.ztiany.view.drawable.FishDrawableFragment;
import com.ztiany.view.drawable.SVGChinaFragment;
import com.ztiany.view.inflater.LayoutInflaterActivity;
import com.ztiany.view.scroll.ScrollFragment;
import com.ztiany.view.scroll.sticky.StickyNavigationFragment;
import com.ztiany.view.window.RealWindowSizeActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final List<Item> LIST = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setContentInsetStartWithNavigation(0);

        RecyclerView recyclerView = findViewById(R.id.activity_main);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(new ItemAdapter(this, LIST));
    }

    static {
        LIST.add(new Item("RecyclerView ??????", RvMainActivity.class));

        LIST.add(new Item("HenCoderPlus ??????", HenCoderPlusFragment.class));
        LIST.add(new Item("HenCoderPlus ViewRoot", ViewRootActivity.class));

        LIST.add(new Item("Bitmap??????", BitmapActivity.class));
        LIST.add(new Item("?????? & ??????", ScrollFragment.class));
        LIST.add(new Item("Sticky Navigation", StickyNavigationFragment.class));
        LIST.add(new Item("Canvas ??????", CanvasFragment.class));
        LIST.add(new Item("??????", MatrixFragment.class));
        LIST.add(new Item("ColorMatrixFilter", ColorMatrixFilterFragment.class));
        LIST.add(new Item("MaskFilter", MaskFilterFragment.class));
        LIST.add(new Item("Path &???????????????", PathFragment.class));
        LIST.add(new Item("Camera ??????", CameraDemoViewFragment.class));
        LIST.add(new Item("Camera3D View", Camera3DFragment.class));
        LIST.add(new Item("Camera3D ??????", Camera3DTheoryFragment.class));
        LIST.add(new Item("????????????", SimpleTextGradualFragment.class));
        LIST.add(new Item("????????????", TextMeasureFragment.class));
        LIST.add(new Item("???????????? ViewPager", TextGradualViewPagerFragment.class));
        LIST.add(new Item("????????????", OverDrawFragment.class));

        LIST.add(new Item("ViewDragHelper??????", ViewDragHelperFragment.class));
        LIST.add(new Item("????????? View", CustomViewFragment.class));
        LIST.add(new Item("???????????????", MessageDragFragment.class));
        LIST.add(new Item("????????????", FlowLayoutFragment.class));
        LIST.add(new Item("????????????", PullToRefreshFragment.class));

        LIST.add(new Item("Spring??????", SpringScrollViewFragment.class));
        LIST.add(new Item("CircularReveal??????", CircularRevealActivity.class));
        LIST.add(new Item("????????????", ReversalActivity.class));

        LIST.add(new Item("BitmapDrawable", DrawableBitmapFragment.class));
        LIST.add(new Item("LayerDrawable", DrawableLayerFragment.class));
        LIST.add(new Item("RotateDrawable", DrawableRotateFragment.class));
        LIST.add(new Item("SelectorDrawable", DrawableSelectorFragment.class));
        LIST.add(new Item("VectorDrawable", DrawableVectorFragment.class));
        LIST.add(new Item("FishDrawable", FishDrawableFragment.class));
        LIST.add(new Item("SVG China Map", SVGChinaFragment.class));

        LIST.add(new Item("Dialog", DialogsActivity.class));
        LIST.add(new Item("Window Size", RealWindowSizeActivity.class));

        LIST.add(new Item("LayoutInflaterCompat", LayoutInflaterActivity.class));
        LIST.add(new Item("????????????", SquareAnimationFragment.class));

        LIST.add(new Item("ConstraintLayout ??????", ConstraintLayoutActivity.class));
        LIST.add(new Item("ViewPager ????????????", ViewPagerFragment.class));
    }

}