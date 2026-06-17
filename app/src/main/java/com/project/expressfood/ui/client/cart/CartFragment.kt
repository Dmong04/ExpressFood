package com.project.expressfood.ui.client.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.expressfood.ExpressFoodApp
import com.project.expressfood.databinding.FragmentCartBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val currency = NumberFormat.getCurrencyInstance(Locale("es", "CR"))

    private val viewModel: CartViewModel by viewModels {
        val app      = requireActivity().application as ExpressFoodApp
        val clientId = app.container.authRepository.currentUser?.uid ?: ""
        CartViewModelFactory(
            app.container.cartRepository,
            app.container.productRepository,
            app.container.orderRepository,
            clientId,
            app.applicationContext,
        )
    }

    private lateinit var adapter: CartAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeCart()
        observeSummary()
        observeCheckout()

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCheckout.setOnClickListener {
            viewModel.checkout()
        }
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            onIncrement = { item ->
                viewLifecycleOwner.lifecycleScope.launch { viewModel.incrementItem(item) }
            },
            onDecrement = { item ->
                viewLifecycleOwner.lifecycleScope.launch { viewModel.decrementItem(item) }
            },
        )
        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = adapter
    }

    private fun observeCart() {
        viewModel.cartItemsWithProducts.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun observeSummary() {
        viewModel.summary.observe(viewLifecycleOwner) { s ->
            binding.tvSubtotal.text = currency.format(s.subtotal)
            binding.tvTax.text      = currency.format(s.tax)
            binding.tvTotal.text    = currency.format(s.total)
        }
    }

    private fun observeCheckout() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.checkoutState.collect { state ->
                when (state) {
                    is CheckoutState.Idle    -> Unit
                    is CheckoutState.Loading -> binding.btnCheckout.isEnabled = false
                    is CheckoutState.Success -> {
                        binding.btnCheckout.isEnabled = true
                        Toast.makeText(requireContext(), "¡Orden procesada con éxito!", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    is CheckoutState.Error   -> {
                        binding.btnCheckout.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}