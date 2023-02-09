package com.urrecliner.phototag;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.exifinterface.media.ExifInterface;
import androidx.core.content.ContextCompat;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.sharedAlpha;
import static com.urrecliner.phototag.Vars.sharedPref;
import static com.urrecliner.phototag.Vars.sharedSigNbr;
import static com.urrecliner.phototag.Vars.sigColors;

class DrawPlaceInfo {

    String sFood, sPlace, sAddress;
    Context context;
    int outWidth, outHeight;
    Bitmap thumbnail;
    String orient = "1";

    PhotoTag updateThumbnail(PhotoTag nowPT) {
        ExifInterface exif = null;
        String fullFileName = nowPT.fullFolder+nowPT.photoName;
        orient = "1";
        try {
            exif = new ExifInterface(fullFileName);
            orient = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        } catch (IOException e) {
            orient = "1";
        }
        if (exif != null) {
            thumbnail = loadThumbnail(exif);
            if (thumbnail == null)
                thumbnail = makeThumbnail(fullFileName);
        }

        int degree = 0;
        switch (orient) {
            case "8":
                degree = -90;;
                break;
            case "6":
                degree = 90;
                break;
            case "3":
                degree = -180;
                break;
        }
        if (degree != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, outWidth, outHeight, matrix, false);
        }

        nowPT.orient = orient;
        nowPT.setThumbnail(thumbnail);
        return nowPT;
    }

    Bitmap loadThumbnail(ExifInterface exif) {

        byte[] imageData=exif.getThumbnail();
        if (imageData == null || imageData.length < 1)
            return null;
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        outWidth = bitmap.getWidth();
        outHeight = bitmap.getHeight();
        return bitmap;
    }

    Bitmap makeThumbnail(String fileName) {

        Bitmap fullBitmap = BitmapFactory.decodeFile(new File(fileName).getAbsolutePath())
                .copy(Bitmap.Config.RGB_565, false);

        int width = fullBitmap.getWidth();
        int height = fullBitmap.getHeight();
        int sWidth = width * 14/16;
        int sHeight = height * 14/16;

        if (width < height) { // if portrait crop height a little more
            sHeight = height * 13/16;
        }
        fullBitmap = Bitmap.createBitmap(fullBitmap, (width - sWidth)/2, (height - sHeight) /2,
                sWidth, sHeight);
        outWidth = (sWidth> sHeight) ? 500: 288; // to fit with normal thumbnail sizeX * 6 / 18;   // smaller scale
        outHeight = outWidth * sHeight / sWidth;
        Log.w("makeThumbnail", fileName+" "+outWidth+" x "+outHeight);
        return Bitmap.createScaledBitmap(fullBitmap, outWidth, outHeight, false);       // crop center

    }

    Bitmap makeChecked(Bitmap photoMap) {

        int delta = 8;
        int delta2 = delta + delta;
        int width = photoMap.getWidth();
        int height = photoMap.getHeight();

        Bitmap outMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outMap);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        final float roundPx = 10;
        paint.setAntiAlias(true);
        paint.setColor(mContext.getColor(R.color.xorColor));
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        Bitmap bitmap = Bitmap.createBitmap(photoMap, delta2, delta2, width-delta2-delta2, height-delta2-delta2);
        canvas.drawBitmap(bitmap, delta2, delta2, paint);
        return outMap;
    }

    Bitmap addDateLocSignature(Context context, Bitmap photoMap, long timeStamp, String food, String place, String address) {

        this.context = context;
        int width = photoMap.getWidth();
        int height = photoMap.getHeight();
        Bitmap newMap = Bitmap.createBitmap(width, height, photoMap.getConfig());
        Canvas canvas = new Canvas(newMap);
        canvas.drawBitmap(photoMap, 0f, 0f, null);
        markDateTime(timeStamp, width, height, canvas);
        markSignature(width, height, canvas);
        if (place.equals(" "))
            return newMap;
        this.sFood = food; this.sPlace = place; this.sAddress = address;
        markFoodPlaceAddress(width, height, canvas);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("signature", sharedSigNbr);
        editor.apply();
        return newMap;
    }

    private void markDateTime(long timeStamp, int width, int height, Canvas canvas) {
        final SimpleDateFormat sdfDate = new SimpleDateFormat("`yy/MM/dd(EEE)", Locale.KOREA);
        final SimpleDateFormat sdfHourMin = new SimpleDateFormat("HH:mm", Locale.KOREA);
        int fontSize = (width>height) ? (width+height)/60 : (width+height)/80;  // date time
        String dateTime = sdfDate.format(timeStamp);
        int xPos = (width>height) ? width/8+fontSize: width/7+fontSize;
        int yPos = (width>height) ? height/11: height/13;
        drawTextOnCanvas(canvas, dateTime, fontSize, xPos, yPos);
        yPos += fontSize*3/2;
        dateTime = sdfHourMin.format(timeStamp);
        fontSize = fontSize * 8 / 9;
        drawTextOnCanvas(canvas, dateTime, fontSize, xPos, yPos);
    }

    private void markSignature(int width, int height, Canvas canvas) {
        int sigSize = (width + height) / 18;
        Bitmap signatureMap = buildSignatureMap();
        Bitmap sigMap = Bitmap.createScaledBitmap(signatureMap, sigSize, sigSize, false);
        int xPos = width - sigSize - width / 40;
        int yPos = (width>height) ? height/16: height/20;
        Paint paint = new Paint(); paint.setAlpha(Integer.parseInt(sharedAlpha));
        canvas.drawBitmap(sigMap, xPos, yPos, paint);
    }

    private void markFoodPlaceAddress(int width, int height, Canvas canvas) {

        int fontSize = (width>height) ? (height + width) / 60: (height + width) / 75;
        int xPos = width / 2;
        int yPos = (width>height) ? height - fontSize*3/2: height - fontSize*3/2+4;
        yPos = drawTextOnCanvas(canvas, sAddress, fontSize, xPos, yPos);
        fontSize = fontSize * 12 / 10;  // Place
        yPos -= fontSize + fontSize / 2;
        yPos = drawTextOnCanvas(canvas, sPlace, fontSize, xPos, yPos);
        yPos -= fontSize + fontSize / 2; // food
        drawTextOnCanvas(canvas, sFood, fontSize, xPos, yPos);
    }

    int drawTextOnCanvas(Canvas canvas, String text, int fontSize, int xPos, int yPos) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(fontSize);
//        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        int cWidth = canvas.getWidth() * 2 / 3;
        float tWidth = paint.measureText(text);
        int pos;
        if (tWidth > cWidth) {
            int length = text.length() / 2;
            for (pos = length; pos < text.length(); pos++)
                if (text.charAt(pos) == ' ')
                    break;
            String text1 = text.substring(pos);
            drawOutLinedText(canvas, text1, xPos, yPos, fontSize);
            yPos -= fontSize + fontSize / 4;
            text1 = text.substring(0, pos);
            drawOutLinedText(canvas, text1, xPos, yPos, fontSize);
            return yPos;
        }
        else
            drawOutLinedText(canvas, text, xPos, yPos, fontSize);
        return yPos;
    }

    void drawOutLinedText(Canvas canvas, String text, int xPos, int yPos, int textSize) {

        int inColor = ContextCompat.getColor(mContext, R.color.infoColor);
        int outColor = ContextCompat.getColor(mContext, R.color.infoOutColor);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setColor(outColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(textSize/5f+3);
        paint.setTypeface(mContext.getResources().getFont(R.font.ttangs_budae));
        canvas.drawText(text, xPos+3, yPos+3, paint);

        paint.setColor(inColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, xPos, yPos, paint);
    }

    Bitmap buildSignatureMap() {
        Bitmap sigMap;
        sigMap = BitmapFactory.decodeResource(mContext.getResources(), sigColors[sharedSigNbr]);
        Bitmap newBitmap = Bitmap.createBitmap(sigMap.getWidth(), sigMap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(sigMap, 0, 0, null);
        return newBitmap;
    }
}