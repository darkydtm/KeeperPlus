package com.tonapps.tonkeeper.ui.screen.settings.main.list.holder

import android.view.ViewGroup
import com.tonapps.tonkeeper.ui.screen.settings.main.list.Item
import uikit.extensions.drawable
import uikit.widget.item.ItemTextView
import uikit.extensions.setMoonOnClickListener

class TextHolder(
    parent: ViewGroup,
    onClick: ((Item) -> Unit)
): Holder<Item.Text>(ItemTextView(parent.context), onClick) {

    private val itemTextView = itemView as ItemTextView

    override fun onBind(item: Item.Text) {
        itemTextView.background = item.position.drawable(context)
        itemTextView.setMoonOnClickListener { onClick.invoke(item) }
        itemTextView.text = getString(item.titleRes)
        itemTextView.data = when (item) {
            is Item.Promotions -> getString(if (item.enabled) com.tonapps.tonkeeperx.R.string.settings_enabled else com.tonapps.tonkeeperx.R.string.settings_disabled)
            is Item.ClickHaptics -> getString(if (item.enabled) com.tonapps.tonkeeperx.R.string.settings_enabled else com.tonapps.tonkeeperx.R.string.settings_disabled)
            else -> item.value
        }
    }
}