package z.hobin.weibofs.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import static android.view.View.MeasureSpec.EXACTLY;

/**
 * 等分格子布局
 */
public class GridLinearLayout extends LinearLayout {
    private int columnCount = 5;
    private int rowCount = 1;
    private ItemLongClickListener childLongClickListener;

    public GridLinearLayout(Context context) {
        super(context);
    }

    public GridLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public GridLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int childWidth = 0, childHeight = 0;

        int modeW = 0, modeH = 0;
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED)
            modeW = MeasureSpec.UNSPECIFIED;
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED)
            modeH = MeasureSpec.UNSPECIFIED;

        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), modeW);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec), modeH);

        int count = getChildCount();
        if (count == 0) {
            super.onMeasure(childWidthMeasureSpec, childHeightMeasureSpec);
            return;
        }
        childWidth = getMeasuredWidth() / getColumnCount();
        childHeight = childWidth;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            child.measure(childWidth, childHeight);
            //childWidth = child.getMeasuredWidth();
            //childHeight = child.getMeasuredHeight();
        }
        int width = childWidth * columnCount;
        int height = childHeight * getRowCount();

        //setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
        setMeasuredDimension(width, height);
    }

    private int margin = 0;
    private int marginTop = margin * 5;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int height = b - t;// 布局区域高度
        int width = r - l;// 布局区域宽度
        int rows = getRowCount();// 行数
        if (getChildCount() == 0)
            return;

        int gridW = (width - margin * (columnCount - 1)) / columnCount;// 格子宽度
        int gridH = (height - marginTop * rows) / rows;// 格子高度

        int left = 0;
        int top = 0;

        for (int i = 0; i < rows; i++) {// 遍历行
            for (int j = 0; j < columnCount; j++) {// 遍历每一行的元素
                View child = this.getChildAt(i * columnCount + j);
                if (child == null)
                    return;
                left = j * gridW + j * margin;
                // 如果当前布局宽度和测量宽度不一样，就直接用当前布局的宽度重新测量
                if (gridW != child.getMeasuredWidth()
                        || gridH != child.getMeasuredHeight()) {
                    child.measure(MeasureSpec.makeMeasureSpec(gridW, EXACTLY), MeasureSpec.makeMeasureSpec(gridH, EXACTLY));
                }
                child.layout(left, top, left + gridW, top + gridH);
                System.out.println("--top--" + top + ",bottom=" + (top + gridH));

            }
            top += gridH + marginTop;
        }
    }

    private Paint linePaint = new Paint();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(4);

        for (int i = 0; i <= getRowCount(); i++) {
            int y = 0;
            if (i == 0) {
                y += linePaint.getStrokeWidth() / 2;
            } else if (i == getRowCount()) {
                y = (int) (getHeight() - linePaint.getStrokeWidth() / 2);
            } else {
                y = getHeight() / getRowCount() * i;
            }
            //canvas.drawLine(0, y, getWidth(), y, linePaint);
        }

        for (int j = 0; j <= getColumnCount(); j++) {
            int x = 0;
            if (j == 0) {
                x += linePaint.getStrokeWidth() / 2;
            } else if (j == getColumnCount()) {
                x = (int) (getWidth() - (linePaint.getStrokeWidth() / 2));
            } else {
                x = getWidth() / getColumnCount() * j;
            }
            //canvas.drawLine(x, 0, x, getHeight(), linePaint);
        }
    }


    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getRowCount() {
        return getChildCount() % columnCount == 0 ? getChildCount() / columnCount : getChildCount() / columnCount + 1;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setOnItemLongClickListener(ItemLongClickListener longClickListener) {
        childLongClickListener = longClickListener;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        child.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (childLongClickListener != null) {
                    childLongClickListener.onItemLongClick(indexOfChild(v), v);
                }
                return true;
            }
        });
    }

    public interface ItemLongClickListener {
        void onItemLongClick(int position, View child);
    }
}