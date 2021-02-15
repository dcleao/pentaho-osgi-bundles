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

import org.hitachivantara.security.web.api.model.csrf.CsrfRequestSetConfiguration;
import org.hitachivantara.security.web.api.model.matcher.RequestMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This class is a POJO implementation of the
 * {@link org.hitachivantara.security.web.api.model.csrf.CsrfRequestSetConfiguration}
 * interface.
 * <p>
 * Additionally, this implementation is proxy-safe.
 */
public class CsrfRequestSetConfigurationPojo implements CsrfRequestSetConfiguration {

  @Nonnull
  private RequestMatcher requestMatcher;

  /**
   * Creates an initially empty CSRF protected request set.
   * <p>
   * No endpoints will be protected; the value of {@link #getRequestMatcher()} will be {@link RequestMatcher#NONE}.
   */
  public CsrfRequestSetConfigurationPojo() {
    this( null );
  }

  /**
   * Creates a CSRF protected request set for a given request matcher.
   *
   * @param requestMatcher The request matcher which identifies the protected requests. If {@code null}, then the value
   *                       will be {@link RequestMatcher#NONE}.
   */
  public CsrfRequestSetConfigurationPojo( RequestMatcher requestMatcher ) {
    this.requestMatcher = requestMatcher != null ? requestMatcher : RequestMatcher.NONE;

  }

  /**
   * @inheritDoc
   */
  @Override
  @Nonnull
  public final RequestMatcher getRequestMatcher() {
    return requestMatcher;
  }

  /**
   * Sets the request matcher that identifies requests in the set.
   *
   * @param requestMatcher The request matcher which identifies the protected requests. If {@code null}, then the value
   *                       will be {@link RequestMatcher#NONE}.
   */
  public final void setRequestMatcher( @Nullable RequestMatcher requestMatcher ) {
    this.requestMatcher = requestMatcher != null ? requestMatcher : RequestMatcher.NONE;
  }

  // Proxy-safe implementation of #equals. Uses instanceof and getters, instead of fields.
  // This is important for the implementation of AggregatedRequestSetCsrfConfiguration#requestSetConfigurationWillUnbind,
  // to be able to remove a proxy from a list...
  @Override
  public boolean equals( Object other ) {
    if ( this == other ) {
      return true;
    }

    if ( !( other instanceof CsrfRequestSetConfiguration ) ) {
      return false;
    }

    CsrfRequestSetConfiguration that = (CsrfRequestSetConfiguration) other;
    return getRequestMatcher().equals( that.getRequestMatcher() );
  }

  @Override
  public int hashCode() {
    return Objects.hash( getRequestMatcher() );
  }
}
