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

package org.hitachivantara.security.web.impl.model.csrf;

import org.hitachivantara.security.web.api.model.csrf.CsrfConfiguration;
import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * This class is an immutable implementation of the {@link CsrfConfiguration} interface,
 * which evaluates the requests that require CSRF protection based on a given request matcher.
 */
public class SimpleCsrfConfiguration implements CsrfConfiguration {

  private final boolean isEnabled;

  @Nonnull
  private final RequestMatcher requestMatcher;

  /**
   * Creates a disabled CSRF configuration.
   *
   * The request matcher will have value {@link RequestMatcher#NONE}.
   */
  public SimpleCsrfConfiguration() {
    this( false, null );
  }

  /**
   * Creates a CSRF configuration for a given request matcher.
   *
   * If the given request matcher is {@code null}, then the configuration
   * will be disabled and the request matcher is defaulted to {@link RequestMatcher#NONE}.
   *
   * @param requestMatcher The request matcher which identifies the protected requests.
   */
  public SimpleCsrfConfiguration( RequestMatcher requestMatcher ) {
    this( requestMatcher != null, requestMatcher );
  }

  /**
   * Creates a CSRF configuration with a given enabled status and for a given request matcher.
   *
   * @param isEnabled Indicates if CSRF protection is enabled for the requests identified by {@code requestMatcher}.
   * @param requestMatcher The request matcher which identifies protected requests.
   */
  public SimpleCsrfConfiguration( boolean isEnabled, RequestMatcher requestMatcher ) {
    this.isEnabled = isEnabled;
    this.requestMatcher = requestMatcher != null ? requestMatcher : RequestMatcher.NONE;
  }

  /**
   * @inheritDoc
   */
  @Override
  public final boolean isEnabled() {
    return isEnabled;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean isEnabled( @Nonnull HttpServletRequest request ) {
    return requestMatcher.test( request );
  }
}
