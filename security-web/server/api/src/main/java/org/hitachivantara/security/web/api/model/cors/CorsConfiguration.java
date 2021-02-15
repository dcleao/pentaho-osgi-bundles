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

package org.hitachivantara.security.web.api.model.cors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * This interface represents a CORS configuration.
 */
public interface CorsConfiguration {
  /**
   * Gets a value that indicates if CORS requests are enabled at the root level.
   * <p>
   * This method is equivalent to calling {@link CorsRequestSetConfiguration#isEnabled()}
   * on the result of {@link #getRootConfiguration()}.
   *
   * @return {@code true} if enabled; {@code false}, otherwise.
   */
  default boolean isEnabled() {
    return getRootConfiguration().isEnabled();
  }

  /**
   * Gets the root CORS configuration.
   *
   * @return The root CORS configuration.
   */
  @Nonnull
  CorsRequestSetConfiguration getRootConfiguration();

  /**
   * Gets the effective CORS configuration applicable to a given HTTP request.
   *
   * If the root configuration does <em>not apply</em> to the given request,
   * then {@code null} is returned. Otherwise, the most-specific configuration is tentatively returned.
   * However, if a disabled ancestor configuration exists along the way, that configuration is returned instead.
   *
   * The effective CORS request set configuration is enabled
   * if all of its parent request sets are enabled.
   *
   * For properties of type {@link java.util.Set}, the values are merged, when set.
   *
   * For other properties of scalar types,
   * {@link CorsRequestSetConfiguration#getMaxAge()} and
   * {@link CorsRequestSetConfiguration#getAllowCredentials()},
   * the value of the child request set is used, when set.
   *
   * @param request The HTTP request.
   * @return The CORS configuration or {@code null}.
   */
  @Nullable
  CorsRequestSetConfiguration getRequestConfiguration( @Nonnull HttpServletRequest request );
}
