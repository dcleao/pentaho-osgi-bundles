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

package org.pentaho.web.security.cors;

import javax.annotation.Nonnull;

/**
 * This interface represents a provider of a CORS configuration.
 */
public interface CorsConfigurationProvider {
  /**
   * Gets the CORS configuration.
   */
  @Nonnull
  CorsConfiguration getCorsConfiguration();

  /**
   * Adds a listener for changes to the CORS configuration.
   *
   * @param listener The CORS configuration listener
   */
  void addCorsConfigurationListener( @Nonnull Runnable listener );
}
