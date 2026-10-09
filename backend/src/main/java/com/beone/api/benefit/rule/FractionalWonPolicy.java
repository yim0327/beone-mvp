package com.beone.api.benefit.rule;

/**
 * 계산된 혜택의 원 미만 금액 처리 방식.
 * 아직 어느 카드도 확인되지 않았으므로 공식 근거가 나올 때까지 미확정 조건으로 둔다(카드 조사 문서, D-03).
 */
public enum FractionalWonPolicy {

	/** 원 미만 절사. */
	TRUNCATE,

	/** 원 미만 반올림. */
	ROUND_HALF_UP

}
