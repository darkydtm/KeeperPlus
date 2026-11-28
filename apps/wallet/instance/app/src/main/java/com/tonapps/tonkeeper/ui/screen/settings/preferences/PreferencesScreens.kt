package com.tonapps.tonkeeper.ui.screen.settings.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.tonkeeper.ui.base.BaseListWalletScreen
import com.tonapps.tonkeeper.ui.base.ScreenContext
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.buttonPrimaryBackgroundColor
import com.tonapps.uikit.color.textPrimaryColor
import com.tonapps.uikit.color.textTertiaryColor
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.settings.SettingsRepository
import org.koin.android.ext.android.inject
import uikit.base.BaseFragment
import uikit.extensions.ClickHaptics
import uikit.extensions.dp
import uikit.extensions.drawable
import uikit.widget.item.ItemSwitchView

class PromotionsScreen : BaseListWalletScreen<ScreenContext.None>(ScreenContext.None), BaseFragment.SwipeBack {
	override val fragmentName = "PromotionsScreen"
	override val viewModel = null
	private val settings: SettingsRepository by inject()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setTitle(getString(R.string.settings_promotions))
		setAdapter(object : RecyclerView.Adapter<SwitchHolder>() {
			override fun getItemCount() = 3
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SwitchHolder(ItemSwitchView(parent.context))
			override fun onBindViewHolder(holder: SwitchHolder, position: Int) {
				val item = holder.item
				item.position = when (position) {
					0 -> ListCell.Position.FIRST
					1 -> ListCell.Position.MIDDLE
					else -> ListCell.Position.LAST
				}
				item.text = getString(when (position) {
					0 -> R.string.settings_promotions_only
					1 -> R.string.settings_rewards
					else -> R.string.settings_recommendations
				})
				item.doOnCheckedChanged = null
				item.setChecked(when (position) {
					0 -> settings.promotionsEnabled
					1 -> settings.rewardsEnabled
					else -> settings.recommendationsEnabled
				}, false)
				item.doOnCheckedChanged = { checked, byUser ->
					if (byUser) when (position) {
						0 -> settings.promotionsEnabled = checked
						1 -> settings.rewardsEnabled = checked
						else -> settings.recommendationsEnabled = checked
					}
				}
			}
		})
	}
}

private class SwitchHolder(val item: ItemSwitchView) : RecyclerView.ViewHolder(item) {
	init {
		item.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
		item.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
		for (index in 0 until item.childCount) {
			item.getChildAt(index).importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
		}
		ViewCompat.setAccessibilityDelegate(item, object : AccessibilityDelegateCompat() {
			override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
				super.onInitializeAccessibilityNodeInfo(host, info)
				info.className = android.widget.Switch::class.java.name
				info.text = item.text
				info.isCheckable = true
				info.isChecked = item.isChecked()
			}
		})
	}
}

class HapticsScreen : BaseListWalletScreen<ScreenContext.None>(ScreenContext.None), BaseFragment.SwipeBack {
	override val fragmentName = "HapticsScreen"
	override val viewModel = null

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setTitle(getString(R.string.settings_haptics))
		setAdapter(object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
			override fun getItemCount() = 2
			override fun getItemViewType(position: Int) = position
			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
				if (viewType == 0) return SwitchHolder(ItemSwitchView(parent.context))
				val layout = LinearLayout(parent.context).apply {
					layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
					orientation = LinearLayout.VERTICAL
					setPadding(16.dp, 16.dp, 16.dp, 16.dp)
					background = ListCell.Position.LAST.drawable(context)
				}
				val label = TextView(parent.context).apply {
					setTextAppearance(uikit.R.style.TextAppearance_Label1)
					setTextColor(context.textPrimaryColor)
				}
				val slider = SeekBar(parent.context).apply {
					max = 2
					progressTintList = ColorStateList.valueOf(context.buttonPrimaryBackgroundColor)
					thumbTintList = ColorStateList.valueOf(context.buttonPrimaryBackgroundColor)
				}
				val explanation = TextView(parent.context).apply {
					setTextAppearance(uikit.R.style.TextAppearance_Body2)
					setTextColor(context.textTertiaryColor)
					setText(R.string.settings_haptics_strength_unavailable)
				}
				layout.addView(label)
				layout.addView(slider)
				layout.addView(explanation)
				return SliderHolder(layout, label, slider, explanation)
			}
			override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
				if (holder is SwitchHolder) {
					holder.item.position = ListCell.Position.FIRST
					holder.item.text = getString(R.string.settings_click_haptics)
					holder.item.doOnCheckedChanged = null
					holder.item.setChecked(ClickHaptics.getEnabled(requireContext()), false)
					holder.item.doOnCheckedChanged = { checked, byUser ->
						if (byUser) {
							ClickHaptics.setEnabled(requireContext(), checked)
							notifyItemChanged(1)
						}
					}
				} else if (holder is SliderHolder) {
					val slider = holder.slider
					slider.setOnSeekBarChangeListener(null)
					slider.progress = ClickHaptics.getStrength(requireContext()) - 1
					val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
					val supportsStrength = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
						vibrator != null && vibrator.hasVibrator() && vibrator.hasAmplitudeControl()
					slider.isEnabled = supportsStrength && ClickHaptics.getEnabled(requireContext())
					holder.explanation.visibility = if (supportsStrength) View.GONE else View.VISIBLE
					slider.contentDescription = getString(R.string.settings_click_haptics_strength)
					fun updateLevel(progress: Int) {
						val level = getString(R.string.settings_haptics_level, progress + 1)
						holder.label.text = "${getString(R.string.settings_click_haptics_strength)} - $level"
						ViewCompat.setStateDescription(slider, level)
					}
					if (supportsStrength) {
						updateLevel(slider.progress)
					} else {
						holder.label.setText(R.string.settings_click_haptics_strength)
					}
					slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
						override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
							updateLevel(progress)
							if (fromUser) {
								ClickHaptics.setStrength(requireContext(), progress + 1)
								ClickHaptics.perform(seekBar)
							}
						}
						override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
						override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
					})
				}
			}
		})
	}
}

private class SliderHolder(view: View, val label: TextView, val slider: SeekBar, val explanation: TextView) : RecyclerView.ViewHolder(view)
