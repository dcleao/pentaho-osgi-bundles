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

package org.hitachivantara.security.web.impl.service.cors.servlet;

import org.hitachivantara.security.web.api.model.cors.CorsConfiguration;
import org.hitachivantara.security.web.impl.model.spring.CorsConfigurationSourceAdapter;
import org.springframework.web.filter.CorsFilter;

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
 * A Servlet filter that handles cross-origin requests according to the CORS protocol.
 * <p>
 * This class is a convenience, thin wrapper around Spring's {@link CorsFilter} that mostly adapts the configuration
 * format and that only calls the inner filter when CORS is enabled at the root level.
 * <p>
 * If desired, Spring's {@code CorsProtocolFilter} can be used directly, even when CORS is disabled. You can use the
 * adapter {@link CorsConfigurationSourceAdapter} to adapt between the configuration formats.
 * <p>
 * For CORS to function properly, the CORS filter must be placed <em>before</em> any authentication filters.
 * This is because pre-flight requests to the target URLs, which use an {@code OPTIONS} method and an
 * {@code Access-Control-Request-Method} header don't contain authentication as per the CORS specification.
 * If the CORS filter is not before the authentication filter, the preflight requets will fail with a {@code 401}
 * HTTP status error.
 */
public class CorsProtocolFilter implements Filter {

  @Nonnull
  private final CorsFilter innerFilter;

  @Nonnull
  private final CorsConfiguration configuration;

  public CorsProtocolFilter( @Nonnull CorsConfiguration configuration ) {
    Objects.requireNonNull( configuration );

    this.configuration = configuration;
    this.innerFilter = new CorsFilter( new CorsConfigurationSourceAdapter( configuration ) );
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
