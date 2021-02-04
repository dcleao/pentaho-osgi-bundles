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

package org.hitachivantara.web.security.api.cors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a configuration of CORS.
 */
public class CorsConfiguration {

  private final boolean isEnabled;

  @Nonnull
  private final Set<String> allowedOriginsSet;

  /**
   * Creates a disabled CORS configuration.
   */
  public CorsConfiguration() {
    this( null );
  }

  /**
   * Creates a CORS configuration.
   *
   * @param allowedOriginsSet A set of CORS allowed origins.
   *                          When {@code null}, the configuration will be disabled.
   */
  public CorsConfiguration( @Nullable Set<String> allowedOriginsSet ) {
    this.isEnabled = allowedOriginsSet != null && !allowedOriginsSet.isEmpty();
    this.allowedOriginsSet = this.isEnabled
      ? Collections.unmodifiableSet( allowedOriginsSet )
      : new HashSet<>();
  }

  /**
   * Gets a value that indicates if CORS requests are enabled,
   * for origins contained in {@link #getAllowedOrigins()}.
   *
   * @return {@code true} if yes; {@code false}, otherwise.
   */
  public final boolean isEnabled() {
    return isEnabled;
  }

  /**
   * Gets the set of allowed external origins.
   * <p>
   * The collection may be empty,
   * in which case CORS requests from all different origins will be disallowed.
   */
  @Nonnull
  public final Set<String> getAllowedOrigins() {
    return allowedOriginsSet;
  }
}
