package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.colorAlpha;
import static com.urrecliner.phototag.Vars.colorDraw;
import static com.urrecliner.phototag.Vars.colorPlate;
import static com.urrecliner.phototag.Vars.colorRGB;
import static com.urrecliner.phototag.Vars.colorRange;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.markTextInColor;
import static com.urrecliner.phototag.Vars.markTextOutColor;
import static com.urrecliner.phototag.Vars.sharedPref;
import static com.urrecliner.phototag.Vars.utils;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class ActivityColor extends AppCompatActivity {

    boolean modeIn = true;
    int currInColor, currOutColor, workColor;
    TextView tvText, tvOutline;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
        colorDraw = new ColorDraw(this);
        modeIn = true;
        currInColor = markTextInColor;
        currOutColor = markTextOutColor;
        workColor = currInColor;
        colorRGB = currInColor;

        colorPlate = findViewById(R.id.colorPlate);
        colorRange = findViewById(R.id.colorRange);

        tvText = findViewById(R.id.colorText);
        tvOutline = findViewById(R.id.colorOutline);

        tvText.setOnClickListener(view -> {
            modeIn = true;
            workColor = currInColor;
            colorRGB = currInColor;
            showColorPlain();
        });

        tvOutline.setOnClickListener(view -> {
            modeIn = false;
            workColor = currOutColor;
            colorRGB = currOutColor;
            showColorPlain();
        });

        TextView tvCancel = findViewById(R.id.cancel);
        tvCancel.setOnClickListener(view -> {
            if (modeIn)
                currInColor = markTextInColor;
            else
                currOutColor = markTextOutColor;
            workColor = (modeIn) ? currInColor : currOutColor;
            colorRGB = (modeIn) ? currInColor : currOutColor;
            showColorPlain();
        });

        TextView tvSelect = findViewById(R.id.select);
        tvSelect.setOnClickListener(view -> {
            if (modeIn) {
                currInColor = workColor;
            }
            else {
                currOutColor = workColor;
            }
            modeIn = !modeIn;
            workColor = (modeIn) ? currInColor:currOutColor;
            colorRGB = workColor;
            showColorPlain();
        });

        colorAlpha = findViewById(R.id.barAlpha);
        colorAlpha.setScaleY(2f);
        int alpha = Color.alpha(workColor);
        colorAlpha.setProgress(alpha);
        colorAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                int barValue = seekBar.getProgress();
//                utils.log("alpha", "barValue = "+barValue+String.format("#%08X", barValue));
                workColor = Color.argb(barValue, Color.red(workColor), Color.green(workColor), Color.blue(workColor));
                colorRGB = workColor;
//                utils.log("alpha", "changed now = "+ workColor +String.format("#%08X", workColor));
                showColorPlain();
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
        });

        final EditText etColor = findViewById(R.id.colorHex);
        etColor.setText(String.format("#%08X", workColor).substring(1,9));
        etColor.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable arg0) {
                String txt = etColor.getText().toString();
                if (txt.length() == 8) {
                    int newColor = (int) Long.parseLong(txt, 16);
                    if (newColor != workColor) {
                        workColor = newColor;
                        showColorPlain();
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        });

        colorPlate.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                workColor = getTouchColor((ImageView) v, event);
                showColorPlain();
            }
            return true;
        });
        colorPlate.post(this::showColorPlain);
        colorRange.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                colorRGB = getTouchColor((ImageView) v, event);
                utils.log("RGB","color "+colorRGB);
                showColorPlain();
            }
            return true;
        });
        colorRange.post(() -> colorDraw.drawColorRange());

    }

    private void showColorPlain() {
        tvText.setTextSize((modeIn) ? 24:16);
        tvOutline.setTextSize((modeIn) ? 16:24);
        tvText.setBackgroundColor((modeIn)? ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null):Color.WHITE);
        tvOutline.setBackgroundColor((!modeIn)? ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null):Color.WHITE);
        colorDraw.drawColorPlate(colorRGB);
        colorDraw.drawColorRange();
        colorDraw.drawNowColor((modeIn)? currInColor:currOutColor, workColor);
        int alpha = Color.alpha(workColor);
        colorAlpha.setProgress(alpha);
        EditText etColor = findViewById(R.id.colorHex);
        etColor.setText(String.format("#%08X", workColor).substring(1,9));
        showSample();

//        colorSpot = findViewById(R.id.colorSpot);
//        ConstraintLayout constraintLayout = new ConstraintLayout(this);
//        ConstraintSet set = new ConstraintSet();
//        set.clone(constraintLayout);
//        set.setTranslation(R.id.colorSpot, 120, 130);
//        set.applyTo(constraintLayout);
    }

    private int getTouchColor(ImageView v, MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        float[] eventXY = new float[] {eventX, eventY};

        Matrix invertMatrix = new Matrix();
        v.getImageMatrix().invert(invertMatrix);
        invertMatrix.mapPoints(eventXY);
        int x = (int)eventXY[0];
        int y = (int)eventXY[1];
        Drawable drawable = v.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        return bitmap.getPixel(x,y);
    }

    private void showSample() {
        ImageView iv = findViewById(R.id.colorSample);
        int width = iv.getWidth();
        int height = iv.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        String text = "원철 Riopapa";
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(54);
        paint.setStrokeWidth(3);
        paint.setColor((modeIn) ? currOutColor:workColor);
        paint.setTypeface(mContext.getResources().getFont(R.font.ttangs_budae));
        int xPos = width / 2;
        int yPos = height - 24;
        int d = 4;
        canvas.drawText(text, xPos - d, yPos - d, paint);
        canvas.drawText(text, xPos + d, yPos - d, paint);
        canvas.drawText(text, xPos - d, yPos + d, paint);
        canvas.drawText(text, xPos + d, yPos + d, paint);
        canvas.drawText(text, xPos - d, yPos, paint);
        canvas.drawText(text, xPos + d, yPos, paint);
        canvas.drawText(text, xPos, yPos - d, paint);
        canvas.drawText(text, xPos, yPos + d, paint);
        paint.setColor((modeIn)? workColor:currInColor);
        canvas.drawText(text, xPos, yPos, paint);
        iv.setImageBitmap(bitmap);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        markTextInColor = currInColor;
        markTextOutColor = currOutColor;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("markTextInColor", markTextInColor);
        editor.putInt("markTextOutColor", markTextOutColor);
        editor.apply();
        finish();
//        Intent intent = new Intent(mContext, ActivityMain.class);
//        startActivity(intent);
    }

}