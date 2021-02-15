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

package org.hitachivantara.security.web.impl.model.cors;

import org.hitachivantara.security.web.api.model.cors.CorsConfiguration;
import org.hitachivantara.security.web.api.model.cors.CorsRequestSetConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * This class is an immutable implementation of the {@link CorsConfiguration} interface, which uses the same CORS
 * settings for all requests.
 */
public class SimpleCorsConfiguration implements CorsConfiguration {

  @Nonnull
  private final CorsRequestSetConfiguration singleRequestSetConfiguration;

  /**
   * Creates a disabled CORS configuration.
   */
  public SimpleCorsConfiguration() {
    this( null );
  }

  /**
   * Creates a CORS configuration with a given root request set configuration.
   *
   * @param singleRequestSetConfiguration The request set configuration that applies to all requests. When {@code null},
   *                                      a disabled CORS request set configuration is used.
   */
  public SimpleCorsConfiguration( @Nullable CorsRequestSetConfiguration singleRequestSetConfiguration ) {
    this.singleRequestSetConfiguration = singleRequestSetConfiguration != null
      ? singleRequestSetConfiguration
      : CorsRequestSetConfigurationPojo.DISABLED;
  }

  /**
   * @inheritDoc
   */
  @Nonnull @Override
  public CorsRequestSetConfiguration getRootConfiguration() {
    return singleRequestSetConfiguration;
  }

  /**
   * @inheritDoc
   */
  @Nonnull @Override
  public CorsRequestSetConfiguration getRequestConfiguration( @Nonnull HttpServletRequest request ) {
    return singleRequestSetConfiguration;
  }
}
