package com.project.expressfood.ui.client.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.expressfood.ExpressFoodApp
import com.project.expressfood.databinding.FragmentReportBinding
import kotlinx.coroutines.launch

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels {
        val app = requireActivity().application as ExpressFoodApp
        val clientId = app.container.authRepository.currentUser?.uid ?: ""
        ReportViewModelFactory(app.container.orderRepository, clientId)
    }

    private lateinit var adapter: ReportAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(com.project.expressfood.R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        setupRecyclerView()
        observeReport()
        observeMonthlyTotal()
    }

    private fun setupRecyclerView() {
        adapter = ReportAdapter(
            onDayClick      = { dateLabel -> viewModel.toggleDay(dateLabel) },
            formatCurrency  = { amount -> viewModel.formatCurrency(amount) },
        )
        binding.rvReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReport.adapter = adapter
    }

    private fun observeReport() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reportItems.collect { items ->
                    adapter.submitList(items)
                    val isEmpty = items.isEmpty()
                    binding.rvReport.visibility  = if (isEmpty) View.GONE else View.VISIBLE
                    binding.tvEmpty.visibility   = if (isEmpty) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun observeMonthlyTotal() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.monthlyTotal.collect { total ->
                    binding.tvMonthlyTotal.text = viewModel.formatCurrency(total)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
