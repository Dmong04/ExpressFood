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
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.snackbar.Snackbar
import com.project.expressfood.ExpressFoodApp
import com.project.expressfood.R
import com.project.expressfood.databinding.FragmentClientHomeBinding
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

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
    private var cartBadge: BadgeDrawable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupCartFab()
        setupReportsButton()
        observeProducts()
        observeCartEvents()
        observeNetworkStatus()
        observeCartBadge()
        viewModel.syncMenu()
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

    @androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
    private fun setupCartFab() {
        cartBadge = BadgeDrawable.create(requireContext()).apply {
            badgeGravity = BadgeDrawable.TOP_END
            isVisible = false
        }
        binding.fabCart.post {
            BadgeUtils.attachBadgeDrawable(cartBadge!!, binding.fabCart)
        }
        binding.fabCart.setOnClickListener {
            findNavController().navigate(R.id.action_clientHomeFragment_to_cartFragment)
        }
    }

    private fun setupReportsButton() {
        binding.toolbar.inflateMenu(R.menu.menu_client_home)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_reports) {
                findNavController().navigate(R.id.action_clientHomeFragment_to_reportFragment)
                true
            } else false
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

    private fun observeCartBadge() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartItemCount.collect { count ->
                    cartBadge?.apply {
                        isVisible = count > 0
                        if (count > 0) number = count
                    }
                }
            }
        }
    }

    @androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
    override fun onDestroyView() {
        super.onDestroyView()
        cartBadge?.let { BadgeUtils.detachBadgeDrawable(it, binding.fabCart) }
        cartBadge = null
        _binding = null
    }
}
