package com.tonapps.tonkeeper.ui.screen.settings.currency

import android.os.Bundle
import android.view.View
import com.tonapps.tonkeeper.ui.base.BaseWalletScreen
import com.tonapps.tonkeeper.ui.base.ScreenContext
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.Adapter
import com.tonapps.tonkeeper.ui.screen.settings.currency.list.Item
import com.tonapps.tonkeeper.worker.WidgetUpdaterWorker
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.wallet.api.entity.CurrencyEntity
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.viewModel
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.smartScrollTo
import uikit.extensions.topScrolled
import uikit.widget.HeaderView
import uikit.widget.SearchInput
import uikit.widget.SimpleRecyclerView

class CurrencyScreen: BaseWalletScreen<ScreenContext.None>(R.layout.fragment_currency, ScreenContext.None), BaseFragment.SwipeBack {

	override val fragmentName: String = "CurrencyScreen"

	override val viewModel: CurrencyViewModel by viewModel()

	private val adapter = Adapter(::selectCurrency)
	private lateinit var listView: SimpleRecyclerView
	private var initialScrollDone = false

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val header = view.findViewById<HeaderView>(R.id.header)
		header.title = getString(Localization.primary_currency)
		header.setIcon(UIKitIcon.ic_chevron_left_16)
		header.doOnCloseClick = { finish() }

		val search = view.findViewById<SearchInput>(R.id.search)
		search.text = viewModel.searchQuery.value
		search.doOnTextChanged = { viewModel.searchQuery.value = it?.toString()?.trim().orEmpty() }

		listView = view.findViewById(R.id.list)
		listView.adapter = adapter
		listView.applyNavBottomPadding(listView.paddingBottom)
		collectFlow(listView.topScrolled, header::setDivider)
		val empty = view.findViewById<View>(R.id.empty)
		collectFlow(viewModel.uiItemsFlow) { items ->
			empty.visibility = if (items.isEmpty() && viewModel.searchQuery.value.isNotEmpty()) View.VISIBLE else View.GONE
			setUiItems(items)
		}
	}

	private fun setUiItems(items: List<Item>) {
		adapter.submitList(items) {
			if (!initialScrollDone && viewModel.searchQuery.value.isEmpty() && items.size > 1) {
				val index = adapter.selectedIndex
				if (index >= 0) {
					listView.smartScrollTo(index)
					initialScrollDone = true
				}
			}
		}
	}

	private fun selectCurrency(currency: CurrencyEntity) {
		viewModel.selectCurrency(currency)
		WidgetUpdaterWorker.update(requireContext())
		finish()
	}

	companion object {
		fun newInstance() = CurrencyScreen()
	}
}
