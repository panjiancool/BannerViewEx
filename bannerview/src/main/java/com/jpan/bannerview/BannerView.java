package com.jpan.bannerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jpan.bannerview.entry.BannerEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GaryPan
 * @date 2019/5/30 - 21:35
 */
public class BannerView extends RelativeLayout {
    private ViewPager mViewPager;
    private LinearLayout mLinearLayout;
    private TextView mBannerTv;
    private Context mContext;
    private ImageView[] mIndicator;
    private int mDotSelectRes;
    private int mDotUnselectRes;
    private boolean isAutoLoop;
    private int mIndicatorPaddingLeft = 0;// indicator 距离左边的距离
    private int mIndicatorPaddingRight = 0;//indicator 距离右边的距离
    private int mIndicatorPaddingTop = 0;//indicator 距离上边的距离
    private int mIndicatorPaddingBottom = 0;//indicator 距离下边的距离

    private List<BannerEntry> mEntryList = new ArrayList<>();
    private OnBannerItemClickListener mOnBannerItemClickListener;
    private int mItemCount;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAutoLoop) return;
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            postDelayed(mRunnable, 5000);
        }
    };

    public interface OnBannerItemClickListener {
        void onClick(int position);
    }

    public BannerView(Context context) {
        this(context, null);
        this.mContext = context;
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        readAttrs(context, attrs);
        init();
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        readAttrs(context, attrs);
        init();
    }

    private void readAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        isAutoLoop = typedArray.getBoolean(R.styleable.BannerView_autoLoop, true);
        mIndicatorPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingLeft, 6);
        mIndicatorPaddingRight = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingRight, 6);
        mIndicatorPaddingTop = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingTop, 0);
        mIndicatorPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingBottom, 0);
        mDotSelectRes = typedArray.getResourceId(R.styleable.BannerView_indicatorSelectRes, R.drawable.dot_select_image);
        mDotUnselectRes = typedArray.getResourceId(R.styleable.BannerView_indicatorUnSelectRes, R.drawable.dot_unselect_image);
        typedArray.recycle();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.layout_bannerview, this);
        mViewPager = view.findViewById(R.id.viewpager);
        mLinearLayout = view.findViewById(R.id.ll_points);
        mBannerTv = view.findViewById(R.id.banner_text);
    }

    /**
     * 数据源设置
     *
     * @param entryList 数据源
     */
    public void setEntryList(List<BannerEntry> entryList) {
        if (entryList != null && !entryList.isEmpty()) {
            mEntryList.clear();
            mEntryList.addAll(entryList);
            mItemCount = entryList.size();
            initView();
        }
    }

    /**
     * banner item的点击监听
     *
     * @param onBannerItemClickListener 监听器
     */
    public void setOnBannerItemClickListener(OnBannerItemClickListener onBannerItemClickListener) {
        mOnBannerItemClickListener = onBannerItemClickListener;
    }

    private void initView() {
        // 给viewpager设置adapter
        BannerPagerAdapter bannerPagerAdapter = new BannerPagerAdapter(mEntryList, mContext);
        mViewPager.setAdapter(bannerPagerAdapter);
        // 初始化底部点指示器
        initIndicator(mContext);
        mViewPager.setCurrentItem(500 * mItemCount);
        mBannerTv.setText(mEntryList.get(0).getName());

        // 给viewpager设置滑动监听
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switchIndicator(position % mItemCount);
                switchBannerName(position % mItemCount);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancelRecycle();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                startRecycle();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void initIndicator(Context context) {
        mIndicator = new ImageView[mItemCount];
        for (int i = 0; i < mIndicator.length; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
            params.setMargins(mIndicatorPaddingLeft, mIndicatorPaddingTop,
                    mIndicatorPaddingRight, mIndicatorPaddingBottom);
            ImageView imageView = new ImageView(context);
            mIndicator[i] = imageView;
            imageView.setBackgroundResource(i == 0 ? mDotSelectRes : mDotUnselectRes);
            mLinearLayout.addView(imageView, params);
        }
        mLinearLayout.setVisibility(mItemCount == 1 ? View.GONE : View.VISIBLE);
    }

    private void switchIndicator(int selectItems) {
        for (int i = 0; i < mIndicator.length; i++) {
            mIndicator[i].setBackgroundResource(i == selectItems ?
                    mDotSelectRes : mDotUnselectRes);
        }
    }

    private void switchBannerName(int currentPos) {
        if (mEntryList != null && mEntryList.size() > currentPos) {
            mBannerTv.setText(mEntryList.get(currentPos).getName());
        }
    }

    private void startRecycle() {
        if (!isAutoLoop) return;
        postDelayed(mRunnable, 5000);
    }

    private void cancelRecycle() {
        removeCallbacks(mRunnable);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            startRecycle();
        } else {
            cancelRecycle();
        }
    }


    private class BannerPagerAdapter extends PagerAdapter {
        private List<BannerEntry> entryList;
        private Context context;
        private int entrySize;

        private BannerPagerAdapter(List<BannerEntry> entryList, Context context) {
            this.entryList = entryList;
            this.context = context;
            entrySize = entryList.size();
        }

        @Override
        public int getCount() {
            return mItemCount == 1 ? 1 : Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            final int realPosition = Math.abs(position) % entrySize;
            BannerEntry entry = entryList.get(realPosition);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setBackgroundResource(entry.getResId());
            container.addView(imageView);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnBannerItemClickListener != null) {
                        mOnBannerItemClickListener.onClick(realPosition);
                    }
                }
            });
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
