package com.example.recipe_app.view.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.recipe_app.R
import com.example.recipe_app.databinding.FragmentLoginBinding
import com.example.recipe_app.model.PersonInfo
import com.example.recipe_app.utils.GreenSnackBar
import com.example.recipe_app.viewModels.AuthViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var loginFragmentBinding: FragmentLoginBinding? = null
    private val viewModel: AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding=FragmentLoginBinding.bind(view)
        loginFragmentBinding=binding

        val etEmailLogin = binding.editTextEmail
        val etPasswordLogin = binding.etPassword
        val btnlogin = binding.btnCreateAccount
        val etTxtLayoutEmail = binding.editTextLayoutEmail
        val etTxtLayoutPassword = binding.etLayoutPassword

        btnlogin.setOnClickListener {

            val email = etEmailLogin.text.toString()
            val password = etPasswordLogin.text.toString()

            val flagEmail=when(email.isEmpty()){
                true -> {
                    etTxtLayoutEmail.error = "Email is required"
                    false
                }
                false -> {
                    etTxtLayoutEmail.error = null
                    true
                }
            }
            val flagPass=when(password.isEmpty()){
                true -> {
                    etTxtLayoutPassword.error = "password is required"
                    false
                }
                false -> {
                    etTxtLayoutPassword.error = null
                    true
                }
            }
            val validEmail = when((email.contains("@") && email.contains(".")) ){
                true -> {
                    etTxtLayoutEmail.error = null
                    true
                }
                false -> {
                    if(flagEmail){
                        etTxtLayoutEmail.error = "Email is not valid"
                        false
                    }
                    else{
                        true
                    }
                }
            }
            if(flagEmail && flagPass && validEmail){
                val currentuser= PersonInfo(0, email, password)
                viewModel.getUserByEmail(email)
                viewModel.user.observe(viewLifecycleOwner){result->
                    if(result!=null){
                        if(result.password == currentuser.password){
                            Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show()
                            val pref=requireActivity().getSharedPreferences("mypref",0)
                            val editor=pref.edit()
                            editor.putBoolean("isloggedin",true)
                            editor.putString("CurrentUserMail", email)
                            editor.apply()
                            editor.commit()
                            findNavController().navigate(R.id.homeActivity)
                            activity?.finish()


                        }
                        else{
                            GreenSnackBar.showSnackBarLong(view, "Incorrect Password")
                        }
                    }
                    else{
                        GreenSnackBar.showSnackBarLong(view, "User not found please create an account")
                    }
                }

            }

        }
        binding.textViewCreateAccount.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loginFragmentBinding=null
    }


}

