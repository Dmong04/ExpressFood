package com.project.expressfood.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.project.expressfood.ExpressFoodApp
import com.project.expressfood.R
import com.project.expressfood.databinding.FragmentLoginBinding
import com.project.expressfood.domain.model.UserRole
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        val appContainer = (requireActivity().application as ExpressFoodApp).container
        AuthViewModel.Factory(appContainer.authRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnGoogleSignIn.setOnClickListener {
            viewModel.signInWithGoogle(requireContext())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.authState.collect { state ->
                        handleAuthState(state)
                    }
                }

                launch {
                    viewModel.isSigningIn.collect { isLoading ->
                        binding.loadingIndicator.isVisible = isLoading
                        binding.btnGoogleSignIn.isEnabled = !isLoading
                    }
                }

                launch {
                    viewModel.loginError.collect { error ->
                        binding.tvError.text = error
                        binding.tvError.isVisible = error != null
                    }
                }
            }
        }
    }

    private fun handleAuthState(state: AuthState) {
        when (state) {
            is AuthState.Authenticated -> {
                navigateToHome(state.user.role)
            }
            is AuthState.Error -> {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
            }
            AuthState.Loading -> {
                binding.loadingIndicator.isVisible = true
            }
            AuthState.Unauthenticated -> {
                binding.loadingIndicator.isVisible = false
            }
        }
    }

    private fun navigateToHome(role: UserRole) {
        val destination = if (role == UserRole.ADMIN) {
            R.id.adminDashboardFragment
        } else {
            R.id.clientHomeFragment
        }

        findNavController().navigate(destination, null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
