package com.ccl.bletools.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;

import com.ccl.bletools.R;


public class BTFilterCardView extends CardView {
    public BTFilterCardView(Context context) {
        this(context, null);
    }

    public BTFilterCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BTFilterCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.view_filter_cardview, this);
    }
}
