/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.playwright;

import static org.junitpioneer.playwright.PlaywrightUtils.PLAYWRIGHT_NAMESPACE;
import static org.junitpioneer.playwright.PlaywrightUtils.isPlaywrightExtensionActive;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junitpioneer.playwright.PlaywrightTest.BrowserName;

public class BrowserTypeParameterResolver implements ParameterResolver {

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return isPlaywrightExtensionActive(extensionContext)
				&& parameterContext.getParameter().getType() == BrowserType.class;
	}

	@Override
	public BrowserType resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		// @formatter:off
		return extensionContext
			.getStore(PLAYWRIGHT_NAMESPACE)
			.getOrComputeIfAbsent(
				"browserType",
				__ -> createBrowserType(parameterContext, extensionContext),
				BrowserType.class);
		// @formatter:on
	}

	private BrowserType createBrowserType(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Playwright playwright = new PlaywrightParameterResolver().resolveParameter(parameterContext, extensionContext);
		return AnnotationUtils
				.findAnnotation(extensionContext.getElement(), PlaywrightTest.class)
				.map(configuration -> createBrowserType(playwright, configuration.browserType()))
				.orElseGet(playwright::firefox);
	}

	private BrowserType createBrowserType(Playwright playwright, BrowserName type) {
		switch (type) {
			case CHROMIUM:
				return playwright.chromium();
			case FIREFOX:
				return playwright.firefox();
			case WEBKIT:
				return playwright.webkit();
			default:
				throw new IllegalArgumentException("Unknown browser type: " + type);
		}
	}

}
