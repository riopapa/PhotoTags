
# Photo Tag

With this application, you can create memory file with place, time stamp based on the file you selected

It retrieves GPS, date time stamp fro Exif  of photo and add them into photo into new photo file. It can save place and address information also.

This application is useful if you want to memorize where you visited in photo

## Permission Required

```
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="com.google.android.providers.gsf.permission.READ_GSERVICES" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Source in github

Application source can be downloaded from [my github][1].

[1]: https://github.com/riopapa/tagPhoto

## Main Screen

Run this program, and it shows all the photos in your Camera directory.

<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="./screenshots/Screen_MainActivity.jpg" width="400" height="800" alt="multiple photos are selected"/> 

<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="./screenshots/Screen_MainActivity3.jpg" width="400" height="800" alt="multiple photos are selected"/> 


1. <**cancel select**> icon <img src="./app/src/main/res/mipmap-xxxhdpi/icon_cancel_select.png" width=20 height=20>&nbsp;&nbsp; will reset all selected items.

2. Select one or multiple photos using check mark, then press <**multi select**> icon <img src="./app/src/main/res/mipmap-xxxhdpi/icon_mark_multi.png" height="20" alt="Mark multiple"/>. New Photo(s) will be create with timestamp (from photo Exif information) and signature logo at the bottom

3. If <**delete**> icon <img src="./app/src/main/res/mipmap-xxxhdpi/icon_delete.png" height="20" alt="One photo selected"/> is pressed, confirm message to delete will be prompted (multiple selection available).
4. <**2 or 3**> icon  <img src="./app/src/main/res/mipmap-xxxhdpi/icon_two.png" width=20 height=20>&nbsp;&nbsp; ,  <img src="./app/src/main/res/mipmap-xxxhdpi/icon_three.png" width=20 height=20>&nbsp;&nbsp;  is for switching two photos or three photos in one row.

5. <**folder**> icon  <img src="./app/src/main/res/mipmap-xxxhdpi/icon_folder_change.png" width=20 height=20>&nbsp;&nbsp;  is for changing directory to access photos.

- By pressing one photo (not check mark area), it will ask you google place information, and the shows big photo with place, address information selection by google map. (refer to <**One photo screen**>)

- _**Now Building nowCount/totalCount**_ will be displayed while building small images, you can press <**hide**> button not to display any time.

## Tag Place Screen

<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="./screenshots/Screen_MarkUpPlace.jpg" width="400" height="800" alt="Markup Place"/> 

- If you press  <**Apply**> icon <img src="./app/src/main/res/mipmap-xxxhdpi/icon_mark_place.png" width=20 height=20>&nbsp;&nbsp; it will create marked photo with date,time, place, address, those goes to suffix of file name, e.g. __20191007_130414 Korean Village Restaurant_ha.jpg__. <br>And this file will be displayed in photos with grayed file name title.

- If <**Delete**> icon <img src="./app/src/main/res/mipmap-xxxhdpi/icon_delete.png" height="20" alt="One photo selected"/> is pressed, confirm message to delete will be prompted

- <**Save**> icon <img src="./app/src/main/res/mipmap-xxxhdpi/icon_save.png" height="20" alt="Save"/> will be activated only when you press <img src="./app/src/main/res/mipmap-xxxhdpi/icon_rotate.png" height="20" alt="One photo selected"/>at the bottom is pressed, which will roate the photo.


- <**Rename to clock**> icon <img src="./app/src/main/res/mipmap-xxxhdpi/icon_rename_clocks.png" height="20" alt="One photo selected"/> is for renaming file name with the date photo has in Exif. This is usefull when your photo has only random number file name.

- Images at the bottom right/left is for navigating to next photo.
 
- If you press  <**go_back**> button screen, photo list will be shown.

## Directory Screen

<br>This screen comes out when you press  <**folder**> icon  <img src="./app/src/main/res/mipmap-xxxhdpi/icon_folder_change.png" width=20 height=20>&nbsp;&nbsp; , each active directory folder containing photos will be promted with max 4 photos.
<br>remarks: Default folder is _*DCIM/Camera*_

<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="./screenshots/Screen_Directory.jpg" width="400" height="800" alt="One photo selected"/>
<br><br>Each directory folder will show max 4 photos in that directory.

- By clicking one directory photo, main activity will be displayed.

- Making thumbnails will be processed at the background with showing progress at the bottom


## What I have learned with this app

- RecyclerView
- Masking imageView
- SnackBar
- Exif handling

## Comments

- Android SDK API 27 or above 
- Signature png file is required
- secrete.xml should be provided by yourself for google maps api key
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="maps_api_key translate="false">AIzaSyCEun8V0vX_idWcvanxxxxxxxxxx</string>
</resources>
```