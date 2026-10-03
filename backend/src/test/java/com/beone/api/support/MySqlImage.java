package com.beone.api.support;

/**
 * MySQL image used by integration tests. Must match the {@code mysql} service image in the
 * root {@code docker-compose.yml}; {@code MySqlVersionAlignmentTest} enforces this.
 */
public final class MySqlImage {

	public static final String TAG = "mysql:8.4.11";

	private MySqlImage() {
	}

}
