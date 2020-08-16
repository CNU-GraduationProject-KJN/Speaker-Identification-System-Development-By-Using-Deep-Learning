package com.example.speaker_identification_system.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.example.speaker_identification_system.Modify.ModifyAudioActivity;
import com.example.speaker_identification_system.Modify.ModifyNameActivity;
import com.example.speaker_identification_system.R;

public class ChooseDialogFragment extends DialogFragment implements View.OnClickListener {


    public static ChooseDialogFragment newInstance() {
        ChooseDialogFragment fragment = new ChooseDialogFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();

        int dialogWidth = getResources().getDimensionPixelSize(R.dimen.dialog_select_width);
        int dialogHeight = getResources().getDimensionPixelSize(R.dimen.dialog_select_height);
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_choose_dialog, null);


        view.findViewById(R.id.btn_top).setOnClickListener(this);
        view.findViewById(R.id.btn_bottom).setOnClickListener(this);

        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }


    private void dismissDialog() {
        this.dismiss();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_top:
                intent = new Intent(getActivity().getApplication(), ModifyNameActivity.class);
                startActivity(intent);
                dismissDialog();
                break;
            case R.id.btn_bottom:
                intent = new Intent(getActivity().getApplication(), ModifyAudioActivity.class);
                startActivity(intent);
                dismissDialog();
                break;
        }
    }



}
