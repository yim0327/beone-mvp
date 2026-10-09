package com.beone.api.benefit.evaluation;

import java.util.Objects;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.card.ServiceId;

/**
 * A fixture key such as {@code 09271:FUEL}: card and service of that card.
 */
record ServiceRef(CardId cardId, ServiceId serviceId) {

	ServiceRef {
		Objects.requireNonNull(cardId, "cardId");
		Objects.requireNonNull(serviceId, "serviceId");
	}

	static ServiceRef parse(String key) {
		String[] parts = CardScopedKey.split(key);
		return new ServiceRef(CardId.of(parts[0]), ServiceId.of(parts[1]));
	}

}
