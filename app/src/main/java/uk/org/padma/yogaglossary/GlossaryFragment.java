package uk.org.padma.yogaglossary;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;



public class GlossaryFragment extends Fragment implements FilterOptionsDialogFragment.FilterOptionsDialogListener {

    public GlossaryAdapter mAdapter;

    EditText txtFilter;
    Button buttClear;
    Button buttFilterOptions;

    public static final String COOKIE_FILTER_FIElD = "filter_field";

    private SharedPreferences mPrefs;

    public GlossaryFragment() {
    }

    public static GlossaryFragment newInstance() {
        GlossaryFragment fragment = new GlossaryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_glossary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String filter_field = mPrefs.getString(COOKIE_FILTER_FIElD, GlossaryAdapter.FILTER_ALL);

        buttFilterOptions = (Button) getActivity().findViewById(R.id.butt_filter_options);
        buttFilterOptions.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 String ff = mPrefs.getString(COOKIE_FILTER_FIElD, GlossaryAdapter.FILTER_ALL);
                 FragmentManager fm = getActivity().getSupportFragmentManager();
                 FilterOptionsDialogFragment optsDialog = FilterOptionsDialogFragment.newInstance(ff);
                 optsDialog.setTargetFragment(GlossaryFragment.this, 300);
                 optsDialog.show(fm, "fragment_edit_name");
             }
        });
        setButtFilterLabel(filter_field);


        // Setup Clear Button
        buttClear = (Button) getActivity().findViewById(R.id.butt_clear);
        buttClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                txtFilter.setText("");
            }
        });

        // Setup Filter Text
        txtFilter = (EditText) getActivity().findViewById(R.id.txt_filter);
        txtFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Auto-generated method stub
             }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.i("ontext", String.valueOf(s));
                if (s.length() == 0) {
                    buttClear.setVisibility(View.INVISIBLE);
                } else {
                    buttClear.setVisibility(View.VISIBLE);
                }

                mAdapter.getFilter().filter(s.toString());
            }
        });



        // Setup listview, adapter and filter
        mAdapter = new GlossaryAdapter(getActivity(), new ArrayList<GEntry>());
        mAdapter.setFilterField(filter_field);

        ListView mListView = (ListView) getActivity().findViewById(R.id.glossary_listview);
        mListView.setAdapter(mAdapter);

        // Parse and load json
        String json_str = null;
        try {

            InputStream inp_stream = getContext().getAssets().open("yoga-glossary.json");
            int size = inp_stream.available();
            byte[] buffer = new byte[size];
            inp_stream.read(buffer);
            inp_stream.close();
            json_str = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        // Decode JSON
        JSONArray m_glossary = null;
        try {
            m_glossary = new JSONArray(json_str);

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Load to memory
        JSONObject je;
        for (int i = 0; i < m_glossary.length(); i++) {
            try {
                je = m_glossary.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            GEntry ent = new GEntry() ;
            ent.term = je.optString("term");
            ent.search = ent.term;

            JSONArray darr = je.optJSONArray("definition");
            for (int ii = 0; ii < darr.length(); ii++) {
                ent.definition.add( darr.optString(ii) );
                ent.search += darr.optString(ii).replace("'","").replace(",", "");
            }
            mAdapter.entries.add(ent);
        }
    }

    public void setButtFilterLabel(String filter_field){
        switch(filter_field){
            case GlossaryAdapter.FILTER_TERM:
                buttFilterOptions.setText("Terms");
                break;
            case GlossaryAdapter.FILTER_DEFINITION:
                buttFilterOptions.setText("Defintions");
                break;
            case GlossaryAdapter.FILTER_ALL:
            default:
                buttFilterOptions.setText("Everything");
                break;
        }
    }

    // This is called when the dialog is completed and the results have been passed
    @Override
    public void onFinishEditDialog(String filter_field) {

        // save filter_files option
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(COOKIE_FILTER_FIElD, filter_field);
        editor.commit();

        setButtFilterLabel(filter_field);
        mAdapter.setFilterField(filter_field);
        mAdapter.getFilter().filter(txtFilter.getText().toString());

    }
}
