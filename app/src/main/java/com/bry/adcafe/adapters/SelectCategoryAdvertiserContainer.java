package com.bry.adcafe.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.ui.SelectCategoryAdvertiser;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.util.List;

/**
 * Created by bryon on 19/03/2018.
 */

@NonReusable
@Layout(R.layout.select_category_container)
public class SelectCategoryAdvertiserContainer {
    @View(R.id.MainCategoryTitle) private TextView mainCategoryNameView;
    @View(R.id.PHViewForGeneralCategory) private PlaceHolderView mPHVewForGeneralCategoryView;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String mCategoryName;
    private List<String> mSubCategories;

    public SelectCategoryAdvertiserContainer(Context context,PlaceHolderView placeHolderView,String CategoryName, List<String> subCategories){
        this.mContext = context;
        this.mPlaceHolderView = placeHolderView;
        this.mCategoryName = CategoryName;
        this.mSubCategories = subCategories;
    }

    @Resolve
    private void onResolved() {
        mainCategoryNameView.setText(mCategoryName+".");
        if(mCategoryName.equals("mens-fasion"))mainCategoryNameView.setText("mens-fashion.");
        if(mCategoryName.equals("womens-fasion"))mainCategoryNameView.setText("womens-fashion.");

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mPHVewForGeneralCategoryView.setLayoutManager(layoutManager);
        for(String subCategory: mSubCategories){
            mPHVewForGeneralCategoryView.addView(new SelectCategoryAdvertiserItem(mContext,mPHVewForGeneralCategoryView, subCategory));
        }

    }

}
