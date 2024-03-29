package org.freyja.annotation;

import org.freyja.cache.access.AccessType;


public enum CacheConcurrencyStrategy {
	NONE(null), READ_ONLY(AccessType.READ_ONLY), NONSTRICT_READ_WRITE(
			AccessType.NONSTRICT_READ_WRITE), READ_WRITE(AccessType.READ_WRITE), TRANSACTIONAL(
			AccessType.TRANSACTIONAL);

	private final AccessType accessType;

	private CacheConcurrencyStrategy(AccessType accessType) {
		this.accessType = accessType;
	}

	public static CacheConcurrencyStrategy fromAccessType(AccessType accessType) {
		final String name = accessType == null ? null : accessType.getName();
		if (AccessType.READ_ONLY.getName().equals(name)) {
			return READ_ONLY;
		} else if (AccessType.READ_WRITE.getName().equals(name)) {
			return READ_WRITE;
		} else if (AccessType.NONSTRICT_READ_WRITE.getName().equals(name)) {
			return NONSTRICT_READ_WRITE;
		} else if (AccessType.TRANSACTIONAL.getName().equals(name)) {
			return TRANSACTIONAL;
		} else {
			return NONE;
		}
	}

	public static CacheConcurrencyStrategy parse(String name) {
		if (READ_ONLY.accessType.getName().equalsIgnoreCase(name)) {
			return READ_ONLY;
		} else if (READ_WRITE.accessType.getName().equalsIgnoreCase(name)) {
			return READ_WRITE;
		} else if (NONSTRICT_READ_WRITE.accessType.getName().equalsIgnoreCase(
				name)) {
			return NONSTRICT_READ_WRITE;
		} else if (TRANSACTIONAL.accessType.getName().equalsIgnoreCase(name)) {
			return TRANSACTIONAL;
		} else if ("none".equalsIgnoreCase(name)) {
			return NONE;
		} else {
			return null;
		}
	}

	public AccessType toAccessType() {
		return accessType;
	}
}
