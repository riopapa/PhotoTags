package com.urrecliner.phototag;

import static com.urrecliner.phototag.Vars.buildBitMap;
import static com.urrecliner.phototag.Vars.buildDB;
import static com.urrecliner.phototag.Vars.dirNotReady;
import static com.urrecliner.phototag.Vars.fullFolder;
import static com.urrecliner.phototag.Vars.mActivity;
import static com.urrecliner.phototag.Vars.mContext;
import static com.urrecliner.phototag.Vars.mainMenu;
import static com.urrecliner.phototag.Vars.makeDirFolder;
import static com.urrecliner.phototag.Vars.markTextInColor;
import static com.urrecliner.phototag.Vars.markTextOutColor;
import static com.urrecliner.phototag.Vars.multiMode;
import static com.urrecliner.phototag.Vars.photoAdapter;
import static com.urrecliner.phototag.Vars.photoDB;
import static com.urrecliner.phototag.Vars.photoDao;
import static com.urrecliner.phototag.Vars.photoTags;
import static com.urrecliner.phototag.Vars.photoView;
import static com.urrecliner.phototag.Vars.sharedPref;
import static com.urrecliner.phototag.Vars.sharedSpan;
import static com.urrecliner.phototag.Vars.short2Folder;
import static com.urrecliner.phototag.Vars.signatureMap;
import static com.urrecliner.phototag.Vars.sizeX;
import static com.urrecliner.phototag.Vars.spanWidth;
import static com.urrecliner.phototag.Vars.squeezeDB;
import static com.urrecliner.phototag.Vars.newPhoto;
import static com.urrecliner.phototag.Vars.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        mActivity = this;
        photoView = findViewById(R.id.photoView);
        utils = new Utils();
        utils.log("tag", "Start--");
        askPermission();
        squeezeDB = new SqueezeDB();
        buildDB = new BuildDB();
        newPhoto = new NewPhoto();
        buildBitMap = new BuildBitMap();

        photoDB = Room.databaseBuilder(getApplicationContext(), PhotoDataBase.class, "photoTag-db")
                .fallbackToDestructiveMigration()   // scima changeable
                .allowMainThreadQueries()           // main thread 에서 IO
                .build();

        photoDao = photoDB.photoDao();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        sizeX = size.x;

        photoTags = new ArrayList<>();
        utils.getPreference();
        fullFolder = sharedPref.getString("fullFolder", new File(Environment.getExternalStorageDirectory(),"DCIM/Camera").toString());
        utils.setShortFolderNames(fullFolder);
        markTextInColor = sharedPref.getInt("markTextInColor", ContextCompat.getColor(mContext, R.color.markInColor));
        markTextOutColor = sharedPref.getInt("markTextOutColor", ContextCompat.getColor(mContext, R.color.markOutColor));
        signatureMap = buildBitMap.buildSignatureMap();
        ArrayList<File> photoFiles = utils.getFilteredFileList(fullFolder);
        if (photoFiles.size() == 0) {
            Toast.makeText(getApplicationContext(), "No jpg files in " + short2Folder + " folder\nSelect Folder", Toast.LENGTH_LONG).show();
//            finish();
//            Intent intent = new Intent(this, DirectoryActivity.class);
//            startActivity(intent);
//            return;
//            shortFolder = "DCIM/Camera";
//            longFolder = new File(Environment.getExternalStorageDirectory(), shortFolder).toString();
        }
        photoFiles.sort(Collections.reverseOrder());

        prepareCards();
        for (File photoFile : photoFiles) {
            PhotoTag photoTag = new PhotoTag();
            photoTag.fullFolder = fullFolder;
            photoTag.photoName = photoFile.getName();
            photoTag.orient = "x";
            photoTags.add(photoTag);
        }

        utils.showFolder(this.getSupportActionBar());
        photoAdapter = new PhotoAdapter();
        photoView.setAdapter(photoAdapter);
        makeDirFolder = new MakeDirFolder();
        squeezeDB.getAll();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dirNotReady) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(() -> {
                        MenuItem item = mainMenu.findItem(R.id.action_Directory);
                        item.setEnabled(true);
                        item.getIcon().setAlpha(255);
                    }, 100);
                }
            }, 1000);
        }
        utils.deleteOldLogFiles();
        FloatingActionButton fabUndo = findViewById(R.id.undo_select);
        fabUndo.setVisibility(View.INVISIBLE);
        fabUndo.setOnClickListener(v -> {
            multiMode = false;
            int totCount = photoTags.size();
            for (int i = 0; i < totCount; i++) {
                PhotoTag photoTag = photoTags.get(i);
                if (photoTag.isChecked()) {
                    photoTag.setChecked(false);
                    photoTags.set(i, photoTag);
                    photoAdapter.notifyItemChanged(i, photoTag);
                }
            }
//            photoAdapter.notifyDataSetChanged();
            fabUndo.setVisibility(View.INVISIBLE);
            MenuItem item = mainMenu.findItem(R.id.action_Delete);
            item.setVisible(false);

        });
        buildDB.fillUp(findViewById(R.id.main_layout));
    }

    static void prepareCards() {
        int span = Integer.parseInt(sharedSpan);
        StaggeredGridLayoutManager SGL = new StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL);
        photoView.setLayoutManager(SGL);
        photoView.addItemDecoration(new DividerItemDecoration(mContext, SGL.getOrientation()));
        photoView.setLayoutManager(SGL);
        photoView.setBackgroundColor(0x88000000 + Color.GRAY);
        spanWidth = (sizeX / span) * 96 / 100;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mainMenu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (dirNotReady) {
            MenuItem item = mainMenu.findItem(R.id.action_Directory);
            item.setEnabled(false);
            item.getIcon().setAlpha(35);
        }
        MenuItem item = mainMenu.findItem(R.id.action_Delete);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.action_setting) {
            intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.action_Directory) {
            finish();
            intent = new Intent(this, DirectoryActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item.getItemId() == R.id.action_Delete) {
            final ArrayList<String> toDeleteList = build_DeletePhoto();
            if (toDeleteList.size()> 0) {
                StringBuilder msg = new StringBuilder();
                for (String s : toDeleteList) msg.append("\n").append(s);
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("Delete multiple photos ?");
                builder.setMessage(msg.toString());
                builder.setPositiveButton("Yes", (dialog, which) -> DeleteMulti.run());
                builder.setNegativeButton("No", (dialog, which) -> { });
                showPopup(builder);
            } else {
                Toast.makeText(mContext,"Photo selection is required to delete",Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    static void showPopup(AlertDialog.Builder builder) {
        AlertDialog dialog = builder.create();
        dialog.show();
        Button btn = dialog.getButton(Dialog.BUTTON_POSITIVE);
        btn.setTextSize(16);
        btn.setAllCaps(false);
        btn = dialog.getButton(Dialog.BUTTON_NEGATIVE);
        btn.setTextSize(24);
        btn.setAllCaps(false);
        btn.setFocusable(true);
        btn.setFocusableInTouchMode(true);
        btn.requestFocus();
    }

    private ArrayList<String> build_DeletePhoto() {
        ArrayList<String> arrayList = new ArrayList<>();

        for (PhotoTag photoTag: photoTags) {
            if (photoTag.isChecked())
                arrayList.add(photoTag.getPhotoName());
        }
        return arrayList;
    }

    final long BACK_DELAY = 4000;
    long backKeyPressedTime;
    @Override
    public void onBackPressed() {

        if(System.currentTimeMillis()<backKeyPressedTime+BACK_DELAY){
            buildDB.cancel();
            squeezeDB.cancel();
            finish();
            new Timer().schedule(new TimerTask() {
                public void run() {
                    finishAffinity();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }, 500);
        }
        Toast.makeText(this, "Press BackKey again to quit",Toast.LENGTH_SHORT).show();
        backKeyPressedTime = System.currentTimeMillis();
    }


    // ↓ ↓ ↓ P E R M I S S I O N   RELATED /////// ↓ ↓ ↓ ↓  BEST CASE 20/09/27 with no lambda
    private final static int ALL_PERMISSIONS_RESULT = 101;
    ArrayList <String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    String [] permissions;

    private void askPermission() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            permissions = info.requestedPermissions;//This array contain
        } catch (Exception e) {
            Log.e("Permission", "Not done", e);
        }

        permissionsToRequest = findUnAskedPermissions();
        if (permissionsToRequest.size() != 0) {
            requestPermissions((String[]) permissionsToRequest.toArray(new String[0]),
//            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    ALL_PERMISSIONS_RESULT);
        }
    }

    private ArrayList<String> findUnAskedPermissions() {
        ArrayList <String> result = new ArrayList<>();
        for (String perm : permissions) if (hasPermission(perm)) result.add(perm);
        return result;
    }
    private boolean hasPermission(String permission) {
        return (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (Object perms : permissionsToRequest) {
                if (hasPermission((String) perms)) {
                    permissionsRejected.add((String) perms);
                }
            }
            if (permissionsRejected.size() > 0) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
            }
        }
    }
    private void showDialog(String msg) {
        showMessageOKCancel(msg,
                (dialog, which) -> MainActivity.this.requestPermissions(permissionsRejected.toArray(
                        new String[0]), ALL_PERMISSIONS_RESULT));
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

// ↑ ↑ ↑ ↑ P E R M I S S I O N    RELATED /////// ↑ ↑ ↑

}