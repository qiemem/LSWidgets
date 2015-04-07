name := "LSWidgets"

scalaVersion := "2.9.3"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xfatal-warnings",
  "-encoding", "UTF8")

val jarName = "LSWidgets.jar"

artifactName := { (_, _, _) => jarName }

retrieveManaged := true

libraryDependencies ++= Seq(
  "org.nlogo" % "NetLogo" % "5.1.0" from
    "http://ccl.northwestern.edu/netlogo/5.1.0/NetLogo.jar",
  "com.miglayout" % "miglayout-swing" % "4.2",
  "com.miglayout" % "miglayout-core" % "4.2"
)

packageBin in Compile <<= (packageBin in Compile, baseDirectory, dependencyClasspath in Runtime) map {
  (jar, base, classpath) =>
  IO.copyFile(jar, base / jarName)
  val libraryJarPaths =
    classpath.files.filter{path =>
      path.getName.endsWith(".jar") &&
      !path.getName.startsWith("scala-library")}
  for(path <- libraryJarPaths) {
    IO.copyFile(path, base / path.getName)
  }
  jar
}

cleanFiles <++= baseDirectory { base => Seq(base / jarName) }
