package com.beone.api.benefit.evaluation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.beone.api.benefit.input.RecommendationInput;
import com.beone.api.benefit.rule.CardRuleVersion;

/**
 * One fixture case (C01-C20) or mutation (M01-M10) mapped to the model.
 *
 * @param baseCaseId base case of a mutation; empty for a case
 * @param ruleSnapshots evaluation-only rule snapshots the fixture states (C02); other cases
 * assume a rule valid on the order date, which the fixture does not spell out
 * @param description case focus or mutation dimension
 * @param sourceKeys official source keys the case cites
 */
record EvaluationScenario(String id, Optional<String> baseCaseId, String description, List<String> sourceKeys,
		RecommendationInput input, List<CardRuleVersion> ruleSnapshots, ExpectedOutcome expected) {

	EvaluationScenario {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(baseCaseId, "baseCaseId");
		Objects.requireNonNull(description, "description");
		sourceKeys = List.copyOf(sourceKeys);
		Objects.requireNonNull(input, "input");
		ruleSnapshots = List.copyOf(ruleSnapshots);
		Objects.requireNonNull(expected, "expected");
	}

}
