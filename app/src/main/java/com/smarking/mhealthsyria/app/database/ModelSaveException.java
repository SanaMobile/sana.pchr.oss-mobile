package com.smarking.mhealthsyria.app.database;

/**
 * Exception to throw when saving a model instance
 *
 * Created by winkler.em@gmail.com, on 05/03/2016.
 */
public class ModelSaveException  extends Exception {

    public static final String TAG = ModelSaveException.class.getSimpleName();

    public ModelSaveException(){
        super();
    }

    public ModelSaveException(Exception e){
        super(e);
    }

    public ModelSaveException(String message){
        super(message);
    }

    public ModelSaveException(String message, Exception e){
        super(message,e);
    }
}
