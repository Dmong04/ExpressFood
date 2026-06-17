package com.project.expressfood.ui.client.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.project.expressfood.ExpressFoodApp
import com.project.expressfood.databinding.FragmentClientHomeBinding
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.project.expressfood.R

class ClientHomeFragment : Fragment() {

    private var _binding: FragmentClientHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by viewModels {
        val app = requireActivity().application as ExpressFoodApp
        val clientId = app.container.authRepository.currentUser?.uid ?: ""
        MenuViewModelFactory(
            app.container.productRepository,
            app.container.cartRepository,
            clientId,
            app.container.networkMonitor,
        )
    }

    private lateinit var adapter: ProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeProducts()
        observeCartEvents()
        observeNetworkStatus()
        viewModel.syncMenu()

        binding.fabCart.setOnClickListener {
            findNavController().navigate(R.id.action_clientHomeFragment_to_cartFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product -> viewModel.addToCart(product) }
        binding.rvMenu.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMenu.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.onSearch(text?.toString() ?: "")
        }
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { adapter.submitList(it) }
            }
        }
    }

    private fun observeCartEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartEvent.collect { message ->
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeNetworkStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isOnline.collect { online ->
                    binding.layoutOfflineBanner.visibility = if (online) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}