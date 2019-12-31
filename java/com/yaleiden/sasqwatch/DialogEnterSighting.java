package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Yale on 6/19/2015.
 */
public class DialogEnterSighting extends DialogFragment {

    private String TAG = "SightingDialog";
    private String[] aEncounter = {"Sighting", "Audial", "Sign"};
    private String[] aBehavior = {"NA","Walking", "Running", "Trundling", "Vocalizing", "Sleeping", "Feeding", "Sitting"};
    private String[] aHabitat = {"Mountain forest", "Mountain meadow", "Lake shore", "Creek side", "River side", "Suburban", "Ag land", "Open land", "Tall grass"};
    private String[] aSign = {"NA","Track", "Scat", "Hair", "Prey kill", "Bedding area", "Territory mark"};

    private EditText editTextComment;
    private Spinner spinnerHabitat;
    private Spinner spinnerBehavior;
    private Spinner spinnerType;
    private Spinner spinnerSign;

    private ArrayAdapter<String> typeAdapter;
    private AutoCompleteTextView actv;

    private ArrayAdapter<String> behaviorAdapter;
    private ArrayAdapter<String> signAdapter;
    private ImageView imageViewSighting;
    private Bitmap sightingBmp;
    String picByteArray;
    Button button;
    OnSightingSaveListener mCallback;
    private static final int SELECT_PHOTO = 100;

    public DialogEnterSighting() {
        // Empty constructor required for DialogFragment
    }


    // Container Activity must implement this interface
    public interface OnSightingSaveListener {
        public void OnSightingSaved(String comment, String state, String behavior, String encounter, String sign, String image);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sighting, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        double inLat = getArguments().getDouble("latitude");
        double inLon = getArguments().getDouble("longitude");
        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText("Enter Data For Sasquatch Encounter");
        TextView textView2 = (TextView) view.findViewById(R.id.textView2);
        TextView textView3 = (TextView) view.findViewById(R.id.textView3);
        textView2.setText("Lat: "+String.valueOf(inLat));
        textView3.setText("Lng: "+String.valueOf(inLon));
        editTextComment = (EditText) view.findViewById(R.id.editTextComment);
        //spinnerHabitat = (Spinner) view.findViewById(R.id.spinnerHabitat);
        actv = (AutoCompleteTextView) view.findViewById(R.id.textViewstate);
        spinnerBehavior = (Spinner) view.findViewById(R.id.spinnerBehavior);
        spinnerType = (Spinner) view.findViewById(R.id.spinnerType);
        spinnerSign = (Spinner) view.findViewById(R.id.spinnerSign);
        imageViewSighting = (ImageView) view.findViewById(R.id.imageViewSighting);
        button = (Button) view.findViewById(R.id.buttonSigthing);
        //getDialog().setTitle("Enter Data For Sasquatch Encounter");

        //habitatAdapter = new ArrayAdapter<String>(getActivity(),R.layout.support_simple_spinner_dropdown_item, aHabitat);
        behaviorAdapter = new ArrayAdapter<String>(getActivity(),R.layout.support_simple_spinner_dropdown_item, aBehavior);
        typeAdapter = new ArrayAdapter<String>(getActivity(),R.layout.support_simple_spinner_dropdown_item, aEncounter);
        signAdapter = new ArrayAdapter<String>(getActivity(),R.layout.support_simple_spinner_dropdown_item, aSign);

        spinnerBehavior.setAdapter(behaviorAdapter);
        //spinnerHabitat.setAdapter(habitatAdapter);
        String[] states = getResources().getStringArray(R.array.states);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,states);
        actv.setAdapter(adapter);

        spinnerType.setAdapter(typeAdapter);
        spinnerSign.setAdapter(signAdapter);



        imageViewSighting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                getActivity().startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "button clicked");

                String comment = "no comments";
                comment = editTextComment.getText().toString();
                //String habitat = spinnerHabitat.getSelectedItem().toString();
                String state = actv.getText().toString();
                String behavior = spinnerBehavior.getSelectedItem().toString();
                String encounter = spinnerType.getSelectedItem().toString();
                String sign = spinnerSign.getSelectedItem().toString();

                try {
                    ((OnSightingSaveListener) getActivity()).OnSightingSaved(comment, state, behavior, encounter, sign, picByteArray);
                } catch (ClassCastException cce) {
                }

            }

        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        Log.d(TAG, "onAttach");

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSightingSaveListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSightingSaveListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                getActivity();
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    PicUtils picUtils = new PicUtils();
                    try {
                        //imageStream = getContentResolver().openInputStream(selectedImage);
                        sightingBmp = picUtils.decodeUri(selectedImage, getActivity());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    picByteArray = picUtils.bitmapToByte(sightingBmp);
                    imageViewSighting.setImageBitmap(sightingBmp);
                }
        }
    }

}