package org.levelspace

import java.util.{ ResourceBundle, MissingResourceException }

object I18N {
  private val bundle = ResourceBundle.getBundle("XW_LS_Strings")
  def get(key: String) = try {
    bundle.getString(key)
  } catch {
    case e: MissingResourceException => key
  }
}
