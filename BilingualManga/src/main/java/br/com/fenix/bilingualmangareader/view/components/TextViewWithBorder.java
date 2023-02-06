package br.com.fenix.bilingualmangareader.view.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class TextViewWithBorder extends androidx.appcompat.widget.AppCompatTextView {
    public TextViewWithBorder(Context context) {
        super(context);
    }

    public TextViewWithBorder(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewWithBorder(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int textColor = getTextColors().getDefaultColor();
        setTextColor(Color.BLACK);
        getPaint().setStrokeWidth(2);
        getPaint().setStyle(Paint.Style.STROKE);
        super.onDraw(canvas);
        setTextColor(textColor);
        getPaint().setStrokeWidth(0);
        getPaint().setStyle(Paint.Style.FILL);
        super.onDraw(canvas);
    }
}