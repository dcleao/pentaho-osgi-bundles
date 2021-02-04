/*!
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
 * Copyright (c) 2019-2021 Hitachi Vantara. All rights reserved.
 */
package org.hitachivantara.web.security.api.csrf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hitachivantara.web.security.api.RequestMatcherConfiguration;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CsrfGateFilter implements Filter {

  private static final Log logger = LogFactory.getLog( CsrfGateFilter.class );

  private final CsrfConfigurationProvider csrfConfigProvider;
  private final CsrfFilter innerFilter;
  private boolean isEnabled;
  private boolean isInitialized;

  public CsrfGateFilter( CsrfTokenRepository csrfTokenRepository, CsrfConfigurationProvider csrfConfigProvider ) {

    Objects.requireNonNull( csrfConfigProvider );

    this.innerFilter = new CsrfFilter( csrfTokenRepository );
    this.csrfConfigProvider = csrfConfigProvider;
    this.isEnabled = true;
    this.isInitialized = false;
  }

  // region Testing support
  CsrfFilter getInnerFilter() {
    return innerFilter;
  }

  boolean isEnabled() {
    return isEnabled;
  }

  boolean isInitialized() {
    return isInitialized;
  }

  void setIsInitialized( boolean isInitialized ) {
    this.isInitialized = isInitialized;
  }
  // endregion

  /**
   * Specifies an {@link AccessDeniedHandler} to be used when CSRF protection fails.
   *
   * @param accessDeniedHandler The {@link AccessDeniedHandler}.
   */
  public void setAccessDeniedHandler( AccessDeniedHandler accessDeniedHandler ) {
    innerFilter.setAccessDeniedHandler( accessDeniedHandler );
  }

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {

    innerFilter.init( filterConfig );

    csrfConfigProvider.addCsrfConfigurationListener( () -> {
      CsrfGateFilter.this.isInitialized = false;
      CsrfGateFilter.this.doInit();
    } );

    doInit();
  }

  private synchronized void doInit() {
    if ( logger.isDebugEnabled() ) {
      logger.debug( "CsrfGateFilter.doInit" );
    }

    if ( isInitialized ) {
      return;
    }

    RequestMatcher requestMatcher = buildCsrfRequestMatcher( csrfConfigProvider.getCsrfConfiguration() );
    isEnabled = requestMatcher != null;
    if ( isEnabled ) {
      innerFilter.setRequireCsrfProtectionMatcher( requestMatcher );
    }

    isInitialized = true;
  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {

    if ( !isInitialized ) {
      doInit();
    }

    if ( isEnabled ) {
      innerFilter.doFilter( servletRequest, servletResponse, filterChain );
    } else {
      filterChain.doFilter( servletRequest, servletResponse );
    }
  }

  @Override
  public void destroy() {
    innerFilter.destroy();
  }

  // region Util
  static RequestMatcher buildCsrfRequestMatcher( CsrfConfiguration csrfConfiguration ) {

    if ( !csrfConfiguration.isEnabled() ) {
      return null;
    }

    List<RequestMatcher> requestMatchers = new ArrayList<>();

    collectRequestMatchers( requestMatchers, csrfConfiguration );

    return requestMatchers.size() > 0 ? new OrRequestMatcher( requestMatchers ) : null;
  }

  private static void collectRequestMatchers( Collection<RequestMatcher> requestMatchers,
                                              CsrfConfiguration csrfConfiguration ) {

    Collection<RequestMatcherConfiguration> requestMatcherConfigurations =
      csrfConfiguration.getProtectedRequestMatchers();

    for ( RequestMatcherConfiguration requestMatcherConfiguration : requestMatcherConfigurations ) {
      Collection<String> httpMethods = requestMatcherConfiguration.getMethods();
      if ( httpMethods == null ) {
        requestMatchers.add(
          new RegexRequestMatcher( requestMatcherConfiguration.getPattern(), null, false ) );
      } else {
        for ( String httpMethod : requestMatcherConfiguration.getMethods() ) {
          requestMatchers.add(
            new RegexRequestMatcher( requestMatcherConfiguration.getPattern(), httpMethod, false ) );
        }
      }
    }
  }
  // endregion
}
