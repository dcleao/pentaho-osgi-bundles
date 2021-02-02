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

package org.pentaho.platform.web.csrf.pentaho;

import org.pentaho.web.security.csrf.CsrfConfigurationProvider;

/**
 * This interface represents a provider of CSRF protection definitions.
 */
public interface PentahoCsrfProtection extends CsrfConfigurationProvider {

  /**
   * Gets a value that indicates if CSRF protection is globally enabled for
   * the Pentaho server.
   *
   * @return {@code true} if enabled; {@code false}, otherwise.
   */
  boolean isEnabled();

  /**
   * Gets a value that indicates if CSRF protection is enabled for
   * the Pentaho server and a given Pentaho platform plugin.
   *
   * When CSRF protection is globally disabled, this method always returns {@code false}.
   *
   * @param pluginId - The identifier of the Pentaho platform plugin.
   *
   * @return {@code true} if enabled for the server and the plugin; {@code false}, otherwise.
   */
  boolean isEnabled( String pluginId );

  /**
   * Gets a value that indicates if the CSRF token can be obtained
   * from different origins, via CORS requests.
   *
   * @return {@code true} if yes; {@code false}, otherwise.
   * @see CsrfConfigurationProvider#getCorsConfiguration()
   */
  boolean isCorsAllowed();
}
