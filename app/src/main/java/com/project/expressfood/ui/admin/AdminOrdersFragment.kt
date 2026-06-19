package com.project.expressfood.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.expressfood.ExpressFoodApp
import com.project.expressfood.databinding.FragmentAdminOrdersBinding
import com.project.expressfood.domain.model.OrderStatus
import kotlinx.coroutines.launch

class AdminOrdersFragment : Fragment() {

    private var _binding: FragmentAdminOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminOrdersViewModel by viewModels {
        val container = (requireActivity().application as ExpressFoodApp).container
        AdminOrdersViewModel.Factory(container.orderRepository, container.userFirestoreService)
    }

    private lateinit var adapter: AdminOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = AdminOrdersAdapter { orderWithClient ->
            showStatusSelectionDialog(orderWithClient)
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.ordersState.collect { orders ->
                        adapter.submitList(orders)
                        binding.tvEmpty.isVisible = orders.isEmpty()
                    }
                }
                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.progressBar.isVisible = loading
                    }
                }
            }
        }
    }

    private fun showStatusSelectionDialog(item: OrderWithClient) {
        val statuses = OrderStatus.entries.toTypedArray()
        val statusNames = statuses.map { translateStatus(it) }.toTypedArray()
        val currentIndex = statuses.indexOf(item.order.status)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Actualizar Estado - #${item.order.orderId.takeLast(6).uppercase()}")
            .setSingleChoiceItems(statusNames, currentIndex) { dialog, which ->
                val selectedStatus = statuses[which]
                viewModel.updateOrderStatus(item.order.orderId, selectedStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun translateStatus(status: OrderStatus): String {
        return when (status) {
            OrderStatus.PENDING -> "Pendiente"
            OrderStatus.PREPARING -> "Preparando"
            OrderStatus.READY -> "Listo"
            OrderStatus.DELIVERED -> "Entregado"
            OrderStatus.CANCELLED -> "Cancelado"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
