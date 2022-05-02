package com.urrecliner.phototag;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PhotoDao {
    //    @Query("SELECT * FROM phototag")  // load all 은 안 하는 걸로
//    List<PhotoTag> getAll();

    /* query all photos within one fullFolder */
    @Query("SELECT * FROM phototag ")
    List<PhotoTag> getAllPhotos();

    /* query all photos within one fullFolder */
    @Query("SELECT photoName  FROM phototag WHERE fullFolder LIKE :fullFolder AND "
           + " orient < '9' ")
    List<String> getAllInFolder(String fullFolder);

    /* query unique directory list */
    @Query("SELECT DISTINCT fullFolder from phototag")
    List<String> getAllFolders();

    /* query photo counts in one folder */
    @Query("SELECT COUNT(photoName) FROM phototag WHERE fullFolder LIKE :fullFolder")
    List<Integer> getRowCount(String fullFolder);

    /* query by unique full path name */
    @Query("SELECT * FROM phototag WHERE fullFolder LIKE :fullFolder AND "
            + "photoName LIKE :photoName LIMIT 1 ")
    PhotoTag getByPhotoName(String fullFolder, String photoName);

    /* query one folder by unique full path name */
    /* if success photoname has lastModified */
    @Query("SELECT * FROM phototag WHERE fullFolder LIKE :fullFolder LIMIT 1 ")
    PhotoTag getFolderInfo(String fullFolder);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PhotoTag photoTag);

    @Update
    void update(PhotoTag photoTag);

    @Delete
    void delete(PhotoTag photoTag);

    @Query("DELETE FROM phototag WHERE fullFolder = :fullFolder")
    void deleteFolder(String fullFolder);

}