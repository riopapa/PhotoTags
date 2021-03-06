package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.byPlaceName;
import static com.urrecliner.phototag.Vars.copyPasteText;
import static com.urrecliner.phototag.Vars.fullFolder;
import static com.urrecliner.phototag.Vars.mActivity;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.makeFolderSumNail;
import static com.urrecliner.phototag.Vars.nowDownLoading;
import static com.urrecliner.phototag.Vars.nowPlace;
import static com.urrecliner.phototag.Vars.nowPos;
import static com.urrecliner.phototag.Vars.photoAdapter;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.photoTags;
import static com.urrecliner.phototag.Vars.photoView;
import static com.urrecliner.phototag.Vars.placeActivity;
import static com.urrecliner.phototag.Vars.placeInfos;
import static com.urrecliner.phototag.Vars.placeType;
import static com.urrecliner.phototag.Vars.sharedRadius;
import static com.urrecliner.phototag.Vars.sharedSigNbr;
import static com.urrecliner.phototag.Vars.sigColors;
import static com.urrecliner.phototag.Vars.tvPlaceAddress;
import static com.urrecliner.phototag.Vars.typeAdapter;
import static com.urrecliner.phototag.Vars.typeIcons;
import static com.urrecliner.phototag.Vars.typeInfos;
import static com.urrecliner.phototag.Vars.typeNames;
import static com.urrecliner.phototag.Vars.typeNumber;
import static com.urrecliner.phototag.Vars.utils;
import static com.urrecliner.phototag.placeNearby.PlaceParser.pageToken;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.location.Geocoder;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.urrecliner.phototag.Utility.SharePhoto;
import com.urrecliner.phototag.placeNearby.PlaceRetrieve;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.senab.photoview.PhotoViewAttacher;

public class TagPlaceActivity extends AppCompatActivity {

    ExifInterface exif = null;
    String strAddress = null, strPlace = null;
    String dateTimeColon, dateTimeFileName = null;
    String maker, model;
    double latitude, longitude, altitude;
    PhotoTag orgPT, newPT;
    File fileFullName;
    String orient;
    Bitmap bitmapImage;
    ImageView photoImage, sigImage;

    static final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
    static final SimpleDateFormat sdfFile = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        placeActivity = this;
        tvPlaceAddress = findViewById(R.id.placeAddress);
        typeInfos = new ArrayList<>();
        for (int i = 0; i < typeNames.length; i++) {
            typeInfos.add(new TypeInfo(typeNames[i], typeIcons[i]));
        }

        RecyclerView typeRecyclerView = findViewById(R.id.type_recycler);
        LinearLayoutManager mLinearLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        typeRecyclerView.setLayoutManager(mLinearLayoutManager);
        typeAdapter = new TypeAdapter(typeInfos);
        typeRecyclerView.setAdapter(typeAdapter);

//        orgPT.isChecked = false;
//        photoTags.set(nowPos, orgPT);
//        photoAdapter.notifyItemChanged(nowPos, orgPT);
        utils.showFolder(this.getSupportActionBar());

        buildPhotoScreen();
    }

    void buildPhotoScreen() {

        orgPT = photoTags.get(nowPos);
        fileFullName = new File (orgPT.fullFolder, orgPT.photoName);
        if (!fileFullName.exists())
            return;
        photoImage = findViewById(R.id.image);
        photoImage.post(this::adjust_SignaturePos);
        bitmapImage = BitmapFactory.decodeFile(fileFullName.getAbsolutePath());
        getPhotoExif(fileFullName);
        orgPT.orient = orient;
        if (!orient.equals("1")) {
            if (orient.equals("6")) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, false);
                orient = "1";
            }
        }

        photoImage.setImageBitmap(bitmapImage);
        sigImage = findViewById(R.id.signature);
        sigImage.setImageResource(sigColors[sharedSigNbr]);
        sigImage.setOnClickListener(view -> {
            sharedSigNbr++;
            sharedSigNbr = sharedSigNbr % sigColors.length;
            sigImage.setImageResource(sigColors[sharedSigNbr]);
        });
        PhotoViewAttacher pA;       // to enable zoom
        pA = new PhotoViewAttacher(photoImage);
        pA.update();
        getLocationInfo();
        TextView tv = findViewById(R.id.photoName);
        tv.setText(fileFullName.getName());
        ImageView ivGetPlaces = findViewById(R.id.queryLocs);
        ivGetPlaces.setOnClickListener(view -> {
            if (latitude == 0 && longitude == 0) {
                Toast.makeText(mContext,"No GPS Information to retrieve places",Toast.LENGTH_LONG).show();
            } else {
                getPlaceByLatLng();
            }
        });
        ivGetPlaces.setImageResource(typeIcons[typeNumber]);

        ImageView iVMark = findViewById(R.id.add_mark);
        iVMark.setOnClickListener(view -> {
            if (latitude == 0 && longitude == 0)
                Toast.makeText(mContext,"No GPS Information to retrieve places",Toast.LENGTH_LONG).show();
            EditText etPlace = findViewById(R.id.placeAddress);
            nowPlace = etPlace.getText().toString();
            if (nowPlace.length() > 2) {
                newPT = NewPhoto.save(orgPT);
                photoTags.add(nowPos, newPT);
                photoAdapter.notifyItemInserted(nowPos);
                finish();
                makeFolderSumNail.makeReady();
            }
        });
        iVMark.setAlpha(fileFullName.getName().endsWith("_ha.jpg") ? 0.2f: 1f);

        ImageView iVPaste = findViewById(R.id.pasteInfo);
        iVPaste.setOnClickListener(view -> {
            EditText etPlace = findViewById(R.id.placeAddress);
            etPlace.setText(copyPasteText);
        });
        iVPaste.setAlpha((copyPasteText.equals("")) ? 0.2f: 1f);

        ImageView iVCopy = findViewById(R.id.copyInfo);
        iVCopy.setOnClickListener(view -> {
            EditText etPlace = findViewById(R.id.placeAddress);
            copyPasteText = etPlace.getText().toString();
            Toast.makeText(mContext, "Text Copied\n" + copyPasteText, Toast.LENGTH_SHORT).show();
            iVPaste.setAlpha(1f);
            iVPaste.setEnabled(true);
        });

        ImageView iVInfo = findViewById(R.id.showInfo);
        iVInfo.setOnClickListener(view -> Toast.makeText(mContext, buildLongInfo(), Toast.LENGTH_LONG).show());

        ImageView iVRotateSave = findViewById(R.id.rotate_save);
        iVRotateSave.setOnClickListener(v -> save_rotatedPhoto());
        iVRotateSave.setAlpha(0.2f);
        ImageView ivRotate = findViewById(R.id.rotate);
        ivRotate.setOnClickListener(view -> {
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, false);
            photoImage.setImageBitmap(bitmapImage);
            PhotoViewAttacher pA1;       // to enable zoom
            pA1 = new PhotoViewAttacher(photoImage);
            pA1.update();
            ivRotate.setAlpha(1f);
            adjust_SignaturePos();
        });

        final ImageView ivLeft = findViewById(R.id.imageL);
        if (nowPos > 0) {
            ivLeft.post(() -> {
                int width = ivLeft.getMeasuredWidth();
                int height = ivLeft.getMeasuredHeight();
                PhotoTag pTag = photoTags.get(nowPos-1);
                Bitmap sumNail = pTag.getSumNailMap();
                if (sumNail == null) {
                    pTag = BuildDB.getPhotoWithMap(pTag);
                    sumNail = pTag.getSumNailMap();
                }
                Bitmap bitmap = maskImage(sumNail, false);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                ivLeft.setImageBitmap(bitmap);
            });
            ivLeft.setOnClickListener(view -> {
                nowPos--;
                buildPhotoScreen();
            });
            ivLeft.setVisibility(View.VISIBLE);
        } else
            ivLeft.setVisibility(View.INVISIBLE);

        final ImageView ivRight = findViewById(R.id.imageR);
        if (nowPos < photoTags.size()-1) {
            ivRight.post(() -> {
                int width = ivRight.getMeasuredWidth();
                int height = ivRight.getMeasuredHeight();
                PhotoTag pTag = photoTags.get(nowPos+1);
                Bitmap sumNail = pTag.getSumNailMap();
                if (sumNail == null) {
                    pTag = BuildDB.getPhotoWithMap(pTag);
                    sumNail = pTag.getSumNailMap();
                }
                Bitmap bitmap = maskImage(sumNail, true);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                ivRight.setImageBitmap(bitmap);
            });
            ivRight.setOnClickListener(view -> {
                nowPos++;
                buildPhotoScreen();
            });
            ivRight.setVisibility(View.VISIBLE);
        }
        else
            ivRight.setVisibility(View.INVISIBLE);
//        if (sharedAutoLoad && !orgPT.photoName.endsWith("_ha.jpg")) {
//            getPlaceByLatLng();
//        }
    }

    private void adjust_SignaturePos() {
        boolean landscape = bitmapImage.getWidth() > bitmapImage.getHeight();
        int rightMargin = (landscape) ? 48: 200;
        int topMargin = ((landscape) ? 380: 48);
        ConstraintLayout.LayoutParams lP = (ConstraintLayout.LayoutParams) sigImage.getLayoutParams();
        lP.topMargin = topMargin;
        lP.rightMargin = rightMargin;
        sigImage.setLayoutParams(lP);

    }
    private Bitmap maskImage(Bitmap mainImage, boolean isRight) {
        Bitmap mask = BitmapFactory.decodeResource(getResources(),(isRight) ? R.mipmap.move_right: R.mipmap.move_left).copy(Bitmap.Config.ARGB_8888, true);
        Bitmap result = Bitmap.createScaledBitmap(mainImage, mask.getWidth()-16, mask.getHeight()-16, false);
        Canvas c = new Canvas(mask);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN) );
        c.drawBitmap(result, 8, 8, paint);
        return mask;
    }

    private void save_rotatedPhoto() {
        PhotoTag pt = photoTags.get(nowPos);
        String orgName = orgPT.photoName;
        String tgtName = orgName.substring(0,orgName.length()-4)+"R.jpg";
        pt.photoName = tgtName;
        photoDao.delete(pt);
        utils.createPhotoFile(fullFolder, orgName, tgtName, bitmapImage, "1");
        pt.orient = "1";
        pt.isChecked = false;
        pt.photoName = tgtName;
        pt.sumNailMap = null;
        photoTags.add(nowPos, orgPT);
        photoAdapter.notifyItemInserted(nowPos++);
        photoAdapter.notifyItemChanged(nowPos);
        photoDao.insert(orgPT);
        MediaScannerConnection.scanFile(mContext,
                new String[]{new File (fullFolder, tgtName).toString()}, null, null);
        finish();
        makeFolderSumNail.makeReady();
    }

    private void getLocationInfo() {
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        strPlace = "";
//        nowLatLng = String.format(Locale.ENGLISH, "%.5f ; %.5f ; %.1f", latitude, longitude, altitude);
//        nowLatLng = LatLngConv.latLng2String(latitude, longitude, altitude);
        strAddress = GPS2Address.get(geocoder, latitude, longitude);
        EditText et = findViewById(R.id.placeAddress);
        String text = "\n"+strAddress;
        et.setText(text);
        et.setSelection(text.indexOf("\n"));
    }

    private void getPlaceByLatLng() {
        placeInfos = new ArrayList<>();
        nowDownLoading = true;
        ImageView iVPlace = findViewById(R.id.queryLocs);
        iVPlace.setAlpha(0.2f);
        EditText et = findViewById(R.id.placeAddress);
        String placeName = et.getText().toString();
        if (placeName.startsWith("?")) {
            String[] placeNames = placeName.split("\n");
            byPlaceName = placeNames[0].substring(1);
        } else
            byPlaceName = "";
        new PlaceRetrieve(mContext, latitude, longitude, placeType, pageToken, sharedRadius, byPlaceName);
        new Timer().schedule(new TimerTask() {
            public void run() {
                iVPlace.setAlpha(1f);
                Intent intent = new Intent(mContext, SelectActivity.class);
                startActivity(intent);
            }
        }, 1500);
    }

    private void getPhotoExif(File fileFullName) {
        Date photoDate;
        try {
            exif = new ExifInterface(fileFullName.getAbsolutePath());
        } catch (Exception e) {
            utils.log("1",e.toString());
            e.printStackTrace();
        }
        maker = exif.getAttribute(ExifInterface.TAG_MAKE);
        model = exif.getAttribute(ExifInterface.TAG_MODEL);
        orient = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        longitude = LatLngConv.DMS2GPS(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
                exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
        latitude = LatLngConv.DMS2GPS(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
                            exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
        altitude = LatLngConv.ALT2GPS(exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE),
                            exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF));
        dateTimeColon = exif.getAttribute(ExifInterface.TAG_DATETIME);
        photoDate = new Date(fileFullName.lastModified());
        if (dateTimeColon != null) {
            try {
                photoDate = sdfDate.parse(dateTimeColon);
            } catch (Exception e) {
                Log.e("Exception", "on date "+ dateTimeColon);
            }
        }
        dateTimeFileName = sdfFile.format(photoDate.getTime());
    }

    private String buildLongInfo() {

        return "Directory : "+ fullFolder +"\nFile Name : "+fileFullName.getName()+
                "\nDevice: "+maker+" - "+model+"\nOrientation: "+ orient +
                "\nLocation: "+latitude+", "+longitude+", "+altitude+
                "\nDate Time: "+dateTimeFileName+
                "\nSize: "+ bitmapImage.getWidth()+" x "+ bitmapImage.getHeight();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        orgPT = photoTags.get(nowPos);

        if (item.getItemId() == R.id.sharePhoto) {
            ArrayList<File> arrayList = new ArrayList<>();
            arrayList.add(fileFullName);
            new SharePhoto().send(getApplicationContext(), arrayList);
            return true;
        }
        else if (item.getItemId() == R.id.markDelete) {
            nowPlace = null;
            deleteOnConfirm(nowPos);
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.renameClock) {
            orgPT.isChecked = false;
            String orgName = orgPT.photoName;
            File tgtFile;
            int C = 67; // 'C'
            do {
                tgtFile = new File(orgPT.fullFolder, dateTimeFileName + (char)C + ".jpg");
                if (!tgtFile.exists())
                    break;
                C++;
            } while (C < 84);
            File oldFile = new File (orgPT.fullFolder, orgName);
            oldFile.renameTo(tgtFile);
            MediaScannerConnection.scanFile(mContext,
                    new String[]{tgtFile.toString(), oldFile.toString()}, null, null);
            orgPT.photoName = tgtFile.getName();
            photoTags.set(nowPos, orgPT);
            photoAdapter.notifyItemChanged(nowPos, orgPT);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void deleteOnConfirm(int pos) {
        final PhotoTag photoTag = photoTags.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Delete photoTag ?");
        builder.setMessage(photoTag.photoName);
        builder.setPositiveButton("Yes",
                (dialog, which) -> {
                    File file = new File (photoTag.fullFolder, photoTag.photoName);
                    if (file.delete()) {
                        MediaScannerConnection.scanFile(mContext, new String[]{file.toString()}, null, null);
                        photoDao.delete(orgPT);
                        photoTags.remove(pos);
                        makeFolderSumNail.makeReady();
                        photoAdapter.notifyItemRemoved(nowPos);
                    }
                })
                .setNegativeButton("No",
                (dialog, which) -> {
                });
        MainActivity.showPopup(builder);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        int pos = (nowPos > 3) ? nowPos-3:0;
        photoView.scrollToPosition(pos);
    }
}