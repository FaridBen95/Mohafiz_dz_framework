package com.MohafizDZ.project.tour_edit_dir.configuration_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.List;

public class TourConfigurationPresenterImpl implements ITourConfigurationPresenter.Presenter{
    private static final String TAG = TourConfigurationPresenterImpl.class.getSimpleName();

    private final ITourConfigurationPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;

    public TourConfigurationPresenterImpl(ITourConfigurationPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {
        generateConfigurationChips();
    }

    private void generateConfigurationChips(){
        for(DataRow row : models.tourConfigurationModel.getRows()){
            view.createChip(row.getString("key"), getString(row.getInteger("value")));
        }
    }

    private String getString(int resId){
        return context.getString(resId);
    }


    @Override
    public void onRefresh() {

    }

    private static class Models{
        private final TourModel tourModel;
        private final TourConfigurationModel tourConfigurationModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            tourConfigurationModel = new TourConfigurationModel(context);
        }
    }
}
