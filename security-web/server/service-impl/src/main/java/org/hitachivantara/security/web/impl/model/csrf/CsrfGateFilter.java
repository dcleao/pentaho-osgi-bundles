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
package org.hitachivantara.security.web.impl.model.csrf;

import org.hitachivantara.security.web.api.model.csrf.CsrfConfiguration;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Objects;

public class CsrfGateFilter implements Filter {

  private final CsrfFilter innerFilter;

  public CsrfGateFilter( CsrfTokenRepository csrfTokenRepository, CsrfConfiguration configuration ) {

    Objects.requireNonNull( configuration );

    this.innerFilter = new CsrfFilter( csrfTokenRepository );
    this.innerFilter.setRequireCsrfProtectionMatcher( configuration::isEnabled );
  }

  // Testing support
  CsrfFilter getInnerFilter() {
    return innerFilter;
  }

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
  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {
    innerFilter.doFilter( servletRequest, servletResponse, filterChain );
  }

  @Override
  public void destroy() {
    innerFilter.destroy();
  }
}
