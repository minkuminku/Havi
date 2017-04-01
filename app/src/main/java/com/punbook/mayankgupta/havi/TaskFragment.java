package com.punbook.mayankgupta.havi;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.punbook.mayankgupta.havi.dummy.Status;
import com.punbook.mayankgupta.havi.dummy.Task;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TaskFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {

    private static final String SEPERATOR = "/";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    private static int counter = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;

    private OnFragmentInteractionListener mListener;

    public TaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskFragment newInstance(String param1, String param2, String param3, String param4) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
            mParam4 = getArguments().getString(ARG_PARAM4);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       // View view = inflater.inflate(R.layout.details2, parent, false);

        final View view = inflater.inflate(R.layout.fragment_task, container, false);

        TextView textView = (TextView) view.findViewById(R.id.task_status);
        textView.setText(mParam1);

        TextView task_summary = (TextView) view.findViewById(R.id.task_summary);
        task_summary.setText(mParam2);

        TextView task_comments = (TextView) view.findViewById(R.id.task_comments);
        task_comments.setText(mParam4);

        final Button submitButton = (Button) view.findViewById(R.id.task_submit_button);

        final EditText editText = (EditText) view.findViewById(R.id.task_comments);

if(Status.parse(mParam1)==Status.ACTIVE){
    submitButton.setEnabled(true);
    //editText.setEnabled(true);

}else {
    submitButton.setEnabled(false);
    //editText.setEnabled(false);
}


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView1 =(TextView) view.findViewById(R.id.task_comments);
                String comments =  textView1.getText().toString();
                Toast.makeText(getContext(),"CLICKED",Toast.LENGTH_SHORT).show();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(SEPERATOR + mParam3 + SEPERATOR + Task.STATUS_KEY, Status.SUBMITTED);
                childUpdates.put(SEPERATOR + mParam3 + SEPERATOR + Task.COMMENTS_KEY, comments);
               //TODO : add correct path for updating db
                // FirebaseDatabase.getInstance().getReference().child("users/user1").updateChildren(childUpdates);
                submitButton.setEnabled(false);

            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}