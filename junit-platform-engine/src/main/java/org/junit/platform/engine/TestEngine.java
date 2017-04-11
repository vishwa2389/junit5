/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.net.URL;
import java.util.Optional;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.PackageUtils;

/**
 * A {@code TestEngine} facilitates <em>discovery</em> and <em>execution</em> of
 * tests for a particular programming model.
 *
 * <p>For example, JUnit provides a {@code TestEngine} that discovers and
 * executes tests written using the JUnit Jupiter programming model.
 *
 * <p>Every {@code TestEngine} must {@linkplain #getId provide its own unique ID},
 * {@linkplain #discover discover tests} from
 * {@link EngineDiscoveryRequest EngineDiscoveryRequests},
 * and {@linkplain #execute execute those tests} according to
 * {@link ExecutionRequest ExecutionRequests}.
 *
 * <p>In order to facilitate test discovery within IDEs and tools prior
 * to launching the JUnit Platform, {@code TestEngine} implementations are
 * encouraged to make use of the
 * {@link org.junit.platform.commons.annotation.Testable @Testable} annotation.
 * For example, the {@code @Test} and {@code @TestFactory} annotations in JUnit
 * Jupiter are meta-annotated with {@code @Testable}. Consult the Javadoc for
 * {@code @Testable} for further details.
 *
 * @see org.junit.platform.engine.EngineDiscoveryRequest
 * @see org.junit.platform.engine.ExecutionRequest
 * @see org.junit.platform.commons.annotation.Testable
 * @since 1.0
 */
@API(Experimental)
public interface TestEngine {

	/**
	 * Get the ID that uniquely identifies this test engine.
	 *
	 * <p>Each test engine must provide a unique ID. For example, JUnit Vintage
	 * and JUnit Jupiter use {@code "junit-vintage"} and {@code "junit-jupiter"},
	 * respectively. When in doubt, you may use the fully qualified name of your
	 * custom {@code TestEngine} implementation class.
	 */
	String getId();

	/**
	 * Discover tests according to the supplied {@link EngineDiscoveryRequest}.
	 *
	 * <p>The supplied {@link UniqueId} must be used as the unique ID of the
	 * returned root {@link TestDescriptor}. In addition, the {@code UniqueId}
	 * must be used to create unique IDs for children of the root's descriptor
	 * by calling {@link UniqueId#append}.
	 *
	 * @param discoveryRequest the discovery request
	 * @param uniqueId the unique ID to be used for this test engine's
	 * {@code TestDescriptor}
	 * @return the root {@code TestDescriptor} of this engine, typically an
	 * instance of {@code EngineDescriptor}
	 * @see org.junit.platform.engine.support.descriptor.EngineDescriptor
	 */
	TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId);

	/**
	 * Execute tests according to the supplied {@link ExecutionRequest}.
	 *
	 * <p>The {@code request} passed to this method contains the root
	 * {@link TestDescriptor} that was previously returned by {@link #discover},
	 * the {@link EngineExecutionListener} to be notified of test execution
	 * events, and {@link ConfigurationParameters} that may influence test execution.
	 *
	 * @param request the request to execute tests for
	 */
	void execute(ExecutionRequest request);

	/**
	 * Get the <em>Group ID</em> of the JAR in which this test engine is packaged.
	 *
	 * <p>This information is used solely for debugging and reporting purposes.
	 *
	 * <p>The default implementation simply returns an empty {@link Optional},
	 * signaling that the group ID is unknown.
	 *
	 * <p>Concrete test engine implementations may override this method in
	 * order to provide a known group ID.
	 *
	 * @return an {@code Optional} containing the group ID; never {@code null}
	 * but potentially empty if the group ID is unknown
	 * @see #getArtifactId()
	 * @see #getVersion()
	 */
	default Optional<String> getGroupId() {
		return Optional.empty();
	}

	/**
	 * Get the <em>Artifact ID</em> of the JAR in which this test engine is packaged.
	 *
	 * <p>This information is used solely for debugging and reporting purposes.
	 *
	 * <p>The default implementation assumes the implementation title is equivalent
	 * to the artifact ID and therefore attempts to query the
	 * {@linkplain Package#getImplementationTitle() implementation title}
	 * from the package attributes for the {@link Package} in which the engine
	 * resides. Note that a package only has attributes if the information is
	 * defined in the {@link java.util.jar.Manifest Manifest} of the JAR
	 * containing that package, and if the class loader created the
	 * {@link Package} instance with the attributes from the manifest.
	 *
	 * <p>If the implementation title cannot be queried from the package
	 * attributes, the default implementation simply returns an empty
	 * {@link Optional}.
	 *
	 * <p>Concrete test engine implementations may override this method in
	 * order to determine the artifact ID by some other means.
	 *
	 * @return an {@code Optional} containing the artifact ID; never
	 * {@code null} but potentially empty if the artifact ID is unknown
	 * @see Class#getPackage()
	 * @see Package#getImplementationTitle()
	 * @see #getGroupId()
	 * @see #getVersion()
	 */
	default Optional<String> getArtifactId() {
		return PackageUtils.getAttribute(getClass(), Package::getImplementationTitle);
	}

	/**
	 * Get the version of this test engine.
	 *
	 * <p>This information is used solely for debugging and reporting purposes.
	 *
	 * <p>Initially, the default implementation tries to retrieve the engine
	 * version from the manifest attribute named: {@code "Engine-Version-" + getId()}
	 *
	 * <p>Then the default implementation attempts to query the
	 * {@linkplain Package#getImplementationVersion() implementation version}
	 * from the package attributes for the {@link Package} in which the engine
	 * resides. Note that a package only has attributes if the information is
	 * defined in the {@link java.util.jar.Manifest Manifest} of the JAR
	 * containing that package, and if the class loader created the
	 * {@link Package} instance with the attributes from the manifest.
	 *
	 * <p>If the implementation version cannot be queried from the package
	 * attributes, the default implementation returns {@code "DEVELOPMENT"}.
	 *
	 * <p>Concrete test engine implementations may override this method to
	 * determine the version by some other means.
	 *
	 * @return an {@code Optional} containing the version; never {@code null}
	 * but potentially empty if the version is unknown
	 * @see Class#getPackage()
	 * @see Package#getImplementationVersion()
	 * @see #getGroupId()
	 * @see #getArtifactId()
	 */
	default Optional<String> getVersion() {
		Optional<String> standalone = PackageUtils.getAttribute(getClass(), "Engine-Version-" + getId());
		if (standalone.isPresent()) {
			return standalone;
		}
		return Optional.of(
			PackageUtils.getAttribute(getClass(), Package::getImplementationVersion).orElse("DEVELOPMENT"));
	}

	/**
	 * Get the location from which this engine was loaded.
	 *
	 * <p>The default implementation queries the {@linkplain ClassLoader} of the
	 * engine class to retrieve the location URL.
	 *
	 * @return an {@code Optional} containing the location; never {@code null}
	 * but potentially empty if the location is unknown
	 */
	default Optional<URL> getSourceLocation() {
		return ClassLoaderUtils.getLocation(this);
	}
}
