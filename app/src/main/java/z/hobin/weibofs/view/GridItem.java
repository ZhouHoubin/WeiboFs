package z.hobin.weibofs.view;

import android.content.Context;
import android.view.ViewGroup;

public class GridItem extends android.support.v7.widget.AppCompatImageView {
    private int left, top, right, bottom;

    public GridItem(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup group = (ViewGroup) getParent();

        int padding = 10;

        left = 0;
        top = 0;
        right = padding;
        bottom = 0;

        if (group != null) {
            int index = group.indexOfChild(this);
            if ((index + 1) % 3 == 1) {
                bottom = padding;
                right = padding;
            } else if ((index + 1) % 3 == 2) {
                bottom = padding;
                right = padding;
            } else if ((index + 1) % 3 == 0) {
                bottom = padding;
                right = 0;
            }
        }
        setPadding(left, top, right, bottom);
    }
}
