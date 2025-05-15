import org.nlogo.build.{ NetLogoExtension, ExtensionDocumentationPlugin }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name := "gogo"
version := "2.0.9"
isSnapshot := true

Compile / resourceDirectory := { baseDirectory.value / "resources" }

Compile / javaSource := baseDirectory.value / "src"
javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path", "-encoding", "us-ascii", "--release", "11")

netLogoVersion      := "7.0.0-beta1-c8d671e"
netLogoClassManager := "gogohid.extension.HIDGogoExtension"

netLogoPackageExtras ++= Seq(
  (baseDirectory.value / "lib" / "hid4java-develop-SNAPSHOT.jar", None)
)

libraryDependencies ++= Seq(
  // Required by hid4java
  "net.java.dev.jna" % "jna" % "5.6.0"
)
