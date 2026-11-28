package com.tonapps.portfolio.screens.raffle

import androidx.compose.runtime.Immutable
import com.tonapps.wallet.data.raffle.entities.RaffleEntity
import java.time.OffsetDateTime

@Immutable
sealed interface RaffleScreenState {
    data object Loading : RaffleScreenState
    data object Error : RaffleScreenState

    @Immutable
    data class Content(
        val raffle: RaffleEntity,
        val kind: RaffleKind,
        val items: List<RaffleListItem>,
        val earnHeaderIndex: Int,
        val participating: Boolean,
        val showGetMore: Boolean,
        val nextPhaseBoundary: OffsetDateTime? = null,
    ) : RaffleScreenState
}

enum class RaffleKind {
    Offer, Active, Result
}
