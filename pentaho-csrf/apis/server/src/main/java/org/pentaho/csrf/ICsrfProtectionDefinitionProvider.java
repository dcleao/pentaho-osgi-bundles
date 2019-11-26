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

package org.pentaho.csrf;

import java.util.Collection;
import java.util.Set;

/**
 * This interface represents a provider of CSRF protection definitions.
 */
public interface ICsrfProtectionDefinitionProvider {

  /**
   * Gets the collection of CSRF protection definitions.
   *
   * The collection may be empty.
   */
  Collection<CsrfProtectionDefinition> getProtectionDefinitions();

  /**
   * Adds a listener for changes to the list of protections definitions.
   */
  void addListener( ICsrfProtectionDefinitionListener listener );

  /**
   * Gets the set of allowed origins which can request a token
   * via the CORS protocol.
   *
   * The collection may be empty, in which case CORS requests from different origins
   * will be disallowed.
   */
  Set<String> getCorsAllowOrigins();
}
