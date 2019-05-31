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

    /**
     * 轮播默认延时跳转时间单位ms
     */
    private static final int DEFAULT_DELAY_TIME_UNIT = 1000;
    /**
     * 轮播默认延时跳转时间5s
     */
    private static final int DEFAULT_DELAY_TIME = 5;
    /**
     * 轮播默认延时跳转时间5s
     */
    private static final int DEFAULT_INDICATOR_BG_COLOR = 0X33000000;

    /**
     * 视图切换ViewPager
     */
    private ViewPager mViewPager;
    /**
     * 指示器父布局
     */
    private LinearLayout mIndicatorLayout;
    /**
     * 指示器根布局
     */
    private LinearLayout mIndicatorParent;
    /**
     * 轮播内容名称
     */
    private TextView mBannerTv;
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 指示器图标数据
     */
    private ImageView[] mIndicatorArray;
    /**
     * 指示器选中状态图片资源id
     */
    private int mDotSelectRes;
    /**
     * 指示器未选中状态图片资源id
     */
    private int mDotUnSelectRes;
    /**
     * 是否自动轮播，默认支持
     */
    private boolean isAutoLoop;
    /**
     * indicator 距离左边的距离
     */
    private int mIndicatorPaddingLeft = 0;
    /**
     * indicator 距离右边的距离
     */
    private int mIndicatorPaddingRight = 0;
    /**
     * indicator 距离上边的距离
     */
    private int mIndicatorPaddingTop = 0;
    /**
     * indicator 距离下边的距离
     */
    private int mIndicatorPaddingBottom = 0;
    /**
     * indicator Indicator背景色
     */
    private int mIndicatorBgColor;
    /**
     * indicator Indicator背景色是否显示
     */
    private boolean isIndicatorBgShow;

    /**
     * 轮播内容数据集
     */
    private List<BannerEntry> mEntryList = new ArrayList<>();
    /**
     * 轮播内容总量
     */
    private int mItemCount;
    /**
     * 轮播延时跳转时间
     */
    private int mDelayTime = DEFAULT_DELAY_TIME;
    /**
     * item点击事件监听回调
     */
    private OnBannerItemClickListener mOnBannerItemClickListener;


    /**
     * 轮播开启
     */
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAutoLoop) return;
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            postDelayed(this, mDelayTime * DEFAULT_DELAY_TIME_UNIT);
        }
    };

    /**
     * 点击事件监听接口
     */
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
        initView();
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        readAttrs(context, attrs);
        initView();
    }

    /**
     * 属性获取
     *
     * @param context 上下文
     * @param attrs   属性参数
     */
    private void readAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        isAutoLoop = typedArray.getBoolean(R.styleable.BannerView_autoLoop, true);
        mIndicatorPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingLeft, 6);
        mIndicatorPaddingRight = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingRight, 6);
        mIndicatorPaddingTop = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingTop, 0);
        mIndicatorPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicatorPaddingBottom, 0);
        mDotSelectRes = typedArray.getResourceId(R.styleable.BannerView_indicatorSelectRes, R.drawable.dot_select_image);
        mDotUnSelectRes = typedArray.getResourceId(R.styleable.BannerView_indicatorUnSelectRes, R.drawable.dot_unselect_image);
        mDelayTime = typedArray.getInteger(R.styleable.BannerView_indicatorDelayTime, DEFAULT_DELAY_TIME);
        mIndicatorBgColor = typedArray.getColor(R.styleable.BannerView_indicatorBgColor, DEFAULT_INDICATOR_BG_COLOR);
        isIndicatorBgShow = typedArray.getBoolean(R.styleable.BannerView_indicatorBgShow, true);
        typedArray.recycle();
    }

    /**
     * 初始化view资源
     */
    private void initView() {
        View view = View.inflate(mContext, R.layout.layout_bannerview, this);
        mViewPager = view.findViewById(R.id.viewpager);
        mIndicatorLayout = view.findViewById(R.id.ll_points);
        mIndicatorParent = view.findViewById(R.id.indicator_parent);
        mBannerTv = view.findViewById(R.id.banner_text);
        if (!isIndicatorBgShow) mIndicatorBgColor = android.R.color.transparent;
        mIndicatorParent.setBackgroundColor(mIndicatorBgColor);
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
            initAdapter();
        }
    }

    /**
     * banner item的点击监听设置
     *
     * @param onBannerItemClickListener 监听器
     */
    public void setOnBannerItemClickListener(OnBannerItemClickListener onBannerItemClickListener) {
        mOnBannerItemClickListener = onBannerItemClickListener;
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
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

    /**
     * 初始化底部指示器
     *
     * @param context 上下文
     */
    private void initIndicator(Context context) {
        mIndicatorArray = new ImageView[mItemCount];
        for (int i = 0; i < mItemCount; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
            params.setMargins(mIndicatorPaddingLeft, mIndicatorPaddingTop,
                    mIndicatorPaddingRight, mIndicatorPaddingBottom);
            ImageView imageView = new ImageView(context);
            mIndicatorArray[i] = imageView;
            imageView.setBackgroundResource(i == 0 ? mDotSelectRes : mDotUnSelectRes);
            mIndicatorLayout.addView(imageView, params);
        }
        mIndicatorLayout.setVisibility(mItemCount == 1 ? View.GONE : View.VISIBLE);
    }

    /**
     * 底部指示器变化
     *
     * @param currentPos 指示器位置
     */
    private void switchIndicator(int currentPos) {
        for (int i = 0; i < mIndicatorArray.length; i++) {
            mIndicatorArray[i].setBackgroundResource(i == currentPos ?
                    mDotSelectRes : mDotUnSelectRes);
        }
    }

    /**
     * 轮播图片名称变化
     *
     * @param currentPos 轮播位置
     */
    private void switchBannerName(int currentPos) {
        if (mEntryList != null && mEntryList.size() > currentPos) {
            mBannerTv.setText(mEntryList.get(currentPos).getName());
        }
    }

    /**
     * 开启循环
     */
    private void startRecycle() {
        if (!isAutoLoop) return;
        postDelayed(mRunnable, 5000);
    }

    /**
     * 终止循环
     */
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

    /**
     * 轮播适配器
     */
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
            return entrySize == 1 ? 1 : Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
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
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
