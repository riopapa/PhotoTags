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
    @Query("SELECT * FROM phototag WHERE fullFolder LIKE :fullFolder")
    List<PhotoTag> getAllInFolder(String fullFolder);

    /* query by unique full path name */
    @Query("SELECT * FROM phototag WHERE fullFolder LIKE :fullFolder AND "
            + "photoName LIKE :photoName LIMIT 1 ")
    PhotoTag getByPhotoName(String fullFolder, String photoName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PhotoTag photoTag);

    @Update
    void update(PhotoTag photoTag);

    @Delete
    void delete(PhotoTag photoTag);

}