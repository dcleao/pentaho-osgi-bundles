package org.pentaho.csrf;

/**
 * This interface represents a listener of changes
 * to CSRF protection definitions of a certain
 * {@link ICsrfProtectionDefinitionProvider }.
 */
public interface ICsrfProtectionDefinitionListener {
  /**
   * Called when the protections definitions of a
   * {@link ICsrfProtectionDefinitionProvider } have changed.
   */
  void onDefinitionsChanged();
}
