package com.vaadin.starter.bakery.app.security;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.context.annotation.ApplicationScope;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.starter.bakery.app.Application;

/**
 * Redirects to the application after successful authentication.
 */
@NullMarked
@SpringComponent
@ApplicationScope
public class RedirectAuthenticationSuccessHandler
		implements AuthenticationSuccessHandler {

	private final String location;

	private ServletContext servletContext;

	@Autowired
	public RedirectAuthenticationSuccessHandler(ServletContext servletContext) {
		this.servletContext = servletContext;
		location = Application.APP_URL;
	}

	private String getAbsoluteUrl(String url) {
		final String relativeUrl;
		if (url.startsWith("/")) {
			relativeUrl = url.substring(1);
		} else {
			relativeUrl = url;
		}
		return String.format("%s/%s", servletContext.getContextPath(),
				relativeUrl);
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		response.sendRedirect(getAbsoluteUrl(location));
	}

}
