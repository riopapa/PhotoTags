package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.buildBitMap;
import static com.urrecliner.phototag.Vars.byPlaceName;
import static com.urrecliner.phototag.Vars.copyPasteText;
import static com.urrecliner.phototag.Vars.fullFolder;
import static com.urrecliner.phototag.Vars.mActivity;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.makeFolderThumbnail;
import static com.urrecliner.phototag.Vars.saveWithTags;
import static com.urrecliner.phototag.Vars.nowDownLoading;
import static com.urrecliner.phototag.Vars.nowPlace;
import static com.urrecliner.phototag.Vars.nowPos;
import static com.urrecliner.phototag.Vars.photoAdapter;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.photoTags;
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

/* thumbnail 은 찍는 방향과 관계없이 가로 방향으로 만들어 줌 따라서 exif가
    '3' 이면
         buildDb 시 180 회전해서 보관,
         tag editor에서도 회전해서 보여주고 180을 기억해둠
        save with tag 일 때는 그 bitimage, orientation = '1'로 보관
    '6' 이면
        buildDb 시 90 회전해서 보관
        tag editor애서도 회전해서 보여주고 90 을 기억해 둠
        save with tag 일 때 그 bitImage, orientation = '1'로 보관
 */


public class TagPlaceActivity extends AppCompatActivity {

    ExifInterface exif = null;
    String strAddress = null, strPlace = null;
    String dateTimeColon, dateTimeFileName = null;
    String maker, model;
    double latitude, longitude, altitude;
    PhotoTag orgPT, newPT;
    File fileFullName;
    String orient;
    Bitmap viewImage;
    ImageView photoView, sigView;
    int degree = 0;

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

        utils.showFolder(this.getSupportActionBar());

        buildPhotoScreen();
    }

    void buildPhotoScreen() {

        orgPT = photoTags.get(nowPos);
        fileFullName = new File (orgPT.fullFolder, orgPT.photoName);
        if (!fileFullName.exists())
            return;
        photoView = findViewById(R.id.image);
        photoView.post(this::adjust_SignaturePos);
        viewImage = BitmapFactory.decodeFile(fileFullName.getAbsolutePath());
        getPhotoExif(fileFullName);
        orgPT.orient = orient;
        degree = 0;

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
            viewImage = Bitmap.createBitmap(viewImage, 0, 0,
                    viewImage.getWidth(), viewImage.getHeight(), matrix, false);
        }

        photoView.setImageBitmap(viewImage);
        sigView = findViewById(R.id.signature);
        sigView.setImageResource(sigColors[sharedSigNbr]);
        sigView.setOnClickListener(view -> {
            sharedSigNbr++;
            sharedSigNbr = sharedSigNbr % sigColors.length;
            sigView.setImageResource(sigColors[sharedSigNbr]);
        });
        PhotoViewAttacher pA;       // to enable zoom
        pA = new PhotoViewAttacher(photoView);
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

        ImageView iVSave = findViewById(R.id.save_with_mark);
        iVSave.setOnClickListener(view -> {
            save_with_tags();
        });
        iVSave.setAlpha(fileFullName.getName().endsWith("_ha.jpg") ? 0.2f: 1f);

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

        ImageView ivRefresh = findViewById(R.id.refresh);
        ivRefresh.setOnClickListener(view -> {
            PhotoTag photoOut = buildBitMap.updateThumbnail(orgPT);
            photoDao.insert(photoOut);
            photoAdapter.notifyItemChanged(nowPos);
            photoAdapter.notifyItemRangeChanged((nowPos > 0) ? nowPos-1:0, 3);
        });

        ImageView iVRotateSave = findViewById(R.id.rotate_save);
        iVRotateSave.setOnClickListener(v -> save_rotatedPhoto());
        iVRotateSave.setAlpha(0.2f);

        ImageView ivRotate = findViewById(R.id.rotate);
        ivRotate.setOnClickListener(view -> {
            rotate_photo(iVRotateSave);
        });

        final ImageView ivLeft = findViewById(R.id.imageL);
        if (nowPos > 0) {
            ivLeft.post(() -> {
                int width = ivLeft.getMeasuredWidth();
                int height = ivLeft.getMeasuredHeight();
                PhotoTag pTag = photoTags.get(nowPos-1);
                Bitmap thumbnail = pTag.getThumbnail();
                if (thumbnail == null) {
                    pTag = BuildDB.getPhotoWithMap(pTag);
                    thumbnail = pTag.getThumbnail();
                }
                Bitmap bitmap = maskImage(thumbnail, false);
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
                Bitmap thumbnail = pTag.getThumbnail();
                if (thumbnail == null) {
                    pTag = BuildDB.getPhotoWithMap(pTag);
                    thumbnail = pTag.getThumbnail();
                }
                Bitmap bitmap = maskImage(thumbnail, true);
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
    }

    private void save_with_tags() {
        if (latitude == 0 && longitude == 0)
            Toast.makeText(mContext,"No GPS Information to retrieve places",Toast.LENGTH_LONG).show();
        EditText etPlace = findViewById(R.id.placeAddress);
        nowPlace = etPlace.getText().toString();
        if (nowPlace.length() > 2) {
            newPT = saveWithTags.save(orgPT, viewImage);
            photoTags.add(nowPos, newPT);
            photoAdapter.notifyItemInserted(nowPos);
            photoAdapter.notifyItemRangeChanged((nowPos > 0) ? nowPos-1:0, 3);
            finish();
            makeFolderThumbnail.makeReady();
        }
    }

    private void rotate_photo(ImageView iVRotateSave) {
        Matrix matrix = new Matrix();
        degree -= 90;
        matrix.postRotate(-90);
        viewImage = Bitmap.createBitmap(viewImage, 0, 0, viewImage.getWidth(), viewImage.getHeight(), matrix, false);
        photoView.setImageBitmap(viewImage);
        PhotoViewAttacher pA1;       // to enable zoom
        pA1 = new PhotoViewAttacher(photoView);
        pA1.update();
        iVRotateSave.setAlpha(1f);
        adjust_SignaturePos();
    }

    private void adjust_SignaturePos() {
        boolean landscape = viewImage.getWidth() > viewImage.getHeight();
        int rightMargin = (landscape) ? 48: 200;
        int topMargin = ((landscape) ? 380: 48);
        ConstraintLayout.LayoutParams lP = (ConstraintLayout.LayoutParams) sigView.getLayoutParams();
        lP.topMargin = topMargin;
        lP.rightMargin = rightMargin;
        sigView.setLayoutParams(lP);

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
        PhotoTag rotatedPhoto = photoTags.get(nowPos);
        String orgName = orgPT.photoName;
        String tgtName = orgName.substring(0,orgName.length()-4)+"R.jpg";
        rotatedPhoto.photoName = tgtName;

        utils.createPhotoFile(fullFolder, orgName, tgtName, viewImage);
        rotatedPhoto.orient = "1";
        rotatedPhoto.isChecked = false;
        rotatedPhoto.photoName = tgtName;
        rotatedPhoto.thumbnail = null;
        photoDao.insert(rotatedPhoto);      // insert or replace
        if (nowPos > 0 && photoTags.get(nowPos-1).photoName.equals(tgtName)) {
                photoTags.set(nowPos-1, rotatedPhoto);
                photoAdapter.notifyItemChanged(nowPos-1);
        } else {
            photoTags.add(nowPos, rotatedPhoto);
            photoAdapter.notifyItemInserted(nowPos);
        }
        photoAdapter.notifyItemRangeChanged((nowPos > 1) ? nowPos-2:0,4);
        MediaScannerConnection.scanFile(mContext,
                new String[]{new File (fullFolder, tgtName).toString()}, null, null);
        finish();
        Toast.makeText(mContext, "Photo thumbnail rotated",Toast.LENGTH_SHORT).show();
        makeFolderThumbnail.makeReady();
    }

    private void getLocationInfo() {
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        strPlace = "";
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
            utils.log("getPhotoExif",e.toString());
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
                "\nSize: "+ viewImage.getWidth()+" x "+ viewImage.getHeight();
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
            photoAdapter.notifyItemRangeChanged((nowPos > 0) ? nowPos-1:0, 3);
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
                        makeFolderThumbnail.makeReady();
                        photoAdapter.notifyItemRemoved(nowPos);
                        photoAdapter.notifyItemRangeChanged((nowPos > 0) ? nowPos-1:0,3);
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
        Vars.photoView.scrollToPosition(pos);
    }
}