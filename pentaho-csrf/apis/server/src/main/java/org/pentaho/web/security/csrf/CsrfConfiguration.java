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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.web.security.csrf;

import org.pentaho.web.security.RequestMatcherConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * This class represents a configuration of the protection against CSRF attacks.
 * <p>
 * Essentially, consists of a set of protected request types (endpoint and method),
 * each represented by a {@link RequestMatcherConfiguration}.
 */
public class CsrfConfiguration {

  private final boolean isEnabled;

  @Nonnull
  private final Collection<RequestMatcherConfiguration> protectedRequestMatchers;

  public CsrfConfiguration() {
    this( null );
  }

  public CsrfConfiguration( @Nullable Collection<RequestMatcherConfiguration> protectedRequestMatchers ) {
    this.isEnabled = protectedRequestMatchers != null && !protectedRequestMatchers.isEmpty();
    this.protectedRequestMatchers = this.isEnabled
      ? Collections.unmodifiableCollection( protectedRequestMatchers )
      : Collections.emptyList();
  }

  /**
   * Gets a value that indicates if CSRF protection is enabled.
   *
   * @return {@code true} if enabled; {@code false}, otherwise.
   */
  public final boolean isEnabled() {
    return isEnabled;
  }

  /**
   * Gets the collection of request matcher configurations that identify CSRF protected endpoints.
   *
   * @return A collection of request matcher configurations.
   */
  @Nonnull
  public final Collection<RequestMatcherConfiguration> getProtectedRequestMatchers() {
    return protectedRequestMatchers;
  }
}
