package com.project.expressfood.ui.client.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.project.expressfood.ExpressFoodApp
import com.project.expressfood.databinding.FragmentOrdersBinding

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrdersViewModel by viewModels {
        val app = requireActivity().application as ExpressFoodApp
        val clientId = app.container.authRepository.currentUser?.uid ?: ""
        OrdersViewModelFactory(
            app.container.orderRepository,
            app.container.productRepository,
            clientId
        )
    }

    private lateinit var adapter: OrdersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OrdersAdapter()
        binding.rvOrders.adapter = adapter
        binding.rvOrders.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        viewModel.ordersState.observe(viewLifecycleOwner) { uiOrders ->
            adapter.submitList(uiOrders)
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
