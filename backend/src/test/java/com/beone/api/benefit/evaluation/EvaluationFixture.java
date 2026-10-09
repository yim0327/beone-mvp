package com.beone.api.benefit.evaluation;

import java.util.List;
import java.util.Map;

/**
 * The whole evaluation fixture mapped to the model. Descriptive fields are kept as text so every
 * fixture key is accounted for.
 */
record EvaluationFixture(String schemaVersion, String status, String reviewStatus, String purpose,
		String policyBasisCommit, Map<String, String> defaultNotes, Map<String, String> sources,
		Map<String, String> reviewRecord, List<EvaluationScenario> cases, List<EvaluationScenario> mutations) {

	EvaluationFixture {
		defaultNotes = Map.copyOf(defaultNotes);
		sources = Map.copyOf(sources);
		reviewRecord = Map.copyOf(reviewRecord);
		cases = List.copyOf(cases);
		mutations = List.copyOf(mutations);
	}

}
