package com.amazic.petalamazic.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.amazic.petalamazic.R;
import com.amazic.petalamazic.databinding.DialogLoadingAdsBinding;
import com.amazic.petalamazic.petal.Petal;

import java.util.Random;

public class LoadingAdsDialog extends Dialog {
    private DialogLoadingAdsBinding binding;
    public LoadingAdsDialog(@NonNull Context context) {
        super(context, R.style.Theme_PetalAmazicAdsLib);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogLoadingAdsBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        setCancelable(false);
        if (Petal.getInstance().isCustomAnimationDialog()) {
            Random random = new Random();
            int randomIndex = random.nextInt(Petal.getInstance().listAnimationDialogRaw.size());
            int randomElement = Petal.getInstance().listAnimationDialogRaw.get(randomIndex);
            setUseAnimationView(randomElement);
        } else {
            setUseProgressBar();
        }
    }

    public void setUseAnimationView(int resId) {
        binding.progressBar.setVisibility(View.GONE);
        binding.animationView.setVisibility(View.VISIBLE);
        binding.animationView.setAnimation(resId);
        binding.animationView.playAnimation();
    }

    public void setUseProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.animationView.setVisibility(View.GONE);
    }
}
