package com.berggrentech.socialife;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater _Inflater, ViewGroup _Container, Bundle _SavedInstanceState) {
        getDialog().setTitle("Please fill out form below to register");
        View view =_Inflater.inflate(R.layout.fragment_register, _Container, false);
        final EditText etFirstName = (EditText) view.findViewById(R.id.register_firstname);
        final EditText etLastName = (EditText) view.findViewById(R.id.register_lastname);
        final EditText etEmail = (EditText) view.findViewById(R.id.register_email);
        final EditText etConfEmail = (EditText) view.findViewById(R.id.register_confirmemail);
        final EditText etPassword = (EditText) view.findViewById(R.id.register_password);
        final EditText etConfPassword = (EditText) view.findViewById(R.id.register_confirmpassword);
        final Button btnRegister = (Button) view.findViewById(R.id.register_button);
        final Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

        // when user clicks register
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get form info
                String firstname = etFirstName.getText().toString();
                String lastname = etLastName.getText().toString();
                String email = etEmail.getText().toString();
                String confEmail = etConfEmail.getText().toString();
                String password = etPassword.getText().toString();
                String confPassword = etConfPassword.getText().toString();

                if(!email.trim().equalsIgnoreCase(confEmail.trim())) {
                    // email doesn't match
                    Toast.makeText(getActivity(), "Emails doesn't match!", Toast.LENGTH_SHORT).show();
                } else if (!password.trim().equalsIgnoreCase(confPassword.trim())) {
                    // password doesn't match
                    Toast.makeText(getActivity(), "Passwords doesn't match!", Toast.LENGTH_SHORT).show();
                } else {
                    // hash pw

                    // everything is set, try to add user
                    //MainActivity.DBM.addUser(new User(firstname, lastname, email, password));

                    // check if user exist (make addUser a bool)
                    // if ok
                    RegisterFragment.this.dismiss();
                    // else
                    // email already exist (forgot password?)
                }
            }
        });

        // dismiss dialog on cancel
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterFragment.this.dismiss();
            }
        });

        return view;
    }
}
