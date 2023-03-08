import org.nlogo.build.{ NetLogoExtension, ExtensionDocumentationPlugin }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name := "gogo"
version := "2.0.8"
isSnapshot := true

Compile / resourceDirectory := { baseDirectory.value / "resources" }

Compile / javaSource := baseDirectory.value / "src"
javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path", "-encoding", "us-ascii", "--release", "11")

netLogoVersion := "6.3.0"
netLogoClassManager := "gogohid.extension.HIDGogoExtension"

libraryDependencies ++= Seq(
  // if the `hid4java` version changes, make sure to update the jar path in `HIDGogoExtension`, too.
  "org.hid4java" % "hid4java" % "0.7.0"
)
