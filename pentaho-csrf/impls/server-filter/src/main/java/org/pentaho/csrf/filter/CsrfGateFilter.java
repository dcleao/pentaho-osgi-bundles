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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.csrf.filter;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.pentaho.csrf.CsrfProtectionDefinition;

import org.pentaho.csrf.ICsrfProtectionDefinitionProvider;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Collection;

@SuppressWarnings( "PackageAccessibility" )
public class CsrfGateFilter implements Filter {

  private static final Log logger = LogFactory.getLog( CsrfGateFilter.class );

  private CsrfFilter innerCsrfFilter;
  private boolean isCsrfProtectionEnabled = true;
  private boolean initialized = false;
  private ICsrfProtectionDefinitionProvider csrfProtectionDefinitionProvider;

  public CsrfGateFilter( CsrfTokenRepository csrfTokenRepository, ICsrfProtectionDefinitionProvider csrfProtectionDefinitionProvider ) {

    this.innerCsrfFilter = new CsrfFilter( csrfTokenRepository );

    if ( csrfProtectionDefinitionProvider == null ) {
      throw new IllegalArgumentException( "csrfProtectionDefinitionProvider" );
    }

    this.csrfProtectionDefinitionProvider = csrfProtectionDefinitionProvider;
  }

  @VisibleForTesting
  boolean getIsCsrfProtectionEnabled() {
    return isCsrfProtectionEnabled;
  }

  @VisibleForTesting
  CsrfFilter getInnerCsrfFilter() {
    return this.innerCsrfFilter;
  }

  @VisibleForTesting
  boolean getInitialized() {
    return this.initialized;
  }

  @VisibleForTesting
  void setInitialized( boolean initialized ) {
    this.initialized = initialized;
  }

  /**
   * Specifies an {@link AccessDeniedHandler} that should be used when CSRF protection fails.
   *
   * @param accessDeniedHandler the {@link AccessDeniedHandler} to use
   */
  public void setAccessDeniedHandler( AccessDeniedHandler accessDeniedHandler ) {
    this.innerCsrfFilter.setAccessDeniedHandler( accessDeniedHandler );
  }

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {

    this.innerCsrfFilter.init( filterConfig );

    this.csrfProtectionDefinitionProvider.addListener( () -> {
      CsrfGateFilter.this.initialized = false;
      CsrfGateFilter.this.doInit();
    } );

    this.doInit();
  }

  private synchronized void doInit() {
    if ( logger.isDebugEnabled() ) {
      logger.debug( "CsrfGateFilter.init" );
    }

    if ( this.initialized ) {
      return;
    }

    Collection<CsrfProtectionDefinition> csrfProtectionDefinitions =
      this.csrfProtectionDefinitionProvider.getProtectionDefinitions();

    boolean isCsrfProtectionEnabled = csrfProtectionDefinitions != null && csrfProtectionDefinitions.size() > 0;
    if ( isCsrfProtectionEnabled ) {
      RequestMatcher requestMatcher = CsrfUtil.buildCsrfRequestMatcher( csrfProtectionDefinitions );
      if ( requestMatcher == null ) {
        isCsrfProtectionEnabled = false;
      } else {
        this.innerCsrfFilter.setRequireCsrfProtectionMatcher( requestMatcher );
      }
    }

    this.isCsrfProtectionEnabled = isCsrfProtectionEnabled;
    this.initialized = true;
  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
      throws IOException, ServletException {

    if ( !this.initialized ) {
      doInit();
    }

    if ( this.isCsrfProtectionEnabled ) {
      this.innerCsrfFilter.doFilter( servletRequest, servletResponse, filterChain );
    } else {
      filterChain.doFilter( servletRequest, servletResponse );
    }
  }

  @Override
  public void destroy() {
    this.innerCsrfFilter.destroy();
  }
}
