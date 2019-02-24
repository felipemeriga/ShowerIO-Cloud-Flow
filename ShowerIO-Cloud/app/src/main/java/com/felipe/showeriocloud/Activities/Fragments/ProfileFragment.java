package com.felipe.showeriocloud.Activities.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.felipe.showeriocloud.Aws.AuthorizationHandle;
import com.felipe.showeriocloud.Aws.CognitoIdentityPoolManager;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.FacebookInformationSeeker;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    @BindView(R.id.profileName)
    public TextView textViewName;

    @BindView(R.id.profileImage)
    public CircleImageView profileImageView;

    @BindView(R.id.linearLayoutEmail)
    public LinearLayout linearLayoutEmail;

    @BindView(R.id.linearLayoutPhone)
    public LinearLayout linearLayoutPhone;

    @BindView(R.id.emailTextView)
    public TextView emailTextView;

    @BindView(R.id.phoneTextView)
    public TextView phoneTextView;

    private OnFragmentInteractionListener mListener;



    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.profile, container, false);

        ButterKnife.bind(this, view);

        if(AuthorizationHandle.mainAuthMethod.equals(AuthorizationHandle.FEDERATED_IDENTITIES)){
            textViewName.setText(FacebookInformationSeeker.facebookName);
            Picasso.get().load(FacebookInformationSeeker.facebookProfilePhotoUrl).into(profileImageView);
            linearLayoutPhone.setVisibility(LinearLayout.GONE);
            emailTextView.setText(FacebookInformationSeeker.facebookEmail);
        } else if(AuthorizationHandle.mainAuthMethod.equals(AuthorizationHandle.COGNITO_POOL)) {
            CognitoIdentityPoolManager.getPool().getUser(CognitoIdentityPoolManager.getPool().getCurrentUser().getUserId()).getDetailsInBackground(detailsHandler);
        }
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

    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            Map<String, String> stringStringHashMap = new HashMap<>();
            CognitoUserAttributes cognitoUserAttributes = cognitoUserDetails.getAttributes();
            stringStringHashMap = cognitoUserAttributes.getAttributes();
            textViewName.setText(stringStringHashMap.get("given_name"));
            emailTextView.setText(stringStringHashMap.get("email"));
            phoneTextView.setText(stringStringHashMap.get("phone_number"));
        }

        @Override
        public void onFailure(Exception exception) {
            textViewName.setText("ShowerLite");
        }
    };
}
