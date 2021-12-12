package com.example.fer_medindex.fragment;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.fer_medindex.R;
import com.example.fer_medindex.view.Background;
import com.example.fer_medindex.view.BackgroundDoctor;
import com.example.fer_medindex.view.ChangePassword;
import com.example.fer_medindex.view.UpdateProfile;
import com.example.fer_medindex.view.UploadProfile;
import com.google.firebase.auth.FirebaseAuth;


public class OptionFragment extends Fragment {

    private Button buttondoiAvatar , buttondoithongtin, buttondoimatkhau, buttonthoat;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_option, container, false);

        buttondoiAvatar = view.findViewById(R.id.doi_avatar);
        buttondoimatkhau = view.findViewById(R.id.doi_mat_khau);
        buttondoithongtin = view.findViewById(R.id.sua_thong_tin);
        buttonthoat=view.findViewById(R.id.dang_xuat);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttondoiAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UploadProfile.class);
            startActivity(intent);
        });

        buttondoithongtin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UpdateProfile.class);
            startActivity(intent);
        });

        buttondoimatkhau.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePassword.class);
            startActivity(intent);
        });

        buttonthoat.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Exit App");
            builder.setMessage("Thoát");
            builder.setPositiveButton("YES", (dialog, which) -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(requireContext(), Background.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
            builder.setNegativeButton("NO", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }
}