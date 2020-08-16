package com.example.speaker_identification_system.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.example.speaker_identification_system.MainActivity;
import com.example.speaker_identification_system.R;

public class ErrorMsgDialogFragment extends DialogFragment implements View.OnClickListener {


    public static ErrorMsgDialogFragment newInstance() {
        ErrorMsgDialogFragment fragment = new ErrorMsgDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        int dialogWidth = getResources().getDimensionPixelSize(R.dimen.dialog_fragment_width);
        int dialogHeight = getResources().getDimensionPixelSize(R.dimen.dialog_fragment_height);
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_error_msg_dialog, null);

        view.findViewById(R.id.done).setOnClickListener(this);

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
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        dismissDialog();
    }

}