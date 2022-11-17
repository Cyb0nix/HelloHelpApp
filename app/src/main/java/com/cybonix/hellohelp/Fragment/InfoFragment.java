package com.cybonix.hellohelp.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cybonix.hellohelp.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InfoFragment extends Fragment {

    ImageView image_mairie;

    String link_image = "https://firebasestorage.googleapis.com/v0/b/hello-help.appspot.com/o/uploads%2Fplace-gabriel-peri-20092014-001.jpg?alt=media&token=986e3d72-9d3d-4496-94e6-836b9c97d148";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_info, container, false);

        image_mairie = view.findViewById(R.id.image_mairie);

        Glide.with(getContext()).load(link_image).into(image_mairie);







        return view;
    }


}
