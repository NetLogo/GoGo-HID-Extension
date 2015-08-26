val baseSettings = Seq(
  scalaVersion := "2.11.6",
  resourceDirectory in Compile := { baseDirectory.value / "resources" },
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xlint", "-Xfatal-warnings",
                      "-encoding", "us-ascii"),
  javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path",
                      "-encoding", "us-ascii"),
  libraryDependencies += "org.nlogo" % "NetLogo" % "5.3.0" from
      Option(System.getProperty("netlogo.jar.url")).getOrElse("http://ccl.northwestern.edu/netlogo/5.3.0/NetLogo.jar"),
  unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "common"
)

lazy val packedJar = taskKey[File]("packed jar")

lazy val root =
  project.in(file(".")).
    aggregate(extension, daemon).
    settings(
      packageBin in Compile := {
        val extensionPacked = (packedJar in Compile in extension).value
        val extensionJar    = (packageBin in Compile in extension).value
        val daemonJar       = (packageBin in Compile in daemon).value
        val copyFiles       = Seq(
          extensionPacked -> baseDirectory.value / extensionPacked.getName,
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
    javaSource in Compile := baseDirectory.value / "extension" / "gogoHID",
    name := "gogo",
    netLogoExtName := "gogohid",
    netLogoClassManager := "gogoHID.extension.HIDGogoExtension",
    netLogoZipSources := false,
    packedJar in Compile := {
      val jar = (packageBin in Compile).value
      val packFile = jar.getParentFile / "gogo.jar.pack.gz"
      val options = Seq(
        "modification-time"  -> "latest",
        "effort"             -> "9",
        "strip-debug"        -> "",
        "no-keep-file-order" -> "",
        "unknown-attribute"  -> "strip")
      Pack.pack(jar, packFile, options)
      packFile
    }
  )

lazy val daemon = project.
  settings(baseSettings).
  settings(
    name := "gogo-daemon",
    javaSource in Compile := baseDirectory.value / "daemon" / "gogoHID",
    artifactName := { (_, _, _) => "gogo-daemon.jar" }
  )


