package com.urrecliner.phototag;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.widget.ImageView;

import static com.urrecliner.phototag.Vars.colorPlate;
import static com.urrecliner.phototag.Vars.colorRange;

class ColorDraw {

    private final ImageView iVBoxCurr;
    private final ImageView iVBoxWork;

    ColorDraw(Activity activity) {
        iVBoxCurr = activity.findViewById(R.id.colorCurr);
        iVBoxWork = activity.findViewById(R.id.colorWork);
    }

    void drawColorPlate(int colorRGB) {
        Shader mSatShader, mValShader;
        RectF mSatValRect;
        Paint mSatValPaint;

        int width = colorPlate.getWidth();
        int height = colorPlate.getHeight();
        mSatValRect = new RectF(0, 0, width, height);
        final RectF rect = mSatValRect;
        mSatValPaint = new Paint();
        mValShader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom,
                    0xffffffff, 0xff000000, Shader.TileMode.CLAMP);
        mSatShader =new LinearGradient(rect.left, rect.top, rect.right, rect.top,
                    0xffffffff, colorRGB, Shader.TileMode.CLAMP);
        ComposeShader mShader = new ComposeShader(mValShader, mSatShader, PorterDuff.Mode.MULTIPLY);
        mSatValPaint.setShader(mShader);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(rect,mSatValPaint);
        colorPlate.setImageBitmap(bitmap);

    }

    void drawColorRange() {
        Paint mSatValPaint;
        int[] colors = { Color.RED, Color.RED, Color.GREEN, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.YELLOW, Color.YELLOW, Color.CYAN, Color.WHITE, Color.WHITE, Color.BLACK};

        int width = colorRange.getWidth();
        int height = colorRange.getHeight();
        final RectF rect = new RectF(0, 0, width, height);
        mSatValPaint = new Paint();

        mSatValPaint.setShader(new LinearGradient(0,0,0, height, colors, null, Shader.TileMode.CLAMP));
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(rect,mSatValPaint);
        colorRange.setImageBitmap(bitmap);
    }

    void drawNowColor(int oldColor, int newColor) {
        int width = iVBoxCurr.getWidth();
        int height = iVBoxCurr.getHeight();
        iVBoxCurr.setImageBitmap(makeBitmapBox(oldColor, width, height));
        iVBoxWork.setImageBitmap(makeBitmapBox(newColor, width, height));
    }

    private Bitmap makeBitmapBox(int innerColor, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(innerColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawRect(2, 2, width-4, height-4, paint);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        canvas.drawRect(1, 1, width, height, paint);
        return bitmap;
    }
}