/*!
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
package org.pentaho.platform.web.csrf.pentaho.impl;

import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.pentaho.web.security.csrf.CsrfConfiguration;
import org.pentaho.platform.web.csrf.pentaho.PentahoCsrfProtection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { PentahoSystem.class, CsrfUtil.class } )
public class CsrfProtectionSystemListenerTest {

  @Mock
  private ISystemSettings systemSettings;
  @Mock
  private IPentahoSession session;
  @Mock
  private PentahoCsrfProtection pentahoCsrfProtection;

  private CsrfProtectionSystemListener csrfProtectionSystemListener;

  @Before
  public void setUp() {
    csrfProtectionSystemListener = new CsrfProtectionSystemListener( pentahoCsrfProtection );
  }

  @Test
  public void testStartupCsrfProtectionDisabled() {
    PowerMockito.mockStatic( PentahoSystem.class );
    when( PentahoSystem.registerReference( any(), eq( CsrfConfiguration.class ) ) ).thenReturn( null );
    when( pentahoCsrfProtection.isEnabled() ).thenReturn( false );

    boolean result = csrfProtectionSystemListener.startup( session );

    assertTrue( result );

    PowerMockito.verifyStatic( times( 0 ) );
    PentahoSystem.registerReference( any() );
  }

  @Test
  public void testStartupCsrfProtectionEnabledButNoCsrfProtectionElement() {
    PowerMockito.mockStatic( PentahoSystem.class );

    when( PentahoSystem.registerReference( any(), eq( CsrfConfiguration.class ) ) ).thenReturn( null );
    when( pentahoCsrfProtection.isEnabled() ).thenReturn( false );
    when( PentahoSystem.getSystemSettings() ).thenReturn( systemSettings );
    when( systemSettings.getSystemSettings( eq( CsrfUtil.CSRF_PROTECTION_ELEMENT ) ) ).thenReturn( new ArrayList() );

    boolean result = csrfProtectionSystemListener.startup( session );

    assertTrue( result );

    PowerMockito.verifyStatic( times( 0 ) );
    PentahoSystem.registerReference( any(), eq( CsrfConfiguration.class ) );
  }

  @Test
  public void testStartupCsrfProtectionEnabledAndOneCsrfProtectionElement() {
    PowerMockito.mockStatic( PentahoSystem.class );
    PowerMockito.mockStatic( CsrfUtil.class );

    when( PentahoSystem.registerReference( any(), eq( CsrfConfiguration.class ) ) ).thenReturn( null );
    when( pentahoCsrfProtection.isEnabled() ).thenReturn( true );
    when( PentahoSystem.getSystemSettings() ).thenReturn( systemSettings );

    Element protectionElem = Mockito.mock( Element.class );
    List<Element> csrfProtectionElems = new ArrayList<>();
    csrfProtectionElems.add( protectionElem );

    when( systemSettings.getSystemSettings( eq( CsrfUtil.CSRF_PROTECTION_ELEMENT ) ) ).thenReturn( csrfProtectionElems );

    CsrfConfiguration csrfConfiguration = Mockito.mock( CsrfConfiguration.class );

    when( CsrfUtil.parseXmlCsrfProtectionDefinition( eq( protectionElem ) ) ).thenReturn( csrfConfiguration );

    boolean result = csrfProtectionSystemListener.startup( session );

    assertTrue( result );

    PowerMockito.verifyStatic( times( 1 ) );
    CsrfUtil.parseXmlCsrfProtectionDefinition( eq( protectionElem ) );

    PowerMockito.verifyStatic( times( 1 ) );
    PentahoSystem.registerReference( any(), eq( CsrfConfiguration.class ) );
  }

  @Test
  public void testStartupCsrfProtectionEnabledAndOneInvalidCsrfProtectionElement() {
    PowerMockito.mockStatic( PentahoSystem.class );
    PowerMockito.mockStatic( CsrfUtil.class );

    when( PentahoSystem.registerReference( any(), eq( CsrfConfiguration.class ) ) ).thenReturn( null );
    when( pentahoCsrfProtection.isEnabled() ).thenReturn( true );
    when( PentahoSystem.getSystemSettings() ).thenReturn( systemSettings );

    Element protectionElem = Mockito.mock( Element.class );
    List<Element> csrfProtectionElems = new ArrayList<>();
    csrfProtectionElems.add( protectionElem );

    when( systemSettings.getSystemSettings( eq( CsrfUtil.CSRF_PROTECTION_ELEMENT ) ) ).thenReturn( csrfProtectionElems );

    when( CsrfUtil.parseXmlCsrfProtectionDefinition( eq( protectionElem ) ) )
        .thenThrow( new IllegalArgumentException( "TEST" ) );

    boolean result = csrfProtectionSystemListener.startup( session );

    assertTrue( result );

    PowerMockito.verifyStatic( times( 1 ) );
    CsrfUtil.parseXmlCsrfProtectionDefinition( eq( protectionElem ) );

    PowerMockito.verifyStatic( times( 0 ) );
    PentahoSystem.registerReference( any(), eq( CsrfConfiguration.class ) );
  }

  @Test
  public void testShutdown() {
    // TODO: test this
    csrfProtectionSystemListener.shutdown();
  }
}

