package com.beone.api.benefit.input;

import java.util.Objects;
import java.util.Optional;

import com.beone.api.benefit.card.CardId;

/**
 * Pre-payment selection setting. In {@code AUTO_RECOMMEND} the representative card only breaks
 * ties among cards with equal benefits (D-12 step 3).
 */
public record SelectionPreference(SelectionMode mode, Optional<CardId> representativeCard) {

	public SelectionPreference {
		Objects.requireNonNull(mode, "mode");
		Objects.requireNonNull(representativeCard, "representativeCard");
		if (mode == SelectionMode.FIXED_REPRESENTATIVE && representativeCard.isEmpty()) {
			throw new IllegalArgumentException("fixed representative mode needs a representative card");
		}
	}

	public static SelectionPreference autoRecommend() {
		return new SelectionPreference(SelectionMode.AUTO_RECOMMEND, Optional.empty());
	}

}
