resolvers      += "netlogo" at "https://dl.cloudsmith.io/public/netlogo/netlogo/maven/"

netLogoVersion := "6.2.0-d27b502"

resourceDirectory in Compile := { baseDirectory.value / "resources" }

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path",
                     "-encoding", "us-ascii")

enablePlugins(org.nlogo.build.ExtensionDocumentationPlugin)
enablePlugins(org.nlogo.build.NetLogoExtension)

javaSource in Compile := baseDirectory.value / "src"

name := "gogo"

version := "2.0.5"

netLogoClassManager := "gogohid.extension.HIDGogoExtension"

netLogoTarget := NetLogoExtension.directoryTarget(baseDirectory.value)

netLogoZipSources := false

libraryDependencies ++= Seq(
  // if the `hid4java` version changes, make sure to update the jar path in `HIDGogoExtension`, too.
  "org.hid4java" % "hid4java" % "0.7.0"
)
