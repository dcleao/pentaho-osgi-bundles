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

package org.hitachivantara.security.web.api.model.csrf;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * This interface represents a CSRF protection configuration.
 */
public interface CsrfConfiguration {
  /**
   * Gets a value that indicates if CSRF protection is enabled, generally.
   *
   * @return {@code true} if enabled; {@code false}, otherwise.
   */
  boolean isEnabled();

  /**
   * Gets a value that indicates if CSRF protection is enabled for a given HTTP request.
   * <p>
   * If {@link #isEnabled()} returns {@code false}, then this method also returns {@code false}.
   *
   * @param request The HTTP request.
   * @return {@code true} if enabled; {@code false}, otherwise.
   */
  boolean isEnabled( @Nonnull HttpServletRequest request );
}
