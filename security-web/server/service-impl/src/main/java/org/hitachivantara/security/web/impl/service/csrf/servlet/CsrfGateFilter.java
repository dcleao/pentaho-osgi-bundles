/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.service.csrf.servlet;

import org.hitachivantara.security.web.api.model.csrf.CsrfConfiguration;
import org.hitachivantara.security.web.impl.model.spring.CsrfRequiredRequestMatcherAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * A Servlet filter that protects protected endpoints against CSRF attacks.
 * <p>
 * This class is a convenience, thin wrapper around Spring's {@link CsrfFilter} that mostly adapts the configuration
 * format and that only calls the inner filter when CSRF protection is enabled.
 * <p>
 * If desired, Spring's {@link CsrfFilter} can be used directly, even when CSRF protection is disabled.
 * You can use the adapter {@link CsrfRequiredRequestMatcherAdapter} to adapt between the configuration formats.
 */
public class CsrfGateFilter implements Filter {

  @Nonnull
  private final CsrfFilter innerFilter;

  @Nonnull
  private final CsrfConfiguration configuration;

  public CsrfGateFilter( @Nonnull CsrfConfiguration configuration ) {
    this( configuration, new HttpSessionCsrfTokenRepository() );
  }

  public CsrfGateFilter( @Nonnull CsrfConfiguration configuration, @Nonnull CsrfTokenRepository csrfTokenRepository ) {
    Objects.requireNonNull( configuration );
    Objects.requireNonNull( csrfTokenRepository );

    this.configuration = configuration;
    this.innerFilter = new CsrfFilter( csrfTokenRepository );
    this.innerFilter.setRequireCsrfProtectionMatcher( new CsrfRequiredRequestMatcherAdapter( configuration ) );
  }

  /**
   * Specifies a {@link AccessDeniedHandler} that should be used when CSRF protection fails.
   *
   * @param accessDeniedHandler the {@link AccessDeniedHandler} to use
   */
  public void setAccessDeniedHandler( AccessDeniedHandler accessDeniedHandler ) {
    innerFilter.setAccessDeniedHandler( accessDeniedHandler );
  }

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {
    innerFilter.init( filterConfig );
  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {
    if ( configuration.isEnabled() ) {
      innerFilter.doFilter( servletRequest, servletResponse, filterChain );
    } else {
      filterChain.doFilter( servletRequest, servletResponse );
    }
  }

  @Override
  public void destroy() {
    innerFilter.destroy();
  }
}
