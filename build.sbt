name := "LSWidgets"

scalaVersion := "2.9.3"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xfatal-warnings",
  "-encoding", "UTF8")

val jarName = "LSWidgets.jar"

artifactName := { (_, _, _) => jarName }

libraryDependencies ++= Seq(
  "org.nlogo" % "NetLogo" % "5.1.0" from
    "http://ccl.northwestern.edu/netlogo/5.1.0/NetLogo.jar"
)

packageBin in Compile <<= (packageBin in Compile, baseDirectory) map {
  (jar, base) =>
  IO.copyFile(jar, base / jarName)
  jar
}

cleanFiles <++= baseDirectory { base => Seq(base / jarName) }
