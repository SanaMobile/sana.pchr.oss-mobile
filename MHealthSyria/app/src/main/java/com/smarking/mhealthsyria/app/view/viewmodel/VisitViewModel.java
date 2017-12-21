package com.smarking.mhealthsyria.app.view.viewmodel;

import com.smarking.mhealthsyria.app.model.Visit;

import java.util.Date;

/**
 * Created by Saravanan Vaithianathan
 * (svaithia@uwaterloo.ca) on 15-04-25.
 */
public class VisitViewModel {

    public final Visit visit;
    public final String displayName;
    public final Date updatedDate;

    public VisitViewModel(String displayName, Date updatedDate, Visit visit){
        this.displayName = displayName;
        this.updatedDate = updatedDate;
        this.visit = visit;
    }

}
