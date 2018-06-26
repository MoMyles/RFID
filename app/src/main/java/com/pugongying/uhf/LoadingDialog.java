package com.pugongying.uhf;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wang.avi.AVLoadingIndicatorView;

public class LoadingDialog extends DialogFragment {

    private AVLoadingIndicatorView aiv;

    public static LoadingDialog newInstance() {

        Bundle args = new Bundle();

        LoadingDialog fragment = new LoadingDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_loading, container, false);
        onBindView(view);
        return view;
    }

    private void onBindView(View view) {
        aiv = view.findViewById(R.id.aiv);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (aiv != null) {
            aiv.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (aiv != null) {
            aiv.hide();
        }
    }
}
