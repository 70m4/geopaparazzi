package eu.geopaparazzi.library.forms;

import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import eu.geopaparazzi.library.R;

public class FragmentList extends android.support.v4.app.ListFragment {
    private List<String> fragmentTitles;
    private String selectedItemName;
    private FormActivity activity;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        activity = (FormActivity) getActivity();
        fragmentTitles = activity.getFragmentTitles();
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.fragment_list_item,
                fragmentTitles.toArray(new String[0]));
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick( ListView l, View v, int position, long id ) {
        selectedItemName = (String) getListAdapter().getItem(position);
        FragmentDetail oldFragment = (FragmentDetail) getFragmentManager().findFragmentById(R.id.detailFragment);
        if (oldFragment != null ){//&& oldFragment.isInLayout()) {
            FragmentDetail newFragment = new FragmentDetail();
            newFragment.setForm(selectedItemName, activity.getSectionObject());
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.remove(oldFragment);
            transaction.add(R.id.detailFragment, newFragment);
            // transaction.replace(R.id.detailFragment, newFragment);
            // transaction.addToBackStack(null); // Ads FirstFragment to the back-stack
            transaction.commit();
        } else {
            String sectionName = activity.getSectionName();
            Intent intent = new Intent(getActivity().getApplicationContext(), FragmentDetailActivity.class);
            intent.putExtra(FormUtilities.ATTR_FORMNAME, selectedItemName);
            intent.putExtra(FormUtilities.ATTR_SECTIONNAME, sectionName);
            startActivity(intent);
        }
    }

    public String getSelectedItemName() {
        return selectedItemName;
    }

    public JSONObject getSectionObject() {
        return activity.getSectionObject();
    }

}