package com.moko.bxp.nordic.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.nordic.R;

import java.util.ArrayList;
import java.util.List;

public class THChartView extends View {
    //xy坐标轴颜色
    private int xylinecolor = Color.GRAY;
    //xy坐标轴宽度
    private int xylinewidth = dpToPx(2);
    //xy坐标轴文字颜色
    private int xytextcolor = Color.GRAY;
    //xy坐标轴文字大小
    private int xytextsize = spToPx(12);
    //折线图中折线的颜色
    private int linecolor = Color.BLUE;
    //x轴各个坐标点水平间距
    private float interval = dpToPx(1);
    //是否在ACTION_UP时，根据速度进行自滑动，没有要求，建议关闭，过于占用GPU
//    private boolean isScroll = false;
    //绘制XY轴坐标对应的画笔
    private Paint xyPaint;
    //绘制XY轴的文本对应的画笔
    private Paint xyTextPaint;
    //画折线对应的画笔
    private Paint linePaint;
    private int bgColor = Color.TRANSPARENT;
    //画背景对应的画笔
    private Paint bgPaint;
    private int width;
    private int height;
    //x轴的原点坐标
    private int xOri;
    //y轴的原点坐标
    private int yOri;
    //折线绘制原点
    private float ylineOri;
    //折线绘制区域高度
    private float lineDrawHeight;
    //第一个点X的坐标
    private float xInit;
    //第一个点对应的最大X坐标
    private float maxXInit;
    //第一个点对应的最小X坐标
    private float minXInit;
    //y轴文字描述
    private String ylineDesc = "Temperature(℃)";
    //y轴的文字描述对应的画笔
    private Paint ylineDescPaint;
    //y轴的文字描述大小
    private int ylineDescsize = spToPx(14);
    //y轴的文字描述宽高
    private float ylineDescWidth;
    private float ylineDescHeight;
    //x轴坐标对应的数据
    private List<Float> xValue = new ArrayList<>();
    //y轴坐标对应的数据
    private List<String> yValue = new ArrayList<>();
    //折线对应的数据
//    private Map<String, Integer> value = new HashMap<>();
    //点击的点对应的X轴的第几个点，默认1
//    private int selectIndex = 1;
    //X轴刻度文本对应的最大矩形，为了选中时，在x轴文本画的框框大小一致
//    private Rect xValueRect;
    //速度检测器
//    private VelocityTracker velocityTracker;
    private float minValue;
    private float maxValue;
    private float diffValue;
    private boolean canScroll;

    public THChartView(Context context) {
        this(context, null);
    }

    public THChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public THChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        initPaint();
    }

    /**
     * 初始化畫筆
     */
    private void initPaint() {
        xyPaint = new Paint();
        xyPaint.setAntiAlias(true);
        xyPaint.setStrokeWidth(xylinewidth);
        xyPaint.setStrokeCap(Paint.Cap.ROUND);
        xyPaint.setColor(xylinecolor);

        xyTextPaint = new Paint();
        xyTextPaint.setAntiAlias(true);
        xyTextPaint.setTextSize(xytextsize);
        xyTextPaint.setStrokeCap(Paint.Cap.ROUND);
        xyTextPaint.setColor(xytextcolor);
        xyTextPaint.setStyle(Paint.Style.FILL);

        ylineDescPaint = new Paint();
        ylineDescPaint.setAntiAlias(true);
        ylineDescPaint.setTextSize(ylineDescsize);
        ylineDescPaint.setStrokeCap(Paint.Cap.ROUND);
        ylineDescPaint.setColor(xytextcolor);
        ylineDescPaint.setStyle(Paint.Style.FILL);
        ylineDescPaint.setFakeBoldText(true);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(xylinewidth);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setColor(linecolor);
        linePaint.setStyle(Paint.Style.STROKE);

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStrokeWidth(xylinewidth);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.chartView, defStyleAttr, 0);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.chartView_xylinecolor) {//xy坐标轴颜色
                xylinecolor = array.getColor(attr, xylinecolor);
            }
            if (attr == R.styleable.chartView_xylinewidth) {//xy坐标轴宽度
                xylinewidth = (int) array.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, xylinewidth, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.chartView_xytextcolor) {//xy坐标轴文字颜色
                xytextcolor = array.getColor(attr, xytextcolor);
            } else if (attr == R.styleable.chartView_xytextsize) {//xy坐标轴文字大小
                xytextsize = (int) array.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, xytextsize, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.chartView_linecolor) {//折线图中折线的颜色
                linecolor = array.getColor(attr, linecolor);
            } else if (attr == R.styleable.chartView_ylineDesc) {//折线图中折线的颜色
                ylineDesc = array.getString(attr);
            }
        }
        array.recycle();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        if (changed) {
        //这里需要确定几个基本点，只有确定了xy轴原点坐标，第一个点的X坐标值及其最大最小值
        width = getWidth();
        height = getHeight();
        //Y轴描述文本宽高
        Rect ylineRect = getTextBounds(ylineDesc, ylineDescPaint);
        ylineDescWidth = ylineRect.width();
        ylineDescHeight = ylineRect.height();
        yValue.clear();
        //Y轴文本最大宽度
        float textYWdith = getTextBounds("-000.0", xyTextPaint).width();
        for (int i = 0; i < yValue.size(); i++) {//求取y轴文本最大的宽度
            float temp = getTextBounds(String.valueOf(yValue.get(i)), xyTextPaint).width();
            if (temp > textYWdith)
                textYWdith = temp;
        }
        int dp5 = dpToPx(5);
        xOri = (int) (dp5 * 2 + textYWdith + xylinewidth + ylineDescHeight);//dp2是y轴文本距离左边，以及距离y轴的距离
        // 计算y轴刻度
        if (xValue.size() > 0) {
            minValue = xValue.get(0);
            maxValue = xValue.get(0);
            for (int i = 0; i < xValue.size(); i++) {
                float value = xValue.get(i);
                if (value < minValue) {
                    minValue = value;
                } else if (value > maxValue) {
                    maxValue = value;
                }
            }
            if (minValue == maxValue) {
                for (int i = 0; i < 5; i++) {
                    yValue.add(String.valueOf(minValue));
                }
            } else {
                diffValue = (maxValue - minValue) / 2;
                yValue.add(MokoUtils.getDecimalFormat("#.0").format(minValue - diffValue));
                yValue.add(String.valueOf(minValue));
                yValue.add(MokoUtils.getDecimalFormat("#.0").format(minValue + diffValue));
                yValue.add(String.valueOf(maxValue));
                yValue.add(MokoUtils.getDecimalFormat("#.0").format(maxValue + diffValue));
            }
        }

        //计算两点之间的间隔
        int size = xValue.size();
        if (size <= 1000) {
            interval = (width - xOri) * 1.0f / size;
        } else {
            canScroll = true;
            interval = (width - xOri) * 1.0f / 1000;
        }

        yOri = height - xylinewidth;//dp3是x轴文本距离底边，dp2是x轴文本距离x轴的距离
        lineDrawHeight = yOri * 1.0f / 2;
        ylineOri = yOri * 3.0f / 4;
        xInit = xOri;
        minXInit = width - interval * (size - 1);
        maxXInit = xInit;
//        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectF = new RectF(0, 0, width, height);
        canvas.drawRect(rectF, bgPaint);
        drawYLineDesc(canvas);

        drawXY(canvas);
        drawBrokenLineAndPoint(canvas);
    }

    private void drawYLineDesc(Canvas canvas) {
        canvas.save();
        canvas.rotate(-90, 0, 0);
        canvas.translate(-height, 0);
        canvas.drawText(ylineDesc, (height - ylineDescWidth) / 2, ylineDescHeight, ylineDescPaint);
        canvas.restore();
    }

    /**
     * 绘制折线和折线交点处对应的点
     *
     * @param canvas
     */
    private void drawBrokenLineAndPoint(Canvas canvas) {
        if (xValue.size() <= 0)
            return;
        //重新开一个图层
        int layerId = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
        drawBrokenLine(canvas);
//        drawBrokenPoint(canvas);

        bgPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        RectF rectF = new RectF(0, 0, xOri, height);
        // 将折线超出x轴坐标的部分截取掉
//        linePaint.setColor(Color.WHITE);
        canvas.drawRect(rectF, bgPaint);
        bgPaint.setXfermode(null);
        //保存图层
        canvas.restoreToCount(layerId);
    }

    /**
     * 绘制折线对应的点
     *
     * @param canvas
     */
//    private void drawBrokenPoint(Canvas canvas) {
//        float dp2 = dpToPx(2);
//        float dp4 = dpToPx(4);
//        float dp7 = dpToPx(7);
//        //绘制节点对应的原点
//        for (int i = 0; i < xValue.size(); i++) {
//            float x = xInit + interval * i;
//            float y = yOri - yOri  * value.get(xValue.get(i)) / yValue.get(yValue.size() - 1);
//            //绘制选中的点
//            if (i == selectIndex - 1) {
//                linePaint.setStyle(Paint.Style.FILL);
//                linePaint.setColor(0xffd0f3f2);
//                canvas.drawCircle(x, y, dp7, linePaint);
//                linePaint.setColor(0xff81dddb);
//                canvas.drawCircle(x, y, dp4, linePaint);
//                drawFloatTextBox(canvas, x, y - dp7, value.get(xValue.get(i)));
//            }
//            //绘制普通的节点
//            linePaint.setStyle(Paint.Style.FILL);
//            linePaint.setColor(Color.WHITE);
//            canvas.drawCircle(x, y, dp2, linePaint);
//            linePaint.setStyle(Paint.Style.STROKE);
//            linePaint.setColor(linecolor);
//            canvas.drawCircle(x, y, dp2, linePaint);
//
//        }
//    }

    /**
     * 绘制显示Y值的浮动框
     *
     * @param canvas
     * @param x
     * @param y
     * @param text
     */
//    private void drawFloatTextBox(Canvas canvas, float x, float y, int text) {
//        int dp6 = dpToPx(6);
//        int dp18 = dpToPx(18);
//        //p1
//        Path path = new Path();
//        path.moveTo(x, y);
//        //p2
//        path.lineTo(x - dp6, y - dp6);
//        //p3
//        path.lineTo(x - dp18, y - dp6);
//        //p4
//        path.lineTo(x - dp18, y - dp6 - dp18);
//        //p5
//        path.lineTo(x + dp18, y - dp6 - dp18);
//        //p6
//        path.lineTo(x + dp18, y - dp6);
//        //p7
//        path.lineTo(x + dp6, y - dp6);
//        //p1
//        path.lineTo(x, y);
//        canvas.drawPath(path, linePaint);
//        linePaint.setColor(Color.WHITE);
//        linePaint.setTextSize(spToPx(14));
//        Rect rect = getTextBounds(text + "", linePaint);
//        canvas.drawText(text + "", x - rect.width() / 2, y - dp6 - (dp18 - rect.height()) / 2, linePaint);
//    }

    /**
     * 绘制折线
     *
     * @param canvas
     */
    private void drawBrokenLine(Canvas canvas) {
//        linePaint.setColor(linecolor);
        //绘制折线
        Path path = new Path();
        float x = xInit;
        float y;
        if (maxValue == minValue) {
            y = ylineOri;
        } else {
            y = ylineOri - lineDrawHeight * ((xValue.get(0) - minValue) / (maxValue - minValue));
        }
        path.moveTo(x, y);
        for (int i = 1; i < xValue.size(); i++) {
            x = xInit + interval * i;
            if (maxValue == minValue) {
                y = ylineOri;
            } else {
                y = ylineOri - lineDrawHeight * ((xValue.get(i) - minValue) / (maxValue - minValue));
            }
            path.lineTo(x, y);
//            canvas.drawLine(x, y, x, yOri, linePaint);
        }
        canvas.drawPath(path, linePaint);
    }

    /**
     * 绘制XY坐标
     *
     * @param canvas
     */
    private void drawXY(Canvas canvas) {
        int length = dpToPx(1);//刻度的长度
        //绘制Y坐标
        canvas.drawLine(xOri - xylinewidth / 2, 0, xOri - xylinewidth / 2, yOri, xyPaint);
//        //绘制y轴箭头
//        xyPaint.setStyle(Paint.Style.STROKE);
//        Path path = new Path();
//        path.moveTo(xOri - xylinewidth / 2 - dpToPx(5), dpToPx(12));
//        path.lineTo(xOri - xylinewidth / 2, xylinewidth / 2);
//        path.lineTo(xOri - xylinewidth / 2 + dpToPx(5), dpToPx(12));
//        canvas.drawPath(path, xyPaint);
        //绘制y轴刻度
        int yScale = yOri / (yValue.size() - 1);
        for (int i = 0; i < yValue.size(); i++) {
            //绘制Y轴刻度
            canvas.drawLine(xOri, yOri - yScale * i, xOri + length, yOri - yScale * i, xyPaint);
//            xyTextPaint.setColor(xytextcolor);
            //绘制Y轴文本
            String text = yValue.get(i);
            Rect rect = getTextBounds(text, xyTextPaint);
            if (i == 0) {
                canvas.drawText(text, 0, text.length(), xOri - xylinewidth - dpToPx(5) - rect.width(), yOri - xylinewidth, xyTextPaint);
                continue;
            } else if (i == 4) {
                canvas.drawText(text, 0, text.length(), xOri - xylinewidth - dpToPx(5) - rect.width(), rect.height() + xylinewidth, xyTextPaint);
                continue;
            } else {
                canvas.drawText(text, 0, text.length(), xOri - xylinewidth - dpToPx(5) - rect.width(), yOri - xylinewidth - yScale * i + rect.height() / 2, xyTextPaint);
                continue;
            }
        }
        //绘制X轴坐标
//        canvas.drawLine(xOri, yOri + xylinewidth / 2, width, yOri + xylinewidth / 2, xyPaint);
//        //绘制x轴箭头
//        xyPaint.setStyle(Paint.Style.STROKE);
//        path = new Path();
//        //整个X轴的长度
//        float xLength = xInit + interval * (xValue.size() - 1) + (width - xOri) * 0.1f;
//        if (xLength < width)
//            xLength = width;
//        path.moveTo(xLength - dpToPx(12), yOri + xylinewidth / 2 - dpToPx(5));
//        path.lineTo(xLength - xylinewidth / 2, yOri + xylinewidth / 2);
//        path.lineTo(xLength - dpToPx(12), yOri + xylinewidth / 2 + dpToPx(5));
//        canvas.drawPath(path, xyPaint);
        //绘制x轴刻度
//        for (int i = 0; i < xValue.size(); i++) {
//            float x = xInit + interval * i;
//            if (x >= xOri) {//只绘制从原点开始的区域
//                xyTextPaint.setColor(xytextcolor);
//                canvas.drawLine(x, yOri, x, yOri - length, xyPaint);
//                //绘制X轴文本
//                String text = xValue.get(i);
//                Rect rect = getTextBounds(text, xyTextPaint);
//                if (i == selectIndex - 1) {
//                    xyTextPaint.setColor(linecolor);
//                    canvas.drawText(text, 0, text.length(), x - rect.width() / 2, yOri + xylinewidth + dpToPx(2) + rect.height(), xyTextPaint);
//                    canvas.drawRoundRect(x - xValueRect.width() / 2 - dpToPx(3), yOri + xylinewidth + dpToPx(1), x + xValueRect.width() / 2 + dpToPx(3), yOri + xylinewidth + dpToPx(2) + xValueRect.height() + dpToPx(2), dpToPx(2), dpToPx(2), xyTextPaint);
//                } else {
//                canvas.drawText(text, 0, text.length(), x - rect.width() / 2, yOri + xylinewidth + dpToPx(2) + rect.height(), xyTextPaint);
//                }
//            }
//        }
    }

    private float startX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (isScrolling)
//            return super.onTouchEvent(event);
//        this.getParent().requestDisallowInterceptTouchEvent(true);//当该view获得点击事件，就请求父控件不拦截事件
//        obtainVelocityTracker(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (canScroll) {//当期的宽度不足以呈现全部数据
                    float dis = event.getX() - startX;
                    startX = event.getX();
                    if (xInit + dis < minXInit) {
                        xInit = minXInit;
                    } else if (xInit + dis > maxXInit) {
                        xInit = maxXInit;
                    } else {
                        xInit = xInit + dis;
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
//                clickAction(event);
//                scrollAfterActionUp();
//                this.getParent().requestDisallowInterceptTouchEvent(false);
//                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
//                this.getParent().requestDisallowInterceptTouchEvent(false);
//                recycleVelocityTracker();
                break;
        }
        return true;
    }

    //是否正在滑动
//    private boolean isScrolling = false;

    /**
     * 手指抬起后的滑动处理
     */
//    private void scrollAfterActionUp() {
//        if (!isScroll)
//            return;
//        final float velocity = getVelocity();
//        float scrollLength = maxXInit - minXInit;
//        if (Math.abs(velocity) < 10000)//10000是一个速度临界值，如果速度达到10000，最大可以滑动(maxXInit - minXInit)
//            scrollLength = (maxXInit - minXInit) * Math.abs(velocity) / 10000;
//        ValueAnimator animator = ValueAnimator.ofFloat(0, scrollLength);
//        animator.setDuration((long) (scrollLength / (maxXInit - minXInit) * 1000));//时间最大为1000毫秒，此处使用比例进行换算
//        animator.setInterpolator(new DecelerateInterpolator());
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                float value = (float) valueAnimator.getAnimatedValue();
//                if (velocity < 0 && xInit > minXInit) {//向左滑动
//                    if (xInit - value <= minXInit)
//                        xInit = minXInit;
//                    else
//                        xInit = xInit - value;
//                } else if (velocity > 0 && xInit < maxXInit) {//向右滑动
//                    if (xInit + value >= maxXInit)
//                        xInit = maxXInit;
//                    else
//                        xInit = xInit + value;
//                }
//                invalidate();
//            }
//        });
//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//                isScrolling = true;
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                isScrolling = false;
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//                isScrolling = false;
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//        animator.start();
//
//    }

    /**
     * 获取速度
     *
     * @return
     */
//    private float getVelocity() {
//        if (velocityTracker != null) {
//            velocityTracker.computeCurrentVelocity(1000);
//            return velocityTracker.getXVelocity();
//        }
//        return 0;
//    }

    /**
     * 点击X轴坐标或者折线节点
     *
     * @param event
     */
//    private void clickAction(MotionEvent event) {
//        int dp8 = dpToPx(8);
//        float eventX = event.getX();
//        float eventY = event.getY();
//        for (int i = 0; i < xValue.size(); i++) {
//            //节点
//            float x = xInit + interval * i;
//            float y = yOri - yOri  * value.get(xValue.get(i)) / yValue.get(yValue.size() - 1);
//            if (eventX >= x - dp8 && eventX <= x + dp8 &&
//                    eventY >= y - dp8 && eventY <= y + dp8 && selectIndex != i + 1) {//每个节点周围8dp都是可点击区域
//                selectIndex = i + 1;
//                invalidate();
//                return;
//            }
//            //X轴刻度
//            String text = xValue.get(i);
//            Rect rect = getTextBounds(text, xyTextPaint);
//            x = xInit + interval * i;
//            y = yOri + xylinewidth + dpToPx(2);
//            if (eventX >= x - rect.width() / 2 - dp8 && eventX <= x + rect.width() + dp8 / 2 &&
//                    eventY >= y - dp8 && eventY <= y + rect.height() + dp8 && selectIndex != i + 1) {
//                selectIndex = i + 1;
//                invalidate();
//                return;
//            }
//        }
//    }


    /**
     * 获取速度跟踪器
     *
     * @param event
     */
//    private void obtainVelocityTracker(MotionEvent event) {
//        if (!isScroll)
//            return;
//        if (velocityTracker == null) {
//            velocityTracker = VelocityTracker.obtain();
//        }
//        velocityTracker.addMovement(event);
//    }

    /**
     * 回收速度跟踪器
     */
//    private void recycleVelocityTracker() {
//        if (velocityTracker != null) {
//            velocityTracker.recycle();
//            velocityTracker = null;
//        }
//    }

//    public int getSelectIndex() {
//        return selectIndex;
//    }
//
//    public void setSelectIndex(int selectIndex) {
//        this.selectIndex = selectIndex;
//        invalidate();
//    }
    public void setxValue(List<Float> xValue) {
        this.xValue = xValue;
        requestLayout();
        invalidate();
    }
//
//    public void setyValue(List<Integer> yValue) {
//        this.yValue = yValue;
//        invalidate();
//    }
//
//    public void setValue(Map<String, Integer> value) {
//        this.value = value;
//        invalidate();
//    }
//
//    public void setValue(Map<String, Integer> value, List<String> xValue, List<Integer> yValue) {
//        this.value = value;
//        this.xValue = xValue;
//        this.yValue = yValue;
//        invalidate();
//    }
//
//    public List<String> getxValue() {
//        return xValue;
//    }
//
//    public List<Integer> getyValue() {
//        return yValue;
//    }
//
//    public Map<String, Integer> getValue() {
//        return value;
//    }

    /**
     * 获取丈量文本的矩形
     *
     * @param text
     * @param paint
     * @return
     */
    private Rect getTextBounds(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    /**
     * dp转化成为px
     *
     * @param dp
     * @return
     */
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f * (dp >= 0 ? 1 : -1));
    }

    /**
     * sp转化为px
     *
     * @param sp
     * @return
     */
    private int spToPx(int sp) {
        float scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (scaledDensity * sp + 0.5f * (sp >= 0 ? 1 : -1));
    }
}