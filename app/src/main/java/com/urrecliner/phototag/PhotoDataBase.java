package com.urrecliner.phototag;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PhotoTag.class}, version = 1)
public abstract class PhotoDataBase extends RoomDatabase {

	public abstract PhotoDao photoDao();

}