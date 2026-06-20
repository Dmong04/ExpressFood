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
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.expressfood.R
import com.project.expressfood.ExpressFoodApp
import com.project.expressfood.databinding.FragmentAdminDashboardBinding
import com.project.expressfood.domain.model.Order
import com.project.expressfood.domain.model.OrderStatus
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminDashboardViewModel by viewModels {
        val container = (requireActivity().application as ExpressFoodApp).container
        AdminDashboardViewModel.Factory(container.orderRepository)
    }

    private lateinit var adapter: AdminDashboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupNavigation()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = AdminDashboardAdapter { order -> showStatusDialog(order) }
        binding.rvUpcomingOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingOrders.adapter = adapter
    }

    private fun setupNavigation() {
        binding.cardManageOrders.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_adminOrders)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    binding.scrollView.isVisible = !state.isLoading

                    val fmt = NumberFormat.getNumberInstance(Locale("es", "CR")).apply {
                        minimumFractionDigits = 0
                        maximumFractionDigits = 0
                    }
                    binding.tvTodayRevenue.text = "₡${fmt.format(state.todayRevenue)}"
                    binding.tvTotalRevenue.text = "₡${fmt.format(state.totalRevenue)}"
                    binding.tvPendingCount.text = state.pendingCount.toString()
                    binding.tvPreparingCount.text = state.preparingCount.toString()
                    binding.tvReadyCount.text = state.readyCount.toString()

                    adapter.submitList(state.upcomingOrders)
                    binding.tvEmpty.isVisible =
                        state.upcomingOrders.isEmpty() && !state.isLoading
                }
            }
        }
    }

    private fun showStatusDialog(order: Order) {
        val statuses = OrderStatus.entries.toTypedArray()
        val names = statuses.map { translateStatus(it) }.toTypedArray()
        val currentIndex = statuses.indexOf(order.status)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pedido #${order.orderId.takeLast(6).uppercase()}")
            .setSingleChoiceItems(names, currentIndex) { dialog, which ->
                viewModel.updateOrderStatus(order.orderId, statuses[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun translateStatus(status: OrderStatus): String = when (status) {
        OrderStatus.PENDING -> "Pendiente"
        OrderStatus.PREPARING -> "Preparando"
        OrderStatus.READY -> "Listo"
        OrderStatus.DELIVERED -> "Entregado"
        OrderStatus.CANCELLED -> "Cancelado"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
