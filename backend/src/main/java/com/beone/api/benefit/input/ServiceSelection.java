package com.beone.api.benefit.input;

import java.time.LocalDate;
import java.util.Objects;

import com.beone.api.benefit.card.ServiceId;

/**
 * 사용자가 카드에 지정한 선택형 서비스와 적용 시작일(09271 FUEL/STORE).
 */
public record ServiceSelection(ServiceId service, LocalDate effectiveFrom) {

	public ServiceSelection {
		Objects.requireNonNull(service, "service");
		Objects.requireNonNull(effectiveFrom, "effectiveFrom");
	}

}
