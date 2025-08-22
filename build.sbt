import org.nlogo.build.{ NetLogoExtension, ExtensionDocumentationPlugin }

enablePlugins(NetLogoExtension, ExtensionDocumentationPlugin)

name := "gogo"
version := "2.1.0"
isSnapshot := true

Compile / resourceDirectory := { baseDirectory.value / "resources" }

Compile / javaSource := baseDirectory.value / "src"
javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path", "-encoding", "us-ascii", "--release", "11")

netLogoVersion      := "7.0.0-beta2-7e8f7a4"
netLogoClassManager := "gogohid.extension.HIDGogoExtension"

netLogoPackageExtras ++= Seq(
  (baseDirectory.value / "lib" / "hid4java-develop-SNAPSHOT.jar", None)
)

libraryDependencies ++= Seq(
  // Required by hid4java
  "net.java.dev.jna" % "jna" % "5.6.0"
)
