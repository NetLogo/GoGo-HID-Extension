val baseSettings = Seq(
  scalaVersion := "2.12.0",
  netLogoVersion := "6.0.4",
  resourceDirectory in Compile := { baseDirectory.value / "resources" },
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings",
                      "-encoding", "us-ascii"),
  javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path",
                      "-encoding", "us-ascii"))

lazy val root =
  project.in(file(".")).
    aggregate(extension, daemon).
    enablePlugins(org.nlogo.build.ExtensionDocumentationPlugin).
    settings(
      packageBin in Compile := {
        val extensionJar    = (packageBin in Compile in extension).value
        val daemonJar       = (packageBin in Compile in daemon).value
        val copyFiles       = Seq(
          extensionJar    -> baseDirectory.value / extensionJar.getName,
          daemonJar       -> baseDirectory.value / daemonJar.getName)
        IO.copy(sources = copyFiles)
        IO.copyDirectory((unmanagedBase in Compile in daemon).value, baseDirectory.value)
        baseDirectory.value
      },
      cleanFiles ++= (baseDirectory.value * "*.jar" +++ baseDirectory.value * "*.pack.gz").get
    )

lazy val extension = project.
  enablePlugins(org.nlogo.build.NetLogoExtension).
  settings(baseSettings).
  settings(
    javaSource in Compile := baseDirectory.value.getParentFile / "src" / "extension" / "gogoHID",
    name := "gogo",
    version := "2.0.1",
    netLogoClassManager := "gogoHID.extension.HIDGogoExtension",
    netLogoZipSources := false)

lazy val daemon = project.
  settings(baseSettings).
  settings(
    name := "gogo-daemon",
    javaSource in Compile := baseDirectory.value.getParentFile / "src" / "daemon" / "gogoHID",
    artifactName := { (_, _, _) => "gogo-daemon.jar" }
  )

netLogoVersion := "6.0.4"
