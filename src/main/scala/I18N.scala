package org.levelspace

import java.util.{ Properties, ResourceBundle, MissingResourceException }

object I18N {
  private val bundle = ResourceBundle.getBundle("XW_LS_Strings")
  def get(key: String): String = try {
    bundle.getString(key)
  } catch {
    case e: MissingResourceException => key
  }
}

object Config {
  private val properties = new Properties()

  properties.load(getClass.getClassLoader.getResource("XW_LS_Configuration.properties").openStream)

  def get(key: String): Option[String] =
    Option(properties.get(key)).flatMap {
      case s: String => Some(s)
      case _         => None
    }
}
