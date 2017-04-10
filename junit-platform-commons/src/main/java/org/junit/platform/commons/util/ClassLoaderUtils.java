/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.net.URL;
import java.util.Optional;

import org.junit.platform.commons.meta.API;

/**
 * Collection of utilities for working with {@linkplain ClassLoader} and associated tasks.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
 */
@API(Internal)
public final class ClassLoaderUtils {

	///CLOVER:OFF
	private ClassLoaderUtils() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Get the location from where this objects underlying class was loaded from.
	 *
	 * @param object to find the location its class was loaded from for
	 * @return an {@code Optional} containing the URL of the class' location; never
	 * {@code null} but potentially empty
	 */
	public static Optional<URL> getLocation(Object object) {
		Preconditions.notNull(object, "object must not be null");
		ClassLoader loader = object.getClass().getClassLoader();
		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
			while (loader != null && loader.getParent() != null) {
				loader = loader.getParent();
			}
		}
		if (loader != null) {
			String name = object.getClass().getCanonicalName();
			name = name.replace(".", "/") + ".class";
			URL resource = loader.getResource(name);
			return Optional.ofNullable(resource);
		}
		return Optional.empty();
	}
}
