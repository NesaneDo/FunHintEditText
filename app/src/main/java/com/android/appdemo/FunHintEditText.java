package com.android.appdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class FunHintEditText extends android.support.v7.widget.AppCompatEditText {

    /*
    可点击文字前的文字
     */
    private String hintFormer;
    /*
    可点击的文字
     */
    private String hintValue;
    /*
    可点击文字后的文字
     */
    private String hintLatter;
    /*
    平行线的颜色
     */
    private int colorParallelLine;
    /*
    下划线画笔
     */
    private Paint paintUnderLine;
    /*
    是否需要平行线
     */
    private boolean isDrawParallelLines;
    /*
    可点击部分的前景色
     */
    private int fgClickableRegion;
    /*
    可点击部分的背景色
     */
    private int bgClickableRegion;

    /*
    回调监听的自持有
     */
    private IOnHintValueClickListener listener;
    public FunHintEditText(Context context) {
        this(context,null);
    }

    public FunHintEditText(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FunHintEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //加载自定义属性
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.FunHintEditText,defStyleAttr,0);
        hintFormer = typedArray.getString(R.styleable.FunHintEditText_hintFormer);
        hintValue = typedArray.getString(R.styleable.FunHintEditText_hintValue);
        hintLatter = typedArray.getString(R.styleable.FunHintEditText_hintLatter);

        fgClickableRegion = typedArray.getColor(R.styleable.FunHintEditText_fgHintClickableRegion, getResources().getColor(R.color.cos_darkPink));
        bgClickableRegion = typedArray.getColor(R.styleable.FunHintEditText_bgHintClickableRegion, getResources().getColor(R.color.cos_shallowPink));
        isDrawParallelLines = typedArray.getBoolean(R.styleable.FunHintEditText_drawParallelLines, true);
        colorParallelLine = typedArray.getColor(R.styleable.FunHintEditText_colorParallelLines, getResources().getColor(R.color.cos_blue));

        init();
        customFunction();

        typedArray.recycle();
    }

    /**
     * 一些初始化工作
     */
    private void init(){
        paintUnderLine=new Paint(Paint.ANTI_ALIAS_FLAG);
        if (hintFormer==null){
            hintFormer="";
        }
        if (hintValue==null){
            hintValue="";
        }
        if (hintLatter==null){
            hintLatter="";
        }
    }


    /**
     * 设置可点击部分文字的样式(可自定义）
     */
    private void customFunction(){

        //合并hint:hintFormer+spanHintValue+hintLatter
        String hint = hintFormer + hintValue + hintLatter;

        /*
        可点击的文字的载体
         */
        SpannableString spanHintValue = new SpannableString(hint);

        int start = hintFormer.length();
        int end = hintFormer.length() + hintValue.length();

        //设置spannable属性
        //背景色
        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(bgClickableRegion);
        spanHintValue.setSpan(backgroundColorSpan, start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
        //前景色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(fgClickableRegion);
        spanHintValue.setSpan(foregroundColorSpan, start, end, SPAN_EXCLUSIVE_EXCLUSIVE);

        //设置hint
        setHint(spanHintValue);
    }

    /**
     * 画下划线
     * @param canvas canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDrawParallelLines) {
            paintUnderLine.setColor(colorParallelLine);
            int lineHeight = getLineHeight();
            //如果maxLines不在(0,128)范围,maxLines=64
            int maxLines = getMaxLines() <= 0||getMaxLines()>=128 ? 64 : getMaxLines();
            for (int i = 0; i < maxLines; i++) {
                canvas.drawLine(0, lineHeight * (i + 1) + 2, getWidth(), lineHeight * (i + 1) + 2, paintUnderLine);
            }
        }
    }

    /**
     * 事件处理
     * 实现部分hint可点击
     * @param event event
     * @return true:自己消费，不向上级传递
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //可点击hint的范围
        int pStartX1, pStartY1, pEndX1, pEndY1,pStartX2=0, pStartY2=0, pEndX2=0, pEndY2=0;
        //给EditText设置的文字大小
        int textSize = (int) getTextSize();
        //测量指定文字宽高
        Rect rect = new Rect();
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        //获取hintFormer文字的大小
        paint.getTextBounds(hintFormer, 0, hintFormer.length(), rect);
        int widthHintFormer = rect.width();
        int heightHintFormer = rect.height();
        //hintFormer占多少行
        int lines = widthHintFormer / getMeasuredWidth() + 1;

        //当前手指抬起的时候事件处理
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //获取在当前View上点击的位置
            int dx = (int) event.getX();
            int dy = (int) event.getY();

            //获取每行可以容纳多少个字
            int charsPerLine = getWidth() / textSize;
            //            LogUtil.showLog(getClass().getName(), "coordination: (" + dx + "," + dy + ")");
            //从hintValue所在的一行开始截取字符串
            String measureText = hintFormer;
            if (lines != 1) {
                measureText = hintFormer.substring((lines - 1) * charsPerLine - 1);
            }

            //计算截取后的字符串的宽度
            paint.getTextBounds(measureText, 0, measureText.length(), rect);
            //hintValue的开始范围
            pStartX1 = rect.width() + getPaddingStart() - 30;
            pStartY1 = heightHintFormer * (lines - 1) - 10;

            //计算hintValue的宽度
            paint.getTextBounds(hintValue, 0, hintValue.length(), rect);
            //hintValue的结束范围
            pEndX1 = pStartX1 + rect.width() + 30;
            pEndY1 = heightHintFormer * lines + 10;
            //如果hintValue的结束范围大于了控件宽度，则说明hintValue跨行了，则需要另一个结束
            if (pEndX1>getWidth()){
                pEndX2=pEndX1-getWidth();
                pStartY2=pStartY1+heightHintFormer;
                pEndY2=pEndY1+heightHintFormer;
            }

//                LogUtil.showLog(getClass().getName(), pStartX + ":" + pEndX + ":" + pStartY + ":" + pEndY);

            //在hintValue范围内的点击，自己处理
            if ((dx >= pStartX1 && dx <= pEndX1 && dy >= pStartY1 && dy <= pEndY1)
                    ||(dx >= pStartX2 && dx <= pEndX2 && dy >= pStartY2 && dy <= pEndY2)) {
                //使EditText失去焦点，无法弹出键盘，处理自己的事件
                setFocusable(false);
                setFocusableInTouchMode(false);
                if (listener != null) {
                    listener.onHintClick(this);
                }
                return true;//自己消费事件，不传递给父级
            }
        }
        //在指定范围之外的点击，则设置可获取焦点，并弹出键盘
        setFocusable(true);
        requestFocus();
        setFocusableInTouchMode(true);
        return super.onTouchEvent(event);
    }


    public interface IOnHintValueClickListener{
        void onHintClick(View v);
    }

    /**
     * 更新View
     * 单独的方法，不在setXXX中，减少系统开销
     */
    public void update(){
        customFunction();
    }

    public String getHintFormer() {
        return hintFormer;
    }

    public void setHintFormer(String hintFormer) {
        this.hintFormer = hintFormer;
    }

    public String getHintValue() {
        return hintValue;
    }

    public void setHintValue(String hintValue) {
        this.hintValue = hintValue;
    }

    public String getHintLatter() {
        return hintLatter;
    }

    public void setHintLatter(String hintLatter) {
        this.hintLatter = hintLatter;
    }

    public int getColorParallelLine() {
        return colorParallelLine;
    }

    public void setColorParallelLine(int colorParallelLine) {
        this.colorParallelLine = colorParallelLine;
    }

    public boolean isDrawParallelLines() {
        return isDrawParallelLines;
    }

    public void setDrawParallelLines(boolean drawParallelLines) {
        isDrawParallelLines = drawParallelLines;
    }

    public int getFgClickableRegion() {
        return fgClickableRegion;
    }

    public void setFgClickableRegion(int fgClickableRegion) {
        this.fgClickableRegion = fgClickableRegion;
    }

    public int getBgClickableRegion() {
        return bgClickableRegion;
    }

    public void setBgClickableRegion(int bgClickableRegion) {
        this.bgClickableRegion = bgClickableRegion;
    }

    public IOnHintValueClickListener getListener() {
        return listener;
    }

    public void setOnHintValueClick(IOnHintValueClickListener listener) {
        this.listener = listener;
    }
}
