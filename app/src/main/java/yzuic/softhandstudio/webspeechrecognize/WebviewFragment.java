package yzuic.softhandstudio.webspeechrecognize;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by d9001 on 2017/4/17.
 */

public class WebviewFragment extends Fragment {
    private final String TAG_WV = "Webview";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        Log.w(TAG_WV, "OnCreate");
        Log.w(TAG_WV, "Now on WebviewFragment!");

        return rootView;
    }

}
