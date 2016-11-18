package in.newsmeme;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mxn.soul.flowingdrawer_core.MenuFragment;

import java.io.FileInputStream;

/**
 * A simple {@link Fragment} subclass.
*/
public class MyMenuFragment extends MenuFragment {

    public MyMenuFragment() {
        // Required empty public constructor
    }

    View view = null;
    static Button englishButton, hindiButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_menu, container,
                false);
        englishButton = (Button) view.findViewById(R.id.englishButton);
        hindiButton = (Button) view.findViewById(R.id.hindiButton);
        try {
            FileInputStream fis = getContext().openFileInput(getResources().getString(R.string.FILENAME));
            int c;
            String lang = "";
            while ((c = fis.read()) != -1) {
                lang = lang + Character.toString((char) c);
            }
            if(lang.equals(getResources().getString(R.string.englishLang))){
                hindiButton.setBackgroundResource(R.color.transparent);
                englishButton.setBackgroundResource(R.color.colorPrimaryTesing);
                englishButton.setTextColor(Color.WHITE);
                hindiButton.setTextColor(Color.BLACK);
            }
            else if(lang.equals(getResources().getString(R.string.hindiLang))){
                englishButton.setBackgroundResource(R.color.transparent);
                hindiButton.setBackgroundResource(R.color.colorPrimaryTesing);
                hindiButton.setTextColor(Color.WHITE);
                englishButton.setTextColor(Color.BLACK);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return setupReveal(view) ;
    }

    public void onOpenMenu(){
        MainActivity.navButton.setImageResource(R.drawable.nav_menu_back);
    }

    public void onCloseMenu(){
        MainActivity.navButton.setImageResource(R.drawable.nav_menu);
    }

}
